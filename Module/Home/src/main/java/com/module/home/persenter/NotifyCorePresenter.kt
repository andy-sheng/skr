package com.module.home.persenter

import android.app.Activity
import android.graphics.Color
import android.os.Handler
import android.os.Message
import android.text.SpannableStringBuilder
import android.view.Gravity
import android.view.View

import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.anim.ObjectPlayControlTemplate
import com.common.core.global.event.ShowDialogInHomeEvent
import com.common.core.permission.SkrAudioPermission
import com.common.core.permission.SkrCameraPermission
import com.common.core.scheme.SchemeSdkActivity
import com.common.core.scheme.event.BothRelationFromSchemeEvent
import com.common.core.scheme.event.DoubleInviteFromSchemeEvent
import com.common.core.scheme.event.GrabInviteFromSchemeEvent
import com.common.core.userinfo.ResultCallback
import com.common.core.userinfo.UserInfoManager
import com.common.core.userinfo.model.UserInfoModel
import com.common.floatwindow.FloatWindow
import com.common.floatwindow.MoveType
import com.common.floatwindow.Screen
import com.common.floatwindow.ViewStateListenerAdapter
import com.common.log.MyLog
import com.common.mvp.PresenterEvent
import com.common.mvp.RxLifeCyclePresenter
import com.common.notification.event.*
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.statistics.StatisticsAdapter
import com.common.utils.ActivityUtils
import com.common.utils.SpanUtils
import com.common.utils.U
import com.common.view.AnimateClickListener
import com.component.busilib.manager.WeakRedDotManager
import com.component.busilib.verify.SkrVerifyUtils
import com.component.dialog.ConfirmDialog
import com.component.dialog.NotifyDialogView
import com.component.notification.DoubleInviteNotifyView
import com.component.notification.FollowNotifyView
import com.component.notification.GrabInviteNotifyView
import com.dialog.view.TipsDialogView
import com.module.RouterConstants
import com.module.home.MainPageSlideApi
import com.module.home.R
import com.module.home.view.INotifyView
import com.module.playways.IPlaywaysModeService
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import com.zq.live.proto.Common.EMsgRoomMediaType

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

import java.util.HashMap
import java.util.concurrent.TimeUnit

import io.reactivex.Observable
import io.reactivex.functions.Consumer
import okhttp3.MediaType
import okhttp3.RequestBody

import com.component.busilib.beauty.FROM_FRIEND_RECOMMEND
import com.component.notification.GrabFullStarNotifyView

class NotifyCorePresenter(internal var mINotifyView: INotifyView) : RxLifeCyclePresenter() {

    internal var mBeFriendDialog: DialogPlus? = null
    internal var mSysWarnDialogPlus: DialogPlus? = null

    internal var mMainPageSlideApi = ApiManager.getInstance().createService(MainPageSlideApi::class.java)

    internal var mSkrAudioPermission: SkrAudioPermission? = SkrAudioPermission()

    internal var mSkrCameraPermission = SkrCameraPermission()

    internal var mRealNameVerifyUtils = SkrVerifyUtils()

    internal var mUiHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                MSG_DISMISS_INVITE_FLOAT_WINDOW -> FloatWindow.destroy(TAG_INVITE_FOALT_WINDOW)
                MSG_DISMISS_RELATION_FLOAT_WINDOW -> FloatWindow.destroy(TAG_RELATION_FOALT_WINDOW)
                MSG_DISMISS_DOUBLE_INVITE_FOALT_WINDOW -> FloatWindow.destroy(TAG_DOUBLE_INVITE_FOALT_WINDOW, 2)
                MSG_DISMISS_DOUBLE_ROOM_INVITE_FOALT_WINDOW -> FloatWindow.destroy(TAG_DOUBLE_ROOM_INVITE_FOALT_WINDOW, 2)
                MSG_DISMISS_STAND_FULL_STAR->FloatWindow.destroy(TAG_STAND_FULL_STAR_FOALT_WINDOW,2)
            }
        }
    }

    internal var mFloatWindowDataFloatWindowObjectPlayControlTemplate: ObjectPlayControlTemplate<FloatWindowData, NotifyCorePresenter>? = object : ObjectPlayControlTemplate<FloatWindowData, NotifyCorePresenter>() {
        override fun accept(cur: FloatWindowData): NotifyCorePresenter? {
            if (FloatWindow.hasFollowWindowShow()) {
                return null
            }
            if (!U.getActivityUtils().isAppForeground) {
                MyLog.d(TAG, "在后台，不弹出通知")
                return null
            }
            return this@NotifyCorePresenter
        }

        override fun onStart(floatWindowData: FloatWindowData, floatWindow: NotifyCorePresenter) {
            if (floatWindowData.mType == FloatWindowData.Type.FOLLOW) {
                showFollowFloatWindow(floatWindowData)
            } else if (floatWindowData.mType == FloatWindowData.Type.GRABINVITE) {
                showGrabInviteFloatWindow(floatWindowData)
            } else if (floatWindowData.mType == FloatWindowData.Type.DOUBLE_GRAB_INVITE) {
                showDoubleInviteFloatWindow(floatWindowData)
            } else if (floatWindowData.mType == FloatWindowData.Type.DOUBLE_ROOM_INVITE) {
                showDoubleInviteFromRoomFloatWindow(floatWindowData)
            }else if(floatWindowData.mType == FloatWindowData.Type.STAND_FULL_STAR){
                showStandFullStarFloatWindow(floatWindowData)
            }
        }

        override fun onEnd(floatWindowData: FloatWindowData?) {

        }
    }

    init {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun destroy() {
        super.destroy()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
        if (mFloatWindowDataFloatWindowObjectPlayControlTemplate != null) {
            mFloatWindowDataFloatWindowObjectPlayControlTemplate!!.destroy()
        }
        if (mBeFriendDialog != null) {
            mBeFriendDialog!!.dismiss(false)
        }
        if (mSysWarnDialogPlus != null) {
            mSysWarnDialogPlus!!.dismiss(false)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: GrabInviteFromSchemeEvent) {
        // TODO: 2019/3/20   一场到底邀请 口令
        if (event.ask == 1) {
            // 需要再次确认弹窗
            UserInfoManager.getInstance().getUserInfoByUuid(event.ownerId, true, object : ResultCallback<UserInfoModel>() {
                override fun onGetLocalDB(o: UserInfoModel): Boolean {
                    return false
                }

                override fun onGetServer(userInfoModel: UserInfoModel?): Boolean {
                    if (userInfoModel != null) {
                        var activity = U.getActivityUtils().topActivity
                        if (activity is SchemeSdkActivity) {
                            activity = U.getActivityUtils().homeActivity
                        }
                        val confirmDialog = ConfirmDialog(activity, userInfoModel, ConfirmDialog.TYPE_INVITE_CONFIRM)
                        confirmDialog.setListener { userInfoModel ->
                            if (userInfoModel != null) {
                                if (!userInfoModel.isFriend) {
                                    MyLog.d(TAG, "同意邀请，强制成为好友$userInfoModel")
                                    UserInfoManager.getInstance().beFriend(userInfoModel.userId, null)
                                }
                            }
                            tryGoGrabRoom(event.mediaType, event.roomId, event.tagId, 2)
                        }
                        confirmDialog.show()
                    }
                    return false
                }
            })
        } else {
            // 不需要直接进
            tryGoGrabRoom(event.mediaType, event.roomId, event.tagId, 2)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: DoubleInviteFromSchemeEvent) {
        // 双人房间邀请口令
        if (event.ask == 1) {
            // 需要再次确认弹窗
            UserInfoManager.getInstance().getUserInfoByUuid(event.ownerId, true, object : ResultCallback<UserInfoModel>() {
                override fun onGetLocalDB(o: UserInfoModel): Boolean {
                    return false
                }

                override fun onGetServer(userInfoModel: UserInfoModel?): Boolean {
                    if (userInfoModel != null) {
                        var activity = U.getActivityUtils().topActivity
                        if (activity is SchemeSdkActivity) {
                            activity = U.getActivityUtils().homeActivity
                        }
                        val confirmDialog = ConfirmDialog(activity, userInfoModel, ConfirmDialog.TYPE_DOUBLE_INVITE_CONFIRM)
                        confirmDialog.setListener {
                            Observable.timer(500, TimeUnit.MILLISECONDS)
                                    .compose(this@NotifyCorePresenter.bindUntilEvent(PresenterEvent.DESTROY))
                                    .subscribe { tryGoDoubleRoom(event.mediaType, event.ownerId, event.roomId, 2) }
                        }
                        confirmDialog.show()
                    }
                    return false
                }
            })
        } else {
            // 不需要直接进
            tryGoDoubleRoom(event.mediaType, event.ownerId, event.roomId, 2)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: BothRelationFromSchemeEvent) {
        // TODO: 2019/3/25 成为好友的的口令
        MyLog.d(TAG, "onEvent event=$event")
        UserInfoManager.getInstance().getUserInfoByUuid(event.useId, true, object : ResultCallback<UserInfoModel>() {
            override fun onGetLocalDB(o: UserInfoModel): Boolean {
                return false
            }

            override fun onGetServer(userInfoModel: UserInfoModel?): Boolean {
                if (userInfoModel != null) {
                    val stringBuilder = SpanUtils()
                            .append("是否确定与").setForegroundColor(Color.parseColor("#7F7F7F"))
                            .append("" + userInfoModel.nicknameRemark).setForegroundColor(Color.parseColor("#F5A623"))
                            .append("成为好友？").setForegroundColor(Color.parseColor("#7F7F7F"))
                            .create()
                    val tipsDialogView = TipsDialogView.Builder(U.app())
                            .setMessageTip(stringBuilder)
                            .setConfirmTip("确定")
                            .setCancelTip("取消")
                            .setConfirmBtnClickListener(object : AnimateClickListener() {
                                override fun click(view: View) {
                                    if (mBeFriendDialog != null) {
                                        mBeFriendDialog!!.dismiss(false)
                                    }
                                    if (userInfoModel.isFriend) {
                                        U.getToastUtil().showShort("你们已经是好友了")
                                    } else {
                                        UserInfoManager.getInstance().beFriend(userInfoModel.userId, null)
                                    }
                                }
                            })
                            .setCancelBtnClickListener(object : AnimateClickListener() {
                                override fun click(view: View) {
                                    if (mBeFriendDialog != null) {
                                        mBeFriendDialog!!.dismiss(false)
                                    }
                                }
                            })
                            .build()

                    var activity = U.getActivityUtils().topActivity
                    if (activity is SchemeSdkActivity) {
                        activity = U.getActivityUtils().homeActivity
                    }
                    mBeFriendDialog = DialogPlus.newDialog(activity!!)
                            .setContentHolder(ViewHolder(tipsDialogView))
                            .setGravity(Gravity.BOTTOM)
                            .setContentBackgroundResource(R.color.transparent)
                            .setOverlayBackgroundResource(R.color.black_trans_80)
                            .setExpanded(false)
                            .create()
                    EventBus.getDefault().post(ShowDialogInHomeEvent(mBeFriendDialog, 30))
                }
                return false
            }
        })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: CRSendInviteUserNotifyEvent) {
        val floatWindowData = FloatWindowData(FloatWindowData.Type.DOUBLE_GRAB_INVITE)
        floatWindowData.userInfoModel = event.userInfoModel
        floatWindowData.extra = event.msg
        mFloatWindowDataFloatWindowObjectPlayControlTemplate!!.add(floatWindowData, true)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: CRInviteInCreateRoomNotifyEvent) {
        val floatWindowData = FloatWindowData(FloatWindowData.Type.DOUBLE_ROOM_INVITE)
        floatWindowData.mediaType = EMsgRoomMediaType.EMR_AUDIO.value
        floatWindowData.userInfoModel = event.user
        floatWindowData.roomID = event.roomID
        floatWindowData.extra = event.inviteMsg
        mFloatWindowDataFloatWindowObjectPlayControlTemplate!!.add(floatWindowData, true)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: FeedLikeNotifyEvent) {
        WeakRedDotManager.getInstance().updateWeakRedRot(WeakRedDotManager.MESSAGE_FEED_LIKE_TYPE, 2, true)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: FeedCommentLikeNotifyEvent) {
        WeakRedDotManager.getInstance().updateWeakRedRot(WeakRedDotManager.MESSAGE_FEED_COMMENT_LIKE_TYPE, 2, true)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: FeedCommentAddNotifyEvent) {
        WeakRedDotManager.getInstance().updateWeakRedRot(WeakRedDotManager.MESSAGE_FEED_COMMENT_ADD_TYPE, 2, true)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: CRSyncInviteUserNotifyEvent) {
        val iRankingModeService = ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation() as IPlaywaysModeService
        iRankingModeService?.jumpToDoubleRoom(event)
    }

    internal fun tryGoGrabRoom(mediaType: Int, roomID: Int, tagID: Int, inviteType: Int) {
        if (mSkrAudioPermission != null) {
            mSkrAudioPermission!!.ensurePermission({
                if (mediaType == EMsgRoomMediaType.EMR_VIDEO.value) {
                    // 视频房间
                    mSkrCameraPermission.ensurePermission({
                        mRealNameVerifyUtils.checkJoinVideoPermission {
                            // 进入视频预览
                            ARouter.getInstance()
                                    .build(RouterConstants.ACTIVITY_BEAUTY_PREVIEW)
                                    .withInt("mFrom", FROM_FRIEND_RECOMMEND)
                                    .withInt("mRoomId", roomID)
                                    .withInt("mInviteType", inviteType)
                                    .navigation()
                        }
                    }, true)
                } else {
                    mRealNameVerifyUtils.checkJoinAudioPermission(tagID) {
                        val iRankingModeService = ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation() as IPlaywaysModeService
                        iRankingModeService?.tryGoGrabRoom(roomID, inviteType)
                    }
                }
            }, true)
        }
    }


    /**
     * @param mediaType
     * @param ownerId
     * @param roomID
     * @param inviteType 1是弹窗，2是剪切板的邀请
     */
    internal fun tryGoDoubleRoom(mediaType: Int, ownerId: Int, roomID: Int, inviteType: Int) {
        mSkrAudioPermission!!.ensurePermission({
            mRealNameVerifyUtils.checkJoinDoubleRoomPermission {
                val map = HashMap<String, Any>()
                map["peerUserID"] = ownerId
                map["roomID"] = roomID
                val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
                ApiMethods.subscribe(mMainPageSlideApi.enterInvitedDoubleFromCreateRoom(body), object : ApiObserver<ApiResult>() {
                    override fun process(result: ApiResult) {
                        if (result.errno == 0) {
                            val iRankingModeService = ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation() as IPlaywaysModeService
                            iRankingModeService.jumpToDoubleRoomFromDoubleRoomInvite(result.data)
                        } else {
                            U.getToastUtil().showShort(result.errmsg)
                        }
                    }

                    override fun onError(e: Throwable) {
                        MyLog.e(TAG, e)
                    }
                }, this@NotifyCorePresenter)
            }
        }, true)

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: FollowNotifyEvent) {
        val floatWindowData = FloatWindowData(FloatWindowData.Type.FOLLOW)
        floatWindowData.userInfoModel = event.mUserInfoModel
        mFloatWindowDataFloatWindowObjectPlayControlTemplate!!.add(floatWindowData, true)

        WeakRedDotManager.getInstance().updateWeakRedRot(WeakRedDotManager.MESSAGE_FOLLOW_RED_ROD_TYPE, 2, true)
        StatisticsAdapter.recordCountEvent("social", "getfollow", null)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: SysWarnNotifyEvent) {
        val notifyDialogView = NotifyDialogView(U.app(), event.title, event.content)
        var activity = U.getActivityUtils().topActivity
        if (activity is SchemeSdkActivity) {
            activity = U.getActivityUtils().homeActivity
        }
        mSysWarnDialogPlus = DialogPlus.newDialog(activity!!)
                .setContentHolder(ViewHolder(notifyDialogView))
                .setGravity(Gravity.CENTER)
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_80)
                .setExpanded(false)
                .create()
        EventBus.getDefault().post(ShowDialogInHomeEvent(mSysWarnDialogPlus, 2))
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: GrabInviteNotifyEvent) {
        // TODO: 2019/5/16 区分前台后台
        if (U.getActivityUtils().isAppForeground) {
            val floatWindowData = FloatWindowData(FloatWindowData.Type.GRABINVITE)
            floatWindowData.userInfoModel = event.mUserInfoModel
            floatWindowData.roomID = event.roomID
            floatWindowData.tagID = event.tagID
            floatWindowData.mediaType = event.mediaType
            mFloatWindowDataFloatWindowObjectPlayControlTemplate!!.add(floatWindowData, true)
        } else {
            // 展示一个通知
            mINotifyView.showNotify(event)
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: ActivityUtils.ForeOrBackgroundChange) {
        mFloatWindowDataFloatWindowObjectPlayControlTemplate!!.endCurrent(null)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: CRRefuseInviteNotifyEvent) {
        val remark = UserInfoManager.getInstance().getRemarkName(event.mUserInfoModel.userId, event.mUserInfoModel.nickname)
        U.getToastUtil().showShort("" + remark + event.refuseMsg)
    }

    /**
     * 5星全服通知
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: EStandFullStarEvent) {
        val floatWindowData = FloatWindowData(FloatWindowData.Type.STAND_FULL_STAR)
        floatWindowData.extra = event.pb.content
        mFloatWindowDataFloatWindowObjectPlayControlTemplate?.add(floatWindowData, true)

    }

    internal fun showGrabInviteFloatWindow(floatWindowData: FloatWindowData) {
        val userInfoModel = floatWindowData.userInfoModel

        mUiHandler.removeMessages(MSG_DISMISS_INVITE_FLOAT_WINDOW)
        mUiHandler.sendEmptyMessageDelayed(MSG_DISMISS_INVITE_FLOAT_WINDOW, 5000)
        val grabInviteNotifyView = GrabInviteNotifyView(U.app())
        grabInviteNotifyView.bindData(userInfoModel)
        grabInviteNotifyView.setListener(object : GrabInviteNotifyView.Listener {
            override fun onIgnore() {
                mUiHandler.removeMessages(MSG_DISMISS_INVITE_FLOAT_WINDOW)
                FloatWindow.destroy(TAG_INVITE_FOALT_WINDOW)
            }

            override fun onAgree() {
                tryGoGrabRoom(floatWindowData.mediaType, floatWindowData.roomID, floatWindowData.tagID, 1)
                mUiHandler.removeMessages(MSG_DISMISS_INVITE_FLOAT_WINDOW)
                FloatWindow.destroy(TAG_INVITE_FOALT_WINDOW)
            }
        })
        FloatWindow.with(U.app())
                .setView(grabInviteNotifyView)
                .setMoveType(MoveType.canRemove)
                .setWidth(Screen.width, 1f)                               //设置控件宽高
                .setHeight(Screen.height, 0.2f)
                .setViewStateListener(object : ViewStateListenerAdapter() {
                    override fun onDismiss(dismissReason: Int) {
                        mFloatWindowDataFloatWindowObjectPlayControlTemplate!!.endCurrent(floatWindowData)
                    }

                    override fun onPositionUpdate(x: Int, y: Int) {
                        super.onPositionUpdate(x, y)
                        mUiHandler.removeMessages(MSG_DISMISS_INVITE_FLOAT_WINDOW)
                        mUiHandler.sendEmptyMessageDelayed(MSG_DISMISS_INVITE_FLOAT_WINDOW, 5000)
                    }
                })
                .setDesktopShow(false)                        //桌面显示
                .setCancelIfExist(false)
                .setReqPermissionIfNeed(false)
                .setTag(TAG_INVITE_FOALT_WINDOW)
                .build()
    }

    internal fun showDoubleInviteFloatWindow(floatWindowData: FloatWindowData) {
        val userInfoModel = floatWindowData.userInfoModel
        mUiHandler.removeMessages(MSG_DISMISS_DOUBLE_INVITE_FOALT_WINDOW)
        mUiHandler.sendEmptyMessageDelayed(MSG_DISMISS_DOUBLE_INVITE_FOALT_WINDOW, 5000)
        val doubleInviteNotifyView = DoubleInviteNotifyView(U.app())
        doubleInviteNotifyView.bindData(userInfoModel, floatWindowData.extra)
        doubleInviteNotifyView.setListener {
            mUiHandler.removeMessages(MSG_DISMISS_DOUBLE_INVITE_FOALT_WINDOW)
            FloatWindow.destroy(TAG_DOUBLE_INVITE_FOALT_WINDOW)
            mSkrAudioPermission!!.ensurePermission({
                mRealNameVerifyUtils.checkJoinDoubleRoomPermission {
                    val map = HashMap<String, Any>()
                    map["peerUserID"] = userInfoModel!!.userId
                    val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
                    ApiMethods.subscribe(mMainPageSlideApi.enterInvitedDoubleRoom(body), object : ApiObserver<ApiResult>() {
                        override fun process(result: ApiResult) {
                            if (result.errno == 0) {
                                val iRankingModeService = ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation() as IPlaywaysModeService
                                iRankingModeService.jumpToDoubleRoom(result.data)
                            } else {
                                U.getToastUtil().showShort(result.errmsg)
                            }
                        }

                        override fun onError(e: Throwable) {
                            MyLog.e(TAG, e)
                        }
                    }, this@NotifyCorePresenter)
                }
            }, true)
        }

        FloatWindow.with(U.app())
                .setView(doubleInviteNotifyView)
                .setMoveType(MoveType.canRemove)
                .setWidth(Screen.width, 1f)                               //设置控件宽高
                .setHeight(Screen.height, 0.2f)
                .setViewStateListener(object : ViewStateListenerAdapter() {
                    override fun onDismiss(dismissReason: Int) {
                        mFloatWindowDataFloatWindowObjectPlayControlTemplate!!.endCurrent(floatWindowData)
                        if (dismissReason != 0) {
                            val map = HashMap<String, Any>()
                            map["peerUserID"] = userInfoModel!!.userId
                            if (dismissReason == 1) {
                                map["refuseType"] = 1 //主动拒绝
                            } else {
                                map["refuseType"] = 2
                            }
                            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
                            ApiMethods.subscribe(mMainPageSlideApi.refuseInvitedDoubleRoom(body), object : ApiObserver<ApiResult>() {
                                override fun process(result: ApiResult) {
                                    if (result.errno == 0) {
                                        MyLog.w(TAG, "process result=$result")
                                    } else {
                                        MyLog.w(TAG, "process result=$result")
                                    }
                                }

                                override fun onError(e: Throwable) {
                                    MyLog.e(TAG, e)
                                }
                            }, this@NotifyCorePresenter)
                        }
                    }

                    override fun onPositionUpdate(x: Int, y: Int) {
                        super.onPositionUpdate(x, y)
                        mUiHandler.removeMessages(MSG_DISMISS_DOUBLE_INVITE_FOALT_WINDOW)
                        mUiHandler.sendEmptyMessageDelayed(MSG_DISMISS_DOUBLE_INVITE_FOALT_WINDOW, 5000)
                    }
                })
                .setDesktopShow(false)                        //桌面显示
                .setCancelIfExist(false)
                .setReqPermissionIfNeed(false)
                .setTag(TAG_DOUBLE_INVITE_FOALT_WINDOW)
                .build()
    }


    internal fun showDoubleInviteFromRoomFloatWindow(floatWindowData: FloatWindowData) {
        val userInfoModel = floatWindowData.userInfoModel

        mUiHandler.removeMessages(MSG_DISMISS_DOUBLE_ROOM_INVITE_FOALT_WINDOW)
        mUiHandler.sendEmptyMessageDelayed(MSG_DISMISS_DOUBLE_ROOM_INVITE_FOALT_WINDOW, 5000)
        val doubleInviteNotifyView = DoubleInviteNotifyView(U.app())
        doubleInviteNotifyView.bindData(userInfoModel, floatWindowData.extra)
        doubleInviteNotifyView.setListener {
            mUiHandler.removeMessages(MSG_DISMISS_DOUBLE_ROOM_INVITE_FOALT_WINDOW)
            FloatWindow.destroy(TAG_DOUBLE_ROOM_INVITE_FOALT_WINDOW)
            tryGoDoubleRoom(floatWindowData.mediaType, userInfoModel!!.userId, floatWindowData.roomID, 1)
        }

        FloatWindow.with(U.app())
                .setView(doubleInviteNotifyView)
                .setMoveType(MoveType.canRemove)
                .setWidth(Screen.width, 1f)                               //设置控件宽高
                .setHeight(Screen.height, 0.2f)
                .setViewStateListener(object : ViewStateListenerAdapter() {
                    override fun onDismiss(dismissReason: Int) {
                        mFloatWindowDataFloatWindowObjectPlayControlTemplate!!.endCurrent(floatWindowData)
                    }

                    override fun onPositionUpdate(x: Int, y: Int) {
                        super.onPositionUpdate(x, y)
                        mUiHandler.removeMessages(MSG_DISMISS_DOUBLE_ROOM_INVITE_FOALT_WINDOW)
                        mUiHandler.sendEmptyMessageDelayed(MSG_DISMISS_DOUBLE_ROOM_INVITE_FOALT_WINDOW, 5000)
                    }
                })
                .setDesktopShow(false)                        //桌面显示
                .setCancelIfExist(false)
                .setReqPermissionIfNeed(false)
                .setTag(TAG_DOUBLE_ROOM_INVITE_FOALT_WINDOW)
                .build()
    }

    internal fun showStandFullStarFloatWindow(floatWindowData: FloatWindowData) {
        mUiHandler.removeMessages(MSG_DISMISS_STAND_FULL_STAR)
        mUiHandler.sendEmptyMessageDelayed(MSG_DISMISS_STAND_FULL_STAR, 5000)

        val view = GrabFullStarNotifyView(U.app())
        view.bindData(floatWindowData.extra)
        FloatWindow.with(U.app())
                .setView(view)
                .setMoveType(MoveType.canRemove)
                .setWidth(Screen.width, 1f)                               //设置控件宽高
                .setHeight(Screen.height, 0.1f)
                .setViewStateListener(object : ViewStateListenerAdapter() {
                    override fun onDismiss(dismissReason: Int) {
                        mFloatWindowDataFloatWindowObjectPlayControlTemplate?.endCurrent(floatWindowData)
                    }

                    override fun onPositionUpdate(x: Int, y: Int) {
                        super.onPositionUpdate(x, y)
                        mUiHandler.removeMessages(MSG_DISMISS_STAND_FULL_STAR)
                        mUiHandler.sendEmptyMessageDelayed(MSG_DISMISS_STAND_FULL_STAR, 5000)
                    }
                })
                .setDesktopShow(false)                        //桌面显示
                .setCancelIfExist(false)
                .setReqPermissionIfNeed(false)
                .setTag(TAG_STAND_FULL_STAR_FOALT_WINDOW)
                .build()
    }

    internal fun showFollowFloatWindow(floatWindowData: FloatWindowData) {
        val userInfoModel = floatWindowData.userInfoModel
        mUiHandler.removeMessages(MSG_DISMISS_RELATION_FLOAT_WINDOW)
        mUiHandler.sendEmptyMessageDelayed(MSG_DISMISS_RELATION_FLOAT_WINDOW, 5000)
        val relationNotifyView = FollowNotifyView(U.app())
        relationNotifyView.bindData(userInfoModel)
        relationNotifyView.setListener {
            // 不消失
            //                mUiHandler.removeMessages(MSG_DISMISS_RELATION_FLOAT_WINDOW);
            //                FloatWindow.destroy(TAG_RELATION_FOALT_WINDOW);
        }
        FloatWindow.with(U.app())
                .setView(relationNotifyView)
                .setMoveType(MoveType.canRemove)
                .setWidth(Screen.width, 1f)                               //设置控件宽高
                .setHeight(Screen.height, 0.2f)
                .setViewStateListener(object : ViewStateListenerAdapter() {
                    override fun onDismiss(dismissReason: Int) {
                        mFloatWindowDataFloatWindowObjectPlayControlTemplate?.endCurrent(floatWindowData)
                    }

                    override fun onPositionUpdate(x: Int, y: Int) {
                        super.onPositionUpdate(x, y)
                        mUiHandler.removeMessages(MSG_DISMISS_RELATION_FLOAT_WINDOW)
                        mUiHandler.sendEmptyMessageDelayed(MSG_DISMISS_RELATION_FLOAT_WINDOW, 5000)
                    }
                })
                .setDesktopShow(false)                        //桌面显示
                .setCancelIfExist(false)
                .setReqPermissionIfNeed(false)
                .setTag(TAG_RELATION_FOALT_WINDOW)
                .build()
    }


    class FloatWindowData(val mType: Type) {
        var userInfoModel: UserInfoModel? = null
        var roomID: Int = 0
        var tagID: Int = 0
        var mediaType: Int = 0
        var extra: String? = null

        override fun toString(): String {
            return "FloatWindowData{" +
                    "mUserInfoModel=" + userInfoModel +
                    ", mType=" + mType +
                    ", mRoomID=" + roomID +
                    '}'.toString()
        }

        /**
         * DOUBLE_GRAB_INVITE是一场到底里的邀请
         * DOUBLE_ROOM_INVITE是唱聊房里的邀请
         * STAND_FULL_STAR 歌单战5星好评
         */
        enum class Type {
            FOLLOW, GRABINVITE, DOUBLE_GRAB_INVITE, DOUBLE_ROOM_INVITE,STAND_FULL_STAR
        }
    }

    companion object {

        internal val TAG_INVITE_FOALT_WINDOW = "TAG_INVITE_FOALT_WINDOW"
        internal val TAG_RELATION_FOALT_WINDOW = "TAG_RELATION_FOALT_WINDOW"
        internal val TAG_DOUBLE_INVITE_FOALT_WINDOW = "TAG_DOUBLE_INVITE_FOALT_WINDOW"
        internal val TAG_DOUBLE_ROOM_INVITE_FOALT_WINDOW = "TAG_DOUBLE_ROOM_INVITE_FOALT_WINDOW"
        internal val TAG_STAND_FULL_STAR_FOALT_WINDOW = "TAG_STAND_FULL_STAR_FOALT_WINDOW"

        internal val MSG_DISMISS_INVITE_FLOAT_WINDOW = 2
        internal val MSG_DISMISS_RELATION_FLOAT_WINDOW = 3
        internal val MSG_DISMISS_DOUBLE_INVITE_FOALT_WINDOW = 4      // 普通邀请
        internal val MSG_DISMISS_DOUBLE_ROOM_INVITE_FOALT_WINDOW = 5 // 邀请好友，在双人房中的邀请
        internal val MSG_DISMISS_STAND_FULL_STAR = 6
    }
}
