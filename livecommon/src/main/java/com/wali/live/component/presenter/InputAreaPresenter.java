package com.wali.live.component.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.base.event.KeyboardEvent;
import com.base.log.MyLog;
import com.mi.live.data.push.SendBarrageManager;
import com.mi.live.data.push.model.BarrageMsg;
import com.mi.live.data.push.model.BarrageMsgType;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.component.ComponentController;
import com.wali.live.component.view.InputAreaView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by yangli on 17/02/20.
 *
 * @module 输入框表现
 */
public class InputAreaPresenter extends ComponentPresenter<InputAreaView.IView>
		implements InputAreaView.IPresenter {
	private static final String TAG = "InputAreaPresenter";

	protected RoomBaseDataModel mMyRoomData;

	public InputAreaPresenter(
			@NonNull IComponentController componentController,
			@NonNull RoomBaseDataModel myRoomData) {
		super(componentController);
		mMyRoomData = myRoomData;
		registerAction(ComponentController.MSG_ON_ORIENTATION);
		registerAction(ComponentController.MSG_ON_BACK_PRESSED);
		registerAction(ComponentController.MSG_CTRL_INPUT_VIEW);
		registerAction(ComponentController.MSG_CTRL_FLY_BARRAGE);
		EventBus.getDefault().register(this);
	}

	@Override
	public void destroy() {
		super.destroy();
		if (EventBus.getDefault().isRegistered(this)) {
			EventBus.getDefault().register(this);
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventMainThread(KeyboardEvent event) {
		MyLog.w(TAG, "KeyboardEvent eventType=" + event.eventType);
		if (mView == null) {
			MyLog.e(TAG, "KeyboardEvent but mView is null");
			return;
		}
		switch (event.eventType) {
			case KeyboardEvent.EVENT_TYPE_KEYBOARD_VISIBLE: // fall through
			case KeyboardEvent.EVENT_TYPE_KEYBOARD_VISIBLE_ALWAYS_SEND:
				int keyboardHeight = Integer.parseInt(String.valueOf(event.obj1));
				mView.onKeyboardShowed(keyboardHeight);
				break;
			case KeyboardEvent.EVENT_TYPE_KEYBOARD_HIDDEN:
				mView.onKeyboardHided();
				break;
		}
	}

	@Override
	public void sendBarrage(String msg, boolean isFlyBarrage) {
		if (TextUtils.isEmpty(msg)) {
			return;
		}
		if (mMyRoomData == null) {
			MyLog.e("sendBarrage but mMyRoomData is null");
			return;
		}
		BarrageMsg barrageMsg = SendBarrageManager.createBarrage(BarrageMsgType.B_MSG_TYPE_TEXT,
				msg, mMyRoomData.getRoomId(), mMyRoomData.getUid(), System.currentTimeMillis(), null);
		SendBarrageManager
				.sendBarrageMessageAsync(barrageMsg)
				.subscribe();
		SendBarrageManager.pretendPushBarrage(barrageMsg);
	}

	@Nullable
	@Override
	protected IAction createAction() {
		return new Action();
	}

	public class Action implements IAction {
		@Override
		public boolean onAction(int source, @Nullable Params params) {
			if (mView == null) {
				return false;
			}
			switch (source) {
				case ComponentController.MSG_ON_ORIENTATION:
					if (params != null) {
						Boolean isLandscape = params.firstItem();
						if (isLandscape != null) {
							mView.onOrientation(isLandscape);
							return true;
						}
					}
					break;
				case ComponentController.MSG_ON_BACK_PRESSED:
					return mView.processBackPress();
				case ComponentController.MSG_CTRL_INPUT_VIEW:
					if (params != null) {
						Boolean isShow = params.firstItem();
						if (isShow != null) {
							return isShow ? mView.showInputView() : mView.hideInputView();
						}
					}
					break;
				case ComponentController.MSG_CTRL_FLY_BARRAGE:
					if (params != null) {
						Boolean isEnable = params.firstItem();
						if (isEnable != null) {
							mView.enableFlyBarrage(isEnable);
							return true;
						}
					}
					break;
				default:
					break;
			}
			return false;
		}
	}
}
