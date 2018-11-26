//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.widget.provider;

import android.content.Context;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import io.rong.imkit.R;
import io.rong.imkit.RongIM;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.utils.FileTypeUtils;
import io.rong.imkit.widget.provider.IContainerItemProvider.MessageProvider;
import io.rong.imlib.RongIMClient.ErrorCode;
import io.rong.imlib.RongIMClient.OperationCallback;
import io.rong.imlib.model.Message.MessageDirection;
import io.rong.imlib.model.Message.SentStatus;
import io.rong.message.FileMessage;

@ProviderTag(
        messageContent = FileMessage.class,
        showProgress = false,
        showReadState = true
)
public class FileMessageItemProvider extends MessageProvider<FileMessage> {
    private static final String TAG = "FileMessageItemProvider";

    public FileMessageItemProvider() {
    }

    public View newView(Context context, ViewGroup group) {
        View view = LayoutInflater.from(context).inflate(R.layout.rc_item_file_message, (ViewGroup) null);
        io.rong.imkit.widget.provider.FileMessageItemProvider.ViewHolder holder = new io.rong.imkit.widget.provider.FileMessageItemProvider.ViewHolder();
        holder.message = (LinearLayout) view.findViewById(R.id.rc_message);
        holder.fileTypeImage = (ImageView) view.findViewById(R.id.rc_msg_iv_file_type_image);
        holder.fileName = (TextView) view.findViewById(R.id.rc_msg_tv_file_name);
        holder.fileSize = (TextView) view.findViewById(R.id.rc_msg_tv_file_size);
        holder.fileUploadProgress = (ProgressBar) view.findViewById(R.id.rc_msg_pb_file_upload_progress);
        holder.cancelButton = (RelativeLayout) view.findViewById(R.id.rc_btn_cancel);
        holder.canceledMessage = (TextView) view.findViewById(R.id.rc_msg_canceled);
        view.setTag(holder);
        return view;
    }

    public void bindView(View v, int position, FileMessage content, final UIMessage message) {
        final io.rong.imkit.widget.provider.FileMessageItemProvider.ViewHolder holder = (io.rong.imkit.widget.provider.FileMessageItemProvider.ViewHolder) v.getTag();
        if (message.getMessageDirection() == MessageDirection.SEND) {
            holder.message.setBackgroundResource(R.drawable.rc_ic_bubble_right_file);
        } else {
            holder.message.setBackgroundResource(R.drawable.rc_ic_bubble_left_file);
        }

        holder.fileName.setText(content.getName());
        long fileSizeBytes = content.getSize();
        holder.fileSize.setText(FileTypeUtils.formatFileSize(fileSizeBytes));
        holder.fileTypeImage.setImageResource(FileTypeUtils.fileTypeImageId(content.getName()));
        if (message.getSentStatus().equals(SentStatus.SENDING) && message.getProgress() < 100) {
            holder.fileUploadProgress.setVisibility(0);
            holder.cancelButton.setVisibility(0);
            holder.canceledMessage.setVisibility(4);
            holder.fileUploadProgress.setProgress(message.getProgress());
        } else {
            if (message.getSentStatus().equals(SentStatus.CANCELED)) {
                holder.canceledMessage.setVisibility(0);
            } else {
                holder.canceledMessage.setVisibility(4);
            }

            holder.fileUploadProgress.setVisibility(4);
            holder.cancelButton.setVisibility(8);
        }

        holder.cancelButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                RongIM.getInstance().cancelSendMediaMessage(message.getMessage(), new OperationCallback() {
                    public void onSuccess() {
                        holder.canceledMessage.setVisibility(0);
                        holder.fileUploadProgress.setVisibility(4);
                        holder.cancelButton.setVisibility(8);
                    }

                    public void onError(ErrorCode errorCode) {
                    }
                });
            }
        });
    }

    public Spannable getContentSummary(FileMessage data) {
        return null;
    }

    public Spannable getContentSummary(Context context, FileMessage data) {
        StringBuilder summaryPhrase = new StringBuilder();
        String fileName = data.getName();
        summaryPhrase.append(context.getString(R.string.rc_message_content_file)).append(" ").append(fileName);
        return new SpannableString(summaryPhrase);
    }

    public void onItemClick(View view, int position, FileMessage content, UIMessage message) {
        Intent intent = new Intent("io.io.rong.imkit.intent.action.openfile");
        intent.setPackage(view.getContext().getPackageName());
        intent.putExtra("FileMessage", content);
        intent.putExtra("Message", message.getMessage());
        intent.putExtra("Progress", message.getProgress());
        view.getContext().startActivity(intent);
    }

    private static class ViewHolder {
        RelativeLayout cancelButton;
        LinearLayout message;
        TextView fileName;
        TextView fileSize;
        TextView canceledMessage;
        ImageView fileTypeImage;
        ProgressBar fileUploadProgress;

        private ViewHolder() {
        }
    }
}
