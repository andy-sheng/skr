//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.widget.adapter;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.net.Uri;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.core.avatar.AvatarUtils;
import com.facebook.drawee.view.SimpleDraweeView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.rong.common.RLog;
import io.rong.imkit.R;
import io.rong.imkit.RongContext;
import io.rong.imkit.mention.RongMentionManager;
import io.rong.imkit.model.ConversationKey;
import io.rong.imkit.model.GroupUserInfo;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.userInfoCache.RongUserInfoManager;
import io.rong.imkit.utilities.RongUtils;
import io.rong.imkit.utils.RongDateUtils;
import io.rong.imkit.widget.DebouncedOnClickListener;
import io.rong.imkit.widget.ProviderContainerView;
import io.rong.imkit.widget.provider.IContainerItemProvider;
import io.rong.imkit.widget.provider.IContainerItemProvider.MessageProvider;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.RongIMClient.ErrorCode;
import io.rong.imlib.RongIMClient.OperationCallback;
import io.rong.imlib.location.message.RealTimeLocationJoinMessage;
import io.rong.imlib.location.message.RealTimeLocationStartMessage;
import io.rong.imlib.model.Conversation.ConversationType;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.Message.MessageDirection;
import io.rong.imlib.model.Message.SentStatus;
import io.rong.imlib.model.MessageContent;
import io.rong.imlib.model.PublicServiceProfile;
import io.rong.imlib.model.ReadReceiptInfo;
import io.rong.imlib.model.UnknownMessage;
import io.rong.imlib.model.UserInfo;
import io.rong.message.GroupNotificationMessage;
import io.rong.message.HistoryDividerMessage;
import io.rong.message.InformationNotificationMessage;
import io.rong.message.RecallNotificationMessage;
import io.rong.message.TextMessage;

public class MessageListAdapter extends BaseAdapter<UIMessage> {
    private static final String TAG = "MessageListAdapter";
    private static long readReceiptRequestInterval = 120L;
    private LayoutInflater mInflater;
    private Context mContext;
    private io.rong.imkit.widget.adapter.MessageListAdapter.OnItemHandlerListener mOnItemHandlerListener;
    boolean evaForRobot = false;
    boolean robotMode = true;
    protected boolean timeGone = false;
    private boolean isShowCheckbox;
    private int maxMessageSelectedCount = -1;
    private io.rong.imkit.widget.adapter.MessageListAdapter.OnMessageCheckedChanged messageCheckedChanged;
    private io.rong.imkit.widget.adapter.MessageListAdapter.OnSelectedCountDidExceed selectedCountDidExceed;

    public MessageListAdapter(Context context) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(this.mContext);

        try {
            if (RongContext.getInstance() != null) {
                readReceiptRequestInterval = (long) RongContext.getInstance().getResources().getInteger(R.integer.rc_read_receipt_request_interval);
            } else {
                RLog.e("MessageListAdapter", "SDK isn't init, use default readReceiptRequestInterval. Please refer to http://support.rongcloud.cn/kb/Mjc2 about how to init.");
            }
        } catch (NotFoundException var3) {
            RLog.e("MessageListAdapter", "rc_read_receipt_request_interval not configure in rc_config.xml");
            var3.printStackTrace();
        }

    }

    public void setMaxMessageSelectedCount(int maxMessageSelectedCount) {
        this.maxMessageSelectedCount = maxMessageSelectedCount;
    }

    public void setSelectedCountDidExceed(io.rong.imkit.widget.adapter.MessageListAdapter.OnSelectedCountDidExceed selectedCountDidExceed) {
        this.selectedCountDidExceed = selectedCountDidExceed;
    }

    public void setOnItemHandlerListener(io.rong.imkit.widget.adapter.MessageListAdapter.OnItemHandlerListener onItemHandlerListener) {
        this.mOnItemHandlerListener = onItemHandlerListener;
    }

    protected io.rong.imkit.widget.adapter.MessageListAdapter.OnItemHandlerListener getItemHandlerListener() {
        return this.mOnItemHandlerListener;
    }

    public boolean isShowCheckbox() {
        return this.isShowCheckbox;
    }

    public void setShowCheckbox(boolean showCheckbox) {
        this.isShowCheckbox = showCheckbox;
    }

    public void setMessageCheckedChanged(io.rong.imkit.widget.adapter.MessageListAdapter.OnMessageCheckedChanged messageCheckedChanged) {
        this.messageCheckedChanged = messageCheckedChanged;
    }

    public long getItemId(int position) {
        UIMessage message = (UIMessage) this.getItem(position);
        return message == null ? -1L : (long) message.getMessageId();
    }

    public int getPositionBySendTime(long sentTime) {
        for (int i = 0; i < this.getCount(); ++i) {
            UIMessage message = (UIMessage) this.getItem(i);
            if (message.getSentTime() > sentTime) {
                return i;
            }
        }

        return this.getCount();
    }

    protected View newView(Context context, int position, ViewGroup group) {
        View result = this.mInflater.inflate(R.layout.rc_item_message, (ViewGroup) null);
        io.rong.imkit.widget.adapter.MessageListAdapter.ViewHolder holder = new io.rong.imkit.widget.adapter.MessageListAdapter.ViewHolder();
        holder.leftIconView = (SimpleDraweeView) this.findViewById(result, R.id.rc_left);
        holder.rightIconView = (SimpleDraweeView) this.findViewById(result, R.id.rc_right);
        holder.nameView = (TextView) this.findViewById(result, R.id.rc_title);
        holder.contentView = (ProviderContainerView) this.findViewById(result, R.id.rc_content);
        holder.layout = (ViewGroup) this.findViewById(result, R.id.rc_layout);
        holder.progressBar = (ProgressBar) this.findViewById(result, R.id.rc_progress);
        holder.warning = (ImageView) this.findViewById(result, R.id.rc_warning);
        holder.readReceipt = (TextView) this.findViewById(result, R.id.rc_read_receipt);
        holder.readReceiptRequest = (TextView) this.findViewById(result, R.id.rc_read_receipt_request);
        holder.readReceiptStatus = (TextView) this.findViewById(result, R.id.rc_read_receipt_status);
        holder.message_check = (CheckBox) this.findViewById(result, R.id.message_check);
        holder.checkboxLayout = (LinearLayout) this.findViewById(result, R.id.ll_message_check);
        holder.time = (TextView) this.findViewById(result, R.id.rc_time);
        holder.sentStatus = (TextView) this.findViewById(result, R.id.rc_sent_status);
        holder.layoutItem = (RelativeLayout) this.findViewById(result, R.id.rc_layout_item_message);
        if (holder.time.getVisibility() == View.GONE) {
            this.timeGone = true;
        } else {
            this.timeGone = false;
        }

        result.setTag(holder);
        return result;
    }

    protected boolean getNeedEvaluate(UIMessage data) {
        String extra = "";
        String robotEva = "";
        String sid = "";
        if (data != null && data.getConversationType() != null && data.getConversationType().equals(ConversationType.CUSTOMER_SERVICE)) {
            if (data.getContent() instanceof TextMessage) {
                extra = ((TextMessage) data.getContent()).getExtra();
                if (TextUtils.isEmpty(extra)) {
                    return false;
                }

                try {
                    JSONObject jsonObj = new JSONObject(extra);
                    robotEva = jsonObj.optString("robotEva");
                    sid = jsonObj.optString("sid");
                } catch (JSONException var6) {
                    ;
                }
            }

            if (data.getMessageDirection() == MessageDirection.RECEIVE && data.getContent() instanceof TextMessage && this.evaForRobot && this.robotMode && !TextUtils.isEmpty(robotEva) && !TextUtils.isEmpty(sid) && !data.getIsHistoryMessage()) {
                return true;
            }
        }

        return false;
    }

    public List<Message> getCheckedMessage() {
        List<Message> checkedMessage = new ArrayList();

        for (int i = 0; i < this.getCount(); ++i) {
            UIMessage uiMessage = (UIMessage) this.getItem(i);
            if (uiMessage.isChecked()) {
                checkedMessage.add(uiMessage.getMessage());
            }
        }

        return checkedMessage;
    }

    private void bindViewClickEvent(View convertView, View contentView, final int position, final UIMessage data) {
        final io.rong.imkit.widget.adapter.MessageListAdapter.ViewHolder holder = (io.rong.imkit.widget.adapter.MessageListAdapter.ViewHolder) convertView.getTag();
        OnClickListener viewClickListener = new OnClickListener() {
            public void onClick(View v) {
                if (io.rong.imkit.widget.adapter.MessageListAdapter.this.isShowCheckbox()) {
                    boolean checked = !data.isChecked();
                    if (io.rong.imkit.widget.adapter.MessageListAdapter.this.maxMessageSelectedCount != -1 && io.rong.imkit.widget.adapter.MessageListAdapter.this.getCheckedMessage().size() >= io.rong.imkit.widget.adapter.MessageListAdapter.this.maxMessageSelectedCount && checked) {
                        if (io.rong.imkit.widget.adapter.MessageListAdapter.this.selectedCountDidExceed != null) {
                            io.rong.imkit.widget.adapter.MessageListAdapter.this.selectedCountDidExceed.onSelectedCountDidExceed();
                        }

                        return;
                    }

                    data.setChecked(checked);
                    holder.message_check.setChecked(checked);
                    if (io.rong.imkit.widget.adapter.MessageListAdapter.this.messageCheckedChanged != null) {
                        io.rong.imkit.widget.adapter.MessageListAdapter.this.messageCheckedChanged.onCheckedEnable(io.rong.imkit.widget.adapter.MessageListAdapter.this.getCheckedMessage().size() > 0);
                    }
                }

            }
        };
        OnTouchListener viewTouchListener = new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (io.rong.imkit.widget.adapter.MessageListAdapter.this.isShowCheckbox() && event.getAction() == 1) {
                    boolean checked = !data.isChecked();
                    if (io.rong.imkit.widget.adapter.MessageListAdapter.this.maxMessageSelectedCount != -1 && io.rong.imkit.widget.adapter.MessageListAdapter.this.getCheckedMessage().size() >= io.rong.imkit.widget.adapter.MessageListAdapter.this.maxMessageSelectedCount && checked) {
                        if (io.rong.imkit.widget.adapter.MessageListAdapter.this.selectedCountDidExceed != null) {
                            io.rong.imkit.widget.adapter.MessageListAdapter.this.selectedCountDidExceed.onSelectedCountDidExceed();
                        }

                        return true;
                    } else {
                        data.setChecked(checked);
                        holder.message_check.setChecked(checked);
                        if (io.rong.imkit.widget.adapter.MessageListAdapter.this.messageCheckedChanged != null) {
                            io.rong.imkit.widget.adapter.MessageListAdapter.this.messageCheckedChanged.onCheckedEnable(io.rong.imkit.widget.adapter.MessageListAdapter.this.getCheckedMessage().size() > 0);
                        }

                        return true;
                    }
                } else {
                    return false;
                }
            }
        };
        OnClickListener contentClickListener = new OnClickListener() {
            public void onClick(View v) {
                if (RongContext.getInstance().getConversationBehaviorListener() != null) {
                    if (RongContext.getInstance().getConversationBehaviorListener().onMessageClick(io.rong.imkit.widget.adapter.MessageListAdapter.this.mContext, v, data.getMessage())) {
                        return;
                    }
                } else if (RongContext.getInstance().getConversationClickListener() != null && RongContext.getInstance().getConversationClickListener().onMessageClick(io.rong.imkit.widget.adapter.MessageListAdapter.this.mContext, v, data.getMessage())) {
                    return;
                }

                Object provider;
                if (io.rong.imkit.widget.adapter.MessageListAdapter.this.getNeedEvaluate(data)) {
                    provider = RongContext.getInstance().getEvaluateProvider();
                } else {
                    provider = RongContext.getInstance().getMessageTemplate(data.getContent().getClass());
                }

                if (provider != null) {
                    ((MessageProvider) provider).onItemClick(v, position, data.getContent(), data);
                }

            }
        };
        OnLongClickListener contentLongClickListener = new OnLongClickListener() {
            public boolean onLongClick(View v) {
                if (io.rong.imkit.widget.adapter.MessageListAdapter.this.isShowCheckbox()) {
                    return true;
                } else {
                    if (RongContext.getInstance().getConversationBehaviorListener() != null) {
                        if (RongContext.getInstance().getConversationBehaviorListener().onMessageLongClick(io.rong.imkit.widget.adapter.MessageListAdapter.this.mContext, v, data.getMessage())) {
                            return true;
                        }
                    } else if (RongContext.getInstance().getConversationClickListener() != null && RongContext.getInstance().getConversationClickListener().onMessageLongClick(io.rong.imkit.widget.adapter.MessageListAdapter.this.mContext, v, data.getMessage())) {
                        return true;
                    }

                    Object provider;
                    if (io.rong.imkit.widget.adapter.MessageListAdapter.this.getNeedEvaluate(data)) {
                        provider = RongContext.getInstance().getEvaluateProvider();
                    } else {
                        provider = RongContext.getInstance().getMessageTemplate(data.getContent().getClass());
                    }

                    if (provider != null) {
                        ((MessageProvider) provider).onItemLongClick(v, position, data.getContent(), data);
                    }

                    return true;
                }
            }
        };
        OnClickListener iconClickListener = new OnClickListener() {
            public void onClick(View v) {
                UserInfo userInfo = data.getUserInfo();
                if (!TextUtils.isEmpty(data.getSenderUserId())) {
                    if (userInfo == null) {
                        userInfo = RongUserInfoManager.getInstance().getUserInfo(data.getSenderUserId());
                    }

                    userInfo = userInfo == null ? new UserInfo(data.getSenderUserId(), (String) null, (Uri) null) : userInfo;
                }

                if (RongContext.getInstance().getConversationBehaviorListener() != null) {
                    RongContext.getInstance().getConversationBehaviorListener().onUserPortraitClick(io.rong.imkit.widget.adapter.MessageListAdapter.this.mContext, data.getConversationType(), userInfo);
                } else if (RongContext.getInstance().getConversationClickListener() != null) {
                    RongContext.getInstance().getConversationClickListener().onUserPortraitClick(io.rong.imkit.widget.adapter.MessageListAdapter.this.mContext, data.getConversationType(), userInfo, data.getTargetId());
                }

            }
        };
        OnLongClickListener iconLongClickListener = new OnLongClickListener() {
            public boolean onLongClick(View v) {
                UserInfo userInfo = data.getUserInfo();
                if (!TextUtils.isEmpty(data.getSenderUserId())) {
                    if (userInfo == null) {
                        userInfo = RongUserInfoManager.getInstance().getUserInfo(data.getSenderUserId());
                    }

                    userInfo = userInfo == null ? new UserInfo(data.getSenderUserId(), (String) null, (Uri) null) : userInfo;
                }

                if (data.getConversationType().equals(ConversationType.GROUP)) {
                    GroupUserInfo groupUserInfo = RongUserInfoManager.getInstance().getGroupUserInfo(data.getTargetId(), data.getSenderUserId());
                    if (groupUserInfo != null && !TextUtils.isEmpty(groupUserInfo.getNickname()) && userInfo != null) {
                        userInfo.setName(groupUserInfo.getNickname());
                    }
                }

                if (data.getMessageDirection().equals(MessageDirection.SEND)) {
                    if (RongContext.getInstance().getConversationBehaviorListener() != null) {
                        return RongContext.getInstance().getConversationBehaviorListener().onUserPortraitLongClick(io.rong.imkit.widget.adapter.MessageListAdapter.this.mContext, data.getConversationType(), userInfo);
                    }

                    if (RongContext.getInstance().getConversationClickListener() != null) {
                        return RongContext.getInstance().getConversationClickListener().onUserPortraitLongClick(io.rong.imkit.widget.adapter.MessageListAdapter.this.mContext, data.getConversationType(), userInfo, data.getTargetId());
                    }
                } else {
                    if (RongContext.getInstance().getConversationBehaviorListener() == null || RongContext.getInstance().getConversationClickListener() == null) {
                        if (!RongContext.getInstance().getResources().getBoolean(R.bool.rc_enable_mentioned_message) || !data.getConversationType().equals(ConversationType.GROUP) && !data.getConversationType().equals(ConversationType.DISCUSSION)) {
                            return false;
                        }

                        RongMentionManager.getInstance().mentionMember(data.getConversationType(), data.getTargetId(), data.getSenderUserId());
                        return true;
                    }

                    if ((RongContext.getInstance().getConversationBehaviorListener() != null && !RongContext.getInstance().getConversationBehaviorListener().onUserPortraitLongClick(io.rong.imkit.widget.adapter.MessageListAdapter.this.mContext, data.getConversationType(), userInfo) || RongContext.getInstance().getConversationClickListener() != null && !RongContext.getInstance().getConversationClickListener().onUserPortraitLongClick(io.rong.imkit.widget.adapter.MessageListAdapter.this.mContext, data.getConversationType(), userInfo, data.getTargetId())) && RongContext.getInstance().getResources().getBoolean(R.bool.rc_enable_mentioned_message) && (data.getConversationType().equals(ConversationType.GROUP) || data.getConversationType().equals(ConversationType.DISCUSSION))) {
                        RongMentionManager.getInstance().mentionMember(data.getConversationType(), data.getTargetId(), data.getSenderUserId());
                        return true;
                    }
                }

                return true;
            }
        };
        if (this.isShowCheckbox() && this.allowShowCheckButton(data.getMessage())) {
            convertView.setOnClickListener(viewClickListener);
            contentView.setOnTouchListener(viewTouchListener);
            holder.rightIconView.setOnClickListener(viewClickListener);
            holder.leftIconView.setOnClickListener(viewClickListener);
        } else {
            contentView.setOnClickListener(contentClickListener);
            contentView.setOnLongClickListener(contentLongClickListener);
            holder.rightIconView.setOnClickListener(iconClickListener);
            holder.leftIconView.setOnClickListener(iconClickListener);
            holder.rightIconView.setOnLongClickListener(iconLongClickListener);
            holder.leftIconView.setOnLongClickListener(iconLongClickListener);
        }

        holder.warning.setOnClickListener(new DebouncedOnClickListener() {
            public void onDebouncedClick(View view) {
                if (io.rong.imkit.widget.adapter.MessageListAdapter.this.getItemHandlerListener() != null) {
                    io.rong.imkit.widget.adapter.MessageListAdapter.this.getItemHandlerListener().onWarningViewClick(position, data.getMessage(), view);
                }

            }
        });
    }

    protected void bindView(View v, int position, final UIMessage data) {
        if (data != null) {
            final io.rong.imkit.widget.adapter.MessageListAdapter.ViewHolder holder = (io.rong.imkit.widget.adapter.MessageListAdapter.ViewHolder) v.getTag();
            if (holder == null) {
                RLog.e("MessageListAdapter", "view holder is null !");
            } else {
                Object provider;
                ProviderTag tag;
                if (this.getNeedEvaluate(data)) {
                    provider = RongContext.getInstance().getEvaluateProvider();
                    tag = RongContext.getInstance().getMessageProviderTag(data.getContent().getClass());
                } else {
                    if (RongContext.getInstance() == null || data.getContent() == null) {
                        RLog.e("MessageListAdapter", "Message is null !");
                        return;
                    }

                    provider = RongContext.getInstance().getMessageTemplate(data.getContent().getClass());
                    if (provider == null) {
                        provider = RongContext.getInstance().getMessageTemplate(UnknownMessage.class);
                        tag = RongContext.getInstance().getMessageProviderTag(UnknownMessage.class);
                    } else {
                        tag = RongContext.getInstance().getMessageProviderTag(data.getContent().getClass());
                    }

                    if (provider == null) {
                        RLog.e("MessageListAdapter", data.getObjectName() + " message provider not found !");
                        return;
                    }
                }

                View msgView;

                try {
                    msgView = holder.contentView.inflate((IContainerItemProvider) provider);
                } catch (Exception var14) {
                    RLog.e("MessageListAdapter", "contentView inflate error");
                    var14.printStackTrace();
                    provider = RongContext.getInstance().getMessageTemplate(UnknownMessage.class);
                    tag = RongContext.getInstance().getMessageProviderTag(UnknownMessage.class);
                    msgView = holder.contentView.inflate((IContainerItemProvider) provider);
                }

                ((IContainerItemProvider) provider).bindView(msgView, position, data);
                if (tag == null) {
                    RLog.e("MessageListAdapter", "Can not find ProviderTag for " + data.getObjectName());
                } else {
                    if (tag.hide()) {
                        holder.contentView.setVisibility(View.GONE);
                        holder.time.setVisibility(View.GONE);
                        holder.nameView.setVisibility(View.GONE);
                        holder.leftIconView.setVisibility(View.GONE);
                        holder.rightIconView.setVisibility(View.GONE);
                        holder.layoutItem.setVisibility(View.GONE);
                        holder.layoutItem.setPadding(0, 0, 0, 0);
                    } else {
                        holder.contentView.setVisibility(View.VISIBLE);
                        holder.layoutItem.setVisibility(View.VISIBLE);
                        holder.layoutItem.setPadding(RongUtils.dip2px(8.0F), RongUtils.dip2px(6.0F), RongUtils.dip2px(8.0F), RongUtils.dip2px(6.0F));
                    }

                    UserInfo userInfo;
                    GroupUserInfo groupUserInfo;
                    if (data.getMessageDirection() == MessageDirection.SEND) {
                        if (tag.showPortrait()) {
                            holder.rightIconView.setVisibility(View.VISIBLE);
                            holder.leftIconView.setVisibility(View.GONE);
                        } else {
                            holder.leftIconView.setVisibility(View.GONE);
                            holder.rightIconView.setVisibility(View.GONE);
                        }

                        if (!tag.centerInHorizontal()) {
                            this.setGravity(holder.layout, Gravity.RIGHT);
                            holder.contentView.containerViewRight();
                            holder.nameView.setGravity(Gravity.RIGHT);
                        } else {
                            this.setGravity(holder.layout, Gravity.CENTER);
                            holder.contentView.containerViewCenter();
                            holder.nameView.setGravity(Gravity.CENTER_HORIZONTAL);
                            holder.contentView.setBackgroundColor(0);
                        }

                        boolean readRec = false;

                        try {
                            readRec = this.mContext.getResources().getBoolean(R.bool.rc_read_receipt);
                        } catch (NotFoundException var13) {
                            RLog.e("MessageListAdapter", "rc_read_receipt not configure in rc_config.xml");
                            var13.printStackTrace();
                        }

                        if (data.getSentStatus() == SentStatus.SENDING) {
                            if (tag.showProgress()) {
                                holder.progressBar.setVisibility(View.VISIBLE);
                            } else {
                                holder.progressBar.setVisibility(View.GONE);
                            }

                            holder.warning.setVisibility(View.GONE);
                            holder.readReceipt.setVisibility(View.GONE);
                        } else if (data.getSentStatus() == SentStatus.FAILED) {
                            holder.progressBar.setVisibility(View.GONE);
                            holder.warning.setVisibility(View.VISIBLE);
                            holder.readReceipt.setVisibility(View.GONE);
                        } else if (data.getSentStatus() == SentStatus.SENT) {
                            holder.progressBar.setVisibility(View.GONE);
                            holder.warning.setVisibility(View.GONE);
                            holder.readReceipt.setVisibility(View.GONE);
                        } else if (readRec && data.getSentStatus() == SentStatus.READ) {
                            holder.progressBar.setVisibility(View.GONE);
                            holder.warning.setVisibility(View.GONE);
                            if (data.getConversationType().equals(ConversationType.PRIVATE) && tag.showReadState()) {
                                holder.readReceipt.setVisibility(View.VISIBLE);
                            } else {
                                holder.readReceipt.setVisibility(View.GONE);
                            }
                        } else {
                            holder.progressBar.setVisibility(View.GONE);
                            holder.warning.setVisibility(View.GONE);
                            holder.readReceipt.setVisibility(View.GONE);
                        }

                        holder.readReceiptRequest.setVisibility(View.GONE);
                        holder.readReceiptStatus.setVisibility(View.GONE);
                        if (readRec && RongContext.getInstance().isReadReceiptConversationType(data.getConversationType()) && (data.getConversationType().equals(ConversationType.GROUP) || data.getConversationType().equals(ConversationType.DISCUSSION))) {
                            if (this.allowReadReceiptRequest(data.getMessage()) && !TextUtils.isEmpty(data.getUId())) {
                                boolean isLastSentMessage = true;

                                for (int i = position + 1; i < this.getCount(); ++i) {
                                    if (((UIMessage) this.getItem(i)).getMessageDirection() == MessageDirection.SEND) {
                                        isLastSentMessage = false;
                                        break;
                                    }
                                }

                                long serverTime = System.currentTimeMillis() - RongIMClient.getInstance().getDeltaTime();
                                if (serverTime - data.getSentTime() < readReceiptRequestInterval * 1000L && isLastSentMessage && (data.getReadReceiptInfo() == null || !data.getReadReceiptInfo().isReadReceiptMessage())) {
                                    holder.readReceiptRequest.setVisibility(View.VISIBLE);
                                }
                            }

                            if (this.allowReadReceiptRequest(data.getMessage()) && data.getReadReceiptInfo() != null && data.getReadReceiptInfo().isReadReceiptMessage()) {
                                if (data.getReadReceiptInfo().getRespondUserIdList() != null) {
                                    holder.readReceiptStatus.setText(String.format(msgView.getResources().getString(R.string.rc_read_receipt_status), data.getReadReceiptInfo().getRespondUserIdList().size()));
                                } else {
                                    holder.readReceiptStatus.setText(String.format(msgView.getResources().getString(R.string.rc_read_receipt_status), View.VISIBLE));
                                }

                                holder.readReceiptStatus.setVisibility(View.VISIBLE);
                            }
                        }

                        holder.nameView.setVisibility(View.GONE);
                        holder.readReceiptRequest.setOnClickListener(new OnClickListener() {
                            public void onClick(View v) {
                                RongIMClient.getInstance().sendReadReceiptRequest(data.getMessage(), new OperationCallback() {
                                    public void onSuccess() {
                                        ReadReceiptInfo readReceiptInfo = data.getReadReceiptInfo();
                                        if (readReceiptInfo == null) {
                                            readReceiptInfo = new ReadReceiptInfo();
                                            data.setReadReceiptInfo(readReceiptInfo);
                                        }

                                        readReceiptInfo.setIsReadReceiptMessage(true);
                                        holder.readReceiptStatus.setText(String.format(v.getResources().getString(R.string.rc_read_receipt_status), View.VISIBLE));
                                        holder.readReceiptRequest.setVisibility(View.GONE);
                                        holder.readReceiptStatus.setVisibility(View.VISIBLE);
                                    }

                                    public void onError(ErrorCode errorCode) {
                                        RLog.e("MessageListAdapter", "sendReadReceiptRequest failed, errorCode = " + errorCode);
                                    }
                                });
                            }
                        });
                        holder.readReceiptStatus.setOnClickListener(new OnClickListener() {
                            public void onClick(View v) {
                                if (io.rong.imkit.widget.adapter.MessageListAdapter.this.mOnItemHandlerListener != null) {
                                    io.rong.imkit.widget.adapter.MessageListAdapter.this.mOnItemHandlerListener.onReadReceiptStateClick(data.getMessage());
                                }

                            }
                        });
                        if (!tag.showWarning()) {
                            holder.warning.setVisibility(View.GONE);
                        }
                    } else {
                        if (tag.showPortrait()) {
                            holder.rightIconView.setVisibility(View.GONE);
                            holder.leftIconView.setVisibility(View.VISIBLE);
                        } else {
                            holder.leftIconView.setVisibility(View.GONE);
                            holder.rightIconView.setVisibility(View.GONE);
                        }

                        if (!tag.centerInHorizontal()) {
                            this.setGravity(holder.layout, 3);
                            holder.contentView.containerViewLeft();
                            holder.nameView.setGravity(3);
                        } else {
                            this.setGravity(holder.layout, 17);
                            holder.contentView.containerViewCenter();
                            holder.nameView.setGravity(1);
                            holder.contentView.setBackgroundColor(0);
                        }

                        holder.progressBar.setVisibility(View.GONE);
                        holder.warning.setVisibility(View.GONE);
                        holder.readReceipt.setVisibility(View.GONE);
                        holder.readReceiptRequest.setVisibility(View.GONE);
                        holder.readReceiptStatus.setVisibility(View.GONE);
                        holder.nameView.setVisibility(View.VISIBLE);
                        if (data.getConversationType() != ConversationType.PRIVATE && tag.showSummaryWithName() && data.getConversationType() != ConversationType.PUBLIC_SERVICE && data.getConversationType() != ConversationType.APP_PUBLIC_SERVICE) {
                            userInfo = data.getUserInfo();
                            if (data.getConversationType().equals(ConversationType.CUSTOMER_SERVICE) && data.getMessageDirection().equals(MessageDirection.RECEIVE)) {
                                if (userInfo == null && data.getMessage() != null && data.getMessage().getContent() != null) {
                                    userInfo = data.getMessage().getContent().getUserInfo();
                                }

                                if (userInfo != null) {
                                    holder.nameView.setText(userInfo.getName());
                                } else {
                                    holder.nameView.setText(data.getSenderUserId());
                                }
                            } else if (data.getConversationType() == ConversationType.GROUP) {
                                groupUserInfo = RongUserInfoManager.getInstance().getGroupUserInfo(data.getTargetId(), data.getSenderUserId());
                                if (groupUserInfo != null) {
                                    holder.nameView.setText(groupUserInfo.getNickname());
                                } else {
                                    if (userInfo == null) {
                                        userInfo = RongUserInfoManager.getInstance().getUserInfo(data.getSenderUserId());
                                    }

                                    if (userInfo == null) {
                                        holder.nameView.setText(data.getSenderUserId());
                                    } else {
                                        holder.nameView.setText(userInfo.getName());
                                    }
                                }
                            } else {
                                if (userInfo == null) {
                                    userInfo = RongUserInfoManager.getInstance().getUserInfo(data.getSenderUserId());
                                }

                                if (userInfo == null) {
                                    holder.nameView.setText(data.getSenderUserId());
                                } else {
                                    holder.nameView.setText(userInfo.getName());
                                }
                            }
                        } else {
                            holder.nameView.setVisibility(View.GONE);
                        }
                    }

                    ConversationKey mKey;
                    Uri portrait;
                    PublicServiceProfile publicServiceProfile;
                    if (holder.rightIconView.getVisibility() == View.VISIBLE) {
                        userInfo = data.getUserInfo();
                        portrait = null;
                        if (data.getConversationType().equals(ConversationType.CUSTOMER_SERVICE) && data.getUserInfo() != null && data.getMessageDirection().equals(MessageDirection.RECEIVE)) {
                            if (userInfo != null) {
                                portrait = userInfo.getPortraitUri();
                            }

                            AvatarUtils.loadAvatarByUrl(holder.rightIconView, AvatarUtils.newParamsBuilder(portrait.toString())
                                    .build());
                        } else if ((data.getConversationType().equals(ConversationType.PUBLIC_SERVICE) || data.getConversationType().equals(ConversationType.APP_PUBLIC_SERVICE)) && data.getMessageDirection().equals(MessageDirection.RECEIVE)) {
                            if (userInfo != null) {
                                portrait = userInfo.getPortraitUri();
                                AvatarUtils.loadAvatarByUrl(holder.rightIconView, AvatarUtils.newParamsBuilder(portrait.toString())
                                        .build());
                            } else {
                                mKey = ConversationKey.obtain(data.getTargetId(), data.getConversationType());
                                publicServiceProfile = RongContext.getInstance().getPublicServiceInfoFromCache(mKey.getKey());
                                portrait = publicServiceProfile.getPortraitUri();
                                AvatarUtils.loadAvatarByUrl(holder.rightIconView, AvatarUtils.newParamsBuilder(portrait.toString())
                                        .build());
                            }
                        } else if (!TextUtils.isEmpty(data.getSenderUserId())) {
                            if (userInfo == null) {
                                userInfo = RongUserInfoManager.getInstance().getUserInfo(data.getSenderUserId());
                            }

                            if (userInfo != null && userInfo.getPortraitUri() != null) {
                                AvatarUtils.loadAvatarByUrl(holder.rightIconView, AvatarUtils.newParamsBuilder(userInfo.getPortraitUri().toString())
                                        .build());
                            } else {
                                AvatarUtils.loadAvatarByUrl(holder.rightIconView, AvatarUtils.newParamsBuilder("").build());
                            }
                        }
                    } else if (holder.leftIconView.getVisibility() == View.VISIBLE) {
                        userInfo = data.getUserInfo();
                        groupUserInfo = null;
                        if (data.getConversationType().equals(ConversationType.CUSTOMER_SERVICE) && data.getMessageDirection().equals(MessageDirection.RECEIVE)) {
                            if (userInfo == null && data.getMessage() != null && data.getMessage().getContent() != null) {
                                userInfo = data.getMessage().getContent().getUserInfo();
                            }

                            if (userInfo != null) {
                                portrait = userInfo.getPortraitUri();
                                AvatarUtils.loadAvatarByUrl(holder.leftIconView, AvatarUtils.newParamsBuilder(portrait.toString())
                                        .setLoadingAvatarResId(R.drawable.rc_cs_default_portrait).build());
                            }
                        } else if ((data.getConversationType().equals(ConversationType.PUBLIC_SERVICE) || data.getConversationType().equals(ConversationType.APP_PUBLIC_SERVICE)) && data.getMessageDirection().equals(MessageDirection.RECEIVE)) {
                            if (userInfo != null) {
                                portrait = userInfo.getPortraitUri();
                                AvatarUtils.loadAvatarByUrl(holder.leftIconView, AvatarUtils.newParamsBuilder(portrait.toString()).build());
                            } else {
                                mKey = ConversationKey.obtain(data.getTargetId(), data.getConversationType());
                                publicServiceProfile = RongContext.getInstance().getPublicServiceInfoFromCache(mKey.getKey());
                                if (publicServiceProfile != null && publicServiceProfile.getPortraitUri() != null) {
                                    AvatarUtils.loadAvatarByUrl(holder.leftIconView, AvatarUtils.newParamsBuilder(publicServiceProfile.getPortraitUri().toString()).build());
                                } else {
                                    AvatarUtils.loadAvatarByUrl(holder.leftIconView, AvatarUtils.newParamsBuilder("").build());
                                }
                            }
                        } else if (!TextUtils.isEmpty(data.getSenderUserId())) {
                            if (userInfo == null) {
                                userInfo = RongUserInfoManager.getInstance().getUserInfo(data.getSenderUserId());
                            }

                            if (userInfo != null && userInfo.getPortraitUri() != null) {
                                AvatarUtils.loadAvatarByUrl(holder.leftIconView, AvatarUtils.newParamsBuilder(userInfo.getPortraitUri().toString())
                                        .build());
                            } else {
                                AvatarUtils.loadAvatarByUrl(holder.leftIconView, AvatarUtils.newParamsBuilder("").build());
                            }
                        }
                    }

                    this.bindViewClickEvent(v, msgView, position, data);
                    if (tag.hide()) {
                        holder.time.setVisibility(View.GONE);
                    } else {
                        if (!this.timeGone) {
                            String time = RongDateUtils.getConversationFormatDate(data.getSentTime(), msgView.getContext());
                            holder.time.setText(time);
                            if (position == 0) {
                                if (data.getMessage() != null && data.getMessage().getContent() != null && data.getMessage().getContent() instanceof HistoryDividerMessage) {
                                    holder.time.setVisibility(View.GONE);
                                } else {
                                    holder.time.setVisibility(View.VISIBLE);
                                }
                            } else {
                                UIMessage pre = (UIMessage) this.getItem(position - 1);
                                if (RongDateUtils.isShowChatTime(data.getSentTime(), pre.getSentTime(), 180)) {
                                    holder.time.setVisibility(View.VISIBLE);
                                } else {
                                    holder.time.setVisibility(View.GONE);
                                }
                            }
                        }

                        if (this.isShowCheckbox() && this.allowShowCheckButton(data.getMessage())) {
                            holder.checkboxLayout.setVisibility(View.VISIBLE);
                            holder.message_check.setFocusable(false);
                            holder.message_check.setClickable(false);
                            holder.message_check.setChecked(data.isChecked());
                        } else {
                            holder.checkboxLayout.setVisibility(View.GONE);
                            data.setChecked(false);
                        }

                        if (this.messageCheckedChanged != null) {
                            this.messageCheckedChanged.onCheckedEnable(this.getCheckedMessage().size() > 0);
                        }

                    }
                }
            }
        }
    }

    protected void setGravity(View view, int gravity) {
        LayoutParams params = (LayoutParams) view.getLayoutParams();
        params.gravity = gravity;
    }

    public void setEvaluateForRobot(boolean needEvaluate) {
        this.evaForRobot = needEvaluate;
    }

    public void setRobotMode(boolean robotMode) {
        this.robotMode = robotMode;
    }

    public boolean allowReadReceiptRequest(Message message) {
        return message != null && message.getContent() != null && message.getContent() instanceof TextMessage;
    }

    protected boolean allowShowCheckButton(Message message) {
        if (message != null) {
            MessageContent messageContent = message.getContent();
            if (messageContent != null && (messageContent instanceof InformationNotificationMessage || messageContent instanceof GroupNotificationMessage || messageContent instanceof RecallNotificationMessage || messageContent instanceof RealTimeLocationStartMessage || messageContent instanceof RealTimeLocationJoinMessage)) {
                return false;
            }
        }

        return true;
    }

    public interface OnItemHandlerListener {
        boolean onWarningViewClick(int var1, Message var2, View var3);

        void onReadReceiptStateClick(Message var1);
    }

    public interface OnMessageCheckedChanged {
        void onCheckedEnable(boolean var1);
    }

    public interface OnSelectedCountDidExceed {
        void onSelectedCountDidExceed();
    }

    protected class ViewHolder {
        public SimpleDraweeView leftIconView;
        public SimpleDraweeView rightIconView;
        public TextView nameView;
        public ProviderContainerView contentView;
        public ProgressBar progressBar;
        public ImageView warning;
        public TextView readReceipt;
        public TextView readReceiptRequest;
        public TextView readReceiptStatus;
        public ViewGroup layout;
        public TextView time;
        public TextView sentStatus;
        public RelativeLayout layoutItem;
        public CheckBox message_check;
        public LinearLayout checkboxLayout;

        protected ViewHolder() {
        }
    }
}
