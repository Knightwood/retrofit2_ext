# retrofit2

## 请求参数注解

| 请求参数注解 | 说明                                                         |
| ------------ | ------------------------------------------------------------ |
| @Body        | 发送非表单数据，多用于Post请求，如果给retrofit2添加了转换器，方法参数里可以直接使用实体类，否则就只能使用RequestBody类型作为方法的参数 |
| @Filed       | 多用于Post方式传递参数，需要结合@FromUrlEncoded使用，即以表单的形式传递参数 |
| @FiledMap    | 多用于Post请求中的表单字段，需要结合@FromUrlEncoded使用      |
| @Part        | 用于表单字段，Part和PartMap与@multipart注解结合使用，适合文件上传的情况 |
| @PartMap     | 用于表单字段，默认接受类型是Map<String,RequestBody>，可用于实现多文件上传 |
| @Path        | 用于Url中的占位符                                            |
| @Query       | 用于Get请求中的参数                                          |
| @QueryMap    | 与Query类似，用于不确定表单参数                              |
| @Url         | 指定请求路径                                                 |

### @Body注解

官方文档解释：

#### Request Body

An object can be specified for use as an HTTP request body with the `@Body` annotation.

```
@POST("users/new")
Call<User> createUser(@Body User user);
```

The object will also be converted using a converter specified on the `Retrofit` instance. If no converter is added, only `RequestBody` can be used.

---

注意事项：

​		如果请求为post实现，那么最好传递参数时使用@Field、@FieldMap和@FormUrlEncoded。因为@Query和或QueryMap都是将参数拼接在url后面的，而@Field或@FieldMap传递的参数时放在请求体的。
使用@Path时，path对应的路径不能包含”/”，否则会将其转化为%2F。在遇到想动态的拼接多节url时，还是使用@Url吧。
@Body标签不能同时和@FormUrlEncoded、@Multipart标签同时使用。否则会报错：@Body parameters cannot be used with form or multi-part encoding.

**@Body**

- 作用：以 `Post`方式 传递 自定义数据类型 给服务器
- 特别注意：如果提交的是一个Map，那么作用相当于 `@Field`

> 不过Map要经过 `FormBody.Builder` 类处理成为符合 Okhttp 格式的表单，如：

```javascript
FormBody.Builder builder = new FormBody.Builder();
builder.add("key","value");
```



# retrofit2的post请求

## 非表单类型

### 参数跟在地址后面，拼接而来，用`@Query`注解

```
http://192.168.43.173/api/trades/{userId}?token={token}

//补全URL,问号后需要加token,post的数据只有一条reason
 @FormUrlEncoded
 @POST("trades/{userId}")
 Call<TradesBean> postResult(
         @Path("userId") String userId,
         @Query("token") String token,
         @Field("reason") String reason;
         )
```

### 放在body里，传`json`,用`@Body`注解





## 表单类型



### 不带文件



### 带文件







