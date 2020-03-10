package com.module.home.setting.activity

import android.os.Bundle
import android.widget.RelativeLayout

import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.common.utils.U
import com.common.view.titlebar.CommonTitleBar
import com.engine.EngineConfigFromServer
import com.engine.Params
import com.kyleduo.switchbutton.SwitchButton
import com.module.RouterConstants
import com.module.home.R

@Route(path = RouterConstants.ACTIVITY_ENGINE_SETTING)
class EngineSettingActivity : BaseActivity() {

    lateinit var mainActContainer: RelativeLayout
    lateinit var titlebar: CommonTitleBar
    lateinit var selfCollectionSb: SwitchButton
//    lateinit var earMonitorSb: SwitchButton
//    lateinit var configManualSb: SwitchButton

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.engine_setting_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        mainActContainer = this.findViewById(R.id.main_act_container)
        titlebar = this.findViewById(R.id.titlebar)
//        configManualSb = this.findViewById(R.id.config_manual_sb)
        selfCollectionSb = this.findViewById(R.id.self_collection_sb)
//        earMonitorSb = this.findViewById(R.id.ear_mon。itor_sb)

//        initStatus()
//        configManualSb.setOnCheckedChangeListener { buttonView, isChecked ->
//            if(isChecked){
//                selfCollectionSb.isEnabled = true
//                earMonitorSb.isEnabled = true
//                U.getToastUtil().showShort("开启手动配置，可设置是否启用自采集")
//            }else{
//                selfCollectionSb.isEnabled = false
//                earMonitorSb.isEnabled = false
//            }
//        }
        var r = EngineConfigFromServer.getSelfCollectionSwitch()
        var configFromServer = EngineConfigFromServer.getDefault()
        if(r==0){
            if(configFromServer.isUseExternalAudio){
                selfCollectionSb.isChecked = true
            }else{
                selfCollectionSb.isChecked = false
            }
        }else{
            if(r==1){
                selfCollectionSb.isChecked = true
            }else if(r==2){
                selfCollectionSb.isChecked = false
            }
        }

        selfCollectionSb.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                EngineConfigFromServer.setSelfCollectionSwitch(1)
//                if(configFromServer.hasServerConfig==true && configFromServer.isEnableAudioPreview==false){
//
//                }else{
                    U.getToastUtil().showShort("可在调音面板开关耳返，开启后在部分机型上可能会引起卡顿")
//                }
            }else{
                EngineConfigFromServer.setSelfCollectionSwitch(2)
            }
        }
        titlebar.leftTextView.setOnClickListener {
            finish()
        }
    }

//    private fun initStatus() {
//        var engineConfigFromServer = EngineConfigFromServer.getDefault()
//        if (EngineConfigFromServer.configManual()) {
//            configManualSb.isChecked = true
//            selfCollectionSb.isEnabled = true
//            earMonitorSb.isEnabled = true
//        } else {
//            configManualSb.isChecked = false
//            selfCollectionSb.isEnabled = false
//            earMonitorSb.isEnabled = false
//        }
//        selfCollectionSb.isChecked = engineConfigFromServer.isUseExternalAudio
//        earMonitorSb.isChecked = engineConfigFromServer.isEnableAudioPreview
//        if (selfCollectionSb.isChecked) {
//            earMonitorSb.isEnabled = true
//        } else {
//            earMonitorSb.isChecked = false
//            earMonitorSb.isEnabled = false
//        }
//    }
//
//    override fun finish() {
//        super.finish()
//        if(configManualSb.isChecked){
//            var a = EngineConfigFromServer.getDefault()
//            a.isUseExternalAudio = selfCollectionSb.isChecked
//            a.isEnableAudioPreview = earMonitorSb.isChecked
//            a.save2Pref()
//        }else{
//            EngineConfigFromServer.clearManualConfig()
//            EngineConfigFromServer.getDefault()
//        }
//
//    }

    override fun useEventBus(): Boolean {
        return false
    }

    override fun resizeLayoutSelfWhenKeyboardShow(): Boolean {
        return true
    }
}
