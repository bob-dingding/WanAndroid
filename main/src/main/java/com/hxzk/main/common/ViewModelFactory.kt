package com.hxzk.main.common

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.hxzk.main.data.source.Repository
import com.hxzk.main.ui.home.HomeViewModel
import com.hxzk.main.ui.integral.IntegraListModel
import com.hxzk.main.ui.login.LoginViewModel
import com.hxzk.main.ui.mine.MineViewModel
import com.hxzk.main.ui.register.RegisterViewModel

/**
 *作者：created by zjt on 2021/3/12
 *描述:
 *
 */
class ViewModelFactory constructor(
    private  val repository: Repository,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner,defaultArgs){
    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ) = with(modelClass){
       when{
            isAssignableFrom(LoginViewModel::class.java) ->
                 LoginViewModel(repository)

            isAssignableFrom(RegisterViewModel::class.java) ->
                RegisterViewModel(repository)

            isAssignableFrom(HomeViewModel::class.java) ->
                HomeViewModel(repository)

            isAssignableFrom(MineViewModel::class.java) ->
                MineViewModel(repository)

           isAssignableFrom(IntegraListModel::class.java) ->
               IntegraListModel(repository)
            else ->
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }

    } as T
}