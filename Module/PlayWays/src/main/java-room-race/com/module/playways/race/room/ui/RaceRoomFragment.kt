package com.module.playways.race.room.ui

import android.os.Bundle
import com.common.base.BaseFragment
import com.module.playways.R
import com.module.playways.race.room.RaceRoomData
import com.module.playways.race.room.bottom.RaceBottomContainerView
import com.module.playways.race.room.view.RaceInputContainerView
import com.module.playways.room.room.comment.CommentView
import com.module.playways.room.room.comment.listener.CommentViewItemListener
import com.module.playways.room.room.view.BottomContainerView

class RaceRoomFragment : BaseFragment() {

    lateinit var mInputContainerView: RaceInputContainerView
    lateinit var mBottomContainerView: RaceBottomContainerView
    lateinit var mCommentView:CommentView
    var mRoomData = RaceRoomData()
    override fun initView(): Int {
        return R.layout.race_room_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        initInputView()
        initBottomView()
        initCommentView()
    }

    private fun initInputView() {
        mInputContainerView = rootView.findViewById(R.id.input_container_view)
        mInputContainerView.setRoomData(mRoomData)
    }

    private fun initBottomView() {
//        run {
//            val viewStub = rootView.findViewById<ViewStub>(R.id.grab_voice_record_tip_view_stub)
//            mVoiceRecordTipsView = VoiceRecordTipsView(viewStub)
//        }
//
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
//                if (mRoomData.getRealRoundInfo() != null) {
//                    val now = mRoomData.getRealRoundInfo()
//                    if (now != null) {
//                        if (now!!.isPKRound() && now!!.getStatus() == EQRoundStatus.QRS_SPK_SECOND_PEER_SING.value) {
//                            if (now!!.getsPkRoundInfoModels().size == 2) {
//                                val userId = now!!.getsPkRoundInfoModels().get(1).getUserID()
//                                mGiftPanelView.show(RoomDataUtils.getPlayerInfoById(mRoomData, userId.toLong()))
//                            } else {
//                                mGiftPanelView.show(RoomDataUtils.getPlayerInfoById(mRoomData, now!!.getUserID().toLong()))
//                            }
//                        } else {
//                            mGiftPanelView.show(RoomDataUtils.getPlayerInfoById(mRoomData, now!!.getUserID().toLong()))
//                        }
//                    } else {
//                        mGiftPanelView.show(null)
//                    }
//                } else {
//                    mGiftPanelView.show(null)
//                }
//
//                mContinueSendView.setVisibility(GONE)
            }
        })
    }

    private fun initCommentView() {
        mCommentView = rootView.findViewById(R.id.comment_view)
        mCommentView.setListener(CommentViewItemListener {
//            userId -> showPersonInfoView(userId)
        })
//        mCommentView.roomData = mRoomData
        //        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mCommentView.getLayoutParams();
        //        layoutParams.height = U.getDisplayUtils().getPhoneHeight() - U.getDisplayUtils().dip2px(430 + 60);
//        mVoiceRecordUiController = VoiceRecordUiController(mBottomContainerView.mVoiceRecordBtn, mVoiceRecordTipsView, mCommentView)
    }

    override fun useEventBus(): Boolean {
        return false
    }
}
