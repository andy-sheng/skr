package com.module.playways.grab.room.view;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.common.core.avatar.AvatarUtils;
import com.common.image.fresco.FrescoWorker;
import com.common.image.model.HttpImage;
import com.common.log.MyLog;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.rank.song.model.SongModel;
import com.module.rank.R;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGADynamicEntity;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * 切换房间过场UI
 */
public class GrabChangeRoomTransitionView extends RelativeLayout {
    public final static String TAG = "GrabChangeRoomTransitionView";

    ExTextView mChangeRoomTipTv;
    ExImageView mChangeRoomIv;

    public GrabChangeRoomTransitionView(Context context) {
        super(context);
        init();
    }

    public GrabChangeRoomTransitionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GrabChangeRoomTransitionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.grab_change_room_transition_view, this);
        mChangeRoomTipTv = (ExTextView) this.findViewById(R.id.change_room_tip_tv);
        mChangeRoomIv = (ExImageView) this.findViewById(R.id.change_room_iv);
        setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {

            }
        });
    }


}
