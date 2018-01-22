package com.wali.live.watchsdk.contest.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.base.image.fresco.BaseImageView;
import com.base.log.MyLog;
import com.base.mvp.specific.RxRelativeLayout;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.push.model.contest.LastQuestionInfoModel;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.contest.cache.ContestGlobalCache;
import com.wali.live.watchsdk.contest.share.ContestShareHelper;
import com.wali.live.watchsdk.eventbus.EventClass;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by liuyanyan on 2018/1/14.
 */

public class ContestSuccessView extends RxRelativeLayout implements View.OnClickListener {
    private TextView mTitleTv;
    //    private TextView mNickNameTv;
    private TextView mDesTv;
    private TextView mMoneyTv;
    private BaseImageView mAvatarIv;

    private ImageView mCloseIv;
    private TextView mShareTv;

    //分享出去保存图的时候需要用到
    private LastQuestionInfoModel mLastQuestionInfoModel;

    public ContestSuccessView(Context context) {
        super(context);
        init(context);
    }

    public ContestSuccessView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ContestSuccessView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.view_conteset_success, this);
        initContentView();
    }

    private void initContentView() {
        mTitleTv = (TextView) findViewById(R.id.title_tv);
//        mNickNameTv = (TextView) findViewById(R.id.nickname_tv);
        mDesTv = (TextView) findViewById(R.id.desc_tv);
        mMoneyTv = (TextView) findViewById(R.id.money_tv);
        mAvatarIv = (BaseImageView) findViewById(R.id.avatar_iv);

        mShareTv = (TextView) findViewById(R.id.share_tv);
        mShareTv.setOnClickListener(this);

        mCloseIv = (ImageView) findViewById(R.id.close_iv);
        mCloseIv.setOnClickListener(this);

        AvatarUtils.loadAvatarByUidTs(mAvatarIv, MyUserInfoManager.getInstance().getUuid(),
                MyUserInfoManager.getInstance().getAvatar(), AvatarUtils.SIZE_TYPE_AVATAR_MIDDLE, true);
//        mNickNameTv.setText(MyUserInfoManager.getInstance().getNickname());
    }

    public void bindData(LastQuestionInfoModel model) {
        MyLog.w(TAG, "bindData");
        String totalBonus = model.getTotalBonus() > 10000 ? String.format(GlobalData.app().getString(R.string.num_time_wan),
                String.valueOf((int) (model.getTotalBonus() / 10000))) : String.valueOf(model.getTotalBonus());

        mTitleTv.setText(GlobalData.app().getString(R.string.share_total2_bonus,
                model.getWinNum(), String.valueOf(totalBonus)));
        mDesTv.setText(getResources().getString(R.string.contest_success_des, String.valueOf(model.getTotalJoinNum())));
        mMoneyTv.setText(String.valueOf(model.getMyBonus()));
        mLastQuestionInfoModel = model;
        saveContestWinSharePic();
    }

    private String saveContestWinSharePic() {
        return ContestShareHelper.saveContestWinSharePic(MyUserInfoManager.getInstance().getUuid(),
                MyUserInfoManager.getInstance().getAvatar(), MyUserInfoManager.getInstance().getNickname(),
                ContestGlobalCache.getRevivalCode(), mLastQuestionInfoModel);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.close_iv) {
            EventBus.getDefault().post(new EventClass.ShowContestView(EventClass.ShowContestView.TYPE_SUCCESS_VIEW,
                    EventClass.ShowContestView.ACTION_HIDE));
        } else if (i == R.id.share_tv) {
            EventBus.getDefault().post(new EventClass.ShowContestView(EventClass.ShowContestView.TYPE_WIN_SHARE_VIEW,
                    EventClass.ShowContestView.ACTION_SHOW));
            setVisibility(GONE);
        }
    }
}
