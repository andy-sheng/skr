package com.module.rankingmode.song.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.common.log.MyLog;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;

/**
 * 选择器
 */
public abstract class BaseSelecterView extends FrameLayout {
    public static final String TAG = "BaseSelecterView";
    public View[] mSelecterViewList;
    private OnSelectListener mOnSelectListener;

    public BaseSelecterView(Context context) {
        this(context, null);
    }

    public BaseSelecterView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseSelecterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public abstract OnSelectListener getOnSelectListener();

    public void initSelecterView(View[] viewArrayList, int defaultViewIndex){
        if(viewArrayList == null){
            MyLog.d(TAG, "selecter view array is null");
            return;
        }

        if(viewArrayList.length <= defaultViewIndex){
            MyLog.d(TAG, "index out of bound");
            return;
        }

        mOnSelectListener = getOnSelectListener();

        mSelecterViewList = viewArrayList.clone();

        View defaultSelectView = viewArrayList[defaultViewIndex];
        defaultSelectView.setSelected(true);

        if(mOnSelectListener != null){
            mOnSelectListener.onSelect(defaultSelectView);
        }

        Observable.fromArray(mSelecterViewList).subscribe(new Consumer<View>() {
            @Override
            public void accept(View view) throws Exception {
                view.setOnClickListener(view1 -> {
                    view1.setSelected(true);

                    if(mOnSelectListener != null){
                        mOnSelectListener.onSelect(view1);
                    }

                    unSelectViewList(view1);
                });
            }
        });
    }

    private void unSelectViewList(View selectedView){
        Observable.fromArray(mSelecterViewList)
                .filter(new Predicate<View>() {
                    @Override
                    public boolean test(View view) throws Exception {
                        return view != selectedView;
                    }
                })
                .subscribe(new Consumer<View>() {
            @Override
            public void accept(View view) throws Exception {
                view.setSelected(false);
            }
        });
    }

    public interface OnSelectListener{
        void onSelect(View view);
    }
}
