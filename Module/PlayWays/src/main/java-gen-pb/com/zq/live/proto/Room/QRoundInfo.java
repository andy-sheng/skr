// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: Room.proto
package com.zq.live.proto.Room;

import com.squareup.wire.FieldEncoding;
import com.squareup.wire.Message;
import com.squareup.wire.ProtoAdapter;
import com.squareup.wire.ProtoReader;
import com.squareup.wire.ProtoWriter;
import com.squareup.wire.WireField;
import com.squareup.wire.internal.Internal;
import com.zq.live.proto.Common.MusicInfo;
import com.zq.live.proto.Common.ResourceInfo;
import java.io.IOException;
import java.lang.Boolean;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.util.List;
import okio.ByteString;

public final class QRoundInfo extends Message<QRoundInfo, QRoundInfo.Builder> {
  public static final ProtoAdapter<QRoundInfo> ADAPTER = new ProtoAdapter_QRoundInfo();

  private static final long serialVersionUID = 0L;

  public static final Integer DEFAULT_USERID = 0;

  public static final Integer DEFAULT_PLAYBOOKID = 0;

  public static final Integer DEFAULT_ROUNDSEQ = 0;

  public static final Integer DEFAULT_INTROBEGINMS = 0;

  public static final Integer DEFAULT_INTROENDMS = 0;

  public static final Integer DEFAULT_SINGBEGINMS = 0;

  public static final Integer DEFAULT_SINGENDMS = 0;

  public static final EQRoundStatus DEFAULT_STATUS = EQRoundStatus.QRS_UNKNOWN;

  public static final EQRoundOverReason DEFAULT_OVERREASON = EQRoundOverReason.ROR_UNKNOWN;

  public static final EQRoundResultType DEFAULT_RESULTTYPE = EQRoundResultType.ROT_UNKNOWN;

  public static final Boolean DEFAULT_ISINCHALLENGE = false;

  public static final EWantSingType DEFAULT_WANTSINGTYPE = EWantSingType.EWST_DEFAULT;

  /**
   * 抢唱成功的玩家id
   */
  @WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  private final Integer userID;

  /**
   * 曲库id
   */
  @WireField(
      tag = 2,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  private final Integer playbookID;

  /**
   * 轮次顺序
   */
  @WireField(
      tag = 3,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  private final Integer roundSeq;

  /**
   * 导唱开始相对时间（相对于startTimeMs时间）
   */
  @WireField(
      tag = 4,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  private final Integer introBeginMs;

  /**
   * 导唱结束相对时间（相对于startTimeMs时间）
   */
  @WireField(
      tag = 5,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  private final Integer introEndMs;

  /**
   * 演唱开始相对时间（相对于startTimeMs时间）
   */
  @WireField(
      tag = 6,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  private final Integer singBeginMs;

  /**
   * 演唱结束相对时间（相对于startTimeMs时间）
   */
  @WireField(
      tag = 7,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  private final Integer singEndMs;

  /**
   * 轮次状态
   */
  @WireField(
      tag = 8,
      adapter = "com.zq.live.proto.Room.EQRoundStatus#ADAPTER"
  )
  private final EQRoundStatus status;

  /**
   * 抢唱列表。兵营告知，服务器没存，所以这个列表是没东西的。
   */
  @WireField(
      tag = 9,
      adapter = "com.zq.live.proto.Room.WantSingInfo#ADAPTER",
      label = WireField.Label.REPEATED
  )
  private final List<WantSingInfo> wantSingInfos;

  /**
   * 切换轮次原因
   */
  @WireField(
      tag = 10,
      adapter = "com.zq.live.proto.Room.EQRoundOverReason#ADAPTER"
  )
  private final EQRoundOverReason overReason;

  /**
   * 当EQRoundOverReason == ROR_MULTI_NO_PASS 演唱结果信息
   */
  @WireField(
      tag = 11,
      adapter = "com.zq.live.proto.Room.EQRoundResultType#ADAPTER"
  )
  private final EQRoundResultType resultType;

  /**
   * 本轮次的歌曲信息
   */
  @WireField(
      tag = 12,
      adapter = "com.zq.live.proto.Common.MusicInfo#ADAPTER"
  )
  private final MusicInfo music;

  /**
   * 爆灭灯列表
   */
  @WireField(
      tag = 13,
      adapter = "com.zq.live.proto.Room.QBLightMsg#ADAPTER",
      label = WireField.Label.REPEATED
  )
  private final List<QBLightMsg> bLightInfos;

  /**
   * 灭灭灯列表
   */
  @WireField(
      tag = 14,
      adapter = "com.zq.live.proto.Room.QMLightMsg#ADAPTER",
      label = WireField.Label.REPEATED
  )
  private final List<QMLightMsg> mLightInfos;

  /**
   * 机器人资源
   */
  @WireField(
      tag = 15,
      adapter = "com.zq.live.proto.Common.ResourceInfo#ADAPTER"
  )
  private final ResourceInfo skrResource;

  /**
   * 等待中用户列表
   */
  @WireField(
      tag = 16,
      adapter = "com.zq.live.proto.Room.OnlineInfo#ADAPTER",
      label = WireField.Label.REPEATED
  )
  private final List<OnlineInfo> waitUsers;

  /**
   * 当局玩的用户列表
   */
  @WireField(
      tag = 17,
      adapter = "com.zq.live.proto.Room.OnlineInfo#ADAPTER",
      label = WireField.Label.REPEATED
  )
  private final List<OnlineInfo> playUsers;

  /**
   * 是否在挑战中
   */
  @WireField(
      tag = 18,
      adapter = "com.squareup.wire.ProtoAdapter#BOOL"
  )
  private final Boolean isInChallenge;

  /**
   * 抢唱方式
   */
  @WireField(
      tag = 19,
      adapter = "com.zq.live.proto.Room.EWantSingType#ADAPTER"
  )
  private final EWantSingType wantSingType;

  /**
   * 一唱到底合唱：内部轮次信息
   */
  @WireField(
      tag = 20,
      adapter = "com.zq.live.proto.Room.QCHOInnerRoundInfo#ADAPTER",
      label = WireField.Label.REPEATED
  )
  private final List<QCHOInnerRoundInfo> CHORoundInfos;

  /**
   * 一唱到底spk：内部轮次信息
   */
  @WireField(
      tag = 21,
      adapter = "com.zq.live.proto.Room.QSPKInnerRoundInfo#ADAPTER",
      label = WireField.Label.REPEATED
  )
  private final List<QSPKInnerRoundInfo> SPKRoundInfos;

  public QRoundInfo(Integer userID, Integer playbookID, Integer roundSeq, Integer introBeginMs,
      Integer introEndMs, Integer singBeginMs, Integer singEndMs, EQRoundStatus status,
      List<WantSingInfo> wantSingInfos, EQRoundOverReason overReason, EQRoundResultType resultType,
      MusicInfo music, List<QBLightMsg> bLightInfos, List<QMLightMsg> mLightInfos,
      ResourceInfo skrResource, List<OnlineInfo> waitUsers, List<OnlineInfo> playUsers,
      Boolean isInChallenge, EWantSingType wantSingType, List<QCHOInnerRoundInfo> CHORoundInfos,
      List<QSPKInnerRoundInfo> SPKRoundInfos) {
    this(userID, playbookID, roundSeq, introBeginMs, introEndMs, singBeginMs, singEndMs, status, wantSingInfos, overReason, resultType, music, bLightInfos, mLightInfos, skrResource, waitUsers, playUsers, isInChallenge, wantSingType, CHORoundInfos, SPKRoundInfos, ByteString.EMPTY);
  }

  public QRoundInfo(Integer userID, Integer playbookID, Integer roundSeq, Integer introBeginMs,
      Integer introEndMs, Integer singBeginMs, Integer singEndMs, EQRoundStatus status,
      List<WantSingInfo> wantSingInfos, EQRoundOverReason overReason, EQRoundResultType resultType,
      MusicInfo music, List<QBLightMsg> bLightInfos, List<QMLightMsg> mLightInfos,
      ResourceInfo skrResource, List<OnlineInfo> waitUsers, List<OnlineInfo> playUsers,
      Boolean isInChallenge, EWantSingType wantSingType, List<QCHOInnerRoundInfo> CHORoundInfos,
      List<QSPKInnerRoundInfo> SPKRoundInfos, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.userID = userID;
    this.playbookID = playbookID;
    this.roundSeq = roundSeq;
    this.introBeginMs = introBeginMs;
    this.introEndMs = introEndMs;
    this.singBeginMs = singBeginMs;
    this.singEndMs = singEndMs;
    this.status = status;
    this.wantSingInfos = Internal.immutableCopyOf("wantSingInfos", wantSingInfos);
    this.overReason = overReason;
    this.resultType = resultType;
    this.music = music;
    this.bLightInfos = Internal.immutableCopyOf("bLightInfos", bLightInfos);
    this.mLightInfos = Internal.immutableCopyOf("mLightInfos", mLightInfos);
    this.skrResource = skrResource;
    this.waitUsers = Internal.immutableCopyOf("waitUsers", waitUsers);
    this.playUsers = Internal.immutableCopyOf("playUsers", playUsers);
    this.isInChallenge = isInChallenge;
    this.wantSingType = wantSingType;
    this.CHORoundInfos = Internal.immutableCopyOf("CHORoundInfos", CHORoundInfos);
    this.SPKRoundInfos = Internal.immutableCopyOf("SPKRoundInfos", SPKRoundInfos);
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.userID = userID;
    builder.playbookID = playbookID;
    builder.roundSeq = roundSeq;
    builder.introBeginMs = introBeginMs;
    builder.introEndMs = introEndMs;
    builder.singBeginMs = singBeginMs;
    builder.singEndMs = singEndMs;
    builder.status = status;
    builder.wantSingInfos = Internal.copyOf("wantSingInfos", wantSingInfos);
    builder.overReason = overReason;
    builder.resultType = resultType;
    builder.music = music;
    builder.bLightInfos = Internal.copyOf("bLightInfos", bLightInfos);
    builder.mLightInfos = Internal.copyOf("mLightInfos", mLightInfos);
    builder.skrResource = skrResource;
    builder.waitUsers = Internal.copyOf("waitUsers", waitUsers);
    builder.playUsers = Internal.copyOf("playUsers", playUsers);
    builder.isInChallenge = isInChallenge;
    builder.wantSingType = wantSingType;
    builder.CHORoundInfos = Internal.copyOf("CHORoundInfos", CHORoundInfos);
    builder.SPKRoundInfos = Internal.copyOf("SPKRoundInfos", SPKRoundInfos);
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof QRoundInfo)) return false;
    QRoundInfo o = (QRoundInfo) other;
    return unknownFields().equals(o.unknownFields())
        && Internal.equals(userID, o.userID)
        && Internal.equals(playbookID, o.playbookID)
        && Internal.equals(roundSeq, o.roundSeq)
        && Internal.equals(introBeginMs, o.introBeginMs)
        && Internal.equals(introEndMs, o.introEndMs)
        && Internal.equals(singBeginMs, o.singBeginMs)
        && Internal.equals(singEndMs, o.singEndMs)
        && Internal.equals(status, o.status)
        && wantSingInfos.equals(o.wantSingInfos)
        && Internal.equals(overReason, o.overReason)
        && Internal.equals(resultType, o.resultType)
        && Internal.equals(music, o.music)
        && bLightInfos.equals(o.bLightInfos)
        && mLightInfos.equals(o.mLightInfos)
        && Internal.equals(skrResource, o.skrResource)
        && waitUsers.equals(o.waitUsers)
        && playUsers.equals(o.playUsers)
        && Internal.equals(isInChallenge, o.isInChallenge)
        && Internal.equals(wantSingType, o.wantSingType)
        && CHORoundInfos.equals(o.CHORoundInfos)
        && SPKRoundInfos.equals(o.SPKRoundInfos);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + (userID != null ? userID.hashCode() : 0);
      result = result * 37 + (playbookID != null ? playbookID.hashCode() : 0);
      result = result * 37 + (roundSeq != null ? roundSeq.hashCode() : 0);
      result = result * 37 + (introBeginMs != null ? introBeginMs.hashCode() : 0);
      result = result * 37 + (introEndMs != null ? introEndMs.hashCode() : 0);
      result = result * 37 + (singBeginMs != null ? singBeginMs.hashCode() : 0);
      result = result * 37 + (singEndMs != null ? singEndMs.hashCode() : 0);
      result = result * 37 + (status != null ? status.hashCode() : 0);
      result = result * 37 + wantSingInfos.hashCode();
      result = result * 37 + (overReason != null ? overReason.hashCode() : 0);
      result = result * 37 + (resultType != null ? resultType.hashCode() : 0);
      result = result * 37 + (music != null ? music.hashCode() : 0);
      result = result * 37 + bLightInfos.hashCode();
      result = result * 37 + mLightInfos.hashCode();
      result = result * 37 + (skrResource != null ? skrResource.hashCode() : 0);
      result = result * 37 + waitUsers.hashCode();
      result = result * 37 + playUsers.hashCode();
      result = result * 37 + (isInChallenge != null ? isInChallenge.hashCode() : 0);
      result = result * 37 + (wantSingType != null ? wantSingType.hashCode() : 0);
      result = result * 37 + CHORoundInfos.hashCode();
      result = result * 37 + SPKRoundInfos.hashCode();
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (userID != null) builder.append(", userID=").append(userID);
    if (playbookID != null) builder.append(", playbookID=").append(playbookID);
    if (roundSeq != null) builder.append(", roundSeq=").append(roundSeq);
    if (introBeginMs != null) builder.append(", introBeginMs=").append(introBeginMs);
    if (introEndMs != null) builder.append(", introEndMs=").append(introEndMs);
    if (singBeginMs != null) builder.append(", singBeginMs=").append(singBeginMs);
    if (singEndMs != null) builder.append(", singEndMs=").append(singEndMs);
    if (status != null) builder.append(", status=").append(status);
    if (!wantSingInfos.isEmpty()) builder.append(", wantSingInfos=").append(wantSingInfos);
    if (overReason != null) builder.append(", overReason=").append(overReason);
    if (resultType != null) builder.append(", resultType=").append(resultType);
    if (music != null) builder.append(", music=").append(music);
    if (!bLightInfos.isEmpty()) builder.append(", bLightInfos=").append(bLightInfos);
    if (!mLightInfos.isEmpty()) builder.append(", mLightInfos=").append(mLightInfos);
    if (skrResource != null) builder.append(", skrResource=").append(skrResource);
    if (!waitUsers.isEmpty()) builder.append(", waitUsers=").append(waitUsers);
    if (!playUsers.isEmpty()) builder.append(", playUsers=").append(playUsers);
    if (isInChallenge != null) builder.append(", isInChallenge=").append(isInChallenge);
    if (wantSingType != null) builder.append(", wantSingType=").append(wantSingType);
    if (!CHORoundInfos.isEmpty()) builder.append(", CHORoundInfos=").append(CHORoundInfos);
    if (!SPKRoundInfos.isEmpty()) builder.append(", SPKRoundInfos=").append(SPKRoundInfos);
    return builder.replace(0, 2, "QRoundInfo{").append('}').toString();
  }

  public byte[] toByteArray() {
    return QRoundInfo.ADAPTER.encode(this);
  }

  public static final QRoundInfo parseFrom(byte[] data) throws IOException {
    QRoundInfo c = null;
       c = QRoundInfo.ADAPTER.decode(data);
    return c;
  }

  /**
   * 抢唱成功的玩家id
   */
  public Integer getUserID() {
    if(userID==null){
        return DEFAULT_USERID;
    }
    return userID;
  }

  /**
   * 曲库id
   */
  public Integer getPlaybookID() {
    if(playbookID==null){
        return DEFAULT_PLAYBOOKID;
    }
    return playbookID;
  }

  /**
   * 轮次顺序
   */
  public Integer getRoundSeq() {
    if(roundSeq==null){
        return DEFAULT_ROUNDSEQ;
    }
    return roundSeq;
  }

  /**
   * 导唱开始相对时间（相对于startTimeMs时间）
   */
  public Integer getIntroBeginMs() {
    if(introBeginMs==null){
        return DEFAULT_INTROBEGINMS;
    }
    return introBeginMs;
  }

  /**
   * 导唱结束相对时间（相对于startTimeMs时间）
   */
  public Integer getIntroEndMs() {
    if(introEndMs==null){
        return DEFAULT_INTROENDMS;
    }
    return introEndMs;
  }

  /**
   * 演唱开始相对时间（相对于startTimeMs时间）
   */
  public Integer getSingBeginMs() {
    if(singBeginMs==null){
        return DEFAULT_SINGBEGINMS;
    }
    return singBeginMs;
  }

  /**
   * 演唱结束相对时间（相对于startTimeMs时间）
   */
  public Integer getSingEndMs() {
    if(singEndMs==null){
        return DEFAULT_SINGENDMS;
    }
    return singEndMs;
  }

  /**
   * 轮次状态
   */
  public EQRoundStatus getStatus() {
    if(status==null){
        return new EQRoundStatus.Builder().build();
    }
    return status;
  }

  /**
   * 抢唱列表。兵营告知，服务器没存，所以这个列表是没东西的。
   */
  public List<WantSingInfo> getWantSingInfosList() {
    if(wantSingInfos==null){
        return new java.util.ArrayList<WantSingInfo>();
    }
    return wantSingInfos;
  }

  /**
   * 切换轮次原因
   */
  public EQRoundOverReason getOverReason() {
    if(overReason==null){
        return new EQRoundOverReason.Builder().build();
    }
    return overReason;
  }

  /**
   * 当EQRoundOverReason == ROR_MULTI_NO_PASS 演唱结果信息
   */
  public EQRoundResultType getResultType() {
    if(resultType==null){
        return new EQRoundResultType.Builder().build();
    }
    return resultType;
  }

  /**
   * 本轮次的歌曲信息
   */
  public MusicInfo getMusic() {
    if(music==null){
        return new MusicInfo.Builder().build();
    }
    return music;
  }

  /**
   * 爆灭灯列表
   */
  public List<QBLightMsg> getBLightInfosList() {
    if(bLightInfos==null){
        return new java.util.ArrayList<QBLightMsg>();
    }
    return bLightInfos;
  }

  /**
   * 灭灭灯列表
   */
  public List<QMLightMsg> getMLightInfosList() {
    if(mLightInfos==null){
        return new java.util.ArrayList<QMLightMsg>();
    }
    return mLightInfos;
  }

  /**
   * 机器人资源
   */
  public ResourceInfo getSkrResource() {
    if(skrResource==null){
        return new ResourceInfo.Builder().build();
    }
    return skrResource;
  }

  /**
   * 等待中用户列表
   */
  public List<OnlineInfo> getWaitUsersList() {
    if(waitUsers==null){
        return new java.util.ArrayList<OnlineInfo>();
    }
    return waitUsers;
  }

  /**
   * 当局玩的用户列表
   */
  public List<OnlineInfo> getPlayUsersList() {
    if(playUsers==null){
        return new java.util.ArrayList<OnlineInfo>();
    }
    return playUsers;
  }

  /**
   * 是否在挑战中
   */
  public Boolean getIsInChallenge() {
    if(isInChallenge==null){
        return DEFAULT_ISINCHALLENGE;
    }
    return isInChallenge;
  }

  /**
   * 抢唱方式
   */
  public EWantSingType getWantSingType() {
    if(wantSingType==null){
        return new EWantSingType.Builder().build();
    }
    return wantSingType;
  }

  /**
   * 一唱到底合唱：内部轮次信息
   */
  public List<QCHOInnerRoundInfo> getCHORoundInfosList() {
    if(CHORoundInfos==null){
        return new java.util.ArrayList<QCHOInnerRoundInfo>();
    }
    return CHORoundInfos;
  }

  /**
   * 一唱到底spk：内部轮次信息
   */
  public List<QSPKInnerRoundInfo> getSPKRoundInfosList() {
    if(SPKRoundInfos==null){
        return new java.util.ArrayList<QSPKInnerRoundInfo>();
    }
    return SPKRoundInfos;
  }

  /**
   * 抢唱成功的玩家id
   */
  public boolean hasUserID() {
    return userID!=null;
  }

  /**
   * 曲库id
   */
  public boolean hasPlaybookID() {
    return playbookID!=null;
  }

  /**
   * 轮次顺序
   */
  public boolean hasRoundSeq() {
    return roundSeq!=null;
  }

  /**
   * 导唱开始相对时间（相对于startTimeMs时间）
   */
  public boolean hasIntroBeginMs() {
    return introBeginMs!=null;
  }

  /**
   * 导唱结束相对时间（相对于startTimeMs时间）
   */
  public boolean hasIntroEndMs() {
    return introEndMs!=null;
  }

  /**
   * 演唱开始相对时间（相对于startTimeMs时间）
   */
  public boolean hasSingBeginMs() {
    return singBeginMs!=null;
  }

  /**
   * 演唱结束相对时间（相对于startTimeMs时间）
   */
  public boolean hasSingEndMs() {
    return singEndMs!=null;
  }

  /**
   * 轮次状态
   */
  public boolean hasStatus() {
    return status!=null;
  }

  /**
   * 抢唱列表。兵营告知，服务器没存，所以这个列表是没东西的。
   */
  public boolean hasWantSingInfosList() {
    return wantSingInfos!=null;
  }

  /**
   * 切换轮次原因
   */
  public boolean hasOverReason() {
    return overReason!=null;
  }

  /**
   * 当EQRoundOverReason == ROR_MULTI_NO_PASS 演唱结果信息
   */
  public boolean hasResultType() {
    return resultType!=null;
  }

  /**
   * 本轮次的歌曲信息
   */
  public boolean hasMusic() {
    return music!=null;
  }

  /**
   * 爆灭灯列表
   */
  public boolean hasBLightInfosList() {
    return bLightInfos!=null;
  }

  /**
   * 灭灭灯列表
   */
  public boolean hasMLightInfosList() {
    return mLightInfos!=null;
  }

  /**
   * 机器人资源
   */
  public boolean hasSkrResource() {
    return skrResource!=null;
  }

  /**
   * 等待中用户列表
   */
  public boolean hasWaitUsersList() {
    return waitUsers!=null;
  }

  /**
   * 当局玩的用户列表
   */
  public boolean hasPlayUsersList() {
    return playUsers!=null;
  }

  /**
   * 是否在挑战中
   */
  public boolean hasIsInChallenge() {
    return isInChallenge!=null;
  }

  /**
   * 抢唱方式
   */
  public boolean hasWantSingType() {
    return wantSingType!=null;
  }

  /**
   * 一唱到底合唱：内部轮次信息
   */
  public boolean hasCHORoundInfosList() {
    return CHORoundInfos!=null;
  }

  /**
   * 一唱到底spk：内部轮次信息
   */
  public boolean hasSPKRoundInfosList() {
    return SPKRoundInfos!=null;
  }

  public static final class Builder extends Message.Builder<QRoundInfo, Builder> {
    private Integer userID;

    private Integer playbookID;

    private Integer roundSeq;

    private Integer introBeginMs;

    private Integer introEndMs;

    private Integer singBeginMs;

    private Integer singEndMs;

    private EQRoundStatus status;

    private List<WantSingInfo> wantSingInfos;

    private EQRoundOverReason overReason;

    private EQRoundResultType resultType;

    private MusicInfo music;

    private List<QBLightMsg> bLightInfos;

    private List<QMLightMsg> mLightInfos;

    private ResourceInfo skrResource;

    private List<OnlineInfo> waitUsers;

    private List<OnlineInfo> playUsers;

    private Boolean isInChallenge;

    private EWantSingType wantSingType;

    private List<QCHOInnerRoundInfo> CHORoundInfos;

    private List<QSPKInnerRoundInfo> SPKRoundInfos;

    public Builder() {
      wantSingInfos = Internal.newMutableList();
      bLightInfos = Internal.newMutableList();
      mLightInfos = Internal.newMutableList();
      waitUsers = Internal.newMutableList();
      playUsers = Internal.newMutableList();
      CHORoundInfos = Internal.newMutableList();
      SPKRoundInfos = Internal.newMutableList();
    }

    /**
     * 抢唱成功的玩家id
     */
    public Builder setUserID(Integer userID) {
      this.userID = userID;
      return this;
    }

    /**
     * 曲库id
     */
    public Builder setPlaybookID(Integer playbookID) {
      this.playbookID = playbookID;
      return this;
    }

    /**
     * 轮次顺序
     */
    public Builder setRoundSeq(Integer roundSeq) {
      this.roundSeq = roundSeq;
      return this;
    }

    /**
     * 导唱开始相对时间（相对于startTimeMs时间）
     */
    public Builder setIntroBeginMs(Integer introBeginMs) {
      this.introBeginMs = introBeginMs;
      return this;
    }

    /**
     * 导唱结束相对时间（相对于startTimeMs时间）
     */
    public Builder setIntroEndMs(Integer introEndMs) {
      this.introEndMs = introEndMs;
      return this;
    }

    /**
     * 演唱开始相对时间（相对于startTimeMs时间）
     */
    public Builder setSingBeginMs(Integer singBeginMs) {
      this.singBeginMs = singBeginMs;
      return this;
    }

    /**
     * 演唱结束相对时间（相对于startTimeMs时间）
     */
    public Builder setSingEndMs(Integer singEndMs) {
      this.singEndMs = singEndMs;
      return this;
    }

    /**
     * 轮次状态
     */
    public Builder setStatus(EQRoundStatus status) {
      this.status = status;
      return this;
    }

    /**
     * 抢唱列表。兵营告知，服务器没存，所以这个列表是没东西的。
     */
    public Builder addAllWantSingInfos(List<WantSingInfo> wantSingInfos) {
      Internal.checkElementsNotNull(wantSingInfos);
      this.wantSingInfos = wantSingInfos;
      return this;
    }

    /**
     * 切换轮次原因
     */
    public Builder setOverReason(EQRoundOverReason overReason) {
      this.overReason = overReason;
      return this;
    }

    /**
     * 当EQRoundOverReason == ROR_MULTI_NO_PASS 演唱结果信息
     */
    public Builder setResultType(EQRoundResultType resultType) {
      this.resultType = resultType;
      return this;
    }

    /**
     * 本轮次的歌曲信息
     */
    public Builder setMusic(MusicInfo music) {
      this.music = music;
      return this;
    }

    /**
     * 爆灭灯列表
     */
    public Builder addAllBLightInfos(List<QBLightMsg> bLightInfos) {
      Internal.checkElementsNotNull(bLightInfos);
      this.bLightInfos = bLightInfos;
      return this;
    }

    /**
     * 灭灭灯列表
     */
    public Builder addAllMLightInfos(List<QMLightMsg> mLightInfos) {
      Internal.checkElementsNotNull(mLightInfos);
      this.mLightInfos = mLightInfos;
      return this;
    }

    /**
     * 机器人资源
     */
    public Builder setSkrResource(ResourceInfo skrResource) {
      this.skrResource = skrResource;
      return this;
    }

    /**
     * 等待中用户列表
     */
    public Builder addAllWaitUsers(List<OnlineInfo> waitUsers) {
      Internal.checkElementsNotNull(waitUsers);
      this.waitUsers = waitUsers;
      return this;
    }

    /**
     * 当局玩的用户列表
     */
    public Builder addAllPlayUsers(List<OnlineInfo> playUsers) {
      Internal.checkElementsNotNull(playUsers);
      this.playUsers = playUsers;
      return this;
    }

    /**
     * 是否在挑战中
     */
    public Builder setIsInChallenge(Boolean isInChallenge) {
      this.isInChallenge = isInChallenge;
      return this;
    }

    /**
     * 抢唱方式
     */
    public Builder setWantSingType(EWantSingType wantSingType) {
      this.wantSingType = wantSingType;
      return this;
    }

    /**
     * 一唱到底合唱：内部轮次信息
     */
    public Builder addAllCHORoundInfos(List<QCHOInnerRoundInfo> CHORoundInfos) {
      Internal.checkElementsNotNull(CHORoundInfos);
      this.CHORoundInfos = CHORoundInfos;
      return this;
    }

    /**
     * 一唱到底spk：内部轮次信息
     */
    public Builder addAllSPKRoundInfos(List<QSPKInnerRoundInfo> SPKRoundInfos) {
      Internal.checkElementsNotNull(SPKRoundInfos);
      this.SPKRoundInfos = SPKRoundInfos;
      return this;
    }

    @Override
    public QRoundInfo build() {
      return new QRoundInfo(userID, playbookID, roundSeq, introBeginMs, introEndMs, singBeginMs, singEndMs, status, wantSingInfos, overReason, resultType, music, bLightInfos, mLightInfos, skrResource, waitUsers, playUsers, isInChallenge, wantSingType, CHORoundInfos, SPKRoundInfos, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_QRoundInfo extends ProtoAdapter<QRoundInfo> {
    public ProtoAdapter_QRoundInfo() {
      super(FieldEncoding.LENGTH_DELIMITED, QRoundInfo.class);
    }

    @Override
    public int encodedSize(QRoundInfo value) {
      return ProtoAdapter.UINT32.encodedSizeWithTag(1, value.userID)
          + ProtoAdapter.UINT32.encodedSizeWithTag(2, value.playbookID)
          + ProtoAdapter.UINT32.encodedSizeWithTag(3, value.roundSeq)
          + ProtoAdapter.UINT32.encodedSizeWithTag(4, value.introBeginMs)
          + ProtoAdapter.UINT32.encodedSizeWithTag(5, value.introEndMs)
          + ProtoAdapter.UINT32.encodedSizeWithTag(6, value.singBeginMs)
          + ProtoAdapter.UINT32.encodedSizeWithTag(7, value.singEndMs)
          + EQRoundStatus.ADAPTER.encodedSizeWithTag(8, value.status)
          + WantSingInfo.ADAPTER.asRepeated().encodedSizeWithTag(9, value.wantSingInfos)
          + EQRoundOverReason.ADAPTER.encodedSizeWithTag(10, value.overReason)
          + EQRoundResultType.ADAPTER.encodedSizeWithTag(11, value.resultType)
          + MusicInfo.ADAPTER.encodedSizeWithTag(12, value.music)
          + QBLightMsg.ADAPTER.asRepeated().encodedSizeWithTag(13, value.bLightInfos)
          + QMLightMsg.ADAPTER.asRepeated().encodedSizeWithTag(14, value.mLightInfos)
          + ResourceInfo.ADAPTER.encodedSizeWithTag(15, value.skrResource)
          + OnlineInfo.ADAPTER.asRepeated().encodedSizeWithTag(16, value.waitUsers)
          + OnlineInfo.ADAPTER.asRepeated().encodedSizeWithTag(17, value.playUsers)
          + ProtoAdapter.BOOL.encodedSizeWithTag(18, value.isInChallenge)
          + EWantSingType.ADAPTER.encodedSizeWithTag(19, value.wantSingType)
          + QCHOInnerRoundInfo.ADAPTER.asRepeated().encodedSizeWithTag(20, value.CHORoundInfos)
          + QSPKInnerRoundInfo.ADAPTER.asRepeated().encodedSizeWithTag(21, value.SPKRoundInfos)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, QRoundInfo value) throws IOException {
      ProtoAdapter.UINT32.encodeWithTag(writer, 1, value.userID);
      ProtoAdapter.UINT32.encodeWithTag(writer, 2, value.playbookID);
      ProtoAdapter.UINT32.encodeWithTag(writer, 3, value.roundSeq);
      ProtoAdapter.UINT32.encodeWithTag(writer, 4, value.introBeginMs);
      ProtoAdapter.UINT32.encodeWithTag(writer, 5, value.introEndMs);
      ProtoAdapter.UINT32.encodeWithTag(writer, 6, value.singBeginMs);
      ProtoAdapter.UINT32.encodeWithTag(writer, 7, value.singEndMs);
      EQRoundStatus.ADAPTER.encodeWithTag(writer, 8, value.status);
      WantSingInfo.ADAPTER.asRepeated().encodeWithTag(writer, 9, value.wantSingInfos);
      EQRoundOverReason.ADAPTER.encodeWithTag(writer, 10, value.overReason);
      EQRoundResultType.ADAPTER.encodeWithTag(writer, 11, value.resultType);
      MusicInfo.ADAPTER.encodeWithTag(writer, 12, value.music);
      QBLightMsg.ADAPTER.asRepeated().encodeWithTag(writer, 13, value.bLightInfos);
      QMLightMsg.ADAPTER.asRepeated().encodeWithTag(writer, 14, value.mLightInfos);
      ResourceInfo.ADAPTER.encodeWithTag(writer, 15, value.skrResource);
      OnlineInfo.ADAPTER.asRepeated().encodeWithTag(writer, 16, value.waitUsers);
      OnlineInfo.ADAPTER.asRepeated().encodeWithTag(writer, 17, value.playUsers);
      ProtoAdapter.BOOL.encodeWithTag(writer, 18, value.isInChallenge);
      EWantSingType.ADAPTER.encodeWithTag(writer, 19, value.wantSingType);
      QCHOInnerRoundInfo.ADAPTER.asRepeated().encodeWithTag(writer, 20, value.CHORoundInfos);
      QSPKInnerRoundInfo.ADAPTER.asRepeated().encodeWithTag(writer, 21, value.SPKRoundInfos);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public QRoundInfo decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.setUserID(ProtoAdapter.UINT32.decode(reader)); break;
          case 2: builder.setPlaybookID(ProtoAdapter.UINT32.decode(reader)); break;
          case 3: builder.setRoundSeq(ProtoAdapter.UINT32.decode(reader)); break;
          case 4: builder.setIntroBeginMs(ProtoAdapter.UINT32.decode(reader)); break;
          case 5: builder.setIntroEndMs(ProtoAdapter.UINT32.decode(reader)); break;
          case 6: builder.setSingBeginMs(ProtoAdapter.UINT32.decode(reader)); break;
          case 7: builder.setSingEndMs(ProtoAdapter.UINT32.decode(reader)); break;
          case 8: {
            try {
              builder.setStatus(EQRoundStatus.ADAPTER.decode(reader));
            } catch (ProtoAdapter.EnumConstantNotFoundException e) {
              builder.addUnknownField(tag, FieldEncoding.VARINT, (long) e.value);
            }
            break;
          }
          case 9: builder.wantSingInfos.add(WantSingInfo.ADAPTER.decode(reader)); break;
          case 10: {
            try {
              builder.setOverReason(EQRoundOverReason.ADAPTER.decode(reader));
            } catch (ProtoAdapter.EnumConstantNotFoundException e) {
              builder.addUnknownField(tag, FieldEncoding.VARINT, (long) e.value);
            }
            break;
          }
          case 11: {
            try {
              builder.setResultType(EQRoundResultType.ADAPTER.decode(reader));
            } catch (ProtoAdapter.EnumConstantNotFoundException e) {
              builder.addUnknownField(tag, FieldEncoding.VARINT, (long) e.value);
            }
            break;
          }
          case 12: builder.setMusic(MusicInfo.ADAPTER.decode(reader)); break;
          case 13: builder.bLightInfos.add(QBLightMsg.ADAPTER.decode(reader)); break;
          case 14: builder.mLightInfos.add(QMLightMsg.ADAPTER.decode(reader)); break;
          case 15: builder.setSkrResource(ResourceInfo.ADAPTER.decode(reader)); break;
          case 16: builder.waitUsers.add(OnlineInfo.ADAPTER.decode(reader)); break;
          case 17: builder.playUsers.add(OnlineInfo.ADAPTER.decode(reader)); break;
          case 18: builder.setIsInChallenge(ProtoAdapter.BOOL.decode(reader)); break;
          case 19: {
            try {
              builder.setWantSingType(EWantSingType.ADAPTER.decode(reader));
            } catch (ProtoAdapter.EnumConstantNotFoundException e) {
              builder.addUnknownField(tag, FieldEncoding.VARINT, (long) e.value);
            }
            break;
          }
          case 20: builder.CHORoundInfos.add(QCHOInnerRoundInfo.ADAPTER.decode(reader)); break;
          case 21: builder.SPKRoundInfos.add(QSPKInnerRoundInfo.ADAPTER.decode(reader)); break;
          default: {
            FieldEncoding fieldEncoding = reader.peekFieldEncoding();
            Object value = fieldEncoding.rawProtoAdapter().decode(reader);
            builder.addUnknownField(tag, fieldEncoding, value);
          }
        }
      }
      reader.endMessage(token);
      return builder.build();
    }

    @Override
    public QRoundInfo redact(QRoundInfo value) {
      Builder builder = value.newBuilder();
      Internal.redactElements(builder.wantSingInfos, WantSingInfo.ADAPTER);
      if (builder.music != null) builder.music = MusicInfo.ADAPTER.redact(builder.music);
      Internal.redactElements(builder.bLightInfos, QBLightMsg.ADAPTER);
      Internal.redactElements(builder.mLightInfos, QMLightMsg.ADAPTER);
      if (builder.skrResource != null) builder.skrResource = ResourceInfo.ADAPTER.redact(builder.skrResource);
      Internal.redactElements(builder.waitUsers, OnlineInfo.ADAPTER);
      Internal.redactElements(builder.playUsers, OnlineInfo.ADAPTER);
      Internal.redactElements(builder.CHORoundInfos, QCHOInnerRoundInfo.ADAPTER);
      Internal.redactElements(builder.SPKRoundInfos, QSPKInnerRoundInfo.ADAPTER);
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}
