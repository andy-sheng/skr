package com.module.playways.race.room.ui

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewStub
import com.common.base.BaseFragment
import com.common.utils.U
import com.module.playways.R
import com.module.playways.grab.room.view.GrabRootView
import com.module.playways.grab.room.voicemsg.VoiceRecordTipsView
import com.module.playways.grab.room.voicemsg.VoiceRecordUiController
import com.module.playways.race.room.RaceRoomData
import com.module.playways.race.room.bottom.RaceBottomContainerView
import com.module.playways.race.room.presenter.RaceCorePresenter
import com.module.playways.race.room.view.RaceInputContainerView
import com.module.playways.room.gift.view.ContinueSendView
import com.module.playways.room.gift.view.GiftDisplayView
import com.module.playways.room.gift.view.GiftPanelView
import com.module.playways.room.room.comment.CommentView
import com.module.playways.room.room.comment.listener.CommentViewItemListener
import com.module.playways.room.room.gift.GiftBigAnimationViewGroup
import com.module.playways.room.room.gift.GiftBigContinuousView
import com.module.playways.room.room.gift.GiftContinueViewGroup
import com.module.playways.room.room.gift.GiftOverlayAnimationViewGroup
import com.module.playways.room.room.view.BottomContainerView

class RaceRoomFragment : BaseFragment() {

    internal lateinit var mInputContainerView: RaceInputContainerView
    internal lateinit var mBottomContainerView: RaceBottomContainerView
    internal lateinit var mVoiceRecordTipsView: VoiceRecordTipsView
    internal lateinit var mCommentView: CommentView
    internal lateinit var mGiftPanelView: GiftPanelView
    internal lateinit var mContinueSendView: ContinueSendView
    internal lateinit var mCorePresenter: RaceCorePresenter

    lateinit var mVoiceRecordUiController: VoiceRecordUiController
    var mRoomData = RaceRoomData()
    override fun initView(): Int {
        return R.layout.race_room_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        mCorePresenter = RaceCorePresenter(mRoomData)
        addPresent(mCorePresenter)

        // 请保证从下面的view往上面的view开始初始化
        rootView.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                mInputContainerView.hideSoftInput()
            }
            false
        }
        initInputView()
        initBottomView()
        initCommentView()
        initGiftPanelView()
        initGiftDisplayView()
    }

    private fun initInputView() {
        mInputContainerView = rootView.findViewById(R.id.input_container_view)
        mInputContainerView.setRoomData(mRoomData)
    }

    private fun initBottomView() {
        run {
            val viewStub = rootView.findViewById<ViewStub>(R.id.voice_record_tip_view_stub)
            mVoiceRecordTipsView = VoiceRecordTipsView(viewStub)
        }
//        mBottomBgVp = rootView.findViewById<ViewGroup>(R.id.bottom_bg_vp)
//        val lp = mBottomBgVp.getLayoutParams() as RelativeLayout.LayoutParams
//        /**
//         * 按比例适配手机
//         */
//        lp.height = U.getDisplayUtils().screenHeight * 284 / 667

        mBottomContainerView = rootView.findViewById(R.id.bottom_container_view)
        mBottomContainerView.setRoomData(mRoomData)
        mBottomContainerView.setListener(object : BottomContainerView.Listener() {
            override fun showInputBtnClick() {
//                if (mPersonInfoDialog != null && mPersonInfoDialog.isShowing()) {
//                    mPersonInfoDialog.dismiss()
//                }
                mInputContainerView.showSoftInput()
            }

            override fun clickRoomManagerBtn() {
                //                U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(GrabRoomFragment.this.getActivity(), OwnerManageFragment.class)
                //                        .setAddToBackStack(true)
                //                        .setHasAnimation(true)
                //                        .setEnterAnim(R.anim.slide_right_in)
                //                        .setExitAnim(R.anim.slide_right_out)
                //                        .addDataBeforeAdd(0, mRoomData)
                //                        .build());
//                SongManagerActivity.open(activity, mRoomData)
//                removeManageSongTipView()
            }

            override fun showGiftPanel() {
                mGiftPanelView.show(null)
                mContinueSendView.setVisibility(View.GONE)
            }
        })
    }

    private fun initCommentView() {
        mCommentView = rootView.findViewById(R.id.comment_view)
        mCommentView.setListener(CommentViewItemListener {
            //            userId -> showPersonInfoView(userId)
        })
        mCommentView.roomData = mRoomData
        val layoutParams = mCommentView.layoutParams
        layoutParams.height = U.getDisplayUtils().getPhoneHeight() - U.getDisplayUtils().dip2px((430 + 60).toFloat())
        mVoiceRecordUiController = VoiceRecordUiController(mBottomContainerView.mVoiceRecordBtn, mVoiceRecordTipsView, mCommentView)
    }

    private fun initGiftPanelView() {
        mGiftPanelView = rootView.findViewById<View>(R.id.gift_panel_view) as GiftPanelView
        mGiftPanelView.setRoomData(mRoomData)
        mContinueSendView = rootView.findViewById<View>(R.id.continue_send_view) as ContinueSendView
        mContinueSendView.setRoomData(mRoomData)
        mContinueSendView.setObserver(object : ContinueSendView.OnVisibleStateListener {
            override fun onVisible(isVisible: Boolean) {
                mBottomContainerView.setOpVisible(!isVisible)
            }
        })
        mGiftPanelView.setIGetGiftCountDownListener(object : GiftDisplayView.IGetGiftCountDownListener {
            override fun getCountDownTs(): Long {
                //                return mGiftTimerPresenter.getCountDownSecond();
                return 0
            }
        })
    }

    private fun initGiftDisplayView() {
        val giftContinueViewGroup = rootView.findViewById<GiftContinueViewGroup>(R.id.gift_continue_vg)
        giftContinueViewGroup.setRoomData(mRoomData)
        val giftOverlayAnimationViewGroup = rootView.findViewById<GiftOverlayAnimationViewGroup>(R.id.gift_overlay_animation_vg)
        giftOverlayAnimationViewGroup.setRoomData(mRoomData)
        val giftBigAnimationViewGroup = rootView.findViewById<GiftBigAnimationViewGroup>(R.id.gift_big_animation_vg)
        giftBigAnimationViewGroup.setRoomData(mRoomData)
        val giftBigContinueView = rootView.findViewById<GiftBigContinuousView>(R.id.gift_big_continue_view)
        giftBigAnimationViewGroup.setGiftBigContinuousView(giftBigContinueView)
        //mDengBigAnimation = rootView.findViewById<View>(R.id.deng_big_animation) as GrabDengBigAnimationView
    }

    override fun useEventBus(): Boolean {
        return false
    }
}
