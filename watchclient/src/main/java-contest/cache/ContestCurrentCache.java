package cache;

import android.text.TextUtils;
import android.util.Pair;

import com.base.log.MyLog;

/**
 * Created by liuyanyan on 2018/1/12.
 *
 * @module 冲顶大会
 * @description 本场缓存数据
 * 本场比赛信息，结束比赛，或者退出房间，清空相关信息
 */
public class ContestCurrentCache {
    private final String TAG = ContestCurrentCache.class.getSimpleName();

    //缓存上一题的答题结果
    private String mSeq;//题目号
    private boolean isCorrect;
    private boolean isUseRevival;
    private String id;

    //全局的是否可以继续答题
    private boolean isContinue;
    private boolean isWatchMode;//观战模式  只有进直播间不能答题才是观战模式
    private Pair<String, Boolean> seqToHasShow;//记录对应场次是否显示过迟到页面
    private boolean isSuccess;//是否通关

    private ContestCurrentCache() {
    }

    private static ContestCurrentCache sInstance = new ContestCurrentCache();

    public static ContestCurrentCache getInstance() {
        return sInstance;
    }

    public String getSeq() {
        return mSeq;
    }

    public void setSeq(String mSeq) {
        this.mSeq = mSeq;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }

    public boolean isUseRevival() {
        return isUseRevival;
    }

    public void setUseRevival(boolean useRevival) {
        isUseRevival = useRevival;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isContinue() {
        return isContinue;
    }

    public void setContinue(boolean aContinue) {
        MyLog.w(TAG, "setContinue = " + aContinue);
        isContinue = aContinue;
    }

    public boolean isWatchMode() {
        return isWatchMode;
    }

    public void setWatchMode(boolean watchMode) {
        isWatchMode = watchMode;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public boolean hasResult() {
        return !TextUtils.isEmpty(mSeq);
    }

    public boolean isNeedShowLateView(String seq) {
        if (!TextUtils.isEmpty(seq)) {
            if (seqToHasShow == null) {
                seqToHasShow = new Pair<>(seq, false);
                return true;
            } else {
                if (seqToHasShow.first.equals(seq)) {
                    if (seqToHasShow.second) {
                        seqToHasShow = new Pair<>(seq, false);
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    seqToHasShow = new Pair<>(seq, false);
                    return true;
                }
            }
        } else {
            return false;
        }
    }

    /*
    * 清除上一题的数据
    * */
    public void clearCache() {
        MyLog.w(TAG, "clearCache");
        mSeq = "";
        isCorrect = false;
        isUseRevival = false;
        id = "";
    }

    public void clearAll(){
        clearCache();
        isContinue = false;
        isWatchMode = false;
        seqToHasShow = null;
        isSuccess = false;
    }

    @Override
    public String toString() {
        return "ContestCurrentCache{" +
                "TAG='" + TAG + '\'' +
                ", mSeq='" + mSeq + '\'' +
                ", isCorrect=" + isCorrect +
                ", isUseRevival=" + isUseRevival +
                ", id='" + id + '\'' +
                ", isContinue=" + isContinue +
                ", isWatchMode=" + isWatchMode +
                '}';
    }
}
