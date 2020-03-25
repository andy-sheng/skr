package com.common.core.gt3

import android.app.Activity
import android.text.TextUtils
import android.util.Log
import com.alibaba.fastjson.JSON
import com.common.log.MyLog
import com.common.rxretrofit.*
import com.geetest.sdk.GT3ConfigBean
import com.geetest.sdk.GT3ErrorBean
import com.geetest.sdk.GT3GeetestUtils
import com.geetest.sdk.GT3Listener
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Gt3Manager constructor(val context: Activity) {

    private val TAG = "Gt3Manager"

    private var gt3GeetestUtils: GT3GeetestUtils? = null
    private var gt3ConfigBean: GT3ConfigBean? = null
    private var uuid: String? = null

    private var callback: ((challenge: String,
                            validate: String,
                            seccode: String) -> Unit)? = null

    fun init() {
        // TODO 请在oncreate方法里初始化以获取足够手势数据来保证第一轮验证成功率
        gt3GeetestUtils = GT3GeetestUtils(context)
        // 配置bean文件，也可在oncreate初始化
        gt3ConfigBean = GT3ConfigBean()
        // 设置验证模式，1：bind，2：unbind
        gt3ConfigBean?.setPattern(1)
        // 设置点击灰色区域是否消失，默认不消失
        gt3ConfigBean?.setCanceledOnTouchOutside(true)
        // 设置语言，如果为null则使用系统默认语言
        gt3ConfigBean?.setLang(null)
        // 设置加载webview超时时间，单位毫秒，默认10000，仅且webview加载静态文件超时，不包括之前的http请求
        gt3ConfigBean?.setTimeout(10000)
        // 设置webview请求超时(用户点选或滑动完成，前端请求后端接口)，单位毫秒，默认10000
        gt3ConfigBean?.setWebviewTimeout(10000)
        // 设置自定义view
        //        gt3ConfigBean?.setLoadImageView(new TestLoadingView(this));
        // 设置回调监听
        gt3ConfigBean?.setListener(object : GT3Listener() {

            /**
             * api1结果回调
             * @param result
             */
            override fun onApi1Result(result: String?) {
                MyLog.i(TAG, "GT3BaseListener-->onApi1Result-->" + result!!)
            }

            /**
             * 验证码加载完成
             * @param duration 加载时间和版本等信息，为json格式
             */
            override fun onDialogReady(duration: String?) {
                MyLog.i(TAG, "GT3BaseListener-->onDialogReady-->" + duration!!)
            }

            /**
             * 验证结果
             * @param result
             */
            override fun onDialogResult(result: String?) {
                MyLog.i(TAG, "GT3BaseListener-->onDialogResult-->" + result!!)
//                {"geetest_challenge":"e8a54e62a403874ed351385004445f79k0","geetest_seccode":"4bc1d06918a16195d6b071f7b191e5a2|jordan","geetest_validate":"4bc1d06918a16195d6b071f7b191e5a2"}
                // 开启api2逻辑
                if (TextUtils.isEmpty(result)) {
                    gt3GeetestUtils?.showFailedDialog()
                } else {
                    gt3GeetestUtils?.showSuccessDialog()
                    var json = JSON.parseObject(result)
                    callback?.invoke(json.getString("geetest_challenge"),  json.getString("geetest_validate"),json.getString("geetest_seccode"))
                }
//                getLoginCode(json.getString("geetest_challenge"), json.getString("geetest_seccode"), json.getString("geetest_validate"))
            }

            /**
             * api2回调
             * @param result
             */
            override fun onApi2Result(result: String?) {
                MyLog.i(TAG, "GT3BaseListener-->onApi2Result-->" + result!!)
            }

            /**
             * 统计信息，参考接入文档
             * @param result
             */
            override fun onStatistics(result: String) {
                MyLog.i(TAG, "GT3BaseListener-->onStatistics-->$result")
            }

            /**
             * 验证码被关闭
             * @param num 1 点击验证码的关闭按钮来关闭验证码, 2 点击屏幕关闭验证码, 3 点击返回键关闭验证码
             */
            override fun onClosed(num: Int) {
                MyLog.i(TAG, "GT3BaseListener-->onClosed-->$num")
            }

            /**
             * 验证成功回调
             * @param result
             */
            override fun onSuccess(result: String) {
                MyLog.i(TAG, "GT3BaseListener-->onSuccess-->$result")
            }

            /**
             * 验证失败回调
             * @param errorBean 版本号，错误码，错误描述等信息
             */
            override fun onFailed(errorBean: GT3ErrorBean) {
                MyLog.i(TAG, "GT3BaseListener-->onFailed-->$errorBean")
            }

            /**
             * api1回调
             */
            override fun onButtonClick() {
                execApi1()
            }
        })
        gt3GeetestUtils?.init(gt3ConfigBean)
    }


    fun startVerify(uuid: String, callback: ((challenge: String,
                                              validate: String,
                                              seccode: String) -> Unit)?) {
        this.uuid = uuid
        this.callback = callback
        // 开启验证
        gt3GeetestUtils?.startCustomFlow()
    }

    private fun execApi1() {
        var gt3ServerApi = ApiManager.getInstance().createService(Gt3ServerApi::class.java)
        gt3ServerApi.api1(uuid).enqueue(object : Callback<ApiResult> {
            override fun onFailure(call: Call<ApiResult>, t: Throwable) {
            }

            override fun onResponse(call: Call<ApiResult>, response: Response<ApiResult>) {
                // 继续验证
                Log.i(TAG, "RequestAPI1-->onPostExecute: $response")
                // SDK可识别格式为
                // {"success":1,"challenge":"06fbb267def3c3c9530d62aa2d56d018","gt":"019924a82c70bb123aae90d483087f94"}
                var result = response.body()
                var jsonObject: JSONObject? = null
                try {
                    jsonObject = JSONObject(result?.data?.toJSONString())
                } catch (e: Exception) {
                    MyLog.e(TAG, e)
                }
                // TODO 将api1请求结果传入此方法，即使为null也要传入，SDK内部已处理，否则易出现无限loading
                gt3ConfigBean?.setApi1Json(jsonObject)
                // 继续api验证
                gt3GeetestUtils?.getGeetest()
            }
        })
    }

//    private fun getLoginCode(challenge: String,
//                             validate: String,
//                             seccode: String) {
//        var gt3ServerApi = ApiManager.getInstance().createService(Gt3ServerApi::class.java)
//        gt3ServerApi.getLoginCode(uuid, challenge, validate, seccode).enqueue(object : Callback<ApiResult> {
//            override fun onFailure(call: Call<ApiResult>, t: Throwable) {
//            }
//
//            override fun onResponse(call: Call<ApiResult>, response: Response<ApiResult>) {
//                // 继续验证
//                Log.i(TAG, "RequestAPI1-->onPostExecute: $response")
//                var result = response.body()
//                if (result?.errno == 0) {
//                    gt3GeetestUtils?.showSuccessDialog()
//                } else {
//                    gt3GeetestUtils?.showFailedDialog()
//                }
//            }
//        })
//    }

    fun destroy() {
        gt3GeetestUtils?.destory()
    }

}