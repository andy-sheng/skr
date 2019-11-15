package com.module.msg.custom;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import io.rong.imlib.model.Message;
import io.rong.imlib.model.Message.MessageDirection;
import io.rong.imlib.model.Message.SentStatus;
import io.rong.message.GIFMessage;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

@ProviderTag(
        messageContent = GIFMessage.class,
        showProgress = false,
        showReadState = true
)
public class MyGIFMessageItemProvider extends MessageProvider<GIFMessage> {
    private static final String TAG = "MyGIFMessageItemProvider";

    public MyGIFMessageItemProvider() {
    }

    public View newView(Context context, ViewGroup group) {
        View view = LayoutInflater.from(context).inflate(layout.rc_item_gif_message2, (ViewGroup)null);
        MyGIFMessageItemProvider.ViewHolder holder = new MyGIFMessageItemProvider.ViewHolder();
        holder.img = (EnhancedImageView)view.findViewById(id.rc_img);
        holder.preProgress = (ProgressBar)view.findViewById(id.rc_pre_progress);
        holder.loadingProgress = (CircleProgressView)view.findViewById(id.rc_gif_progress);
        holder.startDownLoad = (ImageView)view.findViewById(id.rc_start_download);
        holder.downLoadFailed = (ImageView)view.findViewById(id.rc_download_failed);
        holder.length = (TextView)view.findViewById(id.rc_length);
        view.setTag(holder);
        return view;
    }

    public void onItemClick(View view, int position, GIFMessage content, UIMessage message) {
        MyGIFMessageItemProvider.ViewHolder holder = (MyGIFMessageItemProvider.ViewHolder)view.getTag();
        if (holder.startDownLoad.getVisibility() == View.VISIBLE) {
            holder.startDownLoad.setVisibility(View.GONE);
            if (this.checkPermission(view.getContext())) {
                this.downLoad(message.getMessage(), holder);
            } else {
                holder.downLoadFailed.setVisibility(View.VISIBLE);
                holder.length.setVisibility(View.VISIBLE);
                holder.length.setText(this.formatSize(content.getGifDataSize()));
                Toast.makeText(view.getContext(), string.rc_ac_file_download_request_permission, Toast.LENGTH_SHORT).show();
            }
        } else if (holder.downLoadFailed.getVisibility() == View.VISIBLE) {
            holder.downLoadFailed.setVisibility(View.GONE);
            if (this.checkPermission(view.getContext())) {
                this.downLoad(message.getMessage(), holder);
            } else {
                holder.downLoadFailed.setVisibility(View.VISIBLE);
                holder.length.setVisibility(View.VISIBLE);
                holder.length.setText(this.formatSize(content.getGifDataSize()));
                Toast.makeText(view.getContext(), string.rc_ac_file_download_request_permission, Toast.LENGTH_SHORT).show();
            }
        } else if (holder.preProgress.getVisibility() != View.VISIBLE && holder.loadingProgress.getVisibility() != View.VISIBLE && content != null) {
            Intent intent = new Intent("io.rong.imkit.intent.action.gifrview");
            intent.setPackage(view.getContext().getPackageName());
            intent.putExtra("message", message.getMessage());
            view.getContext().startActivity(intent);
        }

    }

    public void bindView(View v, int position, GIFMessage content, UIMessage message) {
        MyGIFMessageItemProvider.ViewHolder holder = (MyGIFMessageItemProvider.ViewHolder)v.getTag();
        holder.startDownLoad.setVisibility(View.GONE);
        holder.downLoadFailed.setVisibility(View.GONE);
        holder.preProgress.setVisibility(View.GONE);
        holder.loadingProgress.setVisibility(View.GONE);
        holder.length.setVisibility(View.GONE);
        int[] paramsValue = this.getParamsValue(v.getContext(), content.getWidth(), content.getHeight());
        ViewGroup.LayoutParams layoutParams = holder.img.getLayoutParams();
        layoutParams.width = paramsValue[0];
        layoutParams.height = paramsValue[1];
//        holder.img.setImageDrawable(v.getContext().getResources().getDrawable(drawable.def_gif_bg));
        int progress = message.getProgress();
        if (message.getMessageDirection() == MessageDirection.SEND) {
            SentStatus status = message.getSentStatus();
            if (progress > 0 && progress < 100) {
                holder.loadingProgress.setProgress(progress, true);
                holder.loadingProgress.setVisibility(View.VISIBLE);
                holder.preProgress.setVisibility(View.GONE);
            } else if (status.equals(SentStatus.SENDING)) {
                holder.loadingProgress.setVisibility(View.GONE);
                holder.preProgress.setVisibility(View.VISIBLE);
            } else if (progress == -1) {
                holder.loadingProgress.setVisibility(View.GONE);
                holder.preProgress.setVisibility(View.GONE);
                holder.downLoadFailed.setVisibility(View.VISIBLE);
                holder.length.setVisibility(View.VISIBLE);
            } else {
                holder.loadingProgress.setVisibility(View.GONE);
                holder.preProgress.setVisibility(View.GONE);
            }
        } else if (message.getReceivedStatus().isDownload()) {
            if (progress > 0 && progress < 100) {
                holder.loadingProgress.setProgress(progress, true);
                holder.loadingProgress.setVisibility(View.VISIBLE);
                holder.preProgress.setVisibility(View.GONE);
                holder.startDownLoad.setVisibility(View.GONE);
            } else if (progress == 100) {
                holder.loadingProgress.setVisibility(View.GONE);
                holder.preProgress.setVisibility(View.GONE);
                holder.length.setVisibility(View.GONE);
                holder.startDownLoad.setVisibility(View.GONE);
            } else if (progress == -1) {
                holder.loadingProgress.setVisibility(View.GONE);
                holder.preProgress.setVisibility(View.GONE);
                holder.downLoadFailed.setVisibility(View.VISIBLE);
                holder.length.setVisibility(View.VISIBLE);
                holder.startDownLoad.setVisibility(View.GONE);
            } else {
                holder.loadingProgress.setVisibility(View.GONE);
                holder.preProgress.setVisibility(View.VISIBLE);
                holder.length.setVisibility(View.VISIBLE);
                holder.startDownLoad.setVisibility(View.GONE);
            }
        } else {
            holder.loadingProgress.setVisibility(View.GONE);
            holder.preProgress.setVisibility(View.GONE);
            holder.length.setVisibility(View.GONE);
            holder.startDownLoad.setVisibility(View.GONE);
            if (progress == -1) {
                holder.downLoadFailed.setVisibility(View.VISIBLE);
                holder.length.setVisibility(View.VISIBLE);
                holder.length.setText(this.formatSize(content.getGifDataSize()));
            }
        }

        if (content.getLocalPath() != null) {
            if (!content.isDestruct()) {
                this.loadGif(v, content.getLocalUri(), holder);
            }
        } else {
            int size = v.getContext().getResources().getInteger(integer.rc_gifmsg_auto_download_size);
            if (content.getGifDataSize() <= (long)(size * 1024)) {
                if (this.checkPermission(v.getContext())) {
                    if (!message.getReceivedStatus().isDownload()) {
                        message.getReceivedStatus().setDownload();
                        this.downLoad(message.getMessage(), holder);
                    }
                } else if (progress != -1) {
                    holder.startDownLoad.setVisibility(View.VISIBLE);
                    holder.length.setVisibility(View.VISIBLE);
                    holder.length.setText(this.formatSize(content.getGifDataSize()));
                }
            } else if (progress > 0 && progress < 100) {
                holder.startDownLoad.setVisibility(View.GONE);
                holder.length.setVisibility(View.VISIBLE);
                holder.length.setText(this.formatSize(content.getGifDataSize()));
            } else if (progress != -1) {
                holder.startDownLoad.setVisibility(View.VISIBLE);
                holder.preProgress.setVisibility(View.GONE);
                holder.loadingProgress.setVisibility(View.GONE);
                holder.downLoadFailed.setVisibility(View.GONE);
                holder.length.setVisibility(View.VISIBLE);
                holder.length.setText(this.formatSize(content.getGifDataSize()));
            }
        }

    }

    public Spannable getContentSummary(GIFMessage data) {
        return null;
    }

    public Spannable getContentSummary(Context context, GIFMessage data) {
        return new SpannableString(context.getString(string.rc_message_content_image));
    }

    private void downLoad(Message downloadMsg, MyGIFMessageItemProvider.ViewHolder holder) {
        holder.preProgress.setVisibility(View.VISIBLE);
        RongIM.getInstance().downloadMediaMessage(downloadMsg, (IDownloadMediaMessageCallback)null);
    }

    private void loadGif(View v, Uri uri, MyGIFMessageItemProvider.ViewHolder holder) {
        holder.img.load(uri.toString());
//        ((RequestBuilder)Glide.with(v.getContext()).asGif().diskCacheStrategy(DiskCacheStrategy.RESOURCE)).load(uri.getPath()).listener(new RequestListener<GifDrawable>() {
//            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
//                return false;
//            }
//
//            public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
//                return false;
//            }
//        }).into(holder.img);
    }

    private String formatSize(long length) {
        float size;
        if (length > 1048576L) {
            size = (float)Math.round((float)length / 1048576.0F * 100.0F) / 100.0F;
            return size + "M";
        } else if (length > 1024L) {
            size = (float)Math.round((float)length / 1024.0F * 100.0F) / 100.0F;
            return size + "KB";
        } else {
            return length + "B";
        }
    }

    private int[] getParamsValue(Context context, int width, int height) {
        int maxWidth = dip2px(context, 120.0F);
        int minValue = dip2px(context, 80.0F);
        float scale;
        int finalWidth;
        int finalHeight;
        if (width > maxWidth) {
            finalWidth = maxWidth;
            scale = (float)width / (float)maxWidth;
            finalHeight = Math.round((float)height / scale);
            if (finalHeight < minValue) {
                finalHeight = minValue;
            }
        } else if (width < minValue) {
            finalWidth = minValue;
            scale = (float)width / (float)minValue;
            finalHeight = Math.round((float)height * scale);
            if (finalHeight < minValue) {
                finalHeight = minValue;
            }
        } else {
            finalWidth = Math.round((float)height);
            finalHeight = Math.round((float)width);
        }

        int[] params = new int[]{finalWidth, finalHeight};
        return params;
    }

    public static int dip2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dpValue * scale + 0.5F);
    }

    private boolean checkPermission(Context context) {
        String[] permission = new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"};
        return PermissionCheckUtil.checkPermissions(context, permission);
    }

    private static class ViewHolder {
        EnhancedImageView img;
        ProgressBar preProgress;
        CircleProgressView loadingProgress;
        ImageView startDownLoad;
        ImageView downLoadFailed;
        TextView length;

        private ViewHolder() {
        }
    }
}
