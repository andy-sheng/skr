package com.module.playways.songmanager.view;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.NoLeakEditText;
import com.dialog.view.StrokeTextView;
import com.module.playways.R;

/**
 * 修改房间名字
 */
public class GrabEditView extends RelativeLayout {

    NoLeakEditText mRoomName;
    ImageView mClearEditIv;
    StrokeTextView mCancelTv;
    StrokeTextView mSaveTv;

    View mPlaceBottomView;
    View mPlaceTopView;

    Listener mListener;
    String mRoomNameText;


    public GrabEditView(Context context, String roomName) {
        super(context);
        this.mRoomNameText = roomName;
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.grab_edit_view_layout, this);
        mRoomName = (NoLeakEditText) this.findViewById(R.id.room_name);
        mClearEditIv = (ImageView) this.findViewById(R.id.clear_edit_iv);
        mCancelTv = (StrokeTextView) this.findViewById(R.id.cancel_tv);
        mSaveTv = (StrokeTextView) this.findViewById(R.id.save_tv);

        mPlaceBottomView = (View) this.findViewById(R.id.place_bottom_view);
        mPlaceTopView = (View) this.findViewById(R.id.place_top_view);

        ViewGroup.LayoutParams layoutParams = mPlaceBottomView.getLayoutParams();
        layoutParams.height = U.getKeyBoardUtils().getKeyBoardHeight();
        mPlaceBottomView.setLayoutParams(layoutParams);

        if (!TextUtils.isEmpty(mRoomNameText)) {
            mRoomName.setText(mRoomNameText);
            mRoomName.setHint(mRoomNameText);
        }

        mCancelTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mListener != null) {
                    mListener.onClickCancel();
                }
            }
        });

        mPlaceBottomView.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mListener != null) {
                    mListener.onClickCancel();
                }
            }
        });

        mPlaceTopView.setOnClickListener(new DebounceViewClickListener() {
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

        mClearEditIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                mRoomName.setText("");
            }
        });

        mRoomName.postDelayed(new Runnable() {
            @Override
            public void run() {
                String editName = mRoomName.getText().toString().trim();
                if (!TextUtils.isEmpty(editName)) {
                    mRoomName.setSelection(editName.length());
                }
                mRoomName.requestFocus();
            }
        }, 300);

    }

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    public interface Listener {
        void onClickCancel();

        void onClickSave(String roomName);
    }

}
