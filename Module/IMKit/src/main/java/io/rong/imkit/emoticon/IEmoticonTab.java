//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.emoticon;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;

public interface IEmoticonTab {
    Drawable obtainTabDrawable(Context var1);

    View obtainTabPager(Context var1);

    void onTableSelected(int var1);
}
