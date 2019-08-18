package com.module.playways.room.gift.view

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Message
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.TranslateAnimation
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout

import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.UserInfoManager
import com.common.core.userinfo.UserInfoServerApi
import com.common.core.userinfo.event.RelationChangeEvent
import com.common.image.fresco.BaseImageView
import com.common.log.MyLog
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExRelativeLayout
import com.common.view.ex.ExTextView
import com.common.view.ex.drawable.DrawableCreator
import com.component.busilib.view.BitmapTextView
import com.module.playways.BaseRoomData
import com.module.playways.R
import com.module.playways.grab.room.GrabRoomData
import com.module.playways.grab.room.event.GrabMyCoinChangeEvent
import com.module.playways.grab.room.event.GrabPlaySeatUpdateEvent
import com.module.playways.grab.room.model.GrabPlayerInfoModel
import com.module.playways.room.gift.GiftServerApi
import com.module.playways.room.gift.adapter.GiftAllPlayersAdapter
import com.module.playways.room.gift.event.BuyGiftEvent
import com.module.playways.room.gift.event.CancelGiftCountDownEvent
import com.module.playways.room.gift.event.ShowHalfRechargeFragmentEvent
import com.module.playways.room.gift.event.StartGiftCountDownEvent
import com.module.playways.room.gift.event.UpdateCoinEvent
import com.module.playways.room.gift.event.UpdateDiamondEvent
import com.module.playways.room.gift.event.UpdateHZEvent
import com.module.playways.room.prepare.model.PlayerInfoModel

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

import java.util.ArrayList

import io.reactivex.disposables.Disposable

class GiftPanelView : FrameLayout {
    val TAG = "GiftPanelView"
    internal lateinit var mIvSelectedIcon: BaseImageView
    internal lateinit  var mTvSelectedName: ExTextView
    internal lateinit  var mAllPlayersTv: ExTextView
    internal lateinit  var mIvRecharge: ExTextView
    internal lateinit  var mIvDiamondIcon: ImageView
    internal lateinit  var mIvSend: ExTextView
    internal lateinit  var mGiftPanelArea: ExRelativeLayout
    internal lateinit  var mAllPlayersRV: RecyclerView
    internal lateinit  var mLlSelectedMan: RelativeLayout
    internal lateinit  var mTvDiamond: BitmapTextView
    internal lateinit  var mRlPlayerSelectArea: RelativeLayout
    internal lateinit  var mFollowTv: ExTextView
    internal lateinit  var mTvCoin: BitmapTextView
    internal lateinit  var mTvCoinChange: ExTextView
    internal var mCoin = 0
    internal var mHz = 0f

    internal var mGiftDisplayView: GiftDisplayView? = null
    internal var mRelationTask: Disposable? = null

    internal lateinit  var mGiftAllPlayersAdapter: GiftAllPlayersAdapter
    internal lateinit  var mUserInfoServerApi: UserInfoServerApi
    internal lateinit  var mIGetGiftCountDownListener: GiftDisplayView.IGetGiftCountDownListener

    //当前迈上的人
    internal var mCurMicroMan: PlayerInfoModel? = null

    internal var mRoomData: BaseRoomData<*>?=null

    private var mHasInit = false

    internal var mGiftServerApi: GiftServerApi?=null

    internal var mNeedFollowDrawable = DrawableCreator.Builder().setCornersRadius(U.getDisplayUtils().dip2px(20f).toFloat())
            .setStrokeWidth(U.getDisplayUtils().dip2px(2f).toFloat())
            .setStrokeColor(Color.parseColor("#3B4E79"))
            .setCornersRadius(U.getDisplayUtils().dip2px(16f).toFloat())
            .setSolidColor(Color.parseColor("#FFC15B"))
            .build()

    internal var mHasFollowDrawable = DrawableCreator.Builder().setCornersRadius(U.getDisplayUtils().dip2px(20f).toFloat())
            .setStrokeWidth(U.getDisplayUtils().dip2px(2f).toFloat())
            .setStrokeColor(U.getColor(R.color.white_trans_50))
            .setCornersRadius(U.getDisplayUtils().dip2px(16f).toFloat())
            .setSolidColor(U.getColor(R.color.transparent))
            .build()

    internal var mUiHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what == HIDE_PANEL) {
                clearAnimation()
                visibility = View.GONE
            }
        }
    }

    private val firstPlayerInfo: GrabPlayerInfoModel?
        get() {
            val grabPlayerInfoModelList = getGrabRoomData()?.getInSeatPlayerInfoList()?:ArrayList()

            for (grabPlayerInfoModel in grabPlayerInfoModelList) {
                if (grabPlayerInfoModel.getUserID().toLong() != MyUserInfoManager.getInstance().uid) {
                    return grabPlayerInfoModel
                }
            }

            return null
        }

    private val playerInfoListExpectSelf: List<GrabPlayerInfoModel>
        get() {
            val grabPlayerInfoModelList = ArrayList(getGrabRoomData()?.getInSeatPlayerInfoList()?:ArrayList())

            val it = grabPlayerInfoModelList.iterator()
            while (it.hasNext()) {
                val grabPlayerInfoModel = it.next()
                if (grabPlayerInfoModel.getUserID().toLong() == MyUserInfoManager.getInstance().uid) {
                    it.remove()
                }
            }

            return grabPlayerInfoModelList
        }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        mGiftServerApi = ApiManager.getInstance().createService(GiftServerApi::class.java)
        mUserInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi::class.java)
    }

    private fun inflate() {
        View.inflate(context, R.layout.gift_panel_view_layout, this)
        mHasInit = true
        mRlPlayerSelectArea = findViewById<View>(R.id.rl_player_select_area) as RelativeLayout
        mGiftPanelArea = findViewById<View>(R.id.gift_panel_area) as ExRelativeLayout
        mIvSelectedIcon = findViewById<View>(R.id.iv_selected_icon) as BaseImageView
        mTvSelectedName = findViewById<View>(R.id.tv_selected_name) as ExTextView
        mAllPlayersTv = findViewById<View>(R.id.all_players_tv) as ExTextView
        mIvRecharge = findViewById(R.id.iv_recharge)
        mIvDiamondIcon = findViewById<View>(R.id.iv_diamond_icon) as ImageView
        mIvSend = findViewById(R.id.iv_send)
        mGiftDisplayView = findViewById<View>(R.id.gift_view) as GiftDisplayView
        mAllPlayersRV = findViewById<View>(R.id.all_players_rv) as RecyclerView
        mLlSelectedMan = findViewById<View>(R.id.ll_selected_man) as RelativeLayout
        mTvDiamond = findViewById<View>(R.id.tv_diamond) as BitmapTextView
        mFollowTv = findViewById<View>(R.id.follow_tv) as ExTextView
        mTvCoin = findViewById<View>(R.id.tv_coin) as BitmapTextView
        mTvCoinChange = findViewById<View>(R.id.tv_coin_change) as ExTextView
        mGiftDisplayView!!.setIGetGiftCountDownListener(mIGetGiftCountDownListener)

        mGiftAllPlayersAdapter = GiftAllPlayersAdapter()
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        mAllPlayersRV.layoutManager = linearLayoutManager
        mAllPlayersRV.adapter = mGiftAllPlayersAdapter
        EventBus.getDefault().register(this)

        mGiftPanelArea.setOnClickListener { v -> }

        mGiftAllPlayersAdapter.setOnClickPlayerListener(object:GiftAllPlayersAdapter.OnClickPlayerListener {
            override fun onClick(playerInfoModel: PlayerInfoModel?) {
                if (mAllPlayersRV.visibility != View.VISIBLE) {
                    return
                }

                mGiftAllPlayersAdapter.setSelectedGrabPlayerInfoModel(playerInfoModel)
                mGiftAllPlayersAdapter.update(playerInfoModel)
                mCurMicroMan = playerInfoModel

                bindSelectedPlayerData()

                mUiHandler.postDelayed({ collapsePlayerList() }, 100)
            }
        })

        setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                if (visibility == View.VISIBLE) {
                    hide()
                }
            }
        })

        mFollowTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                UserInfoManager.getInstance().mateRelation(mCurMicroMan!!.userID, UserInfoManager.RA_BUILD, mCurMicroMan!!.userInfo.isFriend)
            }
        })

        mIvSend.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                if (mGiftDisplayView!!.selectedGift == null) {
                    U.getToastUtil().showShort("请选择礼物")
                    return
                }

                if (mCurMicroMan == null) {
                    U.getToastUtil().showShort("请选择送礼对象")
                    return
                }

                hide()
                EventBus.getDefault().post(BuyGiftEvent(mGiftDisplayView!!.selectedGift, mCurMicroMan!!.userInfo))
            }
        })

        mAllPlayersTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                if (mCurMicroMan == null) {
                    U.getToastUtil().showShort("请选择送礼用户")
                    return
                }


                val visibleState = mAllPlayersRV.visibility

                if (visibleState == View.VISIBLE) {
                    collapsePlayerList()
                } else {
                    val layoutManager = mAllPlayersRV.layoutManager
                    val linearManager = layoutManager as LinearLayoutManager
                    val lastItemPosition = linearManager.findLastVisibleItemPosition()
                    val firstItemPosition = linearManager.findFirstVisibleItemPosition()

                    mAllPlayersTv.isEnabled = false
                    mAllPlayersRV.visibility = View.VISIBLE
                    mLlSelectedMan.visibility = View.GONE
                    for (i in firstItemPosition..lastItemPosition) {
                        val view = linearLayoutManager.getChildAt(i)
                        val translateAnimation = TranslateAnimation((-i * U.getDisplayUtils().dip2px(46f)).toFloat(), 0f, 0f, 0f)
                        translateAnimation.duration = 300
                        translateAnimation.interpolator = DecelerateInterpolator()
                        if (view != null) {
                            view.animation = translateAnimation
                            view.startAnimation(translateAnimation)
                        }
                    }

                    val drawable = U.getDrawable(R.drawable.suoyouren_left)
                    drawable.bounds = Rect(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
                    mAllPlayersTv.setCompoundDrawables(drawable, null, null, null)
                    mAllPlayersTv.text = "收起"

                    mUiHandler.postDelayed({ mAllPlayersTv.isEnabled = true }, 300)
                }
            }
        })

        mIvRecharge.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                EventBus.getDefault().post(ShowHalfRechargeFragmentEvent())
            }
        })

        getZSBalance()

        mTvCoin.setText(getGrabRoomData()?.getCoin()?.toString())
        //        mTvHz.setText(String.format("%.1f", mGrabRoomData.getHzCount()));
        mCoin = getGrabRoomData()?.getCoin()?:0
        mHz = getGrabRoomData()?.getHzCount()?:0f
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: RelationChangeEvent) {
        for (grabPlayerInfoModel in playerInfoListExpectSelf) {
            if (grabPlayerInfoModel.userID == event.useId) {
                grabPlayerInfoModel.userInfo.isFollow = event.isFollow
                grabPlayerInfoModel.userInfo.isFriend = event.isFriend
            }
        }

        bindSelectedPlayerData()
    }

    fun setIGetGiftCountDownListener(IGetGiftCountDownListener: GiftDisplayView.IGetGiftCountDownListener) {
        mIGetGiftCountDownListener = IGetGiftCountDownListener
    }

    fun collapsePlayerList() {
        val layoutManager = mAllPlayersRV.layoutManager
        val linearManager = layoutManager as LinearLayoutManager
        val lastItemPosition = linearManager.findLastVisibleItemPosition()
        val firstItemPosition = linearManager.findFirstVisibleItemPosition()
        mAllPlayersTv.isEnabled = false
        for (i in firstItemPosition..lastItemPosition) {
            val view = linearManager.getChildAt(i)
            if (view != null) {
                val translateAnimation = TranslateAnimation(0f, (-i * U.getDisplayUtils().dip2px(46f)).toFloat(), 0f, 0f)
                translateAnimation.duration = 300
                translateAnimation.interpolator = DecelerateInterpolator()
                view.animation = translateAnimation
                view.startAnimation(translateAnimation)
            } else {
                MyLog.w(TAG, "collapsePlayerList view = null")
            }
        }

        val drawable = U.getDrawable(R.drawable.suoyouren_right)
        drawable.bounds = Rect(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        mAllPlayersTv.setCompoundDrawables(drawable, null, null, null)
        mAllPlayersTv.text = "重选"

        mUiHandler.postDelayed({
            mAllPlayersTv.isEnabled = true
            mAllPlayersRV.visibility = View.INVISIBLE
            mLlSelectedMan.visibility = View.VISIBLE
        }, 300)
    }

    fun updateZS() {
        MyLog.d(TAG, "updateZS")
        getZSBalance()
    }

    private fun getZSBalance() {
        ApiMethods.subscribe(mGiftServerApi?.zsBalance, object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult) {
                MyLog.w(TAG, "getZSBalance process obj=$obj")
                if (obj.errno == 0) {
                    val amount = obj.data!!.getString("totalAmountStr")
                    mTvDiamond.setText(amount)
                }
            }
        }, RequestControl("getZSBalance", ControlType.CancelThis))
    }

    fun setRoomData(grabRoomData: BaseRoomData<*>) {
        mRoomData = grabRoomData
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: GrabPlaySeatUpdateEvent) {
        MyLog.d(TAG, "onEvent event=$event")
        mGiftAllPlayersAdapter.dataList = playerInfoListExpectSelf
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: UpdateDiamondEvent) {
        MyLog.d(TAG, "onEvent event=$event")
        mTvDiamond.setText(String.format("%.1f", event.zuanBalance))
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: UpdateCoinEvent) {
        if (mCoin != event.coinBalance) {
            mCoin = event.coinBalance
            mTvCoin.setText(event.coinBalance.toString() + "")
        }

        getGrabRoomData()?.setCoinNoEvent(mCoin)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: UpdateHZEvent) {
        if (mHz != event.hz) {
            mHz = event.hz
            //            mTvHz.setText(String.format("%.1f", event.getHz()));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: GrabMyCoinChangeEvent) {
        mTvCoin.setText(event.coin.toString() + "")
        mCoin = event.coin
    }

    //外面不希望用这个函数
    @Deprecated("")
    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
    }

    fun hide() {
        mUiHandler.removeMessages(HIDE_PANEL)
        clearAnimation()
        val animation = TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 1.0f)
        animation.duration = ANIMATION_DURATION.toLong()
        animation.repeatMode = Animation.REVERSE
        animation.fillAfter = true
        startAnimation(animation)

        mUiHandler.sendMessageDelayed(mUiHandler.obtainMessage(HIDE_PANEL), ANIMATION_DURATION.toLong())
        EventBus.getDefault().post(CancelGiftCountDownEvent())
    }

    /**
     * @param grabPlayerInfoModel 麦上的人
     */
    fun show(playerInfoModel: PlayerInfoModel?) {
        MyLog.d(TAG, "show grabPlayerInfoModel=$playerInfoModel")
        if (!mHasInit) {
            inflate()
        }

        if (visibility == View.VISIBLE) {
            MyLog.d(TAG, "show" + " getVisibility() == VISIBLE")
            return
        }

        EventBus.getDefault().post(StartGiftCountDownEvent())

        setSelectArea(playerInfoModel)
        mUiHandler.removeMessages(HIDE_PANEL)
        clearAnimation()
        val animation = TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0f)
        animation.duration = ANIMATION_DURATION.toLong()
        animation.repeatMode = Animation.REVERSE
        animation.fillAfter = true
        startAnimation(animation)
        visibility = View.VISIBLE
    }

    private fun setSelectArea(playerInfoModel: PlayerInfoModel?) {
        if (getGrabRoomData()?.getInSeatPlayerInfoList()?.size === 0 || getGrabRoomData()?.getInSeatPlayerInfoList()?.size === 1 && getGrabRoomData()?.getInSeatPlayerInfoList()?.get(0)?.getUserID() === MyUserInfoManager.getInstance().uid.toInt()) {
            //只有自己
            mRlPlayerSelectArea.visibility = View.GONE
        } else {
            mRlPlayerSelectArea.visibility = View.VISIBLE

            if (playerInfoModel != null) {
                if (playerInfoModel.userID.toLong() == MyUserInfoManager.getInstance().uid) {
                    //自己在麦上
                    selectSendGiftPlayer(null)
                } else {
                    //别人在麦上
                    selectSendGiftPlayer(playerInfoModel)
                }
            } else {
                //没人在麦上
                selectSendGiftPlayer(null)
            }

            mGiftAllPlayersAdapter.dataList = playerInfoListExpectSelf
        }
    }

    /**
     * 选择送礼的人
     *
     * @param grabPlayerInfoModel
     */
    private fun selectSendGiftPlayer(playerInfoModel: PlayerInfoModel?) {
        var grabPlayerInfoModel = playerInfoModel
        //麦上没有人
        var isPlayerInMic = true
        if (grabPlayerInfoModel == null) {
            isPlayerInMic = false
            grabPlayerInfoModel = firstPlayerInfo
        }

        if (grabPlayerInfoModel != null) {
            mGiftAllPlayersAdapter.setSelectedGrabPlayerInfoModel(grabPlayerInfoModel)
            mGiftAllPlayersAdapter.update(grabPlayerInfoModel)
            mCurMicroMan = grabPlayerInfoModel

            bindSelectedPlayerData()
        } else {
            mGiftAllPlayersAdapter.setSelectedGrabPlayerInfoModel(null)
            mGiftAllPlayersAdapter.update(grabPlayerInfoModel)
        }

        if (isPlayerInMic) {
            mAllPlayersRV.visibility = View.INVISIBLE
            mLlSelectedMan.visibility = View.VISIBLE
        } else {
            mAllPlayersRV.visibility = View.VISIBLE
            mLlSelectedMan.visibility = View.GONE
        }

        if (mAllPlayersRV.visibility == View.VISIBLE) {
            val drawable = U.getDrawable(R.drawable.suoyouren_left)
            drawable.bounds = Rect(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
            mAllPlayersTv.setCompoundDrawables(drawable, null, null, null)
            mAllPlayersTv.text = "收起"
        } else {
            val drawable = U.getDrawable(R.drawable.suoyouren_right)
            drawable.bounds = Rect(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
            mAllPlayersTv.setCompoundDrawables(drawable, null, null, null)
            mAllPlayersTv.text = "重选"
        }
    }

    private fun bindSelectedPlayerData() {
        if (mCurMicroMan == null) {
            return
        }

        if (mRelationTask != null && !mRelationTask!!.isDisposed) {
            mRelationTask!!.dispose()
        }

        mFollowTv.visibility = View.GONE
        mFollowTv.isEnabled = false
        if (mCurMicroMan!!.userInfo.isFriend || mCurMicroMan!!.userInfo.isFollow) {
            mFollowTv.visibility = View.VISIBLE
            mFollowTv.background = mHasFollowDrawable
            mFollowTv.text = "已关注"
            mFollowTv.setTextColor(U.getColor(R.color.white_trans_50))
        } else {
            mRelationTask = ApiMethods.subscribe(mUserInfoServerApi.getRelation(mCurMicroMan!!.userID), object : ApiObserver<ApiResult>() {
                override fun process(obj: ApiResult) {
                    if (obj.errno == 0) {
                        mFollowTv.visibility = View.VISIBLE

                        val isFriend = obj.data!!.getBooleanValue("isFriend")
                        val isFollow = obj.data!!.getBooleanValue("isFollow")
                        mCurMicroMan!!.userInfo.isFriend = isFriend
                        mCurMicroMan!!.userInfo.isFollow = isFollow

                        if (!mCurMicroMan!!.userInfo.isFollow && !mCurMicroMan!!.userInfo.isFriend) {
                            mFollowTv.isEnabled = true
                            mFollowTv.background = mNeedFollowDrawable
                            mFollowTv.text = "+关注"
                            mFollowTv.setTextColor(Color.parseColor("#ff3b4e79"))
                        } else {
                            mFollowTv.background = mHasFollowDrawable
                            mFollowTv.text = "已关注"
                            mFollowTv.setTextColor(U.getColor(R.color.white_trans_50))
                        }
                    }
                }
            })
        }

        AvatarUtils.loadAvatarByUrl(mIvSelectedIcon,
                AvatarUtils.newParamsBuilder(mCurMicroMan!!.userInfo.avatar)
                        .setBorderColor(U.getColor(R.color.white))
                        .setBorderWidth(U.getDisplayUtils().dip2px(2f).toFloat())
                        .setCircle(true)
                        .build())

        mTvSelectedName.text = mCurMicroMan!!.userInfo.nicknameRemark
    }

    fun getGrabRoomData():GrabRoomData?{
        if(mRoomData is GrabRoomData?){
            return mRoomData as GrabRoomData?
        }
        return null
    }

    fun onBackPressed(): Boolean {
        if (visibility == View.VISIBLE) {
            hide()
            return true
        }

        return false
    }

    fun destroy() {
        if (mGiftDisplayView != null) {
            mGiftDisplayView!!.destroy()
        }
        EventBus.getDefault().unregister(this)
        if (mRelationTask != null && !mRelationTask!!.isDisposed) {
            mRelationTask!!.dispose()
        }
    }

    companion object {
        val SHOW_PANEL = 0
        val HIDE_PANEL = 1
        val ANIMATION_DURATION = 300
    }
}
