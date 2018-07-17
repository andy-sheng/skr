package manager;

import com.base.mvp.IRxView;

/**
 * Created by wanglinzhang on 2018/1/30.
 */

public interface IContestDownloadView extends IRxView {

    void processChanged(int percent);

    void statusChanged(ContestDownloadManager.State status);
}
