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
import com.kiylx.retrofit2_ext.example.FriendData
import com.kiylx.retrofit2_ext.example.WanAndroidRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    lateinit var handler: Handler
    val vm: VM by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        handler = Handler(Looper.getMainLooper())
        this.lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.friendDataFlow.collect {
                    when (it) {
                        Resource2.EmptyLoading -> {
                            Log.d(TAG, "onCreate: empty loading")
                        }
                        is Resource2.Loading<*> -> {
                            Log.d(TAG, "onCreate: loading")
                        }
                        is Resource2.LocalFailed -> {
                            Log.d(TAG, "onCreate: LocalFailed")
                        }
                        is Resource2.RequestError -> {
                            Log.d(TAG, "onCreate: RequestError")
                        }
                        is Resource2.Success -> {
                            Log.d(TAG, "onCreate: Success")
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        handler.postDelayed({
            vm.requestFriendData()
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
            _friendDataFlow.emit(WanAndroidRepo.getData())
        }
    }
}