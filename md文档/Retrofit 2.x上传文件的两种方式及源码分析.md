# Retrofit 2.x上传文件的两种方式及源码分析

[古月有三木](https://juejin.cn/user/1714893872434862/posts)

2020-11-02538阅读4分钟

## A 使用@Part注解

```java
java复制代码@Multipart
@POST("update/img")
Call<ResponseBody> postImg(@Part("param1") RequestBody param1, @Part MultipartBody.Part img);
```

构造请求数据时采用如下方式：

```java
java复制代码RequestBody requestFile = RequestBody.create(img, MediaType.parse("image/jpeg"));
// MultipartBody.Part is used to send also the actual filename
MultipartBody.Part img = MultipartBody.Part.createFormData("img", img.getName(), requestFile);

RequestBody param1 = RequestBody.create("param1", null);

Call<ResponseBody> call = service.postImg(param1, body);
```

如果是多文件：

```java
java复制代码@Multipart
@POST("update/img")
Call<ResponseBody> postImg(@Part("param1") RequestBody param1, @Part MultipartBody.Part[] imgs);
// Call<ResponseBody> postImg(@Part("param1") RequestBody param1, @Part List<MultipartBody.Part> imgs);
```

构造请求数据时传数组/List即可。

## B 使用@Body注解

```java
java复制代码@POST("update/img")
Call<ResponseBody> postImg(@Body RequestBody requestBody);
```

**注意**：此处不能添加`@Multipart`或`@FormUrlEncoded`注解，不然会报错。

构造请求数据时采用如下方式：

```java
java复制代码MultipartBody.Builder builder = new MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("param1","param1")
    	.addFormDataPart("img", img.getName(), RequestBody.create(img, MediaType.parse("image/jpeg")));

postImg(builder.build());
```

如果是多文件，接口注解不变，在添加文件时在继续执行`addFormDataPart`即可。

实际应用中，两种方式效果完全相同，那么，这两种方式在实现上有什么区别吗？

## 源码分析

`Retrofit`解析注解的步骤如下：

1. 解析方法注解；
2. 解析参数和参数注解。

`RequestFactory`类中解析方法注解部分代码如下：

```java
java复制代码private void parseMethodAnnotation(Annotation annotation) {
  ...
  } else if (annotation instanceof Multipart) {
    if (isFormEncoded) {
      throw methodError(method, "Only one encoding annotation is allowed.");
    }
    isMultipart = true;
  ...
}
```

观察代码，发现如果方法注解中含有`Multipart`，则另`isMultipart`为`true`。

下面我们来观察`RequestFactory`类中解析参数注解的部分代码：

1. 对`@Part`的解析

   首先会根据`isMultipart`判断方法是否含有`Multipart`注解，没有注解就报错：

   ```java
   java复制代码if (!isMultipart) {
     throw parameterError(
         method, p, "@Part parameters can only be used with multipart encoding.");
   }
   ```

   然后得到`@Part`的值，对于`@Part`的值有以下几种可能。

   - `@Part`值为空，此时认为参数类型必须为`Multipart.body`、`Multipart.body`数组或`Iterable`类型，如果不为以上三者，则报异常：

     ```java
     java复制代码throw parameterError(
           method,
           p,
           "@Part annotation must supply a name or use MultipartBody.Part parameter type.");
     ```

   - `@Part`值为空，参数类型为`Multipart.body`:

     ```java
     java复制代码if (MultipartBody.Part.class.isAssignableFrom(rawParameterType)) {
        return ParameterHandler.RawPart.INSTANCE;
     }
     ```

   - `@Part`值为空，参数类型为`Iterable`：

     ```java
     java复制代码if (Iterable.class.isAssignableFrom(rawParameterType)) { 
        if (!(type instanceof ParameterizedType)) {
          throw parameterError(
              method,
              p,
              rawParameterType.getSimpleName()
                  + " must include generic type (e.g., "
                  + rawParameterType.getSimpleName()
                  + "<String>)");
        }
        ParameterizedType parameterizedType = (ParameterizedType) type;
        Type iterableType = Utils.getParameterUpperBound(0, parameterizedType); 
        if (!MultipartBody.Part.class.isAssignableFrom(Utils.getRawType(iterableType))) {
          throw parameterError(
              method,
              p,
              "@Part annotation must supply a name or use MultipartBody.Part parameter type.");
        }
        return ParameterHandler.RawPart.INSTANCE.iterable();
     }
     ```

     这里首先判断泛型是否确定类型，然后判断泛型类型是否为`MultipartBody.Part`。

   - `@Part`值为空，参数类型为`Array`：

     ```java
     java复制代码if (rawParameterType.isArray()) {
        Class<?> arrayComponentType = rawParameterType.getComponentType();
        if (!MultipartBody.Part.class.isAssignableFrom(arrayComponentType)) {
          throw parameterError(
              method,
              p,
              "@Part annotation must supply a name or use MultipartBody.Part parameter type.");
        }
        return ParameterHandler.RawPart.INSTANCE.array();
     }
     ```

   - `@Part`值不为空，参数类型为`MultipartBody.Part`，则报错

     ```java
     java复制代码if (MultipartBody.Part.class.isAssignableFrom(rawParameterType)) {
         throw parameterError(
             method,
             p,
             "@Part parameters using the MultipartBody.Part must not "
             + "include a part name in the annotation.");
     } 
     ```

   - `@Part`值不为空，参数类型不为`MultipartBody.Part`。

     ```java
     java复制代码Converter<?, RequestBody> converter =
         retrofit.requestBodyConverter(type, annotations, methodAnnotations);
     return new ParameterHandler.Part<>(method, p, headers, converter);
     ```

2. 对`@PartMap`的解析

   同`@Part`，首先会根据`isMultipart`判断方法是否含有`Multipart`注解，没有注解就报错。

   接着获取参数类型。

   - 判断参数类型是否为`Map`，不是则报错。

     ```java
     java复制代码if (!Map.class.isAssignableFrom(rawParameterType)) {
         throw parameterError(method, p, "@PartMap parameter type must be Map.");
     }
     ```

   - 判断`Map`是否指定泛型类型，未指定则报错。

     ```java
     java复制代码if (!(mapType instanceof ParameterizedType)) {
        throw parameterError(
            method, p, "Map must include generic types (e.g., Map<String, String>)");
     }
     ```

   - 判断`Map`键类型是否为`String`，不是则报错。

     ```java
     java复制代码if (String.class != keyType) {
       throw parameterError(method, p, "@PartMap keys must be of type String: " + keyType);
     }
     ```

   - 判断`Map`值类型是否为`Multipart.body`，是则报错。

     ```java
     java复制代码if (MultipartBody.Part.class.isAssignableFrom(Utils.getRawType(valueType))) {
       throw parameterError(
           method,
           p,
           "@PartMap values cannot be MultipartBody.Part. "
               + "Use @Part List<Part> or a different value type instead.");
     }
     ```

3. 对`@Body`的解析

   首先会判断是否方法注解是否包含`@FormUrlEncoded`和`@Multipart`注解，有则报错。

   ```java
   java复制代码if (isFormEncoded || isMultipart) {
     throw parameterError(
         method, p, "@Body parameters cannot be used with form or multi-part encoding.");
   }
   ```

   然后调用转换器，尝试将数据转换为`RequestBody`。

   ```java
   java复制代码Converter<?, RequestBody> converter;
   try {
     converter = retrofit.requestBodyConverter(type, annotations, methodAnnotations);
   } catch (RuntimeException e) {
     // Wide exception range because factories are user code.
     throw parameterError(method, e, p, "Unable to create @Body converter for %s", type);
   }
   gotBody = true;
   return new ParameterHandler.Body<>(method, p, converter);
   ```

解析成功之后，会将数据传至`ParameterHandler`类进一步解析，下面让我们看看该类做了什么。

1. 对于`@Part`注解，涉及到`ParameterHandler.Part`和`ParameterHandler.RawPart`内部类，其中当`@Part`值不为空时，使用`Part`类解析，否则使用`RawPart`解析。
2. 对于`@PartMap`注解，使用`ParameterHandler.Part`进行解析。
3. 对于`@Body`注解，使用`ParameterHandler.Body`进行解析。

他们都含有一个共同的方法：

```
Part
java复制代码void apply(RequestBuilder builder, @Nullable T value) {
  if (value == null) return; // Skip null values.

  RequestBody body;
  try {
    body = converter.convert(value);
  } catch (IOException e) {
    throw Utils.parameterError(method, p, "Unable to convert " + value + " to RequestBody", e);
  }
  builder.addPart(headers, body);
}
PartMap
java复制代码void apply(RequestBuilder builder, @Nullable Map<String, T> value) throws IOException {
  if (value == null) {
    throw Utils.parameterError(method, p, "Part map was null.");
  }

  for (Map.Entry<String, T> entry : value.entrySet()) {
    String entryKey = entry.getKey();
    if (entryKey == null) {
      throw Utils.parameterError(method, p, "Part map contained null key.");
    }
    T entryValue = entry.getValue();
    if (entryValue == null) {
      throw Utils.parameterError(
          method, p, "Part map contained null value for key '" + entryKey + "'.");
    }

    okhttp3.Headers headers =
        okhttp3.Headers.of(
            "Content-Disposition",
            "form-data; name=\"" + entryKey + "\"",
            "Content-Transfer-Encoding",
            transferEncoding);

    builder.addPart(headers, valueConverter.convert(entryValue));
  }
}
RawPart
java复制代码void apply(RequestBuilder builder, @Nullable MultipartBody.Part value) {
  if (value != null) { // Skip null values.
    builder.addPart(value);
  }
}
Body
java复制代码void apply(RequestBuilder builder, @Nullable T value) {
    if (value == null) {
      throw Utils.parameterError(method, p, "Body parameter value must not be null.");
    }
    RequestBody body;
    try {
      body = converter.convert(value);
    } catch (IOException e) {
      throw Utils.parameterError(method, e, p, "Unable to convert " + value + " to RequestBody");
    }
    builder.setBody(body);
  }
}
```

我们只关系`builder.addPart`和`builder.setBody`方法。

- 查看`addPart`方法源码，该方法在`RequestBuilder`类中：

  ```java
  java复制代码private @Nullable MultipartBody.Builder multipartBuilder;
  
  void addPart(Headers headers, RequestBody body) {
    multipartBuilder.addPart(headers, body);
  }
  
  void addPart(MultipartBody.Part part) {
    multipartBuilder.addPart(part);
  }
  ```

  对于`RawPart`类型，将采用`addPart(MultipartBody.Part part)`方法，对于`Part`类型，将采用`addPart(Headers headers, RequestBody body)`方法。然后，我们查看`MultipartBody.Builder`中的`addPart`方法为：

  ```kotlin
  kotlin复制代码fun addPart(part: Part) = apply {
    parts += part
  }
  ```

- 查看`setBody`源码

  ```java
  java复制代码private @Nullable RequestBody body;
  
  void setBody(RequestBody body) {
    this.body = body;
  }
  ```

- 获得`Request.Builder`方法

  ```java
  java复制代码Request.Builder get() {
    ...
  
    RequestBody body = this.body;
    if (body == null) {
      // Try to pull from one of the builders.
      if (formBuilder != null) {
        body = formBuilder.build();
      } else if (multipartBuilder != null) {
        body = multipartBuilder.build();
      } else if (hasBody) {
        // Body is absent, make an empty body.
        body = RequestBody.create(null, new byte[0]);
      }
    }
  
    ...
  }
  ```

  在该方法中，首先判断`body`是否为空，如果为空则调用`formBuilder`或`multipartBuilder`创建一个`body`。

- 最后，我们再看一下`MultipartBody.Builder`中的`addFormDataPart`方法

  ```java
  java复制代码fun addFormDataPart(name: String, filename: String?, body: RequestBody) = apply {
    addPart(Part.createFormData(name, filename, body))
  }
  
  fun addFormDataPart(name: String, value: String) = apply {
    addPart(Part.createFormData(name, value))
  }
  ```

  可以看出最终还是调用了`addPart`方法。

  ```java
  java复制代码fun addPart(part: Part) = apply {
    parts += part
  }
  ```

分析到这里，我们可以明白，原来两种上传文件方式只是表达方式不同，实际上都会调用`MultipartBody.Builder`的`build`方法创建`RequetBody`，只不过第一种方式创建是交给了`Retrofit`中的`RequestBuilder`类，而第二种是由我们自己调用`MultipartBody.Builder`的`build`方法创建而已。