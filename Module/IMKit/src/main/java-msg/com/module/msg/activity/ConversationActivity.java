package com.module.msg.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.common.base.BaseActivity;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.common.view.titlebar.CommonTitleBar;
import com.dialog.list.DialogListItem;
import com.dialog.list.ListDialog;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.ModuleServiceManager;
import com.module.common.ICallback;
import com.module.msg.IMsgService;
import com.module.msg.api.IMsgServerApi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;
import io.rong.imkit.R;
import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Message;

/**
 * 单聊界面
 */
public class ConversationActivity extends BaseActivity {

    CommonTitleBar mTitleBar;

    String mUserId;

    boolean mIsFriend;

    IMsgService msgService;

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

        msgService = ModuleServiceManager.getInstance().getMsgService();

        RxView.clicks(mTitleBar.getLeftTextView())
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        //U.getSoundUtils().play(TAG, R.raw.normal_back, 500);
                        finish();
                    }
                });


        RxView.clicks(mTitleBar.getRightImageButton())
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        showConfirmOptions();
                    }
                });

        U.getSoundUtils().preLoad(TAG, R.raw.normal_back);
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
                        ApiMethods.subscribe(iMsgServerApi.incSendMsgTimes(Integer.parseInt(mUserId)), null);
                        return message;
                    }
                }

            }

            @Override
            public boolean onSent(Message message, RongIM.SentMessageErrorCode sentMessageErrorCode) {
                return false;
            }
        });
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
        msgService.getBlacklistStatus(mUserId, new ICallback() {
            @Override
            public void onSucess(Object obj) {
                if (obj != null) {
                    boolean result = (boolean) obj;
                    showConfirmOptions(result);
                }
            }

            @Override
            public void onFailed(Object obj, int errcode, String message) {

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
        channels.add(getString(R.string.cancel));
        listDialog = new ListDialog(this);
        List<DialogListItem> listItems = new ArrayList<>();
        for (final String channel : channels) {
            listItems.add(new DialogListItem(channel, new Runnable() {
                @Override
                public void run() {
                    if (channel.equals(getString(R.string.add_to_black_list))) {
                        msgService.addToBlacklist(mUserId, new ICallback() {
                            @Override
                            public void onSucess(Object obj) {
                                U.getToastUtil().showShort("加入成功");
                            }

                            @Override
                            public void onFailed(Object obj, int errcode, String message) {

                            }
                        });
                    } else if (channel.equals(getString(R.string.remove_from_black_list))) {
                        msgService.removeFromBlacklist(mUserId, new ICallback() {
                            @Override
                            public void onSucess(Object obj) {
                                U.getToastUtil().showShort("移除成功");
                            }

                            @Override
                            public void onFailed(Object obj, int errcode, String message) {

                            }
                        });
                    }
                    listDialog.dissmiss();
                }
            }));
        }
        listDialog.showList(listItems);
    }


    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    protected void destroy() {
        super.destroy();
        RongIM.getInstance().setSendMessageListener(null);
        U.getSoundUtils().release(TAG);
    }
}
