package view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.base.mvp.specific.RxRelativeLayout;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.eventbus.EventClass;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by liuyanyan on 2018/1/15.
 */
public class ContestFailView extends RxRelativeLayout implements View.OnClickListener {
    private ImageView mCloseIv;
    private TextView mShareTv;
    private TextView mContinueTv;

    public ContestFailView(Context context) {
        super(context);
        init(context);
    }

    public ContestFailView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ContestFailView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.view_contest_fail, this);
        initContentView();
    }

    private void initContentView() {
        mShareTv = (TextView) findViewById(R.id.share_success_tv);
        mShareTv.setOnClickListener(this);

        mCloseIv = (ImageView) findViewById(R.id.close_iv);
        mCloseIv.setOnClickListener(this);

        mContinueTv = (TextView) findViewById(R.id.continue_tv);
        mContinueTv.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.close_iv) {
            EventBus.getDefault().post(new EventClass.ShowContestView(EventClass.ShowContestView.TYPE_FAIL_VIEW, EventClass.ShowContestView.ACTION_HIDE));
        } else if (i == R.id.share_success_tv) {
            EventBus.getDefault().post(new EventClass.ShowContestView(EventClass.ShowContestView.TYPE_INVITE_SHARE_VIEW, EventClass.ShowContestView.ACTION_SHOW));
            setVisibility(GONE);
        } else if (i == R.id.continue_tv) {
            setVisibility(GONE);
        }
    }
}
