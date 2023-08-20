# json 配置
* 序列化
```kotlin
@Serializable
data class Person(val name:String,val age:Int)

        val json = Json {
            prettyPrint = true //是否格式化，默认false
            prettyPrintIndent = "  " //除首行外各行缩进，默认四个空格
        }
        println(json.encodeToString(Person("小明",18)))

        //print:
        {
          "name": "小明",
          "age": 18
        }
```
* 宽松解析
```kotlin
        val json = "{name:小明,age:\"18\"}" //这是一条格式有问题的json
        val person = Json {
            isLenient = true //json格式有问题，也有可能解析成功，默认false
        }.decodeFromString<Person>(json)
        println(person)
 
        //print:Person(name=小明, age=18)
```
* 忽略未知键
  默认情况下，json和实例化对像键值需要一一对应，但实际案例中，json和对象参数名可能无法一一对应，这种情况可以忽略未使用的键
```kotlin
@Serializable
data class Person(val name:String)
 
val json = """{"name":"小明","age":18}"""
println(Json{
    ignoreUnknownKeys = true
}.decodeFromString<Person>(json))
 
//print:Person(name=小明)
```
* 强制输入值
  json的值类型和实例化对象类型不一致时，使用对象的默认值
```kotlin
        @Serializable
data class Person(val name:String = "小明",val age:Int = 18)

val json = """{"name":"小红","age":null}"""
println(Json{
  coerceInputValues = true //默认false
}.decodeFromString<Person>(json))

//print:Person(name=小红, age=18)
```
* 编码默认值
  序列化时，对象默认值参与序列化
```kotlin
@Serializable
data class Person(val name:String = "小明",val age:Int = 18)
 
println(Json{
    encodeDefaults = false
}.encodeToString(Person(name = "小红")))
 
//plint:{"name":"小红"}
 
 
println(Json{
    encodeDefaults = true
}.encodeToString(Person(name = "小红")))
 
//plint:{"name":"小红","age":18}
```
* 序列化时忽略null
 ```kotlin
@Serializable
data class Person(val name:String = "小明",val age:Int? = null)

println(Json{
encodeDefaults = true
explicitNulls = false
}.encodeToString(Person(name = "小红",age = null)))

//print:{"name":"小红"}


//explicitNulls = true时，值为null也会参与序列化
println(Json{
encodeDefaults = true
explicitNulls = true
}.encodeToString(Person(name = "小红",age = null)))
//print:{"name":"小红","age":null}
```