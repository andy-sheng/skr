package com.wali.live.watchsdk.feedback.contact;

/**
 * Created by zhujianning on 18-7-5.
 */

public class ReportContact {

    public interface IView {
        void reportFeedBack(boolean ret);
    }

    public interface IPresenter {
        void sendReport(long targetId
                , int reportType
                , String roomId
                , String liveUrl
                , String reprotPos
                , String commentProof
                , int contentType
                , String otherReason
        );
    }
}
