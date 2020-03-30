package com.module.club.homepage.view

import android.content.Context
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.core.myinfo.event.MyUserInfoEvent
import com.common.core.userinfo.model.ClubMemberInfo
import com.common.log.MyLog
import com.common.player.SinglePlayer
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.component.busilib.callback.EmptyCallback
import com.kingja.loadsir.callback.Callback
import com.kingja.loadsir.core.LoadService
import com.kingja.loadsir.core.LoadSir
import com.module.RouterConstants
import com.module.club.ClubServerApi
import com.module.club.R
import com.module.club.work.ClubWorkAdapter
import com.module.club.work.WorkModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

// 家族作品
class ClubWorksView(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : ConstraintLayout(context, attrs, defStyleAttr), CoroutineScope by MainScope() {

    var clubMemberInfo: ClubMemberInfo? = null

    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

    val TAG = "ClubWorksView"
    private var offset = 0
    private val cnt = 15
    var hasMore = true
    val recycleView: RecyclerView
    private val mLoadService: LoadService<*>
    private val adapter: ClubWorkAdapter
    private val clubServerApi = ApiManager.getInstance().createService(ClubServerApi::class.java)
    private val playerTag = TAG + hashCode()
    var isPlaying = false

    init {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

        View.inflate(context, R.layout.club_tab_works_view_layout, this)
        recycleView = this.findViewById(R.id.recycler_view)

        adapter = ClubWorkAdapter(object : ClubWorkAdapter.WorkListener {
            override fun onClickPostsAvatar(position: Int, model: WorkModel?) {
                model?.let {
                    recordClick(model)
                    openOtherPersonCenter(it.userID)
                }

            }

        })
        recycleView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recycleView.adapter = adapter

        adapter.clickListener = { position, model, playView ->
            MyLog.d("lijianqun", "position = $position + : model = $model")
            if (isPlaying) {
                stopPlay(position, playView)
            } else {
                play(position, model, playView)
            }

        }
        U.getCommonUtils().setSupportsChangeAnimations(recycleView, false)
        val mLoadSir = LoadSir.Builder()
                .addCallback(EmptyCallback(R.drawable.loading_empty2, "暂无房间", "#99FFFFFF"))
                .build()
        mLoadService = mLoadSir.register(recycleView, Callback.OnReloadListener {
            loadList(0, true, null)
        })
    }


    fun recordClick(model: WorkModel?) {
        /*  if (type != TYPE_POST_PERSON) {
              model?.posts?.postsID?.let {
                  PostsStatistics.addCurClick(it.toInt())
              }
          }*/
    }

    private fun openOtherPersonCenter(userID: Int) {
        val bundle = Bundle()
        bundle.putInt("bundle_user_id", userID)
        ARouter.getInstance()
                .build(RouterConstants.ACTIVITY_OTHER_PERSON)
                .with(bundle)
                .navigation()
    }

    private fun loadList(off: Int, isClean: Boolean, callBack: ((hasMore: Boolean) -> Unit)?) {
        launch {
            val result = subscribe(RequestControl("loadClubWorkList", ControlType.CancelThis)) {
                //                roomServerApi.getPartyRoomList(off, cnt, model.gameMode)
                clubServerApi.getClubWorkList(off, cnt, clubMemberInfo?.club?.clubID!!)
            }
             if (result.errno == 0) {
                 offset = result.data.getIntValue("offset")
                 hasMore = result.data.getBooleanValue("hasMore")
                 var list: MutableList<WorkModel> = ArrayList()
                 if (isClean) {
                      list = JSON.parseArray(result.data.getString("auditingWorks"), WorkModel::class.java)
                     list?.let {
                        for (i in 0 until list.size) {
                            list[i].auditing = true
                        }
                    }
                 }
                 val works = JSON.parseArray(result.data.getString("works"), WorkModel::class.java)
                 if(!list.isNullOrEmpty() && !works.isNullOrEmpty()){
                     list.addAll(works)
                 }
                 addList(list, isClean)
             }
             if (result.errno == -2) {
                 U.getToastUtil().showShort("网络出错了，请检查网络后重试")
             }

          /*  val list: MutableList<WorkModel> = ArrayList()
            for (i in 0..19) {
                val model = WorkModel()
                model.artist = i.toString() + "1test1"
                model.nickName = "$i 1test1 "
                model.songName = "$i 1test1 "
                model.worksURL = "http://res-static.inframe.mobi/app/skr-redpacket-20190304.png"
                model.avatar = "http://res-static.inframe.mobi/app/skr-redpacket-20190304.png"
                list.add(model)
                addList(list, isClean)
            }
*/
            callBack?.invoke(hasMore)
        }
    }


    private fun addList(list: List<WorkModel>?, isClean: Boolean) {
        if (isClean) {
            adapter.mDataList.clear()
            if (!list.isNullOrEmpty()) {
                adapter.mDataList.addAll(list)
            }
            adapter.notifyDataSetChanged()
        } else {
            if (!list.isNullOrEmpty()) {
                val size = adapter.mDataList.size
                adapter.mDataList.addAll(list)
                val newSize = adapter.mDataList.size
                adapter.notifyItemRangeInserted(size, newSize - size)
            }
        }
        //列表空显示
        if (adapter.mDataList.isNullOrEmpty()) {
            mLoadService.showCallback(EmptyCallback::class.java)
        } else {
            mLoadService.showSuccess()
        }
    }


    fun loadData(flag: Boolean, callback: ((hasMore: Boolean) -> Unit)?) {
        loadList(0, true, callback)
    }

    fun loadMoreData(callback: ((hasMore: Boolean) -> Unit)?) {
        loadList(offset, false, callback)
    }


    private fun play(position: Int, model: WorkModel, playView: ImageView) {
        isPlaying = true
        playView.background = U.getDrawable(R.drawable.work_record_pause_icon)
        adapter.mCurrentPlayModel = model
        adapter.notifyDataSetChanged()
        model?.worksURL?.let {
            SinglePlayer.startPlay(playerTag, it)
        }
    }


    fun stopPlay(position: Int, playView: ImageView) {
        if (isPlaying) {
            isPlaying = false
            playView.background = U.getDrawable(R.drawable.work_record_play_icon)
            adapter.mCurrentPlayModel = null
            adapter.notifyDataSetChanged()
            SinglePlayer.pause(playerTag)
        }
    }

    fun stopPlay() {
        if (isPlaying) {
            isPlaying = false
            SinglePlayer.pause(playerTag)
        }
    }

    fun destroy() {
        stopPlay()
        cancel()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: MyUserInfoEvent.UserInfoChangeEvent) {
        loadList(0, true, null)
    }
}