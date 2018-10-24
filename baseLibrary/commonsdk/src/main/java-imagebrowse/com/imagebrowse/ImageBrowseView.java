package com.imagebrowse;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.common.base.R;
import com.common.image.fresco.FrescoWorker;
import com.common.image.fresco.IFrescoCallBack;
import com.common.image.model.BaseImage;
import com.common.image.model.HttpImage;
import com.common.image.model.ImageFactory;
import com.common.image.model.LocalImage;
import com.common.log.MyLog;
import com.common.utils.DisplayUtils;
import com.common.utils.HttpUtils;
import com.common.utils.ImageUtils;
import com.common.utils.U;
import com.common.view.photodraweeview.PhotoDraweeView;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.facebook.imagepipeline.image.ImageInfo;

import java.io.File;
import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * 这个view过于复杂，在使用viewpager时就别重复使用了吧
 * 职责：保证清晰流畅的显示出图片 不管多大
 */
public class ImageBrowseView extends RelativeLayout {

    public final static String TAG = "ImageBrowseView";

    PhotoDraweeView mPhotoDraweeView;
    GifImageView mGifImageView;
    SubsamplingScaleImageView mSubsamplingScaleImageView;

    Handler mUiHandler = new Handler();

    String mPath;

    public ImageBrowseView(Context context) {
        super(context);
        init();
    }

    public ImageBrowseView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ImageBrowseView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mUiHandler.removeCallbacksAndMessages(null);
        if (mSubsamplingScaleImageView != null) {
            mSubsamplingScaleImageView.recycle();
        }
        if (mPath.startsWith("https://") || mPath.startsWith("http://")) {
            if (mPath.endsWith(".gif")) {
                U.getHttpUtils().cancelDownload(mPath);
            }
        }
    }

    private void init() {

    }

    private void showFrescoViewIfNeed() {
        if (mGifImageView != null) {
            mGifImageView.setVisibility(GONE);
        }
        if (mPhotoDraweeView == null) {
            mPhotoDraweeView = new PhotoDraweeView(getContext());
            RelativeLayout.LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            addView(mPhotoDraweeView, 0, lp);
        }
        mPhotoDraweeView.setVisibility(VISIBLE);
    }

    private void showGifViewIfNeed() {
        if (mPhotoDraweeView != null) {
            mPhotoDraweeView.setVisibility(GONE);
        }
        if (mGifImageView == null) {
            mGifImageView = new GifImageView(getContext());
            RelativeLayout.LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            addView(mGifImageView, 0, lp);
        }
        mGifImageView.setVisibility(VISIBLE);
    }

    private void showSubSampleViewIfNeed() {
        if (mGifImageView != null) {
            mGifImageView.setVisibility(GONE);
        }
        if (mSubsamplingScaleImageView == null) {
            mSubsamplingScaleImageView = new SubsamplingScaleImageView(getContext());
            RelativeLayout.LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            addView(mSubsamplingScaleImageView, 0, lp);
        }
        mSubsamplingScaleImageView.setVisibility(VISIBLE);
    }

    public void load(String path) {
        this.mPath = path;
        MyLog.d(TAG, "load" + " path=" + path);
        if (path.startsWith("http://") || path.startsWith("https://")) {
            if (path.endsWith(".gif")) {
                // gif直接走自有逻辑
                downloadGiftByHttpUtils();
            } else {
                //其余情况，先用fresco渐变加载，保证体验
                loadHttpByFresco();
            }
        } else {
            //本地
            String fileType = U.getFileUtils().getImageFileType(path);
            if (!TextUtils.isEmpty(fileType) && fileType.equals("gif")) {
                loadByGif(mPath);
            } else {
                loadLocalByFresco(mPath);
            }
        }
    }

    private void loadHttpByFresco() {
        showFrescoViewIfNeed();
        HttpImage httpImage = (HttpImage) ImageFactory.newHttpImage(mPath)
                .setFailureDrawable(U.app().getResources().getDrawable(R.drawable.load_img_error))
                .setLoadingDrawable(U.app().getResources().getDrawable(R.drawable.loading_place_holder_img))
                .setProgressBarDrawable(new ImageBrowseProgressBar())
                .setTapToRetryEnabled(true)
//                .setLowImageUri()
                .setCallBack(new IFrescoCallBack() {
                    @Override
                    public void processWithInfo(ImageInfo info) {
                        // 加载完成
                        File file = FrescoWorker.getCacheFileFromFrescoDiskCache(mPath);
                        int wh[] = U.getImageUtils().getImageWidthAndHeightFromFile(file.getAbsolutePath());

                        MyLog.d(TAG, "load processWithInfo wh " + wh[0] + " " + wh[1]);
                        // 如果是图特别大，用 subsample加载
                        boolean b1 = wh[0] != 0 && wh[0] > U.getDisplayUtils().getScreenWidth() * 1.5;
                        boolean b2 = wh[1] != 0 && wh[1] > U.getDisplayUtils().getScreenHeight() * 1.5;
                        if (b1 || b2) {
                            loadBySubSampleView(file.getAbsolutePath());
                        }
                    }

                    @Override
                    public void processWithFailure() {

                    }
                })
                .build();
        FrescoWorker.preLoadImg(httpImage, new FrescoWorker.ImageLoadCallBack() {
            @Override
            public void loadSuccess(Bitmap bitmap) {
                MyLog.d(TAG, "loadSuccess" + " bitmap=" + bitmap);
            }

            @Override
            public void onProgressUpdate(float progress) {
                //显示下载进度条
                MyLog.d(TAG, "onProgressUpdate" + " progress=" + progress);
            }

            @Override
            public void loadFail() {
                MyLog.d(TAG, "loadFail");
            }
        }, true);
        FrescoWorker.loadImage(mPhotoDraweeView, httpImage);
    }

    private void loadLocalByFresco(String localPath) {
        showFrescoViewIfNeed();
        LocalImage localImage = (LocalImage) ImageFactory.newLocalImage(localPath)
                .setCallBack(new IFrescoCallBack() {
                    @Override
                    public void processWithInfo(ImageInfo info) {
                        // 加载完成
                        File file = new File(localPath);
                        if (file != null && file.exists()) {
                            int wh[] = U.getImageUtils().getImageWidthAndHeightFromFile(file.getAbsolutePath());

                            MyLog.d(TAG, "load processWithInfo wh " + wh[0] + " " + wh[1]);
                            // 如果是图特别大，用 subsample加载
                            boolean b1 = wh[0] != 0 && wh[0] > U.getDisplayUtils().getScreenWidth() * 1.5;
                            boolean b2 = wh[1] != 0 && wh[1] > U.getDisplayUtils().getScreenHeight() * 1.5;
                            if (b1 || b2) {
                                loadBySubSampleView(file.getAbsolutePath());
                            }
                        }
                    }

                    @Override
                    public void processWithFailure() {

                    }
                })
                .build();
        FrescoWorker.loadImage(mPhotoDraweeView, localImage);
    }

    private void loadBySubSampleView(String localFilePath) {
        showSubSampleViewIfNeed();
        mSubsamplingScaleImageView.setOnImageEventListener(new SubsamplingScaleImageView.DefaultOnImageEventListener() {
            @Override
            public void onPreviewLoadError(Exception e) {
                // 改用fresco
                MyLog.d(TAG, " subSampleTouchView onPreviewLoadError");
            }

            @Override
            public void onImageLoadError(Exception e) {
                MyLog.d(TAG, " subSampleTouchView onPreviewLoadError");
            }

            @Override
            public void onTileLoadError(Exception e) {
                MyLog.d(TAG, " subSampleTouchView onPreviewLoadError");
            }

            @Override
            public void onReady() {

            }

            @Override
            public void onImageLoaded() {
                MyLog.d(TAG, " subSampleTouchView onImageLoaded");
                if (mPhotoDraweeView != null) {
                    mPhotoDraweeView.setVisibility(GONE);
                }
            }
        });
        //竖长图
        mSubsamplingScaleImageView.setImage(ImageSource.uri(localFilePath));
    }

    private File getGifSaveFile(boolean temp) {
        String fileName = U.getMD5Utils().MD5_16(mPath) + ".gif";
        if (temp) {
            fileName += ".temp";
        }
        return new File(U.getAppInfoUtils().getMainDir(), "gif/" + fileName);
    }

    //下载gif
    private void downloadGiftByHttpUtils() {
        MyLog.d(TAG, "downloadGiftByHttpUtils");
        File file = getGifSaveFile(false);
        if (file.exists()) {
            //已经有了，不需要下载
            loadByGif(file.getAbsolutePath());
            return;
        }
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                U.getHttpUtils().downloadFile(mPath, getGifSaveFile(true), new HttpUtils.OnDownloadProgress() {
                    @Override
                    public void onDownloaded(long downloaded, long totalLength) {
                        MyLog.d(TAG, "onDownloaded" + " downloaded=" + downloaded + " totalLength=" + totalLength);
                    }

                    @Override
                    public void onCompleted(String localPath) {
                        MyLog.d(TAG, "onCompleted" + " localPath=" + localPath);
                        File file1 = new File(localPath);
                        File file2 = getGifSaveFile(false);
                        file1.renameTo(file2);
                        mUiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                loadByGif(file2.getAbsolutePath());
                            }
                        });
                    }

                    @Override
                    public void onCanceled() {
                        MyLog.d(TAG, "onCanceled");
                        mUiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                loadHttpByFresco();
                            }
                        });
                    }

                    @Override
                    public void onFailed() {
                        MyLog.d(TAG, "onFailed");
                        mUiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                loadHttpByFresco();
                            }
                        });
                    }
                });
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .subscribe();

    }

    private void loadByGif(String localFilePath) {
        MyLog.d(TAG, "loadByGif" + " localFile=" + localFilePath);
        // 如果是 gif ,直接用android-gif-drawable 加载,不废话了
        showGifViewIfNeed();
        GifDrawable gifFromFile = null;
        try {
            gifFromFile = new GifDrawable(new File(localFilePath));
            mGifImageView.setImageDrawable(gifFromFile);
        } catch (IOException e) {
            // 失败了
            loadLocalByFresco(localFilePath);
        }
    }
}
