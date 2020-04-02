package com.module.club.homepage.view

import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.common.base.BaseActivity
import com.common.callback.Callback
import com.common.core.userinfo.UserInfoManager
import com.common.core.userinfo.model.ClubMemberInfo
import com.common.log.MyLog
import com.component.busilib.R
import com.component.busilib.callback.EmptyCallback
import com.component.person.photo.adapter.PhotoAdapter
import com.component.person.photo.model.PhotoModel
import com.component.person.photo.view.IPhotoWallView
import com.component.person.view.RequestCallBack
import com.imagebrowse.ImageBrowseView
import com.imagebrowse.big.BigImageBrowseFragment
import com.imagebrowse.big.DefaultImageBrowserLoader
import com.kingja.loadsir.core.LoadService
import com.kingja.loadsir.core.LoadSir
import com.module.club.homepage.ClubPhotoCorePresenter
import com.respicker.ResPicker
import com.respicker.activity.ResPickerActivity
import com.respicker.model.ImageItem
import java.util.*

/**
 * 照片墙view
 */
class ClubPhotoWallView(private var mBaseActivity: BaseActivity, private var mCallBack: RequestCallBack?, val clubMemberInfo: ClubMemberInfo?) : RelativeLayout(mBaseActivity), IPhotoWallView {

    val TAG = "ClubPhotoWallView"

    internal var DEFAUAT_CNT = 20       // 默认拉取一页的数量
    var mHasMore = false

    private val mPhotoView: RecyclerView
    internal val mPhotoAdapter: PhotoAdapter
    internal val mPhotoCorePresenter: ClubPhotoCorePresenter

    private var mLastUpdateInfo: Long = 0    //上次更新成功时间

    private val mLoadService: LoadService<*>

    init {
        View.inflate(context, R.layout.photo_wall_view_layout, this)
        mPhotoView = findViewById<View>(R.id.photo_view) as RecyclerView

        mPhotoCorePresenter = ClubPhotoCorePresenter(this, context as BaseActivity, clubMemberInfo!!)

        mPhotoView.isFocusableInTouchMode = false
        val gridLayoutManager = GridLayoutManager(context, 3)
        mPhotoView.layoutManager = gridLayoutManager
        mPhotoAdapter = PhotoAdapter(PhotoAdapter.TYPE_CLUB_PHOTO)

        mPhotoAdapter.mOnClickAddPhotoListener = {
            goAddPhotoFragment()
        }
        mPhotoAdapter.mOnClickPhotoListener = { _, _, model ->
            // 跳到看大图
            BigImageBrowseFragment.open(true, mBaseActivity, object : DefaultImageBrowserLoader<PhotoModel>() {
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
                    loadDataUpdater(item, textView)
                }

                override fun getInitCurrentItemPostion(): Int {
                    return mPhotoAdapter.getPostionOfItem(model)
                }

                override fun getInitList(): List<PhotoModel>? {
                    return mPhotoAdapter.dataList
                }

                override fun loadMore(backward: Boolean, position: Int, data: PhotoModel, callback: Callback<List<PhotoModel>>?) {
                    if (backward) {
                        // 向后加载
                        mPhotoCorePresenter.getPhotos(mPhotoAdapter.successNum, DEFAUAT_CNT, object : Callback<List<PhotoModel>> {
                            override fun onCallback(r: Int, obj: List<PhotoModel>?) {
                                if (callback != null && obj != null) {
                                    callback.onCallback(0, obj)
                                }
                            }
                        })
                    }
                }

                override fun hasMore(backward: Boolean, position: Int, data: PhotoModel): Boolean {
                    return if (backward) {
                        mHasMore
                    } else false
                }

                override fun hasDeleteMenu(): Boolean {
                    return true
                }

                override fun getDeleteListener(): Callback<PhotoModel>? {
                    return Callback { r, obj -> mPhotoCorePresenter!!.deletePhoto(obj) }
                }
            })
        }

        mPhotoAdapter.mDeleteListener = { model ->
            model?.let {
                mPhotoCorePresenter.deletePhoto(it)
            }
        }

        mPhotoAdapter.mReUploadListener = { model ->
            model?.let {
                val photoModelArrayList = ArrayList<PhotoModel>(1)
                photoModelArrayList.add(it)
                mPhotoCorePresenter.upload(photoModelArrayList, true)
            }
        }
        mPhotoView.adapter = mPhotoAdapter
        mPhotoCorePresenter.loadUnSuccessPhotoFromDB()

        val mLoadSir = LoadSir.Builder()
                .addCallback(EmptyCallback(com.module.club.R.drawable.loading_empty2, "暂无相片", "#99000000"))
                .build()
        mLoadService = mLoadSir.register(mPhotoView, com.kingja.loadsir.callback.Callback.OnReloadListener {
            loadData(true,null)
        })
    }

    private fun loadDataUpdater(item: PhotoModel?, textView: TextView) {
        if (item?.picID != null && item.picID != 0) {
            mPhotoCorePresenter.getPicDetail(item.picID) { model ->
                val remarkName = UserInfoManager.getInstance().getRemarkName(model?.picInfo?.userID
                        ?: 0, model?.nickName)
                if (!TextUtils.isEmpty(remarkName)) {
                    textView.visibility = View.VISIBLE
                    textView.text = remarkName
                } else {
                    textView.visibility = View.GONE
                }
            }
        } else {
            textView.visibility = View.GONE
        }
    }

    fun uploadPhotoList(imageItems: List<ImageItem>) {
        mPhotoCorePresenter.uploadPhotoList(imageItems)
    }

    fun getPhotos(isFlag: Boolean,callback:(()->Unit?)?) {
        MyLog.d(TAG, "getPhotos isFlag = $isFlag")
        val now = System.currentTimeMillis()
        if (!isFlag) {
            // 10分钟更新一次吧
            if (now - mLastUpdateInfo < 10 * 60 * 1000) {
                return
            }
        }
        if (mPhotoAdapter.successNum == 0) {
            mPhotoCorePresenter!!.getPhotos(0, DEFAUAT_CNT, Callback { r, obj -> callback?.invoke() })
        }else{
            callback?.invoke()
        }
    }

    fun getMorePhotos(callback: () -> Unit?) {
        mPhotoCorePresenter!!.getPhotos(mPhotoAdapter.successNum, DEFAUAT_CNT, object : Callback<List<PhotoModel>> {
            override fun onCallback(r: Int, obj: List<PhotoModel>?) {
                callback.invoke()
            }
        })
    }

    fun goAddPhotoFragment() {
        ResPicker.getInstance().params = ResPicker.newParamsBuilder()
                .setMultiMode(true)
                .setShowCamera(true)
                .setCrop(false)
                .setSelectLimit(9)
                .setIncludeGif(false)
                .build()
        ResPickerActivity.open(mBaseActivity)
    }


    override fun addPhoto(newOffset: Int, list: List<PhotoModel>?, clear: Boolean, totalNum: Int) {
        MyLog.d(TAG, "showPhoto list=$list clear=$clear totalNum=$totalNum")
        mLastUpdateInfo = System.currentTimeMillis()

        if (list != null && list.isNotEmpty()) {
            mCallBack?.onRequestSucess(true)
            // 有数据
            mHasMore = true
            //            mSmartRefresh.setEnableLoadMore(true);
            if (clear) {
                mPhotoAdapter.dataList?.clear()
                mPhotoAdapter.dataList?.addAll(list)
                mPhotoAdapter.notifyDataSetChanged()
            } else {
                mPhotoAdapter.insertLast(list)
            }
            mLoadService.showSuccess()
        } else {
            mCallBack?.onRequestSucess(false)
            mHasMore = false
            //            mSmartRefresh.setEnableLoadMore(false);
            if (mPhotoAdapter.dataList != null && mPhotoAdapter.dataList!!.size > 0) {
                // 没有更多了
            } else {
                // 没有数据
                mLoadService.showCallback(EmptyCallback::class.java)
            }
        }
    }

    fun loadData(flag: Boolean, callback: (() -> Unit?)?) {
        getPhotos(flag,callback)
    }

    fun loadMoreData(callback: () -> Unit?) {
        getMorePhotos(callback)
    }

    override fun insertPhoto(photoModel: PhotoModel) {
        mPhotoAdapter.insertFirst(photoModel)
    }

    override fun deletePhoto(photoModel: PhotoModel, numchange: Boolean) {
        if (numchange) {
            //            mTotalPhotoNum--;
            //            setPhotoNum();
        }
        mPhotoAdapter.delete(photoModel)
    }


    override fun updatePhoto(photoModel: PhotoModel) {
        if (photoModel.status == PhotoModel.STATUS_SUCCESS) {
            //            mTotalPhotoNum++;
            //            setPhotoNum();
        }
        mPhotoAdapter.update(photoModel)
    }

    override fun loadDataFailed() {
        mCallBack?.onRequestSucess(true)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mPhotoCorePresenter?.destroy()
    }

    fun destroy() {

    }
}
