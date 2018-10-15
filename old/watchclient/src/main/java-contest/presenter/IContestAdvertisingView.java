package presenter;

import com.base.mvp.IRxView;

/**
 * Created by wanglinzhang on 2018/1/31.
 */

public interface IContestAdvertisingView extends IRxView {
    void getRevivalActSuccess();
    void getRevivalActFailed();
    void addRevivalCardActSuccess(int revivalNum);
    void addRevivalCardActFailed(int retCode);

}
