package com.common.core.scheme.processor

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.text.TextUtils
import com.alibaba.android.arouter.launcher.ARouter
import com.common.core.R
import com.common.core.account.UserAccountManager
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.permission.SkrAudioPermission
import com.common.core.scheme.SchemeConstants
import com.common.core.scheme.SchemeUtils
import com.common.core.scheme.event.*
import com.common.log.MyLog
import com.common.rxretrofit.ApiManager
import com.common.utils.FragmentUtils
import com.common.utils.U
import com.idlefish.flutterboost.containers.MyBoostFlutterActivity
import com.module.ModuleServiceManager
import com.module.RouterConstants
import com.module.home.IHomeService
import com.module.playways.IPlaywaysModeService
import org.greenrobot.eventbus.EventBus

/**
 * Created by lan on 16/10/26.
 *
 * @module scheme
 * @description Walilive的Uri的逻辑代码
 */
object SkrSchemeProcessor : ISchemeProcessor {

    private val TAG = "InframeProcessor"

    private var mSkrAudioPermission = SkrAudioPermission()

    override fun process(uri: Uri, context: Context, beforeHomeExistJudge: Boolean): ProcessResult {
        //inframeskr://game/match?from=h5
        //inframeskr://person/homepage?from=h5
        //其中scheme为inframeskr, host为game , relativePath为match, query为from=h5.
        val scheme = uri.scheme
        MyLog.w(TAG, "process uri=" + uri!!)
        if (TextUtils.isEmpty(scheme)) {
            return ProcessResult.NotAccepted
        }

        val authority = uri.authority
        MyLog.w(TAG, "process authority=" + authority!!)
        if (TextUtils.isEmpty(authority)) {
            return ProcessResult.NotAccepted
        }
        if (SchemeConstants.SCHEME_INFRAMESKER == scheme) {
            // beforeHomeExistJudge 表示 HomeActivity 不存在时允不允许执行这个scheme
            if (beforeHomeExistJudge) {
                when (authority) {
                    /**
                     * 写入渠道号
                     */
                    SchemeConstants.HOST_CHANNEL -> {
                        processChannel(uri)
                        return ProcessResult.AcceptedAndContinue
                    }
                }
            } else {
                if (!UserAccountManager.hasAccount()) {
                    MyLog.w(TAG, "processWebUrl 没有登录")
                    return ProcessResult.AcceptedAndReturn
                }
                val path = uri.path
                if (TextUtils.isEmpty(path)) {
                    MyLog.w(TAG, "processWalletUrl path is empty")
                    return ProcessResult.AcceptedAndReturn
                }
                when (authority) {
                    SchemeConstants.HOST_HOME -> {
                        processHomeUrl(uri)
                        return ProcessResult.AcceptedAndReturn
                    }
                    SchemeConstants.HOST_SHARE -> {
                        processShareUrl(uri)
                        return ProcessResult.AcceptedAndReturn
                    }
                    SchemeConstants.HOST_WEB -> {
                        processWebUrl(uri)
                        return ProcessResult.AcceptedAndReturn
                    }
                    SchemeConstants.HOST_WALLET -> {
                        processWalletUrl(uri)
                        return ProcessResult.AcceptedAndReturn
                    }
                    SchemeConstants.HOST_ROOM -> {
                        processRoomUrl(uri)
                        return ProcessResult.AcceptedAndReturn
                    }
                    SchemeConstants.HOST_PERSON -> {
                        processPersonUrl(uri)
                        return ProcessResult.AcceptedAndReturn
                    }
                    SchemeConstants.HOST_RELATION -> {
                        processRelationUrl(uri)
                        return ProcessResult.AcceptedAndReturn
                    }
                    SchemeConstants.HOST_FEED -> {
                        processFeedUrl(uri)
                        return ProcessResult.AcceptedAndReturn
                    }
                    "game" -> {
                        processGameUrl(uri)
                        return ProcessResult.AcceptedAndReturn
                    }
                    SchemeConstants.HOST_POSTS -> {
                        processPostsUrl(uri)
                        return ProcessResult.AcceptedAndReturn
                    }
                    SchemeConstants.HOST_USER -> {
                        processUserUrl(uri)
                        return ProcessResult.AcceptedAndReturn
                    }
                    SchemeConstants.HOST_PAYMENT -> {
                        processPaymentUrl(uri)
                        return ProcessResult.AcceptedAndContinue
                    }
                    SchemeConstants.HOST_MALL -> {
                        processMallUrl(uri)
                        return ProcessResult.AcceptedAndContinue
                    }
                    SchemeConstants.HOST_RELAY -> {
                        processRelayUrl(uri)
                        return ProcessResult.AcceptedAndContinue
                    }
                    "flutter" -> {
                        processFlutterUrl(uri, context)
                        return ProcessResult.AcceptedAndContinue
                    }
                    "all" -> {
                        processCommonUrl(uri, context)
                        return ProcessResult.AcceptedAndContinue
                    }
                    "club" -> {
                        processClubUrl(uri, context)
                        return ProcessResult.AcceptedAndContinue
                    }
                    "party" -> {
                        processPartyUrl(uri, context)
                        return ProcessResult.AcceptedAndContinue
                    }
                    "card_relation" -> {
                        processCardRelationUrl(uri, context)
                        return ProcessResult.AcceptedAndContinue
                    }
                }
            }
        } else if ("rong" == scheme) {
            if (beforeHomeExistJudge) {

            } else {
                if (!UserAccountManager.hasAccount()) {
                    MyLog.w(TAG, "processWebUrl 没有登录")
                    return ProcessResult.AcceptedAndReturn
                }
                val path = uri.path
                if (TextUtils.isEmpty(path)) {
                    MyLog.w(TAG, "processWalletUrl path is empty")
                    return ProcessResult.AcceptedAndReturn
                }
                if ("/conversationlist" == path) {
                    EventBus.getDefault().post(JumpHomeFromSchemeEvent(2))
                }
            }
        }
        return ProcessResult.NotAccepted
    }

    private fun processClubUrl(uri: Uri, context: Context) {
        if (uri.path == "/home") {
            ModuleServiceManager.getInstance().clubService?.tryGoClubHomePage(SchemeUtils.getInt(uri, "clubID", 0))
        } else if (uri.path == "/listPage") {
            EventBus.getDefault().post(JumpHomeFromSchemeEvent(0, "club"))
        } else if (uri.path == "/clubUpload") {
            val category = SchemeUtils.getInt(uri, "category", 0)
            val title = SchemeUtils.getString(uri, "title")
            val path = SchemeUtils.getString(uri, "path")
            val familyID = SchemeUtils.getInt(uri, "familyID", 0)
            ARouter.getInstance().build(RouterConstants.ACTIVITY_CLUB_UPLOAD_SONG)
                    .withInt("category", category)
                    .withString("title", title)
                    .withString("path", path)
                    .withInt("familyID", familyID)
                    .navigation()
        }
    }

    private fun processPartyUrl(uri: Uri, context: Context) {
        if (uri.path == "/listPage") {
            ARouter.getInstance().build(RouterConstants.ACTIVITY_PARTY_HOME)
                    .navigation()
        }
    }

    private fun processCardRelationUrl(uri: Uri, context: Context) {
        if (uri.path == "/invite") {
            val goodsID = SchemeUtils.getInt(uri, "goodsID", 0)
            val packetID = SchemeUtils.getString(uri, "packetID")
            EventBus.getDefault().post(InviteRelationCardSchemeEvent(goodsID, packetID))
        }
    }

    private fun processCommonUrl(uri: Uri, context: Context) {
        if (uri.path == "/feedback") {
            if (SchemeUtils.getBoolean(uri, "useFragment", false)) {
                // 举报
                U.getFragmentUtils().addFragment(
                        FragmentUtils.newAddParamsBuilder(U.getActivityUtils().topActivity as FragmentActivity, Class.forName("com.component.report.fragment.QuickFeedbackFragment") as Class<out Fragment>?)
                                .setAddToBackStack(true)
                                .setHasAnimation(true)
                                .addDataBeforeAdd(0, SchemeUtils.getInt(uri, "from", 0))
                                .addDataBeforeAdd(1, SchemeUtils.getInt(uri, "actionType", 0))
                                .addDataBeforeAdd(2, SchemeUtils.getInt(uri, "targetId", 0))
                                .setEnterAnim(R.anim.slide_in_bottom)
                                .setExitAnim(R.anim.slide_out_bottom)
                                .build())
            } else {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDBACK)
                        .withInt("from", SchemeUtils.getInt(uri, "from", 0))
                        .withInt("roomId", SchemeUtils.getInt(uri, "roomId", 0))
                        .withInt("targetId", SchemeUtils.getInt(uri, "targetId", 0))
                        .withInt("actionType", SchemeUtils.getInt(uri, "actionType", 0))
                        .navigation()
            }
        }

    }

    private fun processFlutterUrl(uri: Uri, context: Context) {
        var pageRouterName = uri.path
        if (pageRouterName.startsWith("/")) {
            pageRouterName = pageRouterName.substring(1, pageRouterName.length)
        }
        // 打开一个flutter page 页面
        val params = SchemeUtils.getParams(uri)
        val intent = MyBoostFlutterActivity.withNewEngine().url(pageRouterName!!)
                .params(params)
                .backgroundMode(MyBoostFlutterActivity.BackgroundMode.opaque)
                .build(context)

        if (context is Activity) {
            var requestCode = 0
            if (params.containsKey("requestCode")) {
                requestCode = params["requestCode"] as Int
            }
            context.startActivityForResult(intent, requestCode)
        } else {
            context.startActivity(intent)
        }

    }

    private fun processRelayUrl(uri: Uri) {
        val path = uri.path
        if (SchemeConstants.PATH_HOME == path) {
            try {
                val iRankingModeService = ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation() as IPlaywaysModeService
                iRankingModeService.tryGoRelayHome()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    private fun processMallUrl(uri: Uri) {
        //inframeskr://mall/mall?mall_tag=7
        val path = uri.path
        if (SchemeConstants.PATH_PACKAGE == path) {
            try {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_MALL_PACKAGE)
                        .navigation()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        } else if (SchemeConstants.PATH_MALL == path) {
            try {
                val tag = SchemeUtils.getInt(uri, SchemeConstants.PARAM_MALL_TAG, 0)
                ARouter.getInstance().build(RouterConstants.ACTIVITY_MALL_MALL)
                        .withInt("tag", tag)
                        .navigation()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun processPaymentUrl(uri: Uri) {
        val path = uri.path
        if (SchemeConstants.PATH_RECHARGE == path) {
            try {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_BALANCE)
                        .navigation()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    private fun processChannel(uri: Uri) {
        MyLog.d(TAG, "processChannel uri=$uri")
        val path = uri.path
        if (TextUtils.isEmpty(path)) {
            MyLog.w(TAG, "processGameUrl path is empty")
            return
        }
        var subchannel = path
        if (subchannel!!.startsWith("/")) {
            subchannel = subchannel.substring(1)
        }
        val sb = StringBuilder()
        sb.append(subchannel)
        val opid = uri.getQueryParameter("opid")
        if (TextUtils.isEmpty(opid)) {
        } else {
            sb.append("_").append(opid)
        }
        U.getChannelUtils().subChannel = sb.toString()
    }

    private fun processWalletUrl(uri: Uri) {
        val path = uri.path
        if (SchemeConstants.PATH_WITH_DRAW == path) {
            try {
                val from = SchemeUtils.getString(uri, SchemeConstants.PARAM_FROM)

                ARouter.getInstance().build(RouterConstants.ACTIVITY_WITH_DRAW)
                        .withString("from", from)
                        .navigation()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    private fun processGameUrl(uri: Uri) {
        val path = uri.path

        if (SchemeConstants.PATH_RANK_CHOOSE_SONG == path) {
            val gameMode = SchemeUtils.getString(uri, SchemeConstants.PARAM_GAME_MODE)
            ARouter.getInstance().build(RouterConstants.ACTIVITY_PLAY_WAYS)
                    .withInt("key_game_type", Integer.parseInt(gameMode))
                    .withBoolean("selectSong", true)
                    .navigation()
        } else if (SchemeConstants.PATH_GRAB_MATCH == path) {

        } else {

        }
    }

    private fun processUserUrl(uri: Uri) {
        val path = uri.path
        if (SchemeConstants.PATH_OTHER_USER_DETAIL == path) {
            val userId = SchemeUtils.getInt(uri, SchemeConstants.PARAM_USER_ID, 0)
            val bundle = Bundle()
            bundle.putInt("bundle_user_id", userId)
            ARouter.getInstance().build(RouterConstants.ACTIVITY_OTHER_PERSON)
                    .with(bundle)
                    .navigation()
        } else {

        }
    }

    private fun processPostsUrl(uri: Uri) {
        val path = uri.path
        if (SchemeConstants.PATH_POSTS_DETAIL == path) {
            val postsID = SchemeUtils.getInt(uri, SchemeConstants.PARAM_POSTS_ID, 0)
            ARouter.getInstance().build(RouterConstants.ACTIVITY_POSTS_DETAIL)
                    .withInt("postsID", postsID)
                    .navigation()
        } else {

        }
    }

    private fun processRoomUrl(uri: Uri) {
        val path = uri.path
        if ("/grabjoin" == path) {
            val ownerId = SchemeUtils.getInt(uri, "owner", 0)
            val roomId = SchemeUtils.getInt(uri, "gameId", 0)
            val tagId = SchemeUtils.getInt(uri, "tagId", 0)
            val ask = SchemeUtils.getInt(uri, "ask", 0)
            val mediaType = SchemeUtils.getInt(uri, "mediaType", 0)
            if (ownerId > 0 && roomId > 0) {
                if (ownerId.toLong() == MyUserInfoManager.uid) {
                    MyLog.d(TAG, "processRoomUrl 房主id是自己，可能从口令粘贴板过来的，忽略")
                    return
                }
                val event = GrabInviteFromSchemeEvent()
                event.ask = ask
                event.ownerId = ownerId
                event.roomId = roomId
                event.tagId = tagId
                event.mediaType = mediaType
                EventBus.getDefault().post(event)
            }
        } else if ("/joindouble" == path) {
            val ownerId = SchemeUtils.getInt(uri, "owner", 0)
            val roomId = SchemeUtils.getInt(uri, "gameId", 0)
            val ask = SchemeUtils.getInt(uri, "ask", 0)
            val mediaType = SchemeUtils.getInt(uri, "mediaType", 0)
            if (ownerId > 0 && roomId > 0) {
                if (ownerId.toLong() == MyUserInfoManager.uid) {
                    MyLog.d(TAG, "processRoomUrl 房主id是自己，可能从口令粘贴板过来的，忽略")
                    return
                }
                val event = DoubleInviteFromSchemeEvent()
                event.ask = ask
                event.ownerId = ownerId
                event.roomId = roomId
                event.mediaType = mediaType
                EventBus.getDefault().post(event)
            }
        } else if ("/jump_match" == path) {
            val tagId = SchemeUtils.getInt(uri, "tagId", 0)
            mSkrAudioPermission.ensurePermission(U.getActivityUtils().homeActivity, {
                val iRankingModeService = ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation() as IPlaywaysModeService
                iRankingModeService?.tryGoGrabMatch(tagId)
            }, true)

        } else if ("/jump_create" == path) {
            mSkrAudioPermission.ensurePermission(U.getActivityUtils().homeActivity, {
                val iRankingModeService = ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation() as IPlaywaysModeService
                iRankingModeService?.tryGoCreateRoom()
            }, true)
        } else if ("/chat_page" == path) {
            EventBus.getDefault().post(JumpHomeDoubleChatPageEvent())
        } else if ("/joinmic" == path) {
            val ownerID = SchemeUtils.getInt(uri, "owner", 0)
            if (ownerID.toLong() == MyUserInfoManager.uid) {
                MyLog.d(TAG, "processRoomUrl 房主id是自己，可能从口令粘贴板过来的，忽略")
                return
            }
            val gameId = SchemeUtils.getInt(uri, "gameId", 0)
            val ask = SchemeUtils.getInt(uri, "ask", 0)
            EventBus.getDefault().post(MicInviteFromSchemeEvent(ownerID, gameId, ask))
        } else if ("/race_audience_match" == path) {
            val service = ARouter.getInstance().build(RouterConstants.SERVICE_HOME).navigation() as IHomeService
            service?.goRaceMatchByAudience()
        } else if ("/joinparty" == path) {
            // ownerid 应该是邀请人的id 不是主持人id
            val ownerId = SchemeUtils.getInt(uri, "owner", 0)
            val roomId = SchemeUtils.getInt(uri, "gameId", 0)
            val ask = SchemeUtils.getInt(uri, "ask", 0)
            val mediaType = SchemeUtils.getInt(uri, "mediaType", 0)
            if (roomId > 0) {
                if (ownerId.toLong() == MyUserInfoManager.uid) {
                    MyLog.d(TAG, "processRoomUrl 房主id是自己，可能从口令粘贴板过来的，忽略")
                    return
                }
                val event = PartyInviteFromSchemeEvent()
                event.ask = ask
                event.ownerId = ownerId
                event.roomId = roomId
                event.mediaType = mediaType
                EventBus.getDefault().post(event)
            } else {
                MyLog.i(TAG, "roomId > 0 && ownerId>0 == false")
            }
        } else if ("/joinrelay" == path) {
            val ownerId = SchemeUtils.getInt(uri, "owner", 0)
            val roomId = SchemeUtils.getInt(uri, "gameId", 0)
            val ask = SchemeUtils.getInt(uri, "ask", 0)
            val mediaType = SchemeUtils.getInt(uri, "mediaType", 0)
            if (ownerId > 0 && roomId > 0) {
                if (ownerId.toLong() == MyUserInfoManager.uid) {
                    MyLog.d(TAG, "processRoomUrl 房主id是自己，可能从口令粘贴板过来的，忽略")
                    return
                }
                val event = RelayInviteFromSchemeEvent()
                event.ask = ask
                event.ownerId = ownerId
                event.roomId = roomId
                event.mediaType = mediaType
                EventBus.getDefault().post(event)
            }
        } else if ("/battle_match" == path) {
            val iRankingModeService = ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation() as IPlaywaysModeService
            // 团战的tagid
            iRankingModeService.tryGoBattleMatch(2000000)

        }
    }

    private fun processPersonUrl(uri: Uri) {
        val path = uri.path
        if ("/jump_update_info" == path) {
            ARouter.getInstance().build(RouterConstants.ACTIVITY_EDIT_INFO)
                    .navigation()
        } else if ("/jump_person_center" == path) {
            // 跳到个人中心
            EventBus.getDefault().post(JumpHomeFromSchemeEvent(3))
        } else if ("/chat" == path) {
            // 跳到私信
            val needPop = ModuleServiceManager.getInstance().msgService.startPrivateChat(U.getActivityUtils().topActivity,
                    SchemeUtils.getString(uri, "targetId"),
                    SchemeUtils.getString(uri, "targetName"),
                    true
            )
        }
    }

    private fun processHomeUrl(uri: Uri) {
        val path = uri.path
        if ("/jump" == path) {
            EventBus.getDefault().post(JumpHomeFromSchemeEvent(0))
        } else if ("trywakeup" == path) {
            // 不做任何操作，只是唤启app
        }
    }

    private fun processRelationUrl(uri: Uri) {
        val path = uri.path
        if ("/bothfollow" == path) {
            val inviterId = SchemeUtils.getInt(uri, "inviterId", 0)
            if (inviterId > 0 && inviterId.toLong() != MyUserInfoManager.uid) {
                val event = BothRelationFromSchemeEvent()
                event.useId = inviterId
                EventBus.getDefault().post(event)
            }
        } else {

        }
    }

    private fun processFeedUrl(uri: Uri) {
        val path = uri.path
        if ("/detail" == path) {
            val feedID = SchemeUtils.getInt(uri, "feedId", 0)
            if (feedID > 0) {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDS_DETAIL)
                        .withInt("feed_ID", feedID)
                        //                        .withInt("from", 4)
                        .navigation()
            }
        } else {

        }
    }

    private fun processShareUrl(uri: Uri) {

    }

    private fun processWebUrl(uri: Uri) {
        val path = uri.path
        if (SchemeConstants.PATH_FULL_SCREEN == path) {
            if (TextUtils.isEmpty(SchemeUtils.getString(uri, SchemeConstants.PARAM_URL))) {
                MyLog.w(TAG, "processWebUrl url is empty")
                return
            }
            var url = SchemeUtils.getString(uri, SchemeConstants.PARAM_URL)
            val showShare = SchemeUtils.getInt(uri, SchemeConstants.PARAM_SHOW_SHARE, 0)
            url = url.replace("@@", "?")
            url = url.replace("@", "&")
            url = ApiManager.getInstance().findRealUrlByChannel(url)
            MyLog.i(TAG, "url=$url")

            ARouter.getInstance().build(RouterConstants.ACTIVITY_WEB)
                    .withString("url", url)
                    .withBoolean("showShare", showShare == 1)
                    .greenChannel().navigation()
        } else {

        }
    }

}
