package com.common.core.userinfo.model

import com.alibaba.fastjson.annotation.JSONField
import com.zq.live.proto.Common.EClubMemberRoleType
import java.io.Serializable

// 成员信息
class ClubMemberInfo : Serializable {
    @JSONField(name = "club")
    var club: ClubInfo? = null
    @JSONField(name = "roleType")
    var roleType: Int = 0
    @JSONField(name = "roleDesc")
    var roleDesc: String = ""

    companion object {
        fun parseFromPB(userClubInfo: com.zq.live.proto.Common.UserClubInfo): ClubMemberInfo {
            val result = ClubMemberInfo()
            result.club = ClubInfo.parseFromPB(userClubInfo.club)
            result.roleType = userClubInfo.roleType.value
            result.roleDesc = userClubInfo.roleDesc
            return result
        }

        fun toUserClubInfoPB(memberInfo: ClubMemberInfo?): com.zq.live.proto.Common.UserClubInfo? {
            return if (memberInfo != null) {
                com.zq.live.proto.Common.UserClubInfo.Builder()
                        .setClub(ClubInfo.toClubInfoPB(memberInfo.club))
                        .setRoleDesc(memberInfo.roleDesc)
                        .setRoleType(EClubMemberRoleType.fromValue(memberInfo.roleType))
                        .build()
            } else {
                null
            }
        }
    }

    override fun toString(): String {
        return "ClubMemberInfo(club=$club, roleType=$roleType, roleDesc='$roleDesc')"
    }
}

// 家族信息
class ClubInfo : Serializable {
    @JSONField(name = "clubID")
    var clubID: Int = 0          //家族ID
    @JSONField(name = "name")
    var name: String = ""        //家族名字
    @JSONField(name = "logo")
    var logo: String = ""        //家族标志
    @JSONField(name = "desc")
    var desc: String = ""        //家族简介
    @JSONField(name = "notice")
    var notice: String = ""      //家族公告
    @JSONField(name = "memberCnt")
    var memberCnt: Int = 0       //家族人数
    @JSONField(name = "hot")
    var hot: Int = 0             //家族人气

    companion object {
        fun parseFromPB(clubInfo: com.zq.live.proto.Common.ClubInfo): ClubInfo {
            val result = ClubInfo()
            result.clubID = clubInfo.clubID
            result.name = clubInfo.name
            result.logo = clubInfo.logo
            result.desc = clubInfo.desc
            result.notice = clubInfo.notice
            result.memberCnt = clubInfo.memberCnt
            result.hot = clubInfo.hot
            return result
        }

        fun toClubInfoPB(clubInfo: ClubInfo?): com.zq.live.proto.Common.ClubInfo? {
            return if (clubInfo != null) {
                com.zq.live.proto.Common.ClubInfo.Builder()
                        .setClubID(clubInfo.clubID)
                        .setName(clubInfo.name)
                        .setLogo(clubInfo.logo)
                        .setDesc(clubInfo.desc)
                        .setNotice(clubInfo.notice)
                        .setMemberCnt(clubInfo.memberCnt)
                        .setHot(clubInfo.hot)
                        .build()
            } else {
                null
            }

        }
    }

    override fun toString(): String {
        return "ClubInfo(clubID=$clubID, name='$name', logo='$logo', desc='$desc', notice='$notice', memberCnt=$memberCnt, hot=$hot)"
    }
}