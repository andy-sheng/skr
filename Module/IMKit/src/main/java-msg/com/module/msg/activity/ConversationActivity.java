package com.module.msg.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.fastjson.JSON;
import com.common.base.BaseActivity;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.permission.SkrNotificationPermission;
import com.common.core.userinfo.ResponseCallBack;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.core.userinfo.UserInfoManager;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.NetworkUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.titlebar.CommonTitleBar;
import com.dialog.list.DialogListItem;
import com.dialog.list.ListDialog;
import com.module.RouterConstants;
import com.module.home.IHomeService;
import com.module.msg.api.IMsgServerApi;
import com.zq.live.proto.Common.EVIPType;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.rong.imkit.R;
import io.rong.imkit.RongIM;
import io.rong.imlib.model.Message;
import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * 单聊界面
 */
public class ConversationActivity extends BaseActivity {

    CommonTitleBar mTitleBar;

    String mUserId;

    boolean mIsFriend;

    ListDialog listDialog;

    String mDescWhenExceed;
    int mCanSendTimes = -1;

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.conversation_activity;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mTitleBar = findViewById(R.id.titlebar);

        if (getIntent() != null && getIntent().getData() != null) {
            String title = getIntent().getData().getQueryParameter("title");
            mUserId = getIntent().getData().getQueryParameter("targetId");
            mTitleBar.getCenterTextView().setText(title);
        }
        mIsFriend = getIntent().getBooleanExtra("isFriend", false);

        mTitleBar.getLeftTextView().setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                finish();
            }
        });

        if (mUserId.equals(UserInfoModel.USER_ID_XIAOZHUSHOU + "")) {
            mTitleBar.getRightImageButton().setVisibility(View.GONE);
        } else {
            mTitleBar.getRightImageButton().setOnClickListener(new DebounceViewClickListener() {
                @Override
                public void clickValid(View v) {
                    showConfirmOptions();
                }
            });
        }

        U.getSoundUtils().preLoad(getTAG(), R.raw.normal_back);
        RongIM.getInstance().setSendMessageListener(new RongIM.OnSendMessageListener() {
            @Override
            public Message onSend(Message message) {
                if (mCanSendTimes == -1) {
                    return message;
                } else {
                    if (mCanSendTimes <= 0) {
                        // 超出发送次数了，提示用户
                        if (!TextUtils.isEmpty(mDescWhenExceed)) {
                            U.getToastUtil().showShort(mDescWhenExceed);
                        } else {
                            U.getToastUtil().showShort("陌生人间不能发送太多消息哦");
                        }
                        return null;
                    } else {
                        mCanSendTimes--;
                        // 告诉服务器自增

                        IMsgServerApi iMsgServerApi = ApiManager.getInstance().createService(IMsgServerApi.class);
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("toUserID", Integer.parseInt(mUserId));
                        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
                        ApiMethods.subscribe(iMsgServerApi.incSendMsgTimes(body), null);
                        return message;
                    }
                }

            }

            @Override
            public boolean onSent(Message message, RongIM.SentMessageErrorCode sentMessageErrorCode) {
                return false;
            }
        });
        checkMsgTimes();
    }


    private void checkMsgTimes() {
        if (MyUserInfoManager.getInstance().getVipType() == EVIPType.EVT_GOLDEN_V.getValue()) {
            return;
        }

        if (mIsFriend) {

        } else {
            // 不是好友，看看有没有资格发消息
            IMsgServerApi iMsgServerApi = ApiManager.getInstance().createService(IMsgServerApi.class);
            ApiMethods.subscribe(iMsgServerApi.checkSendMsg(Integer.parseInt(mUserId)), new ApiObserver<ApiResult>() {

                @Override
                public void process(ApiResult obj) {
                    if (obj.getErrno() == 0) {
                        mCanSendTimes = obj.getData().getIntValue("resTimes");
                        mDescWhenExceed = obj.getData().getString("desc");
                    }
                }
            }, this);
        }
    }

    private void showConfirmOptions() {
        UserInfoManager.getInstance().getBlacklistStatus(Integer.valueOf(mUserId), new ResponseCallBack() {
            @Override
            public void onServerSucess(Object obj) {
                if (obj != null) {
                    boolean result = (boolean) obj;
                    showConfirmOptions(result);
                }
            }

            @Override
            public void onServerFailed() {

            }
        });
    }

    private void showConfirmOptions(boolean isBlack) {
        U.getKeyBoardUtils().hideSoftInputKeyBoard(this);

        final List<String> channels = new ArrayList<>();
        if (isBlack) {
            channels.add(getString(R.string.remove_from_black_list));
        } else {
            channels.add(getString(R.string.add_to_black_list));
        }
        listDialog = new ListDialog(this);
        List<DialogListItem> listItems = new ArrayList<>();
        for (final String channel : channels) {
            listItems.add(new DialogListItem(channel, "#FF3529", new Runnable() {
                @Override
                public void run() {
                    if (channel.equals(getString(R.string.add_to_black_list))) {
                        UserInfoManager.getInstance().addToBlacklist(Integer.valueOf(mUserId), new ResponseCallBack() {
                            @Override
                            public void onServerSucess(Object o) {
                                U.getToastUtil().showShort("加入黑名单成功");
                            }

                            @Override
                            public void onServerFailed() {

                            }
                        });
                    } else if (channel.equals(getString(R.string.remove_from_black_list))) {
                        UserInfoManager.getInstance().removeBlackList(Integer.valueOf(mUserId), new ResponseCallBack() {
                            @Override
                            public void onServerSucess(Object o) {
                                U.getToastUtil().showShort("移除黑名单成功");
                            }

                            @Override
                            public void onServerFailed() {

                            }
                        });
                    }
                    listDialog.dissmiss();
                }
            }));
        }
        listItems.add(new DialogListItem(getString(R.string.cancel), "#007AFF", new Runnable() {
            @Override
            public void run() {
                listDialog.dissmiss();
            }
        }));
        listDialog.showList(listItems);
    }

    @Override
    public void finish() {
        super.finish();
        if (!U.getActivityUtils().isHomeActivityExist()) {
            /**
             * 可能是通过离线push打开的
             */
            IHomeService channelService = (IHomeService) ARouter.getInstance().build(RouterConstants.SERVICE_HOME).navigation();
            if (channelService != null) {
                channelService.goHomeActivity(this);
            }
        }
        /**
         * 如果没有通知栏权限，提示一次
         */
        if (U.getPermissionUtils().checkNotification(U.app())) {
            // 有权限
        } else {
            long lastShowTs = U.getPreferenceUtils().getSettingLong("show_go_notification_page", 0);
            if (System.currentTimeMillis() - lastShowTs > 24 * 60 * 60 * 1000) {
                U.getPreferenceUtils().setSettingLong("show_go_notification_page", System.currentTimeMillis());
                SkrNotificationPermission skrNotificationPermission = new SkrNotificationPermission();
                skrNotificationPermission.ensurePermission(U.getActivityUtils().getHomeActivity(), null, true);
            }
        }
    }

    @Subscribe
    public void onEvent(NetworkUtils.NetworkChangeEvent event) {
        if (U.getNetworkUtils().hasNetwork()) {
            // 变有网了
            if (!mIsFriend && mCanSendTimes == -1) {
                // 非好友，且 次数未初始化，初始化一下
                checkMsgTimes();
            }
        }
    }

    @Override
    public boolean useEventBus() {
        return true;
    }

    @Override
    protected void destroy() {
        super.destroy();
        RongIM.getInstance().setSendMessageListener(null);
        U.getSoundUtils().release(getTAG());
    }

    public String getUserId() {
        return mUserId;
    }
}
