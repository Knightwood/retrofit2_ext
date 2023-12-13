package com.kiylx.libx.http.kotlin


/**
 * 配合[failureThen]和[successThen]构成链式调用，
 * 此方法与[failureThen]，[successThen]都调用了[kotlin.runCatching]，遇到异常将返回[Result.Failure]
 * 根据上一个函数的返回值,[failureThen]和[successThen]方法将返回不同的值或处理
 * 用法：
 *
 * ```
 *  val bytesCount: Long = runCatchingChain {
 *                  //返回值是Result<Long>
 *                 val inputStream = createInputStreamFromContentResolver(contentResolver)
 *                 inputStream!!.available().toLong()
 *             }.failureThen {
 *                  //当上个函数chain返回值，判断为Result.Failure时，此处将会调用，返回新值
 *                 val parcelFileDescriptor = createParcelFileDescriptor(contentResolver)
 *                 parcelFileDescriptor!!.statSize.toLong()
 *             }.successThen {
 *                  //当上个函数failureThen返回值，判断为success时，此处将会调用，返回新值
 *                 val length=this.getOrThrow()
 *                 if (length<0){
 *                     -1
 *                 }else{
 *                     length
 *                 }
 *             }.getOrDefault(-1)//当无异常时，返回得到的值，异常时，返回默认值
 * ```
 */
inline fun <T> runCatchingChain(action: () -> T): Result<T> {
    return runCatching(action)
}

/**
 * 链式调用，当上一个方法的返回值是[Result.Failure]时，此方法将调用action，返回新值。
 * 若上一个方法的返回值不是[Result.Failure]，则返回上一个方法的返回值
 */
inline fun <T> Result<T>.failureThen(action: () -> T): Result<T> {
    return if (this.isFailure) {
        runCatching(action)
    } else {
        this
    }
}

/**
 * 链式调用，当上一个方法的返回值不是[Result.Failure]时，此方法将调用action，返回新值。
 * 若上一个方法的返回值是[Result.Failure]，则返回上一个方法的返回值
 */
inline fun <T> Result<T>.successThen(action: Result<T>.() -> T): Result<T> {
    return if (this.isSuccess) {
        this.runCatching(action)
    } else {
        this
    }
}

class KotlinRunCatchingExt {

}

fun main() {
    val num: Int = runCatchingChain {
        12
    }.failureThen {
        13
    }.successThen {
        14
    }.getOrDefault(10)
    println(num)//14

    val num1: Int = runCatchingChain {
        throw IllegalStateException("")
    }.failureThen {
        13
    }.getOrDefault(10)
    println(num1)//13


    val num2: Int = runCatchingChain<Int> {
        throw IllegalStateException("")
    }.failureThen {
        throw IllegalStateException("")
    }.successThen {
        14
    }.getOrDefault(10)
    println(num2)//10


}
