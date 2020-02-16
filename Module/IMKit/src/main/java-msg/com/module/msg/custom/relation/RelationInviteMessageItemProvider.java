package com.module.msg.custom.relation;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;

import io.rong.imkit.R;
import io.rong.imkit.R.layout;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.widget.provider.IContainerItemProvider.MessageProvider;
import io.rong.imlib.model.Message;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

@ProviderTag(
        messageContent = RelationInviteMsg.class,
        showReadState = true
)
public class RelationInviteMessageItemProvider extends MessageProvider<RelationInviteMsg> {
    private static final String TAG = "RelationInviteMessageItemProvider";

    public RelationInviteMessageItemProvider() {
    }

    RelationInviteMsg contentMsg;
    Message message;

    public View  newView(Context context, ViewGroup group) {
        View view = LayoutInflater.from(context).inflate(layout.rc_item_relation_invite_message, (ViewGroup)null);
        RelationInviteMessageItemProvider.ViewHolder holder = new RelationInviteMessageItemProvider.ViewHolder(view);
        holder.mAgreeTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                RelationMsgProcessor.agree(message.getUId(),contentMsg.getUniqID());
            }
        });
        holder.mRejectTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                RelationMsgProcessor.reject(message.getUId(),contentMsg.getUniqID());
            }
        });
        view.setTag(holder);
        return view;
    }

    public void bindView(View v, int position, RelationInviteMsg msg, UIMessage message) {
        this.contentMsg = msg;
        this.message = message.getMessage();

        RelationInviteMessageItemProvider.ViewHolder holder = (RelationInviteMessageItemProvider.ViewHolder)v.getTag();
        holder.mContentTv.setText(msg.getContent());
        int handle = RelationMsgProcessor.getHandle(message.getMessage());
        if(handle==0){
            holder.mAgreeTv.setVisibility(View.VISIBLE);
            holder.mRejectTv.setVisibility(View.VISIBLE);
            holder.mTipsTv.setVisibility(View.GONE);
        }else if(handle==1){
            holder.mAgreeTv.setVisibility(View.GONE);
            holder.mRejectTv.setVisibility(View.GONE);
            holder.mTipsTv.setVisibility(View.VISIBLE);
            holder.mTipsTv.setText("已同意关系建立");
        }else if(handle==2){
            holder.mAgreeTv.setVisibility(View.GONE);
            holder.mRejectTv.setVisibility(View.GONE);
            holder.mTipsTv.setVisibility(View.VISIBLE);
            holder.mTipsTv.setText("已拒绝关系建立");
        }
    }

    @Override
    public Spannable getContentSummary(RelationInviteMsg msg) {
        return new SpannableString(msg.getContent());
    }

    @Override
    public void onItemClick(View view, int i, RelationInviteMsg msg, UIMessage uiMessage) {
//        JSONObject jo = ClubMsgProcessor.getInviteInfo(uiMessage.getMessage());
//        if(jo!=null && jo.getIntValue("status") == 0
//                && !uiMessage.getSenderUserId().equals(MyUserInfoManager.INSTANCE.getUidStr())){
//            // 点击了同意
//            // 发送一条同意消息
//            ClubHandleMsg contentMsg = ClubHandleMsg.obtain();
//            contentMsg.setMsgUid(uiMessage.getUId());
//            Message msg1 = Message.obtain(uiMessage.getTargetId(), Conversation.ConversationType.PRIVATE, contentMsg);
//
//            RongIM.getInstance().sendMessage(msg1, "pushContent", "pushData", new IRongCallback.ISendMessageCallback() {
//                @Override
//                public void onAttached(Message message) {
//
//                }
//
//                @Override
//                public void onSuccess(Message message) {
//                    // 发成功后 强制存下数据库 不然再进列表又是空的了
//                    jo.put("status",1);
//                    uiMessage.setExtra(jo.toJSONString());
//                    RongIM.getInstance().setMessageExtra(uiMessage.getMessageId(),uiMessage.getExtra());
//                    bindView(view,i,msg,uiMessage);
//                }
//
//                @Override
//                public void onError(Message message, RongIMClient.ErrorCode errorCode) {
//                }
//            });
//        }

//        else{
//            jo.put("status",1);
//            uiMessage.setExtra(jo.toJSONString());
//            RongIM.getInstance().setMessageExtra(uiMessage.getMessageId(),uiMessage.getExtra());
//            bindView(view,i,msg,uiMessage);
//            Log.d("CSM","messageId="+uiMessage.getMessageId());
//        }
    }

    private static class ViewHolder {
        TextView mContentTv;
        ExTextView mRejectTv;
        ExTextView mAgreeTv;
        ExTextView mTipsTv;


        public ViewHolder(View rootView) {
            mContentTv = (TextView)rootView.findViewById(R.id.content_tv);
            mRejectTv = (ExTextView)rootView.findViewById(R.id.reject_tv);
            mAgreeTv = (ExTextView)rootView.findViewById(R.id.agree_tv);
            mTipsTv = (ExTextView)rootView.findViewById(R.id.tips_tv);
        }
    }
}
