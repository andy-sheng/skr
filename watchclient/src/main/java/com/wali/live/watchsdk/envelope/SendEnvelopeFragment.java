package com.wali.live.watchsdk.envelope;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.base.activity.BaseActivity;
import com.base.dialog.DialogUtils;
import com.base.event.KeyboardEvent;
import com.base.fragment.BaseEventBusFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.keyboard.KeyboardUtils;
import com.base.log.MyLog;
import com.base.utils.language.LocaleUtil;
import com.base.view.BackTitleBar;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.recharge.view.RechargeFragment;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.envelope.adapter.EnvelopeChooser;
import com.wali.live.watchsdk.component.adapter.SingleChooser;
import com.wali.live.watchsdk.envelope.presenter.SendEnvelopePresenter;
import com.wali.live.watchsdk.envelope.view.EnvelopeTypeView;
import com.wali.live.watchsdk.webview.WebViewActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.LinkedList;
import java.util.List;

import static com.base.utils.language.LocaleUtil.RED_ENVELOPE_DESC;

/**
 * Created by yangli on 2017/7/18.
 *
 * @module 发红包
 */
public class SendEnvelopeFragment extends BaseEventBusFragment implements View.OnClickListener {
    private final String TAG = "SendEnvelopeFragment";

    public static final String KEY_ROOM_ID = "room_id";
    public static final String KEY_ANCHOR_ID = "zu_id";
    public static final String KEY_VIEWER_COUNT = "key_viewercount";

    private int mSelectedCnt = 666;
    private String mRoomId;
    private long mAnchorId;
    private int mViewerCnt;

    private IPresenter mPresenter;

    private int mNormalTopMargin;
    private BackTitleBar mTitleBar;
    private View mSelectTips;
    private TextView mGoldDiamondTv;
    private TextView mSilverDiamondTv;
    private EditText mInputViewEt;
    private View mSendBtn;

    private final EnvelopeChooser mEnvelopeChooser = new EnvelopeChooser(new SingleChooser.IChooserListener() {
        @Override
        public void onItemSelected(View view) {
            if (!view.isSelected()) {
                view.setSelected(true);
                mSelectedCnt = ((EnvelopeTypeView) view).getmSelectedDiamondNum();
            }
        }
    });

    protected final void $click(int id, View.OnClickListener listener) {
        $(id).setOnClickListener(listener);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.send_btn) {
            onSendBtnClick();
        } else if (i == R.id.instruction_btn) {
            Intent intent = new Intent(getActivity(), WebViewActivity.class);
            intent.putExtra(WebViewActivity.EXTRA_URL, LocaleUtil.getWebViewUrl(RED_ENVELOPE_DESC));
            startActivity(intent);
        } else if (i == R.id.recharge) {
            RechargeFragment.openFragment(getActivity(), R.id.main_act_container, null, true);
            // StatisticsWorker.getsInstance().sendCommand(StatisticsWorker.AC_APP, "redEnvelope-recharge-" + zuId + "-click", 1);
        }
    }

    private void onSendBtnClick() {
        if (mSelectedCnt > MyUserInfoManager.getInstance().getDiamondNum() +
                MyUserInfoManager.getInstance().getVirtualDiamondNum()) {
            DialogUtils.showNormalDialog(getActivity(),
                    getString(R.string.account_withdraw_pay_user_account_not_enough),
                    getString(R.string.money_not_enough_tip), R.string.ok, R.string.cancel,
                    new DialogUtils.IDialogCallback() {
                        @Override
                        public void process(DialogInterface dialogInterface, int i) {
                            RechargeFragment.openFragment(getActivity(), R.id.main_act_container, null, true);
                        }
                    }, null);
        } else {
            String msg = String.valueOf(mInputViewEt.getText());
            if (TextUtils.isEmpty(msg)) {
                msg = getString(R.string.money_coming);
            }
            mPresenter.sendEnvelope(mAnchorId, mRoomId, mViewerCnt, mSelectedCnt, msg);
        }
    }

    @Override
    public int getRequestCode() {
        return mRequestCode;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.send_envelope_fragment, container, false);
    }

    private List<View> addEnvelopeView(ViewGroup viewGroup, EnvelopeTypeView.EnvelopeType... envelopeTypes) {
        List<View> views = new LinkedList<>();
        for (EnvelopeTypeView.EnvelopeType elem : envelopeTypes) {
            EnvelopeTypeView envelopeTypeView = new EnvelopeTypeView(viewGroup.getContext());
            envelopeTypeView.setData(elem);
            viewGroup.addView(envelopeTypeView);
            views.add(envelopeTypeView);
        }
        return views;
    }

    @Override
    protected void bindView() {
        mTitleBar = $(R.id.title_bar);
        mTitleBar.hideBottomLine();
        mTitleBar.setTitle(R.string.send_redpacket);
        mTitleBar.setBackgroundColor(getResources().getColor(R.color.color_ff5345));
        mTitleBar.getBackBtn().setTextColor(getResources().getColorStateList(R.color.color_feefc9_pressed_trans50));
        mTitleBar.getBackBtn().setCompoundDrawablesWithIntrinsicBounds(R.drawable.red_topbar_icon_all_back_bg, 0, 0, 0);
        mTitleBar.getBackBtn().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ViewGroup container1 = $(R.id.envelope_container1);
        List<View> envelopeViews = addEnvelopeView(container1,
                new EnvelopeTypeView.EnvelopeType(666, R.color.color_white, R.drawable.red_item_0),
                new EnvelopeTypeView.EnvelopeType(999, R.color.color_white, R.drawable.red_item_1),
                new EnvelopeTypeView.EnvelopeType(2800, R.color.color_white, R.drawable.red_item_2));
        ViewGroup container2 = $(R.id.envelope_container2);
        envelopeViews.addAll(addEnvelopeView(container2,
                new EnvelopeTypeView.EnvelopeType(5200, R.color.color_ffda7b_90, R.drawable.red_item_3),
                new EnvelopeTypeView.EnvelopeType(6888, R.color.color_ffda7b_90, R.drawable.red_item_4),
                new EnvelopeTypeView.EnvelopeType(8888, R.color.color_ffda7b_90, R.drawable.red_item_5)));
        mEnvelopeChooser.setup(envelopeViews, envelopeViews.get(0));

        mInputViewEt = $(R.id.input_view);
        mSelectTips = $(R.id.select_tips);
        mGoldDiamondTv = $(R.id.gold_diamond_tv);
        mSilverDiamondTv = $(R.id.silver_diamond_tv);
        mSendBtn = $(R.id.send_btn);

        $click(R.id.send_btn, this);
        $click(R.id.instruction_btn, this);
        $click(R.id.recharge, this);

        mNormalTopMargin = ((ViewGroup.MarginLayoutParams) mSelectTips.getLayoutParams()).topMargin;
        mGoldDiamondTv.setText(getString(R.string.gold_count, MyUserInfoManager.getInstance().getDiamondNum()));
        mSilverDiamondTv.setText(getString(R.string.silver_count, MyUserInfoManager.getInstance().getVirtualDiamondNum()));

        Bundle bundle = getArguments();
        mRoomId = bundle.getString(SendEnvelopeFragment.KEY_ROOM_ID);
        mAnchorId = bundle.getLong(SendEnvelopeFragment.KEY_ANCHOR_ID);
        mViewerCnt = bundle.getInt(SendEnvelopeFragment.KEY_VIEWER_COUNT);

        mPresenter = new SendEnvelopePresenter();
        mPresenter.setSendEnvelopeView(new ISendEnvelopeView() {
            @Override
            public void onSendSuccess() {
                finish();
            }
        });

    }

    private void finish() {
        KeyboardUtils.hideKeyboard(getActivity());
        FragmentNaviUtils.popFragment(getActivity());
    }

    @Subscribe(threadMode = ThreadMode.MAIN, priority = 1)
    public void onEventMainThread(KeyboardEvent event) {
        MyLog.w(TAG, "KeyboardEvent eventType=" + event.eventType);
        switch (event.eventType) {
            case KeyboardEvent.EVENT_TYPE_KEYBOARD_VISIBLE: {
                int keyboardHeight = Integer.parseInt(String.valueOf(event.obj1));
                int y = mRootView.getHeight() - mSendBtn.getBottom();
                ViewGroup.MarginLayoutParams layoutParams =
                        (ViewGroup.MarginLayoutParams) mSelectTips.getLayoutParams();
                if (y > 0 && y < keyboardHeight) {
                    layoutParams.topMargin = mNormalTopMargin + (y - keyboardHeight);
                }
                mSelectTips.setLayoutParams(layoutParams);
                break;
            }
            case KeyboardEvent.EVENT_TYPE_KEYBOARD_HIDDEN: {
                ViewGroup.MarginLayoutParams layoutParams =
                        (ViewGroup.MarginLayoutParams) mSelectTips.getLayoutParams();
                layoutParams.topMargin = mNormalTopMargin;
                mSelectTips.setLayoutParams(layoutParams);
                break;
            }
        }
    }

    public static void openFragment(BaseActivity activity, RoomBaseDataModel myRoomData) {
        Bundle bundle = new Bundle();
        bundle.putString(SendEnvelopeFragment.KEY_ROOM_ID, myRoomData.getRoomId());
        bundle.putLong(SendEnvelopeFragment.KEY_ANCHOR_ID, myRoomData.getUid());
        bundle.putInt(SendEnvelopeFragment.KEY_VIEWER_COUNT, myRoomData.getViewerCnt());
        FragmentNaviUtils.addFragment(activity, R.id.main_act_container, SendEnvelopeFragment.class,
                bundle, true, true, true);
    }

    public interface IPresenter {
        /**
         * 发红包
         */
        void sendEnvelope(long anchorId, String roomId, int viewerCnt, int gemCnt, String msg);

        void setSendEnvelopeView(ISendEnvelopeView view);
    }

    public interface ISendEnvelopeView {
        /**
         * 发红包回调
         */
        void onSendSuccess();
    }

}
