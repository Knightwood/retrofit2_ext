package com.kiylx.retrofit2_ext.example2_doc.bean

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 数据类可以继承自此类，这样可以在通用的异常/错误处理中，获取code和msg,集中处理
 */
@kotlinx.serialization.Serializable
sealed class BaseData {
    abstract val code: Int
    abstract val msg: String
}
//下面这些也可以作为数据的父类
sealed class BaseData2 {
    abstract val code: Int
    abstract val msg: String
    abstract val data:Any? //任意类型的data，由子类重写成其他类型
}
//抽象类作为数据的父类，可以不像密封类那样局限于同一个包名下
abstract class BaseData3 {
    abstract val code: Int
    abstract val msg: String
    abstract val data:Any?
}

@kotlinx.serialization.Serializable
class Student(override val code: Int, override val msg: String, val age: Int, val name: String) :
    BaseData()

fun main() {
    val student = Student(200, "success", 12, "tom")
    println(Json.encodeToString(student))

    val base =
        Json.decodeFromString<Student>("{\"code\":200,\"msg\":\"success\",\"age\":12,\"name\":\"tom\"}")
    println((base as BaseData).code)
}
