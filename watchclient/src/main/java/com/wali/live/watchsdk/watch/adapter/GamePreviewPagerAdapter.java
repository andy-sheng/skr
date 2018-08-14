package com.wali.live.watchsdk.watch.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.base.image.fresco.BaseImageView;
import com.base.view.LazyNewView;
import com.mi.live.data.gamecenter.model.GameInfoModel;
import com.wali.live.watchsdk.watch.view.watchgameview.GamePreviewImageView;

public class GamePreviewPagerAdapter extends PagerAdapter {

    GameInfoModel mGameInfoModel = new GameInfoModel();

    public void setData(GameInfoModel gameInfoModel) {
        this.mGameInfoModel = gameInfoModel;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mGameInfoModel.getGameVideoList().size()
                + mGameInfoModel.getScreenShotList().size();
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == (object);
    }


    @Override
    public void destroyItem(ViewGroup viewGroup, int position, Object arg2) {
        GamePreviewImageView gamePreviewImageView = (GamePreviewImageView) arg2;
        viewGroup.removeView(gamePreviewImageView);
    }

    @Override
    public Object instantiateItem(ViewGroup viewGroup, int position) {
        GamePreviewImageView gamePreviewImageView = new GamePreviewImageView(viewGroup.getContext());
        viewGroup.addView(gamePreviewImageView);
        String imageUrl;
        int videoNum = mGameInfoModel.getGameVideoList().size();
        if(position < videoNum){
            imageUrl = mGameInfoModel.getGameVideoList().get(position).getScreenUrl();
        }else{
            imageUrl = mGameInfoModel.getScreenShotList().get(position - videoNum).getPicUrl();
        }
        gamePreviewImageView.setImageUrl(imageUrl);
        return gamePreviewImageView;
    }

}
