package com.module.rankingmode.room;

import android.os.SystemClock;
import com.common.log.MyLog;
import com.module.rankingmode.room.event.RedressGameDevitionEvent;
import org.greenrobot.eventbus.EventBus;

/**
 * 用于游戏管理时间，跟服务器实时矫正数据
 */
public class GameTimeManager {
    public final static String TAG = "GameTimeManager";

    public long gameId = 0;

    //服务器返回的已经过去的时间，最大的已经过去的时间，真正的passtime不是直接用这个，这个只是做更新时候的时间戳
    private long serverGameOverTime = 0;

    //游戏开始的时间，用开机时间记录游戏开始时间
    public volatile long deviationTime = 0;

    private volatile boolean isGameStart = true;

    public void reset(){
        isGameStart = false;
        deviationTime = 0;
        gameId = 0;
    }

    public void init(long gameId){
        MyLog.d(TAG, "init" + " gameId=" + gameId);
        this.gameId = gameId;
    }

    /**
     * 开始游戏的时候已经过了一段时间
     * @param passTime
     */
    public void startGame(long passTime){
        MyLog.d(TAG, "startGame" + " passTime=" + passTime);
        //passTime不能为负数
        if(passTime < 0){
            passTime = 0;
        }

        isGameStart = true;
        deviationTime = SystemClock.elapsedRealtime();
        deviationTime = deviationTime - passTime;
    }

    /**
     * 准确的游戏已经进行的时间
     * @return
     */
    public long getGamePassTime(){
        if(checkGameStart()){
            return SystemClock.elapsedRealtime() - deviationTime;
        }

        return -1;
    }

    /**
     * 得到服务器返回的最新的已经过去的时间，注意！！！ 只是用于更新数据的时间戳
     * @return
     */
    public long getServerGameOverTime() {
        return serverGameOverTime;
    }

    /**
     * 服务器返回的已经过去的时间
     * @param passTime
     */
    public void serverPassTime(long passTime){
        MyLog.d(TAG, "serverPassTime" + " passTime=" + passTime);
        if(checkGameStart()){
            if(passTime < serverGameOverTime){

                return;
            }

            serverGameOverTime = passTime;

            //当前客户端过去的时间
            long clientPassTime = SystemClock.elapsedRealtime() - deviationTime;
            //如果服务器过去的时间大于客户端当前过去的时间，那需要矫正一下
            MyLog.d(TAG, "serverPassTime" + " passTime=" + passTime + ",clientPassTime=" + clientPassTime);
            if(clientPassTime < passTime){
                long deviation = passTime - clientPassTime;
                deviationTime = deviationTime - deviation;
                //如果偏差大于200需要矫正一下
                if(deviation > 200){
                    EventBus.getDefault().post(new RedressGameDevitionEvent());
                }
            }
        }
    }

    public boolean checkGameStart(){
        MyLog.d(TAG, "checkGameStart" );
        if(!isGameStart){
            MyLog.e(TAG, "gameTimeManager is not start");
            return false;
        }

        return true;
    }
}
