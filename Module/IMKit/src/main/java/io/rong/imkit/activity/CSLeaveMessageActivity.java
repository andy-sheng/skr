//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.activity;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.rong.common.RLog;
import io.rong.eventbus.EventBus;
import io.rong.imkit.R;
import io.rong.imkit.RongBaseNoActionbarActivity;
import io.rong.imkit.RongIM;
import io.rong.imkit.model.Event.CSTerminateEvent;
import io.rong.imkit.utilities.RongUtils;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.RongIMClient.ErrorCode;
import io.rong.imlib.RongIMClient.OperationCallback;
import io.rong.imlib.RongIMClient.ResultCallback;
import io.rong.imlib.model.CSLMessageItem;
import io.rong.imlib.model.CSLMessageItem.RemindType;
import io.rong.imlib.model.Conversation.ConversationType;
import io.rong.message.InformationNotificationMessage;

public class CSLeaveMessageActivity extends RongBaseNoActionbarActivity {
  private static final String TAG = "CSLeaveMessageActivity";
  private ArrayList<EditText> mEditList = new ArrayList();
  private String mTargetId;
  private ArrayList<CSLMessageItem> mItemList;

  public CSLeaveMessageActivity() {
  }

  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.rc_cs_leave_message);
    this.mTargetId = this.getIntent().getStringExtra("targetId");
    Bundle bundle = this.getIntent().getExtras();
    if (bundle != null) {
      this.mItemList = bundle.getParcelableArrayList("itemList");
    }

    TextView cancelBtn = (TextView)this.findViewById(R.id.rc_btn_cancel);
    LinearLayout container = (LinearLayout)this.findViewById(R.id.rc_content);
    this.addItemToContainer(container);
    TextView submitBtn = (TextView)this.findViewById(R.id.rc_submit_message);
    submitBtn.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        if (io.rong.imkit.activity.CSLeaveMessageActivity.this.isContentValid()) {
          Map<String, String> map = new HashMap();
          Iterator var3 = io.rong.imkit.activity.CSLeaveMessageActivity.this.mEditList.iterator();

          while(var3.hasNext()) {
            EditText editText = (EditText)var3.next();
            String name = (String)editText.getTag();
            map.put(name, editText.getText().toString());
          }

          RongIMClient.getInstance().leaveMessageCustomService(io.rong.imkit.activity.CSLeaveMessageActivity.this.mTargetId, map, new OperationCallback() {
            public void onSuccess() {
              InformationNotificationMessage notificationMessage = new InformationNotificationMessage(io.rong.imkit.activity.CSLeaveMessageActivity.this.getResources().getString(R.string.rc_cs_message_submited));
              RongIM.getInstance().insertMessage(ConversationType.CUSTOMER_SERVICE, io.rong.imkit.activity.CSLeaveMessageActivity.this.mTargetId, RongIMClient.getInstance().getCurrentUserId(), notificationMessage, (ResultCallback)null);
              io.rong.imkit.activity.CSLeaveMessageActivity.this.finish();
            }

            public void onError(ErrorCode errorCode) {
            }
          });
        }

      }
    });
    cancelBtn.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        io.rong.imkit.activity.CSLeaveMessageActivity.this.hideSoftInputKeyboard();
        io.rong.imkit.activity.CSLeaveMessageActivity.this.finish();
      }
    });
    EventBus.getDefault().register(this);
  }

  private void addItemToContainer(LinearLayout parent) {
    if (this.mItemList != null) {
      for(int i = 0; i < this.mItemList.size(); ++i) {
        CSLMessageItem item = (CSLMessageItem)this.mItemList.get(i);
        LinearLayout itemContainer = new LinearLayout(this);
        LayoutParams params;
        if (item.getType().equals("text")) {
          params = new LayoutParams(-1, RongUtils.dip2px(45.0F));
          itemContainer.setOrientation(LinearLayout.HORIZONTAL);
        } else {
          params = new LayoutParams(-1, -2);
          itemContainer.setOrientation(LinearLayout.VERTICAL);
        }

        if (i > 0) {
          params.setMargins(0, RongUtils.dip2px(1.0F), 0, 0);
        }

        itemContainer.setBackgroundColor(-1);
        itemContainer.setLayoutParams(params);
        TextView view = new TextView(this);
        params = new LayoutParams(-2, RongUtils.dip2px(45.0F));
        params.setMargins(RongUtils.dip2px(14.0F), 0, 0, 0);
        view.setLayoutParams(params);
        view.setTextColor(this.getResources().getColor(R.color.rc_text_color_primary));
        view.setTextSize(16.0F);
        view.setGravity(16);
        view.setText(item.getTitle());
        itemContainer.addView(view);
        EditText editText = new EditText(this);
        params = new LayoutParams(-1, -1);
        editText.setHint(item.getDefaultText());
        editText.setBackgroundColor(0);
        if (item.getType().equals("text")) {
          params.setMargins(RongUtils.dip2px(10.0F), 0, RongUtils.dip2px(14.0F), 0);
          editText.setGravity(19);
          editText.setMaxLines(1);
          editText.setMaxEms(20);
          editText.setSingleLine();
        } else {
          params.setMargins(0, 0, 0, 0);
          editText.setGravity(51);
          editText.setPadding(RongUtils.dip2px(14.0F), 0, 0, 0);
          editText.setInputType(131073);
          editText.setMinLines(3);
          editText.setMaxEms(item.getMax());
          editText.setVerticalScrollBarEnabled(true);
          editText.setMaxLines(3);
          editText.setFilters(new InputFilter[]{new LengthFilter(item.getMax())});
        }

        editText.setTextSize(0, this.getResources().getDimension(R.dimen.rc_conversation_item_name_size));
        editText.setTextColor(this.getResources().getColor(R.color.rc_text_color_primary));
        editText.setTag(item.getName());
        editText.setLayoutParams(params);
        this.mEditList.add(editText);
        itemContainer.addView(editText);
        parent.addView(itemContainer);
      }

    }
  }

  public boolean isContentValid() {
    Iterator var1 = this.mEditList.iterator();

    while(var1.hasNext()) {
      EditText editText = (EditText)var1.next();
      String tag = (String)editText.getTag();
      if (tag == null) {
        RLog.i("CSLeaveMessageActivity", "tag is null !");
        return false;
      }

      CSLMessageItem config = this.getItemConfig(tag);
      if (config == null) {
        RLog.i("CSLeaveMessageActivity", "config is null !");
        return false;
      }

      if (config.isRequired() && TextUtils.isEmpty(editText.getText().toString())) {
        Toast.makeText(this.getBaseContext(), (CharSequence)config.getMessage().get(RemindType.EMPTY.getName()), Toast.LENGTH_SHORT).show();
        return false;
      }

      if (config.getVerification() != null && editText.getText().length() > 0) {
        boolean isValid = true;
        if (config.getVerification().equals("phone")) {
          isValid = this.isMobile(editText.getText().toString());
        } else if (config.getVerification().equals("email")) {
          isValid = this.isEmail(editText.getText().toString());
        }

        if (!isValid) {
          Toast.makeText(this.getBaseContext(), (CharSequence)config.getMessage().get(RemindType.WRONG_FORMAT.getName()), Toast.LENGTH_SHORT).show();
          return false;
        }
      } else if (config.getMax() > 0 && editText.length() > config.getMax()) {
        Toast.makeText(this.getBaseContext(), (CharSequence)config.getMessage().get(RemindType.OVER_LENGTH.getName()), Toast.LENGTH_SHORT).show();
        return false;
      }
    }

    return true;
  }

  public CSLMessageItem getItemConfig(String name) {
    Iterator var2 = this.mItemList.iterator();

    CSLMessageItem item;
    do {
      if (!var2.hasNext()) {
        return null;
      }

      item = (CSLMessageItem)var2.next();
    } while(!item.getName().equals(name));

    return item;
  }

  private boolean isMobile(String phoneNumber) {
    String MOBLIE_PHONE_PATTERN = "^((13[0-9])|(15[0-9])|(18[0-9])|(14[7])|(17[0|6|7|8]))\\d{8}$";
    Pattern p = Pattern.compile(MOBLIE_PHONE_PATTERN);
    Matcher m = p.matcher(phoneNumber);
    return m.matches();
  }

  public boolean isEmail(String email) {
    String str = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
    Pattern p = Pattern.compile(str);
    Matcher m = p.matcher(email);
    return m.matches();
  }

  public final void onEventMainThread(final CSTerminateEvent event) {
    Builder builder = new Builder(this);
    builder.setCancelable(false);
    final AlertDialog alertDialog = builder.create();
    alertDialog.show();
    Window window = alertDialog.getWindow();
    if (window != null) {
      window.setContentView(R.layout.rc_cs_alert_warning);
      TextView tv = (TextView)window.findViewById(R.id.rc_cs_msg);
      tv.setText(event.getText());
      window.findViewById(R.id.rc_btn_ok).setOnClickListener(new OnClickListener() {
        public void onClick(View v) {
          alertDialog.dismiss();
          io.rong.imkit.activity.CSLeaveMessageActivity.this.hideSoftInputKeyboard();
          event.getActivity().finish();
          io.rong.imkit.activity.CSLeaveMessageActivity.this.finish();
        }
      });
    }

  }

  private void hideSoftInputKeyboard() {
    InputMethodManager imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);
    if (imm.isActive() && this.getCurrentFocus() != null && this.getCurrentFocus().getWindowToken() != null) {
      imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 2);
    }

  }

  protected void onDestroy() {
    super.onDestroy();
    EventBus.getDefault().unregister(this);
  }
}
