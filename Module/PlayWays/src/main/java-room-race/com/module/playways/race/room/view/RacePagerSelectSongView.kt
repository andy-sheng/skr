package com.module.playways.race.room.view

import android.content.Context
import android.os.Handler
import android.os.Message
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import com.alibaba.fastjson.JSON
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.view.setDebounceViewClickListener
import com.common.log.MyLog
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.view.ex.ExConstraintLayout
import com.common.view.ex.ExImageView
import com.component.busilib.view.recyclercardview.CardScaleHelper
import com.component.busilib.view.recyclercardview.SpeedRecyclerView
import com.module.playways.race.RaceRoomServerApi
import com.module.playways.race.room.RaceRoomData
import com.module.playways.race.room.adapter.RaceSelectSongRecyclerAdapter
import com.module.playways.race.room.event.RaceWantSingChanceEvent
import com.module.playways.race.room.model.RaceGamePlayInfo
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class RacePagerSelectSongView : ExConstraintLayout {
    val TAG = "RacePagerSelectSongView"

    var hideClickArea: View
    var mPagerRootView: View
    var mRecyclerView: SpeedRecyclerView
    var mRoomData: RaceRoomData? = null
    var mPagerAdapter: RaceSelectSongRecyclerAdapter? = null
    var mHasSignUpItemId = -1
    var mSignUpMethed: ((Int, RaceGamePlayInfo?) -> Unit)? = null
    private var mCardScaleHelper: CardScaleHelper? = null

    //在滑动到最后的时候自动加载更多
    var mLoadMore: Boolean = false
    var mOffset: Int = 0
    var mCnt: Int = 5
    var mHasMore = true
    var mCurrentPosition = 0

    val raceRoomServerApi = ApiManager.getInstance().createService(RaceRoomServerApi::class.java)

    internal var mUiHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what == HIDE_PANEL) {
                clearAnimation()
                visibility = View.GONE
            } else if (msg.what == PAGER_BUG) {
                fakeDrag()
            }
        }
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    fun fakeDrag() {
//        try {
//            mRecyclerView.beginFakeDrag()
//            mRecyclerView.fakeDragBy(1.0f)
//            mRecyclerView.endFakeDrag()
//        } catch (e: Exception) {
//            MyLog.w(TAG, e.toString())
//        }
    }

    init {
        View.inflate(context, com.module.playways.R.layout.race_pager_select_song_view_layout, this)

        mPagerRootView = rootView.findViewById(com.module.playways.R.id.pager_root_view)
        hideClickArea = rootView.findViewById(com.module.playways.R.id.hide_click_area)
        mRecyclerView = rootView.findViewById(com.module.playways.R.id.speed_recyclerView)
        val linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        mRecyclerView.layoutManager = linearLayoutManager

        mPagerAdapter = RaceSelectSongRecyclerAdapter()
        mRecyclerView.adapter = mPagerAdapter
        mPagerAdapter?.mIRaceSelectListener = object : RaceSelectSongRecyclerAdapter.IRaceSelectListener {
            override fun onCloseClick() {
                hideView()
            }

            override fun onSignUp(itemID: Int, model: RaceGamePlayInfo?) {
                mSignUpMethed?.invoke(itemID, model)
            }

            override fun getSignUpItemID(): Int {
                return mHasSignUpItemId
            }

            override fun getRecyclerViewPosition(): Int {
                return mCurrentPosition
            }
        }

        mRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    mCurrentPosition = mCardScaleHelper?.getCurrentItemPos() ?: 0
                    if (!mLoadMore && mCurrentPosition > (mPagerAdapter?.mRaceGamePlayInfoList?.size
                                    ?: 0) - 3) {
                        mLoadMore = true
                        getPlaybookItemList()
                    }
                }
            }
        })

        mCardScaleHelper = CardScaleHelper()
        mCardScaleHelper?.attachToRecyclerView(mRecyclerView)

        mPagerRootView.setDebounceViewClickListener {
            //拦截
        }

        hideClickArea.setDebounceViewClickListener {
            hideView()
        }

        getPlaybookItemList()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        EventBus.getDefault().register(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: RaceWantSingChanceEvent) {
        mHasSignUpItemId = event.itemID
        if (mPagerAdapter?.mRaceGamePlayInfoList?.get(mCurrentPosition)?.commonMusic?.itemID == mHasSignUpItemId) {
            mPagerAdapter?.mRaceGamePlayInfoList?.get(mCurrentPosition)?.let {
                    addSelectedSong(it)
                }
            } else {
            mPagerAdapter?.mRaceGamePlayInfoList?.forEach {
                    if (it.commonMusic?.itemID == mHasSignUpItemId) {
                        addSelectedSong(it)
                        return@forEach
                    }
                }
            }
    }

    private fun addSelectedSong(info: RaceGamePlayInfo) {
        mPagerAdapter?.mRaceGamePlayInfoList?.clear()
        mPagerAdapter?.mRaceGamePlayInfoList?.add(info)
        mPagerAdapter?.notifyDataSetChanged()
    }

    fun showView() {
        if (visibility == View.VISIBLE) {
            return
        }

        clearAnimation()
        val animation = TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0f)
        animation.duration = ANIMATION_DURATION.toLong()
        animation.repeatMode = Animation.REVERSE
        animation.fillAfter = true
        startAnimation(animation)
        visibility = View.VISIBLE
        mUiHandler.sendEmptyMessage(PAGER_BUG)

        if (mPagerAdapter?.mRaceGamePlayInfoList?.size == 0) {
            getPlaybookItemList()
        }
    }

    private fun getPlaybookItemList() {
        if (mHasSignUpItemId > 0) {
            MyLog.d(TAG, "getPlaybookItemList已经选择过了，不需要拉更多了")
            return
        }

        if (!mHasMore) {
            MyLog.w(TAG, "getPlaybookItemList no more data")
            return
        }

        launch {
            val result = subscribe(RequestControl(TAG + "getPlaybookItemList", ControlType.CancelThis)) {
                raceRoomServerApi.getPlaybookItemList(mOffset, mCnt, MyUserInfoManager.getInstance().uid.toInt())
            }

            mLoadMore = false

            if (result.errno == 0) {
                val list = JSON.parseArray(result.data.getString("games"), RaceGamePlayInfo::class.java)
                if (list != null) {
                    mPagerAdapter?.addData(list)
                    mUiHandler.post {
                        mRecyclerView.smoothScrollBy(1, 0)
                    }
                }

                mOffset = result.data.getIntValue("offset")
                mHasMore = result.data.getBoolean("hasMore")
            } else {
                MyLog.w(TAG, "getPlaybookItemList errer is " + result.errno)
            }
        }
    }

    fun hideView() {
        if (mUiHandler.hasMessages(HIDE_PANEL) || View.GONE == visibility) {
            return
        }

        mUiHandler.removeMessages(HIDE_PANEL)
        clearAnimation()
        val animation = TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 1.0f)
        animation.duration = ANIMATION_DURATION.toLong()
        animation.repeatMode = Animation.REVERSE
        animation.fillAfter = true
        startAnimation(animation)

        mUiHandler.sendMessageDelayed(mUiHandler.obtainMessage(HIDE_PANEL), ANIMATION_DURATION.toLong())
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        EventBus.getDefault().unregister(this)
    }

    companion object {
        val HIDE_PANEL = 1
        val PAGER_BUG = 2
        val ANIMATION_DURATION = 300
    }
}