package com.common.core.scheme.processor;

import android.net.Uri;
import android.text.TextUtils;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.core.account.UserAccountManager;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.permission.SkrAudioPermission;
import com.common.core.scheme.SchemeConstants;
import com.common.core.scheme.SchemeUtils;
import com.common.core.scheme.event.BothRelationFromSchemeEvent;
import com.common.core.scheme.event.DoubleInviteFromSchemeEvent;
import com.common.core.scheme.event.GrabInviteFromSchemeEvent;
import com.common.core.scheme.event.JumpHomeDoubleChatPageEvent;
import com.common.core.scheme.event.JumpHomeFromSchemeEvent;
import com.common.log.MyLog;
import com.common.utils.U;
import com.module.RouterConstants;
import com.module.playways.IPlaywaysModeService;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by lan on 16/10/26.
 *
 * @module scheme
 * @description Walilive的Uri的逻辑代码
 */
public class InframeProcessor implements ISchemeProcessor {
    private static final String TAG = SchemeConstants.LOG_PREFIX + "InframeProcessor";

    SkrAudioPermission mSkrAudioPermission = new SkrAudioPermission();

    @Override
    public ProcessResult process(Uri uri, boolean beforeHomeExistJudge) {
        //inframesker://game/match?from=h5
        //inframesker://person/homepage?from=h5
        //其中scheme为inframesker, host为game , relativePath为match, query为from=h5.
        String scheme = uri.getScheme();
        MyLog.w(TAG, "process scheme=" + scheme);
        if (TextUtils.isEmpty(scheme)) {
            return ProcessResult.NotAccepted;
        }

        final String authority = uri.getAuthority();
        MyLog.w(TAG, "process authority=" + authority);
        if (TextUtils.isEmpty(authority)) {
            return ProcessResult.NotAccepted;
        }
        if (SchemeConstants.SCHEME_INFRAMESKER.equals(scheme)) {
            // beforeHomeExistJudge 表示 HomeActivity 不存在时允不允许执行这个scheme
            if (beforeHomeExistJudge) {
                switch (authority) {
                    /**
                     * 写入渠道号
                     */
                    case SchemeConstants.HOST_CHANNEL:
                        processChannel(uri);
                        return ProcessResult.AcceptedAndContinue;
                }
            } else {
                if (!UserAccountManager.getInstance().hasAccount()) {
                    MyLog.w(TAG, "processWebUrl 没有登录");
                    return ProcessResult.AcceptedAndReturn;
                }
                String path = uri.getPath();
                if (TextUtils.isEmpty(path)) {
                    MyLog.w(TAG, "processWalletUrl path is empty");
                    return ProcessResult.AcceptedAndReturn;
                }
                switch (authority) {
                    case SchemeConstants.HOST_HOME:
                        processHomeUrl(uri);
                        return ProcessResult.AcceptedAndReturn;
                    case SchemeConstants.HOST_SHARE:
                        processShareUrl(uri);
                        return ProcessResult.AcceptedAndReturn;
                    case SchemeConstants.HOST_WEB:
                        processWebUrl(uri);
                        return ProcessResult.AcceptedAndReturn;
                    case SchemeConstants.HOST_WALLET:
                        processWalletUrl(uri);
                        return ProcessResult.AcceptedAndReturn;
                    case SchemeConstants.HOST_ROOM:
                        processRoomUrl(uri);
                        return ProcessResult.AcceptedAndReturn;
                    case SchemeConstants.HOST_PERSON:
                        processPersonUrl(uri);
                        return ProcessResult.AcceptedAndReturn;
                    case SchemeConstants.HOST_RELATION:
                        processRelationUrl(uri);
                        return ProcessResult.AcceptedAndReturn;
                    case SchemeConstants.HOST_FEED:
                        processFeedUrl(uri);
                        return ProcessResult.AcceptedAndReturn;
                    case "game":
                        processGameUrl(uri);
                        return ProcessResult.AcceptedAndReturn;
                }
            }
        } else if ("rong".equals(scheme)) {
            if (beforeHomeExistJudge) {

            } else {
                if (!UserAccountManager.getInstance().hasAccount()) {
                    MyLog.w(TAG, "processWebUrl 没有登录");
                    return ProcessResult.AcceptedAndReturn;
                }
                String path = uri.getPath();
                if (TextUtils.isEmpty(path)) {
                    MyLog.w(TAG, "processWalletUrl path is empty");
                    return ProcessResult.AcceptedAndReturn;
                }
                if ("/conversationlist".equals(path)) {
                    EventBus.getDefault().post(new JumpHomeFromSchemeEvent(1));
                }
            }
        }
        return ProcessResult.NotAccepted;
    }

    private void processChannel(Uri uri) {
        MyLog.d(TAG, "processChannel" + " uri=" + uri);
        String path = uri.getPath();
        if (TextUtils.isEmpty(path)) {
            MyLog.w(TAG, "processGameUrl path is empty");
            return;
        }
        String subchannel = path;
        if (subchannel.startsWith("/")) {
            subchannel = subchannel.substring(1);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(subchannel);
        String opid = uri.getQueryParameter("opid");
        if (TextUtils.isEmpty(opid)) {
        } else {
            sb.append("_").append(opid);
        }
        U.getChannelUtils().setSubChannel(sb.toString());
    }

    private void processWalletUrl(Uri uri) {
        String path = uri.getPath();
        if (SchemeConstants.PATH_WITH_DRAW.equals(path)) {
            try {
                String from = SchemeUtils.getString(uri, SchemeConstants.PARAM_FROM);

                ARouter.getInstance().build(RouterConstants.ACTIVITY_WITH_DRAW)
                        .withString("from", from)
                        .navigation();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void processGameUrl(Uri uri) {
        String path = uri.getPath();

        if (SchemeConstants.PATH_RANK_CHOOSE_SONG.equals(path)) {
            String gameMode = SchemeUtils.getString(uri, SchemeConstants.PARAM_GAME_MODE);
            ARouter.getInstance().build(RouterConstants.ACTIVITY_PLAY_WAYS)
                    .withInt("key_game_type", Integer.parseInt(gameMode))
                    .withBoolean("selectSong", true)
                    .navigation();
        } else if (SchemeConstants.PATH_GRAB_MATCH.equals(path)) {

        } else {

        }
    }

    private void processRoomUrl(Uri uri) {
        String path = uri.getPath();
        if ("/grabjoin".equals(path)) {
            int ownerId = SchemeUtils.getInt(uri, "owner", 0);
            int roomId = SchemeUtils.getInt(uri, "gameId", 0);
            int tagId = SchemeUtils.getInt(uri, "tagId", 0);
            int ask = SchemeUtils.getInt(uri, "ask", 0);
            int mediaType = SchemeUtils.getInt(uri, "mediaType", 0);
            if (ownerId > 0 && roomId > 0) {
                if (ownerId == MyUserInfoManager.getInstance().getUid()) {
                    MyLog.d(TAG, "processRoomUrl 房主id是自己，可能从口令粘贴板过来的，忽略");
                    return;
                }
                GrabInviteFromSchemeEvent event = new GrabInviteFromSchemeEvent();
                event.ask = ask;
                event.ownerId = ownerId;
                event.roomId = roomId;
                event.tagId = tagId;
                event.mediaType = mediaType;
                EventBus.getDefault().post(event);
            }
        } else if ("/joindouble".equals(path)) {
            int ownerId = SchemeUtils.getInt(uri, "owner", 0);
            int roomId = SchemeUtils.getInt(uri, "gameId", 0);
            int ask = SchemeUtils.getInt(uri, "ask", 0);
            int mediaType = SchemeUtils.getInt(uri, "mediaType", 0);
            if (ownerId > 0 && roomId > 0) {
                if (ownerId == MyUserInfoManager.getInstance().getUid()) {
                    MyLog.d(TAG, "processRoomUrl 房主id是自己，可能从口令粘贴板过来的，忽略");
                    return;
                }
                DoubleInviteFromSchemeEvent event = new DoubleInviteFromSchemeEvent();
                event.ask = ask;
                event.ownerId = ownerId;
                event.roomId = roomId;
                event.mediaType = mediaType;
                EventBus.getDefault().post(event);
            }
        } else if ("/jump_match".equals(path)) {
            final int tagId = SchemeUtils.getInt(uri, "tagId", 0);
            mSkrAudioPermission.ensurePermission(U.getActivityUtils().getHomeActivity(), new Runnable() {
                @Override
                public void run() {
                    IPlaywaysModeService iRankingModeService = (IPlaywaysModeService) ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation();
                    if (iRankingModeService != null) {
                        iRankingModeService.tryGoGrabMatch(tagId);
                    }
                }
            }, true);

        } else if ("/jump_create".equals(path)) {
            mSkrAudioPermission.ensurePermission(U.getActivityUtils().getHomeActivity(), new Runnable() {
                @Override
                public void run() {
                    IPlaywaysModeService iRankingModeService = (IPlaywaysModeService) ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation();
                    if (iRankingModeService != null) {
                        iRankingModeService.tryGoCreateRoom();
                    }
                }
            }, true);
        } else if ("/chat_page".equals(path)) {
            EventBus.getDefault().post(new JumpHomeDoubleChatPageEvent());
        }
    }

    private void processPersonUrl(Uri uri) {
        String path = uri.getPath();
        if ("/jump_update_info".equals(path)) {
            ARouter.getInstance().build(RouterConstants.ACTIVITY_EDIT_INFO)
                    .navigation();
        } else if ("/jump_person_center".equals(path)) {
            // 跳到个人中心
            EventBus.getDefault().post(new JumpHomeFromSchemeEvent(2));
        }
    }

    private void processHomeUrl(Uri uri) {
        String path = uri.getPath();
        if ("/jump".equals(path)) {
            EventBus.getDefault().post(new JumpHomeFromSchemeEvent(0));
        } else if ("trywakeup".equals(path)) {
            // 不做任何操作，只是唤启app
        }
    }

    private void processRelationUrl(Uri uri) {
        String path = uri.getPath();
        if ("/bothfollow".equals(path)) {
            int inviterId = SchemeUtils.getInt(uri, "inviterId", 0);
            if (inviterId > 0 && inviterId != MyUserInfoManager.getInstance().getUid()) {
                BothRelationFromSchemeEvent event = new BothRelationFromSchemeEvent();
                event.useId = inviterId;
                EventBus.getDefault().post(event);
            }
        } else {

        }
    }

    private void processFeedUrl(Uri uri) {
        String path = uri.getPath();
        if ("/detail".equals(path)) {
            int feedID = SchemeUtils.getInt(uri, "feedId", 0);
            if (feedID > 0) {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDS_DETAIL)
                        .withInt("feed_ID", feedID)
                        .withInt("from", 4)
                        .navigation();
            }
        } else {

        }
    }

    private void processShareUrl(Uri uri) {

    }

    private void processWebUrl(Uri uri) {
        String path = uri.getPath();
        if (SchemeConstants.PATH_FULL_SCREEN.equals(path)) {
            if (TextUtils.isEmpty(SchemeUtils.getString(uri, SchemeConstants.PARAM_URL))) {
                MyLog.w(TAG, "processWebUrl url is empty");
                return;
            }

            String url = SchemeUtils.getString(uri, SchemeConstants.PARAM_URL);
            int showShare = SchemeUtils.getInt(uri, SchemeConstants.PARAM_SHOW_SHARE, 0);
            ARouter.getInstance().build(RouterConstants.ACTIVITY_WEB)
                    .withString("url", url)
                    .withBoolean("showShare", showShare == 1)
                    .greenChannel().navigation();
        } else {

        }
    }


}
