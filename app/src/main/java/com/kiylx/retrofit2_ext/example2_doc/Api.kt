package com.kiylx.retrofit2_ext.example2_doc


import com.kiylx.retrofit2_ext.example2_doc.bean.User
import com.kiylx.retrofit2_ext.example2_doc.bean.User2
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*


/*
方法注解
1. FormUrlEncoded 表示请求体是一个Form表单， content-Type :application/x-www-form-urlencoded

2. MultiPart 表示请求体是一个支持文件上传的Form表单, content-Type :multipart/form-data
方法参数注解:

Field与FieldMap注解 与FormUrlEncoded 注解配合. FieldMap接受类型是Map<String, String>,非String类型会调用toString方法

Part和PartMap注解 与MultiPart 注解配合,适合有文件上传的情况.PartMap接受类型是Map<String, RequestBody>,非RequestBody类型会通过Converter转换

Path, Query, QueryMap, Url,等注解用于url.
Query与QueryMap 和Field与FieldMap注解 功能一样,不同的是Query与QueryMap中的数据体现在url上,而Field与FieldMap注解的数据是请求体,但生成的数据形式一样
注1：{占位符}和PATH尽量只用在URL的path部分，url中的参数使用Query和QueryMap 代替，保证接口定义的简洁
注2：Query、Field和Part这三者都支持数组和实现了Iterable接口的类型，如List，Set等，方便向后台传递数组。



    普通的post请求,方法参数可以直接用使用@Body,参数传bean实例
    @Body                  上传json格式数据，直接传入实体它会自动转为json，这个转化方式是GsonConverterFactory定义的
    @Body注解不能用于表单或者支持文件上传的表单的编码，即不能与@FormUrlEncoded和@Multipart注解同时使用

    不带文件的post[表单请求],方法参数可以使用@Field @FieldMap
    @FormUrlEncoded        请求格式注解，请求实体是一个From表单，每个键值对需要使用@Field注解

    @Field                 请求参数注解，提交请求的表单字段，必须要添加，而且需要配合@FormUrlEncoded使用
    @FieldMap              请求参数注解，与@Field作用一致，用于不确定表单参数

    带文件的或只传文件的[表单请求],方法参数可使用@Part @PartMap
    @Multipart             表示请求实体是一个支持文件上传的表单，需要配合@Part和@PartMap使用，适用于文件上传

    @Part                  用于表单字段，适用于文件上传的情况，@Part支持三种类型：RequestBody、MultipartBody.Part、任意类型
    @PartMap               用于多文件上传， 与@FieldMap和@QueryMap的使用类似
    使用:
    首先声明类型，然后根据类型转化为RequestBody对象，返回RequestBody或者转化为 MultipartBody.Part，
    需要在表单中进行文件上传时，就需要使用该格式：multipart/form-data。


    @Query                        请求参数注解，用于Get请求中的参数


    RequestBody、FormBody、MultipartBody 是okhttp3中的,不是retrofit2包中的类
    RequestBody另一个子类MultipartBody，用于post请求提交复杂类型的请求体。复杂请求体可以同时包含多种类型的的请求体数据。

     */
interface Api {
    //<editor-fold desc="get请求">

    @GET("/get请求的路径")
    fun login(
        @Header("token") token: String,//添加header，也可以在拦截器中读取和修改
    ): Call<User>

    //</editor-fold>

    //<editor-fold desc="post请求-表单">
    //FormUrlEncoded注解:
    //用于修饰Field注解和FieldMap注解
    //使用该注解,表示请求正文将使用表单网址编码。字段应该声明为参数，并用@Field注释或FieldMap注释。
    //使用FormUrlEncoded注解的请求将具”application / x-www-form-urlencoded” MIME类型。字段名称和值将先进行UTF-8进行编码,再根据RFC-3986进行URI编码.
    //也就是常规的，不发送文件的表单请求
    /**
     * post表单请求
     * 仅上传json，不上传文件
     */
    @FormUrlEncoded
    @POST("/post的路径1")
    fun post1(
        @Field("id") id: String,
        @Field("id2") id2: String,
    ): Call<ResponseBody>

    //FieldMap注解:
    //作用于方法的参数
    //用于发送一个表单请求
    //map中每一项的键和值都不能为空,否则抛出IllegalArgumentException异常
    //示例:
    /**
     * 这个post表单请求使用map传参
     */
    @FormUrlEncoded
    @POST("/things")
    fun things(@FieldMap fields: Map<String, String>): Call<ResponseBody>

    //</editor-fold>

    //<editor-fold desc="post请求-json">
    //@Body注解：
    //非表单的post或put请求
    //当你发送一个post或put请求,但是又不想作为请求参数或表单的方式发送请求时,
    //使用该注解定义的参数可以直接传入一个实体类,retrofit会通过convert把该实体序列化并将序列化后的结果直接作为请求体发送出去.
    //示例:
    /**
     * 这是个post json请求
     * 传入 User类 会自动转换，效果等同于[placeUpload2]方法，
     * 但是[placeUpload2]传参会更灵活，因为都早requestbody可以不受现有的entity的结构限制
     *
     */
    @POST("users")
    //on below line we are creating a method to post our data.
    fun placeUpload1(
        @Body dataModal: User,
        @Header("token") token: String = "",
    ): Call<User>

    /**
     * 这也是个post json请求，
     */
    @POST("placeUpload")
    fun placeUpload2(
        @Body body: RequestBody,
        @Header("token") token: String = "",
    ): Call<User>


    //</editor-fold>


    //<editor-fold desc="post请求-带文件的表单">


    //Multipart注解:
    //作用于方法
    //使用该注解,表示请求体是多部分的。 每一部分作为一个参数,且用Part注解声明

    //Part注解:
    //作用于方法的参数,用于定义Multipart请求的每个part
    //使用该注解定义的参数,参数值可以为空,为空时,则忽略
    //使用该注解定义的参数类型有以下3种方式可选:
    //1, 如果类型是okhttp3.MultipartBody.Part，内容将被直接使用。 省略part中的名称,即 @Part  part: MultipartBody.Part
    //2, 如果类型是RequestBody，那么该值将直接与其内容类型一起使用。 在注释中提供part名称（例如，@Part（“foo”）foo: RequestBody ）。
    //3, 其他对象类型将通过使用转换器转换为适当的格式。 在注释中提供part名称（例如，@Part（“foo”） photo: Image）。
    //如果参数类型是MultipartBody.Part，part注解内不能用name，即@Part（“foo”）将会报错

    /**
     * post表单，包含文件和一些字符串
     */
    @Multipart
    @POST("message/addVoice")
    fun uploadVoiceMsg(
        @Part("setUser") id: Int,
        @Part("setUserName") setUserName: String,
        @Part("messageContent") messageContent: String,
        @Part file: MultipartBody.Part?,
        @Header("token") token: String = ""
    ): Call<User2>

    /**
     * Let me explain each part of the definition above.
     *
     * First, you need to declare the entire call as @Multipart request.
     * Let's continue with the annotation for description. The description is just a string value wrapped within a RequestBody instance.
     *
     * Secondly, there’s another @Part within the request: the actual file.
     * We use the MultipartBody.Part class that allows us to send the actual file name besides the binary file data with the request.
     * You’ll see how to create the file object correctly within the following section.
     */
    @Multipart
    @POST("upload")
    fun post3(
        @Part("description") description: RequestBody,//直接把bean序列化成字符串，用requestbody包裹这个字符串
        @Part file: MultipartBody.Part,//对于MultipartBody.Part，前面的part注解里不能放字符串标明字段名称
    ): Call<User2>

    // PartMap注解:
    //作用于方法的参数,以map的方式定义Multipart请求的每个part
    //map中每一项的键和值都不能为空,否则抛出IllegalArgumentException异常
    //使用该注解定义的参数类型有以下2种方式可选:
    //1, 如果类型是RequestBody，那么该值将直接与其内容类型一起使用。
    //2, 其他对象类型将通过使用转换器转换为适当的格式。
    @Multipart
    @POST("/upload")
    fun upload(
        @Part("file") file: RequestBody,
        @PartMap params: Map<String, @JvmSuppressWildcards RequestBody>,
    ): Call<ResponseBody>

    /**
     * part注解可以用在很多种类型上面，retrofit2会自动做转换。
     * 注意方法要加上@Multipart
     */
    @Multipart
    @POST("/post的路径2")
    fun post2(
        @Part part: MultipartBody.Part,//单文件
        @Part files: List<MultipartBody.Part>, //多文件
        //MultipartBody.Part实际上持有了RequestBody。
        //所以，不仅文件可以生成MultipartBody.Part，字符串，json也都可以。
        //但其实这里是retrofit2做了处理，他会把MultipartBody.Part之外的东西进行包装
        @Part part2: RequestBody,
        @Part bean: User,//是的，实体也行，只要转换器能转换即可
    ): Call<ResponseBody>

    /**
     * 对于json字符串，也可以放进multipartbody
     * 比如后端的接口中，用@RequestPart注解注释entity
     * ```
     * @PostMapping("/bb")
     * 	Response bb(@RequestPart Entity entity, @RequestPart(required = false) MultipartFile file);
     *```
     * 对于调用，示例：
     * ```
     * suspend fun test() {
     *         val entity = Entity(1, "33")
     *         val jsonStr = Json.encodeToString(entity)
     *
     *         val entityPart = MultipartBody.Part.createFormData(
     *             "entity",
     *             null,
     *             jsonStr.toRequestBody(MediaTypeStr.json.toMediaTypeOrNull())
     *         )
     *         handle(
     *             mainApi.test(entityPart, null)
     *         )
     *     }
     *```
     */
    @POST("aa/bb")
    @Multipart
    fun test(
        @Part entity: MultipartBody.Part,
        @Part file: MultipartBody.Part?
    ): Call<ResponseBody>


    //但实际上，可以抛开@Multipart注解，使用@Body 直接传文件
    @POST("update/img")
    fun postImg(@Body requestBody: RequestBody): Call<ResponseBody>
    //注意：此处不能添加@Multipart或@FormUrlEncoded注解，不然会报错。
    //
    //构造请求数据时采用如下方式：
    //MultipartBody.Builder builder = new MultipartBody.Builder()
    //        .setType(MultipartBody.FORM)
    //        .addFormDataPart("param1","param1")
    //    	.addFormDataPart("img", img.getName(), RequestBody.create(img, MediaType.parse("image/jpeg")));
    //
    //postImg(builder.build());
    //如果是多文件，接口注解不变，在添加文件时在继续执行addFormDataPart即可。
    //这种方式就是自己构造请求体，而不用retrofit2的处理

    //</editor-fold>

}
