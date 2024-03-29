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
          const val KEY_ISLOGINAGAIN = "key_isloginagain"
        }
    }


    interface Search{
        companion object{
            const val  REQUEST_SEARCH = 10000
        }
    }

    interface  PhotoView{
        companion object{
            const val KEY_CURRENT_RUL = "key_current_url"
            const val KEY_IMGS_RUL = "key_imgs_url"
        }
    }

    interface  IntegralList{
        companion object{
            const val KEY_COINCOUNT = "key_count"
        }
    }


    interface  ModifyUserInfo{
        companion object{
            const val KEY_USER_BG = "key_user_bg"
            const val KEY_USER_AVATAR = "key_user_avatar"
            const val KEY_USER_NAME = "key_user_name"
            const val KEY_USERID="key_userid"
        }
    }

    interface  SystemItem{
        companion object{
            const val KEY_TITLE = "key_title"
            const val KEY_ID = "key_id"
        }
    }

   interface X5Fragment {
       companion object{
           const val KEY_COLLECTIONTIPS = "key_collectiontips"
       }
   }

    interface ShareActivity{
        companion object{
            const val KEY_REQUESTCODE = 0X111
        }
    }
}