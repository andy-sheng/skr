package com.module.playways.songmanager.view

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.FrameLayout

import com.alibaba.fastjson.JSONObject
import com.common.core.myinfo.MyUserInfoManager
import com.common.log.MyLog
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.utils.U
import com.common.utils.dp
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
import com.module.playways.songmanager.SongManagerServerApi
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
    val mSongManageServerApi: SongManagerServerApi = ApiManager.getInstance().createService(SongManagerServerApi::class.java)

    private var mRecommendSongAdapter: RecommendSongAdapter
    private var mDisposable: Disposable? = null
    private var mOffset = 0
    private var mLimit = 20
    private var mMakeGamePanelView: MakeGamePanelView? = null

    private val mDrawableBg = DrawableCreator.Builder()
            .setSolidColor(U.getColor(R.color.white_trans_20))
            .setCornersRadius(0f, 0f, U.getDisplayUtils().dip2px(8f).toFloat(), U.getDisplayUtils().dip2px(8f).toFloat())
            .build()

    private val mMicDrawableBg = DrawableCreator.Builder()
            .setSolidColor(Color.parseColor("#576FE3"))
            .setCornersRadius(8.dp().toFloat())
            .build()

    init {
        View.inflate(context, R.layout.recommend_song_view_layout, this)
        mContainer = findViewById(R.id.container)
        mRecyclerView = findViewById(R.id.recycler_view)
        mRefreshLayout = findViewById(R.id.refreshLayout)

        mRecyclerView.layoutManager = LinearLayoutManager(context)
        if (mType == SongManagerActivity.TYPE_FROM_GRAB) {
            mRecommendSongAdapter = RecommendSongAdapter(isOwner, mType, RecyclerOnItemClickListener { view, position, model ->
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
        } else if (mType == SongManagerActivity.TYPE_FROM_MIC) {
            // 排麦房
            mContainer.background = mMicDrawableBg
            mRecommendSongAdapter = RecommendSongAdapter(true, mType, RecyclerOnItemClickListener { view, position, model -> EventBus.getDefault().post(AddSongEvent(model)) })
        } else if (mType == SongManagerActivity.TYPE_FROM_RELAY_ROOM) {
            mContainer.background = mMicDrawableBg
            mRecommendSongAdapter = RecommendSongAdapter(true, mType, RecyclerOnItemClickListener { view, position, model -> EventBus.getDefault().post(AddSongEvent(model)) })
        } else {
            /**
             * 双人房默认是直接 点唱
             */
            mContainer.background = mDrawableBg
            mRecommendSongAdapter = RecommendSongAdapter(true, mType, RecyclerOnItemClickListener { view, position, model -> EventBus.getDefault().post(AddSongEvent(model)) })
        }

        mRecyclerView.adapter = mRecommendSongAdapter

        mRefreshLayout.setEnableRefresh(false)
        mRefreshLayout.setEnableLoadMore(true)
        mRefreshLayout.setEnableLoadMoreWhenContentNotFull(false)
        mRefreshLayout.setEnableOverScrollDrag(false)

        mRefreshLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {
                getSongList(mOffset, false)
            }

            override fun onRefresh(refreshLayout: RefreshLayout) {
                getSongList(0, true)
            }
        })
    }

    fun tryLoad() {
        // 确认一下，历史需要即使更新么
        if (mRecommendSongAdapter.dataList.isEmpty()) {
            getSongList(0, true)
        }
    }

    private fun getSongList(offset: Int, isClean: Boolean) {
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
                    val recommendTagModelArrayList = if (mType == SongManagerActivity.TYPE_FROM_MIC) {
                        JSONObject.parseArray(result.data!!.getString("details"), SongModel::class.java)
                    } else {
                        JSONObject.parseArray(result.data!!.getString("items"), SongModel::class.java)
                    }
                    mOffset = result.data!!.getIntValue("offset")

                    addRecommendList(recommendTagModelArrayList, isClean)
                } else {
                    U.getToastUtil().showShort(result.errmsg + "")
                }

            }
        })
    }

    private fun addRecommendList(list: List<SongModel>?, clean: Boolean) {
        mRefreshLayout.finishRefresh()
        mRefreshLayout.finishLoadMore()
        mRefreshLayout.setEnableLoadMore(!list.isNullOrEmpty())

        if (clean) {
            mRecommendSongAdapter.dataList.clear()
            if (mRecommendTagModel?.type == 4 && isOwner) {
                // 是双人游戏那一例
                val songModel = SongModel()
                songModel.itemID = SongModel.ID_CUSTOM_GAME
                songModel.playType = StandPlayType.PT_MINI_GAME_TYPE.value
                songModel.itemName = "自定义游戏"
                mRecommendSongAdapter.dataList.add(songModel)
            }
            if (!list.isNullOrEmpty()) {
                mRecommendSongAdapter.dataList.addAll(list)
            }
            mRecommendSongAdapter.notifyDataSetChanged()
        } else {
            if (!list.isNullOrEmpty()) {
                mRecommendSongAdapter.dataList.addAll(list)
                mRecommendSongAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun getListStandBoardObservable(offset: Int): Observable<ApiResult> {
        val tab = mRecommendTagModel?.type ?: 0
        return when (mType) {
            SongManagerActivity.TYPE_FROM_GRAB -> mSongManageServerApi.getListStandBoards(tab, offset, mLimit)
            SongManagerActivity.TYPE_FROM_MIC -> mSongManageServerApi.getMicSongList(offset, mLimit, MyUserInfoManager.uid.toInt(), tab)
            SongManagerActivity.TYPE_FROM_RELAY_ROOM -> mSongManageServerApi.getRelaySongList(offset, mLimit, MyUserInfoManager.uid.toInt(), tab)
            else -> mSongManageServerApi.getDoubleListStandBoards(tab, offset, mLimit)
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
