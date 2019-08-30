package com.module.playways.race.match.fragment

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.alibaba.fastjson.JSON
import com.common.base.BaseFragment
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.UserInfoServerApi
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.utils.FragmentUtils
import com.common.utils.U
import com.common.view.AnimateClickListener
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.component.level.view.NormalLevelView2
import com.component.person.model.ScoreStateModel
import com.module.playways.R
import kotlinx.coroutines.launch

class RaceGuideFragment : BaseFragment() {

    lateinit var topTitle: ImageView
    lateinit var levelBackground: ImageView
    lateinit var levelView: NormalLevelView2
    lateinit var levelDescTv: ExTextView
    lateinit var beginRace: ExTextView
    lateinit var ivBack: ExImageView

    val userServerApi = ApiManager.getInstance().createService(UserInfoServerApi::class.java)

    override fun initView(): Int {
        return R.layout.race_guide_layout_fragment
    }

    override fun initData(savedInstanceState: Bundle?) {
        topTitle = rootView.findViewById(R.id.top_title)
        levelBackground = rootView.findViewById(R.id.level_background)
        levelView = rootView.findViewById(R.id.level_view)
        levelDescTv = rootView.findViewById(R.id.level_desc_tv)
        beginRace = rootView.findViewById(R.id.begin_race)
        ivBack = rootView.findViewById(R.id.iv_back)

        U.getPreferenceUtils().setSettingBoolean("is_first_race", false)

        ivBack.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                activity?.finish()
            }
        })

        beginRace.setOnClickListener(object : AnimateClickListener() {
            override fun click(view: View?) {
                U.getFragmentUtils().popFragment(this@RaceGuideFragment)
                U.getFragmentUtils().addFragment(
                        FragmentUtils.newAddParamsBuilder(activity, RaceMatchFragment::class.java)
                                .setAddToBackStack(false)
                                .setHasAnimation(true)
                                .build()
                )
            }
        })

        getMyLevel()
    }

    private fun getMyLevel() {
        ApiMethods.subscribe(userServerApi.getLevelDetail(MyUserInfoManager.getInstance().uid), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                if (result.errno == 0) {
                    result.data?.let {
                        val stateModel = JSON.parseObject(it.getString("ranking"), ScoreStateModel::class.java)
                        val raceTicketCnt = it.getLongValue("raceTicketCnt")
                        val standLightCnt = it.getLongValue("standLightCnt")
                        bindData(stateModel)
                    }
                }
            }
        }, this)
    }

    private fun bindData(stateModel: ScoreStateModel?) {
        stateModel?.let {
            levelDescTv.text = it.rankingDesc
            levelView.bindData(it.mainRanking, it.subRanking)
        }
    }

    override fun useEventBus(): Boolean {
        return false
    }
}