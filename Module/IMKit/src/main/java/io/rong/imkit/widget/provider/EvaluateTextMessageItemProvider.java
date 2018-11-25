//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.widget.provider;

import android.content.Context;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import io.rong.imkit.R;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM.ConversationBehaviorListener;
import io.rong.imkit.RongIM.ConversationClickListener;
import io.rong.imkit.emoticon.AndroidEmoji;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.widget.AutoLinkTextView;
import io.rong.imkit.widget.ILinkClickListener;
import io.rong.imkit.widget.LinkTextViewMovementMethod;
import io.rong.imkit.widget.provider.IContainerItemProvider.MessageProvider;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Message.MessageDirection;
import io.rong.message.TextMessage;

public class EvaluateTextMessageItemProvider extends MessageProvider<TextMessage> {
  public EvaluateTextMessageItemProvider() {
  }

  public View newView(Context context, ViewGroup group) {
    View view = LayoutInflater.from(context).inflate(R.layout.rc_item_text_message_evaluate, (ViewGroup)null);
    io.rong.imkit.widget.provider.EvaluateTextMessageItemProvider.ViewHolder holder = new io.rong.imkit.widget.provider.EvaluateTextMessageItemProvider.ViewHolder();
    holder.message = (AutoLinkTextView)view.findViewById(R.id.evaluate_text);
    holder.tv_prompt = (TextView)view.findViewById(R.id.tv_prompt);
    holder.iv_yes = (ImageView)view.findViewById(R.id.iv_yes);
    holder.iv_no = (ImageView)view.findViewById(R.id.iv_no);
    holder.iv_complete = (ImageView)view.findViewById(R.id.iv_complete);
    holder.layout_praise = (RelativeLayout)view.findViewById(R.id.layout_praise);
    view.setTag(holder);
    return view;
  }

  public Spannable getContentSummary(TextMessage data) {
    return null;
  }

  public Spannable getContentSummary(Context context, TextMessage data) {
    if (data == null) {
      return null;
    } else {
      String content = data.getContent();
      if (content != null) {
        if (content.length() > 100) {
          content = content.substring(0, 100);
        }

        return new SpannableString(AndroidEmoji.ensure(content));
      } else {
        return null;
      }
    }
  }

  public void onItemClick(View view, int position, TextMessage content, UIMessage message) {
  }

  public void bindView(final View v, final int position, final TextMessage content, final UIMessage data) {
    final io.rong.imkit.widget.provider.EvaluateTextMessageItemProvider.ViewHolder holder = (io.rong.imkit.widget.provider.EvaluateTextMessageItemProvider.ViewHolder)v.getTag();
    if (data.getMessageDirection() == MessageDirection.SEND) {
      v.setBackgroundResource(R.drawable.rc_ic_bubble_right);
    } else {
      v.setBackgroundResource(R.drawable.rc_ic_bubble_left);
    }

    if (data.getEvaluated()) {
      holder.iv_yes.setVisibility(View.GONE);
      holder.iv_no.setVisibility(View.GONE);
      holder.iv_complete.setVisibility(View.VISIBLE);
      holder.tv_prompt.setText("感谢您的评价");
    } else {
      holder.iv_yes.setVisibility(View.VISIBLE);
      holder.iv_no.setVisibility(View.VISIBLE);
      holder.iv_complete.setVisibility(View.GONE);
      holder.tv_prompt.setText("您对我的回答");
    }

    holder.iv_yes.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        String extra = ((TextMessage)data.getContent()).getExtra();
        String knowledgeId = "";
        if (!TextUtils.isEmpty(extra)) {
          try {
            JSONObject jsonObj = new JSONObject(extra);
            knowledgeId = jsonObj.optString("sid");
          } catch (JSONException var5) {
            ;
          }
        }

        RongIMClient.getInstance().evaluateCustomService(data.getSenderUserId(), true, knowledgeId);
        holder.iv_complete.setVisibility(View.VISIBLE);
        holder.iv_yes.setVisibility(View.GONE);
        holder.iv_no.setVisibility(View.GONE);
        holder.tv_prompt.setText("感谢您的评价");
        data.setEvaluated(true);
      }
    });
    holder.iv_no.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        String extra = ((TextMessage)data.getContent()).getExtra();
        String knowledgeId = "";
        if (!TextUtils.isEmpty(extra)) {
          try {
            JSONObject jsonObj = new JSONObject(extra);
            knowledgeId = jsonObj.optString("sid");
          } catch (JSONException var5) {
            ;
          }
        }

        RongIMClient.getInstance().evaluateCustomService(data.getSenderUserId(), false, knowledgeId);
        holder.iv_complete.setVisibility(View.VISIBLE);
        holder.iv_yes.setVisibility(View.GONE);
        holder.iv_no.setVisibility(View.GONE);
        holder.tv_prompt.setText("感谢您的评价");
        data.setEvaluated(true);
      }
    });
    final TextView textView = holder.message;
    if (data.getTextMessageContent() != null) {
      int len = data.getTextMessageContent().length();
      if (v.getHandler() != null && len > 500) {
        v.getHandler().postDelayed(new Runnable() {
          public void run() {
            textView.setText(data.getTextMessageContent());
          }
        }, 50L);
      } else {
        textView.setText(data.getTextMessageContent());
      }
    }

    holder.message.setClickable(true);
    holder.message.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
      }
    });
    holder.message.setOnLongClickListener(new OnLongClickListener() {
      public boolean onLongClick(View v1) {
        io.rong.imkit.widget.provider.EvaluateTextMessageItemProvider.this.onItemLongClick(v, position, content, data);
        return false;
      }
    });
    holder.message.setMovementMethod(new LinkTextViewMovementMethod(new ILinkClickListener() {
      public boolean onLinkClick(String link) {
        ConversationBehaviorListener listener = RongContext.getInstance().getConversationBehaviorListener();
        ConversationClickListener clickListener = RongContext.getInstance().getConversationClickListener();
        boolean result = false;
        if (listener != null) {
          result = listener.onMessageLinkClick(v.getContext(), link);
        } else if (clickListener != null) {
          result = clickListener.onMessageLinkClick(v.getContext(), link, data.getMessage());
        }

        if (listener == null && clickListener == null || !result) {
          String str = link.toLowerCase();
          if (str.startsWith("http") || str.startsWith("https")) {
            Intent intent = new Intent("io.io.rong.imkit.intent.action.webview");
            intent.setPackage(v.getContext().getPackageName());
            intent.putExtra("url", link);
            v.getContext().startActivity(intent);
            result = true;
          }
        }

        return result;
      }
    }));
  }

  private static class ViewHolder {
    AutoLinkTextView message;
    TextView tv_prompt;
    ImageView iv_yes;
    ImageView iv_no;
    ImageView iv_complete;
    RelativeLayout layout_praise;
    boolean longClick;

    private ViewHolder() {
    }
  }
}
