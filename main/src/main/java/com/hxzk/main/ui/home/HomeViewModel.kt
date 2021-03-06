package com.hxzk.main.ui.home

import androidx.lifecycle.*
import com.hxzk.base.extension.logDebug
import com.hxzk.base.extension.sToast
import com.hxzk.main.data.source.Repository
import com.hxzk.main.util.ResponseHandler
import com.hxzk.network.Result
import com.hxzk.network.model.*
import com.hxzk.network.succeeded
import kotlinx.coroutines.launch

/**
 *作者：created by zjt on 2021/4/2
 *描述:
 *
 */
class HomeViewModel(private val repository: Repository) : ViewModel() {

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    /**
     * 是否进行更新的标识
     */
    private val _forceUpdate = MutableLiveData<Boolean>(false)

    /**
     * 是否正在刷新
     */
    val isRefreshing = MutableLiveData(false)

    /**
     * 是否正在加载更多
     */
    val isLoadMoreing = MutableLiveData(false)


    /**
     * 首页banner数据源(用_forceUpdate来控制，主要是因为livedata感知，只会在onResume和onPause)
     */
    private val _banners: LiveData<List<HomeBanner>> = _forceUpdate.switchMap {
        val res = MutableLiveData<List<HomeBanner>>()
        if (it) {
            return@switchMap repository.banner().switchMap {
                transitionBannerItem(it)
            }
        }
        res
    }

    private fun transitionBannerItem(it: Result<*>): LiveData<List<HomeBanner>> {
        val result = MutableLiveData<List<HomeBanner>>()
        if (it.succeeded) {
            val bean = (it as Result.Success<*>).res as ApiResponse<*>
            if (bean.errorCode == 0) {
                result.value = bean.data as List<HomeBanner>
            } else {
                bean.errorMsg.sToast()
            }
        } else {
            result.value = emptyList()
            val res = it as Result.Error
            ResponseHandler.handleFailure(res.e)
        }
        return result
    }

    val banners: LiveData<List<HomeBanner>> = _banners

    /**
     * 获取item的条目,adapter中的getItemCount要用
     */
    val _topArticleSize = MutableLiveData<Int>()


    /**
     * 刷新或加载更多的方法
     * isFirstLoad 只有首次加载才显示loading,否则用smart自带的刷新或更多loading
     */
    fun forceUpdate(isRefresh: Boolean, isFirstLoad: Boolean = false) {
        if (isFirstLoad) {
            _dataLoading.value = true
        }
        _forceUpdate.value = isRefresh
    }


    val _openItem = MutableLiveData<CommonItemModel>()
    val openItem: LiveData<CommonItemModel> = _openItem

    /**
     * 点击Item
     */
    fun clickItem(item: DataX) {
        //将不同的数据bean,转化为公用的
        val model = CommonItemModel(item.id, item.link, item.title,item.collect)
        _openItem.value = model
    }

    /**
     * 请求首页文章列表(包括置顶文章和一般文章列表)
     */
    var curPage: Int = 0
    var pageCount: Int = 0

    private val _itemList: LiveData<ArticleListModel> = _forceUpdate.switchMap {
        if (it) {
            isRefreshing.value = true
            curPage = 0
        } else {
            isLoadMoreing.value = true
        }
        itemList()
    }
    val itemList: LiveData<ArticleListModel> = _itemList

    private fun itemList(): LiveData<ArticleListModel> = repository.articleList(curPage, _itemList.value).switchMap {
        val result = MutableLiveData<ArticleListModel>()
        //请求页码从0开始，返回curPage为1
        curPage = it.curPage
        pageCount = it.pageCount
        result.value = it
        _topArticleSize.value = it.datas.size
        _dataLoading.value = false
        //协程作用域外的代码比协程作用域的代码先执行
        isRefreshing.value = false
        isLoadMoreing.value = false
        result
    }
}


