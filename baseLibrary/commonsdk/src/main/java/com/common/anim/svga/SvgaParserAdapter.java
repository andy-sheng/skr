package com.common.anim.svga;

import android.text.TextUtils;

import com.common.utils.U;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

/**
 * 不重复new SvgaParser 因为每个 SvgaParser 对象中都有一个线程池
 * 因为华为手机对线程的启动有严格限制，否则就会报
 * pthread_create 的OOM
 */
public class SvgaParserAdapter {
    public static final String ROOM_TAG = "ROOM_TAG";

    static HashMap<String, SVGAParser> sSvgaParserMap = new HashMap<>();

    public static void createSvgaParser(String tag) {
        SVGAParser svgaParser = sSvgaParserMap.get(tag);
        if (svgaParser == null) {
            svgaParser = new SVGAParser(U.app());
            sSvgaParserMap.put(tag, svgaParser);

            //        mSVGAParser.setFileDownloader(new SVGAParser.FileDownloader() {
//            @Override
//            public void resume(final URL url, final Function1<? super InputStream, Unit> complete, final Function1<? super Exception, Unit> failure) {
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        OkHttpClient client = new OkHttpClient();
//                        Request request = new Request.Builder().url(url).get().build();
//                        try {
//                            Response response = client.newCall(request).execute();
//                            complete.invoke(response.body().byteStream());
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                            failure.invoke(e);
//                        }
//                    }
//                }).start();
//            }
//        });
        }
    }

    public static void destroySvgaParser(String tag) {
        sSvgaParserMap.remove(tag);
    }

    public static void parse(String tag, String urlOrAssets, SVGAParser.ParseCompletion completion) {
        if (TextUtils.isEmpty(urlOrAssets)) {
            if (completion != null) {
                completion.onError();
            }
            return;
        }
        SVGAParser svgaParser = sSvgaParserMap.get(tag);
        try {
            if (svgaParser != null) {
                if (urlOrAssets.startsWith("http")) {
                    svgaParser.decodeFromURL(new URL(urlOrAssets), completion);
                } else {
                    svgaParser.decodeFromAssets(urlOrAssets, completion);
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
