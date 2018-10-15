package com.wali.live.watchsdk.watch.view.watchgameview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.view.panel.BaseBarrageBtnView;

/**
 * Created by zhujianning on 18-8-9.
 */

public class PortraitGameBarregeBtnView extends BaseBarrageBtnView {

    public PortraitGameBarregeBtnView(@NonNull Context context) {
        this(context, null);
    }

    public PortraitGameBarregeBtnView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PortraitGameBarregeBtnView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    private void init(Context context) {
        View rootView = inflate(context, R.layout.portrait_game_barrage_btn_view, this);
        mBarrageBtnViewTv = (TextView) rootView.findViewById(R.id.barrage_btn_view_txt);
        mBarrageBtnViewIv = (ImageView) rootView.findViewById(R.id.barrage_btn_view_img);
        mBarrageBtnViewIv.setEnabled(false);
        mBarrageBtnViewTv.setText(getResources().getString(R.string.input_tips));

//        mBarrageBtnViewTv.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                optStatistics();
//            }
//        });
    }
}
