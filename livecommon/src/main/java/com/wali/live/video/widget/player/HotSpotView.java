package com.wali.live.video.widget.player;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.base.utils.date.DateTimeUtils;
import com.base.utils.display.DisplayUtils;
import com.live.module.common.R;
import com.wali.live.event.SdkEventClass;
import com.mi.live.data.repository.GiftRepository;
import com.wali.live.base.BaseEvent;
import com.wali.live.dao.Gift;
import com.wali.live.proto.HotSpotProto;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by xzy on 16-10-17.
 */

public class HotSpotView extends LinearLayout {
    private ArrayList<HotSpotProto.HotSpotInfo> spotInfoArrayList = new ArrayList<>();
    private Context mContext;
    private LayoutInflater mInflater;
    private HotspotAdapter mAdapter;
    static PopupWindow popupWindow;

    RecyclerView mRecyclerView;
    ImageView mIvTriangle;
    RelativeLayout mRlTriangle;

    private float[] landScapePosition = new float[2];  //横屏
    private float[] portraitPosition = new float[2];   //竖屏
    private View mView;

    private static final int TOP_MARGIN = -DisplayUtils.dip2px(45f);

    public HotSpotView(Context context) {
        super(context);
        init(context);
    }

    public HotSpotView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public HotSpotView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(SdkEventClass.OrientEvent event) {
        if (event != null) {
            popupWindow.dismiss();
            popupWindow = null;

            Observable.timer(100, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Long>() {
                        @Override
                        public void call(Long aLong) {
                            if (null != mView && mView.getVisibility() == View.VISIBLE) {
                                if (isLandScape(mContext)) {
                                    showHotSpotContent(mContext, mView, landScapePosition[0], landScapePosition[1], spotInfoArrayList);
                                } else {
                                    showHotSpotContent(mContext, mView, portraitPosition[0], portraitPosition[1], spotInfoArrayList);
                                }
                            }
                        }
                    });
        }
    }

    private void init(Context context) {
        inflate(context, R.layout.hotspot_recyclor_layout, this);
        mContext = context;
        mInflater = LayoutInflater.from(context);

        mRecyclerView = $(R.id.hotspot_rylv);
        mIvTriangle = $(R.id.iv_hotspot_triangle);
        mRlTriangle = $(R.id.rl_hotspot_triangle);
        ButterKnife.bind(this);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mAdapter = new HotspotAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(mContext));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }

    public void setData(List<HotSpotProto.HotSpotInfo> hotSpotInfoList, int margin, float[] position, View view) {
        spotInfoArrayList = new ArrayList<>();
        spotInfoArrayList.addAll(hotSpotInfoList);
        mAdapter.notifyDataSetChanged();
        setTriangleMargin(margin);
        setPosition(position);
        mView = view;
    }

    private void setPosition(float[] position) {
        if (isLandScape(mContext)) {
            landScapePosition = position;
            portraitPosition[0] = ((((float) GlobalData.screenWidth - 80) / ((float) GlobalData.screenHeight - 230)) * (position[0] - 40)) + 40;
            portraitPosition[1] = position[1];
        } else {
            portraitPosition = position;
            landScapePosition[0] = ((((float) GlobalData.screenHeight - 230) / ((float) GlobalData.screenWidth - 80)) * (position[0] - 40)) + 40;
            landScapePosition[1] = position[1];
        }
    }

    private void setTriangleMargin(int margin) {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //减去三角形的宽度的一半
        layoutParams.setMargins(margin - 14, 0, 0, 0);//4个参数按顺序分别是左上右下
        mIvTriangle.setLayoutParams(layoutParams);
    }

    public static void hideHotSpot() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
            popupWindow = null;
        }
    }

    class HotspotAdapter extends RecyclerView.Adapter<HotSpotHolder> {

        @Override
        public void onBindViewHolder(HotSpotHolder holder, int position) {
            holder.bindView(spotInfoArrayList.get(position));
        }

        @Override
        public HotSpotHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.hotspot_item_layout, parent, false);
            return new HotSpotHolder(view);
        }

        @Override
        public int getItemCount() {
            return spotInfoArrayList.size();
        }
    }

    class HotSpotHolder extends RecyclerView.ViewHolder {
        TextView hotSpotName;
        TextView hotSpotTime;
        View rootView;

        public HotSpotHolder(View view) {
            super(view);
            hotSpotName = (TextView) view.findViewById(R.id.tv_hotspot_name);
            hotSpotTime = (TextView) view.findViewById(R.id.tv_hotspot_time);
            rootView = view;
        }

        public void bindView(final HotSpotProto.HotSpotInfo hotSpotInfo) {
            setHotSpotContent(hotSpotInfo, hotSpotName);
            hotSpotTime.setText(DateTimeUtils.formatTimeStringForDate(hotSpotInfo.getHotTimeOffset(), "mm:ss"));
            rootView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideHotSpot();
                    EventBus.getDefault().post(new BaseEvent.UserActionEvent(BaseEvent.UserActionEvent.EVENT_TYPE_CLICK_HOTSPOT, hotSpotInfo.getHotTimeOffset(), null));
                }
            });

        }
    }


    public class SimpleDividerItemDecoration extends RecyclerView.ItemDecoration {
        private Drawable mDivider;

        public SimpleDividerItemDecoration(Context context) {
            mDivider = context.getResources().getDrawable(R.drawable.line_divider);
        }

        @Override
        public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
            int left = parent.getPaddingLeft();
            int right = parent.getWidth() - parent.getPaddingRight();

            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = parent.getChildAt(i);

                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

                int top = child.getBottom() + params.bottomMargin;
                int bottom = top + mDivider.getIntrinsicHeight();

                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }
    }

    private static String setHotSpotContent(HotSpotProto.HotSpotInfo hotSpotInfo, TextView textView) {
        SpannableStringBuilder msp = null;
        String str1 = "";

        switch (hotSpotInfo.getType()) {
            case 1:         //连麦
                str1 = GlobalData.app().getString(R.string.invited);
                String str2 = GlobalData.app().getString(R.string.connect_mic);
                msp = new SpannableStringBuilder().append(str1).append(hotSpotInfo.getNickname()).append(str2);
                msp.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), str1.length(), msp.length() - str2.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            case 2:         //音乐
                str1 = GlobalData.app().getString(R.string.share_song);
                msp = new SpannableStringBuilder().append(str1).append(hotSpotInfo.getSong());
                msp.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), str1.length(), msp.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            case 3:         //直播+分享视频
                msp = new SpannableStringBuilder(GlobalData.app().getString(R.string.share_video));
                break;
            case 4:         //直播+分享图片
                msp = new SpannableStringBuilder(GlobalData.app().getString(R.string.share_pic));
                break;
            case 5:         //抽奖
                msp = new SpannableStringBuilder(GlobalData.app().getString(R.string.luck_draw));
                break;
            case 6:         //连接外设
                msp = new SpannableStringBuilder(GlobalData.app().getString(R.string.connect_peripheral));
                break;
            case 7:         //送礼
                Gift gift = GiftRepository.findGiftById(hotSpotInfo.getGiftId());
                msp = new SpannableStringBuilder(GlobalData.app().getString(
                        R.string.user_send_gift_tip, hotSpotInfo.getNickname(),
                        null == gift ? GlobalData.app().getString(R.string.gift) : gift.getInternationalName()
                ));
                msp.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, hotSpotInfo.getNickname().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                msp.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), str1.length() + hotSpotInfo.getNickname().length(), msp.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            case 8:         //直播录屏
            case 9:         //回放录屏   这两个一样
                msp = new SpannableStringBuilder(GlobalData.app().getString(R.string.recorded_video));
                break;
        }

        if (null == msp) return "";
        if (null != textView) {
            textView.setText(msp);
        }

        return msp.toString();
    }

    public static int getMaxHeight(List<HotSpotProto.HotSpotInfo> spotInfoArrayList, Context context) {
        if (null == spotInfoArrayList || spotInfoArrayList.size() == 0 || null == context) return 0;

        int lengh = spotInfoArrayList.size() >= 4 ? 4 : spotInfoArrayList.size();

        float height = 0.0f;
        for (int i = 0; i < lengh; i++) {
            if (measureTextViewHeight(context, spotInfoArrayList.get(i)) == 2) {
                if (i == 3) {
                    height += (58.67f / 2);
                } else {
                    height += 58.67f;
                }
            } else {
                if (i == 3) {
                    height += (42.67f / 2);
                } else {
                    height += 42.67f;
                }
            }
        }

        return DisplayUtils.dip2px(context, height);
    }

    //获取大概高度，内容在3个或3个以内无用
    private static int measureTextViewHeight(Context context, HotSpotProto.HotSpotInfo hotSpotInfo) {
        float width = 0f;
        float timeWidth = 0f;

        TextView textView = new TextView(context);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, DisplayUtils.dip2px(12));
        TextPaint paint = textView.getPaint();
        width = Layout.getDesiredWidth(setHotSpotContent(hotSpotInfo, null), 0, setHotSpotContent(hotSpotInfo, null).length(), paint);
        timeWidth = Layout.getDesiredWidth("14:50", 0, "14:50".length(), paint);

        return width > (720 - DisplayUtils.dip2px(10) * 2 - DisplayUtils.dip2px(16) * 2 - timeWidth - 36) ? 2 : 1;
    }

    public static boolean isShowing() {
        if (popupWindow != null && popupWindow.isShowing()) {
            return true;
        }
        return false;
    }

    public static boolean isLandScape(Context context) {
        if (null == context) return false;
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    public static void showHotSpotContent(final Context context, final View view, final float x, final float y, final List<HotSpotProto.HotSpotInfo> clickNearSpots) {
        if (clickNearSpots == null || clickNearSpots.size() == 0) return;

        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }

        final HotSpotView popupWindow_view = new HotSpotView(context);
        popupWindow = new PopupWindow(popupWindow_view, 720, WindowManager.LayoutParams.WRAP_CONTENT, true);

        if (!popupWindow.isShowing()) {
            int margin = setPopupWindowPosition(popupWindow, context, x, view, getMaxHeight(clickNearSpots, context));

            float[] position = {x, y};
            //先以INVISIBLE画出来，之后再获取高度，然后在显示出来，精准
            popupWindow_view.setVisibility(View.INVISIBLE);
            popupWindow_view.setData(clickNearSpots, margin, position, view);
        }

        Observable.timer(100, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                               @Override
                               public void call(Long aLong) {
                                   if (popupWindow_view.getHeight() <= 0) {
                                       //这种情况一般不发生
                                       hideHotSpot();
                                       return;
                                   }

                                   int height = 0;
                                   RecyclerView recyclerView = (RecyclerView) popupWindow_view.findViewById(R.id.hotspot_rylv);
                                   if (recyclerView == null) return;
                                   if (clickNearSpots.size() > 3) {
                                       for (int i = 0; i < 4; i++) {
                                           View item = recyclerView.getChildAt(i);
                                           if (null == item) { // 有时候这个item为null  但是几率很小，不知道为什么，待解
                                               hideHotSpot();
                                               return;
                                           }

                                           if (i == 3) {
                                               //这是item大于3个的时候的popupWindow的高度，显示三个半
                                               height = height + (item.getHeight() / 2);
                                               break;
                                           }
                                           height = height + item.getHeight();
                                       }
                                   } else {
                                       height = popupWindow_view.getHeight();
                                   }

                                   hideHotSpot();

                                   HotSpotView popupWindow_view2 = new HotSpotView(context);

                                   if (clickNearSpots.size() > 3) {
                                       recyclerView = (RecyclerView) popupWindow_view2.findViewById(R.id.hotspot_rylv);
                                       LayoutParams lp;
                                       lp = (LayoutParams) recyclerView.getLayoutParams();
                                       lp.width = 720;
                                       lp.height = height;
                                       recyclerView.setLayoutParams(lp);
                                   }

                                   if (clickNearSpots.size() > 3) {
                                       popupWindow = new PopupWindow(popupWindow_view2, 720, height + 12, true); // +12是加上小三角的高度
                                   } else {
                                       popupWindow = new PopupWindow(popupWindow_view2, 720, WindowManager.LayoutParams.WRAP_CONTENT, true);
                                   }

                                   popupWindow.setOutsideTouchable(true);
                                   popupWindow.setFocusable(false);
                                   popupWindow.setTouchable(true);

                                   popupWindow.setBackgroundDrawable(new ColorDrawable(0));
                                   popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                                       @Override
                                       public void onDismiss() {
                                           popupWindow.dismiss();
                                       }
                                   });

                                   popupWindow.setTouchInterceptor(new OnTouchListener() {
                                       @Override
                                       public boolean onTouch(View v, MotionEvent event) {
                                           if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                                               popupWindow.dismiss();
                                           }
                                           return false;
                                       }
                                   });

                                   if (!popupWindow.isShowing()) {
                                       int margin2 = setPopupWindowPosition(popupWindow, context, x, view, height);

                                       float[] position2 = {x, y};
                                       popupWindow_view2.setData(clickNearSpots, margin2, position2, view);
                                       popupWindow_view2.setVisibility(View.VISIBLE);
                                   }
                               }
                           }
                );
    }
    //设置popupWindow的位置，以及小三角的位置

    private static int setPopupWindowPosition(PopupWindow popupWindow, Context context, float x, View view, int height) {
        int length;
        if (isLandScape(context)) {
            length = GlobalData.screenHeight;
        } else {
            length = GlobalData.screenWidth;
        }

        int margin;
        if (x < 380) {
            //减去屏幕间距
            margin = (int) x - 20;
            popupWindow.showAsDropDown(view, 20, TOP_MARGIN - height);
        } else if (x > length - 380) {
            //减去屏幕间距和右边剩余部分
            margin = 720 - (length - (int) x - 20);
            popupWindow.showAsDropDown(view, length - 740, TOP_MARGIN - height);
        } else {
            //正常显示
            margin = 360;
            popupWindow.showAsDropDown(view, (int) (x - 360), TOP_MARGIN - height);
        }
        return margin;
    }

    // 不想使用ButterKnife，可以使用下面方法简化代码
    public <V extends View> V $(int id) {
        return (V) findViewById(id);
    }
}