package com.wali.live.watchsdk.income.records;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.base.utils.CommonUtils;
import com.base.utils.display.DisplayUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.income.records.model.RecordsItem;
import com.wali.live.watchsdk.view.EmptyView;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by zhaomin on 17-6-27.
 */
public class EarningsRecordsAdapter extends RecyclerView.Adapter{

    public static final int STATUS_LOADING = 0;
    public static final int STATUS_NOTHING = 1;
    public static final int STATUS_FAILED = 2;

    private static final int EMPTY_TYPE = 1;
    private static final int RECORDS_TYPE = 2;
    private static final int HEADER_TYPE = 3;
    private static final int FOOTER_TYPE = 4;
    private List<RecordsItem> mData;
    private int mStatus; // 0 加载中 1没有

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public void setData(List<RecordsItem> mData) {
        this.mData = mData;
        notifyDataSetChanged();
    }

    public void clear() {
        if (mData != null) {
            mData.clear();
            notifyDataSetChanged();
        }
    }

    public void setStatus(int mStatus) {
        this.mStatus = mStatus;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup  parent, int viewType) {
        if (viewType == EMPTY_TYPE) {
            EmptyView view = (EmptyView) LayoutInflater.from(GlobalData.app()).inflate(R.layout.empty_view, parent, false);
            return new EmptyViewHolder(view);
        } else  if (viewType == RECORDS_TYPE){
            View view = LayoutInflater.from(GlobalData.app()).inflate(R.layout.profit_record_item, parent, false);
            return new RecordViewHolder(view);
        } else if (viewType == HEADER_TYPE) {
            View view = LayoutInflater.from(GlobalData.app()).inflate(R.layout.records_day_header, parent, false);
            return new DayHeadHolder(view);
        } else if (viewType == FOOTER_TYPE) {
            View view = createFooter(parent.getContext());
            return new FooterHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof RecordViewHolder) {
            ((RecordViewHolder) holder).bind(mData.get(position), position);
        } else if (holder instanceof DayHeadHolder) {
            ((DayHeadHolder) holder).bind(mData.get(position));
        } else if (holder instanceof EmptyViewHolder) {
            ((EmptyViewHolder) holder).bind();
        }
    }

    @Override
    public int getItemCount() {
        if (mData == null || mData.isEmpty()) {
            return 1;
        }
        return mData.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (mData != null && !mData.isEmpty()) {
            if (position < mData.size()) {
                return mData.get(position).ismIsDayHint() ? HEADER_TYPE : RECORDS_TYPE;
            } else {
                return FOOTER_TYPE;
            }
        }
        return EMPTY_TYPE;
    }

    private View createFooter(Context context) {
        LinearLayout linearLayout = new LinearLayout(context);
        ViewGroup.MarginLayoutParams layoutParams = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DisplayUtils.dip2px(48.6F));
        linearLayout.setLayoutParams(layoutParams);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setBackgroundColor(context.getResources().getColor(R.color.color_f2f2f2));
        TextView textView = new TextView(context);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.text_size_44));
        textView.setTextColor(context.getResources().getColor(R.color.color_black_trans_40));
        textView.setText(context.getResources().getString(R.string.all_records_this_month));
        textView.setGravity(Gravity.CENTER);
        linearLayout.addView(textView);
        return linearLayout;
    }

    class RecordViewHolder extends RecyclerView.ViewHolder {

        TextView text;
        TextView numTv;
        View splitLine;

        public RecordViewHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.text);
            numTv = (TextView) itemView.findViewById(R.id.num);
            splitLine = itemView.findViewById(R.id.split_line);
        }

        public void bind(RecordsItem item, int position) {
            text.setText(item.getText());
            String symbol = item.getProfitType() == RecordsItem.PROFIT_TYPE_INCOME ? "+" : "";
            DecimalFormat format = new DecimalFormat(EarningsRecordsFragment.NUMBER_FORMAT);
            String num = format.format(item.getProfitChangeNum() / EarningsRecordsFragment.NUMBER_FORMAT_TIME);
            String numHint = String.format(itemView.getContext().getResources().getQuantityString(R.plurals.records_money_amount, (int) item.getProfitChangeNum() / 10, num));
            numTv.setText(symbol + numHint);
            if (item.getProfitType() == RecordsItem.PROFIT_TYPE_INCOME) {
                numTv.setTextColor(itemView.getContext().getResources().getColor(R.color.color_ffa829));
            } else {
                numTv.setTextColor(itemView.getContext().getResources().getColor(R.color.color_black_trans_50));
            }
            if (item.getSourceType() == RecordsItem.SOURCE_TYPE_WECHAT) {
                Drawable drawable = itemView.getContext().getResources().getDrawable(R.drawable.more_right_arrow_bg);
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getIntrinsicHeight());
                numTv.setCompoundDrawables(null, null, drawable, null);
                numTv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mOnItemClickListener != null) {
                            mOnItemClickListener.onWeChatNumClick();
                        }
                    }
                });
            } else {
                numTv.setCompoundDrawables(null, null, null, null);
                numTv.setOnClickListener(null);
            }

            if (position == getItemCount() - 2) { // 倒数第二个 不要分割线。。。。
                splitLine.setVisibility(View.GONE);
            } else {
                splitLine.setVisibility(View.VISIBLE);
            }
        }
    }

    static class DayHeadHolder extends RecyclerView.ViewHolder {

        TextView dayText;

        public DayHeadHolder(View itemView) {
            super(itemView);
            dayText = (TextView) itemView.findViewById(R.id.text);
        }

        public void bind(RecordsItem item) {
            dayText.setText(item.getDay() + itemView.getContext().getResources().getString(R.string.lable_day));
        }
    }

    public class EmptyViewHolder extends RecyclerView.ViewHolder{

        private EmptyView emptyView;

        public EmptyViewHolder(View itemView){
            super(itemView);
            emptyView = (EmptyView) itemView.findViewById(R.id.empty_view);
        }

        public void bind() {
            int text = 0;
            int drawable = 0;
            if (mStatus == STATUS_NOTHING) {
                text = R.string.no_profit;
                drawable = R.drawable.home_empty_icon;
                emptyView.setOnClickListener(null);
            } else if (mStatus == STATUS_LOADING) {
                text = R.string.loading;
                drawable = R.drawable.home_empty_icon;
                emptyView.setOnClickListener(null);
            } else {
                text = R.string.load_failed_try_again;
                drawable = R.drawable.exchange_renovate;
                emptyView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (CommonUtils.isFastDoubleClick()) {
                            return;
                        }
                        if (mOnItemClickListener != null) {
                            mOnItemClickListener.onClickTryAgain();
                        }
                    }
                });
            }
            emptyView.setEmptyTips(text);
            emptyView.setEmptyDrawable(drawable);
        }
    }

    public static class FooterHolder extends RecyclerView.ViewHolder {

        public FooterHolder(View itemView) {
            super(itemView);
        }
    }

    interface OnItemClickListener {

        /**
         * 点击微信兑换
         */
        void onWeChatNumClick();

        void onClickTryAgain();
    }
}
