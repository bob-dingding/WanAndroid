/*
 * Copyright (C) guolin, Suzhou Quxiang Inc. Open source codes for study only.
 * Do not use for commercial purpose.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hxzk.base.util

import android.app.Activity

import java.lang.ref.WeakReference
import java.util.*

/**
 * 应用中所有Activity的管理器，可用于一键杀死所有Activity。
 *
 * @author guolin
 * @since 18/2/8
 */
object ActivityCollector {

    private const val TAG = "ActivityCollector"

    private val activityList = ArrayList<WeakReference<Activity>?>()

    fun size(): Int {
        return activityList.size
    }

    fun add(weakRefActivity: WeakReference<Activity>?) {
        activityList.add(weakRefActivity)
    }

    fun remove(weakRefActivity: WeakReference<Activity>?) {
        val result = activityList.remove(weakRefActivity)
    }

    fun get(index: Int):WeakReference<Activity>? {
       return  activityList.get(index)
    }


    fun finishAll() {
        if (activityList.isNotEmpty()) {
            for (activityWeakReference in activityList) {
                val activity = activityWeakReference?.get()
                if (activity != null && !activity.isFinishing) {
                    activity.finish()
                }
            }
            activityList.clear()
        }
    }


    fun getIndexByClass(mClass: Class<*>) : Int{
        for (pos in 0..activityList.size){
            val activity: Activity? =activityList.get(pos)?.get()
                if(activity?.javaClass == mClass){
                return pos
         }
        }
        return 0
    }

    fun logActivity(){
        for (pos in 0 until activityList.size){
            System.out.println(activityList.get(pos)?.get()?.javaClass.toString())
        }
    }



}
