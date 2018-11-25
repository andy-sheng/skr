//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.fragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import io.rong.imkit.R;

public abstract class BaseFragment extends Fragment implements Callback {
  private static final String TAG = "BaseFragment";
  public static final String TOKEN = "RONG_TOKEN";
  public static final int UI_RESTORE = 1;
  private Handler mHandler;

  public BaseFragment() {
  }

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.mHandler = new Handler(this);
  }

  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
  }

  protected <T extends View> T findViewById(View view, int id) {
    return view.findViewById(id);
  }

  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
  }

  public void onDestroy() {
    super.onDestroy();
  }

  protected Handler getHandler() {
    return this.mHandler;
  }

  public abstract boolean onBackPressed();

  public abstract void onRestoreUI();

  private View obtainView(LayoutInflater inflater, int color, Drawable drawable, CharSequence notice) {
    View view = inflater.inflate(R.layout.rc_wi_notice, (ViewGroup)null);
    ((TextView)view.findViewById(R.id.message)).setText(notice);
    ((ImageView)view.findViewById(R.id.icon)).setImageDrawable(drawable);
    if (color > 0) {
      view.setBackgroundColor(color);
    }

    return view;
  }

  private View obtainView(LayoutInflater inflater, int color, int res, CharSequence notice) {
    View view = inflater.inflate(R.layout.rc_wi_notice, (ViewGroup)null);
    ((TextView)view.findViewById(R.id.message)).setText(notice);
    ((ImageView)view.findViewById(R.id.icon)).setImageResource(res);
    view.setBackgroundColor(color);
    return view;
  }

  public boolean handleMessage(Message msg) {
    switch(msg.what) {
      case 1:
        this.onRestoreUI();
      default:
        return true;
    }
  }
}
