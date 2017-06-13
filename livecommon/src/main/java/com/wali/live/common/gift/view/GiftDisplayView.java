package com.wali.live.common.gift.view;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.log.MyLog;
import com.base.utils.language.LocaleUtil;
import com.live.module.common.R;
import com.wali.live.common.gift.adapter.GiftDisplayRecycleViewAdapter;
import com.wali.live.common.gift.presenter.GiftMallPresenter;
import com.wali.live.dao.Gift;
import com.wali.live.common.gift.adapter.viewHolder.GiftDisplayDividerItemDecoration;
import com.base.utils.display.DisplayUtils;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by chengsimin on 16/2/20.
 */
public class GiftDisplayView extends RelativeLayout {

    public static String TAG = "GiftDisplayView";

    private RecyclerView mGiftDisplayRecycleView;

    private TextView mGiftMallItemTips;

    public GiftDisplayView(Context context,GiftDisplayRecycleViewAdapter.GiftItemListener l) {
        super(context);
        init(context,l);
    }

//    @Override
//    protected void dispatchDraw(Canvas canvas) {
//        super.dispatchDraw(canvas);
//        View localView1 = getChildAt(0);
//        int column = getWidth() / localView1.getWidth();
//        int childCount = getChildCount();
//        Paint localPaint;
//        localPaint = new Paint();
//        localPaint.setStyle(Paint.Style.STROKE);
//        localPaint.setColor(getContext().getResources().getColor(R.color.color_white_trans_20));
//
//        View cellView = getChildAt(0);
//        int height = cellView.getHeight();
//        int width = cellView.getWidth();
//        int left = cellView.getLeft();
//        int top = cellView.getTop()-1;
//        int numColumns = getNumColumns();
//        // 画竖线
//        for (int i = 0; i < numColumns + 1; i++) {
//            int x1 = i * width-1;
//            int y1 = top;
//            int x2 = x1;
//            int y2 = top + 2 * height;
//            canvas.drawLine(x1, y1, x2, y2, localPaint);
//        }
//
//        // 画横线
//        for (int i = 0; i < 3; i++) {
//            int x1 = 0;
//            int y1 = top + i * height;
//            int x2 = GlobalData.screenWidth;
//            int y2 = y1;
//            canvas.drawLine(x1, y1, x2, y2, localPaint);
//        }
//    }

    private GiftDisplayRecycleViewAdapter mGiftDisplayRecycleViewAdapter;

    public void init(Context context,GiftDisplayRecycleViewAdapter.GiftItemListener l) {
        inflate(getContext(),R.layout.layout_gift_display_view,this);
        mGiftDisplayRecycleView = (RecyclerView) findViewById(R.id.gift_display_recycleview);

        mGiftMallItemTips = (TextView) findViewById(R.id.gift_mall_item_tips);

        GridLayoutManager fourColumGridManager = new GridLayoutManager(getContext(), 4);
        mGiftDisplayRecycleViewAdapter = new GiftDisplayRecycleViewAdapter(context,false,l);
        mGiftDisplayRecycleView.setAdapter(mGiftDisplayRecycleViewAdapter);
        mGiftDisplayRecycleView.setLayoutManager(fourColumGridManager);
//        mGiftDisplayRecycleView.addItemDecoration(new GiftDisplayDividerItemDecoration(GiftDisplayDividerItemDecoration.GRID_LIST));
        mGiftDisplayRecycleView.setHasFixedSize(true);

//        GridLayoutManager fourColumGridManager = new GridLayoutManager(getContext(), 4);
//        setLayoutManager(fourColumGridManager);
//        adapter = new GiftDisplayRecycleViewAdapter(context,false,l);
//        addItemDecoration(new GiftDisplayDividerItemDecoration(context, GiftDisplayDividerItemDecoration.GRID_LIST));
//        setAdapter(adapter);
    }

    public void setDataSource(List<GiftMallPresenter.GiftWithCard> dataSource) {
        mGiftDisplayRecycleViewAdapter.setData(dataSource);
        mGiftDisplayRecycleViewAdapter.notifyDataSetChanged();
    }


    /**
     * 服务器返回的comment形式为***<\n>****<\n>***
     *
     * @param selectedView
     * @param info
     * @param position
     */
    public void setSelectedGiftInfo(View selectedView, GiftMallPresenter.GiftWithCard info, int position) {
        Gift gift = info.gift;
        //获取选中item的提示信息
        String giftItemTips;
        if(!LocaleUtil.getLanguageCode().equals(LocaleUtil.LOCALE_SIMPLIFIED_CHINESE.toString())){
            giftItemTips = gift.getInternationGiftComment();
        }else {
            giftItemTips = gift.getComment();
        }
//        giftItemTips = "heheh\nhahaha\n你好";//TEST
        //将comment中的<br>替换程\n
//        String giftItemTips = str.replace("<br>", "\n");

        MyLog.d(TAG,"position:"+position+" giftItemTips:"+giftItemTips);
        if(TextUtils.isEmpty(giftItemTips)){
            mGiftMallItemTips.setVisibility(GONE);
            return;
        }
        if(mGiftMallItemTips.getVisibility() != VISIBLE){
            mGiftMallItemTips.setVisibility(VISIBLE);
        }

        mGiftMallItemTips.setText(giftItemTips);

        int left;
        int top;

        if(isChangeGiftMallItemTipsLeftBackGroup(position)){
            mGiftMallItemTips.setBackgroundResource(R.drawable.tips_right_bg);
            left = selectedView.getWidth() + selectedView.getLeft() - DisplayUtils.dip2px(getContext(), 10f);
        }else {
            mGiftMallItemTips.setBackgroundResource(R.drawable.tips_left_bg);
            left = selectedView.getLeft() - DisplayUtils.dip2px(getContext(), 80f);
        }

        MyLog.d(TAG,"mGiftMallItemTips.getMeasuredHeight():"+mGiftMallItemTips.getMeasuredHeight());

        top = selectedView.getTop() + selectedView.getHeight() / 2 - getGiftMallItemTipsHeight(selectedView) / 2;
        MyLog.d(TAG,"top:"+top+" left:"+left);

        RelativeLayout.LayoutParams params = (LayoutParams) mGiftMallItemTips.getLayoutParams();
        params.leftMargin = left;
        params.topMargin = top;

        mGiftMallItemTips.setLayoutParams(params);
    }

    private boolean isChangeGiftMallItemTipsLeftBackGroup(int position) {
        //0 1 4 5 为朝左的背景图
        if(position % 4 <= 1){
            return true;
        }
        return false;
    }

    private int getGiftMallItemTipsHeight(View selectedView) {
        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        mGiftMallItemTips.measure(w, h);
        return mGiftMallItemTips.getMeasuredHeight() < selectedView.getHeight() ?
                mGiftMallItemTips.getMeasuredHeight() : selectedView.getHeight();
    }

    private int getGiftMallItemTipsWidth() {
        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        mGiftMallItemTips.measure(w, h);
        return mGiftMallItemTips.getMeasuredWidth();
    }

    public void clearGiftMallItemTips(){
        if(mGiftMallItemTips.getVisibility() != GONE){
            mGiftMallItemTips.setVisibility(GONE);
        }
    }
}
