package com.component.report.view;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.base.BaseActivity;
import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.common.view.ex.NoLeakEditText;
import com.component.busilib.R;
import com.component.report.fragment.QuickFeedbackFragment;
import com.respicker.ResPicker;
import com.respicker.activity.ResPickerActivity;
import com.respicker.model.ImageItem;
import com.component.report.adapter.QuickFeedBackAdapter;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

// 与FeedbackFragment中对应 0反馈  1举报  2版权举报
public class FeedbackView extends RelativeLayout {
    public final String TAG = "FeedbackView";

    public static int FEEDBACK_ERRO = 1;  // 反馈问题
    public static int FEEDBACK_SUGGEST = 2; // 功能建议

    int mActionType;

    NoLeakEditText mFeedbackContent;
    ExTextView mContentTextSize;
    ExTextView mSubmitTv;
    ExImageView mSelfSingCatonIv;
    ExImageView mAccNoVoiceIv;
    ExImageView mOtherIv;
    ExImageView mOtherSingCatonIv;
    ExImageView mLyricNoShowIv;
    ExImageView mNewFunIv;
    ExTextView mPicNumTv;
    RecyclerView mRecyclerView;

    QuickFeedBackAdapter mQuickFeedBackAdapter;

    ExImageView[] mSelectIvList;

    List<ImageItem> mImageItemArrayList = new ArrayList<>();

    int mBefore;  // 记录之前的位置
    int[] mType;

    Listener mListener;

    public FeedbackView(Context context) {
        super(context);
    }

    public FeedbackView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FeedbackView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    public void setActionType(int actionType) {
        MyLog.d(TAG, "setActionType" + " actionType=" + actionType);
        mActionType = actionType;
        init();
    }

    private void init() {
        if (mActionType == 0) {
            // 反馈
            inflate(getContext(), R.layout.feedback_view_layout, this);
        } else if (mActionType == 2) {
            // 版权举报
            inflate(getContext(), R.layout.copy_report_view_layout, this);
        } else {
            // 举报
            inflate(getContext(), R.layout.report_view_layout, this);
        }


        mPicNumTv = findViewById(R.id.pic_num_tv);
        mRecyclerView = findViewById(R.id.recycler_view);
        mSelfSingCatonIv = findViewById(R.id.self_sing_caton_iv);
        mAccNoVoiceIv = findViewById(R.id.acc_no_voice_iv);
        mOtherIv = findViewById(R.id.other_iv);
        mOtherSingCatonIv = findViewById(R.id.other_sing_caton_iv);
        mLyricNoShowIv = findViewById(R.id.lyric_no_show_iv);
        mNewFunIv = findViewById(R.id.new_fun_iv);
        mSelectIvList = new ExImageView[]{mSelfSingCatonIv, mAccNoVoiceIv, mOtherIv, mOtherSingCatonIv, mLyricNoShowIv, mNewFunIv};
        mFeedbackContent = findViewById(R.id.feedback_content);
        mContentTextSize = findViewById(R.id.content_text_size);
        mSubmitTv = findViewById(R.id.submit_tv);

        mFeedbackContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mBefore = i;
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int length = editable.length();
                mContentTextSize.setText("" + length + "/200");
                int selectionEnd = mFeedbackContent.getSelectionEnd();
                if (length > 200) {
                    editable.delete(mBefore, selectionEnd);
                    mFeedbackContent.setText(editable.toString());
                    int selection = editable.length();
                    mFeedbackContent.setSelection(selection);
                }
            }
        });

        mSubmitTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mListener != null) {
                    ArrayList<Integer> tags = new ArrayList<>();
                    for (ExImageView imageView : mSelectIvList) {
                        if (imageView.isSelected()) {
                            tags.add(Integer.parseInt((String) imageView.getTag()));
                        }
                    }

                    if (tags.size() == 0) {
                        U.getToastUtil().showShort("请选择顶部的选项哦");
                    } else {
                        mListener.onClickSubmit(tags, mFeedbackContent.getText().toString().trim(), mImageItemArrayList);
                    }
                }
            }
        });

        mQuickFeedBackAdapter = new QuickFeedBackAdapter(new QuickFeedBackAdapter.FeedBackPicManageListener() {
            @Override
            public void addPic() {
                goAddPhotoFragment();
            }

            @Override
            public void deletePic(ImageItem imageItem) {
                mImageItemArrayList.remove(imageItem);
                uploadPhotoList(new ArrayList<ImageItem>(mImageItemArrayList));
            }

            @Override
            public List<ImageItem> getImageItemList() {
                return mImageItemArrayList;
            }
        });

        mRecyclerView.setAdapter(mQuickFeedBackAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        uploadPhotoList(new ArrayList<ImageItem>(mImageItemArrayList));

        Observable.fromArray(mSelectIvList).subscribe(new Consumer<ExImageView>() {
            @Override
            public void accept(ExImageView exImageView) throws Exception {
                exImageView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.setSelected(!v.isSelected());
                    }
                });
            }
        });
    }

    public void uploadPhotoList(List<ImageItem> imageItems) {
        if (imageItems != null) {
            mImageItemArrayList = imageItems;
            mPicNumTv.setText(mImageItemArrayList.size() + "/4");
            ArrayList<ImageItem> imageItemArrayList = new ArrayList<>();
            if (imageItems.size() == 4) {
                imageItemArrayList.addAll(imageItems);
            } else {
                imageItemArrayList.addAll(imageItems);
                ImageItem imageItem = new ImageItem();
                imageItem.setPath("");
                imageItemArrayList.add(imageItem);
            }

            mQuickFeedBackAdapter.setDataList(imageItemArrayList);
            mQuickFeedBackAdapter.notifyDataSetChanged();
        }

    }

    void goAddPhotoFragment() {
        ResPicker.getInstance().setParams(ResPicker.newParamsBuilder()
                .setMultiMode(true)
                .setShowCamera(true)
                .setIncludeGif(false)
                .setCrop(false)
                .setSelectLimit(4)
                .build()
        );

        ResPickerActivity.open((BaseActivity) getContext(), new ArrayList<>(mImageItemArrayList));
    }

    public interface Listener {
        void onClickSubmit(List<Integer> typeList, String content, List<ImageItem> imageItemList);
    }
}
