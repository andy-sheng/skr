package com.example.dialog;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.common.base.BaseFragment;
import com.common.utils.U;
import com.google.android.flexbox.FlexboxLayout;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.DialogPlusBuilder;
import com.orhanobut.dialogplus.GridHolder;
import com.orhanobut.dialogplus.Holder;
import com.orhanobut.dialogplus.ListHolder;
import com.orhanobut.dialogplus.OnCancelListener;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.OnItemClickListener;
import com.orhanobut.dialogplus.ViewHolder;
import com.wali.live.moduletest.R;

public class DialogsFragment extends BaseFragment {
    Toolbar mActivityToolbar;
    RadioGroup mHolderRadioGroup;
    RadioButton mBasicHolderRadioButton;
    RadioButton mListHolderRadioButton;
    RadioButton mGridHolderRadioButton;
    RadioGroup mPositionRadioGroup;
    RadioButton mBottomPosition;
    RadioButton mCenterPosition;
    RadioButton mTopPosition;
    FlexboxLayout mConfigLayout;
    CheckBox mHeaderCheckBox;
    CheckBox mFooterCheckBox;
    CheckBox mExpandedCheckBox;
    CheckBox mFixedHeaderCheckBox;
    CheckBox mFixedFooterCheckBox;
    TextInputEditText mListCountInput;
    TextInputEditText mContentHeightInput;
    TextInputEditText mContentWidthInput;
    Button mShowDialogButton;

    DialogPlus dialogPlus;

    @Override
    public int initView() {
        return R.layout.test_dialog_fragment;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mActivityToolbar = (Toolbar) mRootView.findViewById(R.id.activity_toolbar);
        mHolderRadioGroup = (RadioGroup) mRootView.findViewById(R.id.holderRadioGroup);
        mBasicHolderRadioButton = (RadioButton) mRootView.findViewById(R.id.basic_holder_radio_button);
        mListHolderRadioButton = (RadioButton) mRootView.findViewById(R.id.list_holder_radio_button);
        mGridHolderRadioButton = (RadioButton) mRootView.findViewById(R.id.grid_holder_radio_button);
        mPositionRadioGroup = (RadioGroup) mRootView.findViewById(R.id.positionRadioGroup);
        mBottomPosition = (RadioButton) mRootView.findViewById(R.id.bottomPosition);
        mCenterPosition = (RadioButton) mRootView.findViewById(R.id.centerPosition);
        mTopPosition = (RadioButton) mRootView.findViewById(R.id.topPosition);
        mConfigLayout = (FlexboxLayout) mRootView.findViewById(R.id.configLayout);
        mHeaderCheckBox = (CheckBox) mRootView.findViewById(R.id.headerCheckBox);
        mFooterCheckBox = (CheckBox) mRootView.findViewById(R.id.footerCheckBox);
        mExpandedCheckBox = (CheckBox) mRootView.findViewById(R.id.expandedCheckBox);
        mFixedHeaderCheckBox = (CheckBox) mRootView.findViewById(R.id.fixedHeaderCheckBox);
        mFixedFooterCheckBox = (CheckBox) mRootView.findViewById(R.id.fixedFooterCheckBox);
        mListCountInput = (TextInputEditText) mRootView.findViewById(R.id.listCountInput);
        mContentHeightInput = (TextInputEditText) mRootView.findViewById(R.id.contentHeightInput);
        mContentWidthInput = (TextInputEditText) mRootView.findViewById(R.id.contentWidthInput);
        mShowDialogButton = (Button) mRootView.findViewById(R.id.showDialogButton);

        mShowDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogPlus();
            }
        });
    }

    private void showDialogPlus() {
        boolean showHeader = mHeaderCheckBox.isChecked();
        boolean showFooter = mFooterCheckBox.isChecked();
        boolean fixedHeader = mFixedHeaderCheckBox.isChecked();
        boolean fixedFooter = mFixedFooterCheckBox.isChecked();
        boolean expaned = mExpandedCheckBox.isChecked();
        int gravity = Gravity.BOTTOM;
        if (R.id.topPosition == mPositionRadioGroup.getCheckedRadioButtonId()) {
            gravity = Gravity.TOP;
        } else if (R.id.centerPosition == mPositionRadioGroup.getCheckedRadioButtonId()) {
            gravity = Gravity.CENTER;
        }

        boolean isGrid = true;
        Holder holder;
        if (R.id.basic_holder_radio_button == mHolderRadioGroup.getCheckedRadioButtonId()) {
            holder = new ViewHolder(R.layout.content);
            isGrid = false;
        } else if (R.id.list_holder_radio_button == mHolderRadioGroup.getCheckedRadioButtonId()) {
            holder = new ListHolder();
            isGrid = false;
        } else {
            holder = new GridHolder(3);
            isGrid = true;
        }

        int listCount = Integer.valueOf(mListCountInput.getText().toString().trim());
        SimpleAdapter adapter = new SimpleAdapter(getActivity(), isGrid, listCount);

        // 具体查看源码
        DialogPlusBuilder builder = DialogPlus.newDialog(getActivity())
                .setContentHolder(holder)
                .setCancelable(true)
                .setGravity(gravity)
                .setExpanded(expaned)
                .setAdapter(adapter);
        if (showHeader) builder.setHeader(R.layout.header, fixedHeader);
        if (showFooter) builder.setFooter(R.layout.footer, fixedFooter);

        int contentHeight = Integer.valueOf(mContentHeightInput.getText().toString().trim());
        int contentwidth = Integer.valueOf(mContentWidthInput.getText().toString().trim());
        if (contentHeight != -1){
            builder.setContentHeight(contentHeight);
        }else {
            builder.setContentHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        if (contentwidth != -1){
            builder.setContentWidth(800);
        }
        builder.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(@NonNull DialogPlus dialog, @NonNull View view) {
                if (view instanceof TextView){
                    U.getToastUtil().showShort(((TextView)view).getText().toString());
                }
                dialogPlus.dismiss();
            }
        });
        builder.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull DialogPlus dialog, @NonNull Object item, @NonNull View view, int position) {
                TextView textView = view.findViewById(R.id.text_view);
                U.getToastUtil().showShort(((TextView)textView).getText().toString());
                dialogPlus.dismiss();
            }
        });
        builder.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(@NonNull DialogPlus dialog) {
                U.getToastUtil().showShort("取消");
            }
        });
        builder.setOverlayBackgroundResource(R.color.transparent);
        dialogPlus = builder.create();
        dialogPlus.show();
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
