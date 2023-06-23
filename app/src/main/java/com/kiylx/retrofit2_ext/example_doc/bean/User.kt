package com.kiylx.retrofit2_ext.example_doc.bean

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import java.util.*

data class User(override val code: Int, override val msg: String, val data: String): BaseData()
data class User2(
    override val code: Int, override val msg: String,
    @Serializable(with = UUIDSerializer::class)
    val data: UUID,
    val arr:List<Double>,//对于double这样的数组，可以直接序列化和反序列化
): BaseData()

//在需要注释的字段上@Serializable(with = UUIDSerializer::class)
//注释字段后就能解析
object UUIDSerializer : KSerializer<UUID> {
    override fun deserialize(decoder: Decoder): UUID {
        return UUID.fromString(decoder.decodeString())
    }

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.encodeString(value.toString())
    }
}
fun main() {
    val s =
        "[-0.0280927,0.109398,-0.0239961]"
    val array = Array(3) { 0.0 }
    val ss = Json.decodeFromString<List<Double>>(s)
    print(ss)
}
