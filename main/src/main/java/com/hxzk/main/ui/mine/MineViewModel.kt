package com.hxzk.main.ui.mine

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.hxzk.base.extension.sToast
import com.hxzk.main.data.source.Repository
import com.hxzk.main.util.ResponseHandler
import com.hxzk.network.Result
import com.hxzk.network.model.ApiResponse
import com.hxzk.network.model.UserInfoModel
import com.hxzk.network.succeeded

class MineViewModel(private val repository: Repository) : ViewModel() {

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private  val _userInfo :LiveData<UserInfoModel> = repository.integral().switchMap {
         transition(it)
    }
    private fun transition(it : Result<*>) : LiveData<UserInfoModel> {
        val result =MutableLiveData<UserInfoModel>()
        if (it.succeeded) {
            val bean = (it as Result.Success<*>).res as ApiResponse<*>
            if (bean.errorCode == 0) {
                result.value = bean.data as UserInfoModel
            } else {
                bean.errorMsg.sToast()
            }
        } else {
            val res = it as Result.Error
            ResponseHandler.handleFailure(res.e)
        }
        _dataLoading.value =false
        return result
    }

    val userInfo : LiveData<UserInfoModel> = _userInfo



    private  val _unReadNum = repository.unReadNum().switchMap {
        transForm(it)
    }
    private fun transForm(it : Result<*>) : LiveData<Int> {
        val result =MutableLiveData<Int>()
        if (it.succeeded) {
            val bean = (it as Result.Success<*>).res as ApiResponse<*>
            if (bean.errorCode == 0) {
                result.value =  bean.data as Int
            } else {
                bean.errorMsg.sToast()
            }
        } else {
            val res = it as Result.Error
            ResponseHandler.handleFailure(res.e)
        }
        return result
    }
    val  unReadNum : LiveData<Int> = _unReadNum
}