package useroperate

import android.content.Intent
import android.os.Bundle
import com.common.base.BaseActivity
import com.common.core.R
import com.common.core.userinfo.model.UserInfoModel
import com.common.utils.FragmentUtils
import com.common.utils.U
import useroperate.def.DefaultFansOperateStub
import useroperate.def.DefaultFollowOperateStub
import useroperate.def.DefaultFriendOperateStub
import useroperate.fragment.OperateFriendFragment
import useroperate.inter.AbsRelationOperate
import useroperate.inter.IOperateStub

class OperateFriendActivity : BaseActivity() {
    companion object {
        var operateList: MutableList<IOperateStub<UserInfoModel>>? = null
        fun open(activity: BaseActivity, list: MutableList<IOperateStub<UserInfoModel>>) {
            operateList = list
            val intent = Intent(activity, OperateFriendActivity::class.java)
            activity?.startActivity(intent)
        }

        //如果所有操作和文字都一样，直接用这个
        fun open(builder: Builder) {
            operateList = mutableListOf()
            if (builder.enableFriend) {
                operateList?.add(DefaultFriendOperateStub(builder.text, builder.listener))
            }

            if (builder.enableFollow) {
                operateList?.add(DefaultFollowOperateStub(builder.text, builder.listener))
            }

            if (builder.enableFans) {
                operateList?.add(DefaultFansOperateStub(builder.text, builder.listener))
            }

            val intent = Intent(U.app(), OperateFriendActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            U.app().startActivity(intent)
        }

        class Builder {
            var enableFriend: Boolean = false
            var enableFollow: Boolean = false
            var enableFans: Boolean = false
            var text: String = "邀请"
            var listener: AbsRelationOperate.ClickListener? = null

            constructor()

            fun setEnableFriend(enableFriend: Boolean): Builder {
                this.enableFriend = enableFriend
                return this
            }

            fun setEnableFollow(enableFollow: Boolean): Builder {
                this.enableFollow = enableFollow
                return this
            }

            fun setEnableFans(enableFans: Boolean): Builder {
                this.enableFans = enableFans
                return this
            }

            fun setText(text: String): Builder {
                this.text = text
                return this
            }

            fun setListener(listener: AbsRelationOperate.ClickListener): Builder {
                this.listener = listener
                return this
            }
        }
    }

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.empty_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        // 房主想要邀请别人加入游戏
        // 打开邀请面板
        if (operateList == null || operateList?.size == 0) {
            finish()
            return
        }

        operateList?.let {
            if (it.size > 0) {
                U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(this, OperateFriendFragment::class.java)
                        .setAddToBackStack(false)
                        .setHasAnimation(false)
                        .addDataBeforeAdd(0, operateList)
                        .build())
            }
        }

        operateList = null
    }

    override fun canSlide(): Boolean {
        return false
    }

    override fun useEventBus(): Boolean {
        return false
    }
}