//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.widget.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashSet;
import java.util.List;

import io.rong.imkit.R;
import io.rong.imkit.RongConfigurationManager;
import io.rong.imkit.RongContext;
import io.rong.imkit.model.FileInfo;
import io.rong.imkit.utils.FileTypeUtils;

public class FileListAdapter extends BaseAdapter {
  private List<FileInfo> mFileList;
  private HashSet<FileInfo> mSelectedFiles;
  private Context mContext;

  public FileListAdapter(Context context, List<FileInfo> mFileList, HashSet<FileInfo> mSelectedFiles) {
    this.mFileList = mFileList;
    this.mContext = context;
    this.mSelectedFiles = mSelectedFiles;
  }

  public int getCount() {
    return this.mFileList != null ? this.mFileList.size() : 0;
  }

  public Object getItem(int position) {
    if (this.mFileList == null) {
      return null;
    } else {
      return position >= this.mFileList.size() ? null : this.mFileList.get(position);
    }
  }

  public long getItemId(int position) {
    return (long)position;
  }

  public View getView(int position, View convertView, ViewGroup parent) {
    View view = LayoutInflater.from(this.mContext).inflate(R.layout.rc_wi_file_list_adapter, (ViewGroup)null);
    io.rong.imkit.widget.adapter.FileListAdapter.ViewHolder viewHolder = new io.rong.imkit.widget.adapter.FileListAdapter.ViewHolder();
    viewHolder.fileCheckStateImageView = (ImageView)view.findViewById(R.id.rc_wi_ad_iv_file_check_state);
    viewHolder.fileIconImageView = (ImageView)view.findViewById(R.id.rc_wi_ad_iv_file_icon);
    viewHolder.fileNameTextView = (TextView)view.findViewById(R.id.rc_wi_ad_tv_file_name);
    viewHolder.fileDetailsTextView = (TextView)view.findViewById(R.id.rc_wi_ad_tv_file_details);
    FileInfo file = (FileInfo)this.mFileList.get(position);
    viewHolder.fileNameTextView.setText(file.getFileName());
    if (file.isDirectory()) {
      long filesNumber = file.getFileSize();
      if (filesNumber == 0L) {
        viewHolder.fileDetailsTextView.setText(RongContext.getInstance().getString(R.string.rc_ad_folder_no_files));
      } else {
        viewHolder.fileDetailsTextView.setText(RongContext.getInstance().getString(R.string.rc_ad_folder_files_number, new Object[]{filesNumber}));
      }

      viewHolder.fileIconImageView.setImageResource(FileTypeUtils.getFileIconResource(file));
    } else {
      if (this.mSelectedFiles.contains(file)) {
        viewHolder.fileCheckStateImageView.setImageResource(R.drawable.rc_ad_list_file_checked);
      } else {
        viewHolder.fileCheckStateImageView.setImageResource(R.drawable.rc_ad_list_file_unchecked);
      }

      viewHolder.fileDetailsTextView.setText(RongContext.getInstance().getString(R.string.rc_ad_file_size, new Object[]{FileTypeUtils.formatFileSize(file.getFileSize())}));
      viewHolder.fileIconImageView.setImageResource(FileTypeUtils.getFileIconResource(file));
      if (file.getFileSize() > (long)RongConfigurationManager.getInstance().getFileMaxSize(view.getContext()) * 1024L * 1024L) {
        view.setAlpha(0.4F);
      }
    }

    return view;
  }

  private class ViewHolder {
    ImageView fileCheckStateImageView;
    ImageView fileIconImageView;
    TextView fileNameTextView;
    TextView fileDetailsTextView;

    private ViewHolder() {
    }
  }
}
