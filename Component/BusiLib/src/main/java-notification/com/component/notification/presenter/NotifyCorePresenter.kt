package com.component.notification.presenter

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Message
import android.view.Gravity
import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.common.anim.ObjectPlayControlTemplate
import com.common.core.global.event.ShowDialogInHomeEvent
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.permission.SkrAudioPermission
import com.common.core.permission.SkrCameraPermission
import com.common.core.scheme.SchemeSdkActivity
import com.common.core.scheme.event.*
import com.common.core.userinfo.ResultCallback
import com.common.core.userinfo.UserInfoManager
import com.common.core.userinfo.model.ClubMemberInfo
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
import com.component.busilib.R
import com.component.busilib.beauty.FROM_FRIEND_RECOMMEND
import com.component.busilib.manager.WeakRedDotManager
import com.component.busilib.verify.SkrVerifyUtils
import com.component.dialog.ConfirmDialog
import com.component.dialog.NotifyDialogView
import com.component.dialog.RedPacketRelayDialogView
import com.component.notification.*
import com.component.notification.api.NotifyReqApi
import com.dialog.view.TipsDialogView
import com.module.RouterConstants
import com.module.playways.IPlaywaysModeService
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import com.zq.live.proto.Common.EMsgRoomMediaType
import com.zq.live.proto.Notification.*
import com.zq.live.proto.broadcast.PresentGift
import io.reactivex.Observable
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import java.util.concurrent.TimeUnit

class NotifyCorePresenter() : RxLifeCyclePresenter() {

    private val NOTIFY_CHANNEL_ID = "invite_notify"

    internal var mNManager: NotificationManager? = null

    internal var mBeFriendDialog: DialogPlus? = null

    internal var mSysWarnDialogPlus: DialogPlus? = null

    internal var mNotifyReqApi = ApiManager.getInstance().createService(NotifyReqApi::class.java)

    internal var mSkrAudioPermission: SkrAudioPermission? = SkrAudioPermission()

    internal var mSkrCameraPermission = SkrCameraPermission()

    internal var mRealNameVerifyUtils = SkrVerifyUtils()

    internal var mUiHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                MSG_DISMISS_INVITE_FLOAT_WINDOW -> FloatWindow.destroy(TAG_INVITE_FLOAT_WINDOW)
                MSG_DISMISS_RELATION_FLOAT_WINDOW -> FloatWindow.destroy(TAG_RELATION_FLOAT_WINDOW)
                MSG_DISMISS_DOUBLE_INVITE_FLOAT_WINDOW -> FloatWindow.destroy(TAG_DOUBLE_INVITE_FLOAT_WINDOW, 2)
                MSG_DISMISS_DOUBLE_ROOM_INVITE_FLOAT_WINDOW -> FloatWindow.destroy(TAG_DOUBLE_ROOM_INVITE_FLOAT_WINDOW, 2)
                MSG_DISMISS_STAND_FULL_STAR -> FloatWindow.destroy(TAG_STAND_FULL_STAR_FLOAT_WINDOW, 2)
                MSG_DISMISS_MIC_ROOM_INVITE_FLOAT_WINDOW -> FloatWindow.destroy(TAG_MIC_ROOM_INVITE_FLOAT_WINDOW, 2)
                MSG_DISMISS_GIFT_MALL_FLOAT_WINDOW -> FloatWindow.destroy(TAG_GIFT_MALL_FLOAT_WINDOW, 2)
                MSG_DISMISS_RELAY_INVITE_FLOAT_WINDOW -> FloatWindow.destroy(TAG_RELAY_INVITE_FLOAT_WINDOW, 2)
                MSG_DISMISS_RONG_MSG_NOTIFY_FLOAT_WINDOW -> FloatWindow.destroy(TAG_RONG_MSG_NOTIFY_FLOAT_WINDOW, 2)
                MSG_DISMISS_BIG_GIFT_NOTIFY_FLOAT_WINDOW -> FloatWindow.destroy(TAG_BIG_GIFT_NOTIFY_FLOAT_WINDOW, 2)
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
            when {
                floatWindowData.mType == FloatWindowData.Type.FOLLOW -> showFollowFloatWindow(floatWindowData)
                floatWindowData.mType == FloatWindowData.Type.GRAB_INVITE -> showGrabInviteFloatWindow(floatWindowData)
                floatWindowData.mType == FloatWindowData.Type.DOUBLE_GRAB_INVITE -> showDoubleInviteFloatWindow(floatWindowData)
                floatWindowData.mType == FloatWindowData.Type.DOUBLE_ROOM_INVITE -> showDoubleInviteFromRoomFloatWindow(floatWindowData)
                floatWindowData.mType == FloatWindowData.Type.STAND_FULL_STAR -> showStandFullStarFloatWindow(floatWindowData)
                floatWindowData.mType == FloatWindowData.Type.MIC_INVITE -> showMicInviteFromRoomFloatWindow(floatWindowData)
                floatWindowData.mType == FloatWindowData.Type.MALL_GIFT -> showGiftMallFloatWindow(floatWindowData)
                floatWindowData.mType == FloatWindowData.Type.PARTY_INVITE -> showPartyInviteFromRoomFloatWindow(floatWindowData)
                floatWindowData.mType == FloatWindowData.Type.RELAY_INVITE -> showRelayInviteFromRoomFloatWindow(floatWindowData)
                floatWindowData.mType == FloatWindowData.Type.RONG_MSG_NOTIFY -> showRongMsgNotifyFloatWindow(floatWindowData)
                floatWindowData.mType == FloatWindowData.Type.BIG_GIFT_NOTIFY -> showBigGiftNotifyFloatWindow(floatWindowData)
            }
        }

        override fun onEnd(floatWindowData: FloatWindowData?) {

        }
    }

    init {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        mNManager = U.app().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel(NOTIFY_CHANNEL_ID, "notify", NotificationManager.IMPORTANCE_LOW)
            mNManager?.createNotificationChannel(mChannel)
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

    @Subscribe(threadMode = ThreadMode.MAIN, priority = Int.MAX_VALUE)
    fun onEvent(event: ClubInfoChangeMsg) {
        // 我自己家族信息的改变
        MyUserInfoManager.myUserInfo?.clubInfo = ClubMemberInfo.parseFromPB(event.clubInfo)
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
    fun onEvent(event: MicInviteFromSchemeEvent) {
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
                        val confirmDialog = ConfirmDialog(activity, userInfoModel, ConfirmDialog.TYPE_MIC_INVITE_CONFIRM)
                        confirmDialog.setListener {
                            Observable.timer(500, TimeUnit.MILLISECONDS)
                                    .compose(this@NotifyCorePresenter.bindUntilEvent(PresenterEvent.DESTROY))
                                    .subscribe { tryGoMicRoom(event.ownerId, event.roomId) }
                        }
                        confirmDialog.show()
                    }
                    return false
                }
            })
        } else {
            // 不需要直接进
            tryGoMicRoom(event.ownerId, event.roomId)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PartyInviteFromSchemeEvent) {
        // 派对房间邀请口令
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
                        val confirmDialog = ConfirmDialog(activity, userInfoModel, ConfirmDialog.TYPE_PARTY_INVITE_CONFIRM)
                        confirmDialog.setListener {
                            Observable.timer(500, TimeUnit.MILLISECONDS)
                                    .compose(this@NotifyCorePresenter.bindUntilEvent(PresenterEvent.DESTROY))
                                    .subscribe { tryGoPartyRoom(event.roomId) }
                        }
                        confirmDialog.show()
                    }
                    return false
                }
            })
        } else {
            // 不需要直接进
            tryGoPartyRoom(event.roomId)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: RelayInviteFromSchemeEvent) {
        // 派对房间邀请口令
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
                        val confirmDialog = ConfirmDialog(activity, userInfoModel, ConfirmDialog.TYPE_RELAY_INVITE_CONFIRM)
                        confirmDialog.setListener {
                            Observable.timer(500, TimeUnit.MILLISECONDS)
                                    .compose(this@NotifyCorePresenter.bindUntilEvent(PresenterEvent.DESTROY))
                                    .subscribe { tryToRelayRoom(event.ownerId, event.roomId, 0) }
                        }
                        confirmDialog.show()
                    }
                    return false
                }
            })
        } else {
            // 不需要直接进
            tryToRelayRoom(event.ownerId, event.roomId, 0)
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
    fun onEvent(event: PresentGift) {
        val floatWindowData = FloatWindowData(FloatWindowData.Type.BIG_GIFT_NOTIFY)
        val obj = JSONObject();
        obj.put("content", event.content)
        obj.put("couldEnter", event.couldEnter)
        obj.put("sourceURL", event.sourceURL)
        obj.put("enterScheme", event.enterScheme)
        floatWindowData.extra = obj.toJSONString()
        mFloatWindowDataFloatWindowObjectPlayControlTemplate!!.add(floatWindowData, true)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: MicRoomInviteEvent) {
        val floatWindowData = FloatWindowData(FloatWindowData.Type.MIC_INVITE)
        floatWindowData.userInfoModel = event.mUserInfoModel
        floatWindowData.roomID = event.inviteMicMsg.roomID
        mFloatWindowDataFloatWindowObjectPlayControlTemplate!!.add(floatWindowData, true)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: InvitePartyMsg) {
        val floatWindowData = FloatWindowData(FloatWindowData.Type.PARTY_INVITE)
        floatWindowData.userInfoModel = UserInfoModel.parseFromPB(event.user)
        floatWindowData.roomID = event.roomID
        mFloatWindowDataFloatWindowObjectPlayControlTemplate!!.add(floatWindowData, true)
    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun onEvent(event: PartyRoomInviteEvent) {
//        val floatWindowData = FloatWindowData(FloatWindowData.Type.PARTY_INVITE)
//        floatWindowData.userInfoModel = event.userInfoModel
//        floatWindowData.roomID = event.roomID
//        mFloatWindowDataFloatWindowObjectPlayControlTemplate!!.add(floatWindowData, true)
//    }

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
    fun onEvent(event: SpFollowNewPostMsg) {
        WeakRedDotManager.getInstance().updateWeakRedRot(WeakRedDotManager.MESSAGE_SP_FOLLOW, 2, true)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: SpFollowUpdateAlbumMsg) {
        WeakRedDotManager.getInstance().updateWeakRedRot(WeakRedDotManager.MESSAGE_SP_FOLLOW, 2, true)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: GiftReceivesMsg) {
        WeakRedDotManager.getInstance().updateWeakRedRot(WeakRedDotManager.MESSAGE_GIFT_TYPE, 2, true)
        if (event.giftSource == EGiftSource.EGS_MallGift) {
            //特效赠送的礼物
            val floatWindowData = FloatWindowData(FloatWindowData.Type.MALL_GIFT)
            floatWindowData.extra = event.msgDesc
            floatWindowData.userInfoModel = UserInfoModel.parseFromPB(event.senderInfo)
            mFloatWindowDataFloatWindowObjectPlayControlTemplate!!.add(floatWindowData, true)
        }
    }


//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun onEvent(event: FeedLikeNotifyEvent) {
//        WeakRedDotManager.getInstance().updateWeakRedRot(WeakRedDotManager.MESSAGE_FEED_LIKE_TYPE, 2, true)
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun onEvent(event: FeedCommentLikeNotifyEvent) {
//        WeakRedDotManager.getInstance().updateWeakRedRot(WeakRedDotManager.MESSAGE_FEED_COMMENT_LIKE_TYPE, 2, true)
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun onEvent(event: FeedCommentAddNotifyEvent) {
//        WeakRedDotManager.getInstance().updateWeakRedRot(WeakRedDotManager.MESSAGE_FEED_COMMENT_ADD_TYPE, 2, true)
//    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PostsLikeEvent) {
        WeakRedDotManager.getInstance().updateWeakRedRot(WeakRedDotManager.MESSAGE_POSTS_COMMENT_LIKE_TYPE, 2, true)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PostsCommentLikeMsg) {
        WeakRedDotManager.getInstance().updateWeakRedRot(WeakRedDotManager.MESSAGE_POSTS_COMMENT_LIKE_TYPE, 2, true)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PostsCommentAddEvent) {
        WeakRedDotManager.getInstance().updateWeakRedRot(WeakRedDotManager.MESSAGE_POSTS_COMMENT_LIKE_TYPE, 2, true)
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
                ApiMethods.subscribe(mNotifyReqApi.enterInvitedDoubleFromCreateRoom(body), object : ApiObserver<ApiResult>() {
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

    internal fun tryGoMicRoom(ownerId: Int, roomID: Int) {
        mSkrAudioPermission!!.ensurePermission({
            val iRankingModeService = ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation() as IPlaywaysModeService
            iRankingModeService.jumpMicRoom(roomID)
        }, true)
    }

    internal fun tryGoPartyRoom(roomID: Int) {
        mSkrAudioPermission!!.ensurePermission({
            val iRankingModeService = ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation() as IPlaywaysModeService
            iRankingModeService.tryGoPartyRoom(roomID, 2, 0)
        }, true)
    }

    internal fun tryToRelayRoom(ownerId: Int, roomID: Int, inviteType: Int, ts: Long = 0) {
        mSkrAudioPermission!!.ensurePermission({
            val iRankingModeService = ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation() as IPlaywaysModeService
            iRankingModeService.acceptRelayRoomInvite(ownerId, roomID, ts, inviteType)
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
            val floatWindowData = FloatWindowData(FloatWindowData.Type.GRAB_INVITE)
            floatWindowData.userInfoModel = event.mUserInfoModel
            floatWindowData.roomID = event.roomID
            floatWindowData.tagID = event.tagID
            floatWindowData.mediaType = event.mediaType
            mFloatWindowDataFloatWindowObjectPlayControlTemplate!!.add(floatWindowData, true)
        } else {
            // 展示一个通知
            val it = Intent(U.app(), SchemeSdkActivity::class.java)
            it.putExtra("uri", String.format("inframeskr://room/grabjoin?owner=%d&gameId=%d&ask=1", event.mUserInfoModel.userId, event.roomID))
            val pit = PendingIntent.getActivity(U.app(), 0, it, PendingIntent.FLAG_UPDATE_CURRENT)
            //设置图片,通知标题,发送时间,提示方式等属性
            val mBuilder = Notification.Builder(U.app())
            mBuilder.setContentTitle("@" + MyUserInfoManager.nickName)                        //标题
                    .setContentText("你的好友" + event.mUserInfoModel.nicknameRemark + "邀请你玩游戏")      //内容
                    .setWhen(System.currentTimeMillis())           //设置通知时间
                    .setSmallIcon(R.drawable.app_icon)            //设置小图标
                    .setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_VIBRATE)    //设置默认的三色灯与振动器
                    //                .setPriority(Notification.PRIORITY_MAX)      //设置应用的优先级，可以用来修复在小米手机上可能显示在不重要通知中
                    .setAutoCancel(true)                           //设置点击后取消Notification
                    .setContentIntent(pit)                        //设置PendingIntent
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mBuilder.setChannelId(NOTIFY_CHANNEL_ID)
            }
            val notify1 = mBuilder.build()
            mNManager?.notify(1, notify1)
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

    //在外面（抢唱，party，小k房里面）邀请别人一起合唱之后当被邀请的人同意之后邀请人收到这个push
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: CNRelayEnterFromOuterInviteNotifyEvent) {
        val iRankingModeService = ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation() as IPlaywaysModeService
        iRankingModeService.tryToRelayRoomByOuterInvite(event)
    }

    //在匹配界面邀请别人一起红包合唱之后当被邀请的人同意之后邀请人收到这个push
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: CNRelayEnterFromRedpacketNotifyEvent) {
        val iRankingModeService = ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation() as IPlaywaysModeService
        iRankingModeService.tryToRelayRoomByRedPacketInvite(event)
    }

    //无论在哪里邀请（合唱房间，合唱房间外），都在这里展示被拒绝的toast
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: RelayRoomRefuseMsg) {
        val remark = UserInfoManager.getInstance().getRemarkName(event.user.userID, event.user.nickName)
        U.getToastUtil().showShort("" + remark + event.refuseMsg)
    }

    //收到别人的合唱邀请，无论对方是在哪里邀请（合唱房间，合唱房间外），都在这里处理
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: RelayRoomInviteMsg) {
        val iRankingModeService = ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation() as IPlaywaysModeService
        if (iRankingModeService.canShowRelayInvite(event.user.userID, event.inviteType.value)) {
            if (event.inviteType.value == ERInviteType.RIT_REDPACKET_INVITE.value) {
                val redPacketRelayDialogView = RedPacketRelayDialogView(U.getActivityUtils().topActivity, event)
                redPacketRelayDialogView.clickMethod = {
                    if (it) {
                        tryToRelayRoom(event.user.userID
                                ?: 0, event.roomID, event.inviteType.value, event.inviteTimeMs)
                    } else {
                        iRankingModeService.refuseJoinRelayRoom(event.user.userID
                                ?: 0, 1, event.inviteType.value)
                    }
                }

                redPacketRelayDialogView.timeOutMethod = {
                    iRankingModeService.refuseJoinRelayRoom(event.user.userID
                            ?: 0, 2, event.inviteType.value)
                }

                redPacketRelayDialogView.showByDialog(false)
                redPacketRelayDialogView.starCounDown(7)
            } else {
                val floatWindowData = FloatWindowData(FloatWindowData.Type.RELAY_INVITE)
                floatWindowData.userInfoModel = UserInfoModel.parseFromPB(event.user)
                if (event.hasRoomID()) {
                    floatWindowData.roomID = event.roomID
                }
                val json = JSONObject()
                json["inviteTimeMs"] = event.inviteTimeMs
                json["inviteMsg"] = event.inviteMsg
                json["inviteType"] = event.inviteType.value
                floatWindowData.extra = json.toJSONString()
                mFloatWindowDataFloatWindowObjectPlayControlTemplate!!.add(floatWindowData, true)
            }
        }
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
                FloatWindow.destroy(TAG_INVITE_FLOAT_WINDOW)
            }

            override fun onAgree() {
                tryGoGrabRoom(floatWindowData.mediaType, floatWindowData.roomID, floatWindowData.tagID, 1)
                mUiHandler.removeMessages(MSG_DISMISS_INVITE_FLOAT_WINDOW)
                FloatWindow.destroy(TAG_INVITE_FLOAT_WINDOW)
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
                .setTag(TAG_INVITE_FLOAT_WINDOW)
                .build()
    }

    internal fun showDoubleInviteFloatWindow(floatWindowData: FloatWindowData) {
        val userInfoModel = floatWindowData.userInfoModel
        mUiHandler.removeMessages(MSG_DISMISS_DOUBLE_INVITE_FLOAT_WINDOW)
        mUiHandler.sendEmptyMessageDelayed(MSG_DISMISS_DOUBLE_INVITE_FLOAT_WINDOW, 5000)
        val doubleInviteNotifyView = DoubleInviteNotifyView(U.app())
        doubleInviteNotifyView.bindData(userInfoModel, floatWindowData.extra)
        doubleInviteNotifyView.setListener {
            mUiHandler.removeMessages(MSG_DISMISS_DOUBLE_INVITE_FLOAT_WINDOW)
            FloatWindow.destroy(TAG_DOUBLE_INVITE_FLOAT_WINDOW)
            mSkrAudioPermission!!.ensurePermission({
                mRealNameVerifyUtils.checkJoinDoubleRoomPermission {
                    val map = HashMap<String, Any>()
                    map["peerUserID"] = userInfoModel!!.userId
                    val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
                    ApiMethods.subscribe(mNotifyReqApi.enterInvitedDoubleRoom(body), object : ApiObserver<ApiResult>() {
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
                            ApiMethods.subscribe(mNotifyReqApi.refuseInvitedDoubleRoom(body), object : ApiObserver<ApiResult>() {
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
                        mUiHandler.removeMessages(MSG_DISMISS_DOUBLE_INVITE_FLOAT_WINDOW)
                        mUiHandler.sendEmptyMessageDelayed(MSG_DISMISS_DOUBLE_INVITE_FLOAT_WINDOW, 5000)
                    }
                })
                .setDesktopShow(false)                        //桌面显示
                .setCancelIfExist(false)
                .setReqPermissionIfNeed(false)
                .setTag(TAG_DOUBLE_INVITE_FLOAT_WINDOW)
                .build()
    }


    internal fun showDoubleInviteFromRoomFloatWindow(floatWindowData: FloatWindowData) {
        val userInfoModel = floatWindowData.userInfoModel

        mUiHandler.removeMessages(MSG_DISMISS_DOUBLE_ROOM_INVITE_FLOAT_WINDOW)
        mUiHandler.sendEmptyMessageDelayed(MSG_DISMISS_DOUBLE_ROOM_INVITE_FLOAT_WINDOW, 5000)
        val doubleInviteNotifyView = DoubleInviteNotifyView(U.app())
        doubleInviteNotifyView.bindData(userInfoModel, floatWindowData.extra)
        doubleInviteNotifyView.setListener {
            mUiHandler.removeMessages(MSG_DISMISS_DOUBLE_ROOM_INVITE_FLOAT_WINDOW)
            FloatWindow.destroy(TAG_DOUBLE_ROOM_INVITE_FLOAT_WINDOW)
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
                        mUiHandler.removeMessages(MSG_DISMISS_DOUBLE_ROOM_INVITE_FLOAT_WINDOW)
                        mUiHandler.sendEmptyMessageDelayed(MSG_DISMISS_DOUBLE_ROOM_INVITE_FLOAT_WINDOW, 5000)
                    }
                })
                .setDesktopShow(false)                        //桌面显示
                .setCancelIfExist(false)
                .setReqPermissionIfNeed(false)
                .setTag(TAG_DOUBLE_ROOM_INVITE_FLOAT_WINDOW)
                .build()
    }

    internal fun showGiftMallFloatWindow(floatWindowData: FloatWindowData) {
        val userInfoModel = floatWindowData.userInfoModel
        mUiHandler.removeMessages(MSG_DISMISS_GIFT_MALL_FLOAT_WINDOW)
        mUiHandler.sendEmptyMessageDelayed(MSG_DISMISS_GIFT_MALL_FLOAT_WINDOW, 5000)
        val giftMallNotifyView = GiftMallNotifyView(U.app())
        giftMallNotifyView.bindData(floatWindowData.extra, userInfoModel);

        FloatWindow.with(U.app())
                .setView(giftMallNotifyView)
                .setMoveType(MoveType.canRemove)
                .setWidth(Screen.width, 1f)                               //设置控件宽高
                .setHeight(Screen.height, 0.2f)
                .setViewStateListener(object : ViewStateListenerAdapter() {
                    override fun onDismiss(dismissReason: Int) {
                        mFloatWindowDataFloatWindowObjectPlayControlTemplate!!.endCurrent(floatWindowData)
                    }

                    override fun onPositionUpdate(x: Int, y: Int) {
                        super.onPositionUpdate(x, y)
                        mUiHandler.removeMessages(MSG_DISMISS_GIFT_MALL_FLOAT_WINDOW)
                        mUiHandler.sendEmptyMessageDelayed(MSG_DISMISS_GIFT_MALL_FLOAT_WINDOW, 5000)
                    }
                })
                .setDesktopShow(false)                        //桌面显示
                .setCancelIfExist(false)
                .setReqPermissionIfNeed(false)
                .setTag(TAG_GIFT_MALL_FLOAT_WINDOW)
                .build()
    }

    internal fun showMicInviteFromRoomFloatWindow(floatWindowData: FloatWindowData) {
        val userInfoModel = floatWindowData.userInfoModel

        mUiHandler.removeMessages(MSG_DISMISS_MIC_ROOM_INVITE_FLOAT_WINDOW)
        mUiHandler.sendEmptyMessageDelayed(MSG_DISMISS_MIC_ROOM_INVITE_FLOAT_WINDOW, 5000)
        val notifyView = NormalInviteNotifyView(U.app())
        notifyView.bindData(userInfoModel, "邀请你加入小K房")
        notifyView.setListener {
            mUiHandler.removeMessages(MSG_DISMISS_MIC_ROOM_INVITE_FLOAT_WINDOW)
            FloatWindow.destroy(TAG_MIC_ROOM_INVITE_FLOAT_WINDOW)
            tryGoMicRoom(userInfoModel!!.userId, floatWindowData.roomID)
        }

        FloatWindow.with(U.app())
                .setView(notifyView)
                .setMoveType(MoveType.canRemove)
                .setWidth(Screen.width, 1f)                               //设置控件宽高
                .setHeight(Screen.height, 0.2f)
                .setViewStateListener(object : ViewStateListenerAdapter() {
                    override fun onDismiss(dismissReason: Int) {
                        mFloatWindowDataFloatWindowObjectPlayControlTemplate!!.endCurrent(floatWindowData)
                    }

                    override fun onPositionUpdate(x: Int, y: Int) {
                        super.onPositionUpdate(x, y)
                        mUiHandler.removeMessages(MSG_DISMISS_MIC_ROOM_INVITE_FLOAT_WINDOW)
                        mUiHandler.sendEmptyMessageDelayed(MSG_DISMISS_MIC_ROOM_INVITE_FLOAT_WINDOW, 5000)
                    }
                })
                .setDesktopShow(false)                        //桌面显示
                .setCancelIfExist(false)
                .setReqPermissionIfNeed(false)
                .setTag(TAG_MIC_ROOM_INVITE_FLOAT_WINDOW)
                .build()
    }

    internal fun showPartyInviteFromRoomFloatWindow(floatWindowData: FloatWindowData) {
        val userInfoModel = floatWindowData.userInfoModel

        mUiHandler.removeMessages(MSG_DISMISS_PARTY_ROOM_INVITE_FLOAT_WINDOW)
        mUiHandler.sendEmptyMessageDelayed(MSG_DISMISS_PARTY_ROOM_INVITE_FLOAT_WINDOW, 5000)
        val notifyView = NormalInviteNotifyView(U.app())
        notifyView.bindData(userInfoModel, "邀请你加入主题房")
        notifyView.setListener {
            mUiHandler.removeMessages(MSG_DISMISS_PARTY_ROOM_INVITE_FLOAT_WINDOW)
            FloatWindow.destroy(TAG_PARTY_ROOM_INVITE_FLOAT_WINDOW)
            tryGoPartyRoom(floatWindowData.roomID)
        }

        FloatWindow.with(U.app())
                .setView(notifyView)
                .setMoveType(MoveType.canRemove)
                .setWidth(Screen.width, 1f)                               //设置控件宽高
                .setHeight(Screen.height, 0.2f)
                .setViewStateListener(object : ViewStateListenerAdapter() {
                    override fun onDismiss(dismissReason: Int) {
                        mFloatWindowDataFloatWindowObjectPlayControlTemplate!!.endCurrent(floatWindowData)
                    }

                    override fun onPositionUpdate(x: Int, y: Int) {
                        super.onPositionUpdate(x, y)
                        mUiHandler.removeMessages(MSG_DISMISS_PARTY_ROOM_INVITE_FLOAT_WINDOW)
                        mUiHandler.sendEmptyMessageDelayed(MSG_DISMISS_PARTY_ROOM_INVITE_FLOAT_WINDOW, 5000)
                    }
                })
                .setDesktopShow(false)                        //桌面显示
                .setCancelIfExist(false)
                .setReqPermissionIfNeed(false)
                .setTag(TAG_PARTY_ROOM_INVITE_FLOAT_WINDOW)
                .build()
    }

    internal fun showRelayInviteFromRoomFloatWindow(floatWindowData: FloatWindowData) {
        val userInfoModel = floatWindowData.userInfoModel

        mUiHandler.removeMessages(MSG_DISMISS_RELAY_INVITE_FLOAT_WINDOW)
        mUiHandler.sendEmptyMessageDelayed(MSG_DISMISS_RELAY_INVITE_FLOAT_WINDOW, 5000)
        val notifyView = RelayInviteNotifyView(U.app())
        val jsonObject = JSONObject.parseObject(floatWindowData.extra, JSONObject::class.java)
        val inviteTimeMs = jsonObject.getLongValue("inviteTimeMs")
        val inviteMsg = jsonObject.getString("inviteMsg")
        val inviteType = jsonObject.getIntValue("inviteType")
        notifyView.bindData(userInfoModel, inviteMsg)
        notifyView.setListener {
            mUiHandler.removeMessages(MSG_DISMISS_RELAY_INVITE_FLOAT_WINDOW)
            FloatWindow.destroy(TAG_RELAY_INVITE_FLOAT_WINDOW)
            tryToRelayRoom(userInfoModel?.userId
                    ?: 0, floatWindowData.roomID, inviteType, inviteTimeMs)
        }

        FloatWindow.with(U.app())
                .setView(notifyView)
                .setMoveType(MoveType.canRemove)
                .setWidth(Screen.width, 1f)                               //设置控件宽高
                .setHeight(Screen.height, 0.2f)
                .setViewStateListener(object : ViewStateListenerAdapter() {
                    override fun onDismiss(dismissReason: Int) {
                        val iRankingModeService = ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation() as IPlaywaysModeService
                        if (dismissReason == 2) {
                            iRankingModeService.refuseJoinRelayRoom(userInfoModel?.userId
                                    ?: 0, 2, inviteType)
                        } else if (dismissReason == 1) {
                            iRankingModeService.refuseJoinRelayRoom(userInfoModel?.userId
                                    ?: 0, 1, inviteType)
                        }

                        mFloatWindowDataFloatWindowObjectPlayControlTemplate!!.endCurrent(floatWindowData)
                    }

                    override fun onPositionUpdate(x: Int, y: Int) {
                        super.onPositionUpdate(x, y)
                        mUiHandler.removeMessages(MSG_DISMISS_RELAY_INVITE_FLOAT_WINDOW)
                        mUiHandler.sendEmptyMessageDelayed(MSG_DISMISS_RELAY_INVITE_FLOAT_WINDOW, 5000)
                    }
                })
                .setDesktopShow(false)                        //桌面显示
                .setCancelIfExist(false)
                .setReqPermissionIfNeed(false)
                .setTag(TAG_RELAY_INVITE_FLOAT_WINDOW)
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
                .setTag(TAG_STAND_FULL_STAR_FLOAT_WINDOW)
                .build()
    }


    var rongMsgNotifyView: RongMsgNotifyView? = null

    /**
     * IM消息通知
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: RongMsgNotifyEvent) {
        if (event.buddyCacheEntry?.uuid?.toString() == chatingUserId) {
            // 详情页打开的已经是跟A的会话
            return
        }
        if (rongMsgNotifyView?.mUserInfoModel?.userId == event?.buddyCacheEntry?.uuid) {
            // 如果当前正在显示A的对话 刷新A即可
            mUiHandler.removeMessages(MSG_DISMISS_RONG_MSG_NOTIFY_FLOAT_WINDOW)
            mUiHandler.sendEmptyMessageDelayed(MSG_DISMISS_RONG_MSG_NOTIFY_FLOAT_WINDOW, 5000)
            rongMsgNotifyView?.bindData(event.buddyCacheEntry.toUserInfoModel(), event.content.toString())
        } else {
            val floatWindowData = FloatWindowData(FloatWindowData.Type.RONG_MSG_NOTIFY)
            floatWindowData.userInfoModel = event.buddyCacheEntry.toUserInfoModel()
            floatWindowData.extra = event.content.toString()
            // 如果已经有A的对话在队列里，移除A的对话
            mFloatWindowDataFloatWindowObjectPlayControlTemplate?.remove(floatWindowData)
            mFloatWindowDataFloatWindowObjectPlayControlTemplate?.add(floatWindowData, true)
        }
    }

    internal fun showBigGiftNotifyFloatWindow(floatWindowData: FloatWindowData) {
        mUiHandler.removeMessages(MSG_DISMISS_BIG_GIFT_NOTIFY_FLOAT_WINDOW)
        mUiHandler.sendEmptyMessageDelayed(MSG_DISMISS_BIG_GIFT_NOTIFY_FLOAT_WINDOW, 5000)

        val relationNotifyView = BigGiftNotifyView(U.app())
        val obj = JSONObject.parseObject(floatWindowData.extra)
        val content = obj.getString("content")
        val sourceURL = obj.getString("sourceURL")
        val enterScheme = obj.getString("enterScheme")
        val couldEnter = obj.getBoolean("couldEnter")

        relationNotifyView.bindData(enterScheme, content, couldEnter, sourceURL) {
            mUiHandler.removeMessages(MSG_DISMISS_BIG_GIFT_NOTIFY_FLOAT_WINDOW);
            FloatWindow.destroy(TAG_BIG_GIFT_NOTIFY_FLOAT_WINDOW);
        }

        FloatWindow.with(U.app())
                .setView(relationNotifyView)
                .setMoveType(MoveType.canRemove)
                .setWidth(Screen.width, 1f)                               //设置控件宽高
                .setHeight(Screen.height, 0.2f)
                .setViewStateListener(object : ViewStateListenerAdapter() {
                    override fun onDismiss(dismissReason: Int) {
                        mFloatWindowDataFloatWindowObjectPlayControlTemplate!!.endCurrent(floatWindowData)
                    }

                    override fun onPositionUpdate(x: Int, y: Int) {
                        super.onPositionUpdate(x, y)
                        mUiHandler.removeMessages(MSG_DISMISS_BIG_GIFT_NOTIFY_FLOAT_WINDOW)
                        mUiHandler.sendEmptyMessageDelayed(MSG_DISMISS_BIG_GIFT_NOTIFY_FLOAT_WINDOW, 5000)
                    }
                })
                .setDesktopShow(false)                        //桌面显示
                .setCancelIfExist(false)
                .setReqPermissionIfNeed(false)
                .setTag(TAG_BIG_GIFT_NOTIFY_FLOAT_WINDOW)
                .build()
    }

    internal fun showRongMsgNotifyFloatWindow(floatWindowData: FloatWindowData) {
        val userInfoModel = floatWindowData.userInfoModel
        mUiHandler.removeMessages(MSG_DISMISS_RONG_MSG_NOTIFY_FLOAT_WINDOW)
        mUiHandler.sendEmptyMessageDelayed(MSG_DISMISS_RONG_MSG_NOTIFY_FLOAT_WINDOW, 5000)

        rongMsgNotifyView = RongMsgNotifyView(U.app())
        rongMsgNotifyView?.bindData(userInfoModel, floatWindowData.extra)
        rongMsgNotifyView?.listener = {
            mUiHandler.removeMessages(MSG_DISMISS_RONG_MSG_NOTIFY_FLOAT_WINDOW)
            FloatWindow.destroy(TAG_RONG_MSG_NOTIFY_FLOAT_WINDOW)
        }

        FloatWindow.with(U.app())
                .setView(rongMsgNotifyView!!)
                .setMoveType(MoveType.canRemove)
                .setWidth(Screen.width, 1f)                               //设置控件宽高
                .setHeight(Screen.height, 0.2f)
                .setViewStateListener(object : ViewStateListenerAdapter() {
                    override fun onDismiss(dismissReason: Int) {
                        rongMsgNotifyView = null
                        mFloatWindowDataFloatWindowObjectPlayControlTemplate!!.endCurrent(floatWindowData)
                    }

                    override fun onPositionUpdate(x: Int, y: Int) {
                        super.onPositionUpdate(x, y)
                        mUiHandler.removeMessages(MSG_DISMISS_RONG_MSG_NOTIFY_FLOAT_WINDOW)
                        mUiHandler.sendEmptyMessageDelayed(MSG_DISMISS_RONG_MSG_NOTIFY_FLOAT_WINDOW, 5000)
                    }
                })
                .setDesktopShow(false)                        //桌面显示
                .setCancelIfExist(false)
                .setReqPermissionIfNeed(false)
                .setTag(TAG_RONG_MSG_NOTIFY_FLOAT_WINDOW)
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
            //                FloatWindow.destroy(TAG_RELATION_FLOAT_WINDOW);
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
                .setTag(TAG_RELATION_FLOAT_WINDOW)
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

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as FloatWindowData

            // 针对聊天通知是否是同一条做下特殊处理
            if (mType == Type.RONG_MSG_NOTIFY && other.mType == Type.RONG_MSG_NOTIFY) {
                if (userInfoModel?.userId == other.userInfoModel?.userId) {
                    return true
                }
            }

            if (mType != other.mType) return false
            if (userInfoModel != other.userInfoModel) return false
            if (roomID != other.roomID) return false
            if (tagID != other.tagID) return false
            if (mediaType != other.mediaType) return false
            if (extra != other.extra) return false

            return true
        }

        override fun hashCode(): Int {
            if (mType == Type.RONG_MSG_NOTIFY) {
                return userInfoModel?.userId ?: 0
            }

            var result = mType.hashCode()
            result = 31 * result + (userInfoModel?.hashCode() ?: 0)
            result = 31 * result + roomID
            result = 31 * result + tagID
            result = 31 * result + mediaType
            result = 31 * result + (extra?.hashCode() ?: 0)
            return result
        }


        /**
         * DOUBLE_GRAB_INVITE是一场到底里的邀请
         * DOUBLE_ROOM_INVITE是唱聊房里的邀请
         * STAND_FULL_STAR 歌单战5星好评
         */
        enum class Type {
            FOLLOW,
            GRAB_INVITE,
            DOUBLE_GRAB_INVITE,
            DOUBLE_ROOM_INVITE,
            STAND_FULL_STAR,
            MIC_INVITE,
            MALL_GIFT,
            PARTY_INVITE,
            RELAY_INVITE,
            RONG_MSG_NOTIFY,
            BIG_GIFT_NOTIFY,
        }
    }

    companion object {

        var chatingUserId: String? = null

        internal const val TAG_INVITE_FLOAT_WINDOW = "TAG_INVITE_FLOAT_WINDOW"
        internal const val TAG_RELATION_FLOAT_WINDOW = "TAG_RELATION_FLOAT_WINDOW"
        internal const val TAG_DOUBLE_INVITE_FLOAT_WINDOW = "TAG_DOUBLE_INVITE_FLOAT_WINDOW"
        internal const val TAG_DOUBLE_ROOM_INVITE_FLOAT_WINDOW = "TAG_DOUBLE_ROOM_INVITE_FLOAT_WINDOW"
        internal const val TAG_STAND_FULL_STAR_FLOAT_WINDOW = "TAG_STAND_FULL_STAR_FLOAT_WINDOW"
        internal const val TAG_MIC_ROOM_INVITE_FLOAT_WINDOW = "TAG_MIC_ROOM_INVITE_FLOAT_WINDOW"
        internal const val TAG_PARTY_ROOM_INVITE_FLOAT_WINDOW = "TAG_PARTY_ROOM_INVITE_FLOAT_WINDOW"
        internal const val TAG_GIFT_MALL_FLOAT_WINDOW = "TAG_GIFT_MALL_FLOAT_WINDOW"
        internal const val TAG_RELAY_INVITE_FLOAT_WINDOW = "TAG_RELAY_INVITE_FLOAT_WINDOW"
        internal const val TAG_RONG_MSG_NOTIFY_FLOAT_WINDOW = "TAG_RONG_MSG_NOTIFY_FLOAT_WINDOW"
        internal const val TAG_BIG_GIFT_NOTIFY_FLOAT_WINDOW = "TAG_BIG_GIFT_NOTIFY_FLOAT_WINDOW"

        internal const val MSG_DISMISS_INVITE_FLOAT_WINDOW = 2
        internal const val MSG_DISMISS_RELATION_FLOAT_WINDOW = 3
        internal const val MSG_DISMISS_DOUBLE_INVITE_FLOAT_WINDOW = 4      // 普通邀请
        internal const val MSG_DISMISS_DOUBLE_ROOM_INVITE_FLOAT_WINDOW = 5 // 邀请好友，在双人房中的邀请
        internal const val MSG_DISMISS_STAND_FULL_STAR = 6
        internal const val MSG_DISMISS_MIC_ROOM_INVITE_FLOAT_WINDOW = 7
        internal const val MSG_DISMISS_GIFT_MALL_FLOAT_WINDOW = 8
        internal const val MSG_DISMISS_PARTY_ROOM_INVITE_FLOAT_WINDOW = 9
        internal const val MSG_DISMISS_RELAY_INVITE_FLOAT_WINDOW = 10      // 普通邀请
        internal const val MSG_DISMISS_RONG_MSG_NOTIFY_FLOAT_WINDOW = 11 // 融云消息
        internal const val MSG_DISMISS_BIG_GIFT_NOTIFY_FLOAT_WINDOW = 12 // 大礼物通知
    }
}
