package com.kiylx.retrofit2_ext.example

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiylx.libx.http.kotlin.basic.Resource
import com.kiylx.libx.http.kotlin.basic2.Resource2
import com.kiylx.retrofit2_ext.example.bean.User
import com.kiylx.retrofit2_ext.example.bean.User2
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ViewModelExample : ViewModel() {
    val repoExample = RepoExample()
    var loginLiveData: MutableLiveData<Resource<User>> = MutableLiveData()
    private var _uploadFlow: MutableStateFlow<Resource2<User2>> =
        MutableStateFlow(Resource2.EmptyLoading)
    val uploadFlow: MutableStateFlow<Resource2<User2>>
        get() = _uploadFlow

    fun getLogin() {
        loginLiveData.value = Resource.loading(null)
        viewModelScope.launch {
            loginLiveData.value = repoExample.login()
            //loginLiveData.value=LocalErrorModel.noFile()//可以发送本地错误，领界面更新
        }
    }

    /**
     * 展示使用stateFlow
     */
    fun upload() {
        _uploadFlow.tryEmit(Resource2.loading())
        viewModelScope.launch {
            _uploadFlow.emit(repoExample.upload())
        }
    }
}

