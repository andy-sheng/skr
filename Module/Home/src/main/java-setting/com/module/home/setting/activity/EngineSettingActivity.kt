package com.module.home.setting.activity

import android.os.Bundle
import android.widget.RelativeLayout

import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.common.utils.U
import com.common.view.titlebar.CommonTitleBar
import com.engine.EngineConfigFromServer
import com.kyleduo.switchbutton.SwitchButton
import com.module.RouterConstants
import com.module.home.R

@Route(path = RouterConstants.ACTIVITY_ENGINE_SETTING)
class EngineSettingActivity : BaseActivity() {

    lateinit var mainActContainer: RelativeLayout
    lateinit var titlebar: CommonTitleBar
    lateinit var selfCollectionSb: SwitchButton
    lateinit var earMonitorSb: SwitchButton
    lateinit var configManualSb: SwitchButton

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.engine_setting_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        mainActContainer = this.findViewById(R.id.main_act_container)
        titlebar = this.findViewById(R.id.titlebar)
        configManualSb = this.findViewById(R.id.config_manual_sb)
        selfCollectionSb = this.findViewById(R.id.self_collection_sb)
        earMonitorSb = this.findViewById(R.id.ear_monitor_sb)

        initStatus()
        configManualSb.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                selfCollectionSb.isEnabled = true
                earMonitorSb.isEnabled = true
                U.getToastUtil().showShort("开启手动配置，可设置是否启用自采集")
            }else{
                selfCollectionSb.isEnabled = false
                earMonitorSb.isEnabled = false
            }
        }
        selfCollectionSb.setOnCheckedChangeListener { buttonView, isChecked ->
            earMonitorSb.isEnabled = isChecked
            if(isChecked){
                U.getToastUtil().showShort("开启自采集，可设置是否启用耳返")
            }else{
                earMonitorSb.isChecked = false
            }
        }
        titlebar.leftTextView.setOnClickListener {
            finish()
        }
    }

    private fun initStatus() {
        var engineConfigFromServer = EngineConfigFromServer.getDefault()
        if (EngineConfigFromServer.configManual()) {
            configManualSb.isChecked = true
            selfCollectionSb.isEnabled = true
            earMonitorSb.isEnabled = true
        } else {
            configManualSb.isChecked = false
            selfCollectionSb.isEnabled = false
            earMonitorSb.isEnabled = false
        }
        selfCollectionSb.isChecked = engineConfigFromServer.isUseExternalAudio
        earMonitorSb.isChecked = engineConfigFromServer.isEnableAudioPreview
        if (selfCollectionSb.isChecked) {
            earMonitorSb.isEnabled = true
        } else {
            earMonitorSb.isChecked = false
            earMonitorSb.isEnabled = false
        }
    }

    override fun finish() {
        super.finish()
        if(configManualSb.isChecked){
            var a = EngineConfigFromServer.getDefault()
            a.isUseExternalAudio = selfCollectionSb.isChecked
            a.isEnableAudioPreview = earMonitorSb.isChecked
            a.save2Pref()
        }else{
            EngineConfigFromServer.clearManualConfig()
            EngineConfigFromServer.getDefault()
        }

    }

    override fun useEventBus(): Boolean {
        return false
    }

    override fun resizeLayoutSelfWhenKeybordShow(): Boolean {
        return true
    }
}
