package com.imagebrowse;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.Nullable;
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
import pl.droidsonroids.gif.GifImageView;

/**
 * 这个view过于复杂，在使用viewpager时就别重复使用了吧
 * 职责：保证清晰流畅的显示出图片 不管多大
 */
public class EnhancedImageView extends RelativeLayout {

    public final static String TAG = "ImageBrowseView";

    protected BaseImageView mPhotoDraweeView;
    protected GifImageView mGifImageView;
    protected GifDrawable mGifFromFile;
    protected SubsamplingScaleImageView mSubsamplingScaleImageView;

    protected OnLongClickListener mLongClickListener; //长按事件的监听
    protected OnClickListener mClickListener; //点击事件的监听

    protected Handler mUiHandler = new Handler();

    protected BaseImage mBaseImage;

    public EnhancedImageView(Context context) {
        super(context);
        init();
    }

    public EnhancedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EnhancedImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        this.mClickListener = l;
    }

    @Override
    public void setOnLongClickListener(@Nullable OnLongClickListener l) {
        this.mLongClickListener = l;
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
//        path = "http://bucket-oss-inframe.oss-cn-beijing.aliyuncs.com/1111.jpg?x-oss-process=image/resize,w_480,h_1080/circle,r_500/blur,r_30,s_20";
        MyLog.d(TAG, "load" + " path=" + path);
//        TextView textView = new TextView(getContext());
//        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300);
//        lp.topMargin=400;
//        textView.setTextColor(Color.RED);
//        textView.setGravity(CENTER_IN_PARENT);
//        textView.setText("path=" + path);
//        addView(textView, lp);

        if (path.startsWith("http://") || path.startsWith("https://")) {
            HttpImage httpImage = (HttpImage) ImageFactory.newPathImage(path)
                    .setFailureDrawable(U.app().getResources().getDrawable(R.drawable.load_img_error))
                    .setLoadingDrawable(U.app().getResources().getDrawable(R.drawable.loading_place_holder_img))
                    .setProgressBarDrawable(new ImageBrowseProgressBar())
                    .setTapToRetryEnabled(true)
//                    .setOssProcessors(OssPsFactory.newResizeBuilder().setW(360).build(),OssPsFactory.newCropBuilder().setH(180).build())
                    .build();
            load(httpImage);
        } else {
            LocalImage localImage = (LocalImage) ImageFactory.newPathImage(path)
                    .build();
            load(localImage);
        }
    }

    public void load(BaseImage baseImage) {
        if (baseImage == null || baseImage.getUri() == null) {
            return;
        }

        String path = baseImage.getUri().toString();
        if (path.startsWith("http://") || path.startsWith("https://")) {
            Uri uri = Uri.parse(path);
            if (uri.getPath().endsWith(".gif")) {
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
            mPhotoDraweeView.setOnLongClickListener(this.mLongClickListener);
            mPhotoDraweeView.setOnClickListener(this.mClickListener);
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
            mGifImageView.setOnLongClickListener(this.mLongClickListener);
            mGifImageView.setOnClickListener(this.mClickListener);
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
            mSubsamplingScaleImageView.setOnLongClickListener(this.mLongClickListener);
            mSubsamplingScaleImageView.setOnClickListener(this.mClickListener);
        }
        mSubsamplingScaleImageView.setVisibility(VISIBLE);
    }

    private void loadHttpByFresco(BaseImage httpImage) {
        showFrescoViewIfNeed();
        IFrescoCallBack preCallback = httpImage.getCallBack();
        httpImage.setCallBack(new IFrescoCallBack() {
            @Override
            public void processWithInfo(ImageInfo info, Animatable animatable) {
                if (preCallback != null) {
                    preCallback.processWithInfo(info, animatable);
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
                //MyLog.d(TAG, "onProgressUpdate" + " progress=" + progress);
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
            public void processWithInfo(ImageInfo info, Animatable animatable) {
                if (preCallback != null) {
                    preCallback.processWithInfo(info, animatable);
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

    protected void realLoadByFresco(BaseImage baseImage) {
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
//        MyLog.d(TAG,"loadBySubSampleView degree=" + U.getBitmapUtils().getBitmapDegree(localFilePath)+ " localFilePath:"+localFilePath);
        mSubsamplingScaleImageView.setOrientation(U.getBitmapUtils().getBitmapDegree(localFilePath));
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
                U.getHttpUtils().downloadFileSync(url, getGifSaveFile(url, true), new HttpUtils.OnDownloadProgress() {
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
            if (!mGifFromFile.isRecycled()) {
                mGifImageView.setImageDrawable(mGifFromFile);
            }
        } catch (IOException e) {
            // 失败了
            loadLocalByFresco(mBaseImage);
        }
    }
}
