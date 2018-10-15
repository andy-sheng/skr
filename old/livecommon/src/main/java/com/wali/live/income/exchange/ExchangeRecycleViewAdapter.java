package com.wali.live.income.exchange;

import android.graphics.drawable.Drawable;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.base.utils.CommonUtils;
import com.base.utils.display.DisplayUtils;
import com.live.module.common.R;
import com.wali.live.event.EventClass;
import com.wali.live.income.Exchange;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by caoxiangyu on 16-12-15.
 */
public class ExchangeRecycleViewAdapter extends RecyclerView.Adapter<ExchangeRecycleViewAdapter.ExchangeViewHolder> {

    private final String TAG = getTag();
    public boolean isGold = true;

    public int mType;
    private int mSelected = 0;
    private List<Exchange> mExchangeList = new ArrayList<>();

    public void setData(List<Exchange> list, int type) {
        mType = type;
        mExchangeList.clear();
        mExchangeList.addAll(list);
        notifyed();
    }

    public ExchangeRecycleViewAdapter(int type) {
        if (type == 1) {//是银钻就为1
            isGold = false;
        }
    }

    public void notifyed() {
        EventBus.getDefault().post(new EventClass.ItemClickEvent(getClass().getSimpleName()));
        notifyDataSetChanged();
    }

    @Override
    public ExchangeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.recharge_diamond_price_item, parent, false);
        return new ExchangeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ExchangeViewHolder holder, int position) {
        Exchange data = getExchangeData(position);
        if (data == null) {
            return;
        }
        if (position == mSelected) {
            holder.itemView.setSelected(true);
        } else {
            holder.itemView.setSelected(false);
        }
        if (position == 0) {
            ((ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams()).leftMargin = DisplayUtils.dip2px(5);
            ((ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams()).topMargin = GlobalData.app().getResources().getDimensionPixelSize(R.dimen.view_dimen_40);
        }
        holder.itemView.getLayoutParams().width = (DisplayUtils.getScreenWidth() - DisplayUtils.dip2px(32)) / 3;
        holder.itemView.getLayoutParams().height = DisplayUtils.dip2px(50);
        holder.itemView.setTag(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = (int) v.getTag();
                if (mSelected != i) {
                    mSelected = i;
                    notifyed();
                }
            }
        });
        holder.diamondView.setText(String.valueOf(data.getDiamondNum()));
        Drawable nav_up = AppCompatResources.getDrawable(GlobalData.app(), R.drawable.pay_activity_golden_diamond);
        if (ExchangeRecycleViewAdapter.this.mType == BaseExchangeActivity.TYPE_GAME || !isGold) {
            nav_up = AppCompatResources.getDrawable(GlobalData.app(), R.drawable.pay_activity_silver_diamond);
        }
        nav_up.setBounds(0, 0, nav_up.getMinimumWidth(), nav_up.getMinimumHeight());
        holder.diamondView.setCompoundDrawables(null, null, nav_up, null);
        holder.ticketView.setText(GlobalData.app().getString(R.string.exchange_ticket, data.getTicketNum()));
    }

    public int getSelect() {
        return mSelected;
    }

    public void setSelect(int select) {
        mSelected = select;
    }

    public Exchange getExchangeData(int position) {
        if (position < 0 || position >= getItemCount()) {
            return null;
        }
        return mExchangeList.get(position);
    }

    @Override
    public int getItemCount() {
        return mExchangeList.size();
    }

    public String getTag() {
        return getClass().getSimpleName();
    }

    class ExchangeViewHolder extends RecyclerView.ViewHolder {

        public TextView diamondView;
        public TextView ticketView;

        public ExchangeViewHolder(View itemView) {
            super(itemView);
            diamondView = (TextView) itemView.findViewById(R.id.diamond_number);
            ticketView = (TextView) itemView.findViewById(R.id.price);
        }
    }
}



