package com.wali.live.watchsdk.component.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.preference.PreferenceUtils;
import com.base.utils.Constants;
import com.base.version.http.HttpUtils;
import com.base.version.http.SimpleRequest;
import com.base.version.http.bean.BasicNameValuePair;
import com.base.version.http.bean.NameValuePair;
import com.mi.live.data.api.LiveManager;
import com.mi.live.data.preference.PreferenceKeys;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.common.statistics.StatisticsAlmightyWorker;
import com.wali.live.component.ComponentController;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.statistics.StatisticsKey;
import com.wali.live.watchsdk.component.view.panel.GameDownloadPanel;
import com.wali.live.watchsdk.component.viewmodel.GameViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by lan on 2017/04/10.
 */
public class GameDownloadPresenter extends ComponentPresenter<GameDownloadPanel.IView>
        implements GameDownloadPanel.IPresenter {
    private static final String TAG = "GameDownloadPresenter";

    private static final String GAME_INFO_URL = "http://app.migc.xiaomi.com/contentapi/m/gameinfo?gameId=%s";
    private static final String EXTRA_GAME_URL = "http://app.migc.xiaomi.com/contentapi/page/json/data/5377";

    private static final long ONE_DAY = 12 * 60 * 60 * 1000;

    private RoomBaseDataModel mMyRoomData;

    private GameViewModel mGameModel;
    private Map<String, String> mExtraMap = new HashMap<>();

    public GameDownloadPresenter(@NonNull IComponentController componentController,
                                 @NonNull RoomBaseDataModel myRoomData) {
        super(componentController);
        mMyRoomData = myRoomData;
        registerAction(ComponentController.MSG_ON_LIVE_SUCCESS);
        registerAction(ComponentController.MSG_SHOW_GAME_DOWNLOAD);
        registerAction(ComponentController.MSG_ON_BACK_PRESSED);
    }

    private void getGameInfo() {
        MyLog.d(TAG, "getGameInfo: gameId=" + mMyRoomData.getGameId());
        if (TextUtils.isEmpty(mMyRoomData.getGameId())) {
            return;
        }
        startGameWork();
    }

    private void startGameWork() {
        Observable
                .create((new Observable.OnSubscribe<GameViewModel>() {
                    @Override
                    public void call(Subscriber<? super GameViewModel> subscriber) {
                        getExtraJson();

                        String url = String.format(GAME_INFO_URL, mMyRoomData.getGameId());
                        List<NameValuePair> postBody = new ArrayList();
                        postBody.add(new BasicNameValuePair("gameId", String.valueOf(mMyRoomData.getGameId())));
                        try {
                            SimpleRequest.StringContent result = HttpUtils.doV2Get(url, postBody);
                            GameViewModel gameModel = new GameViewModel(result.getBody());

                            if (mExtraMap.containsKey(gameModel.getGameId())) {
                                gameModel.setDownloadUrl(mExtraMap.get(gameModel.getGameId()));
                            }
                            subscriber.onNext(gameModel);
                            subscriber.onCompleted();
                        } catch (Exception e) {
                            subscriber.onError(e);
                            return;
                        }
                    }
                }))
                .subscribeOn(Schedulers.io())
                .compose(GameDownloadPresenter.this.<GameViewModel>bindUntilEvent(PresenterEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<GameViewModel>() {
                    @Override
                    public void call(GameViewModel gameModel) {
                        if (gameModel.isValid()) {
                            MyLog.d(TAG, "call: onEvent MSG_BOTTOM_SHOE_GAME_ICON");
                            mGameModel = gameModel;

                            mView.inflate();
                            mComponentController.onEvent(ComponentController.MSG_SHOE_GAME_ICON);

                            StatisticsAlmightyWorker.getsInstance().recordDelayDefault(formatGameIconShowKey(), 1);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, throwable);
                    }
                });
    }

    private void getExtraJson() {
        MyLog.w(TAG, "getExtraJson");
        try {
            long time = PreferenceUtils.getSettingLong(GlobalData.app(), PreferenceKeys.PRE_KEY_GAME_LIST_TIME, 0);
            long current = System.currentTimeMillis();
            if (time == 0 || ((current - time) > ONE_DAY)) {
                getExtraJsonFromServer();
                return;
            }
            String extraJson = PreferenceUtils.getSettingString(GlobalData.app(), PreferenceKeys.PRE_KEY_GAME_LIST, "");
            if (TextUtils.isEmpty(extraJson)) {
                getExtraJsonFromServer();
                return;
            }

            parseExtraJson(extraJson);
        } catch (Exception e) {
            getExtraJsonFromServer();
        }
    }

    private void getExtraJsonFromServer() {
        MyLog.w(TAG, "getExtraJsonFromServer");
        try {
            String url = String.format(EXTRA_GAME_URL);
            List<NameValuePair> postBody = new ArrayList();

            SimpleRequest.StringContent result = HttpUtils.doV2Get(url, postBody);
            String extraJson = result.getBody();

            parseExtraJson(extraJson);
            PreferenceUtils.setSettingString(GlobalData.app(), PreferenceKeys.PRE_KEY_GAME_LIST, extraJson);
            PreferenceUtils.setSettingLong(GlobalData.app(), PreferenceKeys.PRE_KEY_GAME_LIST_TIME, System.currentTimeMillis());
        } catch (Exception e) {
        }
    }

    private void parseExtraJson(String json) throws JSONException {
        MyLog.d(TAG, "parseExtraJson json=" + json);
        JSONObject jsonObject = new JSONObject(json);
        int errCode = jsonObject.optInt("errCode");
        if (errCode == 200) {
            mExtraMap.clear();

            JSONArray array = jsonObject.getJSONArray("list");
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                mExtraMap.put(item.optString("gameId"), item.optString("url"));
            }
        }
    }

    private String formatGameIconShowKey() {
        return String.format(StatisticsKey.KEY_GAME_ICON_SHOW, mMyRoomData.getRoomId(), mGameModel.getName());
    }

    private String formatGameIconClickKey() {
        return String.format(StatisticsKey.KEY_GAME_ICON_CLICK, mMyRoomData.getRoomId(), mGameModel.getName());
    }

    private String formatGameDownloadKey() {
        return String.format(StatisticsKey.KEY_GAME_DOWNLOAD_CLICK, mMyRoomData.getRoomId(), mGameModel.getName());
    }

    private void showGameDownloadView() {
        StatisticsAlmightyWorker.getsInstance().recordDelayDefault(formatGameIconClickKey(), 1);

        mView.showGameDownloadView();
    }

    private void hideGameDownloadView() {
        mView.hideGameDownloadView();
    }

    @Override
    public void destroy() {
        super.destroy();
        mView.destroy();
    }

    @Override
    public GameViewModel getGameModel() {
        return mGameModel;
    }

    @Override
    public void reportDownloadKey() {
        StatisticsAlmightyWorker.getsInstance().recordDelayDefault(formatGameDownloadKey(), 1);
    }

    @Override
    protected IAction createAction() {
        return new Action();
    }

    public class Action implements IAction {
        @Override
        public boolean onAction(int source, @Nullable Params params) {
            if (mView == null) {
                MyLog.e(TAG, "onAction: view is null, source=" + source);
                return false;
            }
            switch (source) {
                case ComponentController.MSG_ON_LIVE_SUCCESS:
                    if (!Constants.isGooglePlayBuild && !Constants.isIndiaBuild) {
                        int liveType = mMyRoomData.getLiveType();
                        MyLog.d(TAG, "live type=" + liveType);
                        if (liveType == LiveManager.TYPE_LIVE_GAME) {
                            getGameInfo();
                        }
                    }
                    break;
                case ComponentController.MSG_SHOW_GAME_DOWNLOAD:
                    showGameDownloadView();
                    break;
                case ComponentController.MSG_ON_BACK_PRESSED:
                    if (mView.isShow()) {
                        hideGameDownloadView();
                        return true;
                    } else {
                        return false;
                    }
                default:
                    break;
            }
            return false;
        }
    }
}
