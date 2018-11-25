//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources.NotFoundException;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import io.rong.common.RLog;
import io.rong.common.SystemUtils;
import io.rong.eventbus.EventBus;
import io.rong.imkit.DeleteClickActions;
import io.rong.imkit.IExtensionClickListener;
import io.rong.imkit.IPublicServiceMenuClickListener;
import io.rong.imkit.InputMenu;
import io.rong.imkit.R;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongExtension;
import io.rong.imkit.RongIM;
import io.rong.imkit.RongKitReceiver;
import io.rong.imkit.RongMessageItemLongClickActionManager;
import io.rong.imkit.actions.IClickActions;
import io.rong.imkit.actions.OnMoreActionStateListener;
import io.rong.imkit.manager.AudioPlayManager;
import io.rong.imkit.manager.AudioRecordManager;
import io.rong.imkit.manager.InternalModuleManager;
import io.rong.imkit.manager.SendImageManager;
import io.rong.imkit.manager.UnReadMessageManager;
import io.rong.imkit.mention.RongMentionManager;
import io.rong.imkit.model.ConversationInfo;
import io.rong.imkit.model.Event.CSTerminateEvent;
import io.rong.imkit.model.Event.ConnectEvent;
import io.rong.imkit.model.Event.DraftEvent;
import io.rong.imkit.model.Event.FileMessageEvent;
import io.rong.imkit.model.Event.MessageDeleteEvent;
import io.rong.imkit.model.Event.MessageRecallEvent;
import io.rong.imkit.model.Event.MessageSentStatusUpdateEvent;
import io.rong.imkit.model.Event.MessagesClearEvent;
import io.rong.imkit.model.Event.OnMessageSendErrorEvent;
import io.rong.imkit.model.Event.OnReceiveMessageEvent;
import io.rong.imkit.model.Event.OnReceiveMessageProgressEvent;
import io.rong.imkit.model.Event.PlayAudioEvent;
import io.rong.imkit.model.Event.PublicServiceFollowableEvent;
import io.rong.imkit.model.Event.ReadReceiptEvent;
import io.rong.imkit.model.Event.ReadReceiptRequestEvent;
import io.rong.imkit.model.Event.ReadReceiptResponseEvent;
import io.rong.imkit.model.Event.RemoteMessageRecallEvent;
import io.rong.imkit.model.GroupUserInfo;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.plugin.IPluginModule;
import io.rong.imkit.plugin.location.IUserInfoProvider;
import io.rong.imkit.userInfoCache.RongUserInfoManager;
import io.rong.imkit.utilities.PermissionCheckUtil;
import io.rong.imkit.utilities.PromptPopupDialog;
import io.rong.imkit.utilities.PromptPopupDialog.OnPromptButtonClickedListener;
import io.rong.imkit.widget.AutoRefreshListView;
import io.rong.imkit.widget.AutoRefreshListView.Mode;
import io.rong.imkit.widget.AutoRefreshListView.OnRefreshListener;
import io.rong.imkit.widget.AutoRefreshListView.State;
import io.rong.imkit.widget.CSEvaluateDialog;
import io.rong.imkit.widget.CSEvaluateDialog.EvaluateClickListener;
import io.rong.imkit.widget.SingleChoiceDialog;
import io.rong.imkit.widget.adapter.MessageListAdapter;
import io.rong.imkit.widget.adapter.MessageListAdapter.OnItemHandlerListener;
import io.rong.imkit.widget.adapter.MessageListAdapter.OnMessageCheckedChanged;
import io.rong.imkit.widget.adapter.MessageListAdapter.OnSelectedCountDidExceed;
import io.rong.imkit.widget.provider.EvaluatePlugin;
import io.rong.imkit.widget.provider.MessageItemLongClickAction;
import io.rong.imkit.widget.provider.MessageItemLongClickAction.Builder;
import io.rong.imkit.widget.provider.MessageItemLongClickAction.MessageItemLongClickListener;
import io.rong.imlib.CustomServiceConfig;
import io.rong.imlib.CustomServiceConfig.CSEvaEntryPoint;
import io.rong.imlib.CustomServiceConfig.CSEvaType;
import io.rong.imlib.CustomServiceConfig.CSQuitSuspendType;
import io.rong.imlib.ICustomServiceListener;
import io.rong.imlib.IRongCallback.ISendMediaMessageCallback;
import io.rong.imlib.IRongCallback.ISendMessageCallback;
import io.rong.imlib.MessageTag;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.RongIMClient.ErrorCode;
import io.rong.imlib.RongIMClient.OperationCallback;
import io.rong.imlib.RongIMClient.ResultCallback;
import io.rong.imlib.RongIMClient.SendImageMessageCallback;
import io.rong.imlib.model.CSCustomServiceInfo;
import io.rong.imlib.model.CSGroupItem;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Conversation.ConversationType;
import io.rong.imlib.model.Conversation.PublicServiceType;
import io.rong.imlib.model.CustomServiceMode;
import io.rong.imlib.model.MentionedInfo;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.Message.MessageDirection;
import io.rong.imlib.model.Message.SentStatus;
import io.rong.imlib.model.PublicServiceMenu;
import io.rong.imlib.model.PublicServiceMenu.PublicServiceMenuItemType;
import io.rong.imlib.model.PublicServiceMenuItem;
import io.rong.imlib.model.PublicServiceProfile;
import io.rong.imlib.model.ReadReceiptInfo;
import io.rong.imlib.model.UserInfo;
import io.rong.message.CSPullLeaveMessage;
import io.rong.message.FileMessage;
import io.rong.message.HistoryDividerMessage;
import io.rong.message.ImageMessage;
import io.rong.message.InformationNotificationMessage;
import io.rong.message.LocationMessage;
import io.rong.message.MediaMessageContent;
import io.rong.message.PublicServiceCommandMessage;
import io.rong.message.ReadReceiptMessage;
import io.rong.message.RecallNotificationMessage;
import io.rong.message.TextMessage;
import io.rong.message.VoiceMessage;
import io.rong.push.RongPushClient;

public class ConversationFragment extends UriFragment implements OnScrollListener, IExtensionClickListener, IUserInfoProvider, EvaluateClickListener {
  private static final String TAG = "ConversationFragment";
  protected PublicServiceProfile mPublicServiceProfile;
  private RongExtension mRongExtension;
  private boolean mEnableMention;
  private float mLastTouchY;
  private boolean mUpDirection;
  private float mOffsetLimit;
  private boolean finishing = false;
  private CSCustomServiceInfo mCustomUserInfo;
  private ConversationInfo mCurrentConversationInfo;
  private String mDraft;
  private static final int REQUEST_CODE_ASK_PERMISSIONS = 100;
  private static final int REQUEST_CODE_LOCATION_SHARE = 101;
  private static final int REQUEST_CS_LEAVEL_MESSAGE = 102;
  public static final int SCROLL_MODE_NORMAL = 1;
  public static final int SCROLL_MODE_TOP = 2;
  public static final int SCROLL_MODE_BOTTOM = 3;
  private static final int DEFAULT_HISTORY_MESSAGE_COUNT = 10;
  private static final int DEFAULT_REMOTE_MESSAGE_COUNT = 10;
  private static final int TIP_DEFAULT_MESSAGE_COUNT = 2;
  private static final int SHOW_UNREAD_MESSAGE_COUNT = 10;
  private static final String UN_READ_COUNT = "unReadCount";
  private static final String LIST_STATE = "listState";
  private static final String NEW_MESSAGE_COUNT = "newMessageCount";
  private String mTargetId;
  private ConversationType mConversationType;
  private long indexMessageTime;
  private boolean mReadRec;
  private boolean mSyncReadStatus;
  private int mNewMessageCount;
  private int mUnReadCount;
  private AutoRefreshListView mList;
  private LinearLayout mUnreadMsgLayout;
  private TextView mUnreadMsgCountTv;
  private ImageButton mNewMessageBtn;
  private TextView mNewMessageTextView;
  private MessageListAdapter mListAdapter;
  private View mMsgListView;
  private LinearLayout mNotificationContainer;
  private boolean mHasMoreLocalMessagesUp = true;
  private boolean mHasMoreLocalMessagesDown = true;
  private int mLastMentionMsgId;
  private long mSyncReadStatusMsgTime;
  private boolean mCSNeedToQuit = false;
  private List<String> mLocationShareParticipants;
  private CustomServiceConfig mCustomServiceConfig;
  private CSEvaluateDialog mEvaluateDialg;
  private RongKitReceiver mKitReceiver;
  private MessageItemLongClickAction clickAction;
  private OnMoreActionStateListener moreActionStateListener;
  private Bundle mSavedInstanceState;
  private Parcelable mListViewState;
  private final int CS_HUMAN_MODE_CUSTOMER_EXPIRE = 0;
  private final int CS_HUMAN_MODE_SEAT_EXPIRE = 1;
  private Message firstUnreadMessage;
  private OnScrollListener mOnScrollListener = new OnScrollListener() {
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
      if (io.rong.imkit.fragment.ConversationFragment.this.mUnreadMsgLayout.getVisibility() == View.VISIBLE && io.rong.imkit.fragment.ConversationFragment.this.firstUnreadMessage != null) {
        int firstPosition = io.rong.imkit.fragment.ConversationFragment.this.mListAdapter.findPosition((long) io.rong.imkit.fragment.ConversationFragment.this.firstUnreadMessage.getMessageId());
        if (firstVisibleItem <= firstPosition) {
          TranslateAnimation animation = new TranslateAnimation(0.0F, 700.0F, 0.0F, 0.0F);
          animation.setDuration(700L);
          animation.setFillAfter(true);
          io.rong.imkit.fragment.ConversationFragment.this.mUnreadMsgLayout.startAnimation(animation);
          io.rong.imkit.fragment.ConversationFragment.this.mUnreadMsgLayout.setClickable(false);
          io.rong.imkit.fragment.ConversationFragment.this.mList.removeCurrentOnScrollListener();
        }
      }

    }
  };
  private boolean robotType = true;
  private long csEnterTime;
  private boolean csEvaluate = true;
  ICustomServiceListener customServiceListener = new ICustomServiceListener() {
    public void onSuccess(CustomServiceConfig config) {
      io.rong.imkit.fragment.ConversationFragment.this.mCustomServiceConfig = config;
      if (config.isBlack) {
        io.rong.imkit.fragment.ConversationFragment.this.onCustomServiceWarning(io.rong.imkit.fragment.ConversationFragment.this.getString(R.string.rc_blacklist_prompt), false, io.rong.imkit.fragment.ConversationFragment.this.robotType);
      }

      if (config.robotSessionNoEva) {
        io.rong.imkit.fragment.ConversationFragment.this.csEvaluate = false;
        io.rong.imkit.fragment.ConversationFragment.this.mListAdapter.setEvaluateForRobot(true);
      }

      if (io.rong.imkit.fragment.ConversationFragment.this.mRongExtension != null) {
        if (config.evaEntryPoint.equals(CSEvaEntryPoint.EVA_EXTENSION) && io.rong.imkit.fragment.ConversationFragment.this.mCustomServiceConfig != null) {
          io.rong.imkit.fragment.ConversationFragment.this.mRongExtension.addPlugin(new EvaluatePlugin(io.rong.imkit.fragment.ConversationFragment.this.mCustomServiceConfig.isReportResolveStatus));
        }

        if (config.isDisableLocation) {
//          List<IPluginModule> defaultPlugins = ConversationFragment.this.mRongExtension.getPluginModules();
//          IPluginModule location = null;
//
//          for(int ix = 0; ix < defaultPlugins.size(); ++ix) {
//            if (defaultPlugins.get(ix) instanceof DefaultLocationPlugin) {
//              location = (IPluginModule)defaultPlugins.get(ix);
//            }
//          }
//
//          if (location != null) {
//            ConversationFragment.this.mRongExtension.removePlugin(location);
//          }
        }
      }

      if (config.quitSuspendType.equals(CSQuitSuspendType.NONE)) {
        try {
          io.rong.imkit.fragment.ConversationFragment.this.mCSNeedToQuit = RongContext.getInstance().getResources().getBoolean(R.bool.rc_stop_custom_service_when_quit);
        } catch (NotFoundException var5) {
          var5.printStackTrace();
        }
      } else {
        io.rong.imkit.fragment.ConversationFragment.this.mCSNeedToQuit = config.quitSuspendType.equals(CSQuitSuspendType.SUSPEND);
      }

      for(int i = 0; i < io.rong.imkit.fragment.ConversationFragment.this.mListAdapter.getCount(); ++i) {
        UIMessage uiMessage = (UIMessage) io.rong.imkit.fragment.ConversationFragment.this.mListAdapter.getItem(i);
        if (uiMessage.getContent() instanceof CSPullLeaveMessage) {
          uiMessage.setCsConfig(config);
        }
      }

      io.rong.imkit.fragment.ConversationFragment.this.mListAdapter.notifyDataSetChanged();
      if (!TextUtils.isEmpty(config.announceMsg)) {
        io.rong.imkit.fragment.ConversationFragment.this.onShowAnnounceView(config.announceMsg, config.announceClickUrl);
      }

    }

    public void onError(int code, String msg) {
      io.rong.imkit.fragment.ConversationFragment.this.onCustomServiceWarning(msg, false, io.rong.imkit.fragment.ConversationFragment.this.robotType);
    }

    public void onModeChanged(CustomServiceMode mode) {
      if (io.rong.imkit.fragment.ConversationFragment.this.mRongExtension != null && io.rong.imkit.fragment.ConversationFragment.this.isActivityExist()) {
        io.rong.imkit.fragment.ConversationFragment.this.mRongExtension.setExtensionBarMode(mode);
        if (!mode.equals(CustomServiceMode.CUSTOM_SERVICE_MODE_HUMAN) && !mode.equals(CustomServiceMode.CUSTOM_SERVICE_MODE_HUMAN_FIRST)) {
          if (mode.equals(CustomServiceMode.CUSTOM_SERVICE_MODE_NO_SERVICE)) {
            io.rong.imkit.fragment.ConversationFragment.this.csEvaluate = false;
          }
        } else {
          if (io.rong.imkit.fragment.ConversationFragment.this.mCustomServiceConfig != null && io.rong.imkit.fragment.ConversationFragment.this.mCustomServiceConfig.userTipTime > 0 && !TextUtils.isEmpty(io.rong.imkit.fragment.ConversationFragment.this.mCustomServiceConfig.userTipWord)) {
            io.rong.imkit.fragment.ConversationFragment.this.startTimer(0, io.rong.imkit.fragment.ConversationFragment.this.mCustomServiceConfig.userTipTime * 60 * 1000);
          }

          if (io.rong.imkit.fragment.ConversationFragment.this.mCustomServiceConfig != null && io.rong.imkit.fragment.ConversationFragment.this.mCustomServiceConfig.adminTipTime > 0 && !TextUtils.isEmpty(io.rong.imkit.fragment.ConversationFragment.this.mCustomServiceConfig.adminTipWord)) {
            io.rong.imkit.fragment.ConversationFragment.this.startTimer(1, io.rong.imkit.fragment.ConversationFragment.this.mCustomServiceConfig.adminTipTime * 60 * 1000);
          }

          io.rong.imkit.fragment.ConversationFragment.this.robotType = false;
          io.rong.imkit.fragment.ConversationFragment.this.csEvaluate = true;
        }

      }
    }

    public void onQuit(String msg) {
      RLog.i("ConversationFragment", "CustomService onQuit.");
      if (io.rong.imkit.fragment.ConversationFragment.this.getActivity() != null) {
        if (io.rong.imkit.fragment.ConversationFragment.this.mCustomServiceConfig != null && io.rong.imkit.fragment.ConversationFragment.this.mCustomServiceConfig.evaEntryPoint.equals(CSEvaEntryPoint.EVA_END) && !io.rong.imkit.fragment.ConversationFragment.this.robotType) {
          io.rong.imkit.fragment.ConversationFragment.this.csQuitEvaluate(msg);
        } else {
          io.rong.imkit.fragment.ConversationFragment.this.csQuit(msg);
        }

      }
    }

    public void onPullEvaluation(String dialogId) {
      if (io.rong.imkit.fragment.ConversationFragment.this.mEvaluateDialg == null) {
        io.rong.imkit.fragment.ConversationFragment.this.onCustomServiceEvaluation(true, dialogId, io.rong.imkit.fragment.ConversationFragment.this.robotType, io.rong.imkit.fragment.ConversationFragment.this.csEvaluate);
      }

    }

    public void onSelectGroup(List<CSGroupItem> groups) {
      io.rong.imkit.fragment.ConversationFragment.this.onSelectCustomerServiceGroup(groups);
    }
  };
  private int conversationUnreadCount;
  private boolean isClickUnread = false;

  public ConversationFragment() {
  }

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.mSavedInstanceState = savedInstanceState;
    if (savedInstanceState != null) {
      this.mNewMessageCount = savedInstanceState.getInt("newMessageCount");
      this.mListViewState = savedInstanceState.getParcelable("listState");
    }

    RLog.i("ConversationFragment", "onCreate");
    InternalModuleManager.getInstance().onLoaded();

    try {
      this.mEnableMention = this.getActivity().getResources().getBoolean(R.bool.rc_enable_mentioned_message);
    } catch (NotFoundException var6) {
      RLog.e("ConversationFragment", "rc_enable_mentioned_message not found in rc_config.xml");
    }

    try {
      this.mReadRec = this.getResources().getBoolean(R.bool.rc_read_receipt);
      this.mSyncReadStatus = this.getResources().getBoolean(R.bool.rc_enable_sync_read_status);
    } catch (NotFoundException var5) {
      RLog.e("ConversationFragment", "rc_read_receipt not found in rc_config.xml");
      var5.printStackTrace();
    }

    this.mKitReceiver = new RongKitReceiver();
    IntentFilter filter = new IntentFilter();
    filter.addAction("android.intent.action.PHONE_STATE");
    filter.addAction("android.intent.action.NEW_OUTGOING_CALL");

    try {
      this.getActivity().registerReceiver(this.mKitReceiver, filter);
    } catch (Exception var4) {
      var4.printStackTrace();
    }

  }

  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    final View view = inflater.inflate(R.layout.rc_fr_conversation, container, false);
    this.mRongExtension = (RongExtension)view.findViewById(R.id.rc_extension);
    this.mRongExtension.setFragment(this);
    this.mOffsetLimit = 70.0F * this.getActivity().getResources().getDisplayMetrics().density;
    this.mMsgListView = this.findViewById(view, R.id.rc_layout_msg_list);
    this.mList = (AutoRefreshListView)this.findViewById(this.mMsgListView, R.id.rc_list);
    this.mList.requestDisallowInterceptTouchEvent(true);
    this.mList.setMode(Mode.BOTH);
    this.mListAdapter = this.onResolveAdapter(this.getActivity());
    this.mList.setAdapter(this.mListAdapter);
    this.mList.setOnRefreshListener(new OnRefreshListener() {
      public void onRefreshFromStart() {
        if (io.rong.imkit.fragment.ConversationFragment.this.mHasMoreLocalMessagesUp) {
          io.rong.imkit.fragment.ConversationFragment.this.getHistoryMessage(io.rong.imkit.fragment.ConversationFragment.this.mConversationType, io.rong.imkit.fragment.ConversationFragment.this.mTargetId, 10, Mode.START, 1, -1);
        } else {
          io.rong.imkit.fragment.ConversationFragment.this.getRemoteHistoryMessages(io.rong.imkit.fragment.ConversationFragment.this.mConversationType, io.rong.imkit.fragment.ConversationFragment.this.mTargetId, 10);
        }

      }

      public void onRefreshFromEnd() {
        if (io.rong.imkit.fragment.ConversationFragment.this.mHasMoreLocalMessagesDown && io.rong.imkit.fragment.ConversationFragment.this.indexMessageTime > 0L) {
          io.rong.imkit.fragment.ConversationFragment.this.getHistoryMessage(io.rong.imkit.fragment.ConversationFragment.this.mConversationType, io.rong.imkit.fragment.ConversationFragment.this.mTargetId, 10, Mode.END, 1, -1);
        } else {
          io.rong.imkit.fragment.ConversationFragment.this.mList.onRefreshComplete(0, 0, false);
        }

      }
    });
    this.mList.setOnTouchListener(new OnTouchListener() {
      public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == 2 && io.rong.imkit.fragment.ConversationFragment.this.mList.getCount() - io.rong.imkit.fragment.ConversationFragment.this.mList.getHeaderViewsCount() - io.rong.imkit.fragment.ConversationFragment.this.mList.getFooterViewsCount() == 0) {
          if (io.rong.imkit.fragment.ConversationFragment.this.mHasMoreLocalMessagesUp) {
            io.rong.imkit.fragment.ConversationFragment.this.getHistoryMessage(io.rong.imkit.fragment.ConversationFragment.this.mConversationType, io.rong.imkit.fragment.ConversationFragment.this.mTargetId, 10, Mode.START, 1, -1);
          } else if (io.rong.imkit.fragment.ConversationFragment.this.mList.getRefreshState() != State.REFRESHING) {
            io.rong.imkit.fragment.ConversationFragment.this.getRemoteHistoryMessages(io.rong.imkit.fragment.ConversationFragment.this.mConversationType, io.rong.imkit.fragment.ConversationFragment.this.mTargetId, 10);
          }

          return true;
        } else {
          if (event.getAction() == 1 && io.rong.imkit.fragment.ConversationFragment.this.mRongExtension != null && io.rong.imkit.fragment.ConversationFragment.this.mRongExtension.isExtensionExpanded()) {
            io.rong.imkit.fragment.ConversationFragment.this.mRongExtension.collapseExtension();
          }

          return false;
        }
      }
    });
    if (RongContext.getInstance().getNewMessageState()) {
      this.mNewMessageTextView = (TextView)this.findViewById(view, R.id.rc_new_message_number);
      this.mNewMessageBtn = (ImageButton)this.findViewById(view, R.id.rc_new_message_count);
      this.mNewMessageBtn.setOnClickListener(new OnClickListener() {
        public void onClick(View v) {
          io.rong.imkit.fragment.ConversationFragment.this.mList.setSelection(io.rong.imkit.fragment.ConversationFragment.this.mListAdapter.getCount());
          io.rong.imkit.fragment.ConversationFragment.this.mNewMessageBtn.setVisibility(View.GONE);
          io.rong.imkit.fragment.ConversationFragment.this.mNewMessageTextView.setVisibility(View.GONE);
          io.rong.imkit.fragment.ConversationFragment.this.mNewMessageCount = 0;
        }
      });
    }

    if (RongContext.getInstance().getUnreadMessageState()) {
      this.mUnreadMsgLayout = (LinearLayout)this.findViewById(this.mMsgListView, R.id.rc_unread_message_layout);
      this.mUnreadMsgCountTv = (TextView)this.findViewById(this.mMsgListView, R.id.rc_unread_message_count);
    }

    this.mList.addOnScrollListener(this);
    this.mListAdapter.setOnItemHandlerListener(new OnItemHandlerListener() {
      public boolean onWarningViewClick(final int position, final Message data, View v) {
        RongIMClient.getInstance().deleteMessages(new int[]{data.getMessageId()}, new ResultCallback<Boolean>() {
          public void onSuccess(Boolean aBoolean) {
            if (aBoolean) {
              io.rong.imkit.fragment.ConversationFragment.this.mListAdapter.remove(position);
              data.setMessageId(0);
              io.rong.imkit.fragment.ConversationFragment.this.onResendItemClick(data);
            }

          }

          public void onError(ErrorCode e) {
          }
        });
        return true;
      }

      public void onReadReceiptStateClick(Message message) {
        io.rong.imkit.fragment.ConversationFragment.this.onReadReceiptStateClick(message);
      }
    });
    this.showNewMessage();
    view.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
      public void onGlobalLayout() {
        Rect r = new Rect();
        view.getWindowVisibleDisplayFrame(r);
        int screenHeight = view.getRootView().getHeight();
        int keypadHeight = screenHeight - r.bottom;
        if ((double)keypadHeight > (double)screenHeight * 0.15D) {
          io.rong.imkit.fragment.ConversationFragment.this.mList.setSelection(io.rong.imkit.fragment.ConversationFragment.this.mListAdapter.getCount());
          view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
          if (io.rong.imkit.fragment.ConversationFragment.this.mNewMessageCount > 0 && io.rong.imkit.fragment.ConversationFragment.this.mNewMessageBtn != null) {
            io.rong.imkit.fragment.ConversationFragment.this.mNewMessageCount = 0;
            io.rong.imkit.fragment.ConversationFragment.this.mNewMessageBtn.setVisibility(View.GONE);
            io.rong.imkit.fragment.ConversationFragment.this.mNewMessageTextView.setVisibility(View.GONE);
          }
        }

      }
    });
    return view;
  }

  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    if (this.showMoreClickItem()) {
      this.clickAction = (new Builder()).title(this.getResources().getString(R.string.rc_dialog_item_message_more)).actionListener(new MessageItemLongClickListener() {
        public boolean onMessageItemLongClick(Context context, UIMessage message) {
          io.rong.imkit.fragment.ConversationFragment.this.setMoreActionState(message);
          return true;
        }
      }).build();
      RongMessageItemLongClickActionManager.getInstance().addMessageItemLongClickAction(this.clickAction);
    }

  }

  public void onScrollStateChanged(AbsListView view, int scrollState) {
    if (scrollState == 1) {
      if (this.mRongExtension != null) {
        this.mRongExtension.collapseExtension();
      }
    } else if (scrollState == 0) {
      int last = this.mList.getLastVisiblePosition();
      if (this.mNewMessageBtn != null && last == this.mList.getCount() - 1) {
        this.mNewMessageCount = 0;
        this.mNewMessageBtn.setVisibility(View.GONE);
        this.mNewMessageTextView.setVisibility(View.GONE);
      }
    }

  }

  public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
  }

  public void onSaveInstanceState(Bundle outState) {
    outState.putInt("unReadCount", this.mUnReadCount);
    outState.putInt("newMessageCount", this.mNewMessageCount);
    outState.putParcelable("listState", this.mList.onSaveInstanceState());
    super.onSaveInstanceState(outState);
  }

  public void onResume() {
    if (!this.getActivity().isFinishing() && this.mRongExtension != null) {
      RLog.d("ConversationFragment", "onResume when back from other activity.");
      this.mRongExtension.resetEditTextLayoutDrawnStatus();
    }

    RongPushClient.clearAllNotifications(this.getActivity());
    super.onResume();
  }

  public final void getUserInfo(String userId, UserInfoCallback callback) {
    UserInfo userInfo = RongUserInfoManager.getInstance().getUserInfo(userId);
    if (userInfo != null) {
      callback.onGotUserInfo(userInfo);
    }

  }

  public void setMoreActionStateListener(OnMoreActionStateListener moreActionStateListener) {
    this.moreActionStateListener = moreActionStateListener;
  }

  public void setMoreActionState(UIMessage message) {
    for(int i = 0; i < this.mListAdapter.getCount(); ++i) {
      ((UIMessage)this.mListAdapter.getItem(i)).setChecked(false);
    }

    this.mListAdapter.setMessageCheckedChanged(new OnMessageCheckedChanged() {
      public void onCheckedEnable(boolean enable) {
        io.rong.imkit.fragment.ConversationFragment.this.mRongExtension.setMoreActionEnable(enable);
      }
    });
    this.mListAdapter.setSelectedCountDidExceed(new OnSelectedCountDidExceed() {
      public void onSelectedCountDidExceed() {
        io.rong.imkit.fragment.ConversationFragment.this.messageSelectedCountDidExceed();
      }
    });
    this.mRongExtension.showMoreActionLayout(this.getMoreClickActions());
    this.mListAdapter.setShowCheckbox(true);
    message.setChecked(true);
    this.mListAdapter.notifyDataSetChanged();
    if (this.moreActionStateListener != null) {
      this.moreActionStateListener.onShownMoreActionLayout();
    }

  }

  public void resetMoreActionState() {
    this.mRongExtension.hideMoreActionLayout();
    this.mListAdapter.setShowCheckbox(false);
    this.mListAdapter.notifyDataSetChanged();
    if (this.moreActionStateListener != null) {
      this.moreActionStateListener.onHiddenMoreActionLayout();
    }

  }

  public List<IClickActions> getMoreClickActions() {
    List<IClickActions> actions = new ArrayList();
    DeleteClickActions deleteClickActions = new DeleteClickActions();
    actions.add(deleteClickActions);
    return actions;
  }

  public List<Message> getCheckedMessages() {
    return this.mListAdapter != null ? this.mListAdapter.getCheckedMessage() : null;
  }

  public void setMaxMessageSelectedCount(int maxMessageSelectedCount) {
    if (this.mListAdapter != null) {
      this.mListAdapter.setMaxMessageSelectedCount(maxMessageSelectedCount);
    }

  }

  protected void messageSelectedCountDidExceed() {
    Toast.makeText(this.getActivity(), R.string.rc_exceeded_max_limit, Toast.LENGTH_SHORT).show();
  }

  public boolean showMoreClickItem() {
    return false;
  }

  public MessageListAdapter onResolveAdapter(Context context) {
    return new MessageListAdapter(context);
  }

  protected void initFragment(Uri uri) {
    this.indexMessageTime = this.getActivity().getIntent().getLongExtra("indexMessageTime", 0L);
    RLog.d("ConversationFragment", "initFragment : " + uri + ",this=" + this + ", time = " + this.indexMessageTime);
    if (uri != null) {
      String typeStr = uri.getLastPathSegment().toUpperCase(Locale.US);
      this.mConversationType = ConversationType.valueOf(typeStr);
      this.mTargetId = uri.getQueryParameter("targetId");
      this.mRongExtension.setConversation(this.mConversationType, this.mTargetId);
      RongIMClient.getInstance().getTextMessageDraft(this.mConversationType, this.mTargetId, new ResultCallback<String>() {
        public void onSuccess(String s) {
          io.rong.imkit.fragment.ConversationFragment.this.mDraft = s;
          if (io.rong.imkit.fragment.ConversationFragment.this.mRongExtension != null) {
            if (!TextUtils.isEmpty(s)) {
              EditText editText = io.rong.imkit.fragment.ConversationFragment.this.mRongExtension.getInputEditText();
              editText.setText(s);
              editText.setSelection(editText.length());
              editText.requestFocus();
            }

            io.rong.imkit.fragment.ConversationFragment.this.mRongExtension.setExtensionClickListener(io.rong.imkit.fragment.ConversationFragment.this);
          }

        }

        public void onError(ErrorCode e) {
          if (io.rong.imkit.fragment.ConversationFragment.this.mRongExtension != null) {
            io.rong.imkit.fragment.ConversationFragment.this.mRongExtension.setExtensionClickListener(io.rong.imkit.fragment.ConversationFragment.this);
          }

        }
      });
      this.mCurrentConversationInfo = ConversationInfo.obtain(this.mConversationType, this.mTargetId);
      RongContext.getInstance().registerConversationInfo(this.mCurrentConversationInfo);
      this.mNotificationContainer = (LinearLayout)this.mMsgListView.findViewById(R.id.rc_notification_container);
      if (this.mConversationType.equals(ConversationType.CUSTOMER_SERVICE) && this.getActivity() != null && this.getActivity().getIntent() != null && this.getActivity().getIntent().getData() != null) {
        this.mCustomUserInfo = (CSCustomServiceInfo)this.getActivity().getIntent().getParcelableExtra("customServiceInfo");
      }

      if (this.mConversationType.equals(ConversationType.CHATROOM)) {
        boolean createIfNotExist = this.isActivityExist() && this.getActivity().getIntent().getBooleanExtra("createIfNotExist", true);
        int pullCount = this.getResources().getInteger(R.integer.rc_chatroom_first_pull_message_count);
        if (createIfNotExist) {
          RongIMClient.getInstance().joinChatRoom(this.mTargetId, pullCount, new OperationCallback() {
            public void onSuccess() {
              RLog.i("ConversationFragment", "joinChatRoom onSuccess : " + io.rong.imkit.fragment.ConversationFragment.this.mTargetId);
            }

            public void onError(ErrorCode errorCode) {
              RLog.e("ConversationFragment", "joinChatRoom onError : " + errorCode);
              if (io.rong.imkit.fragment.ConversationFragment.this.isActivityExist()) {
                if (errorCode != ErrorCode.RC_NET_UNAVAILABLE && errorCode != ErrorCode.RC_NET_CHANNEL_INVALID) {
                  io.rong.imkit.fragment.ConversationFragment.this.onWarningDialog(io.rong.imkit.fragment.ConversationFragment.this.getString(R.string.rc_join_chatroom_failure));
                } else {
                  io.rong.imkit.fragment.ConversationFragment.this.onWarningDialog(io.rong.imkit.fragment.ConversationFragment.this.getString(R.string.rc_notice_network_unavailable));
                }
              }

            }
          });
        } else {
          RongIMClient.getInstance().joinExistChatRoom(this.mTargetId, pullCount, new OperationCallback() {
            public void onSuccess() {
              RLog.i("ConversationFragment", "joinExistChatRoom onSuccess : " + io.rong.imkit.fragment.ConversationFragment.this.mTargetId);
            }

            public void onError(ErrorCode errorCode) {
              RLog.e("ConversationFragment", "joinExistChatRoom onError : " + errorCode);
              if (io.rong.imkit.fragment.ConversationFragment.this.isActivityExist()) {
                if (errorCode != ErrorCode.RC_NET_UNAVAILABLE && errorCode != ErrorCode.RC_NET_CHANNEL_INVALID) {
                  io.rong.imkit.fragment.ConversationFragment.this.onWarningDialog(io.rong.imkit.fragment.ConversationFragment.this.getString(R.string.rc_join_chatroom_failure));
                } else {
                  io.rong.imkit.fragment.ConversationFragment.this.onWarningDialog(io.rong.imkit.fragment.ConversationFragment.this.getString(R.string.rc_notice_network_unavailable));
                }
              }

            }
          });
        }
      } else if (this.mConversationType != ConversationType.APP_PUBLIC_SERVICE && this.mConversationType != ConversationType.PUBLIC_SERVICE) {
        if (this.mConversationType.equals(ConversationType.CUSTOMER_SERVICE)) {
          this.onStartCustomService(this.mTargetId);
        } else if (this.mEnableMention && (this.mConversationType.equals(ConversationType.DISCUSSION) || this.mConversationType.equals(ConversationType.GROUP))) {
          RongMentionManager.getInstance().createInstance(this.mConversationType, this.mTargetId, this.mRongExtension.getInputEditText());
        }
      } else {
        PublicServiceCommandMessage msg = new PublicServiceCommandMessage();
        msg.setCommand(PublicServiceMenuItemType.Entry.getMessage());
        Message message = Message.obtain(this.mTargetId, this.mConversationType, msg);
        RongIMClient.getInstance().sendMessage(message, (String)null, (String)null, new ISendMessageCallback() {
          public void onAttached(Message message) {
          }

          public void onSuccess(Message message) {
          }

          public void onError(Message message, ErrorCode errorCode) {
          }
        });
        PublicServiceType publicServiceType;
        if (this.mConversationType == ConversationType.PUBLIC_SERVICE) {
          publicServiceType = PublicServiceType.PUBLIC_SERVICE;
        } else {
          publicServiceType = PublicServiceType.APP_PUBLIC_SERVICE;
        }

        this.getPublicServiceProfile(publicServiceType, this.mTargetId);
      }
    }

    RongIMClient.getInstance().getConversation(this.mConversationType, this.mTargetId, new ResultCallback<Conversation>() {
      public void onSuccess(Conversation conversation) {
        if (conversation != null && io.rong.imkit.fragment.ConversationFragment.this.isActivityExist()) {
          if (io.rong.imkit.fragment.ConversationFragment.this.mSavedInstanceState != null) {
            io.rong.imkit.fragment.ConversationFragment.this.mUnReadCount = io.rong.imkit.fragment.ConversationFragment.this.mSavedInstanceState.getInt("unReadCount");
          } else {
            io.rong.imkit.fragment.ConversationFragment.this.mUnReadCount = conversation.getUnreadMessageCount();
          }

          io.rong.imkit.fragment.ConversationFragment.this.conversationUnreadCount = io.rong.imkit.fragment.ConversationFragment.this.mUnReadCount;
          if (io.rong.imkit.fragment.ConversationFragment.this.mUnReadCount > 0) {
            if (io.rong.imkit.fragment.ConversationFragment.this.mReadRec && io.rong.imkit.fragment.ConversationFragment.this.mConversationType == ConversationType.PRIVATE && RongContext.getInstance().isReadReceiptConversationType(ConversationType.PRIVATE)) {
              RongIMClient.getInstance().sendReadReceiptMessage(io.rong.imkit.fragment.ConversationFragment.this.mConversationType, io.rong.imkit.fragment.ConversationFragment.this.mTargetId, conversation.getSentTime());
            }

            if (io.rong.imkit.fragment.ConversationFragment.this.mSyncReadStatus && (!io.rong.imkit.fragment.ConversationFragment.this.mReadRec && io.rong.imkit.fragment.ConversationFragment.this.mConversationType == ConversationType.PRIVATE || io.rong.imkit.fragment.ConversationFragment.this.mConversationType == ConversationType.GROUP || io.rong.imkit.fragment.ConversationFragment.this.mConversationType == ConversationType.DISCUSSION)) {
              RongIMClient.getInstance().syncConversationReadStatus(io.rong.imkit.fragment.ConversationFragment.this.mConversationType, io.rong.imkit.fragment.ConversationFragment.this.mTargetId, conversation.getSentTime(), (OperationCallback)null);
            }
          }

          if (conversation.getMentionedCount() > 0) {
            io.rong.imkit.fragment.ConversationFragment.this.getLastMentionedMessageId(io.rong.imkit.fragment.ConversationFragment.this.mConversationType, io.rong.imkit.fragment.ConversationFragment.this.mTargetId);
          } else {
            RongIMClient.getInstance().getTheFirstUnreadMessage(io.rong.imkit.fragment.ConversationFragment.this.mConversationType, io.rong.imkit.fragment.ConversationFragment.this.mTargetId, new ResultCallback<Message>() {
              public void onSuccess(Message message) {
                io.rong.imkit.fragment.ConversationFragment.this.firstUnreadMessage = message;
                if (io.rong.imkit.fragment.ConversationFragment.this.mUnReadCount > 10 && io.rong.imkit.fragment.ConversationFragment.this.mUnreadMsgLayout != null && io.rong.imkit.fragment.ConversationFragment.this.firstUnreadMessage != null) {
                  io.rong.imkit.fragment.ConversationFragment.this.showUnreadMsgLayout();
                }

                io.rong.imkit.fragment.ConversationFragment.this.refreshUnreadUI();
                RongIM.getInstance().clearMessagesUnreadStatus(io.rong.imkit.fragment.ConversationFragment.this.mConversationType, io.rong.imkit.fragment.ConversationFragment.this.mTargetId, (ResultCallback)null);
              }

              public void onError(ErrorCode e) {
                RongIM.getInstance().clearMessagesUnreadStatus(io.rong.imkit.fragment.ConversationFragment.this.mConversationType, io.rong.imkit.fragment.ConversationFragment.this.mTargetId, (ResultCallback)null);
              }
            });
          }

          if (io.rong.imkit.fragment.ConversationFragment.this.mUnReadCount > 10 && io.rong.imkit.fragment.ConversationFragment.this.mUnreadMsgLayout != null) {
            if (io.rong.imkit.fragment.ConversationFragment.this.mUnReadCount > 99) {
              io.rong.imkit.fragment.ConversationFragment.this.mUnreadMsgCountTv.setText(String.format("%s%s", "99+", io.rong.imkit.fragment.ConversationFragment.this.getActivity().getResources().getString(R.string.rc_new_messages)));
            } else {
              io.rong.imkit.fragment.ConversationFragment.this.mUnreadMsgCountTv.setText(String.format("%s%s", io.rong.imkit.fragment.ConversationFragment.this.mUnReadCount, io.rong.imkit.fragment.ConversationFragment.this.getActivity().getResources().getString(R.string.rc_new_messages)));
            }

            io.rong.imkit.fragment.ConversationFragment.this.mUnreadMsgLayout.setOnClickListener(new OnClickListener() {
              public void onClick(View v) {
                io.rong.imkit.fragment.ConversationFragment.this.mUnreadMsgLayout.setClickable(false);
                io.rong.imkit.fragment.ConversationFragment.this.mList.removeOnScrollListener(io.rong.imkit.fragment.ConversationFragment.this.mOnScrollListener);
                TranslateAnimation animation = new TranslateAnimation(0.0F, 500.0F, 0.0F, 0.0F);
                animation.setDuration(500L);
                io.rong.imkit.fragment.ConversationFragment.this.mUnreadMsgLayout.startAnimation(animation);
                animation.setFillAfter(true);
                animation.setAnimationListener(new AnimationListener() {
                  public void onAnimationStart(Animation animation) {
                  }

                  public void onAnimationEnd(Animation animation) {
                    if (io.rong.imkit.fragment.ConversationFragment.this.firstUnreadMessage == null) {
                      RLog.e("ConversationFragment", "firstUnreadMessage is null");
                    } else {
                      io.rong.imkit.fragment.ConversationFragment.this.indexMessageTime = io.rong.imkit.fragment.ConversationFragment.this.firstUnreadMessage.getSentTime();
                      int position = io.rong.imkit.fragment.ConversationFragment.this.mListAdapter.findPosition((long) io.rong.imkit.fragment.ConversationFragment.this.firstUnreadMessage.getMessageId());
                      if (position == 0) {
                        io.rong.imkit.fragment.ConversationFragment.this.mList.setSelection(position);
                      } else if (position > 0) {
                        io.rong.imkit.fragment.ConversationFragment.this.mList.setSelection(position - 1);
                      } else {
                        io.rong.imkit.fragment.ConversationFragment.this.isClickUnread = true;
                        io.rong.imkit.fragment.ConversationFragment.this.mListAdapter.clear();
                        io.rong.imkit.fragment.ConversationFragment.this.getHistoryMessage(io.rong.imkit.fragment.ConversationFragment.this.mConversationType, io.rong.imkit.fragment.ConversationFragment.this.mTargetId, 10, Mode.END, 2, io.rong.imkit.fragment.ConversationFragment.this.firstUnreadMessage.getMessageId());
                      }

                    }
                  }

                  public void onAnimationRepeat(Animation animation) {
                  }
                });
              }
            });
          }
        }

      }

      public void onError(ErrorCode e) {
      }
    });
    Mode mode = this.indexMessageTime > 0L ? Mode.END : Mode.START;
    int scrollMode = this.indexMessageTime > 0L ? 1 : 3;
    this.getHistoryMessage(this.mConversationType, this.mTargetId, 10, mode, scrollMode, -1);
    if (!EventBus.getDefault().isRegistered(this)) {
      EventBus.getDefault().register(this);
    }

  }

  private void showUnreadMsgLayout() {
    TranslateAnimation translateAnimation = new TranslateAnimation(300.0F, 0.0F, 0.0F, 0.0F);
    AlphaAnimation alphaAnimation = new AlphaAnimation(0.0F, 1.0F);
    translateAnimation.setDuration(1000L);
    alphaAnimation.setDuration(2000L);
    AnimationSet set = new AnimationSet(true);
    set.addAnimation(translateAnimation);
    set.addAnimation(alphaAnimation);
    this.mUnreadMsgLayout.setVisibility(View.VISIBLE);
    this.mUnreadMsgLayout.startAnimation(set);
  }

  public void getPublicServiceProfile(PublicServiceType publicServiceType, String publicServiceId) {
    RongIM.getInstance().getPublicServiceProfile(publicServiceType, this.mTargetId, new ResultCallback<PublicServiceProfile>() {
      public void onSuccess(PublicServiceProfile publicServiceProfile) {
        io.rong.imkit.fragment.ConversationFragment.this.updatePublicServiceMenu(publicServiceProfile);
      }

      public void onError(ErrorCode e) {
      }
    });
  }

  protected void updatePublicServiceMenu(PublicServiceProfile publicServiceProfile) {
    List<InputMenu> inputMenuList = new ArrayList();
    PublicServiceMenu menu = publicServiceProfile.getMenu();
    ArrayList<PublicServiceMenuItem> items = menu != null ? menu.getMenuItems() : null;
    if (items != null && this.mRongExtension != null) {
      this.mPublicServiceProfile = publicServiceProfile;
      Iterator var5 = items.iterator();

      while(var5.hasNext()) {
        PublicServiceMenuItem item = (PublicServiceMenuItem)var5.next();
        InputMenu inputMenu = new InputMenu();
        inputMenu.title = item.getName();
        inputMenu.subMenuList = new ArrayList();
        Iterator var8 = item.getSubMenuItems().iterator();

        while(var8.hasNext()) {
          PublicServiceMenuItem i = (PublicServiceMenuItem)var8.next();
          inputMenu.subMenuList.add(i.getName());
        }

        inputMenuList.add(inputMenu);
      }

      this.mRongExtension.setInputMenu(inputMenuList, true);
    }

  }

  public void hideNotificationView(View notificationView) {
    if (notificationView != null) {
      View view = this.mNotificationContainer.findViewById(notificationView.getId());
      if (view != null) {
        this.mNotificationContainer.removeView(view);
        if (this.mNotificationContainer.getChildCount() == 0) {
          this.mNotificationContainer.setVisibility(View.GONE);
        }
      }

    }
  }

  public void showNotificationView(View notificationView) {
    if (notificationView != null) {
      View view = this.mNotificationContainer.findViewById(notificationView.getId());
      if (view == null) {
        this.mNotificationContainer.addView(notificationView);
        this.mNotificationContainer.setVisibility(View.VISIBLE);
      }
    }
  }

  public View inflateNotificationView(@LayoutRes int layout) {
    return LayoutInflater.from(this.getActivity()).inflate(layout, this.mNotificationContainer, false);
  }

  public void onResendItemClick(Message message) {
    if (message.getContent() instanceof ImageMessage) {
      ImageMessage imageMessage = (ImageMessage)message.getContent();
      if (imageMessage.getRemoteUri() != null && !imageMessage.getRemoteUri().toString().startsWith("file")) {
        RongIM.getInstance().sendMessage(message, (String)null, (String)null, (ISendMediaMessageCallback)null);
      } else {
        RongIM.getInstance().sendImageMessage(message, (String)null, (String)null, (SendImageMessageCallback)null);
      }
    } else if (message.getContent() instanceof LocationMessage) {
      RongIM.getInstance().sendLocationMessage(message, (String)null, (String)null, (ISendMessageCallback)null);
    } else if (message.getContent() instanceof MediaMessageContent) {
      MediaMessageContent mediaMessageContent = (MediaMessageContent)message.getContent();
      if (mediaMessageContent.getMediaUrl() != null) {
        RongIM.getInstance().sendMessage(message, (String)null, (String)null, (ISendMediaMessageCallback)null);
      } else {
        RongIM.getInstance().sendMediaMessage(message, (String)null, (String)null, (ISendMediaMessageCallback)null);
      }
    } else {
      RongIM.getInstance().sendMessage(message, (String)null, (String)null, (ISendMessageCallback)null);
    }

  }

  public void onReadReceiptStateClick(Message message) {
  }

  public void onSelectCustomerServiceGroup(final List<CSGroupItem> groupList) {
    if (!this.isActivityExist()) {
      RLog.w("ConversationFragment", "onSelectCustomerServiceGroup Activity has finished");
    } else {
      List<String> singleDataList = new ArrayList();
      singleDataList.clear();

      for(int i = 0; i < groupList.size(); ++i) {
        if (((CSGroupItem)groupList.get(i)).getOnline()) {
          singleDataList.add(((CSGroupItem)groupList.get(i)).getName());
        }
      }

      if (singleDataList.size() == 0) {
        RongIMClient.getInstance().selectCustomServiceGroup(this.mTargetId, (String)null);
      } else {
        final SingleChoiceDialog singleChoiceDialog = new SingleChoiceDialog(this.getActivity(), singleDataList);
        singleChoiceDialog.setTitle(this.getActivity().getResources().getString(R.string.rc_cs_select_group));
        singleChoiceDialog.setOnOKButtonListener(new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            int selItem = singleChoiceDialog.getSelectItem();
            RongIMClient.getInstance().selectCustomServiceGroup(io.rong.imkit.fragment.ConversationFragment.this.mTargetId, ((CSGroupItem)groupList.get(selItem)).getId());
          }
        });
        singleChoiceDialog.setOnCancelButtonListener(new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            RongIMClient.getInstance().selectCustomServiceGroup(io.rong.imkit.fragment.ConversationFragment.this.mTargetId, (String)null);
          }
        });
        singleChoiceDialog.show();
      }
    }
  }

  private void csQuit(String msg) {
    if (this.getHandler() != null) {
      this.getHandler().removeCallbacksAndMessages((Object)null);
    }

    if (this.mEvaluateDialg == null) {
      if (this.mCustomServiceConfig != null) {
        this.onCustomServiceWarning(msg, this.mCustomServiceConfig.quitSuspendType == CSQuitSuspendType.NONE, this.robotType);
      }
    } else {
      this.mEvaluateDialg.destroy();
    }

    if (this.mCustomServiceConfig != null && !this.mCustomServiceConfig.quitSuspendType.equals(CSQuitSuspendType.NONE)) {
      RongContext.getInstance().getEventBus().post(new CSTerminateEvent(this.getActivity(), msg));
    }

  }

  private void csQuitEvaluateButtonClick(String msg) {
    if (this.mEvaluateDialg != null) {
      this.mEvaluateDialg.destroy();
      this.mEvaluateDialg = null;
    }

    if (this.getHandler() != null) {
      this.getHandler().removeCallbacksAndMessages((Object)null);
    }

    if (this.mEvaluateDialg == null) {
      this.onCustomServiceWarning(msg, false, this.robotType);
    } else {
      this.mEvaluateDialg.destroy();
    }

    if (this.mCustomServiceConfig != null && !this.mCustomServiceConfig.quitSuspendType.equals(CSQuitSuspendType.NONE)) {
      RongContext.getInstance().getEventBus().post(new CSTerminateEvent(this.getActivity(), msg));
    }

  }

  private void csQuitEvaluate(final String msg) {
    if (this.mEvaluateDialg == null) {
      this.mEvaluateDialg = new CSEvaluateDialog(this.getActivity(), this.mTargetId);
      this.mEvaluateDialg.setClickListener(new EvaluateClickListener() {
        public void onEvaluateSubmit() {
          io.rong.imkit.fragment.ConversationFragment.this.csQuitEvaluateButtonClick(msg);
        }

        public void onEvaluateCanceled() {
          io.rong.imkit.fragment.ConversationFragment.this.csQuitEvaluateButtonClick(msg);
        }
      });
      this.mEvaluateDialg.setOnCancelListener(new OnCancelListener() {
        public void onCancel(DialogInterface dialog) {
          if (io.rong.imkit.fragment.ConversationFragment.this.mEvaluateDialg != null) {
            io.rong.imkit.fragment.ConversationFragment.this.mEvaluateDialg = null;
          }

        }
      });
      this.mEvaluateDialg.showStar("");
    }

  }

  public void onPause() {
    this.finishing = this.getActivity().isFinishing();
    if (this.finishing) {
      this.destroy();
    } else {
      this.stopAudioThingsDependsOnVoipMode();
    }

    super.onPause();
  }

  public void onShowAnnounceView(String announceMsg, String announceUrl) {
  }

  private void destroy() {
    RongIM.getInstance().clearMessagesUnreadStatus(this.mConversationType, this.mTargetId, (ResultCallback)null);
    if (this.getHandler() != null) {
      this.getHandler().removeCallbacksAndMessages((Object)null);
    }

    if (this.mEnableMention && (this.mConversationType.equals(ConversationType.DISCUSSION) || this.mConversationType.equals(ConversationType.GROUP))) {
      RongMentionManager.getInstance().destroyInstance(this.mConversationType, this.mTargetId);
    }

    if (this.mConversationType.equals(ConversationType.CHATROOM)) {
      SendImageManager.getInstance().cancelSendingImages(this.mConversationType, this.mTargetId);
      RongIM.getInstance().quitChatRoom(this.mTargetId, (OperationCallback)null);
    }

    if (this.mConversationType.equals(ConversationType.CUSTOMER_SERVICE) && this.mCSNeedToQuit) {
      this.onStopCustomService(this.mTargetId);
    }

    if (this.mSyncReadStatus && this.mSyncReadStatusMsgTime > 0L && (this.mConversationType.equals(ConversationType.DISCUSSION) || this.mConversationType.equals(ConversationType.GROUP))) {
      RongIMClient.getInstance().syncConversationReadStatus(this.mConversationType, this.mTargetId, this.mSyncReadStatusMsgTime, (OperationCallback)null);
    }

    EventBus.getDefault().unregister(this);
    this.stopAudioThingsDependsOnVoipMode();

    try {
      if (this.mKitReceiver != null) {
        this.getActivity().unregisterReceiver(this.mKitReceiver);
      }
    } catch (Exception var2) {
      var2.printStackTrace();
    }

    RongContext.getInstance().unregisterConversationInfo(this.mCurrentConversationInfo);
    this.destroyExtension();
  }

  private void stopAudioThingsDependsOnVoipMode() {
    if (!AudioPlayManager.getInstance().isInVOIPMode(this.getActivity())) {
      AudioPlayManager.getInstance().stopPlay();
    }

    AudioRecordManager.getInstance().destroyRecord();
  }

  private void destroyExtension() {
    String text = this.mRongExtension.getInputEditText().getText().toString();
    text = text.trim();
    if (TextUtils.isEmpty(text) && !TextUtils.isEmpty(this.mDraft) || !TextUtils.isEmpty(text) && TextUtils.isEmpty(this.mDraft) || !TextUtils.isEmpty(text) && !TextUtils.isEmpty(this.mDraft) && !text.equals(this.mDraft)) {
      RongIMClient.getInstance().saveTextMessageDraft(this.mConversationType, this.mTargetId, text, (ResultCallback)null);
      DraftEvent draft = new DraftEvent(this.mConversationType, this.mTargetId, text);
      RongContext.getInstance().getEventBus().post(draft);
    }

    this.mRongExtension.onDestroy();
    this.mRongExtension = null;
  }

  public void onDestroy() {
    RongMessageItemLongClickActionManager.getInstance().removeMessageItemLongClickAction(this.clickAction);
    if (!this.finishing) {
      this.destroy();
    }

    super.onDestroy();
  }

  public boolean isLocationSharing() {
//    return LocationManager.getInstance().isSharing();
    return false;
  }

  public void showQuitLocationSharingDialog(final Activity activity) {
    PromptPopupDialog.newInstance(activity, this.getString(R.string.rc_ext_warning), this.getString(R.string.rc_real_time_exit_notification), this.getString(R.string.rc_action_bar_ok)).setPromptButtonClickedListener(new OnPromptButtonClickedListener() {
      public void onPositiveButtonClicked() {
        activity.finish();
      }
    }).show();
  }

  public boolean onBackPressed() {
    if (this.mRongExtension != null && this.mRongExtension.isExtensionExpanded()) {
      this.mRongExtension.collapseExtension();
      return true;
    } else if (this.mConversationType != null && this.mCustomServiceConfig != null && this.mConversationType.equals(ConversationType.CUSTOMER_SERVICE) && this.mCustomServiceConfig != null && this.mCustomServiceConfig.quitSuspendType.equals(CSQuitSuspendType.NONE)) {
      return this.onCustomServiceEvaluation(false, "", this.robotType, this.csEvaluate);
    } else if (this.mRongExtension != null && this.mRongExtension.isMoreActionShown()) {
      this.resetMoreActionState();
      return true;
    } else {
      return false;
    }
  }

  public boolean handleMessage(android.os.Message msg) {
    InformationNotificationMessage info;
    switch(msg.what) {
      case 0:
        if (this.isActivityExist() && this.mCustomServiceConfig != null) {
          info = new InformationNotificationMessage(this.mCustomServiceConfig.userTipWord);
          RongIM.getInstance().insertMessage(ConversationType.CUSTOMER_SERVICE, this.mTargetId, this.mTargetId, info, System.currentTimeMillis(), (ResultCallback)null);
          return true;
        }

        return true;
      case 1:
        if (this.isActivityExist() && this.mCustomServiceConfig != null) {
          info = new InformationNotificationMessage(this.mCustomServiceConfig.adminTipWord);
          RongIM.getInstance().insertMessage(ConversationType.CUSTOMER_SERVICE, this.mTargetId, this.mTargetId, info, System.currentTimeMillis(), (ResultCallback)null);
          return true;
        }

        return true;
      default:
        return false;
    }
  }

  private boolean isActivityExist() {
    return this.getActivity() != null && !this.getActivity().isFinishing();
  }

  public void onWarningDialog(String msg) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
    builder.setCancelable(false);
    final AlertDialog alertDialog = builder.create();
    alertDialog.show();
    Window window = alertDialog.getWindow();
    window.setContentView(R.layout.rc_cs_alert_warning);
    TextView tv = (TextView)window.findViewById(R.id.rc_cs_msg);
    tv.setText(msg);
    window.findViewById(R.id.rc_btn_ok).setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        alertDialog.dismiss();
        FragmentManager fm = io.rong.imkit.fragment.ConversationFragment.this.getChildFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
          fm.popBackStack();
        } else {
          io.rong.imkit.fragment.ConversationFragment.this.getActivity().finish();
        }

      }
    });
  }

  public void onCustomServiceWarning(String msg, final boolean evaluate, final boolean robotType) {
    if (!this.isActivityExist()) {
      RLog.w("ConversationFragment", "Activity has finished");
    } else {
      AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
      builder.setCancelable(false);
      final AlertDialog alertDialog = builder.create();
      alertDialog.show();
      Window window = alertDialog.getWindow();
      window.setContentView(R.layout.rc_cs_alert_warning);
      TextView tv = (TextView)window.findViewById(R.id.rc_cs_msg);
      tv.setText(msg);
      window.findViewById(R.id.rc_btn_ok).setOnClickListener(new OnClickListener() {
        public void onClick(View v) {
          InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
          imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
          alertDialog.dismiss();
          if (evaluate) {
            io.rong.imkit.fragment.ConversationFragment.this.onCustomServiceEvaluation(false, "", robotType, evaluate);
          } else {
            FragmentManager fm = io.rong.imkit.fragment.ConversationFragment.this.getChildFragmentManager();
            if (fm.getBackStackEntryCount() > 0) {
              fm.popBackStack();
            } else {
              io.rong.imkit.fragment.ConversationFragment.this.getActivity().finish();
            }
          }

        }
      });
    }
  }

  public boolean onCustomServiceEvaluation(boolean isPullEva, String dialogId, boolean robotType, boolean evaluate) {
    if (this.isActivityExist()) {
      if (evaluate) {
        long currentTime = System.currentTimeMillis();
        int interval = 60;

        try {
          interval = this.getActivity().getResources().getInteger(R.integer.rc_custom_service_evaluation_interval);
        } catch (NotFoundException var10) {
          var10.printStackTrace();
        }

        if (currentTime - this.csEnterTime < (long)(interval * 1000) && !isPullEva) {
          InputMethodManager imm = (InputMethodManager)this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
          if (imm != null && imm.isActive() && this.getActivity().getCurrentFocus() != null && this.getActivity().getCurrentFocus().getWindowToken() != null) {
            imm.hideSoftInputFromWindow(this.getActivity().getCurrentFocus().getWindowToken(), 2);
          }

          FragmentManager fm = this.getChildFragmentManager();
          if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
          } else {
            this.getActivity().finish();
          }

          return false;
        }

        this.mEvaluateDialg = new CSEvaluateDialog(this.getActivity(), this.mTargetId);
        this.mEvaluateDialg.setClickListener(this);
        this.mEvaluateDialg.setOnCancelListener(new OnCancelListener() {
          public void onCancel(DialogInterface dialog) {
            if (io.rong.imkit.fragment.ConversationFragment.this.mEvaluateDialg != null) {
              io.rong.imkit.fragment.ConversationFragment.this.mEvaluateDialg = null;
            }

          }
        });
        if (this.mCustomServiceConfig != null && this.mCustomServiceConfig.evaluateType.equals(CSEvaType.EVA_UNIFIED)) {
          this.mEvaluateDialg.showStarMessage(this.mCustomServiceConfig.isReportResolveStatus);
        } else if (robotType) {
          this.mEvaluateDialg.showRobot(true);
        } else {
          this.onShowStarAndTabletDialog(dialogId);
        }
      } else {
        FragmentManager fm = this.getChildFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
          fm.popBackStack();
        } else {
          this.getActivity().finish();
        }
      }
    }

    return true;
  }

  public void onShowStarAndTabletDialog(String dialogId) {
    this.mEvaluateDialg.showStar(dialogId);
  }

  public void onSendToggleClick(View v, String text) {
    if (!TextUtils.isEmpty(text) && !TextUtils.isEmpty(text.trim())) {
      TextMessage textMessage = TextMessage.obtain(text);
      MentionedInfo mentionedInfo = RongMentionManager.getInstance().onSendButtonClick();
      if (mentionedInfo != null) {
        textMessage.setMentionedInfo(mentionedInfo);
      }

      Message message = Message.obtain(this.mTargetId, this.mConversationType, textMessage);
      RongIM.getInstance().sendMessage(message, (String)null, (String)null, (ISendMessageCallback)null);
    } else {
      RLog.e("ConversationFragment", "text content must not be null");
    }
  }

  public void onImageResult(List<Uri> selectedImages, boolean origin) {
    SendImageManager.getInstance().sendImages(this.mConversationType, this.mTargetId, selectedImages, origin);
    if (this.mConversationType.equals(ConversationType.PRIVATE)) {
      RongIMClient.getInstance().sendTypingStatus(this.mConversationType, this.mTargetId, "RC:ImgMsg");
    }

  }

  public void onEditTextClick(EditText editText) {
  }

  public void onLocationResult(double lat, double lng, String poi, Uri thumb) {
    LocationMessage locationMessage = LocationMessage.obtain(lat, lng, poi, thumb);
    Message message = Message.obtain(this.mTargetId, this.mConversationType, locationMessage);
    RongIM.getInstance().sendLocationMessage(message, (String)null, (String)null, (ISendMessageCallback)null);
    if (this.mConversationType.equals(ConversationType.PRIVATE)) {
      RongIMClient.getInstance().sendTypingStatus(this.mConversationType, this.mTargetId, "RC:LBSMsg");
    }

  }

  public void onSwitchToggleClick(View v, ViewGroup inputBoard) {
    if (this.robotType) {
      RongIMClient.getInstance().switchToHumanMode(this.mTargetId);
    }

  }

  public void onVoiceInputToggleTouch(View v, MotionEvent event) {
    String[] permissions = new String[]{"android.permission.RECORD_AUDIO"};
    if (!PermissionCheckUtil.checkPermissions(this.getActivity(), permissions) && event.getAction() == 0) {
      PermissionCheckUtil.requestPermissions(this, permissions, 100);
    } else {
      if (event.getAction() == 0) {
        if (AudioPlayManager.getInstance().isPlaying()) {
          AudioPlayManager.getInstance().stopPlay();
        }

        if (!AudioPlayManager.getInstance().isInNormalMode(this.getActivity())) {
          Toast.makeText(this.getActivity(), this.getActivity().getString(R.string.rc_voip_occupying), Toast.LENGTH_SHORT).show();
          return;
        }

        AudioRecordManager.getInstance().startRecord(v.getRootView(), this.mConversationType, this.mTargetId);
        this.mLastTouchY = event.getY();
        this.mUpDirection = false;
        ((Button)v).setText(R.string.rc_audio_input_hover);
      } else if (event.getAction() == 2) {
        if (this.mLastTouchY - event.getY() > this.mOffsetLimit && !this.mUpDirection) {
          AudioRecordManager.getInstance().willCancelRecord();
          this.mUpDirection = true;
          ((Button)v).setText(R.string.rc_audio_input);
        } else if (event.getY() - this.mLastTouchY > -this.mOffsetLimit && this.mUpDirection) {
          AudioRecordManager.getInstance().continueRecord();
          this.mUpDirection = false;
          ((Button)v).setText(R.string.rc_audio_input_hover);
        }
      } else if (event.getAction() == 1 || event.getAction() == 3) {
        AudioRecordManager.getInstance().stopRecord();
        ((Button)v).setText(R.string.rc_audio_input);
      }

      if (this.mConversationType.equals(ConversationType.PRIVATE)) {
        RongIMClient.getInstance().sendTypingStatus(this.mConversationType, this.mTargetId, "RC:VcMsg");
      }

    }
  }

  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (requestCode == 100 && grantResults.length > 0 && grantResults[0] != 0) {
      this.mRongExtension.showRequestPermissionFailedAlter(this.getResources().getString(R.string.rc_permission_grant_needed));
    } else {
      this.mRongExtension.onRequestPermissionResult(requestCode, permissions, grantResults);
    }

  }

  public void onEmoticonToggleClick(View v, ViewGroup extensionBoard) {
  }

  public void onPluginToggleClick(View v, ViewGroup extensionBoard) {
  }

  public void beforeTextChanged(CharSequence s, int start, int count, int after) {
  }

  public void onTextChanged(CharSequence s, int start, int before, int count) {
    int cursor;
    int offset;
    if (count == 0) {
      cursor = start + before;
      offset = -before;
    } else {
      cursor = start;
      offset = count;
    }

    if (!this.mConversationType.equals(ConversationType.GROUP) && !this.mConversationType.equals(ConversationType.DISCUSSION)) {
      if (this.mConversationType.equals(ConversationType.PRIVATE) && offset != 0) {
        RongIMClient.getInstance().sendTypingStatus(this.mConversationType, this.mTargetId, "RC:TxtMsg");
      }
    } else {
      RongMentionManager.getInstance().onTextEdit(this.mConversationType, this.mTargetId, cursor, offset, s.toString());
    }

  }

  public void afterTextChanged(Editable s) {
  }

  public boolean onKey(View v, int keyCode, KeyEvent event) {
    if (event.getKeyCode() == 67 && event.getAction() == 0) {
      EditText editText = (EditText)v;
      int cursorPos = editText.getSelectionStart();
      RongMentionManager.getInstance().onDeleteClick(this.mConversationType, this.mTargetId, editText, cursorPos);
    }

    return false;
  }

  public void onMenuClick(int root, int sub) {
    if (this.mPublicServiceProfile != null) {
      PublicServiceMenuItem item = (PublicServiceMenuItem)this.mPublicServiceProfile.getMenu().getMenuItems().get(root);
      if (sub >= 0) {
        item = (PublicServiceMenuItem)item.getSubMenuItems().get(sub);
      }

      if (item.getType().equals(PublicServiceMenuItemType.View)) {
        IPublicServiceMenuClickListener menuClickListener = RongContext.getInstance().getPublicServiceMenuClickListener();
        if (menuClickListener == null || !menuClickListener.onClick(this.mConversationType, this.mTargetId, item)) {
          String action = "io.io.rong.imkit.intent.action.webview";
          Intent intent = new Intent(action);
          intent.setPackage(this.getActivity().getPackageName());
          intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          intent.putExtra("url", item.getUrl());
          this.getActivity().startActivity(intent);
        }
      }

      PublicServiceCommandMessage msg = PublicServiceCommandMessage.obtain(item);
      RongIMClient.getInstance().sendMessage(this.mConversationType, this.mTargetId, msg, (String)null, (String)null, new ISendMessageCallback() {
        public void onAttached(Message message) {
        }

        public void onSuccess(Message message) {
        }

        public void onError(Message message, ErrorCode errorCode) {
        }
      });
    }

  }

  public void onPluginClicked(IPluginModule pluginModule, int position) {
  }

  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == 102) {
      this.getActivity().finish();
    } else {
      this.mRongExtension.onActivityPluginResult(requestCode, resultCode, data);
    }

  }

  private String getNameFromCache(String targetId) {
    UserInfo info = RongContext.getInstance().getUserInfoFromCache(targetId);
    return info == null ? targetId : info.getName();
  }

  public void onEventMainThread(ReadReceiptRequestEvent event) {
    RLog.d("ConversationFragment", "ReadReceiptRequestEvent");
    if ((this.mConversationType.equals(ConversationType.GROUP) || this.mConversationType.equals(ConversationType.DISCUSSION)) && RongContext.getInstance().isReadReceiptConversationType(event.getConversationType()) && event.getConversationType().equals(this.mConversationType) && event.getTargetId().equals(this.mTargetId)) {
      for(int i = 0; i < this.mListAdapter.getCount(); ++i) {
        if (((UIMessage)this.mListAdapter.getItem(i)).getUId().equals(event.getMessageUId())) {
          final UIMessage uiMessage = (UIMessage)this.mListAdapter.getItem(i);
          ReadReceiptInfo readReceiptInfo = uiMessage.getReadReceiptInfo();
          if (readReceiptInfo == null) {
            readReceiptInfo = new ReadReceiptInfo();
            uiMessage.setReadReceiptInfo(readReceiptInfo);
          }

          if (readReceiptInfo.isReadReceiptMessage() && readReceiptInfo.hasRespond()) {
            return;
          }

          readReceiptInfo.setIsReadReceiptMessage(true);
          readReceiptInfo.setHasRespond(false);
          List<Message> messageList = new ArrayList();
          messageList.add(((UIMessage)this.mListAdapter.getItem(i)).getMessage());
          RongIMClient.getInstance().sendReadReceiptResponse(event.getConversationType(), event.getTargetId(), messageList, new OperationCallback() {
            public void onSuccess() {
              uiMessage.getReadReceiptInfo().setHasRespond(true);
            }

            public void onError(ErrorCode errorCode) {
              RLog.e("ConversationFragment", "sendReadReceiptResponse failed, errorCode = " + errorCode);
            }
          });
          break;
        }
      }
    }

  }

  public void onEventMainThread(ReadReceiptResponseEvent event) {
    RLog.d("ConversationFragment", "ReadReceiptResponseEvent");
    if ((this.mConversationType.equals(ConversationType.GROUP) || this.mConversationType.equals(ConversationType.DISCUSSION)) && RongContext.getInstance().isReadReceiptConversationType(event.getConversationType()) && event.getConversationType().equals(this.mConversationType) && event.getTargetId().equals(this.mTargetId)) {
      for(int i = 0; i < this.mListAdapter.getCount(); ++i) {
        if (((UIMessage)this.mListAdapter.getItem(i)).getUId().equals(event.getMessageUId())) {
          UIMessage uiMessage = (UIMessage)this.mListAdapter.getItem(i);
          ReadReceiptInfo readReceiptInfo = uiMessage.getReadReceiptInfo();
          if (readReceiptInfo == null) {
            readReceiptInfo = new ReadReceiptInfo();
            readReceiptInfo.setIsReadReceiptMessage(true);
            uiMessage.setReadReceiptInfo(readReceiptInfo);
          }

          readReceiptInfo.setRespondUserIdList(event.getResponseUserIdList());
          int first = this.mList.getFirstVisiblePosition();
          int last = this.mList.getLastVisiblePosition();
          int position = this.getPositionInListView(i);
          if (position >= first && position <= last) {
            this.mListAdapter.getView(i, this.getListViewChildAt(i), this.mList);
          }
          break;
        }
      }
    }

  }

  public void onEventMainThread(MessageDeleteEvent deleteEvent) {
    RLog.d("ConversationFragment", "MessageDeleteEvent");
    if (deleteEvent.getMessageIds() != null) {
      Iterator var2 = deleteEvent.getMessageIds().iterator();

      while(var2.hasNext()) {
        long messageId = (long)(Integer)var2.next();
        int position = this.mListAdapter.findPosition(messageId);
        if (position >= 0) {
          this.mListAdapter.remove(position);
        }
      }

      this.mListAdapter.notifyDataSetChanged();
    }

  }

  public void onEventMainThread(PublicServiceFollowableEvent event) {
    RLog.d("ConversationFragment", "PublicServiceFollowableEvent");
    if (event != null && !event.isFollow()) {
      this.getActivity().finish();
    }

  }

  public void onEventMainThread(MessagesClearEvent clearEvent) {
    RLog.d("ConversationFragment", "MessagesClearEvent");
    if (clearEvent.getTargetId().equals(this.mTargetId) && clearEvent.getType().equals(this.mConversationType)) {
      this.mListAdapter.clear();
      this.mListAdapter.notifyDataSetChanged();
    }

  }

  public void onEventMainThread(MessageRecallEvent event) {
    RLog.d("ConversationFragment", "MessageRecallEvent");
    if (event.isRecallSuccess()) {
      RecallNotificationMessage recallNotificationMessage = event.getRecallNotificationMessage();
      int position = this.mListAdapter.findPosition((long)event.getMessageId());
      if (position != -1) {
        UIMessage uiMessage = (UIMessage)this.mListAdapter.getItem(position);
        if (uiMessage.getMessage().getContent() instanceof VoiceMessage) {
          AudioPlayManager.getInstance().stopPlay();
        }

        if (uiMessage.getMessage().getContent() instanceof FileMessage) {
          RongIM.getInstance().cancelDownloadMediaMessage(uiMessage.getMessage(), (OperationCallback)null);
        }

        ((UIMessage)this.mListAdapter.getItem(position)).setContent(recallNotificationMessage);
        int first = this.mList.getFirstVisiblePosition();
        int last = this.mList.getLastVisiblePosition();
        int listPos = this.getPositionInListView(position);
        if (listPos >= first && listPos <= last) {
          this.mListAdapter.getView(position, this.getListViewChildAt(position), this.mList);
        }
      }
    } else {
      Toast.makeText(this.getActivity(), R.string.rc_recall_failed, Toast.LENGTH_SHORT).show();
    }

  }

  public void onEventMainThread(RemoteMessageRecallEvent event) {
    RLog.d("ConversationFragment", "RemoteMessageRecallEvent");
    int position = this.mListAdapter.findPosition((long)event.getMessageId());
    int first = this.mList.getFirstVisiblePosition();
    int last = this.mList.getLastVisiblePosition();
    if (position >= 0) {
      UIMessage uiMessage = (UIMessage)this.mListAdapter.getItem(position);
      if (uiMessage.getMessage().getContent() instanceof VoiceMessage) {
        AudioPlayManager.getInstance().stopPlay();
      }

      if (uiMessage.getMessage().getContent() instanceof FileMessage) {
        RongIM.getInstance().cancelDownloadMediaMessage(uiMessage.getMessage(), (OperationCallback)null);
      }

      uiMessage.setContent(event.getRecallNotificationMessage());
      int listPos = this.getPositionInListView(position);
      if (listPos >= first && listPos <= last) {
        this.mListAdapter.getView(position, this.getListViewChildAt(position), this.mList);
      }
    }

  }

  public void onEventMainThread(Message msg) {
    RLog.d("ConversationFragment", "Event message : " + msg.getMessageId() + ", " + msg.getObjectName() + ", " + msg.getSentStatus());
    if (this.mTargetId.equals(msg.getTargetId()) && this.mConversationType.equals(msg.getConversationType()) && msg.getMessageId() > 0) {
      int position = this.mListAdapter.findPosition((long)msg.getMessageId());
      if (position >= 0) {
        if (msg.getSentStatus().equals(SentStatus.FAILED)) {
          long serverTime = msg.getSentTime() - RongIMClient.getInstance().getDeltaTime();
          msg.setSentTime(serverTime);
        }

        ((UIMessage)this.mListAdapter.getItem(position)).setMessage(msg);
        this.mListAdapter.getView(position, this.getListViewChildAt(position), this.mList);
      } else {
        UIMessage uiMessage = UIMessage.obtain(msg);
        if (msg.getContent() instanceof CSPullLeaveMessage) {
          uiMessage.setCsConfig(this.mCustomServiceConfig);
        }

        long sentTime = uiMessage.getSentTime();
        if (uiMessage.getMessageDirection() == MessageDirection.SEND && uiMessage.getSentStatus() == SentStatus.SENDING) {
          sentTime = uiMessage.getSentTime() - RongIMClient.getInstance().getDeltaTime();
          uiMessage.setSentTime(sentTime);
        }

        int insertPosition = this.mListAdapter.getPositionBySendTime(sentTime);
        this.mListAdapter.add(uiMessage, insertPosition);
        this.mListAdapter.notifyDataSetChanged();
      }

      MessageTag msgTag = (MessageTag)msg.getContent().getClass().getAnnotation(MessageTag.class);
      if (this.mNewMessageCount <= 0 && msgTag != null && (msgTag.flag() == 3 || this.mList.getLastVisiblePosition() == this.mList.getCount() - this.mList.getHeaderViewsCount() - 1)) {
        this.mList.setTranscriptMode(2);
        this.mList.post(new Runnable() {
          public void run() {
            if (io.rong.imkit.fragment.ConversationFragment.this.getActivity() != null && io.rong.imkit.fragment.ConversationFragment.this.mList != null) {
              io.rong.imkit.fragment.ConversationFragment.this.mList.setSelection(io.rong.imkit.fragment.ConversationFragment.this.mListAdapter.getCount());
            }

          }
        });
        this.mList.setTranscriptMode(0);
      }

      if (this.mConversationType.equals(ConversationType.CUSTOMER_SERVICE) && msg.getMessageDirection() == MessageDirection.SEND && !this.robotType && this.mCustomServiceConfig != null && this.mCustomServiceConfig.userTipTime > 0 && !TextUtils.isEmpty(this.mCustomServiceConfig.userTipWord)) {
        this.startTimer(0, this.mCustomServiceConfig.userTipTime * 60 * 1000);
      }
    }

  }

  public void onEventMainThread(MessageSentStatusUpdateEvent event) {
    Message message = event.getMessage();
    if (message != null && !message.getMessageDirection().equals(MessageDirection.RECEIVE)) {
      RLog.d("ConversationFragment", "MessageSentStatusEvent event : " + event.getMessage().getMessageId() + ", " + event.getSentStatus());
      int position = this.mListAdapter.findPosition((long)message.getMessageId());
      if (position >= 0) {
        ((UIMessage)this.mListAdapter.getItem(position)).setSentStatus(event.getSentStatus());
        this.mListAdapter.getView(position, this.getListViewChildAt(position), this.mList);
      }

    } else {
      RLog.e("ConversationFragment", "MessageSentStatusUpdateEvent message is null or direction is RECEIVE");
    }
  }

  public void onEventMainThread(FileMessageEvent event) {
    Message msg = event.getMessage();
    RLog.d("ConversationFragment", "FileMessageEvent message : " + msg.getMessageId() + ", " + msg.getObjectName() + ", " + msg.getSentStatus());
    if (this.mTargetId.equals(msg.getTargetId()) && this.mConversationType.equals(msg.getConversationType()) && msg.getMessageId() > 0 && msg.getContent() instanceof MediaMessageContent) {
      int position = this.mListAdapter.findPosition((long)msg.getMessageId());
      if (position >= 0) {
        UIMessage uiMessage = (UIMessage)this.mListAdapter.getItem(position);
        uiMessage.setMessage(msg);
        uiMessage.setProgress(event.getProgress());
        if (msg.getContent() instanceof FileMessage) {
          ((FileMessage)msg.getContent()).progress = event.getProgress();
        }

        ((UIMessage)this.mListAdapter.getItem(position)).setMessage(msg);
      }
    }

  }

  public void onEventMainThread(GroupUserInfo groupUserInfo) {
    RLog.d("ConversationFragment", "GroupUserInfoEvent " + groupUserInfo.getGroupId() + " " + groupUserInfo.getUserId() + " " + groupUserInfo.getNickname());
    if (groupUserInfo.getNickname() != null && groupUserInfo.getGroupId() != null) {
      int count = this.mListAdapter.getCount();
      int first = this.mList.getFirstVisiblePosition();
      int last = this.mList.getLastVisiblePosition();

      for(int i = 0; i < count; ++i) {
        UIMessage uiMessage = (UIMessage)this.mListAdapter.getItem(i);
        if (uiMessage.getSenderUserId().equals(groupUserInfo.getUserId())) {
          uiMessage.setNickName(true);
          UserInfo userInfo = uiMessage.getUserInfo();
          if (userInfo != null) {
            userInfo.setName(groupUserInfo.getNickname());
            uiMessage.setUserInfo(userInfo);
          }

          int pos = this.getPositionInListView(i);
          if (pos >= first && pos <= last) {
            this.mListAdapter.getView(i, this.getListViewChildAt(i), this.mList);
          }
        }
      }

    }
  }

  private View getListViewChildAt(int adapterIndex) {
    int header = this.mList.getHeaderViewsCount();
    int first = this.mList.getFirstVisiblePosition();
    return this.mList.getChildAt(adapterIndex + header - first);
  }

  private int getPositionInListView(int adapterIndex) {
    int header = this.mList.getHeaderViewsCount();
    return adapterIndex + header;
  }

  private int getPositionInAdapter(int listIndex) {
    int header = this.mList.getHeaderViewsCount();
    return listIndex <= 0 ? 0 : listIndex - header;
  }

  private void showNewMessage() {
    if (this.mNewMessageCount > 0 && this.mNewMessageBtn != null) {
      this.mNewMessageBtn.setVisibility(View.VISIBLE);
      if (this.mNewMessageTextView != null) {
        this.mNewMessageTextView.setVisibility(View.VISIBLE);
        if (this.mNewMessageCount > 99) {
          this.mNewMessageTextView.setText("99+");
        } else {
          this.mNewMessageTextView.setText(this.mNewMessageCount + "");
        }
      }
    }

  }

  public void onEventMainThread(OnMessageSendErrorEvent event) {
    this.onEventMainThread(event.getMessage());
  }

  public void onEventMainThread(OnReceiveMessageEvent event) {
    Message message = event.getMessage();
    RLog.i("ConversationFragment", "OnReceiveMessageEvent, " + message.getMessageId() + ", " + message.getObjectName() + ", " + message.getReceivedStatus().toString());
    ConversationType conversationType = message.getConversationType();
    String targetId = message.getTargetId();
    if (this.mConversationType.equals(conversationType) && this.mTargetId.equals(targetId) && this.shouldUpdateMessage(message, event.getLeft())) {
      if (event.getLeft() == 0 && message.getConversationType().equals(ConversationType.PRIVATE) && RongContext.getInstance().isReadReceiptConversationType(ConversationType.PRIVATE) && message.getMessageDirection().equals(MessageDirection.RECEIVE)) {
        if (this.mReadRec && !TextUtils.isEmpty(message.getUId())) {
          RongIMClient.getInstance().sendReadReceiptMessage(message.getConversationType(), message.getTargetId(), message.getSentTime());
        }

        if (!this.mReadRec && this.mSyncReadStatus) {
          RongIMClient.getInstance().syncConversationReadStatus(message.getConversationType(), message.getTargetId(), message.getSentTime(), (OperationCallback)null);
        }
      }

      if (this.mSyncReadStatus) {
        this.mSyncReadStatusMsgTime = message.getSentTime();
      }

      if (message.getMessageId() > 0) {
        if (!SystemUtils.isInBackground(this.getActivity())) {
          message.getReceivedStatus().setRead();
          RongIMClient.getInstance().setMessageReceivedStatus(message.getMessageId(), message.getReceivedStatus(), (ResultCallback)null);
          if (message.getMessageDirection().equals(MessageDirection.RECEIVE)) {
            UnReadMessageManager.getInstance().onMessageReceivedStatusChanged();
          }
        }

        if (this.mConversationType.equals(ConversationType.CUSTOMER_SERVICE) && !this.robotType && this.mCustomServiceConfig != null && this.mCustomServiceConfig.adminTipTime > 0 && !TextUtils.isEmpty(this.mCustomServiceConfig.adminTipWord)) {
          this.startTimer(1, this.mCustomServiceConfig.adminTipTime * 60 * 1000);
        }
      }

      RLog.d("ConversationFragment", "mList.getCount(): " + this.mList.getCount() + " getLastVisiblePosition:" + this.mList.getLastVisiblePosition());
      if (this.mNewMessageBtn != null && this.mList.getCount() - this.mList.getLastVisiblePosition() > 2 && MessageDirection.SEND != message.getMessageDirection() && message.getConversationType() != ConversationType.CHATROOM && message.getConversationType() != ConversationType.CUSTOMER_SERVICE && message.getConversationType() != ConversationType.APP_PUBLIC_SERVICE && message.getConversationType() != ConversationType.PUBLIC_SERVICE) {
        ++this.mNewMessageCount;
        this.showNewMessage();
      }

      this.onEventMainThread(event.getMessage());
    }

  }

  public void onEventBackgroundThread(final PlayAudioEvent event) {
    this.getHandler().post(new Runnable() {
      public void run() {
        io.rong.imkit.fragment.ConversationFragment.this.handleAudioPlayEvent(event);
      }
    });
  }

  private void handleAudioPlayEvent(PlayAudioEvent event) {
    RLog.i("ConversationFragment", "PlayAudioEvent");
    int first = this.mList.getFirstVisiblePosition();
    int last = this.mList.getLastVisiblePosition();
    int position = this.mListAdapter.findPosition((long)event.messageId);
    if (event.continuously && position >= 0) {
      while(first <= last) {
        ++position;
        ++first;
        UIMessage uiMessage = (UIMessage)this.mListAdapter.getItem(position);
        if (uiMessage != null && uiMessage.getContent() instanceof VoiceMessage && uiMessage.getMessageDirection().equals(MessageDirection.RECEIVE) && !uiMessage.getReceivedStatus().isListened()) {
          uiMessage.continuePlayAudio = true;
          this.mListAdapter.getView(position, this.getListViewChildAt(position), this.mList);
          break;
        }
      }
    }

  }

  public void onEventMainThread(OnReceiveMessageProgressEvent event) {
    if (this.mList != null) {
      int first = this.mList.getFirstVisiblePosition();

      for(int last = this.mList.getLastVisiblePosition(); first <= last; ++first) {
        int position = this.getPositionInAdapter(first);
        UIMessage uiMessage = (UIMessage)this.mListAdapter.getItem(position);
        if (uiMessage.getMessageId() == event.getMessage().getMessageId()) {
          uiMessage.setProgress(event.getProgress());
          if (this.isResumed()) {
            this.mListAdapter.getView(position, this.getListViewChildAt(position), this.mList);
          }
          break;
        }
      }
    }

  }

  public void onEventMainThread(ConnectEvent event) {
    RLog.i("ConversationFragment", "ConnectEvent : " + event.getConnectStatus());
    if (this.mListAdapter.getCount() == 0) {
      Mode mode = this.indexMessageTime > 0L ? Mode.END : Mode.START;
      int scrollMode = this.indexMessageTime > 0L ? 1 : 3;
      this.getHistoryMessage(this.mConversationType, this.mTargetId, 10, mode, scrollMode, -1);
    }

  }

  public void onEventMainThread(UserInfo userInfo) {
    RLog.i("ConversationFragment", "userInfo " + userInfo.getUserId());
    int first = this.mList.getFirstVisiblePosition();
    int last = this.mList.getLastVisiblePosition();

    for(int i = 0; i < this.mListAdapter.getCount(); ++i) {
      UIMessage uiMessage = (UIMessage)this.mListAdapter.getItem(i);
      if (userInfo.getUserId().equals(uiMessage.getSenderUserId()) && !uiMessage.isNickName()) {
        if (uiMessage.getConversationType().equals(ConversationType.CUSTOMER_SERVICE) && uiMessage.getMessage() != null && uiMessage.getMessage().getContent() != null && uiMessage.getMessage().getContent().getUserInfo() != null) {
          uiMessage.setUserInfo(uiMessage.getMessage().getContent().getUserInfo());
        } else {
          uiMessage.setUserInfo(userInfo);
        }

        int position = this.getPositionInListView(i);
        if (position >= first && position <= last) {
          this.mListAdapter.getView(i, this.getListViewChildAt(i), this.mList);
        }
      }
    }

  }

  public void onEventMainThread(PublicServiceProfile publicServiceProfile) {
    RLog.i("ConversationFragment", "publicServiceProfile");
    if (publicServiceProfile != null && this.mConversationType.equals(publicServiceProfile.getConversationType()) && this.mTargetId.equals(publicServiceProfile.getTargetId())) {
      int first = this.mList.getFirstVisiblePosition();

      for(int last = this.mList.getLastVisiblePosition(); first <= last; ++first) {
        int position = this.getPositionInAdapter(first);
        UIMessage message = (UIMessage)this.mListAdapter.getItem(position);
        if (message != null && (TextUtils.isEmpty(message.getTargetId()) || publicServiceProfile.getTargetId().equals(message.getTargetId()))) {
          this.mListAdapter.getView(position, this.getListViewChildAt(position), this.mList);
        }
      }

      this.updatePublicServiceMenu(publicServiceProfile);
    }

  }

  public void onEventMainThread(ReadReceiptEvent event) {
    RLog.i("ConversationFragment", "ReadReceiptEvent");
    if (RongContext.getInstance().isReadReceiptConversationType(event.getMessage().getConversationType()) && this.mTargetId.equals(event.getMessage().getTargetId()) && this.mConversationType.equals(event.getMessage().getConversationType()) && event.getMessage().getMessageDirection().equals(MessageDirection.RECEIVE)) {
      ReadReceiptMessage content = (ReadReceiptMessage)event.getMessage().getContent();
      long ntfTime = content.getLastMessageSendTime();

      for(int i = this.mListAdapter.getCount() - 1; i >= 0; --i) {
        UIMessage uiMessage = (UIMessage)this.mListAdapter.getItem(i);
        if (uiMessage.getMessageDirection().equals(MessageDirection.SEND) && uiMessage.getSentStatus() == SentStatus.SENT && ntfTime >= uiMessage.getSentTime()) {
          uiMessage.setSentStatus(SentStatus.READ);
          int first = this.mList.getFirstVisiblePosition();
          int last = this.mList.getLastVisiblePosition();
          int position = this.getPositionInListView(i);
          if (position >= first && position <= last) {
            this.mListAdapter.getView(i, this.getListViewChildAt(i), this.mList);
          }
        }
      }
    }

  }

  public MessageListAdapter getMessageAdapter() {
    return this.mListAdapter;
  }

  public boolean shouldUpdateMessage(Message message, int left) {
    return true;
  }

  public void getHistoryMessage(ConversationType conversationType, String targetId, int lastMessageId, int reqCount, io.rong.imkit.fragment.ConversationFragment.LoadMessageDirection direction, final IHistoryDataResultCallback<List<Message>> callback) {
    if (direction == io.rong.imkit.fragment.ConversationFragment.LoadMessageDirection.UP) {
      RongIMClient.getInstance().getHistoryMessages(conversationType, targetId, lastMessageId, reqCount, new ResultCallback<List<Message>>() {
        public void onSuccess(List<Message> messages) {
          if (callback != null) {
            callback.onResult(messages);
          }

        }

        public void onError(ErrorCode e) {
          RLog.e("ConversationFragment", "getHistoryMessages " + e);
          if (callback != null) {
            callback.onResult(null);
          }

        }
      });
    } else {
      int before = 10;
      int after = 10;
      if (this.mListAdapter.getCount() > 0 || this.indexMessageTime != 0L || this.isClickUnread) {
        after = 10;
        before = 0;
      }

      RongIMClient.getInstance().getHistoryMessages(conversationType, targetId, this.indexMessageTime, before, after, new ResultCallback<List<Message>>() {
        public void onSuccess(List<Message> messages) {
          if (callback != null) {
            callback.onResult(messages);
          }

          if (messages != null && messages.size() > 0 && io.rong.imkit.fragment.ConversationFragment.this.mHasMoreLocalMessagesDown) {
            io.rong.imkit.fragment.ConversationFragment.this.indexMessageTime = ((Message)messages.get(0)).getSentTime();
          } else {
            io.rong.imkit.fragment.ConversationFragment.this.indexMessageTime = 0L;
          }

        }

        public void onError(ErrorCode e) {
          RLog.e("ConversationFragment", "getHistoryMessages " + e);
          if (callback != null) {
            callback.onResult(null);
          }

          io.rong.imkit.fragment.ConversationFragment.this.indexMessageTime = 0L;
        }
      });
    }

  }

  private void getHistoryMessage(ConversationType conversationType, String targetId, final int reqCount, Mode mode, final int scrollMode, int messageId) {
    this.mList.onRefreshStart(mode);
    if (conversationType.equals(ConversationType.CHATROOM)) {
      this.mList.onRefreshComplete(0, 0, false);
      RLog.w("ConversationFragment", "Should not get local message in chatroom");
    } else {
      int fromMsgId = messageId;
      if (messageId < 0) {
        if (this.mListAdapter.getCount() == 0) {
          fromMsgId = -1;
        } else if (((UIMessage)this.mListAdapter.getItem(0)).getMessage().getContent() instanceof HistoryDividerMessage) {
          fromMsgId = this.firstUnreadMessage.getMessageId();
        } else {
          fromMsgId = ((UIMessage)this.mListAdapter.getItem(0)).getMessageId();
        }
      }

      final io.rong.imkit.fragment.ConversationFragment.LoadMessageDirection direction = mode == Mode.START ? io.rong.imkit.fragment.ConversationFragment.LoadMessageDirection.UP : io.rong.imkit.fragment.ConversationFragment.LoadMessageDirection.DOWN;
      int finalFromMsgId = fromMsgId;
      this.getHistoryMessage(conversationType, targetId, fromMsgId, reqCount, direction, new IHistoryDataResultCallback<List<Message>>() {
        public void onResult(List<Message> messages) {
          int msgCount = messages == null ? 0 : messages.size();
          RLog.i("ConversationFragment", "getHistoryMessage " + msgCount);
          if (direction == io.rong.imkit.fragment.ConversationFragment.LoadMessageDirection.DOWN) {
            io.rong.imkit.fragment.ConversationFragment.this.mList.onRefreshComplete(msgCount > 1 ? msgCount : 0, msgCount, false);
            io.rong.imkit.fragment.ConversationFragment.this.mHasMoreLocalMessagesDown = msgCount > 1;
          } else {
            io.rong.imkit.fragment.ConversationFragment.this.mList.onRefreshComplete(msgCount, reqCount, false);
            io.rong.imkit.fragment.ConversationFragment.this.mHasMoreLocalMessagesUp = msgCount == reqCount;
          }

          if (messages != null && messages.size() > 0) {
            int index = 0;
            if (direction == io.rong.imkit.fragment.ConversationFragment.LoadMessageDirection.DOWN) {
              index = io.rong.imkit.fragment.ConversationFragment.this.mListAdapter.getCount();
            }

            boolean needRefresh = false;
            Iterator var5 = messages.iterator();

            while(var5.hasNext()) {
              Message message = (Message)var5.next();
              boolean contains = false;

              for(int i = 0; i < io.rong.imkit.fragment.ConversationFragment.this.mListAdapter.getCount(); ++i) {
                contains = ((UIMessage) io.rong.imkit.fragment.ConversationFragment.this.mListAdapter.getItem(i)).getMessageId() == message.getMessageId();
                if (contains) {
                  break;
                }
              }

              if (!contains) {
                UIMessage uiMessage = UIMessage.obtain(message);
                if (message.getContent() != null && message.getContent().getUserInfo() != null) {
                  uiMessage.setUserInfo(message.getContent().getUserInfo());
                }

                if (message.getContent() instanceof CSPullLeaveMessage) {
                  uiMessage.setCsConfig(io.rong.imkit.fragment.ConversationFragment.this.mCustomServiceConfig);
                }

                io.rong.imkit.fragment.ConversationFragment.this.mListAdapter.add(uiMessage, index);
                needRefresh = true;
              }
            }

            if (io.rong.imkit.fragment.ConversationFragment.this.firstUnreadMessage != null) {
              io.rong.imkit.fragment.ConversationFragment.this.refreshUnreadUI();
            }

            io.rong.imkit.fragment.ConversationFragment.this.refreshUI(messages, needRefresh, index, scrollMode, finalFromMsgId, direction);
          }

        }

        public void onError() {
          io.rong.imkit.fragment.ConversationFragment.this.mList.onRefreshComplete(reqCount, reqCount, false);
        }
      });
    }
  }

  private void refreshUnreadUI() {
    boolean historyDividerInserted = false;
    if (this.firstUnreadMessage != null) {
      Message hisMessage;
      UIMessage hisUIMessage;
      List msgList;
      int insertPosition;
      if (this.conversationUnreadCount > 10) {
        if (this.conversationUnreadCount <= 10) {
          hisMessage = Message.obtain(this.mTargetId, this.mConversationType, HistoryDividerMessage.obtain(this.getResources().getString(R.string.rc_new_message_divider_content)));
          hisUIMessage = UIMessage.obtain(hisMessage);
          insertPosition = this.mListAdapter.findPosition((long)this.firstUnreadMessage.getMessageId());
          if (insertPosition == 0) {
            msgList = RongIM.getInstance().getHistoryMessages(this.mConversationType, this.mTargetId, this.firstUnreadMessage.getMessageId(), 1);
            if (msgList != null && msgList.size() == 1) {
              hisUIMessage.setSentTime(((Message)msgList.get(0)).getSentTime());
              this.mListAdapter.add(hisUIMessage, insertPosition);
              historyDividerInserted = true;
            }
          } else if (insertPosition > 0) {
            hisUIMessage.setSentTime(((UIMessage)this.mListAdapter.getItem(insertPosition - 1)).getSentTime());
            this.mListAdapter.add(hisUIMessage, insertPosition);
            historyDividerInserted = true;
          }

          this.conversationUnreadCount = 0;
        } else if (this.conversationUnreadCount < this.mListAdapter.getCount() && this.isAdded()) {
          hisMessage = Message.obtain(this.mTargetId, this.mConversationType, HistoryDividerMessage.obtain(this.getResources().getString(R.string.rc_new_message_divider_content)));
          hisUIMessage = UIMessage.obtain(hisMessage);
          insertPosition = this.mListAdapter.findPosition((long)this.firstUnreadMessage.getMessageId());
          if (insertPosition > 0) {
            hisUIMessage.setSentTime(((UIMessage)this.mListAdapter.getItem(insertPosition - 1)).getSentTime());
            this.mListAdapter.add(hisUIMessage, insertPosition);
            historyDividerInserted = true;
          }

          this.conversationUnreadCount = 0;
        }
      }

      if (this.isClickUnread) {
        this.isClickUnread = false;
        if (!historyDividerInserted && this.getActivity() != null && this.isAdded()) {
          hisMessage = Message.obtain(this.mTargetId, this.mConversationType, HistoryDividerMessage.obtain(this.getActivity().getResources().getString(R.string.rc_new_message_divider_content)));
          hisUIMessage = UIMessage.obtain(hisMessage);
          insertPosition = this.mListAdapter.findPosition((long)this.firstUnreadMessage.getMessageId());
          if (insertPosition == 0) {
            msgList = RongIM.getInstance().getHistoryMessages(this.mConversationType, this.mTargetId, this.firstUnreadMessage.getMessageId(), 1);
            long sentTime = 0L;
            if (msgList != null && msgList.size() == 1) {
              sentTime = ((Message)msgList.get(0)).getSentTime();
            }

            if (sentTime > 0L) {
              hisUIMessage.setSentTime(sentTime);
              this.mListAdapter.add(hisUIMessage, insertPosition);
            }
          } else if (insertPosition > 0) {
            hisUIMessage.setSentTime(((UIMessage)this.mListAdapter.getItem(insertPosition - 1)).getSentTime());
            this.mListAdapter.add(hisUIMessage, insertPosition);
          }
        }

        this.conversationUnreadCount = 0;
      }

    }
  }

  private void refreshUI(List<Message> messages, boolean needRefresh, int index, int scrollMode, int last, io.rong.imkit.fragment.ConversationFragment.LoadMessageDirection direction) {
    if (needRefresh) {
      this.mListAdapter.notifyDataSetChanged();
      if (this.mLastMentionMsgId > 0) {
        index = this.mListAdapter.findPosition((long)this.mLastMentionMsgId);
        this.mList.setSelection(index);
        this.mLastMentionMsgId = 0;
      } else if (2 == scrollMode) {
        this.mList.setSelection(0);
      } else if (scrollMode == 3) {
        if (last == -1 && this.mSavedInstanceState != null) {
          this.mList.onRestoreInstanceState(this.mListViewState);
        } else {
          this.mList.setSelection(this.mListAdapter.getCount());
        }
      } else if (direction == io.rong.imkit.fragment.ConversationFragment.LoadMessageDirection.DOWN) {
        int selected = this.mList.getSelectedItemPosition();
        if (selected <= 0) {
          for(int i = 0; i < this.mListAdapter.getCount(); ++i) {
            if (((UIMessage)this.mListAdapter.getItem(i)).getSentTime() == this.indexMessageTime) {
              this.mList.setSelection(i);
              break;
            }
          }
        } else {
          this.mList.setSelection(this.mListAdapter.getCount() - messages.size());
        }
      } else {
        this.mList.setSelection(messages.size() + 1);
      }

      this.sendReadReceiptResponseIfNeeded(messages);
      if (RongContext.getInstance().getUnreadMessageState() && last == -1 && this.mUnReadCount > 10) {
        this.mList.postDelayed(new Runnable() {
          public void run() {
            io.rong.imkit.fragment.ConversationFragment.this.mList.addOnScrollListener(io.rong.imkit.fragment.ConversationFragment.this.mOnScrollListener);
          }
        }, 100L);
      }
    }

  }

  public void getRemoteHistoryMessages(ConversationType conversationType, String targetId, long dateTime, int reqCount, final IHistoryDataResultCallback<List<Message>> callback) {
    RongIMClient.getInstance().getRemoteHistoryMessages(conversationType, targetId, dateTime, reqCount, new ResultCallback<List<Message>>() {
      public void onSuccess(List<Message> messages) {
        if (callback != null) {
          callback.onResult(messages);
        }

      }

      public void onError(ErrorCode e) {
        RLog.e("ConversationFragment", "getRemoteHistoryMessages " + e);
        if (callback != null) {
          callback.onResult(null);
        }

      }
    });
  }

  private void getRemoteHistoryMessages(ConversationType conversationType, String targetId, final int reqCount) {
    this.mList.onRefreshStart(Mode.START);
    if (this.mConversationType.equals(ConversationType.CHATROOM)) {
      this.mList.onRefreshComplete(0, 0, false);
      RLog.w("ConversationFragment", "Should not get remote message in chatroom");
    } else {
      long dateTime = this.mListAdapter.getCount() == 0 ? 0L : ((UIMessage)this.mListAdapter.getItem(0)).getSentTime();
      this.getRemoteHistoryMessages(conversationType, targetId, dateTime, reqCount, new IHistoryDataResultCallback<List<Message>>() {
        public void onResult(List<Message> messages) {
          RLog.i("ConversationFragment", "getRemoteHistoryMessages " + (messages == null ? 0 : messages.size()));
          Message lastMessage = null;
          if (messages != null && messages.size() > 0) {
            if (io.rong.imkit.fragment.ConversationFragment.this.mListAdapter.getCount() == 0) {
              lastMessage = (Message)messages.get(0);
            }

            List<UIMessage> remoteListx = new ArrayList();
            Iterator var4 = messages.iterator();

            while(var4.hasNext()) {
              Message message = (Message)var4.next();
              if (message.getMessageId() > 0) {
                UIMessage uiMessage = UIMessage.obtain(message);
                if (message.getContent() instanceof CSPullLeaveMessage) {
                  uiMessage.setCsConfig(io.rong.imkit.fragment.ConversationFragment.this.mCustomServiceConfig);
                }

                if (message.getContent() != null && message.getContent().getUserInfo() != null) {
                  uiMessage.setUserInfo(message.getContent().getUserInfo());
                }

                remoteListx.add(uiMessage);
              }
            }

            List<UIMessage> remoteList = io.rong.imkit.fragment.ConversationFragment.this.filterMessage(remoteListx);
            if (remoteList != null && remoteList.size() > 0) {
              var4 = remoteList.iterator();

              while(var4.hasNext()) {
                UIMessage uiMessagex = (UIMessage)var4.next();
                uiMessagex.setSentStatus(SentStatus.READ);
                io.rong.imkit.fragment.ConversationFragment.this.mListAdapter.add(uiMessagex, 0);
              }

              io.rong.imkit.fragment.ConversationFragment.this.mListAdapter.notifyDataSetChanged();
              io.rong.imkit.fragment.ConversationFragment.this.mList.setSelection(messages.size() + 1);
              io.rong.imkit.fragment.ConversationFragment.this.sendReadReceiptResponseIfNeeded(messages);
              io.rong.imkit.fragment.ConversationFragment.this.mList.onRefreshComplete(messages.size(), reqCount, false);
              if (lastMessage != null) {
                RongContext.getInstance().getEventBus().post(lastMessage);
              }
            } else {
              io.rong.imkit.fragment.ConversationFragment.this.mList.onRefreshComplete(0, reqCount, false);
            }
          } else {
            io.rong.imkit.fragment.ConversationFragment.this.mList.onRefreshComplete(0, reqCount, false);
          }

        }

        public void onError() {
          io.rong.imkit.fragment.ConversationFragment.this.mList.onRefreshComplete(0, reqCount, false);
        }
      });
    }
  }

  private List<UIMessage> filterMessage(List<UIMessage> srcList) {
    Object destList;
    if (this.mListAdapter.getCount() > 0) {
      destList = new ArrayList();

      for(int i = 0; i < this.mListAdapter.getCount(); ++i) {
        Iterator var4 = srcList.iterator();

        while(var4.hasNext()) {
          UIMessage msg = (UIMessage)var4.next();
          if (!((List)destList).contains(msg) && msg.getMessageId() != ((UIMessage)this.mListAdapter.getItem(i)).getMessageId()) {
            ((List)destList).add(msg);
          }
        }
      }
    } else {
      destList = srcList;
    }

    return (List)destList;
  }

  private void getLastMentionedMessageId(ConversationType conversationType, String targetId) {
    RongIMClient.getInstance().getUnreadMentionedMessages(conversationType, targetId, new ResultCallback<List<Message>>() {
      public void onSuccess(List<Message> messages) {
        if (messages != null && messages.size() > 0) {
          io.rong.imkit.fragment.ConversationFragment.this.mLastMentionMsgId = ((Message)messages.get(0)).getMessageId();
          int index = io.rong.imkit.fragment.ConversationFragment.this.mListAdapter.findPosition((long) io.rong.imkit.fragment.ConversationFragment.this.mLastMentionMsgId);
          RLog.i("ConversationFragment", "getLastMentionedMessageId " + io.rong.imkit.fragment.ConversationFragment.this.mLastMentionMsgId + " " + index);
          if (io.rong.imkit.fragment.ConversationFragment.this.mLastMentionMsgId > 0 && index >= 0) {
            io.rong.imkit.fragment.ConversationFragment.this.mList.setSelection(index);
            io.rong.imkit.fragment.ConversationFragment.this.mLastMentionMsgId = 0;
          }
        }

        RongIM.getInstance().clearMessagesUnreadStatus(io.rong.imkit.fragment.ConversationFragment.this.mConversationType, io.rong.imkit.fragment.ConversationFragment.this.mTargetId, (ResultCallback)null);
      }

      public void onError(ErrorCode e) {
        RongIM.getInstance().clearMessagesUnreadStatus(io.rong.imkit.fragment.ConversationFragment.this.mConversationType, io.rong.imkit.fragment.ConversationFragment.this.mTargetId, (ResultCallback)null);
      }
    });
  }

  private void sendReadReceiptResponseIfNeeded(List<Message> messages) {
    if (this.mReadRec && (this.mConversationType.equals(ConversationType.GROUP) || this.mConversationType.equals(ConversationType.DISCUSSION)) && RongContext.getInstance().isReadReceiptConversationType(this.mConversationType)) {
      List<Message> responseMessageList = new ArrayList();
      Iterator var3 = messages.iterator();

      while(var3.hasNext()) {
        Message message = (Message)var3.next();
        ReadReceiptInfo readReceiptInfo = message.getReadReceiptInfo();
        if (readReceiptInfo != null && readReceiptInfo.isReadReceiptMessage() && !readReceiptInfo.hasRespond()) {
          responseMessageList.add(message);
        }
      }

      if (responseMessageList.size() > 0) {
        RongIMClient.getInstance().sendReadReceiptResponse(this.mConversationType, this.mTargetId, responseMessageList, (OperationCallback)null);
      }
    }

  }

  public void onExtensionCollapsed() {
  }

  public void onExtensionExpanded(int h) {
    if (this.indexMessageTime > 0L) {
      this.mListAdapter.clear();
      if (this.firstUnreadMessage == null) {
        this.indexMessageTime = 0L;
      }

      this.conversationUnreadCount = this.mUnReadCount;
      this.getHistoryMessage(this.mConversationType, this.mTargetId, 10, Mode.START, 1, -1);
    } else {
      this.mList.setSelection(this.mListAdapter.getCount());
      if (this.mNewMessageCount > 0) {
        this.mNewMessageCount = 0;
        this.mNewMessageBtn.setVisibility(View.GONE);
        this.mNewMessageTextView.setVisibility(View.GONE);
      }
    }

  }

  public void onStartCustomService(String targetId) {
    this.csEnterTime = System.currentTimeMillis();
    this.mRongExtension.setExtensionBarMode(CustomServiceMode.CUSTOM_SERVICE_MODE_NO_SERVICE);
    RongIMClient.getInstance().startCustomService(targetId, this.customServiceListener, this.mCustomUserInfo);
  }

  public void onStopCustomService(String targetId) {
    RongIMClient.getInstance().stopCustomService(targetId);
  }

  public final void onEvaluateSubmit() {
    if (this.mEvaluateDialg != null) {
      this.mEvaluateDialg.destroy();
      this.mEvaluateDialg = null;
    }

    if (this.mCustomServiceConfig != null && this.mCustomServiceConfig.quitSuspendType.equals(CSQuitSuspendType.NONE)) {
      this.getActivity().finish();
    }

  }

  public final void onEvaluateCanceled() {
    if (this.mEvaluateDialg != null) {
      this.mEvaluateDialg.destroy();
      this.mEvaluateDialg = null;
    }

    if (this.mCustomServiceConfig != null && this.mCustomServiceConfig.quitSuspendType.equals(CSQuitSuspendType.NONE)) {
      this.getActivity().finish();
    }

  }

  private void startTimer(int event, int interval) {
    this.getHandler().removeMessages(event);
    this.getHandler().sendEmptyMessageDelayed(event, (long)interval);
  }

  private void stopTimer(int event) {
    this.getHandler().removeMessages(event);
  }

  public ConversationType getConversationType() {
    return this.mConversationType;
  }

  public String getTargetId() {
    return this.mTargetId;
  }

  protected static enum LoadMessageDirection {
    DOWN,
    UP;

    private LoadMessageDirection() {
    }
  }
}
