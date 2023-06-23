package com.kiylx.retrofit2_ext.example

abstract class Data {
    abstract val errorCode: Int
    abstract val errorMsg: String
    abstract val data: Any?
}

@kotlinx.serialization.Serializable
data class FriendData(
    override val errorCode: Int = 0,
    override val errorMsg: String = "",
    override val data: FriendInfo?=null
) : Data() {

    @kotlinx.serialization.Serializable
    data class FriendInfo(
        val category: String = "源码",
        val icon: String = "",
        val id: Int = 22,
        val link: String = "https://www.androidos.net.cn/sourcecode",
        val name: String = "androidos",
        val order: Int = 11,
        val visible: Int = 1,
    )
}