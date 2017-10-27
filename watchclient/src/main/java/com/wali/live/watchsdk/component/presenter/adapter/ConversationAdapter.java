package com.wali.live.watchsdk.component.presenter.adapter;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.base.view.MLTextView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.adapter.ClickItemAdapter;

/**
 * Created by yangli on 2017/10/27.
 */
public class ConversationAdapter extends ClickItemAdapter<ConversationAdapter.ConversationItem,
        ClickItemAdapter.BaseHolder, ConversationAdapter.IConversationClickListener> {

    @Override
    public BaseHolder newViewHolder(int viewType) {
        View view = mInflater.inflate(R.layout.conversation_list_item, null);
        return new ConversationHolder(view);
    }

    protected class ConversationHolder extends ClickItemAdapter.BaseHolder<ConversationItem, Object>
            implements View.OnClickListener {

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
        }

        @Override
        public void onClick(View v) {
        }

        @Override
        public void bindView(ConversationItem item, Object listener) {
        }
    }

    public static class ConversationItem extends ClickItemAdapter.BaseItem {
    }

    public interface IConversationClickListener {
        void onItemClick(ConversationItem item);
    }
}
