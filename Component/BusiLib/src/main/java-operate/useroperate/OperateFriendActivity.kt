package useroperate

import android.content.Intent
import android.os.Bundle
import com.common.base.BaseActivity
import com.common.core.R
import com.common.core.userinfo.model.UserInfoModel
import com.common.utils.FragmentUtils
import com.common.utils.U
import useroperate.fragment.OperateFriendFragment
import useroperate.inter.IOperateStub

class OperateFriendActivity : BaseActivity() {
    companion object {
        var operateList: List<IOperateStub<UserInfoModel>>? = null
        fun open(activity: BaseActivity, list: List<IOperateStub<UserInfoModel>>) {
            operateList = list
            val intent = Intent(activity, OperateFriendActivity::class.java)
            activity?.startActivity(intent)
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

    override fun useEventBus(): Boolean {
        return false
    }
}