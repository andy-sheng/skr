package com.wali.live.modulewatch.model.roominfo;

public interface AnchorAction {
    // 房间id
    public String getRoomId();
    public void setRoomId(String mRoomId);

    // 视频流地址
    public String getViewUrl();
    public void setViewUrl(String mViewUrl);

    // 粉丝数
    public int getFansNum();
    public void setFansNum(int fansNum);

    // 星票数
    public void setLiveTicketNum(int liveTicketNum);
    public int getLiveTicketNum();

    public int getPayBarrageGiftId();
}
