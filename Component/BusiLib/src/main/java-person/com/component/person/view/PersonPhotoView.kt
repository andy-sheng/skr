package com.component.person.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import com.alibaba.fastjson.JSON
import com.common.callback.Callback
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.UserInfoServerApi
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.view.ex.ExTextView
import com.component.busilib.R
import com.component.person.photo.adapter.PhotoAdapter
import com.component.person.photo.model.PhotoModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

// 个人主页照片
class PersonPhotoView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : ConstraintLayout(context, attrs, defStyleAttr), CoroutineScope by MainScope() {

    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

    private val userInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi::class.java)

    private val photoTitleTv: TextView
    private val recyclerView: RecyclerView
    private val photoNumTv: ExTextView
    private val photoAdapter: PhotoAdapter

    init {
        View.inflate(context, R.layout.person_center_photo_view, this)

        photoTitleTv = rootView.findViewById(R.id.photo_title_tv)
        recyclerView = this.findViewById(R.id.recycler_view)
        photoNumTv = this.findViewById(R.id.photo_num_tv)

        recyclerView.layoutManager = GridLayoutManager(context, 3)
        photoAdapter = PhotoAdapter(PhotoAdapter.TYPE_PERSON_CENTER_VIEW)
        recyclerView.adapter = photoAdapter
    }

    fun initData(flag: Boolean) {
        getPhotos(0)
    }

    private fun getPhotos(userID: Long) {
        ApiMethods.subscribe(userInfoServerApi.getPhotos(userID, 0, 20), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult?) {
                if (result != null && result.errno == 0) {
                    val list = JSON.parseArray(result.data?.getString("pic"), PhotoModel::class.java)
                    val totalCount = result.data!!.getIntValue("totalCount")
                    addPhotos(list, totalCount, true)
                }
            }

            override fun onNetworkError(errorType: ApiObserver.ErrorType) {
                super.onNetworkError(errorType)
            }
        })
    }

    private fun addPhotos(list: List<PhotoModel>?, totalCount: Int, clear: Boolean) {
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