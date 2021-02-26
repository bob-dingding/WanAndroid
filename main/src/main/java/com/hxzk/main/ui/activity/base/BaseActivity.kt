package com.hxzk.main.ui.activity.base

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hxzk.main.event.MessageEvent
import com.hxzk.base.util.ActivityCollector
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.lang.ref.WeakReference

open abstract class BaseActivity : AppCompatActivity() {


    var activity : Activity? = null
    var weakReference :WeakReference<Activity>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity = this
        weakReference = WeakReference(this)
        ActivityCollector.add(weakReference)
        EventBus.getDefault().register(this)
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        setupViews()
    }

    override fun onDestroy() {
        super.onDestroy()
        activity = null
        ActivityCollector.remove(weakReference)
        EventBus.getDefault().unregister(this)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    open fun onMessageEvent(messageEvent: MessageEvent) {}

    abstract fun  setupViews()

}