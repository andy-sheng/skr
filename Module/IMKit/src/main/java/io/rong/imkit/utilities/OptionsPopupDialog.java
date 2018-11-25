//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.utilities;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import io.rong.imkit.R;

public class OptionsPopupDialog extends AlertDialog {
  private Context mContext;
  private ListView mListView;
  private String[] arrays;
  private io.rong.imkit.utilities.OptionsPopupDialog.OnOptionsItemClickedListener mItemClickedListener;

  public static io.rong.imkit.utilities.OptionsPopupDialog newInstance(Context context, String[] arrays) {
    io.rong.imkit.utilities.OptionsPopupDialog optionsPopupDialog = new io.rong.imkit.utilities.OptionsPopupDialog(context, arrays);
    return optionsPopupDialog;
  }

  public OptionsPopupDialog(Context context, String[] arrays) {
    super(context);
    this.mContext = context;
    this.arrays = arrays;
  }

  protected void onStart() {
    super.onStart();
    LayoutInflater inflater = (LayoutInflater)this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View view = inflater.inflate(R.layout.rc_dialog_popup_options, (ViewGroup)null);
    this.mListView = (ListView)view.findViewById(R.id.rc_list_dialog_popup_options);
    ArrayAdapter<String> adapter = new ArrayAdapter(this.mContext, R.layout.rc_dialog_popup_options_item, R.id.rc_dialog_popup_item_name, this.arrays);
    this.mListView.setAdapter(adapter);
    this.mListView.setOnItemClickListener(new OnItemClickListener() {
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (io.rong.imkit.utilities.OptionsPopupDialog.this.mItemClickedListener != null) {
          io.rong.imkit.utilities.OptionsPopupDialog.this.mItemClickedListener.onOptionsItemClicked(position);
          io.rong.imkit.utilities.OptionsPopupDialog.this.dismiss();
        }

      }
    });
    this.setContentView(view);
    LayoutParams layoutParams = this.getWindow().getAttributes();
    layoutParams.width = this.getPopupWidth();
    layoutParams.height = -2;
    this.getWindow().setAttributes(layoutParams);
  }

  public io.rong.imkit.utilities.OptionsPopupDialog setOptionsPopupDialogListener(io.rong.imkit.utilities.OptionsPopupDialog.OnOptionsItemClickedListener itemListener) {
    this.mItemClickedListener = itemListener;
    return this;
  }

  private int getPopupWidth() {
    int distanceToBorder = (int)this.mContext.getResources().getDimension(R.dimen.rc_popup_dialog_distance_to_edge);
    return this.getScreenWidth() - 2 * distanceToBorder;
  }

  private int getScreenWidth() {
    return ((WindowManager)((WindowManager)this.mContext.getSystemService(Context.WINDOW_SERVICE))).getDefaultDisplay().getWidth();
  }

  public interface OnOptionsItemClickedListener {
    void onOptionsItemClicked(int var1);
  }
}
