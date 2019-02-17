package com.module.msg.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.common.base.BaseActivity;
import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.titlebar.CommonTitleBar;
import com.didichuxing.doraemonkit.kit.sysinfo.SysInfoItem;
import com.didichuxing.doraemonkit.ui.widget.dialog.DialogListItem;
import com.didichuxing.doraemonkit.ui.widget.dialog.ListDialog;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.ModuleServiceManager;
import com.module.common.ICallback;
import com.module.msg.IMsgService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;
import io.rong.imkit.R;

import static com.umeng.socialize.utils.ContextUtil.getContext;

/**
 * 单聊界面
 */
public class ConversationActivity extends BaseActivity {

    CommonTitleBar mTitleBar;

    String mUserId;

    IMsgService msgService;

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
        msgService = ModuleServiceManager.getInstance().getMsgService();

        RxView.clicks(mTitleBar.getLeftTextView())
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
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
        final List<String> channels = new ArrayList<>();
        if (isBlack) {
            channels.add(getString(R.string.remove_from_black_list));
        } else {
            channels.add(getString(R.string.add_to_black_list));
        }
        channels.add(getString(R.string.cancel));
        ListDialog listDialog = new ListDialog(this);
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
}
