package com.wali.live.income.net;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.live.module.common.R;
import com.wali.live.proto.PayProto;

/**
 * Created by liuyanyan on 16/3/10.
 */
public class WithdrawRecordData {
    public static final int STATUS_PROCESS = 1;
    public static final int STATUS_SUCCESS = 2;
    public static final int STATUS_FAIL = 3;

    //    private int ticketNum;//消耗了多少票
    private String itemID;//游标
    private int amount;//兑换了多少钱(分)
    private long recordTime;//时间，单位ms
    private int status;//提现状态，1-处理中，2-已经到账，3-提现失败
    private String itemKey;//订单号
    private String statusMsg;//提现状态描述
    private int ortherCurrencyAmount;
    private int withdrawType;


    public WithdrawRecordData() {
    }

    public WithdrawRecordData(PayProto.WithdrawRecord record) {
        if (null != record) {
            this.itemID = record.getItemId();
            this.amount = record.getAmount();
            this.recordTime = record.getTimestamp();
            this.status = record.getStatus();
            this.itemKey = record.getItemKey();
            this.statusMsg = record.getStatusMsg();
            if(record.hasOrtherCurrencyAmount())
                this.ortherCurrencyAmount=record.getOrtherCurrencyAmount();
            if(record.hasWithdrawType()&&record.getWithdrawType()!=null){
                if(record.getWithdrawType().equals(PayProto.WithdrawType.ALIPAY_WITHDRAW)){
                    withdrawType=UserProfit.TYPE_ALI;
                }else if(record.getWithdrawType().equals(PayProto.WithdrawType.WEIXIN_WITHDRAW)){
                    withdrawType=UserProfit.TYPE_WX;
                }else if(record.getWithdrawType().equals(PayProto.WithdrawType.PAYPAL_WITHDRAW)){
                    withdrawType=UserProfit.TYPE_PAYPAL;
                }
            }else{
                withdrawType=UserProfit.TYPE_WX;
            }
        } else {
            MyLog.e("record is null");
        }
    }

//    public int getTicketNum() {
//        return ticketNum;
//    }
//
//    public void setTicketNum(int ticketNum) {
//        this.ticketNum = ticketNum;
//    }

    public int getAmount() {
        return amount;
    }

    public void WsetAmount(int amount) {
        this.amount = amount;
    }

    public long getRecordTime() {
        return recordTime;
    }

    public void setRecordTime(long recordTime) {
        this.recordTime = recordTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setItemID(String itemId) {
        this.itemID = itemId;
    }

    public String getItemID() {
        return itemID;
    }

    public void setItemKey(String itemKey) {
        this.itemKey = itemKey;
    }

    public String getItemKey() {
        return itemKey;
    }

    public int getOrtherCurrencyAmount() {
        return ortherCurrencyAmount;
    }

    public void setOrtherCurrencyAmount(int ortherCurrencyAmount) {
        this.ortherCurrencyAmount = ortherCurrencyAmount;
    }

    public int getWithdrawType() {
        return withdrawType;
    }

    public void setWithdrawType(int withdrawType) {
        this.withdrawType = withdrawType;
    }

    public String getRecordStatusString() {
        switch (status) {
            case STATUS_PROCESS:
                return GlobalData.app().getString(R.string.record_processing);
            case STATUS_SUCCESS:
                return GlobalData.app().getString(R.string.record_success);
            default:
                return GlobalData.app().getString(R.string.record_fail);
        }
    }

    public String getStatusMsg(){
        if(status != STATUS_PROCESS && status != STATUS_SUCCESS){
            return statusMsg;
        } else {
            return "";
        }
    }
}
