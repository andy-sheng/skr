package com.module.playways.race.room.view.topContent

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.common.core.myinfo.MyUserInfo
import com.common.core.myinfo.MyUserInfoManager
import com.common.log.MyLog

import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.engine.EngineEvent
import com.module.playways.R
import com.module.playways.grab.room.top.GrabTopItemView
import com.module.playways.race.room.RaceRoomData
import com.module.playways.race.room.model.RacePlayerInfoModel
import kotlinx.android.synthetic.main.race_top_content_view_layout.view.*

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class RaceTopContentView : ConstraintLayout {
    val TAG = "RaceTopContentView"

    val arrowIv: ImageView
    val backgroundIv: ExImageView
    val recyclerView: RecyclerView
    val maskIv: ExImageView
    val moreTv: TextView

    val adapter: RaceTopContentAdapter = RaceTopContentAdapter()

    internal var mIsOpen = true
    private var mRoomData: RaceRoomData? = null

    internal var mCurSeq = -2
    @Volatile
    internal var mHasBurst = false

    internal var mListener: Listener? = null

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    init {
        View.inflate(context, R.layout.race_top_content_view_layout, this)

        arrowIv = rootView.findViewById(R.id.arrow_iv)
        backgroundIv = rootView.findViewById(R.id.background_iv)
        recyclerView = rootView.findViewById(R.id.recycler_view)
        maskIv = rootView.findViewById(R.id.mask_iv)
        moreTv = rootView.findViewById(R.id.more_tv)

        addChildView()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = adapter

        arrowIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                mListener?.clickArrow(!mIsOpen)
            }
        })

        moreTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                mListener?.clickMore()
            }
        })
    }

    fun setArrowIcon(open: Boolean) {
        if (open) {
            // 展开状态
            mIsOpen = true
            arrowIv.setImageResource(R.drawable.yichangdaodi_dingbuzhankai)
        } else {
            // 折叠状态
            mIsOpen = false
            arrowIv.setImageResource(R.drawable.yichangdaodi_dingbushouqi)
        }
    }

    private fun addChildView() {
//        for (i in 0 until PLAYER_COUNT) {
//            val vp = VP()
//            vp.grabTopItemView = GrabTopItemView(context)
//            if (i == PLAYER_COUNT - 1) {
//                vp.grabTopItemView!!.setCanShowInviteWhenEmpty(true)
//            } else {
//                vp.grabTopItemView!!.setCanShowInviteWhenEmpty(false)
//            }
//            vp.grabTopItemView!!.hideGrabIcon()
//            vp.grabTopItemView!!.tryAddParent(mContentLl)
//            vp.grabTopItemView!!.setToPlaceHolder()
//            mGrabTopItemViewArrayList.add(vp)
//        }
//        resetAllGrabTopItemView()
    }

    private fun resetAllGrabTopItemView() {
//        for (vp in mGrabTopItemViewArrayList) {
//            vp.grabTopItemView!!.reset()
//            vp.grabTopItemView!!.setToPlaceHolder()
//        }
    }

    //只有轮次切换的时候调用
    private fun initData() {
        val list = mRoomData?.getPlayerInfoList<RacePlayerInfoModel>()
        if (!list.isNullOrEmpty()) {
            adapter.mDataList.clear()
            adapter.mDataList.addAll(list)
            adapter.notifyDataSetChanged()

            if (adapter.mDataList.size >= 7) {
                moreTv.text = "${adapter.mDataList.size}人"
                moreTv.visibility = View.VISIBLE
                maskIv.visibility = View.VISIBLE
            } else {
                moreTv.visibility = View.GONE
                maskIv.visibility = View.GONE
            }
        } else {
            MyLog.e(TAG, "initData 没人？？？？")

            // todo 加点测试数据
            adapter.mDataList.clear()
            for (i in 0..10) {
                val race = RacePlayerInfoModel()
                race.userID = MyUserInfoManager.getInstance().uid.toInt()
                race.userInfo = MyUserInfo.toUserInfoModel(MyUserInfoManager.getInstance().myUserInfo)
                adapter.mDataList.add(race)
            }
            adapter.notifyDataSetChanged()
            if (adapter.mDataList.size >= 7) {
                moreTv.text = "${adapter.mDataList.size}人"
                moreTv.visibility = View.VISIBLE
                maskIv.visibility = View.VISIBLE
            } else {
                moreTv.visibility = View.GONE
                maskIv.visibility = View.GONE
            }
        }
    }

    //刚进来的时候初始化灯
    private fun initLight() {
//        val iterator = mInfoMap.entries.iterator()
//        while (iterator.hasNext()) {
//            iterator.next().value.grabTopItemView!!.setLight(true)
//        }
    }

    //这里可能人员有变动
//    fun setModeGrab() {
//        // 切换到抢唱模式
//        if (mAnimatorAllSet != null) {
//            mAnimatorAllSet!!.cancel()
//        }
//        initData()
//        for (uId in mInfoMap.keys) {
//            val vp = mInfoMap[uId]
//            if (vp != null && vp.grabTopItemView != null) {
//                vp.grabTopItemView!!.visibility = View.VISIBLE
//                vp.grabTopItemView!!.reset()
//                val wantSingerInfo = WantSingerInfo()
//                wantSingerInfo.userID = uId
//                val grabRoundInfoModel = mRoomData!!.getRealRoundInfo<GrabRoundInfoModel>()
//                // TODO: 2019/2/26 判空
//                if (grabRoundInfoModel != null && grabRoundInfoModel.wantSingInfos.contains(wantSingerInfo)) {
//                    vp.grabTopItemView!!.setGrap(wantSingerInfo.wantSingType)
//                } else {
//                    vp.grabTopItemView!!.hideGrabIcon()
//                }
//            }
//        }
//
//        val lp = mContentLl.layoutParams as ConstraintLayout.LayoutParams
//        lp.leftMargin = U.getDisplayUtils().dip2px(7f)
//        lp.rightMargin = U.getDisplayUtils().dip2px(48f)
//        mContentLl.layoutParams = lp
//    }

//    fun setModeSing() {
//        MyLog.d(TAG, "setModeSing")
//        val now = mRoomData!!.getRealRoundInfo<GrabRoundInfoModel>() ?: return
//        mHasBurst = false
//        val singerUserIds = now.singUserIds
//        if (!now.isParticipant && now.enterStatus == now.status) {
//            // 如果是演唱阶段进来的参与者
//            syncLight()
//            for (userId in singerUserIds) {
//                val vp = mInfoMap[userId]
//                if (vp == null) {
//                    MyLog.d(TAG, "没有在选手席位找到 id=$userId 相应ui，return")
//                    continue
//                }
//                if (vp.grabTopItemView != null) {
//                    vp.grabTopItemView!!.setGetSingChance()
//                }
//                val finalGrabTopItemView = vp.grabTopItemView
//                finalGrabTopItemView!!.mCircleAnimationView.visibility = View.VISIBLE
//                finalGrabTopItemView.mCircleAnimationView.setProgress(100)
//                vp.grabTopItemView!!.mAvatarIv.scaleX = 1.08f
//                vp.grabTopItemView!!.mAvatarIv.scaleY = 1.08f
//                vp.grabTopItemView!!.setGrap(now.wantSingType)
//            }
//            EventBus.getDefault().post(LightOffAnimationOverEvent())
//            return
//        }

//        val allAnimator = ArrayList<Animator>()

//        if (now.status != EQRoundStatus.QRS_SPK_SECOND_PEER_SING.value) {
//            val animatorSet123s = ArrayList<Animator>()
//            // 第二轮不需要重复播这个动画
//            for (userId in singerUserIds) {
//                val vp = mInfoMap[userId]
//                if (vp == null) {
//                    MyLog.d(TAG, "没有在选手席位找到 id=$userId 相应ui，return")
//                    continue
//                }
//                if (vp.grabTopItemView != null) {
//                    vp.grabTopItemView!!.setGetSingChance()
//                }
//                val finalGrabTopItemView = vp.grabTopItemView
//                run {
//                    // 这是圈圈动画
//                    val objectAnimator1 = ValueAnimator()
//                    objectAnimator1.setIntValues(0, 100)
//                    objectAnimator1.duration = 495
//                    objectAnimator1.addUpdateListener { animation ->
//                        val p = animation.animatedValue as Int
//                        finalGrabTopItemView!!.mCircleAnimationView.setProgress(p)
//                    }
//                    objectAnimator1.addListener(object : AnimatorListenerAdapter() {
//                        override fun onAnimationStart(animation: Animator) {
//                            super.onAnimationStart(animation)
//                            finalGrabTopItemView!!.mCircleAnimationView.visibility = View.VISIBLE
//                        }
//
//                        override fun onAnimationCancel(animation: Animator) {
//                            super.onAnimationCancel(animation)
//                            finalGrabTopItemView!!.mCircleAnimationView.visibility = View.GONE
//                        }
//                    })
//
//                    // 接下来是头像放大一点的动画
//                    val objectAnimator2 = ObjectAnimator.ofFloat<View>(vp.grabTopItemView!!.mAvatarIv, View.SCALE_X, 1, 1.08f)
//                    val objectAnimator3 = ObjectAnimator.ofFloat<View>(vp.grabTopItemView!!.mAvatarIv, View.SCALE_Y, 1, 1.08f)
//                    val animatorSet23 = AnimatorSet()
//                    animatorSet23.playTogether(objectAnimator2, objectAnimator3)
//                    animatorSet23.duration = (4 * 33).toLong()
//
//                    val animatorSet123 = AnimatorSet()
//                    animatorSet123.playTogether(objectAnimator1, animatorSet23)
//                    animatorSet123s.add(animatorSet123)
//                }
//            }
//            val animatorSet123ss = AnimatorSet()
//            animatorSet123ss.playTogether(animatorSet123s)
//            allAnimator.add(animatorSet123ss)
//        }

    // 等待47个节拍
    //            {
    //                // 放大透明度消失
    //                ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(vp.grabTopItemView, View.ALPHA, 1, 0);
    //                ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(vp.grabTopItemView.mAvatarIv, View.SCALE_X, 1.0f, 1.08f);
    //                ObjectAnimator objectAnimator3 = ObjectAnimator.ofFloat(vp.grabTopItemView.mAvatarIv, View.SCALE_Y, 1.0f, 1.08f);
    //
    //                ValueAnimator objectAnimator4 = new ValueAnimator();
    //                objectAnimator4.setFloatValues(1, 0);
    //                objectAnimator4.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
    //                    @Override
    //                    public void onAnimationUpdate(ValueAnimator animation) {
    //                        float weight = (float) animation.getAnimatedValue();
    //                        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) finalGrabTopItemView.getLayoutParams();
    //                        lp.weight = weight;
    //                        finalGrabTopItemView.setLayoutParams(lp);
    //
    //                        RelativeLayout.LayoutParams lp2 = (LayoutParams) mContentLl.getLayoutParams();
    //                        int t = (int) (U.getDisplayUtils().dip2px(15) * weight);
    //                        lp2.leftMargin = U.getDisplayUtils().dip2px(30) - t;
    //                        lp2.rightMargin = U.getDisplayUtils().dip2px(30) - t;
    //                        mContentLl.setLayoutParams(lp2);
    //                    }
    //                });
    //                AnimatorSet animatorSet1234 = new AnimatorSet();
    //                animatorSet1234.playTogether(objectAnimator1, objectAnimator2, objectAnimator3, objectAnimator4);
    //                animatorSet1234.setDuration(9 * 33);
    //                animatorSet1234.setStartDelay(47 * 33);
    //                allAnimator.add(animatorSet1234);
    //            }

    // 等 125 个节拍 把灯变亮
//        run {
//            val liangdengList = ArrayList<Animator>()
//            var i = 0
//            for (uId in mInfoMap.keys) {
//                val vp1 = mInfoMap[uId]
//                val itemView = vp1.grabTopItemView
//                var filter = false
//                for (uid2 in singerUserIds) {
//                    if (uId == uid2) {
//                        filter = true
//                        continue
//                    }
//                }
//                if (filter) {
//                    continue
//                }
//
//                //                if (!itemView.getPlayerInfoModel().isOnline()) {
//                //                    continue;
//                //                }
//                val objectAnimator1 = ValueAnimator()
//                objectAnimator1.setIntValues(0, 0)
//                objectAnimator1.duration = 1
//                val tti = i
//                objectAnimator1.addListener(object : AnimatorListenerAdapter() {
//                    override fun onAnimationStart(animation: Animator) {
//                        super.onAnimationStart(animation)
//                        //                        if (tti == 0) {
//                        //                            U.getSoundUtils().play(GrabRoomFragment.TAG, R.raw.grab_lightup);
//                        //                        }
//                    }
//
//                    override fun onAnimationCancel(animation: Animator) {
//                        super.onAnimationCancel(animation)
//                        onAnimationEnd(animation)
//                    }
//
//                    override fun onAnimationEnd(animation: Animator) {
//                        super.onAnimationEnd(animation)
//                        itemView!!.setLight(true)
//                    }
//                })
//                objectAnimator1.startDelay = (i * 4 * 33).toLong()
//                i++
//                liangdengList.add(objectAnimator1)
//            }
//            val animatorSet1s = AnimatorSet()
//            animatorSet1s.playTogether(liangdengList)
//            animatorSet1s.addListener(object : AnimatorListenerAdapter() {
//                override fun onAnimationStart(animation: Animator) {
//                    super.onAnimationStart(animation)
//                    for (uid in singerUserIds) {
//                        val vp1 = mInfoMap[uid]
//                        if (vp1 != null) {
//                            // 出灯前 圈圈消失
//                            val itemView = vp1.grabTopItemView
//                            itemView!!.mCircleAnimationView.visibility = View.GONE
//                            itemView.hideGrabIcon()
//                        }
//                    }
//
//                }
//            })
//            animatorSet1s.startDelay = (20 * 33).toLong()
//            allAnimator.add(animatorSet1s)
//        }
//
//
//        if (mAnimatorAllSet != null) {
//            mAnimatorAllSet!!.cancel()
//        }
//
//        mAnimatorAllSet = AnimatorSet()
//        mAnimatorAllSet!!.playSequentially(allAnimator)
//        mAnimatorAllSet!!.addListener(object : AnimatorListenerAdapter() {
//            override fun onAnimationCancel(animation: Animator) {
//                super.onAnimationCancel(animation)
//                //setModeGrab();
//            }
//
//            override fun onAnimationEnd(animation: Animator) {
//                super.onAnimationEnd(animation)
//                syncLight()
//                EventBus.getDefault().post(LightOffAnimationOverEvent())
//            }
//        })
//        mAnimatorAllSet!!.start()
//    }

    //有人爆灯了，这个时候所有的灯都闪烁
//    fun toBurstState() {
//        mHasBurst = true
//        for (uId in mInfoMap.keys) {
//            val vp = mInfoMap[uId]
//            if (vp != null && vp.grabTopItemView != null && !RoomDataUtils.isRoundSinger(mRoomData!!.getRealRoundInfo(), uId.toLong())) {
//                vp.grabTopItemView!!.startEvasive()
//            }
//        }
//    }
//
//    private fun syncLight() {
//        val now = mRoomData!!.getRealRoundInfo<GrabRoundInfoModel>()
//        if (now != null) {
//            for (noPassingInfo in now.mLightInfos) {
//                val vp = mInfoMap[noPassingInfo.userID]
//                if (vp != null && vp.grabTopItemView != null) {
//                    vp.grabTopItemView!!.setLight(false)
//                }
//            }
//        }
//    }
//
//    fun grap(wantSingerInfo: WantSingerInfo) {
//        val vp = mInfoMap[wantSingerInfo.userID]
//        if (vp != null && vp.grabTopItemView != null) {
//            vp.grabTopItemView!!.setGrap(wantSingerInfo.wantSingType)
//        }
//    }
//
//    fun lightOff(uid: Int) {
//        if (mHasBurst) {
//            MyLog.w(TAG, "已经爆灯了，所以灭灯也忽略 uid 是：$uid")
//            return
//        }
//        val vp = mInfoMap[uid]
//        if (vp != null && vp.grabTopItemView != null) {
//            //            setLightOffAnimation(vp);
//            setLightOff(vp)
//        }
//    }
//
//    fun onlineChange(playerInfoModel: PlayerInfoModel?) {
//        if (playerInfoModel != null && playerInfoModel.userInfo != null) {
//            val vp = mInfoMap[playerInfoModel.userInfo.userId]
//            if (vp != null) {
//                vp.grabTopItemView!!.updateOnLineState(playerInfoModel)
//            }
//        } else {
//            MyLog.w(TAG, "onlineChange playerInfoModel error")
//        }
//    }
//
//    private fun setLightOff(vp: VP) {
//        val grabTopItemView = vp.grabTopItemView
//        grabTopItemView!!.setLight(false)
//    }

    //    /**
    //     * 执行灭灯动画
    //     *
    //     * @param vp
    //     */
    //    private void setLightOffAnimation(VP vp) {
    //        GrabTopItemView grabTopItemView = vp.grabTopItemView;
    //        grabTopItemView.mFlagIv.setVisibility(GONE);
    //
    //        int[] position1 = new int[2];
    //        grabTopItemView.mFlagIv.getLocationInWindow(position1);
    //
    //        int[] position2 = new int[2];
    //        SVGAImageView mMieDengIv = vp.SVGAImageView;
    //        mMieDengIv.getLocationInWindow(position2);
    //
    //        mMieDengIv.setTranslationX(position1[0] - U.getDisplayUtils().dip2px(32));
    //        mMieDengIv.setTranslationY(U.getDisplayUtils().dip2px(12f));
    //
    //        SvgaParserAdapter.parse("grab_miedeng.svga", new SVGAParser.ParseCompletion() {
    //            @Override
    //            public void onComplete(SVGAVideoEntity svgaVideoEntity) {
    //                SVGADrawable drawable = new SVGADrawable(svgaVideoEntity);
    //                mMieDengIv.setVisibility(VISIBLE);
    //                mMieDengIv.stopAnimation(true);
    //                mMieDengIv.setImageDrawable(drawable);
    //                mMieDengIv.startAnimation();
    //            }
    //
    //            @Override
    //            public void onError() {
    //
    //            }
    //        });
    //
    //        mMieDengIv.setCallback(new SVGACallback() {
    //            @Override
    //            public void onPause() {
    //
    //            }
    //
    //            @Override
    //            public void onFinished() {
    //                mMieDengIv.stopAnimation(true);
    //                mMieDengIv.setVisibility(GONE);
    //                grabTopItemView.setLight(false);
    //            }
    //
    //            @Override
    //            public void onRepeat() {
    //                onFinished();
    //            }
    //
    //            @Override
    //            public void onStep(int i, double v) {
    //
    //            }
    //        });
    //    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun onEvent(event: GrabPlaySeatUpdateEvent) {
//        MyLog.d(TAG, "onEvent event=$event")
//        initData()
//    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: EngineEvent) {
//        /**
//         * 提示说话声纹
//         */
//        if (event.getType() == EngineEvent.TYPE_USER_AUDIO_VOLUME_INDICATION) {
//            val list = event.getObj<List<EngineEvent.UserVolumeInfo>>()
//            for (uv in list) {
//                //MyLog.d(TAG,"UserVolumeInfo uv=" + uv);
//                var uid = uv.uid
//                if (uid == 0) {
//                    uid = MyUserInfoManager.getInstance().uid.toInt()
//                }
//                val vp = mInfoMap[uid]
//                if (vp != null && vp.grabTopItemView != null && vp.grabTopItemView!!.isShown) {
//                    vp.grabTopItemView!!.showSpeakingAnimation()
//                }
//            }
//        } else if (event.getType() == EngineEvent.TYPE_USER_MUTE_AUDIO) {
//            //用户闭麦，开麦
//            val userStatus = event.getUserStatus()
//            if (userStatus != null) {
//                val userId = userStatus.userId
//                val vp = mInfoMap[userId]
//                if (vp != null && vp.grabTopItemView != null && vp.grabTopItemView!!.isShown && userStatus.isAudioMute) {
//                    vp.grabTopItemView!!.hideSpeakingAnimation()
//                }
//            }
//        }
    }

    fun setRoomData(roomData: RaceRoomData) {
        mRoomData = roomData
        initData()
        //        mGrabAudienceView.setRoomData(mRoomData);
        //        if (mGrabTopItemViewArrayList.size() != 0) {
        //            VP vp = mGrabTopItemViewArrayList.get(mGrabTopItemViewArrayList.size() - 1);
        //            if (mRoomData.getRoomType() == GrabRoomType.ROOM_TYPE_GUIDE) {
        //                // 新手房
        //                vp.grabTopItemView.setCanShowInviteWhenEmpty(false);
        //            } else {
        //                if (mRoomData.getOwnerId() != 0) {
        //                    // 房主房
        //                    if (mRoomData.isOwner()) {
        //                        vp.grabTopItemView.setCanShowInviteWhenEmpty(true);
        //                    } else {
        //                        vp.grabTopItemView.setCanShowInviteWhenEmpty(false);
        //                    }
        //                } else {
        //                    // 普通房
        //                    vp.grabTopItemView.setCanShowInviteWhenEmpty(true);
        //                }
        //            }
        //        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
//        if (mAnimatorAllSet != null) {
//            mAnimatorAllSet!!.cancel()
//        }
        //        for (int i = 0; i < mGrabTopItemViewArrayList.size(); i++) {
        //            VP vp = mGrabTopItemViewArrayList.get(i);
        //            if (vp != null) {
        //                if (vp.SVGAImageView != null) {
        //                    vp.SVGAImageView.setCallback(null);
        //                    vp.SVGAImageView.stopAnimation(true);
        //                }
        //            }
        //        }
        EventBus.getDefault().unregister(this)
    }


//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun onEvent(event: SomeOneGrabEvent) {
//        grap(event.mWantSingerInfo)
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun onEvent(event: GrabSomeOneLightOffEvent) {
//        lightOff(event.uid)
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun onEvent(event: GrabSomeOneLightBurstEvent) {
//        toBurstState()
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun onEvent(event: SomeOneOnlineChangeEvent) {
//        onlineChange(event.playerInfoModel)
//    }

    internal class VP {
        var grabTopItemView: GrabTopItemView? = null
        //SVGAImageView SVGAImageView;
    }

    fun setListener(listener: Listener) {
        mListener = listener
    }

    interface Listener {
        fun clickArrow(open: Boolean)
        fun clickMore()
    }

    companion object {
        val PLAYER_COUNT = 7
    }
}