package com.imagebrowse;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.common.base.R;
import com.common.image.fresco.BaseImageView;
import com.common.image.fresco.FrescoWorker;
import com.common.image.fresco.IFrescoCallBack;
import com.common.image.model.BaseImage;
import com.common.image.model.HttpImage;
import com.common.image.model.ImageFactory;
import com.common.image.model.LocalImage;
import com.common.log.MyLog;
import com.common.utils.HttpUtils;
import com.common.utils.U;
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
import pl.droidsonroids.gif.GifDrawableBuilder;
import pl.droidsonroids.gif.GifImageView;
import pl.droidsonroids.gif.GifOptions;

/**
 * 这个view过于复杂，在使用viewpager时就别重复使用了吧
 * 职责：保证清晰流畅的显示出图片 不管多大
 */
public class ExImageView extends RelativeLayout {

    public final static String TAG = "ImageBrowseView";

    protected BaseImageView mPhotoDraweeView;
    protected GifImageView mGifImageView;
    protected GifDrawable mGifFromFile;
    protected SubsamplingScaleImageView mSubsamplingScaleImageView;

    protected Handler mUiHandler = new Handler();

    protected BaseImage mBaseImage;

    public ExImageView(Context context) {
        super(context);
        init();
    }

    public ExImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ExImageView(Context context, AttributeSet attrs, int defStyleAttr) {
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
        if (mBaseImage != null) {
            String path = mBaseImage.getUri().toString();
            if (path.startsWith("https://") || path.startsWith("http://")) {
                if (path.endsWith(".gif")) {
                    U.getHttpUtils().cancelDownload(path);
                }
            }
        }
        if (mGifFromFile != null) {
            mGifFromFile.recycle();
        }
    }

    private void init() {

    }

    protected boolean useSubSampleView() {
        return false;
    }

    public void load(String path) {
        MyLog.d(TAG, "load" + " path=" + path);
        if (path.startsWith("http://") || path.startsWith("https://")) {
            HttpImage httpImage = (HttpImage) ImageFactory.newHttpImage(path)
                    .setFailureDrawable(U.app().getResources().getDrawable(R.drawable.load_img_error))
                    .setLoadingDrawable(U.app().getResources().getDrawable(R.drawable.loading_place_holder_img))
                    .setProgressBarDrawable(new ImageBrowseProgressBar())
                    .setTapToRetryEnabled(true)
                    .build();
            load(httpImage);
        } else {
            LocalImage localImage = (LocalImage) ImageFactory.newLocalImage(path)
                    .build();
            load(localImage);
        }
    }

    public void load(BaseImage baseImage) {
        String path = baseImage.getUri().toString();
        if (path.startsWith("http://") || path.startsWith("https://")) {
            if (path.endsWith(".gif")) {
                // gif直接走自有逻辑
                downloadGiftByHttpUtils(path);
            } else {
                //其余情况，先用fresco渐变加载，保证体验
                loadHttpByFresco(baseImage);
            }
        } else {
            //本地
            path = baseImage.getUri().getPath();
            String fileType = U.getFileUtils().getImageFileType(path);
            if (!TextUtils.isEmpty(fileType) && fileType.equals("gif")) {
                loadByGif(path);
            } else {
                loadLocalByFresco(baseImage);
            }
        }
    }

    protected void createFrescoView() {
        mPhotoDraweeView = new BaseImageView(getContext());
    }

    private void showFrescoViewIfNeed() {
        if (mGifImageView != null) {
            mGifImageView.setVisibility(GONE);
        }
        if (mPhotoDraweeView == null) {
            createFrescoView();
            LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
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
            LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
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
            LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            addView(mSubsamplingScaleImageView, 0, lp);
        }
        mSubsamplingScaleImageView.setVisibility(VISIBLE);
    }

    private void loadHttpByFresco(BaseImage httpImage) {
        showFrescoViewIfNeed();
        IFrescoCallBack preCallback = httpImage.getCallBack();
        httpImage.setCallBack(new IFrescoCallBack() {
            @Override
            public void processWithInfo(ImageInfo info) {
                if (preCallback != null) {
                    preCallback.processWithInfo(info);
                }
                if (!useSubSampleView()) {
                    return;
                }
                // 加载完成
                File file = FrescoWorker.getCacheFileFromFrescoDiskCache(httpImage.getUri());
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
                if (preCallback != null) {
                    preCallback.processWithFailure();
                }
            }
        });
        FrescoWorker.preLoadImg((HttpImage) httpImage, new FrescoWorker.ImageLoadCallBack() {
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
        realLoadByFresco(httpImage);
    }

    private void loadLocalByFresco(BaseImage localImage) {
        showFrescoViewIfNeed();
        IFrescoCallBack preCallback = localImage.getCallBack();

        localImage.setCallBack(new IFrescoCallBack() {
            @Override
            public void processWithInfo(ImageInfo info) {
                if (preCallback != null) {
                    preCallback.processWithInfo(info);
                }
                if (!useSubSampleView()) {
                    return;
                }
                // 加载完成
                File file = new File(localImage.getUri().getPath());
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
                if (preCallback != null) {
                    preCallback.processWithFailure();
                }
            }
        });
        realLoadByFresco(localImage);
    }

    protected void realLoadByFresco(BaseImage baseImage){
        FrescoWorker.loadImage(mPhotoDraweeView, baseImage);
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

    private File getGifSaveFile(String url, boolean temp) {
        String fileName = U.getMD5Utils().MD5_16(url) + ".gif";
        if (temp) {
            fileName += ".temp";
        }
        return new File(U.getAppInfoUtils().getMainDir(), "gif/" + fileName);
    }

    //下载gif
    private void downloadGiftByHttpUtils(String url) {
        MyLog.d(TAG, "downloadGiftByHttpUtils");
        File file = getGifSaveFile(url, false);
        if (file.exists()) {
            //已经有了，不需要下载
            loadByGif(file.getAbsolutePath());
            return;
        }
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                U.getHttpUtils().downloadFile(url, getGifSaveFile(url, true), new HttpUtils.OnDownloadProgress() {
                    @Override
                    public void onDownloaded(long downloaded, long totalLength) {
                        MyLog.d(TAG, "onDownloaded" + " downloaded=" + downloaded + " totalLength=" + totalLength);
                    }

                    @Override
                    public void onCompleted(String localPath) {
                        MyLog.d(TAG, "onCompleted" + " localPath=" + localPath);
                        File file1 = new File(localPath);
                        File file2 = getGifSaveFile(url, false);
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
                                loadHttpByFresco(mBaseImage);
                            }
                        });
                    }

                    @Override
                    public void onFailed() {
                        MyLog.d(TAG, "onFailed");
                        mUiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                loadHttpByFresco(mBaseImage);
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
        if (mGifFromFile != null) {
            mGifFromFile.recycle();
        }
        try {
            mGifFromFile = new GifDrawable(localFilePath);
            mGifImageView.setImageDrawable(mGifFromFile);
        } catch (IOException e) {
            // 失败了
            loadLocalByFresco(mBaseImage);
        }
    }
}
