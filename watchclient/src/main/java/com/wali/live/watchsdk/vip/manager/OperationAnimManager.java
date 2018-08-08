package com.wali.live.watchsdk.vip.manager;

import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.IOUtils;
import com.base.utils.network.Network;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.constant.MiLinkConstant;
import com.mi.milink.sdk.aidl.PacketData;
import com.mi.milink.sdk.debug.MiLinkMonitor;
import com.wali.live.network.ImageUrlDNSManager;
import com.wali.live.proto.OperationActivityProto;
import com.wali.live.watchsdk.vip.model.OperationAnimRes;
import com.wali.live.watchsdk.vip.model.OperationAnimation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import rx.Subscriber;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import static com.mi.live.data.milink.command.MiLinkCommand.COMMAND_OPERATION_ANIM_RESOURCE;

public class OperationAnimManager {

    public static String TAG = "OperationAnimManager";
    private static boolean isLoading = false;
    private static String OPERATION_ZIP_PATH = "operate_anim_res_";
    private static String JSON_FILE_NAME = "content.json";//资源包里的json文件名称，这个本来应该server提供，这里先写死了
    private static Handler mainHandler = new Handler(Looper.getMainLooper());
    //资源已经存在的特效
    private static ConcurrentMap<Integer, OperationAnimation> mExistedAnim = new ConcurrentHashMap<>();  //这个是入场动画资源信息,在app启动的时候就下载完成了,但没有下载资源

    //记录正在下载的特效
    private static HashSet<Integer> mDownloadingGiftSet = new HashSet<Integer>();

    /**
     * 专门用于下载特效动画资源
     */
    private static PublishSubject<OperationAnimation> mDownloadAnimationRes = PublishSubject.create();
    private static Runnable resetLoadingRunnable = new Runnable() {
        @Override
        public void run() {
            isLoading = false;
        }
    };

    static {
        mDownloadAnimationRes
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Subscriber<OperationAnimation>() {
                    @Override
                    public void onCompleted() {
                        MyLog.w(TAG, "onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.w(TAG, "download onError msg:" + e);
                    }

                    @Override
                    public void onNext(OperationAnimation animation) {
                        MyLog.d(TAG, "Thread name:" + Thread.currentThread() + " animation=" + animation.toString());

                        String jsonFilePath = getJsonPathIfResExist(generateFile(animation.getAnimationId(), animation.getAnimResUrl()), JSON_FILE_NAME);
                        if (TextUtils.isEmpty(jsonFilePath)) {
                            // 路径还未存在，说明下载-》解压这一步还未完成，如果路径存在，但是文件不存在，说明文件被删除了，重新下载--》解压。
                            MyLog.d(TAG, "animation url:" + animation.getAnimResUrl() + " resource not exist, post to download queue");

                            // 下载成功后，填充信息
                            downloadAndUnzip(animation);
                        } else {
                            MyLog.d(TAG, "animation id:" + animation.getAnimationId() + " resource exist,go on! json path is :" + jsonFilePath);
                            if (!TextUtils.isEmpty(jsonFilePath)) {
                                animation.completeAnimInfo(jsonFilePath);
                                if (animation.getTopWebpPath() != null && animation.getTopWebpPath().endsWith(".webp")) {
                                    addResToExistedSet(animation);
                                }
                            }

                        }

                    }
                });
    }

    /**
     * 从服务器拉取礼物列表
     *
     * @param needDownloadRes   app刚刚启动只需要加载资源包信息,而不需要加载,真正的资源文件
     */
    public static void pullResListFromServer(boolean needDownloadRes) {
        if (isLoading) {
            MyLog.i(TAG, "pull operation anim Res From Server isLoading already,cancel this");
            return;
        }
        isLoading = true;
        mainHandler.postDelayed(resetLoadingRunnable, 0);
        OperationActivityProto.GetEffectResourcesC2sReq req = OperationActivityProto.GetEffectResourcesC2sReq
                .newBuilder().setUuid(UserAccountManager.getInstance().getUuidAsLong()).build();

        PacketData data = new PacketData();
        data.setCommand(COMMAND_OPERATION_ANIM_RESOURCE);
        data.setData(req.toByteArray());
        MyLog.d(TAG, "pull Operation Anim Res request : " + req.toString());
        PacketData resp = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
        if (resp != null) {
            try {
                OperationActivityProto.GetEffectResourcesC2sRsp response = OperationActivityProto.GetEffectResourcesC2sRsp.parseFrom(resp.getData());
                MyLog.d(TAG, "Get operation anim List Rsp:" + response);
                processData(response, needDownloadRes);
            } catch (InvalidProtocolBufferException e) {
                MyLog.d(TAG, "pull InvalidProtocolBuffer :" + e);
            }
        } else {
            MyLog.d(TAG, "pull pullResListFromServer :null");
        }
    }

    public static OperationAnimation getExistedAnimRes(int animId) {
        MyLog.d(TAG, "get Existed Animation Res" + mExistedAnim.get(animId));
        OperationAnimation animation = mExistedAnim.get(animId);
        if(animation == null){
            loadRes();
            return null;
        }

        String jsonFilePath = getJsonPathIfResExist(generateFile(animId , animation.getAnimResUrl()), JSON_FILE_NAME);

        if (animation != null && !TextUtils.isEmpty(jsonFilePath)) {
            synchronized (mExistedAnim) {
                animation.completeAnimInfo(jsonFilePath);
                return animation;
            }
        }

        loadRes();

//        if (!TextUtils.isEmpty(jsonFilePath)) {//文件系统里有，组装起来返回
//            animation = new OperationAnimation();
//            animation.setAnimationId(animId);
//            animation.completeAnimInfo(jsonFilePath);
//            if (animation.getBottomWebpPath() != null && animation.getBottomWebpPath().endsWith(".webp")) {
//                addResToExistedSet(animation);
//            }
//            return animation;
//        }
        //内存  文件系统都没有说明真没有
        return null;
    }

    private static void addResToExistedSet(OperationAnimation anim) {
        mExistedAnim.put(anim.getAnimationId(), anim);
    }

    private static File generateFile(int resId, String downloadUrl) {
        // 准备存放文件的目录
        downloadUrl = getMD5(downloadUrl);
        MyLog.d(TAG, "md5之后的downloadUrl is " + downloadUrl);
        File file = new File(GlobalData.app().getFilesDir(), OPERATION_ZIP_PATH + resId + downloadUrl);
        return file;
    }

    private static String getMD5(String url) {
        String result="";
        try {
            MessageDigest md= MessageDigest.getInstance("md5");
            md.update(url.getBytes());
            byte[] bytes = md.digest();
            StringBuilder sb=new StringBuilder();
            for(byte b:bytes){
                String str= Integer.toHexString(b&0xFF);
                if(str.length()==1){
                    sb.append("0");
                }
                sb.append(str);
            }
            result=sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void downloadAndUnzip(OperationAnimation animation) {
        if (mDownloadingGiftSet.contains(animation.getAnimationId())) {
            MyLog.w(TAG, animation + " already downloading");
            return;
        }
        mDownloadingGiftSet.add(animation.getAnimationId());
        MyLog.w(TAG, "downloadAndUnzip:" + animation);

        String zipUrl = animation.getAnimResUrl();

        //创建好保存zip的file
        File parentFile = GlobalData.app().getFilesDir();
        File zipFile = new File(parentFile, OPERATION_ZIP_PATH + animation.getAnimationId() + ".zip");
        // 不存在则创建
        if (zipFile.exists()) {
            zipFile.delete();
        }

        try {
            zipFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        boolean downloadResult = false;
        long downloadBeginTime = System.currentTimeMillis();
        try {
            downloadResult = Network.downloadFile(ImageUrlDNSManager.getAvailableUrl(zipUrl), Uri.parse(zipUrl).getHost(), new FileOutputStream(zipFile), false, com.base.global.GlobalData.app());
        } catch (FileNotFoundException e) {
            MyLog.e(TAG, e);
        }

        if (!downloadResult) {
            try {
                downloadResult = Network.downloadFile(zipUrl, new FileOutputStream(zipFile));
            } catch (FileNotFoundException e) {
                MyLog.e(TAG, e);
            }
        }
        boolean unZipResult;
        if (downloadResult) {
            MyLog.w(TAG, "download success unzip");
            // 准备存放解压文件的文件夹
            File unZipFile = generateFile(animation.getAnimationId(), animation.getAnimResUrl());
            unZipFile.mkdirs();
            unZipResult = IOUtils.unZip(zipFile.getAbsolutePath(), unZipFile.getAbsolutePath());
            // 解压成功
            if (unZipResult) {
                // 将压缩文件删除了
                MyLog.d(TAG, "unzip success");
                zipFile.delete();
                // 填充该特效的信息
                String jsonFilePath = getJsonPathIfResExist(unZipFile, JSON_FILE_NAME);
                if (!TextUtils.isEmpty(jsonFilePath)) {
                    animation.completeAnimInfo(jsonFilePath);
                    if (animation.getTopWebpPath() != null) {
                        addResToExistedSet(animation);
                    }
                }

            }
            mDownloadingGiftSet.remove(animation.getAnimationId());
        } else {
            MyLog.w(TAG, "download operation resource failed");
            MiLinkMonitor.getInstance().trace("", 0, COMMAND_OPERATION_ANIM_RESOURCE,
                    1, downloadBeginTime,
                    System.currentTimeMillis(), 0, 0, 0);
        }
    }

    public static void loadRes() {
        pullResListFromServer(true);
    }

    public static void processData(OperationActivityProto.GetEffectResourcesC2sRsp response, boolean needDownLoadRes) {
        if (response == null) {
            return;
        }
        if (response.hasEffectResources()) {
            ArrayList<OperationAnimRes> list = new ArrayList<>();
            OperationActivityProto.EffectResources resources = response.getEffectResources();
            for (int i = 0; i < resources.getItemsCount(); i++) {
                list.add(OperationAnimRes.loadFromPB(resources.getItems(i)));
            }
            for (OperationAnimRes res : list) {
                OperationAnimation animation = new OperationAnimation();
                animation.setAnimationId(res.getResourceId());
                animation.setAnimResUrl(res.getResourceUrl());
                if(needDownLoadRes){
                    mDownloadAnimationRes.onNext(animation);
                }else {
                    addResToExistedSet(animation);
                }
            }
        }
    }

    /**
     * 判断动画资源是否存在
     *
     * @param animationResFile
     * @return
     */
    private static String getJsonPathIfResExist(File animationResFile, String jsonFileName) {
        MyLog.d("animationResFile:" + animationResFile + ", jsonFileName:" + jsonFileName);
        if (!animationResFile.exists()) {
            //文件夹都不存在，肯定没有资源
            MyLog.d(TAG, "animationResFile is null");
            return null;
        }
        // 文件夹存在
        String animationJsonPath = findConfigPath(animationResFile, jsonFileName);
        if (TextUtils.isEmpty(animationJsonPath)) {
            MyLog.d(TAG, "animationJsonPath is null");
            return null;
        }
        // 且能找到json文件
        return animationJsonPath;
    }

    private static String findConfigPath(File file, String fileName) {
        MyLog.d("fileName:" + fileName);
        if (file.isFile()) {
            if (file.getName().endsWith(fileName)) {
                return file.getAbsolutePath();
            } else {
                return null;
            }
        }
        if (file.isDirectory()) {
            for (File temp : file.listFiles()) {
                String re = findConfigPath(temp, fileName);
                if (re != null) {
                    return re;
                }
            }
        }
        return null;
    }

}
