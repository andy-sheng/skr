package com.module.msg.test1;

import android.content.Context;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.imagebrowse.EnhancedImageView;
import com.imagebrowse.big.BigImageBrowseFragment;
import com.module.msg.custom.MyGIFMessageItemProvider;

import io.rong.imkit.R.id;
import io.rong.imkit.R.integer;
import io.rong.imkit.R.layout;
import io.rong.imkit.R.string;
import io.rong.imkit.RongIM;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.utilities.PermissionCheckUtil;
import io.rong.imkit.widget.CircleProgressView;
import io.rong.imkit.widget.provider.IContainerItemProvider.MessageProvider;
import io.rong.imlib.IRongCallback.IDownloadMediaMessageCallback;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.Message.MessageDirection;
import io.rong.imlib.model.Message.SentStatus;
import io.rong.message.GIFMessage;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

@ProviderTag(
        messageContent = CustomTestMsg.class,
        showReadState = true
)
public class MyTestMessageItemProvider extends MessageProvider<CustomTestMsg> {
    private static final String TAG = "MyTestMessageItemProvider";

    public MyTestMessageItemProvider() {
    }

    public View  newView(Context context, ViewGroup group) {
        View view = LayoutInflater.from(context).inflate(layout.rc_item_test_message1, (ViewGroup)null);
        MyTestMessageItemProvider.ViewHolder holder = new MyTestMessageItemProvider.ViewHolder();
        holder.counterTv =  view.findViewById(id.counter_tv);
        view.setTag(holder);
        return view;
    }

    public void bindView(View v, int position, CustomTestMsg msg, UIMessage message) {
        MyTestMessageItemProvider.ViewHolder holder = (MyTestMessageItemProvider.ViewHolder)v.getTag();
        holder.counterTv.setText(msg.getContent()+" "+message.getExtra());
    }

    @Override
    public Spannable getContentSummary(CustomTestMsg gifMessage) {
        return new SpannableString("[数字消息]");
    }

    @Override
    public void onItemClick(View view, int i, CustomTestMsg msg, UIMessage uiMessage) {
//        msg.setContent(msg.getContent()+"消息已经被处理");
        uiMessage.setExtra("处理了"+uiMessage.getExtra());
        RongIM.getInstance().setMessageExtra(uiMessage.getMessageId(),uiMessage.getExtra());
        bindView(view,i,msg,uiMessage);
//        RongIM.getInstance().up(Conversation.ConversationType.PRIVATE,uiMessage.getTargetId(),uiMessage.getSenderUserId(),msg);
    }

    private static class ViewHolder {
        TextView counterTv;
        private ViewHolder() {
        }
    }
}
