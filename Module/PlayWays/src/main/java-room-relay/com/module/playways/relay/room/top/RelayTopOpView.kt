package com.module.playways.relay.room.top

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import com.common.core.view.setDebounceViewClickListener
import com.module.playways.R

// 顶部操作栏
class RelayTopOpView : RelativeLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val feedBackIv: ImageView
    private val gameRuleIv: ImageView
    private val ivVoiceSetting: ImageView

    var listener: Listener? = null

    init {
        View.inflate(context, R.layout.relay_top_op_view, this)

        feedBackIv = this.findViewById(R.id.feed_back_iv)
        gameRuleIv = this.findViewById(R.id.game_rule_iv)
        ivVoiceSetting = this.findViewById(R.id.iv_voice_setting)

        feedBackIv.setDebounceViewClickListener { listener?.onClickFeedBack() }
        gameRuleIv.setDebounceViewClickListener { listener?.onClickGameRule() }
        ivVoiceSetting.setDebounceViewClickListener { listener?.onClickVoiceAudition() }
    }

    interface Listener {

        fun onClickGameRule()

        fun onClickFeedBack()

        fun onClickVoiceAudition()
    }
}