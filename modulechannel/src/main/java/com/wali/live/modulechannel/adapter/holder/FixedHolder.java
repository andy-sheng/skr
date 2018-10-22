package com.wali.live.modulechannel.adapter.holder;

import android.graphics.Paint;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.CharacterStyle;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import com.common.image.fresco.BaseImageView;
import com.common.image.fresco.FrescoWorker;
import com.common.image.fresco.IFrescoCallBack;
import com.common.image.model.ImageFactory;
import com.common.log.MyLog;
import com.common.utils.U;
import com.facebook.drawee.drawable.ScalingUtils.ScaleType;
import com.wali.live.modulechannel.R;
import com.wali.live.modulechannel.helper.HolderHelper;
import com.wali.live.modulechannel.model.viewmodel.BaseJumpItem;
import com.wali.live.modulechannel.model.viewmodel.ChannelBannerViewModel;
import com.wali.live.modulechannel.model.viewmodel.ChannelLiveGroupViewModel;
import com.wali.live.modulechannel.model.viewmodel.ChannelLiveViewModel;
import com.wali.live.modulechannel.model.viewmodel.ChannelNavigateViewModel;
import com.wali.live.modulechannel.model.viewmodel.ChannelPageHeaderViewModel;
import com.wali.live.modulechannel.model.viewmodel.ChannelRankingViewModel;
import com.wali.live.modulechannel.model.viewmodel.ChannelShowViewModel;
import com.wali.live.modulechannel.model.viewmodel.ChannelTwoTextViewModel;

import java.util.List;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description 固定样式的item，提供ShowViewModel，UserViewModel，LiveViewModel的抽象绑定方法
 */
public abstract class FixedHolder extends HeadHolder {
    public static final int LABEL_HIGHLIGHT_COLOR = R.color.color_ff2966;

    public static final int IMAGE_CORNER_RADIUS = 8;
    protected static int mImageCornerRadius = IMAGE_CORNER_RADIUS;
    protected static int mImageBorderWidth = 0;

    protected int mLabelColor = LABEL_HIGHLIGHT_COLOR;

    public FixedHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void bindView() {
        super.bindView();
        if (mViewModel instanceof ChannelShowViewModel) {
            bindShowModel((ChannelShowViewModel) mViewModel.get());
        } else if (mViewModel instanceof ChannelTwoTextViewModel) {
            bindTwoTextModel((ChannelTwoTextViewModel) mViewModel.get());
        } else if (mViewModel instanceof ChannelUserViewModel) {
            bindUserModel((ChannelUserViewModel) mViewModel.get());
        } else if (mViewModel instanceof ChannelLiveViewModel) {
            bindLiveModel((ChannelLiveViewModel) mViewModel.get());
        } else if (mViewModel instanceof ChannelPageHeaderViewModel) {
            bindPageHeaderModel((ChannelPageHeaderViewModel) mViewModel.get());
        } else if (mViewModel instanceof ChannelNavigateViewModel) {
            bindNavigateModel((ChannelNavigateViewModel) mViewModel.get());
        } else if (mViewModel instanceof ChannelRankingViewModel) {
            bindRankingModel((ChannelRankingViewModel) mViewModel.get());
        } else if(mViewModel instanceof ChannelLiveGroupViewModel){
            bindLiveGroupViewModel((ChannelLiveGroupViewModel)mViewModel.get());
        } else if (mViewModel instanceof ChannelBannerViewModel) {
            bindBannerViewModel((ChannelBannerViewModel)mViewModel.get());
        }
    }

    protected void exposureItem(BaseJumpItem item) {
        if (item == null) {
            return;
        }
        MyLog.d(TAG, "exposure item=" + item.isExposured());
        if (!item.isExposured()) {
            HolderHelper.sendExposureCommand(item);
            item.setIsExposured(true);
        }
    }

    protected void bindRankingModel(ChannelRankingViewModel baseViewModel) {
    }

    protected void bindShowModel(ChannelShowViewModel viewModel) {
    }

    protected void bindTwoTextModel(ChannelTwoTextViewModel viewModel) {
    }

    protected void bindUserModel(ChannelUserViewModel viewModel) {
    }

    protected void bindLiveModel(ChannelLiveViewModel viewModel) {
    }

    protected void bindNavigateModel(ChannelNavigateViewModel viewModel) {
    }

    protected void bindPageHeaderModel(ChannelPageHeaderViewModel viewModel) {

    }

    protected void bindLiveGroupViewModel(ChannelLiveGroupViewModel viewModel) {

    }

    protected void bindBannerViewModel(ChannelBannerViewModel viewModel) {

    }

    protected void bindText(TextView tv, String... texts) {
        if (tv == null) {
            return;
        }
        for (String text : texts) {
            if (!TextUtils.isEmpty(text)) {
                tv.setVisibility(View.VISIBLE);
                tv.setText(text);
                return;
            }
        }
        tv.setVisibility(View.GONE);
    }

    protected void bindTextWithItem(TextView tv, ChannelLiveViewModel.BaseItem item) {
        //Todo-暂时去了
        if (tv == null || item.getUser() == null) {
            return;
        }
//        int level = item.getUser().getLevel();
//        GetConfigManager.LevelItem levelItem = ItemDataFormatUtils.getLevelItem(level);
//        tv.setText(String.valueOf(level));
//        tv.setBackgroundDrawable(levelItem.drawableBG);
    }


    protected void bindImage(BaseImageView iv, String url, boolean isCircle, int width, int height, ScaleType scaleType) {
        bindImageWithCallback(iv, url, isCircle, width, height, scaleType, null);
    }

    protected void bindImageWithCallback(BaseImageView iv, String url, boolean isCircle, int width, int height, ScaleType scaleType, IFrescoCallBack callBack) {
        if (iv == null) {
            return;
        }
        FrescoWorker.loadImage(iv,
                ImageFactory.newHttpImage(url)
                        .setIsCircle(isCircle)
                        .setWidth(width).setHeight(height)
                        .setScaleType(scaleType)
                        .setLoadingDrawable(U.app().getResources().getDrawable(R.color.color_f2f2f2))
                        .setFailureDrawable(U.app().getResources().getDrawable(R.color.color_f2f2f2))
                        .setCallBack(callBack)
                        .build());
    }


    protected void bindImageWithCorner(BaseImageView iv, String url, boolean isCircle, int width, int height, ScaleType scaleType, IFrescoCallBack callBack) {
        if (iv == null) {
            return;
        }

        FrescoWorker.loadImage(iv,
                ImageFactory.newHttpImage(url)
                        .setIsCircle(isCircle)
                        .setWidth(width).setHeight(height)
                        .setScaleType(scaleType)
                        .setLoadingDrawable(U.app().getResources().getDrawable(R.color.color_f2f2f2))
                        .setFailureDrawable(U.app().getResources().getDrawable(R.color.color_f2f2f2))
                        .setCornerRadius(10)
                        .setBorderWidth(mImageBorderWidth)
                        .setCallBack(callBack)
                        .build());
    }

    public static void bindImageWithBorder(BaseImageView iv, String url, boolean isCircle, int width, int height, ScaleType scaleType) {
        if (iv == null) {
            return;
        }
        FrescoWorker.loadImage(iv,
                ImageFactory.newHttpImage(url)
                        .setIsCircle(isCircle)
                        .setWidth(width).setHeight(height)
                        .setScaleType(scaleType)
                        .setLoadingDrawable(U.app().getResources().getDrawable(R.color.color_f2f2f2))
                        .setFailureDrawable(U.app().getResources().getDrawable(R.color.color_f2f2f2))
                        .setCornerRadius(mImageCornerRadius)
                        .setBorderWidth(mImageBorderWidth)
                        .setBorderColor(U.app().getResources().getColor(R.color.color_e5e5e5))
                        .build());
    }

    protected void jumpItem(BaseJumpItem item) {
        // 打点
        HolderHelper.sendClickCommand(item);

        // 增加不是答题直播间的判断
        if (item instanceof ChannelLiveViewModel.LiveItem && (((ChannelLiveViewModel.BaseLiveItem) item).isEnterRoom())
                && !((ChannelLiveViewModel.LiveItem) item).isContestRoom()) {
            int position = ((ChannelLiveViewModel.LiveItem) item).getListPosition();
            if (position != -1) {
                mJumpListener.jumpWatchWithLiveList(position);
                return;
            }
        }
        mJumpListener.jumpScheme(item.getSchemeUri());
    }

    /**
     * label高亮可点击
     */
    protected boolean bindLabel(List<ChannelLiveViewModel.RichText> labels, String oriText, TextView textView) {
        if (oriText == null) {
            oriText = "";
        }
        if (labels != null && !labels.isEmpty()) {
            ChannelLiveViewModel.RichText richText = labels.get(0);
            String text = richText.getText();
            String url = richText.getJumpUrl();
            String totalText = text + oriText;
            SpannableString ss = new SpannableString(totalText);
            ss.setSpan(new LabelClickSpan(url), 0, text.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            textView.setText(ss);
            textView.setMovementMethod(LinkMovementMethod.getInstance());
            return true;
        } else {
            textView.setText(oriText);
            return false;
        }
    }

    public static String parseCountString(int count) {
        String sCount = String.valueOf(count);
        if (count > 10000) {
            String unit = "w";
            sCount = String.format("%.1f" + unit, (float) (count / 10000.0));
        }
        return sCount;
    }

    public class LabelClickSpan extends ClickableSpan {
        String url;

        @Override
        public void onClick(View widget) {
            if (!TextUtils.isEmpty(url)) {
                mJumpListener.jumpScheme(url);
            } else {
                MyLog.e(TAG, "LabelClickSpan onClick url is empty");
            }
        }

        public LabelClickSpan(String url) {
            this.url = url;
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setColor(itemView.getContext().getResources().getColor(mLabelColor));
        }
    }

    public static class FakeBoldSpan extends CharacterStyle {

        @Override
        public void updateDrawState(TextPaint tp) {
//            tp.setFakeBoldText(true);//一种伪粗体效果，比原字体加粗的效果弱一点
            tp.setStyle(Paint.Style.FILL_AND_STROKE);
            tp.setStrokeWidth(0.5f);//控制字体加粗的程度
        }
    }
}
