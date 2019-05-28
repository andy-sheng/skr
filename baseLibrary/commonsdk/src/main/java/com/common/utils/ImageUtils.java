package com.common.utils;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Looper;

import com.common.log.MyLog;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ImageUtils {

    public static final String IMG_URL_POSTFIX = "@style@"; //img  url 的后缀,如果有后缀,不再去拼接@style

    ImageUtils() {
    }

    public interface GetWidthHeightFromNetworkCallback {
        void onResult(int w, int h);
    }

    /**
     * 运营位显示图片时先去网络获取图片的宽高信息
     *
     * @param url 图片地址
     * @return 宽，高
     */
    public void getImageWidthAndHeightFromNetwrok(String url, GetWidthHeightFromNetworkCallback callback) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            int wh[] = getImageWidthAndHeightFromNetwrokInner(url);
            callback.onResult(wh[0], wh[1]);
        } else {
            Observable.create(new ObservableOnSubscribe<Object>() {
                @Override
                public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                    int wh[] = getImageWidthAndHeightFromNetwrokInner(url);
                    emitter.onNext(wh);
                    emitter.onComplete();
                }
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Object>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(Object o) {
                            int[] wh = (int[]) o;
                            callback.onResult(wh[0], wh[1]);
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        }
    }

    private int[] getImageWidthAndHeightFromNetwrokInner(String url) {
        int option[] = new int[2];
        try {
            URL m_url = new URL(url);
            HttpURLConnection con = (HttpURLConnection) m_url.openConnection();
            InputStream in = con.getInputStream();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, options);

            int height = options.outHeight;
            int width = options.outWidth;
            option[0] = width;
            option[1] = height;
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return option;
    }

    /**
     * 运营位显示图片时先去网络获取图片的宽高信息
     *
     * @return 宽，高
     */
    public int[] getImageWidthAndHeightFromFile(String path) {
        int option[] = new int[2];
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;//这个参数设置为true才有效，
        Bitmap bmp = BitmapFactory.decodeFile(path, options);//这里的bitmap是个空
        if (bmp == null) {
            MyLog.e("通过options获取到的bitmap为空", "===");
        }
        option[0] = options.outHeight;
        option[1] = options.outWidth;
        return option;
    }

    /**
     * 根据本地路径得到图片的真实类型
     * 不耗时
     *
     * @param filePath
     * @return
     */
    public TYPE getImageType(String filePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        String mimeType = options.outMimeType;
        if (mimeType == null) {
            return TYPE.UNKNOW;
        } else if (mimeType.equals("image/gif")) {
            return TYPE.GIF;
        } else if (mimeType.equals("image/png")) {
            return TYPE.PNG;
        } else if (mimeType.equals("image/jpeg")) {
            return TYPE.JPG;
        } else if (mimeType.equals("image/jpg")) {
            return TYPE.JPG;
        } else if (mimeType.equals("image/webp")) {
            return TYPE.WEBP;
        } else {
            return TYPE.UNKNOW;
        }
    }

    public enum TYPE {
        JPG, GIF, PNG, WEBP, UNKNOW
    }

    public enum SIZE {
        ORIGIN(1080), SIZE_160(160), SIZE_320(320), SIZE_480(480), SIZE_640(640);

        int w;

        SIZE(int w) {
            this.w = w;
        }

        public int getW() {
            return w;
        }

    }

    /**
     * 通知系统相册有变化，触发相册表刷新
     *
     * @param file
     */
    public void notifyGalleryChangeByBroadcast(File file) {
        if (file != null) {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(file);
            mediaScanIntent.setData(contentUri);
            U.app().sendBroadcast(mediaScanIntent);
        }
    }
}
