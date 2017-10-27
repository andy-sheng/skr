package com.wali.live.watchsdk.sixin.recycler;

import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.base.log.MyLog;
import com.base.view.MLTextView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.mi.live.data.account.MyUserInfoManager;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.lit.recycler.holder.BaseHolder;
import com.wali.live.watchsdk.sixin.message.SixinMessageItem;

/**
 * Created by lan on 16-5-20.
 */
public class SixinMessageHolder extends BaseHolder<SixinMessageItem> {
    public SimpleDraweeView avatar;
    public ImageView certificationType;
    public ImageView resendBtnDV;

    public FrameLayout bubbleArea;
    public LinearLayout messageContent;

    public MLTextView sendStatus;
    public MLTextView recvMsgTimestamp;

    public TextView mSenderNickname;

    public SixinMessageHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void initView() {
        avatar = $(R.id.avatar);
        certificationType = $(R.id.user_certification_type);
        resendBtnDV = $(R.id.resend_btn);
        bubbleArea = $(R.id.bubble_area);
        messageContent = $(R.id.message_content);
        sendStatus = $(R.id.send_msg_status);
        recvMsgTimestamp = $(R.id.recv_msg_time_stamp);
        mSenderNickname = $(R.id.sender_nickname);
    }

    @Override
    protected void bindView() {

    }

    // 设置头像
    protected void setSenderAvatarAndName() {
        if (mViewModel.isInbound()) { // 收到的消息
            if (mViewModel.getSender() == null) {
                MyLog.w(TAG, "setSenderAvatarAndName getItemData().getSenderBuddy() == null");
                return;
            }

            if (mViewModel.getSender() != null) {
                String url = AvatarUtils.getAvatarUrlByUid(mViewModel.getSender().getUid(), mViewModel.getSender().getAvatar());
                AvatarUtils.loadAvatarByUrl(avatar, url, true);
            }
        } else { // 自己发出的消息
            String url = AvatarUtils.getAvatarUrlByUid(MyUserInfoManager.getInstance().getUuid(), mViewModel.getSender().getAvatar());
            AvatarUtils.loadAvatarByUrl(avatar, url, true);
        }
    }

    // 设置发送者名字
    protected void bindSenderNickName(SixinMessageHolder sixInMessageViewHolder, SixinMessageItem sixInMessageItem) {
        String name = TextUtils.isEmpty(sixInMessageItem.getSender().getNickname()) ? String.valueOf(sixInMessageItem.getSender().getUid()) : sixInMessageItem.getSender().getNickname();
        sixInMessageViewHolder.mSenderNickname.setText(name);
    }
}