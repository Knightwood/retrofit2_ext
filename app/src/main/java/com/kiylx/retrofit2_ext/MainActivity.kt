package com.kiylx.retrofit2_ext

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import com.kiylx.libx.http.kotlin.basic2.Resource2
import com.kiylx.libx.http.kotlin.basic3.UiState
import com.kiylx.libx.http.kotlin.basic3.flow.DataUiState
import com.kiylx.retrofit2_ext.example_2.FriendData
import com.kiylx.retrofit2_ext.example_2.WanAndroidRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    lateinit var handler: Handler
    val vm: VM by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        handler = Handler(Looper.getMainLooper())
        resources2ObserveData()
        rawResponseObserveData()
    }

    private fun rawResponseObserveData() {
        this.lifecycleScope.launch{
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                //可以观察界面数据
                vm._friendUiData.asUiStateFlow().collect{
                    //观察界面数据
                    when(it){
                        UiState.Empty -> TODO()
                        UiState.INIT -> TODO()
                        UiState.Loading -> TODO()
                        is UiState.OtherErr -> TODO()
                        is UiState.RequestErr -> TODO()
                        is UiState.Success<*> -> TODO()
                        else -> {}
                    }
                    //读取请求成功的数据
                    vm._friendUiData.getData()
                    //其他的操作...
                }

            }
        }
    }

    /**
     * 返回值是resources2
     */
    private fun resources2ObserveData() {
        this.lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.friendDataFlow.collect {
                    when (it) {
                        Resource2.EmptyLoading -> {
                            Log.d(TAG, "onCreate: empty loading")
                        }
                        is Resource2.Loading -> {
                            Log.d(TAG, "onCreate: loading")
                        }

                        is Resource2.Error -> {
                            Log.d(TAG, "onCreate: RequestError")
                        }
                        is Resource2.Success -> {
                            Log.d(TAG, "onCreate: Success")
                        }

                        is Resource2.OtherError -> TODO()
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        //模拟操作，延迟一下时间再请求
        handler.postDelayed({
            vm.requestFriendData()//请求数据
            vm.requestFriendData2()//请求数据
        }, 1500L)
    }

    companion object {
        const val TAG = "tty2-MainActivity"
    }

}

class VM : ViewModel() {
    private var _friendDataFlow: MutableStateFlow<Resource2<FriendData>> =
        MutableStateFlow(Resource2.EmptyLoading)
    val friendDataFlow: StateFlow<Resource2<FriendData>>
        get() = _friendDataFlow

    fun requestFriendData() {
        viewModelScope.launch {
            _friendDataFlow.emit(WanAndroidRepo.resource2GetData())
        }
    }

    //<editor-fold desc="rawResponse版本">
    /**
     * 持有ui数据和网络数据
     */
    var _friendUiData: DataUiState<FriendData> = DataUiState(FriendData())
    fun requestFriendData2() {
        viewModelScope.launch {
            //通过网络获取数据
            val res=WanAndroidRepo.rawResponseGetData()
            //根据网络数据的请求状态更新ui数据和上一次请求成功的界面数据
            _friendUiData.setStateWithData(res)

        }
    }
    //</editor-fold>
}