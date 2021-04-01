package com.hxzk.app

import android.os.Bundle
import android.transition.Transition
import com.hxzk.base.util.AndroidVersion
import com.hxzk.main.callback.SimpleTransitionListener
import com.hxzk.main.event.FinishActivityEvent
import com.hxzk.main.ui.login.LoginActivity
import kotlinx.android.synthetic.main.activity_open_source_login.*
import org.greenrobot.eventbus.EventBus

class OpenSourceLoginActivity : LoginActivity() {


    /**
     * 是否正在进行transition动画。
     */
    protected var isTransitioning = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_source_login)
    }

    override fun setupViews() {
        viewPager2 =vp2
        //Android5.0及其以上版本且有动画效果
        if (AndroidVersion.hasLollipop()) {
            isTransitioning = true
            //转场动画监听
            window.sharedElementEnterTransition.addListener(object : SimpleTransitionListener() {
                override fun onTransitionEnd(transition: Transition) {
                    val event = FinishActivityEvent()
                    event.activityClass = OpenSourceSplashActivity::class.java
                    EventBus.getDefault().post(event)
                    isTransitioning = false
                }
            })
        }

        close.setOnClickListener {
            if (!isTransitioning) {
                finish()
            }
        }
        initVP()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }


}