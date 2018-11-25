//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.OnScanCompletedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import io.rong.common.FileUtils;
import io.rong.common.RLog;
import io.rong.imageloader.core.DisplayImageOptions;
import io.rong.imageloader.core.DisplayImageOptions.Builder;
import io.rong.imageloader.core.ImageLoader;
import io.rong.imageloader.core.assist.FailReason;
import io.rong.imageloader.core.imageaware.ImageAware;
import io.rong.imageloader.core.imageaware.ImageViewAware;
import io.rong.imageloader.core.listener.ImageLoadingListener;
import io.rong.imageloader.core.listener.ImageLoadingProgressListener;
import io.rong.imkit.R;
import io.rong.imkit.RongBaseNoActionbarActivity;
import io.rong.imkit.plugin.image.AlbumBitmapCacheHelper;
import io.rong.imkit.plugin.image.AlbumBitmapCacheHelper.ILoadImageCallback;
import io.rong.imkit.plugin.image.HackyViewPager;
import io.rong.imkit.utilities.OptionsPopupDialog;
import io.rong.imkit.utilities.OptionsPopupDialog.OnOptionsItemClickedListener;
import io.rong.imkit.utilities.PermissionCheckUtil;
import io.rong.imkit.utils.SystemUtils;
import io.rong.imlib.RongCommonDefine.GetMessageDirection;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.RongIMClient.ErrorCode;
import io.rong.imlib.RongIMClient.ResultCallback;
import io.rong.imlib.model.Conversation.ConversationType;
import io.rong.imlib.model.Message;
import io.rong.message.ImageMessage;
import io.rong.photoview.PhotoView;
import io.rong.photoview.PhotoViewAttacher.OnPhotoTapListener;

public class PicturePagerActivity extends RongBaseNoActionbarActivity implements OnLongClickListener {
  private static final String TAG = "PicturePagerActivity";
  private static final int IMAGE_MESSAGE_COUNT = 10;
  private HackyViewPager mViewPager;
  private ImageMessage mCurrentImageMessage;
  private ConversationType mConversationType;
  private int mCurrentMessageId;
  private String mTargetId = null;
  private int mCurrentIndex = 0;
  private ImageAware mDownloadingImageAware;
  private io.rong.imkit.activity.PicturePagerActivity.ImageAdapter mImageAdapter;
  private boolean isFirstTime = false;
  private OnPageChangeListener mPageChangeListener = new OnPageChangeListener() {
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    public void onPageSelected(int position) {
      RLog.i("PicturePagerActivity", "onPageSelected. position:" + position);
      io.rong.imkit.activity.PicturePagerActivity.this.mCurrentIndex = position;
      View view = io.rong.imkit.activity.PicturePagerActivity.this.mViewPager.findViewById(position);
      if (view != null) {
        io.rong.imkit.activity.PicturePagerActivity.this.mImageAdapter.updatePhotoView(position, view);
      }

      if (position == io.rong.imkit.activity.PicturePagerActivity.this.mImageAdapter.getCount() - 1) {
        io.rong.imkit.activity.PicturePagerActivity.this.getConversationImageUris(io.rong.imkit.activity.PicturePagerActivity.this.mImageAdapter.getItem(position).getMessageId(), GetMessageDirection.BEHIND);
      } else if (position == 0) {
        io.rong.imkit.activity.PicturePagerActivity.this.getConversationImageUris(io.rong.imkit.activity.PicturePagerActivity.this.mImageAdapter.getItem(position).getMessageId(), GetMessageDirection.FRONT);
      }

    }

    public void onPageScrollStateChanged(int state) {
    }
  };

  public PicturePagerActivity() {
  }

  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.rc_fr_photo);
    Message currentMessage = (Message)this.getIntent().getParcelableExtra("message");
    this.mCurrentImageMessage = (ImageMessage)currentMessage.getContent();
    this.mConversationType = currentMessage.getConversationType();
    this.mCurrentMessageId = currentMessage.getMessageId();
    this.mTargetId = currentMessage.getTargetId();
    this.mViewPager = (HackyViewPager)this.findViewById(R.id.viewpager);
    this.mViewPager.setOnPageChangeListener(this.mPageChangeListener);
    this.mImageAdapter = new io.rong.imkit.activity.PicturePagerActivity.ImageAdapter();
    this.isFirstTime = true;
    this.getConversationImageUris(this.mCurrentMessageId, GetMessageDirection.FRONT);
    this.getConversationImageUris(this.mCurrentMessageId, GetMessageDirection.BEHIND);
  }

  private void getConversationImageUris(int mesageId, final GetMessageDirection direction) {
    if (this.mConversationType != null && !TextUtils.isEmpty(this.mTargetId)) {
      RongIMClient.getInstance().getHistoryMessages(this.mConversationType, this.mTargetId, "RC:ImgMsg", mesageId, 10, direction, new ResultCallback<List<Message>>() {
        public void onSuccess(List<Message> messages) {
          ArrayList<io.rong.imkit.activity.PicturePagerActivity.ImageInfo> lists = new ArrayList();
          if (messages != null) {
            if (direction.equals(GetMessageDirection.FRONT)) {
              Collections.reverse(messages);
            }

            for(int i = 0; i < messages.size(); ++i) {
              Message message = (Message)messages.get(i);
              if (message.getContent() instanceof ImageMessage) {
                ImageMessage imageMessage = (ImageMessage)message.getContent();
                Uri largeImageUri = imageMessage.getLocalUri() == null ? imageMessage.getRemoteUri() : imageMessage.getLocalUri();
                if (imageMessage.getThumUri() != null && largeImageUri != null) {
                  lists.add(io.rong.imkit.activity.PicturePagerActivity.this.new ImageInfo(message.getMessageId(), imageMessage.getThumUri(), largeImageUri));
                }
              }
            }
          }

          if (direction.equals(GetMessageDirection.FRONT) && io.rong.imkit.activity.PicturePagerActivity.this.isFirstTime) {
            lists.add(io.rong.imkit.activity.PicturePagerActivity.this.new ImageInfo(io.rong.imkit.activity.PicturePagerActivity.this.mCurrentMessageId, io.rong.imkit.activity.PicturePagerActivity.this.mCurrentImageMessage.getThumUri(), io.rong.imkit.activity.PicturePagerActivity.this.mCurrentImageMessage.getLocalUri() == null ? io.rong.imkit.activity.PicturePagerActivity.this.mCurrentImageMessage.getRemoteUri() : io.rong.imkit.activity.PicturePagerActivity.this.mCurrentImageMessage.getLocalUri()));
            io.rong.imkit.activity.PicturePagerActivity.this.mImageAdapter.addData(lists, direction.equals(GetMessageDirection.FRONT));
            io.rong.imkit.activity.PicturePagerActivity.this.mViewPager.setAdapter(io.rong.imkit.activity.PicturePagerActivity.this.mImageAdapter);
            io.rong.imkit.activity.PicturePagerActivity.this.isFirstTime = false;
            io.rong.imkit.activity.PicturePagerActivity.this.mViewPager.setCurrentItem(lists.size() - 1);
            io.rong.imkit.activity.PicturePagerActivity.this.mCurrentIndex = lists.size() - 1;
          } else if (lists.size() > 0) {
            io.rong.imkit.activity.PicturePagerActivity.this.mImageAdapter.addData(lists, direction.equals(GetMessageDirection.FRONT));
            io.rong.imkit.activity.PicturePagerActivity.this.mImageAdapter.notifyDataSetChanged();
            if (direction.equals(GetMessageDirection.FRONT)) {
              io.rong.imkit.activity.PicturePagerActivity.this.mViewPager.setCurrentItem(lists.size());
              io.rong.imkit.activity.PicturePagerActivity.this.mCurrentIndex = lists.size();
            }
          }

        }

        public void onError(ErrorCode e) {
        }
      });
    }

  }

  protected void onPause() {
    super.onPause();
  }

  protected void onDestroy() {
    super.onDestroy();
  }

  public boolean onPictureLongClick(View v, Uri thumbUri, Uri largeImageUri) {
    return false;
  }

  public boolean onLongClick(View v) {
    io.rong.imkit.activity.PicturePagerActivity.ImageInfo imageInfo = this.mImageAdapter.getImageInfo(this.mCurrentIndex);
    if (imageInfo != null) {
      Uri thumbUri = imageInfo.getThumbUri();
      Uri largeImageUri = imageInfo.getLargeImageUri();
      if (this.onPictureLongClick(v, thumbUri, largeImageUri)) {
        return true;
      }

      if (largeImageUri == null) {
        return false;
      }

      final File file;
      if (!largeImageUri.getScheme().startsWith("http") && !largeImageUri.getScheme().startsWith("https")) {
        file = new File(largeImageUri.getPath());
      } else {
        file = ImageLoader.getInstance().getDiskCache().get(largeImageUri.toString());
      }

      String[] items = new String[]{this.getString(R.string.rc_save_picture)};
      OptionsPopupDialog.newInstance(this, items).setOptionsPopupDialogListener(new OnOptionsItemClickedListener() {
        public void onOptionsItemClicked(int which) {
          if (which == 0) {
            String[] permissions = new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"};
            if (!PermissionCheckUtil.requestPermissions(io.rong.imkit.activity.PicturePagerActivity.this, permissions)) {
              return;
            }

            File path = Environment.getExternalStorageDirectory();
            String defaultPath = io.rong.imkit.activity.PicturePagerActivity.this.getString(R.string.rc_image_default_saved_path);
            String appName = SystemUtils.getAppName(io.rong.imkit.activity.PicturePagerActivity.this);
            StringBuilder builder = new StringBuilder(defaultPath);
            if (appName != null) {
              builder.append(appName).append(File.separator);
            }

            String appPath = builder.toString();
            File dir = new File(path, appPath);
            if (!dir.exists()) {
              dir.mkdirs();
            }

            if (file != null && file.exists()) {
              String name = System.currentTimeMillis() + ".jpg";
              FileUtils.copyFile(file, dir.getPath() + File.separator, name);
              MediaScannerConnection.scanFile(io.rong.imkit.activity.PicturePagerActivity.this, new String[]{dir.getPath() + File.separator + name}, (String[])null, (OnScanCompletedListener)null);
              Toast.makeText(io.rong.imkit.activity.PicturePagerActivity.this, io.rong.imkit.activity.PicturePagerActivity.this.getString(R.string.rc_save_picture_at), Toast.LENGTH_SHORT).show();
            } else {
              Toast.makeText(io.rong.imkit.activity.PicturePagerActivity.this, io.rong.imkit.activity.PicturePagerActivity.this.getString(R.string.rc_src_file_not_found), Toast.LENGTH_SHORT).show();
            }
          }

        }
      }).show();
    }

    return true;
  }

  private class ImageInfo {
    private int messageId;
    private Uri thumbUri;
    private Uri largeImageUri;

    ImageInfo(int messageId, Uri thumbnail, Uri largeImageUri) {
      this.messageId = messageId;
      this.thumbUri = thumbnail;
      this.largeImageUri = largeImageUri;
    }

    public int getMessageId() {
      return this.messageId;
    }

    public Uri getLargeImageUri() {
      return this.largeImageUri;
    }

    public Uri getThumbUri() {
      return this.thumbUri;
    }
  }

  private class ImageAdapter extends PagerAdapter {
    private ArrayList<io.rong.imkit.activity.PicturePagerActivity.ImageInfo> mImageList;

    private ImageAdapter() {
      this.mImageList = new ArrayList();
    }

    private View newView(Context context, io.rong.imkit.activity.PicturePagerActivity.ImageInfo imageInfo) {
      View result = LayoutInflater.from(context).inflate(R.layout.rc_fr_image, (ViewGroup)null);
      io.rong.imkit.activity.PicturePagerActivity.ImageAdapter.ViewHolder holder = new io.rong.imkit.activity.PicturePagerActivity.ImageAdapter.ViewHolder();
      holder.progressBar = (ProgressBar)result.findViewById(R.id.rc_progress);
      holder.progressText = (TextView)result.findViewById(R.id.rc_txt);
      holder.photoView = (PhotoView)result.findViewById(R.id.rc_photoView);
      holder.photoView.setOnLongClickListener(io.rong.imkit.activity.PicturePagerActivity.this);
      holder.photoView.setOnPhotoTapListener(new OnPhotoTapListener() {
        public void onPhotoTap(View view, float x, float y) {
          io.rong.imkit.activity.PicturePagerActivity.this.finish();
        }

        public void onOutsidePhotoTap() {
        }
      });
      result.setTag(holder);
      return result;
    }

    public void addData(ArrayList<io.rong.imkit.activity.PicturePagerActivity.ImageInfo> newImages, boolean direction) {
      if (newImages != null && newImages.size() != 0) {
        if (this.mImageList.size() == 0) {
          this.mImageList.addAll(newImages);
        } else if (direction && !io.rong.imkit.activity.PicturePagerActivity.this.isFirstTime && !this.isDuplicate(((io.rong.imkit.activity.PicturePagerActivity.ImageInfo)newImages.get(0)).getMessageId())) {
          ArrayList<io.rong.imkit.activity.PicturePagerActivity.ImageInfo> temp = new ArrayList();
          temp.addAll(this.mImageList);
          this.mImageList.clear();
          this.mImageList.addAll(newImages);
          this.mImageList.addAll(this.mImageList.size(), temp);
        } else if (!io.rong.imkit.activity.PicturePagerActivity.this.isFirstTime && !this.isDuplicate(((io.rong.imkit.activity.PicturePagerActivity.ImageInfo)newImages.get(0)).getMessageId())) {
          this.mImageList.addAll(this.mImageList.size(), newImages);
        }

      }
    }

    private boolean isDuplicate(int messageId) {
      Iterator var2 = this.mImageList.iterator();

      io.rong.imkit.activity.PicturePagerActivity.ImageInfo info;
      do {
        if (!var2.hasNext()) {
          return false;
        }

        info = (io.rong.imkit.activity.PicturePagerActivity.ImageInfo)var2.next();
      } while(info.getMessageId() != messageId);

      return true;
    }

    public io.rong.imkit.activity.PicturePagerActivity.ImageInfo getItem(int index) {
      return (io.rong.imkit.activity.PicturePagerActivity.ImageInfo)this.mImageList.get(index);
    }

    public int getItemPosition(Object object) {
      return -2;
    }

    public int getCount() {
      return this.mImageList.size();
    }

    public boolean isViewFromObject(View view, Object object) {
      return view == object;
    }

    public Object instantiateItem(ViewGroup container, int position) {
      RLog.i("PicturePagerActivity", "instantiateItem.position:" + position);
      View imageView = this.newView(container.getContext(), (io.rong.imkit.activity.PicturePagerActivity.ImageInfo)this.mImageList.get(position));
      this.updatePhotoView(position, imageView);
      imageView.setId(position);
      container.addView(imageView);
      return imageView;
    }

    public void destroyItem(ViewGroup container, int position, Object object) {
      RLog.i("PicturePagerActivity", "destroyItem.position:" + position);
      io.rong.imkit.activity.PicturePagerActivity.ImageAdapter.ViewHolder holder = (io.rong.imkit.activity.PicturePagerActivity.ImageAdapter.ViewHolder)container.findViewById(position).getTag();
      holder.photoView.setImageURI((Uri)null);
      container.removeView((View)object);
    }

    private void updatePhotoView(int position, View view) {
      final io.rong.imkit.activity.PicturePagerActivity.ImageAdapter.ViewHolder holder = (io.rong.imkit.activity.PicturePagerActivity.ImageAdapter.ViewHolder)view.getTag();
      Uri originalUri = ((io.rong.imkit.activity.PicturePagerActivity.ImageInfo)this.mImageList.get(position)).getLargeImageUri();
      Uri thumbUri = ((io.rong.imkit.activity.PicturePagerActivity.ImageInfo)this.mImageList.get(position)).getThumbUri();
      if (originalUri != null && thumbUri != null) {
        File file;
        if (originalUri.getScheme() == null || !originalUri.getScheme().startsWith("http") && !originalUri.getScheme().startsWith("https")) {
          file = new File(originalUri.getPath());
        } else {
          file = ImageLoader.getInstance().getDiskCache().get(originalUri.toString());
        }

        if (file != null && file.exists()) {
          AlbumBitmapCacheHelper.getInstance().addPathToShowlist(file.getAbsolutePath());
          Bitmap bitmap = AlbumBitmapCacheHelper.getInstance().getBitmap(file.getAbsolutePath(), 0, 0, new ILoadImageCallback() {
            public void onLoadImageCallBack(Bitmap bitmap, String p, Object... objects) {
              if (bitmap != null) {
                holder.photoView.setImageBitmap(bitmap);
              }
            }
          }, new Object[]{position});
          if (bitmap != null) {
            holder.photoView.setImageBitmap(bitmap);
          } else {
            Drawable drawablex = Drawable.createFromPath(thumbUri.getPath());
            holder.photoView.setImageDrawable(drawablex);
          }
        } else if (position != io.rong.imkit.activity.PicturePagerActivity.this.mCurrentIndex) {
          Drawable drawable = Drawable.createFromPath(thumbUri.getPath());
          holder.photoView.setImageDrawable(drawable);
        } else {
          ImageAware imageAware = new ImageViewAware(holder.photoView);
          if (io.rong.imkit.activity.PicturePagerActivity.this.mDownloadingImageAware != null) {
            ImageLoader.getInstance().cancelDisplayTask(io.rong.imkit.activity.PicturePagerActivity.this.mDownloadingImageAware);
          }

          ImageLoader.getInstance().displayImage(originalUri.toString(), imageAware, this.createDisplayImageOptions(thumbUri), new ImageLoadingListener() {
            public void onLoadingStarted(String imageUri, View view) {
              holder.progressText.setVisibility(View.VISIBLE);
              holder.progressBar.setVisibility(View.VISIBLE);
              holder.progressText.setText("0%");
            }

            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
              holder.progressText.setVisibility(View.GONE);
              holder.progressBar.setVisibility(View.GONE);
            }

            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
              holder.progressText.setVisibility(View.GONE);
              holder.progressBar.setVisibility(View.GONE);
            }

            public void onLoadingCancelled(String imageUri, View view) {
              holder.progressText.setVisibility(View.GONE);
              holder.progressText.setVisibility(View.GONE);
            }
          }, new ImageLoadingProgressListener() {
            public void onProgressUpdate(String imageUri, View view, int current, int total) {
              holder.progressText.setText(current * 100 / total + "%");
              if (current == total) {
                holder.progressText.setVisibility(View.GONE);
                holder.progressBar.setVisibility(View.GONE);
              } else {
                holder.progressText.setVisibility(View.VISIBLE);
                holder.progressBar.setVisibility(View.VISIBLE);
              }

            }
          });
          io.rong.imkit.activity.PicturePagerActivity.this.mDownloadingImageAware = imageAware;
        }

      } else {
        RLog.e("PicturePagerActivity", "large uri and thumbnail uri of the image should not be null.");
      }
    }

    public io.rong.imkit.activity.PicturePagerActivity.ImageInfo getImageInfo(int position) {
      return (io.rong.imkit.activity.PicturePagerActivity.ImageInfo)this.mImageList.get(position);
    }

    private DisplayImageOptions createDisplayImageOptions(Uri uri) {
      Builder builder = new Builder();
      Drawable drawable = Drawable.createFromPath(uri.getPath());
      return builder.resetViewBeforeLoading(false).cacheInMemory(false).cacheOnDisk(true).bitmapConfig(Config.RGB_565).showImageForEmptyUri(drawable).showImageOnFail(drawable).showImageOnLoading(drawable).handler(new Handler()).build();
    }

    public class ViewHolder {
      ProgressBar progressBar;
      TextView progressText;
      PhotoView photoView;

      public ViewHolder() {
      }
    }
  }
}
