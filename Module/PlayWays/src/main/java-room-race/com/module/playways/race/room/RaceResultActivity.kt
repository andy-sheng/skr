package com.module.playways.race.room

import android.os.Bundle
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
import com.component.level.view.NormalLevelView2
import com.glidebitmappool.BitmapFactoryAdapter
import com.module.RouterConstants
import com.module.playways.R
import com.module.playways.listener.AnimationListener
import com.module.playways.race.RaceRoomServerApi
import com.module.playways.race.room.model.LevelResultModel
import com.module.playways.race.room.model.SaveRankModel
import com.opensource.svgaplayer.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Route(path = RouterConstants.ACTIVITY_RACE_RESULT)
class RaceResultActivity : BaseActivity() {

    val mTag = "RaceResultActivity"

    lateinit var changeTv: TextView
    lateinit var circleView: CircleCountDownView
    lateinit var levelView: NormalLevelView2
    lateinit var levelSvga: SVGAImageView
    lateinit var levelDescTv: ExTextView
    lateinit var descTv: TextView
    lateinit var countDownTv: TextView
    lateinit var playAgainTv: ExTextView
    lateinit var ivBack: ExImageView


    lateinit var levelSave: CircleCountDownView
    lateinit var levelSolid: ExImageView
    lateinit var levelMedia: ImageView
    lateinit var levelSaveDesc: TextView

    lateinit var vipLevelSave: CircleCountDownView
    lateinit var vipLevelSolid: ExImageView
    lateinit var vipLevelMedia: ImageView
    lateinit var vipLevelDesc: TextView

    val raceRoomServerApi = ApiManager.getInstance().createService(RaceRoomServerApi::class.java)

    var roomID: Int = -1
    var roundSeq: Int = -1

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
        countDownTv = findViewById(R.id.count_down_tv)
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

        ivBack.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                finish()
            }
        })

        playAgainTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                goMatchPage()
            }
        })

        descTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_WEB)
                        .withString(RouterConstants.KEY_WEB_URL, "https://fe.inframe.mobi/pages/banner/2p8p3gf3ujzxsw97z.html")
                        .navigation()
            }
        })

        getResult()
        U.getSoundUtils().preLoad(mTag, R.raw.newrank_resultpage)
        launch {
            delay(200)
            U.getSoundUtils().play(mTag, R.raw.newrank_resultpage)
        }
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

    private fun showResult(raceResultModel: LevelResultModel) {
        descTv.text = "距离下次升段还需${raceResultModel.gap}经验"
        if (raceResultModel.get >= 0) {
            changeTv.text = "+${raceResultModel.get}"
        } else {
            changeTv.text = raceResultModel.get.toString()
        }

        if (!raceResultModel.states.isNullOrEmpty() && raceResultModel.states?.size == 3) {
            // 初始化数据
            val begin = raceResultModel.states?.get(0)!!
            val middle = raceResultModel.states?.get(1)!!
            val end = raceResultModel.states?.get(2)!!

            levelView.bindData(begin.mainRanking, begin.subRanking)
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
            levelView.bindData(raceResultModel.getLastState()?.mainRanking
                    ?: 0, raceResultModel.getLastState()?.subRanking ?: 0)
        }

        launch {
            repeat(8) {
                countDownTv.text = "${8 - it}后自动进入下一场挑战"
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
                            .withString("url", ApiManager.getInstance().findRealUrlByChannel("https://app.inframe.mobi/user/vip?title=1"))
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

                    levelView.bindData(after.mainRanking, after.subRanking)
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
    }

    private fun requestDynamicBitmapItem(levelBefore: Int, subLevelBefore: Int, levelNow: Int, sublevelNow: Int): SVGADynamicEntity {
        val dynamicEntity = SVGADynamicEntity()
        if (LevelConfigUtils.getImageResoucesSubLevel(levelBefore, subLevelBefore) != 0) {
            dynamicEntity.setDynamicImage(BitmapFactoryAdapter.decodeResource(resources, LevelConfigUtils.getImageResoucesSubLevel(levelBefore, subLevelBefore)), "keyLevelBefore")
        }
        if (LevelConfigUtils.getImageResoucesLevel(levelBefore) != 0) {
            dynamicEntity.setDynamicImage(BitmapFactoryAdapter.decodeResource(resources, LevelConfigUtils.getImageResoucesLevel(levelBefore)), "keyMedalBefore")
        }

        if (LevelConfigUtils.getImageResoucesSubLevel(levelNow, sublevelNow) != 0) {
            dynamicEntity.setDynamicImage(BitmapFactoryAdapter.decodeResource(resources, LevelConfigUtils.getImageResoucesSubLevel(levelNow, sublevelNow)), "keyLevelNew")
        }

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
        U.getSoundUtils().release(mTag)
    }
}
