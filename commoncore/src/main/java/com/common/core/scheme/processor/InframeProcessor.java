package com.common.core.scheme.processor;

import android.net.Uri;
import android.text.TextUtils;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.core.account.UserAccountManager;
import com.common.core.scheme.SchemeConstants;
import com.common.core.scheme.SchemeUtils;
import com.common.log.MyLog;
import com.common.utils.U;
import com.module.RouterConstants;
import com.module.grab.IGrabModeGameService;

/**
 * Created by lan on 16/10/26.
 *
 * @module scheme
 * @description Walilive的Uri的逻辑代码
 */
public class InframeProcessor implements ISchemeProcessor {
    private static final String TAG = SchemeConstants.LOG_PREFIX + "InframeProcessor";


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
            if (beforeHomeExistJudge) {
                switch (authority) {
                    case SchemeConstants.HOST_CHANNEL:
                        processChannel(uri);
                        return ProcessResult.AcceptedAndContinue;
                }
            } else {
                switch (authority) {
                    case SchemeConstants.HOST_SHARE:
                        processShareUrl(uri);
                        return ProcessResult.AcceptedAndReturn;
                    case SchemeConstants.HOST_WEB:
                        processWebUrl(uri);
                        return ProcessResult.AcceptedAndReturn;
                    case SchemeConstants.HOST_GAME:
                        processGameUrl(uri);
                        return ProcessResult.AcceptedAndReturn;
                    case SchemeConstants.HOST_WALLET:
                        processWalletUrl(uri);
                        return ProcessResult.AcceptedAndReturn;
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
        if (TextUtils.isEmpty(path)) {
            MyLog.w(TAG, "processWalletUrl path is empty");
            return;
        }

        if (SchemeConstants.PATH_WITH_DRAW.equals(path)) {
            if (!UserAccountManager.getInstance().hasAccount()) {
                MyLog.w(TAG, "processWalletUrl 没有登录");
                return;
            }

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
        if (TextUtils.isEmpty(path)) {
            MyLog.w(TAG, "processGameUrl path is empty");
            return;
        }

        if (SchemeConstants.PATH_RANK_CHOOSE_SONG.equals(path)) {
            try {
                if (!UserAccountManager.getInstance().hasAccount()) {
                    MyLog.w(TAG, "processGameUrl 没有登录");
                    return;
                }

                String gameMode = SchemeUtils.getString(uri, SchemeConstants.PARAM_GAME_MODE);
                ARouter.getInstance().build(RouterConstants.ACTIVITY_PLAY_WAYS)
                        .withInt("key_game_type", Integer.parseInt(gameMode))
                        .withBoolean("selectSong", true)
                        .navigation();
            } catch (Exception e) {
                MyLog.e(TAG, e);
            }
        } else if (SchemeConstants.PATH_GRAB_MATCH.equals(path)) {
            try {
                if (!UserAccountManager.getInstance().hasAccount()) {
                    MyLog.w(TAG, "processGameUrl 没有登录");
                    return;
                }

                int tagId = SchemeUtils.getInt(uri, SchemeConstants.PARAM_TAG_ID, 2);
                IGrabModeGameService iGrabModeGameService = (IGrabModeGameService) ARouter.getInstance().build(RouterConstants.SERVICE_GRAB_SERVICE).navigation();
                iGrabModeGameService.jump(0, tagId);
            } catch (Exception e) {
                MyLog.e(TAG, e);
            }
        } else {

        }
    }

    private void processShareUrl(Uri uri) {

    }

    private void processWebUrl(Uri uri) {
        String path = uri.getPath();
        if (TextUtils.isEmpty(path)) {
            MyLog.w(TAG, "processWebUrl path is empty");
            return;
        }

        if (SchemeConstants.PATH_FULL_SCREEN.equals(path)) {
            try {
                if (TextUtils.isEmpty(SchemeUtils.getString(uri, SchemeConstants.PARAM_URL))) {
                    MyLog.w(TAG, "processWebUrl url is empty");
                    return;
                }

                if (!UserAccountManager.getInstance().hasAccount()) {
                    MyLog.w(TAG, "processWebUrl 没有登录");
                    return;
                }

                String url = SchemeUtils.getString(uri, SchemeConstants.PARAM_URL);
                int showShare = SchemeUtils.getInt(uri, SchemeConstants.PARAM_SHOW_SHARE, 0);
                ARouter.getInstance().build(RouterConstants.ACTIVITY_WEB)
                        .withString("url", url)
                        .withBoolean("showShare", showShare == 1)
                        .greenChannel().navigation();
            } catch (Exception e) {
                MyLog.e(TAG, e);
            }
        } else {

        }
    }


}
