//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.fragment;

import android.content.Intent;
import android.support.v4.app.Fragment;

import java.util.Iterator;

import io.rong.imkit.fragment.UriFragment;

public abstract class DispatchResultFragment extends UriFragment {
    public DispatchResultFragment() {
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        int index = requestCode >> 12;
        if (index != 0) {
            --index;
            Fragment fragment = this.getOffsetFragment(index, this);
            if (fragment != null) {
                fragment.onActivityResult(requestCode & 4095, resultCode, data);
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void startActivityForResult(Fragment fragment, Intent intent, int requestCode) {
        int index = this.getFragmentOffset(0, fragment, this);
        if (index > 15) {
            throw new RuntimeException("DispatchFragment only support 16 fragments。");
        } else if (requestCode == -1) {
            this.startActivityForResult(intent, -1);
        } else if ((requestCode & -4096) != 0) {
            throw new IllegalArgumentException("Can only use lower 12 bits for requestCode");
        } else {
            this.startActivityForResult(intent, (index + 1 << 12) + (requestCode & 4095));
        }
    }

    private int getFragmentOffset(int offset, Fragment targetFragment, Fragment parentFragment) {
        if (parentFragment != null && parentFragment.getChildFragmentManager() != null && parentFragment.getChildFragmentManager().getFragments() != null) {
            Iterator var4 = parentFragment.getChildFragmentManager().getFragments().iterator();
            if (var4.hasNext()) {
                Fragment item = (Fragment) var4.next();
                ++offset;
                return targetFragment == item ? offset : this.getFragmentOffset(offset, targetFragment, item);
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    private Fragment getOffsetFragment(int offset, Fragment fragment) {
        if (offset == 0) {
            return fragment;
        } else {
            Iterator var3 = this.getChildFragmentManager().getFragments().iterator();

            Fragment item;
            do {
                if (!var3.hasNext()) {
                    return null;
                }

                item = (Fragment) var3.next();
                --offset;
                if (offset == 0) {
                    return item;
                }
            }
            while (item.getChildFragmentManager().getFragments() == null || item.getChildFragmentManager().getFragments().size() <= 0);

            return this.getOffsetFragment(offset, item);
        }
    }
}
