package com.component.person.photo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.common.callback.Callback
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.view.setDebounceViewClickListener
import com.common.log.MyLog
import com.component.busilib.R
import com.component.busilib.callback.EmptyCallback
import com.component.person.photo.adapter.PhotoAdapter
import com.component.person.photo.model.PhotoModel
import com.component.person.photo.presenter.PersonPhotoPresenter
import com.component.person.photo.view.IPhotoWallView
import com.imagebrowse.ImageBrowseView
import com.imagebrowse.big.BigImageBrowseFragment
import com.imagebrowse.big.DefaultImageBrowserLoader
import com.kingja.loadsir.core.LoadService
import com.kingja.loadsir.core.LoadSir
import com.module.RouterConstants
import com.respicker.ResPicker
import com.respicker.activity.ResPickerActivity
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import com.umeng.socialize.utils.DeviceConfig.context
import java.util.ArrayList

// 照片页面
@Route(path = RouterConstants.ACTIVITY_PERSON_PHOTO)
class PersonPhotoActivity : BaseActivity(), IPhotoWallView {

    lateinit var backIv: ImageView
    lateinit var divider: View
    lateinit var refreshLayout: SmartRefreshLayout
    lateinit var recyclerView: RecyclerView

    var adapter: PhotoAdapter? = null
    var presenter: PersonPhotoPresenter? = null

    private var mLoadService: LoadService<*>? = null

    var userID: Int = 0

    private var offset = 0
    private var mHasMore = false
    private val DEFAUAT_CNT = 20       // 默认拉取一页的数量
    private var mLastUpdateInfo: Long = 0    //上次更新成功时间

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.person_photo_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        userID = intent.getIntExtra("userID", 0)
        if (userID == 0) {
            MyLog.w(TAG, "PersonPhotoActivity userID = $userID")
            finish()
        }

        backIv = findViewById(R.id.back_iv)
        divider = findViewById(R.id.divider)
        refreshLayout = findViewById(R.id.refreshLayout)
        recyclerView = findViewById(R.id.recycler_view)

        backIv.setDebounceViewClickListener {
            finish()
        }

        refreshLayout.apply {
            setEnableLoadMore(true)
            setEnableRefresh(true)
            setEnableLoadMoreWhenContentNotFull(false)
            setEnableOverScrollDrag(false)

            setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
                override fun onLoadMore(refreshLayout: RefreshLayout) {
                    getMorePhotos()
                }

                override fun onRefresh(refreshLayout: RefreshLayout) {
                    getPhotos(true)
                }
            })
        }

        presenter = PersonPhotoPresenter(this)
        addPresent(presenter)

        val mLoadSir = LoadSir.Builder()
                .addCallback(EmptyCallback(R.drawable.photo_wall_empty_icon, "暂无照片", "#80000000"))
                .build()
        mLoadService = mLoadSir.register(refreshLayout) { getPhotos(true) }

        adapter = if (userID == MyUserInfoManager.uid.toInt()) {
            PhotoAdapter(PhotoAdapter.TYPE_PERSON_CENTER)
        } else {
            PhotoAdapter(PhotoAdapter.TYPE_OTHER_PERSON_CENTER)
        }
        val gridLayoutManager = GridLayoutManager(context, 3)
        recyclerView.layoutManager = gridLayoutManager
        recyclerView.adapter = adapter
        adapter?.mOnClickPhotoListener = { _, position, model ->
            // 跳到看大图
            BigImageBrowseFragment.open(true, this, object : DefaultImageBrowserLoader<PhotoModel>() {
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
                    return if (userID == MyUserInfoManager.uid.toInt()) {
                        adapter?.getPositionOfItem(model) ?: 0
                    } else {
                        position
                    }
                }

                override fun getInitList(): List<PhotoModel>? {
                    return adapter?.dataList
                }

                override fun loadMore(backward: Boolean, position: Int, data: PhotoModel, callback: Callback<List<PhotoModel>>?) {
                    if (backward) {
                        // 向后加载
                        presenter?.getPhotos(userID, adapter?.successNum
                                ?: 0, DEFAUAT_CNT, Callback<List<PhotoModel>> { r, obj ->
                            if (callback != null && obj != null) {
                                callback.onCallback(0, obj)
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
                    return userID == MyUserInfoManager.uid.toInt()
                }

                override fun getDeleteListener(): Callback<PhotoModel>? {
                    return Callback { r, obj -> presenter?.deletePhoto(obj) }
                }
            })
        }

        if (userID == MyUserInfoManager.uid.toInt()) {
            adapter?.mOnClickAddPhotoListener = {
                goAddPhotoFragment()
            }

            adapter?.mDeleteListener = { model ->
                model?.let {
                    presenter?.deletePhoto(it)
                }
            }

            adapter?.mReUploadListener = { model ->
                model?.let {
                    val photoModelArrayList = ArrayList<PhotoModel>(1)
                    photoModelArrayList.add(it)
                    presenter?.upload(photoModelArrayList, true)
                }
            }
            presenter?.loadUnSuccessPhotoFromDB()
        }

        getPhotos(false)
    }

    private fun getPhotos(flag: Boolean) {
        val now = System.currentTimeMillis()
        if (!flag) {
            // 10分钟更新一次吧
            if (now - mLastUpdateInfo < 10 * 60 * 1000) {
                return
            }
        }
        if (userID == MyUserInfoManager.uid.toInt()) {
            if (adapter?.successNum == 0) {
                presenter?.getPhotos(userID, 0, DEFAUAT_CNT)
            } else {
                finshLoadMoreRefresh()
            }
        } else {
            presenter?.getPhotos(userID, 0, DEFAUAT_CNT)
        }
    }

    private fun getMorePhotos() {
        if (userID == MyUserInfoManager.uid.toInt()) {
            presenter?.getPhotos(userID, adapter?.successNum ?: 0, DEFAUAT_CNT)
        } else {
            presenter?.getPhotos(userID, offset, DEFAUAT_CNT)
        }
    }

    private fun goAddPhotoFragment() {
        ResPicker.getInstance().params = ResPicker.newParamsBuilder()
                .setMultiMode(true)
                .setShowCamera(true)
                .setCrop(false)
                .setSelectLimit(9)
                .setIncludeGif(false)
                .build()
        ResPickerActivity.open(this)
    }

    override fun addPhoto(newOffset: Int, list: List<PhotoModel>?, clear: Boolean, totalNum: Int) {
        MyLog.d(TAG, "showPhoto list=$list clear=$clear totalNum=$totalNum")
        finshLoadMoreRefresh()
        offset = newOffset
        mLastUpdateInfo = System.currentTimeMillis()

        if (list != null && list.isNotEmpty()) {
            mHasMore = true
            if (clear) {
                adapter?.dataList?.clear()
                adapter?.dataList?.addAll(list)
                adapter?.notifyDataSetChanged()
            } else {
                adapter?.insertLast(list)
            }
        } else {
            mHasMore = false
        }

        if (userID != MyUserInfoManager.uid.toInt()) {
            if (adapter?.dataList != null && (adapter?.dataList?.size ?: 0) > 0) {
                // 没有更多了
                mLoadService?.showSuccess()
            } else {
                // 没有数据
                mLoadService?.showCallback(EmptyCallback::class.java)

            }
        }
    }

    override fun insertPhoto(photoModel: PhotoModel) {
        adapter?.insertFirst(photoModel)
    }

    override fun deletePhoto(photoModel: PhotoModel, numchange: Boolean) {
        if (numchange) {
            //            mTotalPhotoNum--;
            //            setPhotoNum();
        }
        adapter?.delete(photoModel)
    }

    override fun updatePhoto(imageItem: PhotoModel) {
        if (imageItem.status == PhotoModel.STATUS_SUCCESS) {
            //            mTotalPhotoNum++;
            //            setPhotoNum();
        }
        adapter?.update(imageItem)
    }

    override fun loadDataFailed() {
        finshLoadMoreRefresh()
    }

    private fun finshLoadMoreRefresh() {
        refreshLayout.finishLoadMore()
        refreshLayout.finishRefresh()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == ResPickerActivity.REQ_CODE_RES_PICK) {
            val imageItems = ResPicker.getInstance().selectedImageList
            presenter?.uploadPhotoList(imageItems)
        }
    }

    override fun useEventBus(): Boolean {
        return false
    }

    override fun canSlide(): Boolean {
        return false
    }
}