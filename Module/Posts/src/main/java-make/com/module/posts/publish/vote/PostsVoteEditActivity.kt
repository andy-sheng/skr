package com.module.posts.publish.vote

import android.app.Activity
import android.content.Intent
import android.opengl.Visibility
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.common.player.SinglePlayer
import com.common.recorder.MyMediaRecorder
import com.common.utils.U
import com.common.view.DiffuseView
import com.common.view.countdown.CircleCountDownView
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.common.view.titlebar.CommonTitleBar
import com.module.RouterConstants
import com.module.posts.R
import com.module.posts.publish.PostsPublishModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Route(path = RouterConstants.ACTIVITY_POSTS_VOTE_EDIT)
class PostsVoteEditActivity : BaseActivity() {
    companion object {
        const val REQ_CODE_VOTE_EDIT = 12
    }

    lateinit var mainActContainer: ConstraintLayout
    lateinit var titleBar: CommonTitleBar
    lateinit var voteRv: RecyclerView
    lateinit var divideLine: View
    lateinit var addVoteTv: ExTextView

    lateinit var voteAdapter: PostsVoteEditAdapter

    lateinit var model: PostsPublishModel

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.posts_vote_edit_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        model = intent.getSerializableExtra("model") as PostsPublishModel
        mainActContainer = findViewById(R.id.main_act_container)
        titleBar = findViewById(R.id.title_bar)
        voteRv = findViewById(R.id.vote_rv)
        divideLine = findViewById(R.id.divide_line)
        addVoteTv = findViewById(R.id.add_vote_tv)

        voteAdapter = PostsVoteEditAdapter()
        voteRv.layoutManager = LinearLayoutManager(this@PostsVoteEditActivity)
        voteRv.adapter = voteAdapter

        for (v in model.voteList) {
            voteAdapter.dataList.add(EditContentModel(v))
        }
        while (voteAdapter.dataList.size < 2) {
            voteAdapter.dataList.add(EditContentModel(""))
        }
        voteAdapter.delClickListener = { model, pos ->
            voteAdapter.dataList.removeAt(pos)
            voteAdapter.notifyDataSetChanged()
            if (voteAdapter.dataList.size < 4) {
                addVoteTv.visibility = View.VISIBLE
            }
        }
        addVoteTv.setOnClickListener {
            voteAdapter.dataList.add(EditContentModel(""))
            voteAdapter.notifyItemInserted(voteAdapter.dataList.size)
            if (voteAdapter.dataList.size >= 4) {
                addVoteTv.visibility = View.GONE
            }
        }
        titleBar.leftImageButton.setOnClickListener {
            finish()
        }

        titleBar.rightTextView.setOnClickListener {
            // 先判断数据是否合法
            val l = ArrayList<String>()
            for (v in voteAdapter.dataList) {
                if (v.content.isNotEmpty()) {
                    l.add(v.content)
                }
            }
            if (l.size < 2) {
                U.getToastUtil().showShort("投票至少要两个选项哦")
                return@setOnClickListener
            }
            setResult(Activity.RESULT_OK, Intent().apply {
                putStringArrayListExtra("vote_list", l)
            })
            finish()
        }
    }

    override fun resizeLayoutSelfWhenKeybordShow(): Boolean {
        return true
    }

    override fun useEventBus(): Boolean {
        return false
    }
}
