package com.module.playways.songmanager.view

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.FrameLayout

import com.alibaba.fastjson.JSONObject
import com.common.log.MyLog
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.utils.U
import com.common.view.ex.ExFrameLayout
import com.common.view.ex.drawable.DrawableCreator
import com.common.view.recyclerview.RecyclerOnItemClickListener
import com.module.playways.R
import com.module.playways.grab.room.GrabRoomServerApi
import com.module.playways.songmanager.SongManagerActivity
import com.module.playways.songmanager.adapter.RecommendSongAdapter
import com.module.playways.songmanager.customgame.MakeGamePanelView
import com.module.playways.songmanager.event.AddSongEvent
import com.module.playways.songmanager.model.RecommendTagModel
import com.module.playways.room.song.model.SongModel
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import com.zq.live.proto.Common.StandPlayType

import org.greenrobot.eventbus.EventBus

import io.reactivex.Observable
import io.reactivex.disposables.Disposable

/**
 * 推荐歌曲view
 */
class RecommendSongView(context: Context, internal var mType: Int,
                        internal var isOwner: Boolean,
                        internal var mGameID: Int,
                        private val mRecommendTagModel: RecommendTagModel?) : FrameLayout(context) {

    val TAG = "GrabSongManageView"

    val mContainer: ExFrameLayout
    val mRecyclerView: RecyclerView
    val mRefreshLayout: SmartRefreshLayout
    val mGrabRoomServerApi: GrabRoomServerApi

    lateinit var mRecommendSongAdapter: RecommendSongAdapter
    private var mDisposable: Disposable? = null
    private var mOffset = 0
    private var mLimit = 20
    private var mMakeGamePanelView: MakeGamePanelView? = null

    val mDrawableBg = DrawableCreator.Builder()
            .setSolidColor(U.getColor(R.color.white_trans_20))
            .setCornersRadius(0f, 0f, U.getDisplayUtils().dip2px(8f).toFloat(), U.getDisplayUtils().dip2px(8f).toFloat())
            .build()

    init {
        View.inflate(context, R.layout.recommend_song_view_layout, this)
        mContainer = findViewById(R.id.container)
        mRecyclerView = findViewById(R.id.recycler_view)
        mRefreshLayout = findViewById(R.id.refreshLayout)

        mGrabRoomServerApi = ApiManager.getInstance().createService(GrabRoomServerApi::class.java)
        initData()
    }

    fun initData() {

        mRecyclerView.layoutManager = LinearLayoutManager(context)
        if (mType == SongManagerActivity.TYPE_FROM_GRAB) {
            mRecommendSongAdapter = RecommendSongAdapter(isOwner, RecyclerOnItemClickListener { view, position, model ->
                if (isOwner && model != null && model.itemID == SongModel.ID_CUSTOM_GAME) {
                    if (mMakeGamePanelView != null) {
                        mMakeGamePanelView!!.dismiss()
                    }
                    mMakeGamePanelView = MakeGamePanelView(context)
                    mMakeGamePanelView!!.showByDialog(mGameID)
                } else {
                    EventBus.getDefault().post(AddSongEvent(model!!))
                }
            })
        } else {
            /**
             * 双人房默认是直接 点唱
             */
            mContainer.background = mDrawableBg
            mRecommendSongAdapter = RecommendSongAdapter(true, RecyclerOnItemClickListener { view, position, model -> EventBus.getDefault().post(AddSongEvent(model)) })
        }

        mRecyclerView.adapter = mRecommendSongAdapter

        mRefreshLayout.setEnableRefresh(false)
        mRefreshLayout.setEnableLoadMore(true)
        mRefreshLayout.setEnableLoadMoreWhenContentNotFull(false)
        mRefreshLayout.setEnableOverScrollDrag(false)

        mRefreshLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {
                getSongList(mOffset)
            }

            override fun onRefresh(refreshLayout: RefreshLayout) {
                getSongList(0)
            }
        })
        getSongList(0)
    }

    fun tryLoad() {
        if (mRecommendSongAdapter.dataList.isEmpty()) {
            getSongList(0)
        }
    }

    private fun getSongList(offset: Int) {
        if (mRecommendTagModel == null) {
            MyLog.e(TAG, "getSongList mRecommendTagModel is null")
            return
        }

        if (mDisposable != null && !mDisposable!!.isDisposed) {
            mDisposable!!.dispose()
        }
        mDisposable = ApiMethods.subscribe(getListStandBoardObservable(offset), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                mRefreshLayout.finishLoadMore()

                if (result.errno == 0) {
                    val recommendTagModelArrayList = JSONObject.parseArray(result.data!!.getString("items"), SongModel::class.java)
                    mOffset = result.data!!.getIntValue("offset")
                    if (recommendTagModelArrayList == null || recommendTagModelArrayList.size == 0) {
                        mRefreshLayout.setEnableLoadMore(false)
                        return
                    }
                    if (offset == 0) {
                        mRecommendSongAdapter.dataList.clear()
                        if (mRecommendTagModel.type == 4 && isOwner) {
                            // 是双人游戏那一例
                            val songModel = SongModel()
                            songModel.itemID = SongModel.ID_CUSTOM_GAME
                            songModel.playType = StandPlayType.PT_MINI_GAME_TYPE.value
                            songModel.itemName = "自定义游戏"
                            mRecommendSongAdapter.dataList.add(songModel)
                        }
                    }
                    mRecommendSongAdapter.dataList.addAll(recommendTagModelArrayList)
                    mRecommendSongAdapter.notifyDataSetChanged()
                } else {
                    U.getToastUtil().showShort(result.errmsg + "")
                }

            }
        })
    }

    private fun getListStandBoardObservable(offset: Int): Observable<ApiResult> {
        return if (mType == SongManagerActivity.TYPE_FROM_GRAB) {
            mGrabRoomServerApi.getListStandBoards(mRecommendTagModel!!.type, offset, mLimit)
        } else {
            mGrabRoomServerApi.getDoubleListStandBoards(mRecommendTagModel!!.type, offset, mLimit)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        destroy()
    }

    fun destroy() {
        if (mDisposable != null && !mDisposable!!.isDisposed) {
            mDisposable!!.dispose()
        }
    }
}
