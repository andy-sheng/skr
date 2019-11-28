package com.module.playways.grab.room.top

import android.animation.*
import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import com.common.core.myinfo.MyUserInfoManager
import com.common.log.MyLog
import com.common.view.DebounceViewClickListener
import com.component.busilib.constans.GrabRoomType
import com.engine.EngineEvent
import com.module.playways.R
import com.module.playways.RoomDataUtils
import com.module.playways.grab.room.GrabRoomData
import com.module.playways.grab.room.event.*
import com.module.playways.grab.room.model.WantSingerInfo
import com.module.playways.room.prepare.model.PlayerInfoModel
import com.zq.live.proto.GrabRoom.EQRoundStatus
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class GrabTopContentView : ConstraintLayout {

    val TAG = "GrabTopContentView"
    private lateinit var mArrowIv: ImageView
    private lateinit var mInviteIv: ImageView
    private lateinit var mContentLl: LinearLayout

    private val mInfoMap = LinkedHashMap<Int, VP>()
    private val mGrabTopItemViewArrayList = ArrayList<VP>()
    private var mRoomData: GrabRoomData? = null
    internal var mAnimatorAllSet: AnimatorSet? = null
    //    GrabAudienceView mGrabAudienceView;
    internal var mIsOpen = true


    internal var mCurSeq = -2

    @Volatile
    internal var mHasBurst = false

    internal var mListener: Listener? = null

    lateinit var horizontalScrollView: HorizontalScrollView

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        View.inflate(context, R.layout.grab_top_content_view_layout, this)
        //        setClickable(true);
        horizontalScrollView = (this.findViewById<View>(R.id.scroll_view) as HorizontalScrollView)
        mContentLl = horizontalScrollView.getChildAt(0) as LinearLayout
        //        mGrabAudienceView = (GrabAudienceView) this.findViewById(R.id.grab_audience_view);
        mArrowIv = this.findViewById<View>(R.id.arrow_iv) as ImageView
        mInviteIv = this.findViewById(R.id.invite_iv)
//        addChildView()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

        mArrowIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                if (mListener != null) {
                    mListener!!.clickArrow(!mIsOpen)
                    // true 就要
                }
            }
        })

        mInviteIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                EventBus.getDefault().post(GrabWantInviteEvent())
            }
        })
    }

    fun setArrowIcon(open: Boolean) {
        if (open) {
            // 展开状态
            mIsOpen = true
            mArrowIv.setImageResource(R.drawable.race_expand_icon)
        } else {
            // 折叠状态
            mIsOpen = false
            mArrowIv.setImageResource(R.drawable.race_shrink_icon)
        }
    }

    private fun addChildView() {
        if (mGrabTopItemViewArrayList.size != getMaxCount()) {
            mContentLl.removeAllViews()
            for (i in 0 until getMaxCount()) {
                val vp = VP()
                vp.grabTopItemView = GrabTopItemView(context)
                //            if (i == PLAYER_COUNT - 1) {
                //                vp.grabTopItemView.setCanShowInviteWhenEmpty(true);
                //            } else {
                //                vp.grabTopItemView.setCanShowInviteWhenEmpty(false);
                //            }
                vp.grabTopItemView!!.hideGrabIcon()
                vp.grabTopItemView!!.tryAddParent(mContentLl)
                vp.grabTopItemView!!.setToPlaceHolder()
                //            vp.SVGAImageView = new SVGAImageView(getContext());
                //            LayoutParams lp = new LayoutParams(U.getDisplayUtils().dip2px(100), U.getDisplayUtils().dip2px(100));
                //            GrabTopContentView.this.addView(vp.SVGAImageView, lp);
                mGrabTopItemViewArrayList.add(vp)
            }
        }
        resetAllGrabTopItemView()
    }

    private fun resetAllGrabTopItemView() {
        for (vp in mGrabTopItemViewArrayList) {
            vp.grabTopItemView?.reset()
            vp.grabTopItemView?.setToPlaceHolder()
//            vp.grabTopItemView?.visibility = View.GONE
        }
    }

    //只有轮次切换的时候调用
    private fun initData() {
        if (mRoomData?.hasGameBegin() == false) {
            MyLog.d(TAG, "游戏未开始，不能用轮次信息里更新头像")
            resetAllGrabTopItemView()
            val list = mRoomData!!.getPlayerAndWaiterInfoList()
            var i = 0
            while (i < list.size && i < mGrabTopItemViewArrayList.size) {
                val vp = mGrabTopItemViewArrayList[i]
                val playerInfoModel = list[i]
                mInfoMap[playerInfoModel.userID] = vp
                vp.grabTopItemView!!.bindData(playerInfoModel, mRoomData!!.ownerId == playerInfoModel.userID)
                i++
            }
        } else {
            val now = mRoomData?.expectRoundInfo
            if (now == null) {
                MyLog.w(TAG, "initData data error")
                return
            }
            if (mCurSeq == now.roundSeq) {
                MyLog.w(TAG, "initdata 轮次一样，无需更新")
                return
            }
            mCurSeq = now.roundSeq
            for (i in mGrabTopItemViewArrayList.indices) {
                val vp = mGrabTopItemViewArrayList[i]
                vp.grabTopItemView!!.visibility = View.VISIBLE
            }
            resetAllGrabTopItemView()
            val playerInfoModels = now.playUsers
            mInfoMap.clear()
            MyLog.d(TAG, "initData playerInfoModels.size() is " + playerInfoModels.size)
            var i = 0
            while (i < playerInfoModels.size && i < mGrabTopItemViewArrayList.size) {
                val vp = mGrabTopItemViewArrayList[i]
                val playerInfoModel = playerInfoModels[i]
                mInfoMap[playerInfoModel.userID] = vp
                vp.grabTopItemView?.bindData(playerInfoModel, mRoomData!!.ownerId == playerInfoModel.userID)
                for (userId in now.singUserIds) {
                    if (userId == playerInfoModel.userID && now.isSingStatus) {
                        if (vp.grabTopItemView != null) {
                            vp.grabTopItemView!!.setGetSingChance()
                        }
                        val finalGrabTopItemView = vp.grabTopItemView
                        finalGrabTopItemView?.mCircleAnimationView?.visibility = View.VISIBLE
                        finalGrabTopItemView?.mCircleAnimationView?.setProgress(100)
                        vp.grabTopItemView?.mAvatarIv?.scaleX = 1.08f
                        vp.grabTopItemView?.mAvatarIv?.scaleY = 1.08f
                        vp.grabTopItemView?.setGrap(now.wantSingType)
                    }
                }
                i++
            }

            MyLog.d(TAG, "initData + now.getStatus() " + now.status)
            if (now.status == EQRoundStatus.QRS_INTRO.value) {
                for (wantSingerInfo in now.wantSingInfos) {
                    val vp = mInfoMap[wantSingerInfo.userID]
                    vp?.grabTopItemView?.setGrap(wantSingerInfo.wantSingType)
                }
            } else {
                MyLog.d(TAG, "initData else")
                for (vp in mGrabTopItemViewArrayList) {
                    MyLog.d(TAG, "initData else 2")
                    vp?.grabTopItemView?.hideGrabIcon()
                }
                initLight()
                syncLight()
            }
            setInviteStatus(playerInfoModels.size)
        }
        //        ConstraintLayout.LayoutParams lp = (LayoutParams) mContentLl.getLayoutParams();
        //        lp.leftMargin = U.getDisplayUtils().dip2px(7);
        //        lp.rightMargin = U.getDisplayUtils().dip2px(48);
        //        mContentLl.setLayoutParams(lp);
    }

    private fun setInviteStatus(size: Int) {
        if (mGrabTopItemViewArrayList.size != 0) {
            if (mRoomData!!.roomType == GrabRoomType.ROOM_TYPE_GUIDE) {
                // 新手房
                mInviteIv.visibility = View.GONE
            } else {
                if (mRoomData?.ownerId != 0) {
                    // 房主房
                    if (mRoomData?.isOwner == true && size < getMaxCount()) {
                        mInviteIv.visibility = View.VISIBLE
                    } else {
                        mInviteIv.visibility = View.GONE
                    }
                } else {
                    // 普通房
                    if (mRoomData!!.roomType == GrabRoomType.ROOM_TYPE_PLAYBOOK) {
                        mInviteIv.visibility = View.GONE
                    } else {
                        if (size < getMaxCount()) {
                            mInviteIv.visibility = View.VISIBLE
                        } else {
                            mInviteIv.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private fun getMaxCount(): Int {
        return (mRoomData?.grabConfigModel?.maxUserCnt ?: 7)
    }

    //刚进来的时候初始化灯
    private fun initLight() {
        val iterator = mInfoMap.entries.iterator()
        while (iterator.hasNext()) {
            iterator.next().value.grabTopItemView!!.setLight(true)
        }
    }

    //这里可能人员有变动
    fun setModeGrab() {
        // 切换到抢唱模式
        if (mAnimatorAllSet != null) {
            mAnimatorAllSet!!.cancel()
        }
        initData()
        for (uId in mInfoMap.keys) {
            val vp = mInfoMap[uId]
            if (vp != null && vp.grabTopItemView != null) {
                vp.grabTopItemView!!.visibility = View.VISIBLE
                vp.grabTopItemView!!.reset()
                val wantSingerInfo = WantSingerInfo()
                wantSingerInfo.userID = uId
                val grabRoundInfoModel = mRoomData!!.realRoundInfo
                // TODO: 2019/2/26 判空
                if (grabRoundInfoModel != null && grabRoundInfoModel.wantSingInfos.contains(wantSingerInfo)) {
                    vp.grabTopItemView?.setGrap(wantSingerInfo.wantSingType)
                } else {
                    //                    if (vp.grabTopItemView.getPlayerInfoModel().isOnline()) {
                    //                        vp.grabTopItemView.setGrap(false);
                    //                    } else {
                    //                        //离线了
                    //                    }
                    vp.grabTopItemView?.hideGrabIcon()
                }
            }
        }

        //        ConstraintLayout.LayoutParams lp = (LayoutParams) mContentLl.getLayoutParams();
        //        lp.leftMargin = U.getDisplayUtils().dip2px(7);
        //        lp.rightMargin = U.getDisplayUtils().dip2px(48);
        //        mContentLl.setLayoutParams(lp);
    }

    fun setModeSing() {
        MyLog.d(TAG, "setModeSing")
        val now = mRoomData!!.realRoundInfo ?: return
        mHasBurst = false
        val singerUserIds = now.singUserIds
        if (!now.isParticipant && now.enterStatus == now.status) {
            // 如果是演唱阶段进来的参与者
            syncLight()
            for (userId in singerUserIds) {
                val vp = mInfoMap[userId]
                if (vp == null) {
                    MyLog.d(TAG, "没有在选手席位找到 id=$userId 相应ui，return")
                    continue
                }
                if (vp.grabTopItemView != null) {
                    vp.grabTopItemView?.setGetSingChance()
                }
                val finalGrabTopItemView = vp.grabTopItemView
                finalGrabTopItemView!!.mCircleAnimationView.visibility = View.VISIBLE
                finalGrabTopItemView.mCircleAnimationView.setProgress(100)
                vp.grabTopItemView?.mAvatarIv?.scaleX = 1.08f
                vp.grabTopItemView?.mAvatarIv?.scaleY = 1.08f
                vp.grabTopItemView?.setGrap(now.wantSingType)
            }
            EventBus.getDefault().post(LightOffAnimationOverEvent())
            return
        }

        val allAnimator = ArrayList<Animator>()

        if (now.status != EQRoundStatus.QRS_SPK_SECOND_PEER_SING.value) {
            val animatorSet123s = ArrayList<Animator>()
            // 第二轮不需要重复播这个动画
            for (userId in singerUserIds) {
                val vp = mInfoMap[userId]
                if (vp == null) {
                    MyLog.d(TAG, "没有在选手席位找到 id=$userId 相应ui，return")
                    continue
                }
                if (vp.grabTopItemView != null) {
                    vp.grabTopItemView?.setGetSingChance()
                }
                val finalGrabTopItemView = vp.grabTopItemView
                run {
                    // 这是圈圈动画
                    val objectAnimator1 = ValueAnimator()
                    objectAnimator1.setIntValues(0, 100)
                    objectAnimator1.duration = 495
                    objectAnimator1.addUpdateListener { animation ->
                        val p = animation.animatedValue as Int
                        finalGrabTopItemView!!.mCircleAnimationView.setProgress(p)
                    }
                    objectAnimator1.addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationStart(animation: Animator) {
                            super.onAnimationStart(animation)
                            finalGrabTopItemView!!.mCircleAnimationView.visibility = View.VISIBLE
                        }

                        override fun onAnimationCancel(animation: Animator) {
                            super.onAnimationCancel(animation)
                            finalGrabTopItemView!!.mCircleAnimationView.visibility = View.GONE
                        }
                    })

                    // 接下来是头像放大一点的动画
                    val propertyValuesHolder1 = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.08f)
                    val propertyValuesHolder2 = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.08f)
                    val objectAnimator = ObjectAnimator.ofPropertyValuesHolder(vp.grabTopItemView?.mAvatarIv, propertyValuesHolder1, propertyValuesHolder2)
                    val animatorSet23 = AnimatorSet()
                    animatorSet23.playTogether(objectAnimator)
                    animatorSet23.duration = (4 * 33).toLong()

                    val animatorSet123 = AnimatorSet()
                    animatorSet123.playTogether(objectAnimator1, animatorSet23)
                    animatorSet123s.add(animatorSet123)
                }
            }
            val animatorSet123ss = AnimatorSet()
            animatorSet123ss.playTogether(animatorSet123s)
            allAnimator.add(animatorSet123ss)
        }

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
        run {
            val liangdengList = ArrayList<Animator>()
            var i = 0
            for (uId in mInfoMap.keys) {
                val vp1 = mInfoMap[uId]
                val itemView = vp1?.grabTopItemView
                var filter = false
                for (uid2 in singerUserIds) {
                    if (uId == uid2) {
                        filter = true
                        continue
                    }
                }
                if (filter) {
                    continue
                }

                //                if (!itemView.getPlayerInfoModel().isOnline()) {
                //                    continue;
                //                }
                val objectAnimator1 = ValueAnimator()
                objectAnimator1.setIntValues(0, 0)
                objectAnimator1.duration = 1
                val tti = i
                objectAnimator1.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator) {
                        super.onAnimationStart(animation)
                        //                        if (tti == 0) {
                        //                            U.getSoundUtils().play(GrabRoomFragment.TAG, R.raw.grab_lightup);
                        //                        }
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        super.onAnimationCancel(animation)
                        onAnimationEnd(animation)
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        itemView!!.setLight(true)
                    }
                })
                objectAnimator1.startDelay = (i * 4 * 33).toLong()
                i++
                liangdengList.add(objectAnimator1)
            }
            val animatorSet1s = AnimatorSet()
            animatorSet1s.playTogether(liangdengList)
            animatorSet1s.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    super.onAnimationStart(animation)
                    for (uid in singerUserIds) {
                        val vp1 = mInfoMap[uid]
                        if (vp1 != null) {
                            // 出灯前 圈圈消失
                            val itemView = vp1.grabTopItemView
                            itemView!!.mCircleAnimationView.visibility = View.GONE
                            itemView.hideGrabIcon()
                        }
                    }

                }
            })
            animatorSet1s.startDelay = (20 * 33).toLong()
            allAnimator.add(animatorSet1s)
        }


        if (mAnimatorAllSet != null) {
            mAnimatorAllSet!!.cancel()
        }

        mAnimatorAllSet = AnimatorSet()
        mAnimatorAllSet!!.playSequentially(allAnimator)
        mAnimatorAllSet!!.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationCancel(animation: Animator) {
                super.onAnimationCancel(animation)
                //setModeGrab();
            }

            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                syncLight()
                EventBus.getDefault().post(LightOffAnimationOverEvent())
            }
        })
        mAnimatorAllSet!!.start()
    }

    //有人爆灯了，这个时候所有的灯都闪烁
    private fun toBurstState() {
        mHasBurst = true
        for (uId in mInfoMap.keys) {
            val vp = mInfoMap[uId]
            if (vp != null && vp.grabTopItemView != null && !RoomDataUtils.isRoundSinger(mRoomData!!.realRoundInfo, uId.toLong())) {
                vp.grabTopItemView?.startEvasive()
            }
        }
    }

    private fun syncLight() {
        val now = mRoomData!!.realRoundInfo
        if (now != null) {
            for (noPassingInfo in now.mLightInfos) {
                val vp = mInfoMap[noPassingInfo.userID]
                if (vp != null && vp.grabTopItemView != null) {
                    vp.grabTopItemView?.setLight(false)
                }
            }
        }
    }

    private fun grap(wantSingerInfo: WantSingerInfo) {
        val vp = mInfoMap[wantSingerInfo.userID]
        if (vp != null && vp.grabTopItemView != null) {
            vp.grabTopItemView?.setGrap(wantSingerInfo.wantSingType)
        }
    }

    private fun lightOff(uid: Int) {
        if (mHasBurst) {
            MyLog.w(TAG, "已经爆灯了，所以灭灯也忽略 uid 是：$uid")
            return
        }
        val vp = mInfoMap[uid]
        if (vp != null && vp.grabTopItemView != null) {
            //            setLightOffAnimation(vp);
            setLightOff(vp)
        }
    }

    private fun onlineChange(playerInfoModel: PlayerInfoModel?) {
        if (playerInfoModel != null && playerInfoModel.userInfo != null) {
            val vp = mInfoMap[playerInfoModel.userInfo.userId]
            if (vp != null) {
                vp.grabTopItemView?.updateOnLineState(playerInfoModel)
            }
        } else {
            MyLog.w(TAG, "onlineChange playerInfoModel error")
        }
    }

    private fun setLightOff(vp: VP) {
        val grabTopItemView = vp.grabTopItemView
        grabTopItemView!!.setLight(false)
    }

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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: GrabPlaySeatUpdateEvent) {
        MyLog.d(TAG, "onEvent event=$event")
        initData()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: EngineEvent) {
        /**
         * 提示说话声纹
         */
        if (event.getType() == EngineEvent.TYPE_USER_AUDIO_VOLUME_INDICATION) {
            val list = event.getObj<List<EngineEvent.UserVolumeInfo>>()
            for (uv in list) {
                //MyLog.d(TAG,"UserVolumeInfo uv=" + uv);
                var uid = uv.uid
                if (uid == 0) {
                    uid = MyUserInfoManager.uid.toInt()
                }
                val vp = mInfoMap[uid]
                if (vp != null && vp.grabTopItemView != null && vp.grabTopItemView?.isShown == true) {
                    vp.grabTopItemView?.showSpeakingAnimation()
                }
            }
        } else if (event.getType() == EngineEvent.TYPE_USER_MUTE_AUDIO) {
            //用户闭麦，开麦
            val userStatus = event.getUserStatus()
            if (userStatus != null) {
                val userId = userStatus.userId
                val vp = mInfoMap[userId]
                if (vp?.grabTopItemView?.isShown == true && userStatus.isAudioMute) {
                    vp.grabTopItemView?.hideSpeakingAnimation()
                }
            }
        }
    }

    fun onChangeRoom() {
        horizontalScrollView?.scrollTo(0, 0)
    }

    fun setRoomData(roomData: GrabRoomData) {
        mRoomData = roomData
        addChildView()
        //        mGrabAudienceView.setRoomData(mRoomData);
        setInviteStatus(mRoomData?.getPlayerAndWaiterInfoList()?.size ?: 1)
        initData()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (mAnimatorAllSet != null) {
            mAnimatorAllSet!!.cancel()
        }
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


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: SomeOneGrabEvent) {
        grap(event.mWantSingerInfo)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: GrabSomeOneLightOffEvent) {
        lightOff(event.uid)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: GrabSomeOneLightBurstEvent) {
        toBurstState()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: SomeOneOnlineChangeEvent) {
        onlineChange(event.playerInfoModel)
    }

    internal class VP {
        var grabTopItemView: GrabTopItemView? = null
        //SVGAImageView SVGAImageView;
    }

    fun setListener(listener: Listener) {
        mListener = listener
    }

    interface Listener {
        fun clickArrow(open: Boolean)
    }

}