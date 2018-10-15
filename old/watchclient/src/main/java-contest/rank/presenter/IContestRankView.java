package rank.presenter;


import com.base.mvp.IRxView;
import rank.model.ContestRankModel;

/**
 * Created by lan on 2018/1/11.
 */
public interface IContestRankView extends IRxView {
    void setContestRank(ContestRankModel model);
}
