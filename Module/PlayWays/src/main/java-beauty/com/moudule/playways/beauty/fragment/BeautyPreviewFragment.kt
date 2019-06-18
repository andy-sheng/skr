package com.moudule.playways.beauty.fragment

import android.os.Bundle
import android.view.View

import com.common.base.BaseFragment
import com.common.log.MyLog
import com.module.playways.R
import com.moudule.playways.beauty.view.BeautyControlPanelView
import com.common.view.ex.ExTextView
import android.view.TextureView
import android.view.ViewStub
import com.alibaba.android.arouter.launcher.ARouter
import com.component.busilib.friends.RecommendModel
import com.component.busilib.friends.SpecialModel
import com.engine.Params
import com.module.RouterConstants
import com.module.playways.IPlaywaysModeService
import com.zq.mediaengine.capture.CameraCapture
import com.zq.mediaengine.kit.ZqEngineKit


class BeautyPreviewFragment : BaseFragment() {

    lateinit var mVideoTexture: TextureView    // 视频的view
    lateinit var mBeautyControlView: BeautyControlPanelView
    lateinit var mEnterRoomTv: ExTextView
    var mFrom: Int? = 0
    var mSpecialModel: SpecialModel? = null
    var mRecommendModel: RecommendModel? = null
    override fun initView(): Int {
        return R.layout.beauty_preview_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        mVideoTexture = mRootView.findViewById<View>(R.id.video_texture) as TextureView
        var viewStub = mRootView.findViewById<ViewStub>(R.id.beauty_control_panel_view_stub);
        mBeautyControlView = BeautyControlPanelView(viewStub)
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
                if (mFrom == 1) {
                    // 从匹配进入的继续去匹配页面
                    val iRankingModeService = ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation() as IPlaywaysModeService
                    mSpecialModel?.run {
                        iRankingModeService?.tryGoGrabMatch(mSpecialModel!!.getTagID())
                        activity?.finish()
                    }?.run {
                        MyLog.d("mSpecialModel is null")
                    }
                } else if (mFrom == 2) {
                    val iRankingModeService = ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation() as IPlaywaysModeService
                    mRecommendModel?.let {
                        iRankingModeService?.tryGoGrabRoom(mRecommendModel!!.getRoomInfo()!!.getRoomID(), 0)
                    }
                }
            }
        })
        mBeautyControlView.tryInflate()
        var params = Params.getFromPref()
        params.isEnableVideo = true;
        ZqEngineKit.getInstance().init("BeautyPreview", params)
        config()
        ZqEngineKit.getInstance().startCameraPreview()
        ZqEngineKit.getInstance().setLocalVideoRect(0f, 0f, 1f, 1f, 1f)
    }

    protected fun config() {
        // 设置推流分辨率
        ZqEngineKit.getInstance().setPreviewResolution(ZqEngineKit.VIDEO_RESOLUTION_720P)
        ZqEngineKit.getInstance().setTargetResolution(ZqEngineKit.VIDEO_RESOLUTION_360P)

        // 设置推流帧率
        ZqEngineKit.getInstance().previewFps = 30f
        ZqEngineKit.getInstance().targetFps = 30f

        // 设置视频方向（横屏、竖屏）
        //mIsLandscape = false;
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ZqEngineKit.getInstance().rotateDegrees = 0

        // 选择前后摄像头
        ZqEngineKit.getInstance().cameraFacing = CameraCapture.FACING_FRONT

        // 设置预览View
        ZqEngineKit.getInstance().setDisplayPreview(mVideoTexture)
    }

    override fun destroy() {
        super.destroy()
        ZqEngineKit.getInstance().stopCameraPreview()
        ZqEngineKit.getInstance().destroy("BeautyPreview")
    }


    override fun setArguments(args: Bundle?) {
        super.setArguments(args)
        mFrom = args?.getInt("from")
        mSpecialModel = args?.getSerializable("SpecialModel") as SpecialModel?
        mRecommendModel = args?.getSerializable("RecommendModel") as RecommendModel?
    }

    override fun useEventBus(): Boolean {
        return false
    }
}
