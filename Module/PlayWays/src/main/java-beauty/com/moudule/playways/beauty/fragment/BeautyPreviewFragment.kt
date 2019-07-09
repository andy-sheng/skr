package com.moudule.playways.beauty.fragment

import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.view.View

import com.common.base.BaseFragment
import com.common.log.MyLog
import com.module.playways.R
import com.moudule.playways.beauty.view.BeautyControlPanelView
import com.common.view.ex.ExTextView
import android.view.TextureView
import android.view.ViewStub
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.core.permission.SkrCameraPermission
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.titlebar.CommonTitleBar
import com.component.busilib.beauty.FROM_CREATE_GRAB_ROOM
import com.component.busilib.beauty.FROM_FRIEND_RECOMMEND
import com.component.busilib.beauty.FROM_IN_GRAB_ROOM
import com.component.busilib.beauty.FROM_MATCH
import com.component.busilib.friends.SpecialModel
import com.engine.Params
import com.module.RouterConstants
import com.module.playways.IPlaywaysModeService
import com.module.playways.grab.room.GrabRoomServerApi
import com.module.playways.room.prepare.model.JoinGrabRoomRspModel
import com.moudule.playways.beauty.event.ReturnFromBeautyActivityEvent
import com.zq.mediaengine.capture.CameraCapture
import com.zq.mediaengine.kit.ZqEngineKit
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import java.util.HashMap


class BeautyPreviewFragment : BaseFragment() {

    lateinit var mVideoTexture: TextureView    // 视频的view
    lateinit var mBeautyControlView: BeautyControlPanelView
    lateinit var mEnterRoomTv: ExTextView
    var mFrom: Int? = 0
    var mSpecialModel: SpecialModel? = null
    var mRoomId: Int? = null
    var mInviteType: Int? = null
    var mRoomType: Int? = null
    lateinit var mTitleBar: CommonTitleBar
    lateinit var mBeautyOpenBtn: View
    override fun initView(): Int {
        return R.layout.beauty_preview_fragment_layout
    }

    var mSkrCameraPermission = SkrCameraPermission();

    override fun initData(savedInstanceState: Bundle?) {
        MyLog.d(TAG, "mFrom=${mFrom}")
        mTitleBar = mRootView.findViewById<CommonTitleBar>(R.id.titlebar)
        mTitleBar.leftTextView.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                onBackPressed()
            }
        })
        mVideoTexture = mRootView.findViewById<View>(R.id.video_texture) as TextureView
        var lp = mVideoTexture.layoutParams as ConstraintLayout.LayoutParams
        lp.height = U.getDisplayUtils().screenWidth * 16 / 9
        var viewStub = mRootView.findViewById<ViewStub>(R.id.beauty_control_panel_view_stub);
        mBeautyControlView = BeautyControlPanelView(viewStub)
        mEnterRoomTv = mRootView.findViewById<View>(R.id.enter_room_tv) as ExTextView
        mBeautyOpenBtn = mRootView.findViewById<View>(R.id.beauty_open_btn)

        mBeautyOpenBtn.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                mBeautyControlView.show()
            }
        })

        mEnterRoomTv.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if (mFrom == FROM_MATCH) {
                    // 从匹配进入的继续去匹配页面
                    val iRankingModeService = ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation() as IPlaywaysModeService
                    iRankingModeService?.tryGoGrabMatch(mSpecialModel!!.getTagID())
                    activity?.finish()
                } else if (mFrom == FROM_FRIEND_RECOMMEND) {
                    val iRankingModeService = ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation() as IPlaywaysModeService
                    iRankingModeService?.tryGoGrabRoom(mRoomId!!, mInviteType!!)
                    activity?.finish()
                } else if (mFrom == FROM_CREATE_GRAB_ROOM) {
                    createRoom()
                }
            }
        })


        if (mFrom != FROM_IN_GRAB_ROOM) {
            var params = Params.getFromPref()
            params.isEnableVideo = true;
            ZqEngineKit.getInstance().init("BeautyPreview", params)
            config()
        } else {
            mBeautyOpenBtn.visibility = View.GONE
            mEnterRoomTv.visibility = View.GONE
            mBeautyControlView.show()
            mBeautyControlView.enableHide(false)
        }
        // 设置预览View
        ZqEngineKit.getInstance().unbindAllRemoteVideo()
        ZqEngineKit.getInstance().setDisplayPreview(mVideoTexture)
        ZqEngineKit.getInstance().setLocalVideoRect(0f, 0f, 1f, 1f, 1f)
        mSkrCameraPermission.ensurePermission({
            ZqEngineKit.getInstance().startCameraPreview()
        }, true)
    }

    protected fun config() {
        // 设置推流分辨率
        ZqEngineKit.getInstance().setPreviewResolution(ZqEngineKit.VIDEO_RESOLUTION_540P)
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

    }

    override fun onResume() {
        super.onResume()
        ZqEngineKit.getInstance().startCameraPreview()
    }

    override fun onPause() {
        super.onPause()
        if (activity?.isFinishing == false) {
            ZqEngineKit.getInstance().stopCameraPreview()
        }
    }

    override fun destroy() {
        super.destroy()
        if (mFrom != FROM_IN_GRAB_ROOM) {
            ZqEngineKit.getInstance().setDisplayPreview(null as TextureView?)
            ZqEngineKit.getInstance().stopCameraPreview()
            ZqEngineKit.getInstance().destroy("BeautyPreview")
        }
    }

    override fun onBackPressed(): Boolean {
        if (mFrom != FROM_IN_GRAB_ROOM) {
            if (mBeautyControlView.onBackPressed()) {
                return true
            }
        }
        activity?.finish()
        if (mFrom == FROM_IN_GRAB_ROOM) {
            ZqEngineKit.getInstance().setDisplayPreview(null as TextureView?)
            ZqEngineKit.getInstance().stopCameraPreview()
            EventBus.getDefault().post(ReturnFromBeautyActivityEvent())
        }
        return true
    }

    override fun setArguments(args: Bundle?) {
        super.setArguments(args)
        mFrom = args?.getInt("mFrom")
        mSpecialModel = args?.getSerializable("mSpecialModel") as SpecialModel?
        mRoomId = args?.getInt("mRoomId")
        mInviteType = args?.getInt("mInviteType")
        mRoomType = args?.getInt("mRoomType")
    }


    /**
     * 创建房间
     */
    private fun createRoom() {
        val grabRoomServerApi = ApiManager.getInstance().createService(GrabRoomServerApi::class.java)
        val map = HashMap<String, Any>()
        map["roomType"] = mRoomType!!
        map["tagID"] = mSpecialModel!!.tagID

        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        ApiMethods.subscribe(grabRoomServerApi.createRoom(body), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                if (result.errno == 0) {
                    val grabCurGameStateModel = JSON.parseObject(result.data!!.toString(), JoinGrabRoomRspModel::class.java)
                    grabCurGameStateModel.isHasGameBegin = false
                    //先跳转
                    ARouter.getInstance().build(RouterConstants.ACTIVITY_GRAB_ROOM)
                            .withSerializable("prepare_data", grabCurGameStateModel)
                            .withSerializable("special_model", mSpecialModel)
                            .navigation()
                    //结束当前Activity
                    this@BeautyPreviewFragment.activity?.finish()
                } else {
                    // 房间创建失败
                    U.getToastUtil().showShort("" + result.errmsg)
                }
            }
        }, this, ApiMethods.RequestControl("create-room", ApiMethods.ControlType.CancelThis))
    }

    override fun useEventBus(): Boolean {
        return false
    }
}
