package com.module.msg.custom.club;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import io.rong.imkit.R.id;
import io.rong.imkit.R.layout;
import io.rong.imkit.RongIM;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.widget.provider.IContainerItemProvider.MessageProvider;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

@ProviderTag(
        messageContent = ClubInviteMsg.class,
        showReadState = true
)
public class ClubInviteMessageItemProvider extends MessageProvider<ClubInviteMsg> {
    private static final String TAG = "MyTestMessageItemProvider";

    public ClubInviteMessageItemProvider() {
    }

    public View  newView(Context context, ViewGroup group) {
        View view = LayoutInflater.from(context).inflate(layout.rc_item_test_message1, (ViewGroup)null);
        ClubInviteMessageItemProvider.ViewHolder holder = new ClubInviteMessageItemProvider.ViewHolder();
        holder.counterTv =  view.findViewById(id.counter_tv);
        view.setTag(holder);
        return view;
    }

    public void bindView(View v, int position, ClubInviteMsg msg, UIMessage message) {
        ClubInviteMessageItemProvider.ViewHolder holder = (ClubInviteMessageItemProvider.ViewHolder)v.getTag();
        holder.counterTv.setText(message.getExtra());
    }

    @Override
    public Spannable getContentSummary(ClubInviteMsg gifMessage) {
        return new SpannableString("[家族邀请]");
    }

    @Override
    public void onItemClick(View view, int i, ClubInviteMsg msg, UIMessage uiMessage) {
        JSONObject jo = JSON.parseObject(uiMessage.getExtra());
        jo.put("hasDeal",true);
        uiMessage.setExtra(jo.toJSONString());
        RongIM.getInstance().setMessageExtra(uiMessage.getMessageId(),uiMessage.getExtra());
        bindView(view,i,msg,uiMessage);
    }

    private static class ViewHolder {
        TextView counterTv;
        private ViewHolder() {
        }
    }
}
