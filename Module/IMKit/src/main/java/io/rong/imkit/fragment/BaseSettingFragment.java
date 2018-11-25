//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Locale;

import io.rong.imkit.R;
import io.rong.imkit.RongContext;
import io.rong.imlib.model.Conversation.ConversationType;

public abstract class BaseSettingFragment extends BaseFragment implements OnClickListener {
  TextView mTextView;
  CheckBox mCheckBox;
  RelativeLayout mSettingItem;
  String mTargetId;
  ConversationType mConversationType;

  public BaseSettingFragment() {
  }

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Intent intent = null;
    if (this.getActivity() != null) {
      intent = this.getActivity().getIntent();
      if (intent.getData() != null) {
        this.mConversationType = ConversationType.valueOf(intent.getData().getLastPathSegment().toUpperCase(Locale.US));
        this.mTargetId = intent.getData().getQueryParameter("targetId");
      }
    }

  }

  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.rc_fragment_base_setting, container, false);
    this.mTextView = (TextView)view.findViewById(R.id.rc_title);
    this.mCheckBox = (CheckBox)view.findViewById(R.id.rc_checkbox);
    this.mSettingItem = (RelativeLayout)view.findViewById(R.id.rc_setting_item);
    return view;
  }

  public void onActivityCreated(Bundle savedInstanceState) {
    this.mTextView.setText(this.setTitle());
    this.mCheckBox.setEnabled(this.setSwitchButtonEnabled());
    if (View.GONE == this.setSwitchBtnVisibility()) {
      this.mCheckBox.setVisibility(View.GONE);
    } else if (View.VISIBLE == this.setSwitchBtnVisibility()) {
      this.mCheckBox.setVisibility(View.VISIBLE);
    }

    this.mCheckBox.setOnClickListener(this);
    this.mSettingItem.setOnClickListener(this);
    this.initData();
    super.onActivityCreated(savedInstanceState);
  }

  public void onClick(View v) {
    if (v == this.mSettingItem) {
      this.onSettingItemClick(v);
    } else if (v == this.mCheckBox) {
      this.toggleSwitch(this.mCheckBox.isChecked());
    }

  }

  public void onDestroy() {
    super.onDestroy();
    RongContext.getInstance().getEventBus().unregister(this);
  }

  protected ConversationType getConversationType() {
    return this.mConversationType;
  }

  protected String getTargetId() {
    return this.mTargetId;
  }

  protected abstract String setTitle();

  protected abstract boolean setSwitchButtonEnabled();

  protected abstract int setSwitchBtnVisibility();

  protected abstract void onSettingItemClick(View var1);

  protected abstract void toggleSwitch(boolean var1);

  protected abstract void initData();

  protected void setSwitchBtnStatus(boolean status) {
    this.mCheckBox.setChecked(status);
  }

  protected boolean getSwitchBtnStatus() {
    return this.mCheckBox.isChecked();
  }

  public boolean onBackPressed() {
    return false;
  }

  public void onRestoreUI() {
    this.initData();
  }
}
