package com.hxzk.main.common

/**
 *作者：created by zjt on 2021/3/30
 *描述:每个页面对应需要的常量
 *
 */
interface Const {
    interface Auth {
        companion object {
          const val KEY_ACCOUNT = "key_account"
          const val KEY_PWD = "key_pwd"
        }
    }


    interface Search{
        companion object{
            const val  REQUEST_SEARCH = 10000
        }
    }



}