package com.hxzk.main.ui.mine


import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.hxzk.base.extension.action
import com.hxzk.base.extension.actionBundle
import com.hxzk.base.extension.sToast
import com.hxzk.base.util.Preference
import com.hxzk.base.util.progressdialog.ProgressDialogUtil
import com.hxzk.main.R
import com.hxzk.main.common.Const
import com.hxzk.main.common.Const.ModifyUserInfo.Companion.KEY_USER_AVATAR
import com.hxzk.main.common.Const.ModifyUserInfo.Companion.KEY_USER_BG
import com.hxzk.main.common.Const.ModifyUserInfo.Companion.KEY_USER_NAME
import com.hxzk.main.databinding.FragmentMineBinding
import com.hxzk.main.event.*
import com.hxzk.main.extension.getViewModelFactory
import com.hxzk.main.ui.share.ShareActivity
import com.hxzk.main.ui.base.BaseFragment
import com.hxzk.main.ui.browsehistroy.BrowsingHistoryActivity
import com.hxzk.main.ui.collection.CollectionActivity
import com.hxzk.main.ui.integral.IntegralActivity
import com.hxzk.main.ui.main.MainActivity
import com.hxzk.main.ui.modifyuserinfo.ModifyUserInfoActivity
import com.hxzk.main.ui.notify.NotifyActivity
import com.hxzk.main.ui.setting.SettingActivity
import com.hxzk.main.util.ColorUtil
import com.hxzk.main.util.CropCircleTransformation
import com.hxzk.main.util.ViewUtils
import kotlinx.android.synthetic.main.fragment_mine.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class MineFragment : BaseFragment() , View.OnClickListener {
    val viewModel by viewModels<MineViewModel> { getViewModelFactory()}
    lateinit var databindding: FragmentMineBinding
    lateinit var activity: MainActivity


    private var userAvatarUri: String? = null
    private var userBgImageUri: String? = null

    private var userBgLoadListener: RequestListener<Bitmap> = object : RequestListener<Bitmap> {
        override fun onResourceReady(bitmap: Bitmap?, model: Any?, target: Target<Bitmap>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
            if (bitmap == null) {
                return false
            }
            val bitmapWidth = bitmap.width
            val bitmapHeight = bitmap.height
            if (bitmapWidth <= 0 || bitmapHeight <= 0) {
                return false
            }
            Palette.from(bitmap)
                    .maximumColorCount(3)
                    .clearFilters()
                    // 测量图片头部的颜色，以确定状态栏和导航栏的颜色
                    .setRegion(0, 0, bitmapWidth - 1, (bitmapHeight * 0.1).toInt())
                    .generate { palette ->
                        val isDark = ColorUtil.isBitmapDark(palette, bitmap)
                        if (isDark) {
                            //设置状态栏颜色为透明,页面展示效果为内容沾满状态栏
                            val message = TransparentStatusBarEvent(true)
                             EventBus.getDefault().post(message)
                            //状态栏为暗色模式（状态栏图标和文字变成白色）
                            ViewUtils.clearLightStatusBar(activity.window, databindding.ivBgWall)
                        } else {
                            val message = TransparentStatusBarEvent(true)
                             EventBus.getDefault().post(message)
                            ViewUtils.setLightStatusBar(activity.window, databindding.ivBgWall)
                        }
                    }

            val left = (bitmapWidth * 0.2).toInt()
            val right = bitmapWidth - left
            val top = bitmapHeight / 2
            val bottom = bitmapHeight - 1
            Palette.from(bitmap)
                    .maximumColorCount(3)
                    .clearFilters()
                    // 测量图片下半部分的颜色，以确定用户信息的颜色
                    .setRegion(left, top, right, bottom)
                    .generate { palette ->
                        val isDark = ColorUtil.isBitmapDark(palette, bitmap)
                        val color: Int
                        color = if (isDark) {
                            ContextCompat.getColor(activity, R.color.text_white)
                        } else {
                            ContextCompat.getColor(activity, R.color.primary_text)
                        }
                        databindding.tvUserName.setTextColor(color)
                        databindding.tvUserId.setTextColor(color)
                        databindding.tvLevel.setTextColor(color)
                        databindding.tvRanking.setTextColor(color)
                    }
            return false
        }

        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
            return false
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        databindding = FragmentMineBinding.inflate(inflater, container, false).apply {
            this.viewmodel = viewModel
        }
        databindding.lifecycleOwner = this.viewLifecycleOwner
        return super.onCreateView(databindding.root)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity = requireActivity() as MainActivity
        initEvent()
        initObserve()
    }

    private fun initObserve() {
        //进入就启动加载
        ProgressDialogUtil.getInstance().showDialog(activity)
        viewModel.dataLoading.observe(viewLifecycleOwner){
            if (it) ProgressDialogUtil.getInstance().showDialog(activity)    else ProgressDialogUtil.getInstance().dismissDialog()
        }


        viewModel.unReadNum.observe(viewLifecycleOwner){
             if(it == 0){
                tv_notifyNum.visibility = View.GONE
             }else {
                  val num =   if (it > 99) "99+" else  it.toString()
                 tv_notifyNum.visibility =View.VISIBLE
                 tv_notifyNum.text = num
             }
        }
    }


    private fun initEvent() {
        iv_userPhoto.setOnClickListener(this)
        stv_integral.setOnClickListener(this)
        iv_notify.setOnClickListener(this)
        tv_notifyNum.setOnClickListener(this)
        stv_readHistory.setOnClickListener(this)
        stv_collection.setOnClickListener(this)
        stv_share.setOnClickListener(this)
        stv_setting.setOnClickListener(this)
        }


    override fun onClick(v: View?) {
        when(v?.id){
            R.id.stv_integral ->
                if(viewModel.userInfo.value != null){
                activity.actionBundle<IntegralActivity>(activity,Bundle().also {
                    it.putInt(Const.IntegralList.KEY_COINCOUNT, viewModel.userInfo.value!!.coinCount)
                })
            }else{
                    getString(R.string.common_tips_pleasewaiting).sToast()
                }

            R.id.iv_userPhoto ->
                activity.actionBundle<ModifyUserInfoActivity>(activity,Bundle().apply {
                putString(KEY_USER_BG,userAvatarUri)
                putString(KEY_USER_AVATAR,userBgImageUri)
                putString(KEY_USER_NAME,viewModel.userInfo.value?.username)
            })

            R.id.iv_notify , R.id.tv_notifyNum ->  activity.action<NotifyActivity>(activity)

            R.id.stv_readHistory-> activity.action<BrowsingHistoryActivity>(activity)

            R.id.stv_collection -> if(viewModel.userInfo.value != null){ activity.action<CollectionActivity>(activity) }else{
            getString(R.string.common_tips_pleasewaiting).sToast()
        }

            R.id.stv_share ->
                if(viewModel.userInfo.value != null){
                    var sUserId by Preference(Const.ModifyUserInfo.KEY_USERID,0)
                    sUserId = viewModel.userInfo.value!!.userId
                    activity.action<ShareActivity>(activity)
                }else{
                    getString(R.string.common_tips_pleasewaiting).sToast()
                }

            R.id.stv_setting ->   activity.action<SettingActivity>(activity)

        }
    }

    override fun onStart() {
        super.onStart()
        var saveUserAvatar by Preference(KEY_USER_AVATAR,"")
        userAvatarUri = saveUserAvatar
        var saveUserBg by Preference(KEY_USER_BG,"")
        userBgImageUri = saveUserBg

        Glide.with(this)
                .load(userAvatarUri)
                .transform(CropCircleTransformation(activity))
                .placeholder(R.drawable.loading_bg_circle)
                .error(R.drawable.avatar_default)
                .into(iv_userPhoto)

        Glide.with(this).asBitmap()
                .listener(userBgLoadListener)
                .load(userBgImageUri)
                .placeholder(R.drawable.bg_wall)
                .error(R.drawable.bg_wall)
                .into(iv_bgWall)
    }


    //监听是否进入到了未读消息的Fragment，需要将未读消息数目设置为0
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(messageEvent: MessageEvent) {
        if (messageEvent is UnReadNumEvent) {
            //说明进入了未读消息Fragment,将消息数控件隐藏
            if(messageEvent.notifyNum == 0 ){
                tv_notifyNum.visibility = View.GONE
            }
        }else if(messageEvent is ClearUserMessage){
            if(messageEvent.isClear){
                databindding.tvLevel.text = resources.getString(R.string.mine_userid)
                databindding.tvUserName.text = ""
                databindding.tvUserId.text = resources.getString(R.string.mine_userid)
                databindding.tvRanking.text = resources.getString(R.string.mine_ranking)
            }
        }
    }
}