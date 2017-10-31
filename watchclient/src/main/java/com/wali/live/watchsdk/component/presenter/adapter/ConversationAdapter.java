package com.wali.live.watchsdk.component.presenter.adapter;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.BaseImage;
import com.base.image.fresco.image.ResImage;
import com.base.utils.date.DateTimeUtils;
import com.base.utils.display.DisplayUtils;
import com.base.utils.span.SpanUtils;
import com.base.view.MLTextView;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.mi.live.data.user.User;
import com.wali.live.common.smiley.SmileyParser;
import com.wali.live.dao.Conversation;
import com.wali.live.dao.SixinMessage;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.utils.ItemDataFormatUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.adapter.ClickItemAdapter;
import com.wali.live.watchsdk.sixin.pojo.SixinTarget;

import org.json.JSONObject;

import static com.wali.live.dao.Conversation.EXT_IS_FOLLOW_MSG;
import static com.wali.live.dao.Conversation.INTERACT_CONVERSATION_TARGET;
import static com.wali.live.watchsdk.component.presenter.panel.MessagePresenter.TARGET_888;
import static com.wali.live.watchsdk.component.presenter.panel.MessagePresenter.TARGET_999;
import static com.wali.live.watchsdk.component.presenter.panel.MessagePresenter.TARGET_OFFICIAL;

/**
 * Created by yangli on 2017/10/27.
 */
public class ConversationAdapter extends ClickItemAdapter<ConversationAdapter.ConversationItem,
        ClickItemAdapter.BaseHolder, ConversationAdapter.IConversationClickListener> {

    private static final int MAX_SHOW_UNREAD_SIZE = 99; // 当未读数超过99显示...

    @Override
    public BaseHolder newViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.conversation_list_item, parent, false);
        return new ConversationHolder(view);
    }

    protected class ConversationHolder extends ClickItemAdapter.BaseHolder<ConversationItem, Object>
            implements View.OnClickListener {

        private ConversationItem dataItem;

        private SimpleDraweeView avatarIv;
        private ImageView certificationType; // 头像右下角的验证角标
        private MLTextView fromTv;
        private MLTextView msgNumTView;
        private ImageView unsendIv;
        private TextView subject;
        private MLTextView date;
        private CheckBox checkbox;
        private ImageView alertIv;

        public ConversationHolder(View view) {
            super(view);
            avatarIv = $(R.id.avatar);
            certificationType = $(R.id.user_certification_type);
            fromTv = $(R.id.from);
            msgNumTView = $(R.id.new_msg_num);
            unsendIv = $(R.id.conv_unsend_iv);
            subject = $(R.id.subject);
            date = $(R.id.date);
            checkbox = $(R.id.conversation_checkbox);
            alertIv = $(R.id.new_msg_alert);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null && dataItem != null) {
                mListener.onItemClick(dataItem);
            }
        }

        @Override
        public void bindView(final ConversationItem item, Object listener) {
            dataItem = item;
            final Context context = GlobalData.app();
            // 绑定头像
            if (item.localAvatarResId > 0) {
                //这些Conversation需要绑定本地的avatar
                BaseImage baseImage = new ResImage(item.localAvatarResId);
                baseImage.setIsCircle(false);
                baseImage.setLoadingDrawable(context.getResources().getDrawable(R.drawable.avatar_default_b));
                baseImage.setLoadingScaleType(ScalingUtils.ScaleType.CENTER_CROP);
                FrescoWorker.loadImage(avatarIv, baseImage);
            } else {
                AvatarUtils.loadAvatarByUidTsCorner(avatarIv, item.user.getUid(), 0, 14, 0, 0);
            }
            // 绑定头像右下角图标
            if (item.targetType == SixinMessage.TARGET_TYPE_GROUP) {
                certificationType.setImageDrawable(context.getResources().getDrawable(R.drawable.group_icon));
            } else {
                certificationType.setImageDrawable(ItemDataFormatUtils.getCertificationImgSource(item.certificationType));
            }

            // 绑定消息内容
            if (item.msgType == SixinMessage.S_MSG_TYPE_DRAFT) {
                Spannable atSpan = SpanUtils.addColorSpan(context.getResources().getString(R.string.draft), R.color.color_cd1f1f);
                SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(atSpan);
                spannableStringBuilder.append(" ").append(item.spannableSubject);
                subject.setText(spannableStringBuilder);
            } else if (item.hasSomeOneAtMe) {
                Spannable atSpan = SpanUtils.addColorSpan(context.getResources().getString(R.string.someone_at_you), R.color.color_cd1f1f);
                SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(atSpan);
                spannableStringBuilder.append(" ").append(item.spannableSubject);
                subject.setText(spannableStringBuilder);
            } else {
                subject.setText(item.spannableSubject);
            }

            // 绑定接收时间
            if (item.receivedTime == 0) {
                date.setVisibility(View.GONE);
            } else {
                date.setVisibility(View.VISIBLE);
                date.setText(DateTimeUtils.formatTimeStringForConversation(context, item.receivedTime));
            }

            // 绑定消息名称
            bindName(item);

            // 绑定未读数
            bindUnreadStatus(item);
        }

        private void bindName(ConversationItem item) {
            if (item.uid == TARGET_999) {
                fromTv.setText(R.string.user_name_999);
            } else if (item.uid == TARGET_888) {
                fromTv.setText(R.string.user_name_888);
            } else if (item.uid == INTERACT_CONVERSATION_TARGET) {
                fromTv.setText(R.string.message_interact_notify);
            } else if (item.uid == TARGET_OFFICIAL) {
                fromTv.setText(R.string.user_name_100000);
            } else {
                final CharSequence name = item.getSpannableName();
                fromTv.setText(!TextUtils.isEmpty(name) ? name : String.valueOf(item.uid));
            }
        }

        private void bindUnreadStatus(ConversationItem item) {
            if (item.ignoreStatus == Conversation.IGNOE_BUT_SHOW_UNREAD) {
                alertIv.setImageResource(R.drawable.little_red_dot1);
                msgNumTView.setBackgroundResource(R.drawable.little_red_dot_number1);
            } else {
                alertIv.setImageResource(R.drawable.little_red_dot);
                msgNumTView.setBackgroundResource(R.drawable.little_red_dot_number);
            }
            if (item.unreadCount > 0) {
                if (item.ignoreStatus == Conversation.IGNOE_UNSHOW_UNREAD) {
                    alertIv.setVisibility(View.VISIBLE);
                    msgNumTView.setVisibility(View.GONE);
                } else {
                    alertIv.setVisibility(View.GONE);
                    msgNumTView.setVisibility(View.VISIBLE);
                    if (item.unreadCount > MAX_SHOW_UNREAD_SIZE) {
                        msgNumTView.setText("...");
                    } else {
                        msgNumTView.setText(String.valueOf(item.unreadCount));
                    }
                }
            } else {
                msgNumTView.setVisibility(View.GONE);
                alertIv.setVisibility(View.GONE);
                if (!TextUtils.isEmpty(item.ext)) {
                    try {
                        JSONObject jsonObject = new JSONObject(item.ext);
                        int followNum = jsonObject.getInt(EXT_IS_FOLLOW_MSG);
                        if (followNum > 0) {
                            alertIv.setVisibility(View.VISIBLE);
                        }
                    } catch (Exception e) {
                        // nothing to do
                    }
                }
            }
        }
    }

    public static class ConversationItem extends ClickItemAdapter.BaseItem {
        public long id;
        public long uid;
        private User user;

        // 对话列表图标，若localAvatarResId > 0,则使用localAvatarResId，否则使用icon
        private int localAvatarResId = 0;
        private int targetType;
        private int focusState;
        private int certificationType; // 用来标识用户头像右下角的角标
        private int msgType; // 消息类型

        private CharSequence spannableSubject;
        private CharSequence spannableName;

        public long receivedTime = 0L;
        private int ignoreStatus;
        public int unreadCount = 0;
        private boolean hasSomeOneAtMe; // 是否有人At我
        private String ext;  //extra信息

        // 进入对话详情需要
        private SixinTarget mSixinTarget;

        public ConversationItem() {
        }

        public ConversationItem(Conversation conversation, int avatarResId) {
            localAvatarResId = avatarResId;
            updateFrom(conversation);
        }

        public SixinTarget getSixinTarget() {
            if (mSixinTarget == null) {
                mSixinTarget = new SixinTarget(user, focusState, targetType);
            }
            return mSixinTarget;
        }

        public void updateFrom(Conversation conversation) {
            id = conversation.getId();
            msgType = conversation.getMsgType();
            uid = conversation.getTarget();

            if (conversation.getUnreadCount() != null) {
                unreadCount = conversation.getUnreadCount();
            }
            receivedTime = conversation.getReceivedTime();
            spannableName = conversation.getTargetName();
            spannableSubject = conversation.getContent();
            if (!TextUtils.isEmpty(spannableSubject)) {
                spannableSubject = SmileyParser.getInstance().addSmileySpans(GlobalData.app(),
                        spannableSubject,
                        DisplayUtils.dip2px(12.0f), true, false, true);
            }
            ignoreStatus = conversation.getIgnoreStatus() != null ?
                    conversation.getIgnoreStatus() : Conversation.NOT_IGNORE;
            ext = conversation.getExt();

            certificationType = conversation.getCertificationType() != null ?
                    conversation.getCertificationType() : 0;
            targetType = conversation.getTargetType();
            focusState = conversation.getFocusStatue();
            hasSomeOneAtMe = conversation.hasSomeOneAtMe();

            user = new User();
            user.setUid(uid);
            user.setNickname(conversation.getTargetName());
            user.setCertificationType(certificationType);
        }

        public final CharSequence getSpannableName() {
            if (uid == Conversation.UNFOCUS_CONVERSATION_TARGET) {
                return GlobalData.app().getResources().getString(R.string.unfocus_robot_name);
            }
            return spannableName;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ConversationItem) {
                return this == obj || uid == ((ConversationItem) obj).uid;
            } else {
                return super.equals(obj);
            }
        }
    }

    public interface IConversationClickListener {
        void onItemClick(ConversationItem item);
    }
}
