package com.common.core.upgrade;

import com.common.rxretrofit.ApiResult;


public interface UpgradeCheckApi {

    /**
     * packageName:"com.zq.live"
     * platfrom:"android"
     * versionCode:"50001"
     * channel:"DEFAULT"
     *
     * data:{
     *  needUpdate:"true"
     *  updateInfo:{
     *   downloadUrl:"http://xxxx/xxx.apk"
     *   date:"2019.1.31"
     *   versionCode:50021
     *   forceUpdate:true
     *   size:1024*1024*25(25M)
     *   updateTitle:"发现新版本"
     *   updateMsg:" 1.bug修复 \n 2.功能更新"
     *  }
     * }
     * @return
     */
    io.reactivex.Observable<ApiResult> getUpdateInfo();
}
