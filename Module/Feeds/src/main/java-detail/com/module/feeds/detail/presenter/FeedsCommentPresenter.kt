package com.module.feeds.detail.presenter

import com.alibaba.fastjson.JSON
import com.common.mvp.AbsCoroutinePresenter
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.module.feeds.detail.FeedsDetailServerApi
import com.module.feeds.detail.model.FirstLevelCommentModel

class FeedsCommentPresenter(val mFeedId: Int) : AbsCoroutinePresenter() {
    val mFeedsDetailServerApi = ApiManager.getInstance().createService(FeedsDetailServerApi::class.java)
    val mCount = 30
    var mOffset = 0
    val mModelList: ArrayList<FirstLevelCommentModel> = ArrayList()

    init {
        addToLifeCycle()
    }

    fun getFirstLevelCommentList(callBack: (List<FirstLevelCommentModel>?) -> Unit) {
        ApiMethods.subscribe(mFeedsDetailServerApi.getFirstLevelCommentList(mOffset, mCount, mFeedId), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult?) {
                if (obj?.errno == 0) {
                    val list: List<FirstLevelCommentModel>? = JSON.parseArray(obj.data.getString("comments"), FirstLevelCommentModel::class.java)
                    list?.let {
                        mModelList.addAll(it)
                        callBack(mModelList)
                    }
                    mOffset = obj.data.getIntValue("offset")
                }
            }
        }, this)
    }
}