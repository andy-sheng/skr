package com.wali.live.watchsdk.income.exchange;

import android.graphics.drawable.Drawable;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.base.utils.display.DisplayUtils;
import com.wali.live.event.EventClass;
import com.wali.live.income.Exchange;
import com.wali.live.watchsdk.R;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by caoxiangyu on 16-12-15.
 */
public class ExchangeRecycleViewAdapter extends RecyclerView.Adapter<ExchangeRecycleViewAdapter.ExchangeViewHolder> {

    private final String TAG = getTag();
    public boolean isGold = true;
    private int mGoldWeight,mSilverWeight;

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
                R.layout.recharge_mixed_diamond_price_item, parent, false);
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
            ((ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams()).rightMargin = DisplayUtils.dip2px(5);
            ((ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams()).topMargin = GlobalData.app().getResources().getDimensionPixelSize(R.dimen.view_dimen_40);
        }
//        holder.itemView.getLayoutParams().width = (DisplayUtils.getScreenWidth() - DisplayUtils.dip2px(40)) / 3;
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

        holder.ticketView.setText(GlobalData.app().getString(R.string.exchange_ticket, data.getTicketNum()));
        if (mType == com.wali.live.income.exchange.BaseExchangeActivity.TYPE_SHOW){
            holder.firstDiamondView.setText(String.valueOf(data.getGoldGemCnt()));
            Drawable goldIcon = AppCompatResources.getDrawable(GlobalData.app(),R.drawable.pay_activity_golden_diamond);
            goldIcon.setBounds(0,0,goldIcon.getMinimumWidth(),goldIcon.getMinimumHeight());
            Drawable silverIcon = AppCompatResources.getDrawable(GlobalData.app(),R.drawable.pay_activity_silver_diamond);
            silverIcon.setBounds(0,0,goldIcon.getMinimumWidth(),goldIcon.getMinimumHeight());
            holder.secondDiamondView.setText(String.valueOf(data.getSilverGemCnt()));
            if(data.getGoldGemCnt() == 0){
                holder.firstDiamondView.setVisibility(View.GONE);
                holder.secondDiamondView.setVisibility(View.VISIBLE);
                holder.ivAdd.setVisibility(View.GONE);
                holder.secondDiamondView.setCompoundDrawables(null,null,silverIcon,null);
            }else if (data.getSilverGemCnt() == 0){
                holder.firstDiamondView.setVisibility(View.VISIBLE);
                holder.secondDiamondView.setVisibility(View.GONE);
                holder.ivAdd.setVisibility(View.GONE);
                holder.firstDiamondView.setCompoundDrawables(null,null,goldIcon,null);
            }else {
                holder.firstDiamondView.setVisibility(View.VISIBLE);
                holder.secondDiamondView.setVisibility(View.VISIBLE);
                holder.ivAdd.setVisibility(View.VISIBLE);
                holder.firstDiamondView.setCompoundDrawables(goldIcon,null,null,null);
                holder.secondDiamondView.setCompoundDrawables(silverIcon,null,null,null);
            }
            return;
        }
        Drawable nav_up;
        if (ExchangeRecycleViewAdapter.this.mType == com.wali.live.income.exchange.BaseExchangeActivity.TYPE_GAME || !isGold) {
            nav_up = AppCompatResources.getDrawable(GlobalData.app(), R.drawable.pay_activity_silver_diamond);
            holder.ivAdd.setVisibility(View.GONE);
            holder.firstDiamondView.setVisibility(View.GONE);
            holder.secondDiamondView.setVisibility(View.VISIBLE);
            holder.secondDiamondView.setText(String.valueOf(data.getDiamondNum()));
        }else {
            nav_up = AppCompatResources.getDrawable(GlobalData.app(), R.drawable.pay_activity_golden_diamond);
            holder.ivAdd.setVisibility(View.GONE);
            holder.firstDiamondView.setVisibility(View.VISIBLE);
            holder.secondDiamondView.setVisibility(View.GONE);
            holder.secondDiamondView.setText(String.valueOf(data.getDiamondNum()));
        }
        nav_up.setBounds(0, 0, nav_up.getMinimumWidth(), nav_up.getMinimumHeight());
        holder.secondDiamondView.setCompoundDrawables(null, null, nav_up, null);
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

        public TextView firstDiamondView;
        public TextView secondDiamondView;
        public ImageView ivAdd;
        public TextView ticketView;

        public ExchangeViewHolder(View itemView) {
            super(itemView);
            firstDiamondView = (TextView) itemView.findViewById(R.id.gold_diamond_number);
            secondDiamondView = (TextView) itemView.findViewById(R.id.silver_diamond_number);
            ivAdd = (ImageView) itemView.findViewById(R.id.iv_add);
            ticketView = (TextView) itemView.findViewById(R.id.price);
        }
    }
}



