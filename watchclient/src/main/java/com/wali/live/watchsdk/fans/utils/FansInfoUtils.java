package com.wali.live.watchsdk.fans.utils;

import com.wali.live.proto.VFansCommonProto;
import com.wali.live.watchsdk.R;

/**
 * Created by lan on 2017/11/8.
 */
public class FansInfoUtils {
    /**
     * @colorWrong
     */
    public static int getGroupMemberLevelDrawable(int level) {
        int id = R.drawable.live_pet_group_member_level1;
        switch (level) {
            case 2:
                id = R.drawable.live_pet_group_member_level2;
                break;
            case 3:
                id = R.drawable.live_pet_group_member_level3;
                break;
            case 4:
                id = R.drawable.live_pet_group_member_level4;
                break;
            case 5:
                id = R.drawable.live_pet_group_member_level5;
                break;
            case 6:
                id = R.drawable.live_pet_group_member_level6;
                break;
            case 7:
                id = R.drawable.live_pet_group_member_level7;
                break;
            case 8:
                id = R.drawable.live_pet_group_member_level8;
                break;
        }
        return id;
    }

    public static int getImageResourcesByCharmLevelValue(int charmLevel) {
        int id;
        switch (charmLevel) {
            case 1:
                id = R.drawable.live_pet_group_level;
                break;
            case 2:
                id = R.drawable.live_pet_grade_tomorrow;
                break;
            case 3:
                id = R.drawable.live_pet_grade_trend;
                break;
            case 4:
                id = R.drawable.live_pet_grade_big;
                break;
            case 5:
                id = R.drawable.live_pet_grade_super;
                break;
            case 6:
                id = R.drawable.live_pet_grade_big_cast;
                break;
            case 7:
                id = R.drawable.live_pet_grade_superstar;
                break;
            default:
                id = R.drawable.live_pet_grade_king;
                break;
        }
        return id;
    }

    public static int getMemberRoleStringByType(int memberType) {
        int result = R.string.vfan_member_role_none;
        switch (memberType) {
            case VFansCommonProto.GroupMemType.OWNER_VALUE:
                result = R.string.vfan_member_role_owner;
                break;
            case VFansCommonProto.GroupMemType.ADMIN_VALUE:
                result = R.string.vfan_member_role_admin;
                break;
            case VFansCommonProto.GroupMemType.DEPUTY_ADMIN_VALUE:
                result = R.string.vfan_member_role_deputy_admin;
                break;
            case VFansCommonProto.GroupMemType.MASS_VALUE:
                result = R.string.vfan_member_role_mass;
                break;
            case VFansCommonProto.GroupMemType.NONE_VALUE:
                result = R.string.vfan_member_role_none;
                break;
            case VFansCommonProto.GroupMemType.BLACK_VALUE:
                result = R.string.vfan_member_role_black;
                break;
        }
        return result;
    }
}
