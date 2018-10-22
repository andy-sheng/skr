package com.wali.live.modulechannel.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.log.MyLog;
import com.wali.live.modulechannel.R;
import com.wali.live.modulechannel.adapter.holder.BannerHolder;
import com.wali.live.modulechannel.adapter.holder.BannerNoSpaceHolder;
import com.wali.live.modulechannel.adapter.holder.BaseHolder;
import com.wali.live.modulechannel.adapter.holder.CommunityRankHolder;
import com.wali.live.modulechannel.adapter.holder.ConcernCardHolder;
import com.wali.live.modulechannel.adapter.holder.DefaultCardHolder;
import com.wali.live.modulechannel.adapter.holder.EffectCardHolder;
import com.wali.live.modulechannel.adapter.holder.FiveCircleHolder;
import com.wali.live.modulechannel.adapter.holder.FiveCircleWithStrokeHolder;
import com.wali.live.modulechannel.adapter.holder.GameCardHolder;
import com.wali.live.modulechannel.adapter.holder.JumpImpl;
import com.wali.live.modulechannel.adapter.holder.LargeCardFloatHeaderHolder;
import com.wali.live.modulechannel.adapter.holder.LargeCardHeadHolder;
import com.wali.live.modulechannel.adapter.holder.LargeCardHolder;
import com.wali.live.modulechannel.adapter.holder.LiveOrVideoCollectionHolder;
import com.wali.live.modulechannel.adapter.holder.MaxFiveCircleHolder;
import com.wali.live.modulechannel.adapter.holder.NavigateHolder;
import com.wali.live.modulechannel.adapter.holder.NavigationListHolder;
import com.wali.live.modulechannel.adapter.holder.NoticeScrollHolder;
import com.wali.live.modulechannel.adapter.holder.OneCardHolder;
import com.wali.live.modulechannel.adapter.holder.OneListHolder;
import com.wali.live.modulechannel.adapter.holder.OneLiveListHolder;
import com.wali.live.modulechannel.adapter.holder.RecommendCardHolder;
import com.wali.live.modulechannel.adapter.holder.SixMakeupHolder;
import com.wali.live.modulechannel.adapter.holder.SplitLineHolder;
import com.wali.live.modulechannel.adapter.holder.StayExposureHolder;
import com.wali.live.modulechannel.adapter.holder.ThreeCardHolder;
import com.wali.live.modulechannel.adapter.holder.ThreeCardLeftBigHolder;
import com.wali.live.modulechannel.adapter.holder.ThreeCardRightBigHolder;
import com.wali.live.modulechannel.adapter.holder.ThreeCircleHolder;
import com.wali.live.modulechannel.adapter.holder.ThreeConcernCardHolder;
import com.wali.live.modulechannel.adapter.holder.ThreeInnerCardHolder;
import com.wali.live.modulechannel.adapter.holder.ThreeOuterCardHolder;
import com.wali.live.modulechannel.adapter.holder.ThreePicHolder;
import com.wali.live.modulechannel.adapter.holder.ThreePicTwoLineHolder;
import com.wali.live.modulechannel.adapter.holder.TopicGridHolder;
import com.wali.live.modulechannel.adapter.holder.TwoCardHolder;
import com.wali.live.modulechannel.adapter.holder.TwoCardWideHolder;
import com.wali.live.modulechannel.adapter.holder.TwoLayerHolder;
import com.wali.live.modulechannel.adapter.holder.TwoLiveGroupCardHolder;
import com.wali.live.modulechannel.adapter.holder.TwoLongCoverHolder;
import com.wali.live.modulechannel.adapter.holder.TwoWideCardHolder;
import com.wali.live.modulechannel.adapter.holder.VariableLengthTagHolder;
import com.wali.live.modulechannel.model.viewmodel.BaseViewModel;
import com.wali.live.modulechannel.model.viewmodel.ChannelUiType;
import com.wali.live.modulechannel.model.viewmodel.ChannelViewModel;
import com.wali.live.modulechannel.view.FoldView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lan on 15-12-3.
 *
 * @module 频道
 * @description 频道view的基本适配器
 */
public class ChannelRecyclerAdapter extends EmptyRecyclerAdapter {
    public static final String TAG = ChannelRecyclerAdapter.class.getSimpleName();

    private List<? extends BaseViewModel> mChannelModels = new ArrayList<>();

    private WeakReference<Activity> mActRef;
    private JumpImpl mJumpImpl;
    private long mChannelId;

    public ChannelRecyclerAdapter(Activity activity) {
        mJumpImpl = new JumpImpl(activity);
    }

    public void setData(List<? extends BaseViewModel> channelModels, long channelId) {
        mChannelModels = channelModels;
        mChannelId = channelId;

        mJumpImpl.setChannelId(mChannelId);
        mJumpImpl.process(mChannelModels);
        notifyDataSetChanged();
    }

    public BaseViewModel getData(int position) {
        if (position < 0 || position >= mChannelModels.size()) {
            return null;
        }
        return mChannelModels.get(position);
    }

    @Override
    protected int getDataCount() {
        return mChannelModels == null ? 0 : mChannelModels.size();
    }

    @Override
    protected int getItemType(int position) {
        ChannelViewModel channelViewModel = mChannelModels.get(position).get();
        return channelViewModel.getUiType();
    }

    @Override
    public BaseHolder onCreateHolder(ViewGroup parent, int viewType) {
        BaseHolder holder = null;
        View view;
        switch (viewType) {
            case ChannelUiType.TYPE_MAX_FIVE_CIRCLE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_max_five_circle_item, parent, false);
                holder = new MaxFiveCircleHolder(view);
                break;
            case ChannelUiType.TYPE_THREE_PIC:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_three_pic_item, parent, false);
                holder = new ThreePicHolder(view);
                break;
            case ChannelUiType.TYPE_FIVE_CIRCLE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_five_circle_with_stroke_item, parent, false);
                holder = new FiveCircleWithStrokeHolder(view);
                break;
            case ChannelUiType.TYPE_TWO_LAYER:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_two_layer_item, parent, false);
                holder = new TwoLayerHolder(view);
                break;
            case ChannelUiType.TYPE_THREE_CIRCLE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_three_circle_item, parent, false);
                holder = new ThreeCircleHolder(view);
                break;
            case ChannelUiType.TYPE_BANNER:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_banner_item, parent, false);
                holder = new BannerHolder(view);
                break;
            case ChannelUiType.TYPE_TWO_CARD:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_two_card_item, parent, false);
                holder = new TwoCardHolder(view);
                break;
            case ChannelUiType.TYPE_SCROLL_CARD:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_three_pic_two_line_item, parent, false);
                holder = new ThreePicTwoLineHolder(view);
                break;
            case ChannelUiType.TYPE_SCROLL_CIRCLE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_five_circle_item, parent, false);
                holder = new FiveCircleHolder(view);
                break;
            case ChannelUiType.TYPE_ONE_CARD:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_one_card_item, parent, false);
                holder = new OneCardHolder(view);
                break;
            case ChannelUiType.TYPE_ONE_CARD_DEFAULT:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_default_card_item, parent, false);
                holder = new DefaultCardHolder(view);
                break;
            case ChannelUiType.TYPE_SIX_CARD:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_six_makeup_item, parent, false);
                holder = new SixMakeupHolder(view);
                break;
            case ChannelUiType.TYPE_CONCERN_CARD:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_concern_card_item, parent, false);
                holder = new ConcernCardHolder(view);
                break;
            case ChannelUiType.TYPE_SPLIT_LINE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_split_line_item, parent, false);
                holder = new SplitLineHolder(view);
                break;
            case ChannelUiType.TYPE_THREE_CARD:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_three_card_item, parent, false);
                holder = new ThreeCardHolder(view);
                break;
            case ChannelUiType.TYPE_ONE_LIST:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_one_list_item, parent, false);
                holder = new OneListHolder(view);
                break;
            case ChannelUiType.TYPE_ONE_LIVE_LIST:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_one_live_list_item, parent, false);
                holder = new OneLiveListHolder(view);
                break;
            case ChannelUiType.TYPE_TWO_WIDE_CARD:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_two_wide_card_item, parent, false);
                holder = new TwoWideCardHolder(view);
                break;
            case ChannelUiType.TYPE_LARGE_CARD:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_large_card_item, parent, false);
                holder = new LargeCardHolder(view);
                break;
            case ChannelUiType.TYPE_NAVIGATE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_navigate_item, parent, false);
                holder = new NavigateHolder(view);
                break;
            case ChannelUiType.TYPE_THREE_NEW:
                // michannel_three_makeup_item & ThreeMakeupHolder
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_three_inner_card_item, parent, false);
                holder = new ThreeInnerCardHolder(view);
                break;
//            case ChannelUiType.TYPE_VIDEO_BANNER:
//                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.michannel_video_banner_item, parent, false);
//                holder = new VideoBannerHolder(view);
//                break;
            case ChannelUiType.TYPE_NOTICE_SCROLL:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_notice_scroll_item, parent, false);
                holder = new NoticeScrollHolder(view);
                break;
            case ChannelUiType.TYPE_THREE_CONCERN_CARD:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_three_concern_card_item, parent, false);
                holder = new ThreeConcernCardHolder(view);
                break;
            case ChannelUiType.TYPE_EFFECT_CARD:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_effect_card_item, parent, false);
                EffectCardHolder effectCardHolder = new EffectCardHolder(view);
                effectCardHolder.enableOffsetAnimation(true);
                holder = effectCardHolder;
                break;
            case ChannelUiType.TYPE_THREE_INNER_CARD:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_three_outer_card_item, parent, false);
                holder = new ThreeOuterCardHolder(view);
                break;
            case ChannelUiType.TYPE_TOPIC_GRID:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_recycler_item, parent, false);
                holder = new TopicGridHolder(view);
                break;
            case ChannelUiType.TYPE_GAME_CARD:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_game_card_item, parent, false);
                holder = new GameCardHolder(view);
                break;
            case ChannelUiType.TYPE_PLACEHOLDER_RANK:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_community_head_layout, parent, false);
                holder = new CommunityRankHolder(view);
                break;
            case ChannelUiType.TYPE_FOUR_LINE_NAVIGATION:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_circle_navigation_item, parent, false);
                holder = new NavigationListHolder(view);
                break;
            case ChannelUiType.TYPE_THREE_CARD_LEFT_BIG:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_three_item_left_big, parent, false);
                holder = new ThreeCardLeftBigHolder(view);
                break;
            case ChannelUiType.TYPE_THREE_CARD_RIGHT_BIG:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_three_item_right_big, parent, false);
                holder = new ThreeCardRightBigHolder(view);
                break;
            case ChannelUiType.TYPE_FLOAT_HEADER:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_large_card_float_header, parent, false);
                holder = new LargeCardFloatHeaderHolder(view);
                break;
            case ChannelUiType.TYPE_HEAD_LARGE_CARD:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_large_card_head_item, parent, false);
                holder = new LargeCardHeadHolder(view);
                break;
            case ChannelUiType.TYPE_VARIABLE_LENGTH_TAG:
                view = new FoldView(parent.getContext());
                holder = new VariableLengthTagHolder(view);
                break;
            case ChannelUiType.TYPE_TWO_LONG_COVER:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_two_long_cover_item, parent, false);
                holder = new TwoLongCoverHolder(view);
                break;
//            case ChannelUiType.TYPE_PAGE_HEADER:
//                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.michannel_page_header_item, parent, false);
//                holder = new ChannelPageHeaderHolder(view);
//                break;
            case ChannelUiType.TYPE_BANNER_NO_SPACE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_banner_no_space_item, parent, false);
                holder = new BannerNoSpaceHolder(view);
                break;
            case ChannelUiType.TYPE_RECOMMEND_CARD:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_recommend_card, parent, false);
                holder = new RecommendCardHolder(view);
                break;
            case ChannelUiType.TYPE_TWO_CARD_WIDE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_live_two_card_wide_item, parent, false);
                holder = new TwoCardWideHolder(view);
                break;
            case ChannelUiType.TYPE_LIVE_GROUP:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_live_group_two_card_item, parent, false);
                holder = new TwoLiveGroupCardHolder(view);
                break;
//            case ChannelUiType.TYPE_ONE_CARD_LIVE:
//                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.michannel_one_card_live_item, parent, false);
//                holder = new OneCardLiveHolder(view);
//                break;
            case ChannelUiType.TYPE_LIVE_OR_VIDEO_COLLECTION:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_two_card_item, parent, false);
                holder = new LiveOrVideoCollectionHolder(view);
                break;
//            case ChannelUiType.TYPE_ONE_SQUARE_CARD:
//                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_one_square_item, parent, false);
//                holder = new OneSquareHolder(view);
//                break;
//            case ChannelUiType.TYPE_LIVE_OR_LIVE_GROUP:
//                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.michannel_two_card_item, parent, false);
//                holder = new LiveOrLiveGroupHolder(view);
//                break;
//            case ChannelUiType.TYPE_ONE_WIDE_CARD:
//                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_one_wide_item, parent, false);
//                holder = new OneWideCardHolder(view);
//                break;
//            case ChannelUiType.TYPE_GAME_WATCH_SINGLE_LIVE:
//                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.michannel_one_game_live_single_item, parent, false);
//                holder = new GameLiveSingleHolder(view);
//                break;
//            case ChannelUiType.TYPE_PLACEHOLDER:
//                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.michannel_place_holder_item, parent, false);
//                holder = new ChannelPlaceHolder(view);
//                break;
            default:
                MyLog.d(TAG, "viewType is : " + viewType);
                break;
        }
        if (holder != null) {
            holder.setJumpListener(mJumpImpl);
            holder.setChannelData(mChannelId);
        }
        return holder;
    }

    @Override
    protected void onBindHolder(BaseHolder holder, int position) {
        if (holder == null) {
            // 此分支不应该进来
            MyLog.e(TAG, "onBindViewHolder error : " + position);
            return;
        }
        holder.bindModel(getDataItem(position), position);
    }

    private BaseViewModel getDataItem(int position) {
        int channelModelPos = position;
        if (channelModelPos < 0 || channelModelPos >= mChannelModels.size()) {
            return null;
        }
        return mChannelModels.get(channelModelPos);
    }


    @Override
    public void onViewAttachedToWindow(BaseHolder holder) {
        MyLog.d(TAG, "onViewAttachedToWindow " + mChannelId);
        if (holder != null && holder instanceof StayExposureHolder) {
            ((StayExposureHolder) holder).onHolderAttached();
        }
    }

    @Override
    public void onViewDetachedFromWindow(BaseHolder holder) {
        MyLog.d(TAG, "onViewDetachedFromWindow " + mChannelId);
        if (holder != null && holder instanceof StayExposureHolder) {
            ((StayExposureHolder) holder).onHolderDetached();
        }
    }
}
