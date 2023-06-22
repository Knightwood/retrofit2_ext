package com.kiylx.retrofit2_ext.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kiylx.libx.http.kotlin.basic.Status.*
import com.kiylx.libx.http.kotlin.basic2.Resource2
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RExampleActivity : AppCompatActivity() {
    lateinit var vm: ViewModelExample

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vm = ViewModelProvider(this)[ViewModelExample::class.java]
        //livedata和resource的方式
        vm.loginLiveData.observe(this) {//观察结果
            when (it.status) {//根据结果改变页面状态
                SUCCESS -> TODO()
                REQUEST_ERROR -> TODO()
                LOADING -> TODO()
                LOCAL_ERR -> TODO()
            }
        }

        //resources2和stateflow的方式
        this.lifecycleScope.launch {
            // repeatOnLifecycle 每当生命周期处于 STARTED 或以后的状态时会在新的协程中
            // 启动执行代码块，并在生命周期进入 STOPPED 时取消协程。
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.uploadFlow.collectLatest { resource ->
                    when (resource) {
                        is Resource2.Loading<*> -> TODO()
                        is Resource2.LocalFailed -> TODO()
                        is Resource2.RequestError -> TODO()
                        is Resource2.Success -> TODO()
                        Resource2.EmptyLoading -> TODO()
                    }
                }

            }
        }
    }
    override fun onStart() {
        super.onStart()
        vm.getLogin()//调用方法

    }
}