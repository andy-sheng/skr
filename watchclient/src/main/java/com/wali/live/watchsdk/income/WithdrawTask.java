package com.wali.live.watchsdk.income;

import android.os.AsyncTask;

import com.base.log.MyLog;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.api.ErrorCode;
import com.wali.live.event.EventClass;
import com.wali.live.income.exchange.BaseExchangeActivity;
import com.wali.live.proto.MibiTicketProto;
import com.wali.live.proto.PayProto;
import com.wali.live.task.IActionCallBack;
import com.wali.live.task.ITaskCallBack;
import com.wali.live.utils.AsyncTaskUtils;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyanyan on 16/3/1.gs
 */
public class WithdrawTask {
    private static final String TAG = "WithdrawTask";


    public static void getGameExchangeList(final SoftReference<ITaskCallBack> callBack) {
        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {

            List<com.wali.live.income.Exchange> resultList = new ArrayList<>();
            int usableTicketCount = 0;
            int gmaeTicketCount = 0;
            private int errCode;

            @Override
            protected Boolean doInBackground(Void... params) {
                MibiTicketProto.GetGameTicketExchangeResponse rsp = WithdrawManager.pullGameExchangeListSync();
                if (null != rsp) {
                    errCode = rsp.getRetCode();
                    if (errCode == ErrorCode.CODE_SUCCESS) {
                        usableTicketCount = rsp.getUsableTicketCnt();
                        gmaeTicketCount = rsp.getUsableGameTicketCnt();
                        if (null != rsp.getGemExchangeListList()) {
                            for (PayProto.GemExchange gemExchange : rsp.getGemExchangeListList()) {
                                resultList.add(new com.wali.live.income.Exchange(gemExchange));
                            }
                            return true;
                        } else {
                            MyLog.e(TAG, "rsp.getGemExchangeListList is null");
                        }
                    } else {
                        MyLog.e(TAG, "errCode = " + errCode);
                    }
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (null != callBack.get()) {
                    if (result) {
                        //WithdrawManager.updateExchangeListCache(resultList);
                        callBack.get().processWithMore(resultList, usableTicketCount, gmaeTicketCount, BaseExchangeActivity.TYPE_GAME);
                    } else {
                        callBack.get().processWithFailure(errCode);
                    }
                }
            }
        };
        AsyncTaskUtils.exe(task);
    }

    public static void getExchangeList(final SoftReference<ITaskCallBack> callBack) {
        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {

            List<com.wali.live.income.Exchange> resultList = new ArrayList<>();
            int usableTicketCount = 0;
            int gmaeTicketCount = 0;
            private int errCode;

            @Override
            protected Boolean doInBackground(Void... params) {
                PayProto.GetExchangeResponse rsp = WithdrawManager.pullExchangeListSync();
                if (null != rsp) {
                    errCode = rsp.getRetCode();
                    if (errCode == ErrorCode.CODE_SUCCESS) {
                        usableTicketCount = rsp.getUasbleTicketCnt();
                        gmaeTicketCount = rsp.getUsableGameTicketCnt();
                        if (null != rsp.getGemExchangeListList()) {
                            for (PayProto.GemExchange gemExchange : rsp.getGemExchangeListList()) {
                                resultList.add(new com.wali.live.income.Exchange(gemExchange));
                            }
                            return true;
                        } else {
                            MyLog.e(TAG, "rsp.getGemExchangeListList is null");
                        }
                    } else {
                        MyLog.e(TAG, "errCode = " + errCode);
                    }
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (null != callBack.get()) {
                    if (result) {
                        //WithdrawManager.updateExchangeListCache(resultList);
                        callBack.get().processWithMore(resultList, usableTicketCount, gmaeTicketCount, BaseExchangeActivity.TYPE_SHOW);
                    } else {
                        callBack.get().processWithFailure(errCode);
                    }
                }
            }
        };
        AsyncTaskUtils.exe(task);
    }

    public static void getUserProfit(final SoftReference<ITaskCallBack> callBack) {
        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {

            UserProfit profit;
            private int errCode;

            @Override
            protected Boolean doInBackground(Void... params) {
                PayProto.QueryProfitResponse rsp = WithdrawManager.pullUserProfitSync();
                if (null != rsp) {
                    errCode = rsp.getRetCode();
                    if (errCode == ErrorCode.CODE_SUCCESS) {
                        profit = new UserProfit(rsp);
                        return true;
                    } else {
                        MyLog.e(TAG, "errCode = " + errCode);
                    }
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (callBack.get() != null) {
                    if (result) {
                        callBack.get().process(profit);
                    } else {
                        callBack.get().processWithFailure(errCode);
                    }
                }
            }
        };
        AsyncTaskUtils.exe(task);
    }


    @Deprecated
    public static void exchangeDiamond(final SoftReference<ITaskCallBack> callBack, final int exchangeId, final int diamondNum, final int ticketNum, final int giveDiamondNum) {
        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {

            private int errCode;
            private int usableDiamondNum;

            @Override
            protected Boolean doInBackground(Void... params) {
                PayProto.ExchangeGemResponse rsp = WithdrawManager.exchangeDiamondSync(exchangeId, diamondNum, ticketNum, giveDiamondNum);
                if (null != rsp) {
                    errCode = rsp.getRetCode();
                    if (errCode == ErrorCode.CODE_SUCCESS) {
                        usableDiamondNum = rsp.getUsableGemCnt();
                        MyLog.d(TAG, "ExchangeGemResponse = " + rsp);
                        MyUserInfoManager.getInstance().setDiamondNum(usableDiamondNum);
                        EventBus.getDefault().post(new EventClass.WithdrawEvent(EventClass.WithdrawEvent.EVENT_TYPE_ACCOUNT_TICKET_CHANGE));
                        return true;
                    } else {
                        MyLog.e(TAG, "errCode = " + errCode);
                    }
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (null != callBack.get()) {
                    if (result) {
                        callBack.get().process(usableDiamondNum);
                    } else {
                        callBack.get().processWithFailure(errCode);
                    }
                }
            }
        };
        AsyncTaskUtils.exe(task);
    }

    public static void getWithdrawRecords(final WeakReference<IActionCallBack> callBack, final String lastItemId, final int limitId) {
        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {

            private int errCode;
            private List<WithdrawRecordData> ResultList = new ArrayList<>();

            @Override
            protected Boolean doInBackground(Void... params) {
                PayProto.WithdrawRecordResponse rsp = WithdrawManager.withdrawRecordsSync(lastItemId, limitId);
                if (null != rsp) {
                    errCode = rsp.getRetCode();
                    if (errCode == ErrorCode.CODE_SUCCESS) {
                        for (PayProto.WithdrawRecord record : rsp.getWithdrawRecordsList()) {
                            ResultList.add(new WithdrawRecordData(record));
                        }

                        /****************************************/
                        /*WithdrawRecordData w1 = new WithdrawRecordData();
                        w1.setAmount(20);
                        w1.setRecordTime(20160309);
                        w1.setStatus(1);
                        w1.setTicketNum(1);
                        ResultList.add(w1);
                        errCode = ErrorCode.CODE_SUCCESS;*/
                        /****************************************/
                        return true;
                    } else {
                        MyLog.e(TAG, "errCode = " + errCode);
                    }
                } else {
                    MyLog.d(TAG, "reponse ,rsp is null");
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (callBack.get() != null) {
                    if (result) {
                        callBack.get().processAction("", errCode, ResultList);
                    } else {
                        callBack.get().processAction("", errCode);
                    }
                }
            }
        };
        AsyncTaskUtils.exe(task);
    }

}
