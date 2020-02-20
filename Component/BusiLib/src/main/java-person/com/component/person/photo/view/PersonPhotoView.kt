package com.component.person.photo.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.alibaba.fastjson.JSON
import com.common.callback.Callback
import com.common.core.userinfo.UserInfoServerApi
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.utils.U
import com.common.view.ex.ExTextView
import com.component.busilib.R
import com.component.person.photo.adapter.PhotoAdapter
import com.component.person.photo.model.PhotoModel
import com.imagebrowse.ImageBrowseView
import com.imagebrowse.big.BigImageBrowseFragment
import com.imagebrowse.big.DefaultImageBrowserLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

// 个人主页照片
class PersonPhotoView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : ConstraintLayout(context, attrs, defStyleAttr), CoroutineScope by MainScope() {

    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

    private val userInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi::class.java)
    private var mLastUpdateTime: Long = 0  // 上次刷新时间

    private val photoTitleTv: TextView
    private val recyclerView: RecyclerView
    private val photoNumTv: ExTextView
    private val photoTitleArrow: ImageView
    private val photoArrow: ImageView

    private val photoAdapter: PhotoAdapter

    private var userID:Long = 0
    private var mHasMore = false

    init {
        View.inflate(context, R.layout.person_center_photo_view, this)

        photoTitleTv = rootView.findViewById(R.id.photo_title_tv)
        recyclerView = this.findViewById(R.id.recycler_view)
        photoNumTv = this.findViewById(R.id.photo_num_tv)

        photoTitleArrow = this.findViewById(R.id.photo_title_arrow)
        photoArrow = this.findViewById(R.id.photo_arrow)

        recyclerView.layoutManager = GridLayoutManager(context, 3)
        photoAdapter = PhotoAdapter(PhotoAdapter.TYPE_PERSON_CENTER_VIEW)
        recyclerView.adapter = photoAdapter

        photoAdapter.mOnClickPhotoListener = { _, position, _ ->
            BigImageBrowseFragment.open(true, context as FragmentActivity, object : DefaultImageBrowserLoader<PhotoModel>() {
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
                    return photoAdapter?.mDataList
                }

                override fun loadMore(backward: Boolean, position: Int, data: PhotoModel, callback: Callback<List<PhotoModel>>?) {
                    if (backward) {
                        // 向后加载
                        getPhotos(photoAdapter?.successNum ?: 0, Callback { r, list ->
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
            })
        }
    }

    fun initData(userID: Long, flag: Boolean) {
        // 也设一个时间间隔吧
        this.userID = userID
        val now = System.currentTimeMillis()
        if (!flag) {
            if (now - mLastUpdateTime < 60 * 1000) {
                return
            }
        }
        getPhotos(0)
    }

    private fun getPhotos(off: Int, callback: Callback<List<PhotoModel>>? = null) {
        ApiMethods.subscribe(userInfoServerApi.getPhotos(userID, off, 20), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult?) {
                if (result != null && result.errno == 0) {
                    mLastUpdateTime = System.currentTimeMillis()
                    val list = JSON.parseArray(result.data?.getString("pic"), PhotoModel::class.java)
                    val totalCount = result.data!!.getIntValue("totalCount")
                    if (off == 0) {
                        addPhotos(list, totalCount, true)
                    } else {
                        addPhotos(list, totalCount, false)
                    }
                    callback?.onCallback(0, list)
                }
            }

            override fun onNetworkError(errorType: ApiObserver.ErrorType) {
                super.onNetworkError(errorType)
            }
        })
    }

    private fun addPhotos(list: List<PhotoModel>?, totalCount: Int, clear: Boolean) {
        mHasMore = !list.isNullOrEmpty()
        if (totalCount == 0) {
            photoArrow.visibility = View.GONE
            photoTitleArrow.visibility = View.VISIBLE
        } else {
            photoArrow.visibility = View.VISIBLE
            photoTitleArrow.visibility = View.GONE
        }
        if (clear) {
            photoAdapter.mDataList?.clear()
            if (!list.isNullOrEmpty()) {
                photoAdapter.mDataList?.addAll(list)
            }
            photoAdapter.notifyDataSetChanged()
        } else {
            if (!list.isNullOrEmpty()) {
                if (photoAdapter.mDataList?.size ?: 0 >= 3) {
                    photoAdapter.mDataList?.addAll(list)
                } else {
                    photoAdapter.mDataList?.addAll(list)
                    photoAdapter.notifyDataSetChanged()
                }
            }
        }

        if (photoAdapter.mDataList?.isNullOrEmpty() == true) {
            recyclerView.visibility = View.GONE
        } else {
            recyclerView.visibility = View.VISIBLE
        }

        if (totalCount >= 3) {
            photoNumTv.visibility = View.VISIBLE
            photoNumTv.text = "${totalCount}张"
        } else {
            photoNumTv.visibility = View.GONE
        }
    }

    fun destory() {
        cancel()
    }
}