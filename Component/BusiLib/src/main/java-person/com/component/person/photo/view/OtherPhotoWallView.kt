package com.component.person.photo.view

import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.widget.RelativeLayout

import com.alibaba.fastjson.JSON
import com.common.base.BaseFragment
import com.common.callback.Callback
import com.common.core.userinfo.UserInfoServerApi
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.component.busilib.R
import com.component.person.view.RequestCallBack
import com.imagebrowse.ImageBrowseView
import com.imagebrowse.big.BigImageBrowseFragment
import com.imagebrowse.big.DefaultImageBrowserLoader
import com.component.person.photo.adapter.PhotoAdapter
import com.component.person.photo.model.PhotoModel

class OtherPhotoWallView(internal var mFragment: BaseFragment, internal var mUserId: Int, internal var mCallBack: RequestCallBack?, internal var mListener: AppCanSrollListener?) : RelativeLayout(mFragment.context) {

    val TAG = "PhotoWallView"

    internal var offset: Int = 0  // 拉照片偏移量
    internal val DEFAUAT_CNT = 20       // 默认拉取一页的数量
    private var mLastUpdateInfo: Long = 0
    internal var mHasMore = false

    private val mPhotoView: RecyclerView
    internal val mPhotoAdapter: PhotoAdapter
    internal val mUserInfoServerApi: UserInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi::class.java)

    init {
        View.inflate(context, R.layout.photo_other_wall_view_layout, this)

        mPhotoView = findViewById<View>(R.id.photo_view) as RecyclerView
        mPhotoView.isFocusableInTouchMode = false
        mPhotoView.layoutManager = GridLayoutManager(context, 3)
        mPhotoAdapter = PhotoAdapter(PhotoAdapter.TYPE_OTHER_PERSON_CENTER)

        mPhotoAdapter.mOnClickPhotoListener = { _, position, _ ->
            BigImageBrowseFragment.open(true, mFragment.activity, object : DefaultImageBrowserLoader<PhotoModel>() {
                override fun init() {

                }

                override fun load(imageBrowseView: ImageBrowseView, position: Int, item: PhotoModel) {
                    if (TextUtils.isEmpty(item.picPath)) {
                        imageBrowseView.load(item.localPath)
                    } else {
                        imageBrowseView.load(item.picPath)
                    }
                }

                override fun getInitCurrentItemPostion(): Int {
                    return position
                }

                override fun getInitList(): List<PhotoModel>? {
                    return mPhotoAdapter.dataList
                }

                override fun loadMore(backward: Boolean, position: Int, data: PhotoModel, callback: Callback<List<PhotoModel>>?) {
                    if (backward) {
                        // 向后加载
                        getPhotos(mUserId, mPhotoAdapter.successNum, DEFAUAT_CNT, Callback { r, list ->
                            if (callback != null && list != null) {
                                callback.onCallback(0, list)
                            }
                        })
                    }
                }

                override fun hasMore(backward: Boolean, position: Int, data: PhotoModel): Boolean {
                    return if (backward) {
                        mHasMore
                    } else false
                }

                override fun hasMenu(): Boolean {
                    return false
                }
            })
        }
        mPhotoView.adapter = mPhotoAdapter
    }

    fun getPhotos(isFlag: Boolean) {
        val now = System.currentTimeMillis()
        if (!isFlag) {
            // 10分钟更新一次吧
            if (now - mLastUpdateInfo < 10 * 60 * 1000) {
                return
            }
        }
        getPhotos(mUserId, 0, DEFAUAT_CNT, null)
    }

    fun getMorePhotos() {
        getPhotos(mUserId, offset, DEFAUAT_CNT, null)
    }

    private fun getPhotos(userId: Int, offset: Int, cnt: Int, callback: Callback<List<PhotoModel>>?) {
        ApiMethods.subscribe(mUserInfoServerApi.getPhotos(userId.toLong(), offset, cnt), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult?) {
                if (result!!.errno == 0) {
                    if (result != null && result.errno == 0) {
                        val list = JSON.parseArray(result.data!!.getString("pic"), PhotoModel::class.java)
                        val newOffset = result.data!!.getIntValue("offset")
                        val totalCount = result.data!!.getIntValue("totalCount")
                        if (offset == 0) {
                            addPhotos(list, newOffset, totalCount, true)
                        } else {
                            addPhotos(list, newOffset, totalCount, false)
                        }
                        callback?.onCallback(0, list)
                    }
                } else {
                    addPhotosFail()
                }
            }

            override fun onNetworkError(errorType: ApiObserver.ErrorType) {
                super.onNetworkError(errorType)
                addPhotosFail()
            }
        }, mFragment)
    }

    fun addPhotos(list: List<PhotoModel>?, newOffset: Int, totalNum: Int, clear: Boolean) {
        offset = newOffset
        mLastUpdateInfo = System.currentTimeMillis()

        mCallBack?.onRequestSucess(!list.isNullOrEmpty())

        if (clear) {
            mPhotoAdapter.dataList?.clear()
        }

        if (list != null && list.isNotEmpty()) {
            if (mPhotoView.layoutManager !is GridLayoutManager) {
                mPhotoView.layoutManager = GridLayoutManager(context, 3)
            }
            mHasMore = true
            mPhotoAdapter.dataList!!.addAll(list)
            mPhotoAdapter.notifyDataSetChanged()
            if (mListener != null) {
                mListener!!.notifyAppbarSroll(true)
            }
        } else {
            mHasMore = false
            if (mPhotoAdapter.dataList != null && mPhotoAdapter.dataList!!.size > 0) {
                // 没有更多了
            } else {
                // 没有数据
                mPhotoView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                if (mListener != null) {
                    mListener!!.notifyAppbarSroll(false)
                }
            }
        }
    }


    fun addPhotosFail() {
        if (mCallBack != null) {
            mCallBack!!.onRequestSucess(true)
        }
        if (mPhotoAdapter.dataList == null || mPhotoAdapter.dataList!!.size == 0) {
            if (mListener != null) {
                mListener!!.notifyAppbarSroll(false)
            }
        }
    }

    fun destory(){

    }

    interface AppCanSrollListener {
        fun notifyAppbarSroll(canScroll: Boolean)
    }
}
