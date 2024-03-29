package com.hxzk.main.ui.home

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.*
import android.widget.CheckBox
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.observe
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.ml.scan.HmsScan
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions
import com.hxzk.base.extension.actionBundle
import com.hxzk.base.extension.sToast
import com.hxzk.base.util.AndroidVersion
import com.hxzk.base.util.GlobalUtil
import com.hxzk.base.util.Preference
import com.hxzk.base.util.progressdialog.ProgressDialogUtil
import com.hxzk.main.R
import com.hxzk.main.callback.BannerItemListener
import com.hxzk.main.common.Const
import com.hxzk.main.databinding.FragmentHomeBinding
import com.hxzk.main.extension.getViewModelFactory
import com.hxzk.main.ui.adapter.HomeItemAdapter
import com.hxzk.main.ui.base.BaseFragment
import com.hxzk.main.ui.main.MainActivity
import com.hxzk.main.ui.search.SearchActivity
import com.hxzk.main.ui.x5Webview.X5MainActivity
import com.hxzk.main.ui.x5Webview.X5MainActivity.Companion.KEY_ITEMBEAN
import com.hxzk.network.model.ArticleListModel
import com.hxzk.network.model.CommonItemModel
import com.hxzk.network.model.DataX
import com.hxzk.network.model.HomeBanner
import kotlinx.android.synthetic.main.fragment_home.*



/**
 * 首页Fragment
 */
class HomeFragment : BaseFragment(), BannerItemListener {

    val homeViewModel by viewModels<HomeViewModel> { getViewModelFactory() }
    lateinit var  homeFragDataBinding : FragmentHomeBinding

    private lateinit var listAdapter: HomeItemAdapter

    lateinit var activity: MainActivity
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeFragDataBinding = FragmentHomeBinding.inflate(inflater, container, false).apply {
            this.viewModel = homeViewModel
        }
        //使fragmeng中的toolbar菜单生效
        setHasOptionsMenu(true)
        return super.onCreateView(homeFragDataBinding.root)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity = getActivity() as MainActivity
        //设置了lifecycleOwner后绑定了LiveData数据源的xml控件才会随着数据变化而改变
        homeFragDataBinding.lifecycleOwner = this.viewLifecycleOwner
        initToolbar()
        setupListAdapter()
        onItemClick()
        smartListener()
        initObserve()
        //刷新数据(如果断点,会影响)
        homeViewModel.banners.observe(viewLifecycleOwner) {}
        homeViewModel.itemList.observe(viewLifecycleOwner) {
            listAdapter.notifyDataSetChanged()
        }
        homeViewModel.forceUpdate(true,true)
    }

    private fun initObserve() {
        homeViewModel.dataLoading.observe(viewLifecycleOwner){
            if (it) ProgressDialogUtil.getInstance().showDialog(activity)    else ProgressDialogUtil.getInstance().dismissDialog()
        }
        homeViewModel.unCollectionPos.observe(viewLifecycleOwner){
            //对适配器重新绑定了一次数据,刷新当前索引到最后一个item索引的内容,注意要-1
            //会重新执行onBindViewHolder
                //homeViewModel.itemList.value?.get(it)?.collect  = !homeViewModel.itemList.value?.get(it)?.collect!!
                homeViewModel.itemList.value?.size?.let { it1 -> listAdapter.notifyItemRangeChanged(it, it1) }
        }
    }


    private fun smartListener() {
        //刷新
        homeFragDataBinding.smartRefresh.setOnRefreshListener {
            homeViewModel.forceUpdate(true)
        }
        //加载更多
        homeFragDataBinding.smartRefresh.setOnLoadMoreListener {
            homeViewModel.forceUpdate(false)
        }

        homeViewModel.isRefreshing.observe(viewLifecycleOwner) {
            if (!it){
                homeFragDataBinding.smartRefresh.finishRefresh()
            }
        }
        homeViewModel.isLoadMoreing.observe(viewLifecycleOwner){
            if (!it){
                homeFragDataBinding.smartRefresh.finishLoadMore()
            }
        }

    }



    /**
     * 列表项的点击
     */
  private fun onItemClick(){
       homeViewModel.openItem.observe(viewLifecycleOwner) {
           val mBundle =Bundle()
           mBundle.putParcelable(KEY_ITEMBEAN,it)
           activity.actionBundle<X5MainActivity>(activity,mBundle)
       }
    }

    /**
     * 设置RecyclerView的Adapter
     */
    private fun setupListAdapter() {
        val viewModel = homeFragDataBinding.viewModel
        if (viewModel != null) {
            listAdapter = HomeItemAdapter(viewModel)
            listAdapter.setBannerItemListener(this)
            homeFragDataBinding.recycler.adapter = listAdapter
        }
    }


    /**
     * 设置toolbar
     */
    private fun initToolbar() {
        activity.setSupportActionBar(toolbar)
        val actionBar = activity.supportActionBar
        //隐藏左侧图标
        actionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_home_toolbar, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_search -> {
                // Android 5.0版本启用transition动画会存在一些效果上的异常，因此这里只在Android 5.1以上启用此动画
                if (AndroidVersion.hasLollipopMR1()) {
                    val searchMenuView: View? = toolbar?.findViewById(R.id.menu_search)
                    val options = ActivityOptions.makeSceneTransitionAnimation(
                        activity, searchMenuView,
                        getString(R.string.transition_search_back)
                    ).toBundle()
                    startActivity(
                        Intent(activity, SearchActivity::class.java),
                         options
                    )
                } else {
                    startActivity(
                        Intent(activity, SearchActivity::class.java)
                    )
                }
            }
            R.id.menu_scanCode ->{
                // TODO: 2021/7/15  需要增加权限判断
                startDefaultMode()
            }
        }
        return true
    }

    /**
     * 启动扫描服务
     */
    private fun startDefaultMode() {
        // 扫码选项参数
        val options = HmsScanAnalyzerOptions.Creator().setHmsScanTypes(HmsScan.ALL_SCAN_TYPE).create()
        ScanUtil.startScan(activity, REQUEST_CODE_SCAN_DEFAULT_MODE, options)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK || data == null) {
            return
        }
        when (requestCode) {
            REQUEST_CODE_SCAN_DEFAULT_MODE -> {
                val hmsScan: HmsScan? = data.getParcelableExtra(ScanUtil.RESULT)
                // 获取扫码结果 ScanUtil.RESULT
                if (!TextUtils.isEmpty(hmsScan?.getOriginalValue())) {
                    if(hmsScan?.getOriginalValue() != null){
                        showAlert(hmsScan.getOriginalValue())
                    }
                }
            }
        }
    }

    private fun showAlert(copyStr : String) {
            val dialog = AlertDialog.Builder(activity, R.style.AlertDialogStyle)
                    .setTitle(GlobalUtil.getString(R.string.sacnCode_title))
                    .setMessage(copyStr)
                    .setPositiveButton(GlobalUtil.getString(R.string.sacnCode_copy)) { _, _ ->
                       if(GlobalUtil.copy(copyStr,activity)){
                           GlobalUtil.getString(R.string.sacnCode_clip_success).sToast()
                       }else{
                           GlobalUtil.getString(R.string.sacnCode_clip_failure).sToast()
                       }
                    }.setNegativeButton(GlobalUtil.getString(R.string.sacnCode_cancel)){ _, _ ->

                    }
                    .create()
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()

        }



    override fun onItemClick(data: HomeBanner, position: Int) {
        val model = CommonItemModel(data.id,data.url,data.url)
        val mBundle =Bundle()
        mBundle.putParcelable(KEY_ITEMBEAN,model)
        activity.actionBundle<X5MainActivity>(activity,mBundle)
    }
    
    companion object{
        const val CODE_SELECT_IMAGE = 0x1111;
        const val REQUEST_CODE_SCAN_DEFAULT_MODE = 0x222
    }
}