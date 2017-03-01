package com.wali.live.fragment;

import android.os.Bundle;

/**
 * Created by lan on 15-4-20.
 */
public interface FragmentDataListener {
    void onFragmentResult(int requestCode, int resultCode, Bundle bundle);
}
