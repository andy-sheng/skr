package com.moudule.playways.beauty.fragment

import android.os.Bundle
import android.view.View

import com.common.base.BaseFragment
import com.common.log.MyLog
import com.module.playways.R
import com.moudule.playways.beauty.view.BeautyControlPanelView
import com.common.view.ex.ExTextView
import android.view.TextureView



class BeautyPreviewFragment : BaseFragment() {

    lateinit var mVideoTexture : TextureView    // 视频的view
    lateinit var mBeautyControlView: BeautyControlPanelView
    lateinit var mEnterRoomTv: ExTextView

    override fun initView(): Int {
        return R.layout.beauty_preview_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        mVideoTexture = mRootView.findViewById<View>(R.id.video_texture) as TextureView
        mBeautyControlView = mRootView.findViewById<View>(R.id.beauty_control_view) as BeautyControlPanelView
        mEnterRoomTv = mRootView.findViewById<View>(R.id.enter_room_tv) as ExTextView

        mBeautyControlView.setListener(object : BeautyControlPanelView.Listener {
            override fun onChangeBeauty(id: Int, progress: Int) {
                MyLog.d("BeautyPreviewFragment", "onChangeBeauty id = " + id + "progress = " + progress)
            }

            override fun onChangeFiter(id: Int, progress: Int) {
                MyLog.d("BeautyPreviewFragment", "onChangeFiter id = " + id + "progress = " + progress)
            }

            override fun onChangePater(id: Int) {
                MyLog.d("BeautyPreviewFragment", "onChangePater id =" + id)
            }

        })

        mEnterRoomTv.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {

            }
        })

    }

    override fun useEventBus(): Boolean {
        return false
    }
}
