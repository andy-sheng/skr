package view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.wali.live.watchsdk.R;

/**
 * Created by jiyangli on 18-1-15.
 */
public class ResurrectionView extends LinearLayout {
    Animation rotate;
    Animation animIn;
    Animation animOut;
    ImageView imgAnim;

    public ResurrectionView(Context context) {
        super(context);
        init(context);
    }

    public ResurrectionView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ResurrectionView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.resurrection_layout, this);

        imgAnim = (ImageView) findViewById(R.id.resurrection_layout_imgAnim);

        animIn = (Animation) AnimationUtils.loadAnimation(context, R.anim.scale_0_to_11_to_1);
        animOut = (Animation) AnimationUtils.loadAnimation(context, R.anim.slide_top_out);

        if (rotate == null) {
            rotate = AnimationUtils.loadAnimation(getContext(), R.anim.rotate);
            rotate.setAnimationListener(new Animation.AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    stop();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
        animOut.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                setVisibility(GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void stop() {
        startAnimation(animOut);

    }

    public void start() {
        startAnimation(animIn);
        imgAnim.startAnimation(rotate);
    }

}
