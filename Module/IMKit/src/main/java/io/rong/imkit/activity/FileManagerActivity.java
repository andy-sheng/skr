//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashSet;

import io.rong.imkit.R;
import io.rong.imkit.RongBaseActivity;
import io.rong.imkit.model.FileInfo;
import io.rong.imkit.utils.FileTypeUtils;

public class FileManagerActivity extends RongBaseActivity implements OnClickListener {
  private static final int REQUEST_FOR_SELECTED_FILES = 730;
  private static final int ALL_FILE_FILES = 1;
  private static final int ALL_VIDEO_FILES = 2;
  private static final int ALL_AUDIO_FILES = 3;
  private static final int ALL_OTHER_FILES = 4;
  private static final int ALL_RAM_FILES = 5;
  private static final int ALL_SD_FILES = 6;
  private static final int ROOT_DIR = 100;
  private static final int SD_CARD_ROOT_DIR = 101;
  private static final int FILE_TRAVERSE_TYPE_ONE = 200;
  private static final int FILE_TRAVERSE_TYPE_TWO = 201;
  private TextView mFileTextView;
  private TextView mVideoTextView;
  private TextView mAudioTextView;
  private TextView mOtherTextView;
  private TextView mMobileMemoryTextView;
  private TextView mSDCardTextView;
  private LinearLayout mSDCardLinearLayout;
  private LinearLayout mSDCardOneLinearLayout;
  private LinearLayout mSDCardTwoLinearLayout;
  private String[] mPath;
  private String mSDCardPath;
  private String mSDCardPathOne;
  private String mSDCardPathTwo;

  public FileManagerActivity() {
  }

  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.rc_ac_file_manager);
    this.mFileTextView = (TextView)this.findViewById(R.id.rc_ac_tv_file_manager_file);
    this.mVideoTextView = (TextView)this.findViewById(R.id.rc_ac_tv_file_manager_video);
    this.mAudioTextView = (TextView)this.findViewById(R.id.rc_ac_tv_file_manager_audio);
    this.mOtherTextView = (TextView)this.findViewById(R.id.rc_ac_tv_file_manager_picture);
    this.mMobileMemoryTextView = (TextView)this.findViewById(R.id.rc_ac_tv_file_manager_mobile_memory);
    this.mSDCardTextView = (TextView)this.findViewById(R.id.rc_ac_tv_file_manager_SD_card);
    this.mSDCardLinearLayout = (LinearLayout)this.findViewById(R.id.rc_ac_ll_sd_card);
    this.mSDCardOneLinearLayout = (LinearLayout)this.findViewById(R.id.rc_ac_ll_sd_card_one);
    this.mSDCardTwoLinearLayout = (LinearLayout)this.findViewById(R.id.rc_ac_ll_sd_card_two);
    this.mFileTextView.setOnClickListener(this);
    this.mVideoTextView.setOnClickListener(this);
    this.mAudioTextView.setOnClickListener(this);
    this.mOtherTextView.setOnClickListener(this);
    this.mMobileMemoryTextView.setOnClickListener(this);
    this.mSDCardTextView.setOnClickListener(this);
    this.mSDCardOneLinearLayout.setOnClickListener(this);
    this.mSDCardTwoLinearLayout.setOnClickListener(this);
    TextView title = (TextView)this.findViewById(R.id.rc_action_bar_title);
    title.setText(R.string.rc_ac_file_send_preview);
    this.mPath = FileTypeUtils.getExternalStorageDirectories(this);
    if (this.mPath.length == 1) {
      this.mSDCardLinearLayout.setVisibility(View.VISIBLE);
      this.mSDCardPath = this.mPath[0];
    }

    if (this.mPath.length == 2) {
      this.mSDCardPathOne = this.mPath[0];
      this.mSDCardPathTwo = this.mPath[1];
      this.mSDCardOneLinearLayout.setVisibility(View.VISIBLE);
      this.mSDCardTwoLinearLayout.setVisibility(View.VISIBLE);
    }

  }

  public void onClick(View v) {
    Intent intent = new Intent(this, FileListActivity.class);
    if (v == this.mFileTextView) {
      intent.putExtra("rootDirType", 100);
      intent.putExtra("fileFilterType", 1);
      intent.putExtra("fileTraverseType", 200);
    }

    if (v == this.mVideoTextView) {
      intent.putExtra("rootDirType", 100);
      intent.putExtra("fileFilterType", 2);
      intent.putExtra("fileTraverseType", 200);
    }

    if (v == this.mAudioTextView) {
      intent.putExtra("rootDirType", 100);
      intent.putExtra("fileFilterType", 3);
      intent.putExtra("fileTraverseType", 200);
    }

    if (v == this.mOtherTextView) {
      intent.putExtra("rootDirType", 100);
      intent.putExtra("fileFilterType", 4);
      intent.putExtra("fileTraverseType", 200);
    }

    if (v == this.mMobileMemoryTextView) {
      intent.putExtra("rootDirType", 100);
      intent.putExtra("fileFilterType", 5);
      intent.putExtra("fileTraverseType", 201);
    }

    if (v == this.mSDCardTextView) {
      intent.putExtra("rootDirType", 101);
      intent.putExtra("fileFilterType", 6);
      intent.putExtra("fileTraverseType", 201);
      intent.putExtra("rootDir", this.mSDCardPath);
    }

    if (v == this.mSDCardOneLinearLayout) {
      intent.putExtra("rootDirType", 101);
      intent.putExtra("fileFilterType", 6);
      intent.putExtra("fileTraverseType", 201);
      intent.putExtra("rootDir", this.mSDCardPathOne);
    }

    if (v == this.mSDCardTwoLinearLayout) {
      intent.putExtra("rootDirType", 101);
      intent.putExtra("fileFilterType", 6);
      intent.putExtra("fileTraverseType", 201);
      intent.putExtra("rootDir", this.mSDCardPathTwo);
    }

    this.startActivityForResult(intent, 730);
  }

  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == 730 && data != null) {
      HashSet<FileInfo> selectedFileInfos = (HashSet)data.getSerializableExtra("selectedFiles");
      Intent intent = new Intent();
      intent.putExtra("sendSelectedFiles", selectedFileInfos);
      this.setResult(-1, intent);
      this.finish();
    }

  }
}
