package com.component.person.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import com.alibaba.fastjson.JSON
import com.common.callback.Callback
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.UserInfoManager
import com.common.core.userinfo.UserInfoServerApi
import com.common.rxretrofit.*
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.component.busilib.R
import com.component.person.photo.adapter.PhotoAdapter
import com.component.person.photo.model.PhotoModel
import com.imagebrowse.ImageBrowseView
import com.imagebrowse.big.BigImageBrowseFragment
import com.imagebrowse.big.DefaultImageBrowserLoader
import io.reactivex.Observable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

class PhotoHorizView(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : ConstraintLayout(context, attrs, defStyleAttr) {

    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

    private val photoViewBg: ExImageView
    private val photoView: RecyclerView
    private val photoNumTv: ExTextView
    private val divider: View

    private val photoAdapter: PhotoAdapter
    private var mHasMore = false
    private var mOffset = 0
    private var DEFAULT_CNT = 10

    var userID: Int = 0   //给个人卡片的照片上用的
    var clubID: Int = 0   //给家族卡片的照片上用的
    private var isMyClub = false

    private val userInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi::class.java)

    init {
        View.inflate(context, R.layout.photo_horiz_view_layout, this)

        photoViewBg = this.findViewById(R.id.photo_view_bg)
        photoView = this.findViewById(R.id.photo_view)
        photoNumTv = this.findViewById(R.id.photo_num_tv)
        divider = this.findViewById(R.id.divider)

        photoView.isFocusableInTouchMode = false
        photoView.layoutManager = GridLayoutManager(context, 3)
        photoAdapter = PhotoAdapter(PhotoAdapter.TYPE_PERSON_CARD)
        photoView.adapter = photoAdapter
        photoAdapter?.mOnClickPhotoListener = { _, position, _ ->
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

                override fun loadUpdater(textView: TextView, position: Int, item: PhotoModel?) {
                    if (clubID != 0) {
                        // 这是家族的卡片
                        loadDataUpdater(item, textView)
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

    fun setIsMyClub(flag: Boolean) {
        this.isMyClub = flag
        if (isMyClub) {
            divider.visibility = View.GONE
        }
    }

    private fun loadDataUpdater(item: PhotoModel?, textView: TextView) {
        if (item?.picID != null && item.picID != 0) {
            ApiMethods.subscribe(userInfoServerApi.getClubPhotoDetail(item.picID), object : ApiObserver<ApiResult>() {
                override fun process(result: ApiResult?) {
                    if (result?.errno == 0) {
                        val nickName = result.data.getString("nickName")
                        val userID = result.data.getJSONObject("picInfo").getIntValue("userID")
                        val remarkName = UserInfoManager.getInstance().getRemarkName(userID, nickName)
                        if (!TextUtils.isEmpty(remarkName)) {
                            textView.visibility = View.VISIBLE
                            textView.text = remarkName
                        } else {
                            textView.visibility = View.GONE
                        }
                    } else {
                        textView.visibility = View.GONE
                    }
                }

                override fun onNetworkError(errorType: ApiObserver.ErrorType) {
                    super.onNetworkError(errorType)
                    textView.visibility = View.GONE
                }
            }, RequestControl("getPicDetail", ControlType.CancelThis))
        } else {
            textView.visibility = View.GONE
        }
    }

    internal fun getPhotos(off: Int, callback: Callback<List<PhotoModel>>? = null) {
        val observable = if (clubID != 0) {
            userInfoServerApi?.getClubPhotos(clubID.toLong(), off, DEFAULT_CNT)
        } else {
            userInfoServerApi?.getPhotos(userID.toLong(), off, DEFAULT_CNT)
        }

        ApiMethods.subscribe(observable, object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult?) {
                if (result != null && result.errno == 0) {
                    mOffset = result.data!!.getIntValue("offset")
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

    fun addPhotos(list: List<PhotoModel>?, totalCount: Int, clear: Boolean) {
        mHasMore = !list.isNullOrEmpty()
        if (clear) {
            photoAdapter?.mDataList?.clear()
            if (!list.isNullOrEmpty()) {
                photoAdapter?.mDataList?.addAll(list)
            }
            photoAdapter?.notifyDataSetChanged()
        } else {
            if (!list.isNullOrEmpty()) {
                if (photoAdapter?.mDataList?.size ?: 0 >= 3) {
                    photoAdapter?.mDataList?.addAll(list)
                } else {
                    photoAdapter?.mDataList?.addAll(list)
                    photoAdapter?.notifyDataSetChanged()
                }
            }
        }

        if (photoAdapter?.mDataList?.isNullOrEmpty() == true) {
            photoView.visibility = View.GONE
            photoViewBg.visibility = View.GONE
            if (isMyClub) {
                divider.visibility = View.GONE
            } else {
                divider.visibility = View.VISIBLE
            }
        } else {
            photoView.visibility = View.VISIBLE
            photoViewBg.visibility = View.VISIBLE
            if (userID != MyUserInfoManager.uid.toInt()) {
                divider.visibility = View.GONE
            }
        }

        if (totalCount >= 3) {
            photoNumTv.visibility = View.VISIBLE
            photoNumTv.text = "${totalCount}张"
        } else {
            photoNumTv.visibility = View.GONE
        }
    }
}