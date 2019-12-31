package com.module.playways.race.room

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.anim.svga.SvgaParserAdapter
import com.common.base.BaseActivity
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.model.ScoreStateModel
import com.common.core.view.setDebounceViewClickListener
import com.common.log.MyLog
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.component.busilib.view.CircleCountDownView
import com.component.level.utils.LevelConfigUtils
import com.dialog.view.TipsDialogView
import com.glidebitmappool.BitmapFactoryAdapter
import com.module.RouterConstants
import com.module.playways.R
import com.module.playways.listener.AnimationListener
import com.module.playways.race.RaceRoomServerApi
import com.module.playways.race.room.model.LevelResultModel
import com.module.playways.race.room.model.SaveRankModel
import com.opensource.svgaplayer.*
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import kotlinx.android.synthetic.main.input_container_view_layout.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody

@Route(path = RouterConstants.ACTIVITY_RACE_RESULT)
class RaceResultActivity : BaseActivity() {

    val mTag = "RaceResultActivity"

    lateinit var changeTv: TextView
    lateinit var circleView: CircleCountDownView
    lateinit var levelView: ImageView
    lateinit var levelSvga: SVGAImageView
    lateinit var levelDescTv: ExTextView
    lateinit var descTv: TextView
    lateinit var subLevelSvga: SVGAImageView
    lateinit var countDownTv: TextView
    lateinit var playAgainTv: ExTextView
    lateinit var zuanshiSaveTv: ExTextView
    lateinit var ivBack: ExImageView

    lateinit var levelSave: CircleCountDownView
    lateinit var levelSolid: ExImageView
    lateinit var levelMedia: ImageView
    lateinit var levelSaveDesc: TextView

    lateinit var vipLevelSave: CircleCountDownView
    lateinit var vipLevelSolid: ExImageView
    lateinit var vipLevelMedia: ImageView
    lateinit var vipLevelDesc: TextView

    private var mGameRuleDialog: DialogPlus? = null
    private var mTipsDialogView: TipsDialogView? = null

    private val raceRoomServerApi = ApiManager.getInstance().createService(RaceRoomServerApi::class.java)

    var result: LevelResultModel? = null
    var roomID: Int = -1
    var roundSeq: Int = -1

    var goMatchJob: Job? = null

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.race_result_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        roomID = intent.getIntExtra("roomID", -1)
        roundSeq = intent.getIntExtra("roundSeq", -1)
        if (roomID == -1 || roundSeq == -1) {
            MyLog.e(TAG, "no roomID or roundSeq")
            finish()
            return
        }
        changeTv = findViewById(R.id.change_tv)

        circleView = findViewById(R.id.circle_view)
        levelView = findViewById(R.id.level_view)
        levelSvga = findViewById(R.id.level_svga)
        levelDescTv = findViewById(R.id.level_desc_tv)
        descTv = findViewById(R.id.desc_tv)
        subLevelSvga = findViewById(R.id.sub_level_svga)
        countDownTv = findViewById(R.id.count_down_tv)

        zuanshiSaveTv = findViewById(R.id.zuanshi_save_tv)
        playAgainTv = findViewById(R.id.play_again_tv)
        ivBack = findViewById(R.id.iv_back)

        levelSave = findViewById(R.id.level_save)
        levelSaveDesc = findViewById(R.id.level_save_desc)
        vipLevelSave = findViewById(R.id.vip_level_save)
        vipLevelDesc = findViewById(R.id.vip_level_desc)

        levelSave = findViewById(R.id.level_save)
        levelSolid = findViewById(R.id.level_solid)
        levelMedia = findViewById(R.id.level_media)
        levelSaveDesc = findViewById(R.id.level_save_desc)

        vipLevelSave = findViewById(R.id.vip_level_save)
        vipLevelSolid = findViewById(R.id.vip_level_solid)
        vipLevelMedia = findViewById(R.id.vip_level_media)
        vipLevelDesc = findViewById(R.id.vip_level_desc)

        ivBack.setDebounceViewClickListener { finish() }

        playAgainTv.setDebounceViewClickListener { goMatchPage() }

        descTv.setDebounceViewClickListener {
            goMatchJob?.cancel()
            showGameRuleDialog()
        }

        zuanshiSaveTv.setDebounceViewClickListener {
            goMatchJob?.cancel()
            countDownTv.visibility = View.GONE
            showConfirmDialog(false)
        }

        getResult()
        U.getSoundUtils().preLoad(mTag, R.raw.newrank_resultpage)
        launch {
            delay(200)
            U.getSoundUtils().play(mTag, R.raw.newrank_resultpage)
        }
    }

    private fun showConfirmDialog(isRecharge: Boolean) {
        mTipsDialogView?.dismiss(false)
        mTipsDialogView = TipsDialogView.Builder(this)
                .setMessageTip(if (isRecharge) "钻石余额不足" else "是否花费${result?.moneySaveState?.zsAmount
                        ?: 0}钻石保段一次")
                .setConfirmTip(if (isRecharge) "立即充值" else "确定")
                .setCancelTip("取消")
                .setConfirmBtnClickListener {
                    mTipsDialogView?.dismiss(false)
                    if (isRecharge) {
                        ARouter.getInstance().build(RouterConstants.ACTIVITY_BALANCE)
                                .navigation()
                    } else {
                        diamondSaveLevel()
                    }
                }
                .setCancelBtnClickListener {
                    mTipsDialogView?.dismiss()
                }
                .build()
        mTipsDialogView?.showByDialog()
    }

    private fun diamondSaveLevel() {
        launch {
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(null))
            val result = subscribe { raceRoomServerApi.diamondSaveLevel(body) }
            if (result.errno == 0) {
                val raceResultModel = JSON.parseObject(result.data.getString("item"), LevelResultModel::class.java)
                if (raceResultModel != null) {
                    U.getToastUtil().showShort("钻石保段成功")
                    showResult(raceResultModel)
                    return@launch
                } else {
                    MyLog.e(TAG, "getResult erro 服务器数据为空")
                }
            } else {
                if (result.errno == 8412201) {
                    // 钻石不足
                    showConfirmDialog(true)
                } else {
                    U.getToastUtil().showShort(result.errmsg)
                }
            }
        }
    }

    private fun showGameRuleDialog() {
        mGameRuleDialog?.dismiss(false)
        mGameRuleDialog = DialogPlus.newDialog(this)
                .setContentHolder(ViewHolder(R.layout.race_game_rule_view_layout))
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_50)
                .setMargin(U.getDisplayUtils().dip2px(16f), -1, U.getDisplayUtils().dip2px(16f), -1)
                .setExpanded(false)
                .setGravity(Gravity.CENTER)
                .create()
        mGameRuleDialog?.show()
    }

    private fun getResult() {
        launch {
            val result = subscribe { raceRoomServerApi.getResult(roomID, MyUserInfoManager.uid.toInt(), roundSeq) }
            if (result.errno == 0) {
                val raceResultModel = JSON.parseObject(result.data.getString("userScoreChange"), LevelResultModel::class.java)
                if (raceResultModel != null) {
                    showResult(raceResultModel)
                    return@launch
                } else {
                    MyLog.e(TAG, "getResult erro 服务器数据为空")
                }
            } else {

            }
        }
    }

    fun initAnimation() {
        circleView.cancelAnim()
        levelSvga.callback = null
        levelSvga.stopAnimation()
        subLevelSvga.callback = null
        subLevelSvga.stopAnimation()
        levelSave.cancelAnim()
        vipLevelSave.cancelAnim()
    }

    private fun showResult(raceResultModel: LevelResultModel) {
        this.result = raceResultModel
        when (raceResultModel.moneySaveState?.status) {
            SaveRankModel.ESRS_ENABLE -> {
                // 服务器定的，已启用才是可点击
                zuanshiSaveTv.visibility = View.VISIBLE
                zuanshiSaveTv.text = "${raceResultModel.moneySaveState?.zsAmount}钻保段"
            }
            else -> {
                zuanshiSaveTv.visibility = View.GONE
            }
        }
        descTv.text = "距离下次升段还需${raceResultModel.gap}经验"
        if (raceResultModel.get >= 0) {
            changeTv.text = "+${raceResultModel.get}"
        } else {
            changeTv.text = raceResultModel.get.toString()
        }

        initAnimation()

        if (!raceResultModel.states.isNullOrEmpty() && raceResultModel.states?.size == 3) {
            // 初始化数据
            val begin = raceResultModel.states?.get(0)!!
            val middle = raceResultModel.states?.get(1)!!
            val end = raceResultModel.states?.get(2)!!

//            // todo 搞一个测试数据
            // todo 段位结果数据
//            val begin = ScoreStateModel()
//            begin.seq = 0
//            begin.mainRanking = 1
//            begin.subRanking = 1
//            begin.currExp = 0
//            begin.maxExp = 5
//            begin.rankingDesc = "潜力新秀I"
//            val middle = ScoreStateModel()
//            middle.seq = 0
//            middle.mainRanking = 2
//            middle.subRanking = 2
//            middle.currExp = 2
//            middle.maxExp = 5
//            middle.rankingDesc = "潜力新秀II"
//            val end = ScoreStateModel()
//            end.seq = 0
//            end.mainRanking = 1
//            end.subRanking = 1
//            end.currExp = 0
//            end.maxExp = 5
//            end.rankingDesc = "潜力新秀I"
            // todo 普通保段数据
//            val beginSimple = SaveRankModel()
//            beginSimple.status = 1
//            beginSimple.curBar = 2
//            beginSimple.maxBar = 6
//            val middleSimple = SaveRankModel()
//            middleSimple.status = 2
//            middleSimple.curBar = 6
//            middleSimple.maxBar = 6
//            val endSimple = SaveRankModel()
//            endSimple.status = 3
//            endSimple.curBar = 6
//            endSimple.maxBar = 6
            // todo vip保段数据
//            val beginVip = SaveRankModel()
//            beginVip.status = 2
//            beginVip.curBar = 0
//            beginVip.maxBar = 0
//            val endVip = SaveRankModel()
//            endVip.status = 2
//            endVip.curBar = 0
//            endVip.maxBar = 0
//            // todo 测试数据

            levelDescTv.text = begin.rankingDesc

            if (LevelConfigUtils.getImageResoucesLevel(begin.mainRanking) != 0) {
                levelView.background = U.getDrawable(LevelConfigUtils.getImageResoucesLevel(begin.mainRanking))
            }
            circleView.cancelAnim()
            circleView.max = 360
            circleView.progress = 360 * begin.currExp / begin.maxExp

            // 初始化日常保段或vip
            val beginSimple = raceResultModel.simpleSaveStates?.get(0)!!
            val middleSimple = raceResultModel.simpleSaveStates?.get(1)!!
            val endSimple = if (raceResultModel.simpleSaveStates?.size == 3) {
                raceResultModel.simpleSaveStates?.get(2)
            } else {
                null
            }

            levelSave.cancelAnim()
            levelSave.max = 360
            levelSave.progress = 360 * beginSimple.curBar / beginSimple.maxBar
            setSimpleSaveInfo(beginSimple)

            val beginVip = raceResultModel.vipSaveStates?.get(0)!!
            val endVip = raceResultModel.vipSaveStates?.get(1)!!
            vipLevelSave.cancelAnim()
            vipLevelSave.max = 360
            vipLevelSave.progress = 360
            setVipInfo(beginVip)

            // 开始动画
            levelChangeAnimation(begin, middle, object : AnimationListener {
                override fun onFinish() {
                    levelSaveAnimationGo(beginSimple, middleSimple, endSimple, object : AnimationListener {
                        override fun onFinish() {
                            launch {
                                delay(500)  // 等一下
                                // 是否要用保段动画
                                if (beginVip.status == endVip.status) {
                                    // 不用变
                                } else {
                                    setVipInfo(endVip)
                                }
                                setSimpleSaveInfo(endSimple)
                                if (checkHasSaveAnimation(beginSimple, middleSimple, endSimple) == 1) {
                                    U.getToastUtil().showShort("保段成功")
                                } else if (endVip.status > beginVip.status && endVip.status == SaveRankModel.ESRS_USED) {
                                    U.getToastUtil().showShort("VIP保段成功")
                                }
                                levelChangeAnimation(middle, end, object : AnimationListener {
                                    override fun onFinish() {
                                    }
                                })
                            }
                        }
                    })
                }
            })
        } else {
            // 服务器有错误
            if (LevelConfigUtils.getImageResoucesLevel(raceResultModel.getLastState()?.mainRanking
                            ?: 0) != 0) {
                levelView.background = U.getDrawable(LevelConfigUtils.getImageResoucesLevel(raceResultModel.getLastState()?.mainRanking
                        ?: 0))
            }
        }

        goMatchJob?.cancel()
        countDownTv.visibility = View.VISIBLE
        goMatchJob = launch {
            repeat(8) {
                countDownTv.text = "${8 - it}s后自动进入下一场挑战"
                delay(1000)
            }
            goMatchPage()
        }
    }

    private fun setSimpleSaveAreaAlph(alph: Float) {
        levelSave.alpha = alph
        levelSolid.alpha = alph
        levelMedia.alpha = alph
    }

    private fun setVipSaveAreaAlph(alph: Float) {
        vipLevelSave.alpha = alph
        vipLevelSolid.alpha = alph
        vipLevelMedia.alpha = alph
    }

    private fun setSimpleSaveInfo(model: SaveRankModel?) {
        when {
            model?.status == SaveRankModel.ESRS_USED -> {
                setSimpleSaveAreaAlph(0.3f)
                levelSaveDesc.text = "日常保段已使用"
            }
            model?.status == SaveRankModel.ESRS_ENABLE -> {
                setSimpleSaveAreaAlph(1f)
                levelSaveDesc.text = "日常保段"
            }
            else -> {
                setSimpleSaveAreaAlph(1f)
                levelSaveDesc.text = "日常保段"
            }
        }
    }

    fun setVipInfo(model: SaveRankModel) {
        when {
            model.status == SaveRankModel.ESRS_USED -> {
                setVipSaveAreaAlph(0.3f)
                vipLevelDesc.text = "vip保段已使用"
            }
            model.status == SaveRankModel.ESRS_ENABLE -> {
                setVipSaveAreaAlph(1f)
                vipLevelDesc.text = "vip保段"
            }
            else -> {
                setVipSaveAreaAlph(0.3f)
                vipLevelDesc.text = "开启VIP保段"
                vipLevelDesc.setDebounceViewClickListener {
                    ARouter.getInstance().build(RouterConstants.ACTIVITY_WEB)
                            .withString("url", ApiManager.getInstance().findRealUrlByChannel("https://app.inframe.mobi/user/newVip?title=1"))
                            .greenChannel().navigation()
                    finish()
                }
            }
        }
    }


    // 保段的动画
    fun levelSaveAnimationGo(begin: SaveRankModel, middle: SaveRankModel, end: SaveRankModel?, listener: AnimationListener) {
        when (checkHasSaveAnimation(begin, middle, end)) {
            1 -> {
                levelSave.playProgress(360 * begin.curBar / begin.maxBar, 360 * middle.curBar / middle.maxBar) {
                    if (end != null) {
                        levelSave.playProgress(360 * middle.curBar / middle.maxBar, 360 * end.curBar / end.maxBar) {
                            setSimpleSaveAreaAlph(0.3f)
                            listener.onFinish()
                        }
                    } else {
                        setSimpleSaveAreaAlph(0.3f)
                        listener.onFinish()
                    }
                }
            }
            2 -> {
                levelSave.playProgress(360 * begin.curBar / begin.maxBar, 360 * middle.curBar / middle.maxBar) {
                    if (end != null) {
                        levelSave.playProgress(360 * middle.curBar / middle.maxBar, 360 * end.curBar / end.maxBar) {
                            listener.onFinish()
                        }
                    } else {
                        listener.onFinish()
                    }
                }
            }
            else -> {
                // 没有动画 donothing
                listener.onFinish()
            }
        }
    }

    // -1 表示服务器错误  0表示无动画 1表示有动画用过保段 2表示有动画没用过保段
    fun checkHasSaveAnimation(begin: SaveRankModel, middle: SaveRankModel, end: SaveRankModel?): Int {
        // 状态不可逆 为开启 开启 和已用
        if (end != null) {
            if (end.status >= middle.status && middle.status >= begin.status) {
                if (end.status == middle.status && middle.status == begin.status) {
                    return 0
                } else {
                    // 状态发生变化
                    return if (end.status == SaveRankModel.ESRS_USED) {
                        1
                    } else {
                        2
                    }
                }
            } else {
                return -1
            }
        } else {
            if (middle.status > begin.status) {
                if (middle.status == SaveRankModel.ESRS_USED) {
                    return 1
                } else {
                    return 2
                }
            } else if (middle.status == begin.status) {
                return 0
            } else {
                return -1
            }
        }

    }

    // 积分变化用CircleCountDownView 段位变化用一个svga
    fun levelChangeAnimation(before: ScoreStateModel, after: ScoreStateModel, listener: AnimationListener) {
        val start = before.currExp.toFloat() / before.maxExp.toFloat()
        val end = after.currExp.toFloat() / after.maxExp.toFloat()
        when (isLevelUp(before, after)) {
            true -> {
                // 段位上升 积分-段位-积分
                expAnimation(start, 1f, object : AnimationListener {
                    override fun onFinish() {
                        levelAnimation(before, after, object : AnimationListener {
                            override fun onFinish() {
                                expAnimation(0f, end, object : AnimationListener {
                                    override fun onFinish() {
                                        listener.onFinish()
                                    }
                                })
                            }
                        })
                    }
                })
            }
            false -> {
                // 段位下降 积分-段位-积分
                expAnimation(start, 0f, object : AnimationListener {
                    override fun onFinish() {
                        levelAnimation(before, after, object : AnimationListener {
                            override fun onFinish() {
                                expAnimation(1f, end, object : AnimationListener {
                                    override fun onFinish() {
                                        listener.onFinish()
                                    }
                                })
                            }
                        })
                    }
                })
            }
            else -> {
                // 段位不变 积分
                expAnimation(start, end, object : AnimationListener {
                    override fun onFinish() {
                        listener.onFinish()
                    }
                })
            }
        }
    }

    // 积分动画(两个百分比)
    fun expAnimation(start: Float, end: Float, listener: AnimationListener) {
        if (start == end) {
            circleView.progress = (360 * start).toInt()
            listener.onFinish()
        } else {
            // 做动画吧
            circleView.playProgress((start * 360).toInt(), (end * 360).toInt()) {
                listener.onFinish()
            }
        }
    }

    // 段位变化动画
    fun levelAnimation(before: ScoreStateModel, after: ScoreStateModel, listener: AnimationListener) {
        if (before.mainRanking == after.mainRanking && before.subRanking == after.subRanking) {
            listener.onFinish()
        } else {
            // 做动画吧
            // 只要不相等，一定有subLevelAnimation动画
            if (before.mainRanking != after.mainRanking) {
                mainLevelAnimation(before, after, listener)
                subLevelAnimation(after, null)
            } else {
                subLevelAnimation(after, listener)
            }
        }
    }

    private fun mainLevelAnimation(before: ScoreStateModel, after: ScoreStateModel, listener: AnimationListener) {
        levelSvga.callback = null
        levelSvga.clearAnimation()
        levelSvga.visibility = View.VISIBLE
        levelSvga.loops = 1
        SvgaParserAdapter.parse("level_change.svga", object : SVGAParser.ParseCompletion {
            override fun onComplete(videoItem: SVGAVideoEntity) {
                val drawable = SVGADrawable(videoItem, requestDynamicBitmapItem(before.mainRanking, before.subRanking, after.mainRanking, after.subRanking))
                levelSvga.setImageDrawable(drawable)
                levelSvga.startAnimation()
            }

            override fun onError() {

            }
        })

        levelSvga.callback = object : SVGACallback {
            override fun onPause() {

            }

            override fun onFinished() {
                levelSvga.callback = null
                levelSvga.stopAnimation(true)
                levelSvga.visibility = View.GONE

                levelDescTv.text = after.rankingDesc
                if (LevelConfigUtils.getImageResoucesLevel(after.mainRanking) != null) {
                    levelView.background = U.getDrawable(LevelConfigUtils.getImageResoucesLevel(after.mainRanking))
                }
                listener.onFinish()
            }

            override fun onRepeat() {
                if (levelSvga.isAnimating) {
                    levelSvga.stopAnimation(false)
                }
            }

            override fun onStep(i: Int, v: Double) {

            }
        }
    }

    private fun subLevelAnimation(after: ScoreStateModel, listener: AnimationListener?) {
        subLevelSvga.callback = null
        subLevelSvga.clearAnimation()
        subLevelSvga.visibility = View.VISIBLE
        subLevelSvga.loops = 1
        SvgaParserAdapter.parse("sub_level_change.svga", object : SVGAParser.ParseCompletion {
            override fun onComplete(videoItem: SVGAVideoEntity) {
                val drawable = SVGADrawable(videoItem)
                subLevelSvga.setImageDrawable(drawable)
                subLevelSvga.startAnimation()
            }

            override fun onError() {

            }
        })

        subLevelSvga.callback = object : SVGACallback {
            override fun onPause() {

            }

            override fun onFinished() {
                subLevelSvga.callback = null
                subLevelSvga.stopAnimation(true)
                subLevelSvga.visibility = View.GONE

                levelDescTv.text = after.rankingDesc
                listener?.onFinish()
            }

            override fun onRepeat() {
                if (subLevelSvga.isAnimating) {
                    subLevelSvga.stopAnimation(false)
                }
            }

            override fun onStep(i: Int, v: Double) {

            }
        }
    }

    private fun requestDynamicBitmapItem(levelBefore: Int, subLevelBefore: Int, levelNow: Int, sublevelNow: Int): SVGADynamicEntity {
        val dynamicEntity = SVGADynamicEntity()
//        if (LevelConfigUtils.getImageResoucesSubLevel(levelBefore, subLevelBefore) != 0) {
//            dynamicEntity.setDynamicImage(BitmapFactoryAdapter.decodeResource(resources, LevelConfigUtils.getImageResoucesSubLevel(levelBefore, subLevelBefore)), "keyLevelBefore")
//        }
        if (LevelConfigUtils.getImageResoucesLevel(levelBefore) != 0) {
            dynamicEntity.setDynamicImage(BitmapFactoryAdapter.decodeResource(resources, LevelConfigUtils.getImageResoucesLevel(levelBefore)), "keyMedalBefore")
        }
//        if (LevelConfigUtils.getImageResoucesSubLevel(levelNow, sublevelNow) != 0) {
//            dynamicEntity.setDynamicImage(BitmapFactoryAdapter.decodeResource(resources, LevelConfigUtils.getImageResoucesSubLevel(levelNow, sublevelNow)), "keyLevelNew")
//        }
        if (LevelConfigUtils.getImageResoucesLevel(levelNow) != 0) {
            dynamicEntity.setDynamicImage(BitmapFactoryAdapter.decodeResource(resources, LevelConfigUtils.getImageResoucesLevel(levelNow)), "keyMedalNew")
        }
        return dynamicEntity
    }

    // 段位是否上升 null即没变化 true上升  false下降
    fun isLevelUp(before: ScoreStateModel, after: ScoreStateModel): Boolean? {
        return if (after.mainRanking == before.mainRanking && after.subRanking == before.subRanking) {
            null
        } else {
            when {
                after.mainRanking > before.mainRanking -> true
                after.mainRanking < before.mainRanking -> false
                else -> after.subRanking < before.subRanking   // 子段位数值越小，段位越高
            }
        }
    }


    private fun goMatchPage() {
        finish()
        ARouter.getInstance().build(RouterConstants.ACTIVITY_RACE_MATCH_ROOM)
                .navigation()
    }

    override fun useEventBus(): Boolean {
        return false
    }

    override fun destroy() {
        super.destroy()
        mGameRuleDialog?.dismiss(false)
        mTipsDialogView?.dismiss(false)
        U.getSoundUtils().release(mTag)
    }
}
