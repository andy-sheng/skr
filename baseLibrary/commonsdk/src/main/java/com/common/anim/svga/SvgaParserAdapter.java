package com.common.anim.svga;

import android.text.TextUtils;

import com.common.log.MyLog;
import com.common.utils.U;
import com.opensource.svgaplayer.SVGAParser;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 不重复new SvgaParser 因为每个 SvgaParser 对象中都有一个线程池
 * 因为华为手机对线程的启动有严格限制，否则就会报
 * pthread_create 的OOM
 */
public class SvgaParserAdapter {

    static SVGAParser sSvgaParser = new SVGAParser(U.app());

    static {
        /**
         * 检查一下下载为什么不命中缓存
         */
        sSvgaParser.setFileDownloader(new SVGAParser.FileDownloader() {
            @NotNull
            @Override
            public Function0<Unit> resume(@NotNull URL url, @NotNull Function1<? super InputStream, Unit> complete, @NotNull Function1<? super Exception, Unit> failure) {
                Observable.create(new ObservableOnSubscribe<Object>() {
                    @Override
                    public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                        OkHttpClient client = new OkHttpClient();
                        Request request = new Request.Builder().url(url).get().build();
                        try {
                            Response response = client.newCall(request).execute();
                            complete.invoke(response.body().byteStream());
                        } catch (IOException e) {
                            e.printStackTrace();
                            failure.invoke(e);
                        }
                        emitter.onComplete();
                    }
                }).subscribeOn(Schedulers.io())
                        .subscribe(new Consumer<Object>() {
                            @Override
                            public void accept(Object o) throws Exception {

                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                MyLog.d("SvgaParserAdapter", "accept" + " throwable=" + throwable);


                            }
                        });
                return null;
            }
        });
    }

    public static void parse(String urlOrAssets, SVGAParser.ParseCompletion completion) {
        if (TextUtils.isEmpty(urlOrAssets)) {
            if (completion != null) {
                completion.onError();
            }
            return;
        }
        try {
            if (sSvgaParser != null) {
                if (urlOrAssets.startsWith("http")) {
                    sSvgaParser.decodeFromURL(new URL(urlOrAssets), completion);
                } else {
                    sSvgaParser.decodeFromAssets(urlOrAssets, completion);
                }
            } else {
                if (completion != null) {
                    completion.onError();
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

}
