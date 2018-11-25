//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.widget;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import java.util.List;

import io.rong.imkit.utilities.OptionsPopupDialog;
import io.rong.imkit.utilities.OptionsPopupDialog.OnOptionsItemClickedListener;
import io.rong.imkit.widget.BaseDialogFragment;

/** @deprecated */
@Deprecated
public class ArraysDialogFragment extends BaseDialogFragment {
  private static final String ARGS_ARRAYS = "args_arrays";
  private io.rong.imkit.widget.ArraysDialogFragment.OnArraysDialogItemListener mItemListener;
  private int count;

  public ArraysDialogFragment() {
  }

  public static io.rong.imkit.widget.ArraysDialogFragment newInstance(String title, String[] arrays) {
    io.rong.imkit.widget.ArraysDialogFragment dialogFragment = new io.rong.imkit.widget.ArraysDialogFragment();
    Bundle bundle = new Bundle();
    bundle.putStringArray("args_arrays", arrays);
    dialogFragment.setArguments(bundle);
    return dialogFragment;
  }

  public int getCount() {
    return this.count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public io.rong.imkit.widget.ArraysDialogFragment setArraysDialogItemListener(io.rong.imkit.widget.ArraysDialogFragment.OnArraysDialogItemListener mItemListener) {
    this.mItemListener = mItemListener;
    return this;
  }

  public void show(FragmentManager manager) {
    String[] arrays = this.getArguments().getStringArray("args_arrays");
    this.setCount(arrays.length);
    List<Fragment> fragmentList = manager.getFragments();
    if (fragmentList != null) {
      Fragment fragment = (Fragment)fragmentList.get(0);
      if (fragment != null) {
        Context context = fragment.getActivity();
        if (context != null) {
          OptionsPopupDialog.newInstance(context, arrays).setOptionsPopupDialogListener(new OnOptionsItemClickedListener() {
            public void onOptionsItemClicked(int which) {
              io.rong.imkit.widget.ArraysDialogFragment.this.mItemListener.OnArraysDialogItemClick((DialogInterface)null, which);
            }
          }).show();
        }
      }
    }

  }

  public interface OnArraysDialogItemListener {
    void OnArraysDialogItemClick(DialogInterface var1, int var2);
  }
}
