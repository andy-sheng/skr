//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.ViewFlipper;

import io.rong.imkit.utilities.LangUtils;
import io.rong.imkit.utilities.PermissionCheckUtil;

public class RongBaseActivity extends Activity {
  private ViewFlipper mContentView;
  protected ViewGroup titleContainer;
  protected ImageView searchButton;
  protected TextView title;

  public RongBaseActivity() {
  }

  protected void attachBaseContext(Context newBase) {
    Context context = LangUtils.getConfigurationContext(newBase);
    super.attachBaseContext(context);
  }

  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    super.setContentView(R.layout.rc_base_activity_layout);
    this.titleContainer = (ViewGroup)this.findViewById(R.id.rc_ac_ll_base_title);
    this.searchButton = (ImageView)this.findViewById(R.id.rc_search);
    this.title = (TextView)this.findViewById(R.id.rc_action_bar_title);
    View back = this.findViewById(R.id.rc_action_bar_back);
    back.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        io.rong.imkit.RongBaseActivity.this.finish();
      }
    });
    this.mContentView = (ViewFlipper)this.findViewById(R.id.rc_base_container);
  }

  public void setContentView(int resId) {
    View view = LayoutInflater.from(this).inflate(resId, (ViewGroup)null);
    LayoutParams lp = new LayoutParams(-1, -1, 1.0F);
    this.mContentView.addView(view, lp);
  }

  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (!PermissionCheckUtil.checkPermissions(this, permissions)) {
      PermissionCheckUtil.showRequestPermissionFailedAlter(this, PermissionCheckUtil.getNotGrantedPermissionMsg(this, permissions, grantResults));
    } else {
      super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

  }
}
