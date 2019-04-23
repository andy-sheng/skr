package com.module.playways.grab.room.songmanager.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.view.DebounceViewClickListener;
import com.common.view.ex.NoLeakEditText;
import com.dialog.view.StrokeTextView;
import com.module.playways.R;

/**
 * 修改房间名字
 */
public class GrabEditView extends RelativeLayout {

    NoLeakEditText mRoomName;
    StrokeTextView mCancelTv;
    StrokeTextView mSaveTv;

    Listener mListener;


    public GrabEditView(Context context) {
        super(context);
        init();
    }

    public GrabEditView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GrabEditView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.grab_edit_view_layout, this);
        mRoomName = (NoLeakEditText) this.findViewById(R.id.room_name);
        mCancelTv = (StrokeTextView) this.findViewById(R.id.cancel_tv);
        mSaveTv = (StrokeTextView) this.findViewById(R.id.save_tv);

        mCancelTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mListener != null) {
                    mListener.onClickCancel();
                }
            }
        });

        mSaveTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                String name = mRoomName.getText().toString().trim();
                if (mListener != null) {
                    mListener.onClickSave(name);
                }
            }
        });
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    public interface Listener {
        void onClickCancel();

        void onClickSave(String roomName);
    }

}
