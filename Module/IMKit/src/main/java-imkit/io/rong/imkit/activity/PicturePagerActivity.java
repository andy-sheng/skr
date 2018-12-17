
package io.rong.imkit.activity;

import android.content.Context;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.common.log.MyLog;
import com.common.utils.HttpUtils;
import com.common.utils.U;
import com.imagebrowse.ImageBrowseView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;

import io.rong.common.RLog;
import io.rong.imkit.R;
import io.rong.imkit.RongBaseNoActionbarActivity;
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

public class PicturePagerActivity extends RongBaseNoActionbarActivity implements OnLongClickListener {
    private static final String TAG = "PicturePagerActivity";

    private HackyViewPager mViewPager;
    private ImageMessage mCurrentImageMessage;
    private ConversationType mConversationType;
    private int mCurrentMessageId;
    private String mTargetId = null;
    private int mCurrentIndex = 0;
    private io.rong.imkit.activity.PicturePagerActivity.ImageAdapter mImageAdapter;
    private boolean isFirstTime = false;

    private OnPageChangeListener mPageChangeListener = new OnPageChangeListener() {
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        public void onPageSelected(int position) {
            RLog.i("PicturePagerActivity", "onPageSelected. position:" + position);
            PicturePagerActivity.this.mCurrentIndex = position;
            View view = PicturePagerActivity.this.mViewPager.findViewById(position);
            if (view != null) {
                PicturePagerActivity.this.mImageAdapter.updatePhotoView(position, view);
            }

            if (position == PicturePagerActivity.this.mImageAdapter.getCount() - 1) {
                PicturePagerActivity.this.getConversationImageUris(PicturePagerActivity.this.mImageAdapter.getItem(position).getMessageId(), GetMessageDirection.BEHIND);
            } else if (position == 0) {
                PicturePagerActivity.this.getConversationImageUris(PicturePagerActivity.this.mImageAdapter.getItem(position).getMessageId(), GetMessageDirection.FRONT);
            }

        }

        public void onPageScrollStateChanged(int state) {

        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.rc_fr_photo);
        Message currentMessage = (Message) this.getIntent().getParcelableExtra("message");
        this.mCurrentImageMessage = (ImageMessage) currentMessage.getContent();
        this.mConversationType = currentMessage.getConversationType();
        this.mCurrentMessageId = currentMessage.getMessageId();
        this.mTargetId = currentMessage.getTargetId();
        this.mViewPager = (HackyViewPager) this.findViewById(R.id.viewpager);
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

                        for (int i = 0; i < messages.size(); ++i) {
                            Message message = (Message) messages.get(i);
                            if (message.getContent() instanceof ImageMessage) {
                                ImageMessage imageMessage = (ImageMessage) message.getContent();
                                Uri largeImageUri = imageMessage.getLocalUri() == null ? imageMessage.getRemoteUri() : imageMessage.getLocalUri();
                                if (imageMessage.getThumUri() != null && largeImageUri != null) {
                                    lists.add(PicturePagerActivity.this.new ImageInfo(message.getMessageId(), imageMessage.getThumUri(), largeImageUri));
                                }
                            }
                        }
                    }

                    if (direction.equals(GetMessageDirection.FRONT) && io.rong.imkit.activity.PicturePagerActivity.this.isFirstTime) {
                        lists.add(io.rong.imkit.activity.PicturePagerActivity.this.new ImageInfo(io.rong.imkit.activity.PicturePagerActivity.this.mCurrentMessageId, io.rong.imkit.activity.PicturePagerActivity.this.mCurrentImageMessage.getThumUri(), io.rong.imkit.activity.PicturePagerActivity.this.mCurrentImageMessage.getLocalUri() == null ? io.rong.imkit.activity.PicturePagerActivity.this.mCurrentImageMessage.getRemoteUri() : io.rong.imkit.activity.PicturePagerActivity.this.mCurrentImageMessage.getLocalUri()));
                        PicturePagerActivity.this.mImageAdapter.addData(lists, direction.equals(GetMessageDirection.FRONT));
                        PicturePagerActivity.this.mViewPager.setAdapter(io.rong.imkit.activity.PicturePagerActivity.this.mImageAdapter);
                        PicturePagerActivity.this.isFirstTime = false;
                        PicturePagerActivity.this.mViewPager.setCurrentItem(lists.size() - 1);
                        PicturePagerActivity.this.mCurrentIndex = lists.size() - 1;
                    } else if (lists.size() > 0) {
                        PicturePagerActivity.this.mImageAdapter.addData(lists, direction.equals(GetMessageDirection.FRONT));
                        PicturePagerActivity.this.mImageAdapter.notifyDataSetChanged();
                        if (direction.equals(GetMessageDirection.FRONT)) {
                            PicturePagerActivity.this.mViewPager.setCurrentItem(lists.size());
                            PicturePagerActivity.this.mCurrentIndex = lists.size();
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
        PicturePagerActivity.ImageInfo imageInfo = this.mImageAdapter.getImageInfo(this.mCurrentIndex);
        if (imageInfo != null) {
            Uri thumbUri = imageInfo.getThumbUri();
            Uri largeImageUri = imageInfo.getLargeImageUri();

            if (this.onPictureLongClick(v, thumbUri, largeImageUri)) {
                return true;
            }

            if (largeImageUri == null) {
                return false;
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
                        String defaultPath = PicturePagerActivity.this.getString(R.string.rc_image_default_saved_path);
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

                        downLoadImageByUri(largeImageUri, thumbUri, defaultPath);
                    }

                }
            }).show();
        }

        return true;
    }

    private void downLoadImageByUri(Uri largeImageUri, Uri thumbUri, String defaultPath) {
        if (largeImageUri != null && thumbUri != null) {
            if (largeImageUri.getScheme() == null || !largeImageUri.getScheme().startsWith("http") && !largeImageUri.getScheme().startsWith("https")) {
                // todo 后续是否要处理本地路径
                U.getToastUtil().showShort("本地路径");
            } else {
                Observable.create(new ObservableOnSubscribe<Object>() {
                    @Override
                    public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                        U.getHttpUtils().downloadFileSync(largeImageUri.toString(), getImageSaveFile(largeImageUri.toString() ,defaultPath), new HttpUtils.OnDownloadProgress() {
                            @Override
                            public void onDownloaded(long downloaded, long totalLength) {
                                MyLog.d(TAG, "onDownloaded" + " downloaded=" + downloaded + " totalLength=" + totalLength);
                            }

                            @Override
                            public void onCompleted(String localPath) {
                                MyLog.d(TAG, "onCompleted" + " localPath=" + localPath);
                                File file1 = new File(localPath);
                                File file2 = getImageSaveFile(largeImageUri.toString(), defaultPath);
                                file1.renameTo(file2);
                                U.getToastUtil().showShort("onCompleted");
                            }

                            @Override
                            public void onCanceled() {
                                MyLog.d(TAG, "onCanceled");
                                U.getToastUtil().showShort("onCanceled");
                            }

                            @Override
                            public void onFailed() {
                                MyLog.d(TAG, "onFailed");
                                U.getToastUtil().showShort("onFailed");
                            }
                        });
                        emitter.onComplete();
                    }
                }).subscribeOn(Schedulers.io())
                        .subscribe();
            }
        } else {
            RLog.e("PicturePagerActivity", "large uri and thumbnail uri of the image should not be null.");
        }
    }

    private File getImageSaveFile(String url, String defaultPath) {
        String fileName = U.getMD5Utils().MD5_16(url) + ".jpg";
        return new File(U.getAppInfoUtils().getMainDir(), defaultPath + fileName);
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

        private ArrayList<PicturePagerActivity.ImageInfo> mImageList;

        private ImageAdapter() {
            this.mImageList = new ArrayList();
        }

        private View newView(Context context, PicturePagerActivity.ImageInfo imageInfo) {
            View result = LayoutInflater.from(context).inflate(R.layout.rc_fr_image, (ViewGroup) null);
            PicturePagerActivity.ImageAdapter.ViewHolder holder = new PicturePagerActivity.ImageAdapter.ViewHolder();
            holder.progressBar = (ProgressBar) result.findViewById(R.id.rc_progress);
            holder.progressText = (TextView) result.findViewById(R.id.rc_txt);
            ImageBrowseView imageBrowseView = new ImageBrowseView(result.getContext());
            holder.photoView = (ImageBrowseView) result.findViewById(R.id.rc_photoView);
            holder.photoView.setViewLongClickListener(PicturePagerActivity.this);
            result.setTag(holder);
            return result;
        }

        public void addData(ArrayList<io.rong.imkit.activity.PicturePagerActivity.ImageInfo> newImages, boolean direction) {
            if (newImages != null && newImages.size() != 0) {
                if (this.mImageList.size() == 0) {
                    this.mImageList.addAll(newImages);
                } else if (direction && !PicturePagerActivity.this.isFirstTime && !this.isDuplicate(newImages.get(0).getMessageId())) {
                    ArrayList<io.rong.imkit.activity.PicturePagerActivity.ImageInfo> temp = new ArrayList();
                    temp.addAll(this.mImageList);
                    this.mImageList.clear();
                    this.mImageList.addAll(newImages);
                    this.mImageList.addAll(this.mImageList.size(), temp);
                } else if (!io.rong.imkit.activity.PicturePagerActivity.this.isFirstTime && !this.isDuplicate(newImages.get(0).getMessageId())) {
                    this.mImageList.addAll(this.mImageList.size(), newImages);
                }

            }
        }

        private boolean isDuplicate(int messageId) {
            Iterator var2 = this.mImageList.iterator();

            PicturePagerActivity.ImageInfo info;
            do {
                if (!var2.hasNext()) {
                    return false;
                }

                info = (PicturePagerActivity.ImageInfo) var2.next();
            } while (info.getMessageId() != messageId);

            return true;
        }

        public ImageInfo getItem(int index) {
            return this.mImageList.get(index);
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
            View imageView = this.newView(container.getContext(), this.mImageList.get(position));
            this.updatePhotoView(position, imageView);
            imageView.setId(position);
            container.addView(imageView);
            return imageView;
        }

        public void destroyItem(ViewGroup container, int position, Object object) {
            RLog.i("PicturePagerActivity", "destroyItem.position:" + position);
            PicturePagerActivity.ImageAdapter.ViewHolder holder = (PicturePagerActivity.ImageAdapter.ViewHolder) container.findViewById(position).getTag();
            container.removeView((View) object);
        }

        private void updatePhotoView(int position, View view) {
            final PicturePagerActivity.ImageAdapter.ViewHolder holder = (PicturePagerActivity.ImageAdapter.ViewHolder) view.getTag();
            Uri originalUri = this.mImageList.get(position).getLargeImageUri();
            Uri thumbUri = this.mImageList.get(position).getThumbUri();
            if (originalUri != null && thumbUri != null) {
//                holder.photoView.load(thumbUri.toString());
                if (originalUri.getScheme() == null || !originalUri.getScheme().startsWith("http") && !originalUri.getScheme().startsWith("https")) {
                    holder.photoView.load(originalUri.getPath());
                } else {
                    holder.photoView.load(originalUri.toString());
                }
            } else {
                RLog.e("PicturePagerActivity", "large uri and thumbnail uri of the image should not be null.");
            }
        }

        public PicturePagerActivity.ImageInfo getImageInfo(int position) {
            return this.mImageList.get(position);
        }

        public class ViewHolder {
            ProgressBar progressBar;
            TextView progressText;
            ImageBrowseView photoView;

            public ViewHolder() {
            }
        }
    }

}
