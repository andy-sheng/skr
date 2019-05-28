//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.widget;

import android.support.v4.app.DialogFragment;
import android.view.View;

public class BaseDialogFragment extends DialogFragment {
    public BaseDialogFragment() {
    }

    protected <T extends View> T getView(View view, int id) {
        return view.findViewById(id);
    }
}
