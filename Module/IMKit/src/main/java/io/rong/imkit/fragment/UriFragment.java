//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import io.rong.imkit.fragment.BaseFragment;

public abstract class UriFragment extends BaseFragment {
    public static final String RONG_URI = "RONG_URI";
    private boolean mViewCreated;
    private Uri mUri;
    private io.rong.imkit.fragment.UriFragment.IActionBarHandler mBarHandler;

    public UriFragment() {
    }

    protected Bundle obtainUriBundle(Uri uri) {
        Bundle args = new Bundle();
        args.putParcelable("RONG_URI", uri);
        return args;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.mUri == null) {
            if (savedInstanceState == null) {
                this.mUri = this.getActivity().getIntent().getData();
            } else {
                this.mUri = (Uri) savedInstanceState.getParcelable("RONG_URI");
            }
        }

        this.mViewCreated = true;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (this.getUri() != null) {
            this.initFragment(this.getUri());
        }

    }

    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("RONG_URI", this.getUri());
        super.onSaveInstanceState(outState);
    }

    public void onRestoreUI() {
        if (this.getUri() != null) {
            this.initFragment(this.getUri());
        }

    }

    public void setActionBarHandler(io.rong.imkit.fragment.UriFragment.IActionBarHandler mBarHandler) {
        this.mBarHandler = mBarHandler;
    }

    protected io.rong.imkit.fragment.UriFragment.IActionBarHandler getActionBarHandler() {
        return this.mBarHandler;
    }

    public Uri getUri() {
        return this.mUri;
    }

    public void setUri(Uri uri) {
        this.mUri = uri;
        if (this.mViewCreated) {
            this.initFragment(uri);
        }

    }

    protected abstract void initFragment(Uri var1);

    public boolean onBackPressed() {
        return false;
    }

    protected interface IActionBarHandler {
        void onTitleChanged(CharSequence var1);

        void onUnreadCountChanged(int var1);
    }
}
