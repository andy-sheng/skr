//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.BackStackEntry;

import io.rong.imkit.R;
import io.rong.imkit.RongBaseNoActionbarActivity;
import io.rong.imkit.fragment.FileListFragment;

public class FileListActivity extends RongBaseNoActionbarActivity {
  private int fragmentCount = 0;

  public FileListActivity() {
  }

  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.getWindow().setFlags(2048, 2048);
    this.requestWindowFeature(1);
    this.setContentView(R.layout.rc_ac_file_list);
    if (this.getSupportFragmentManager().findFragmentById(R.id.rc_ac_fl_storage_folder_list_fragment) == null) {
      FileListFragment fileListFragment = new FileListFragment();
      this.showFragment(fileListFragment);
    }

  }

  public void showFragment(Fragment fragment) {
    ++this.fragmentCount;
    this.getSupportFragmentManager().beginTransaction().addToBackStack(this.fragmentCount + "").replace(R.id.rc_ac_fl_storage_folder_list_fragment, fragment).commitAllowingStateLoss();
  }

  public void onBackPressed() {
    if (--this.fragmentCount == 0) {
      FragmentManager fm = this.getSupportFragmentManager();

      for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {
        BackStackEntry entry = fm.getBackStackEntryAt(i);
        Fragment fragment = fm.findFragmentByTag(entry.getName());
        if (fragment != null) {
          fragment.onDestroy();
        }
      }

      this.finish();
    } else {
      super.onBackPressed();
    }

  }
}
