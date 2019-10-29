// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: Common.proto
package com.zq.live.proto.Common;

import com.squareup.wire.FieldEncoding;
import com.squareup.wire.Message;
import com.squareup.wire.ProtoAdapter;
import com.squareup.wire.ProtoReader;
import com.squareup.wire.ProtoWriter;
import com.squareup.wire.WireField;
import com.squareup.wire.internal.Internal;
import java.io.IOException;
import java.lang.Boolean;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import okio.ByteString;

/**
 * java -jar -Dfile.encoding=UTF-8 ./proto/wire-compiler-2.3.0-SNAPSHOT-jar-with-dependencies.jar \
 * --proto_path=./proto --java_out=./commoncore/src/main/java-gen-pb/ Common.proto
 * 用户信息
 */
public final class UserInfo extends Message<UserInfo, UserInfo.Builder> {
  public static final ProtoAdapter<UserInfo> ADAPTER = new ProtoAdapter_UserInfo();

  private static final long serialVersionUID = 0L;

  public static final Integer DEFAULT_USERID = 0;

  public static final String DEFAULT_NICKNAME = "";

  public static final String DEFAULT_AVATAR = "";

  public static final ESex DEFAULT_SEX = ESex.SX_UNKNOWN;

  public static final String DEFAULT_DESCRIPTION = "";

  public static final Boolean DEFAULT_ISSYSTEM = false;

  public static final Integer DEFAULT_MAINLEVEL = 0;

  /**
   * 用户ID
   */
  @WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  private final Integer userID;

  /**
   * 发信者昵称
   */
  @WireField(
      tag = 2,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  private final String nickName;

  /**
   * 头像地址
   */
  @WireField(
      tag = 3,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  private final String avatar;

  /**
   * 性别
   */
  @WireField(
      tag = 4,
      adapter = "com.zq.live.proto.Common.ESex#ADAPTER"
  )
  private final ESex sex;

  /**
   * 个人描述
   */
  @WireField(
      tag = 5,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  private final String description;

  /**
   * 是否为系统
   */
  @WireField(
      tag = 6,
      adapter = "com.squareup.wire.ProtoAdapter#BOOL"
  )
  private final Boolean isSystem;

  /**
   * 主段位
   */
  @WireField(
      tag = 7,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  private final Integer mainLevel;

  /**
   * vip类型
   */
  @WireField(
      tag = 8,
      adapter = "com.zq.live.proto.Common.VipInfo#ADAPTER"
  )
  private final VipInfo vipInfo;

  /**
   * 排位信息
   */
  @WireField(
      tag = 9,
      adapter = "com.zq.live.proto.Common.UserRanking#ADAPTER"
  )
  private final UserRanking ranking;

  /**
   * 会员信息
   */
  @WireField(
      tag = 10,
      adapter = "com.zq.live.proto.Common.HonorInfo#ADAPTER"
  )
  private final HonorInfo honorInfo;

  public UserInfo(Integer userID, String nickName, String avatar, ESex sex, String description,
      Boolean isSystem, Integer mainLevel, VipInfo vipInfo, UserRanking ranking,
      HonorInfo honorInfo) {
    this(userID, nickName, avatar, sex, description, isSystem, mainLevel, vipInfo, ranking, honorInfo, ByteString.EMPTY);
  }

  public UserInfo(Integer userID, String nickName, String avatar, ESex sex, String description,
      Boolean isSystem, Integer mainLevel, VipInfo vipInfo, UserRanking ranking,
      HonorInfo honorInfo, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.userID = userID;
    this.nickName = nickName;
    this.avatar = avatar;
    this.sex = sex;
    this.description = description;
    this.isSystem = isSystem;
    this.mainLevel = mainLevel;
    this.vipInfo = vipInfo;
    this.ranking = ranking;
    this.honorInfo = honorInfo;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.userID = userID;
    builder.nickName = nickName;
    builder.avatar = avatar;
    builder.sex = sex;
    builder.description = description;
    builder.isSystem = isSystem;
    builder.mainLevel = mainLevel;
    builder.vipInfo = vipInfo;
    builder.ranking = ranking;
    builder.honorInfo = honorInfo;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof UserInfo)) return false;
    UserInfo o = (UserInfo) other;
    return unknownFields().equals(o.unknownFields())
        && Internal.equals(userID, o.userID)
        && Internal.equals(nickName, o.nickName)
        && Internal.equals(avatar, o.avatar)
        && Internal.equals(sex, o.sex)
        && Internal.equals(description, o.description)
        && Internal.equals(isSystem, o.isSystem)
        && Internal.equals(mainLevel, o.mainLevel)
        && Internal.equals(vipInfo, o.vipInfo)
        && Internal.equals(ranking, o.ranking)
        && Internal.equals(honorInfo, o.honorInfo);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + (userID != null ? userID.hashCode() : 0);
      result = result * 37 + (nickName != null ? nickName.hashCode() : 0);
      result = result * 37 + (avatar != null ? avatar.hashCode() : 0);
      result = result * 37 + (sex != null ? sex.hashCode() : 0);
      result = result * 37 + (description != null ? description.hashCode() : 0);
      result = result * 37 + (isSystem != null ? isSystem.hashCode() : 0);
      result = result * 37 + (mainLevel != null ? mainLevel.hashCode() : 0);
      result = result * 37 + (vipInfo != null ? vipInfo.hashCode() : 0);
      result = result * 37 + (ranking != null ? ranking.hashCode() : 0);
      result = result * 37 + (honorInfo != null ? honorInfo.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (userID != null) builder.append(", userID=").append(userID);
    if (nickName != null) builder.append(", nickName=").append(nickName);
    if (avatar != null) builder.append(", avatar=").append(avatar);
    if (sex != null) builder.append(", sex=").append(sex);
    if (description != null) builder.append(", description=").append(description);
    if (isSystem != null) builder.append(", isSystem=").append(isSystem);
    if (mainLevel != null) builder.append(", mainLevel=").append(mainLevel);
    if (vipInfo != null) builder.append(", vipInfo=").append(vipInfo);
    if (ranking != null) builder.append(", ranking=").append(ranking);
    if (honorInfo != null) builder.append(", honorInfo=").append(honorInfo);
    return builder.replace(0, 2, "UserInfo{").append('}').toString();
  }

  public byte[] toByteArray() {
    return UserInfo.ADAPTER.encode(this);
  }

  public static final UserInfo parseFrom(byte[] data) throws IOException {
    UserInfo c = null;
       c = UserInfo.ADAPTER.decode(data);
    return c;
  }

  /**
   * 用户ID
   */
  public Integer getUserID() {
    if(userID==null){
        return DEFAULT_USERID;
    }
    return userID;
  }

  /**
   * 发信者昵称
   */
  public String getNickName() {
    if(nickName==null){
        return DEFAULT_NICKNAME;
    }
    return nickName;
  }

  /**
   * 头像地址
   */
  public String getAvatar() {
    if(avatar==null){
        return DEFAULT_AVATAR;
    }
    return avatar;
  }

  /**
   * 性别
   */
  public ESex getSex() {
    if(sex==null){
        return new ESex.Builder().build();
    }
    return sex;
  }

  /**
   * 个人描述
   */
  public String getDescription() {
    if(description==null){
        return DEFAULT_DESCRIPTION;
    }
    return description;
  }

  /**
   * 是否为系统
   */
  public Boolean getIsSystem() {
    if(isSystem==null){
        return DEFAULT_ISSYSTEM;
    }
    return isSystem;
  }

  /**
   * 主段位
   */
  public Integer getMainLevel() {
    if(mainLevel==null){
        return DEFAULT_MAINLEVEL;
    }
    return mainLevel;
  }

  /**
   * vip类型
   */
  public VipInfo getVipInfo() {
    if(vipInfo==null){
        return new VipInfo.Builder().build();
    }
    return vipInfo;
  }

  /**
   * 排位信息
   */
  public UserRanking getRanking() {
    if(ranking==null){
        return new UserRanking.Builder().build();
    }
    return ranking;
  }

  /**
   * 会员信息
   */
  public HonorInfo getHonorInfo() {
    if(honorInfo==null){
        return new HonorInfo.Builder().build();
    }
    return honorInfo;
  }

  /**
   * 用户ID
   */
  public boolean hasUserID() {
    return userID!=null;
  }

  /**
   * 发信者昵称
   */
  public boolean hasNickName() {
    return nickName!=null;
  }

  /**
   * 头像地址
   */
  public boolean hasAvatar() {
    return avatar!=null;
  }

  /**
   * 性别
   */
  public boolean hasSex() {
    return sex!=null;
  }

  /**
   * 个人描述
   */
  public boolean hasDescription() {
    return description!=null;
  }

  /**
   * 是否为系统
   */
  public boolean hasIsSystem() {
    return isSystem!=null;
  }

  /**
   * 主段位
   */
  public boolean hasMainLevel() {
    return mainLevel!=null;
  }

  /**
   * vip类型
   */
  public boolean hasVipInfo() {
    return vipInfo!=null;
  }

  /**
   * 排位信息
   */
  public boolean hasRanking() {
    return ranking!=null;
  }

  /**
   * 会员信息
   */
  public boolean hasHonorInfo() {
    return honorInfo!=null;
  }

  public static final class Builder extends Message.Builder<UserInfo, Builder> {
    private Integer userID;

    private String nickName;

    private String avatar;

    private ESex sex;

    private String description;

    private Boolean isSystem;

    private Integer mainLevel;

    private VipInfo vipInfo;

    private UserRanking ranking;

    private HonorInfo honorInfo;

    public Builder() {
    }

    /**
     * 用户ID
     */
    public Builder setUserID(Integer userID) {
      this.userID = userID;
      return this;
    }

    /**
     * 发信者昵称
     */
    public Builder setNickName(String nickName) {
      this.nickName = nickName;
      return this;
    }

    /**
     * 头像地址
     */
    public Builder setAvatar(String avatar) {
      this.avatar = avatar;
      return this;
    }

    /**
     * 性别
     */
    public Builder setSex(ESex sex) {
      this.sex = sex;
      return this;
    }

    /**
     * 个人描述
     */
    public Builder setDescription(String description) {
      this.description = description;
      return this;
    }

    /**
     * 是否为系统
     */
    public Builder setIsSystem(Boolean isSystem) {
      this.isSystem = isSystem;
      return this;
    }

    /**
     * 主段位
     */
    public Builder setMainLevel(Integer mainLevel) {
      this.mainLevel = mainLevel;
      return this;
    }

    /**
     * vip类型
     */
    public Builder setVipInfo(VipInfo vipInfo) {
      this.vipInfo = vipInfo;
      return this;
    }

    /**
     * 排位信息
     */
    public Builder setRanking(UserRanking ranking) {
      this.ranking = ranking;
      return this;
    }

    /**
     * 会员信息
     */
    public Builder setHonorInfo(HonorInfo honorInfo) {
      this.honorInfo = honorInfo;
      return this;
    }

    @Override
    public UserInfo build() {
      return new UserInfo(userID, nickName, avatar, sex, description, isSystem, mainLevel, vipInfo, ranking, honorInfo, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_UserInfo extends ProtoAdapter<UserInfo> {
    public ProtoAdapter_UserInfo() {
      super(FieldEncoding.LENGTH_DELIMITED, UserInfo.class);
    }

    @Override
    public int encodedSize(UserInfo value) {
      return ProtoAdapter.UINT32.encodedSizeWithTag(1, value.userID)
          + ProtoAdapter.STRING.encodedSizeWithTag(2, value.nickName)
          + ProtoAdapter.STRING.encodedSizeWithTag(3, value.avatar)
          + ESex.ADAPTER.encodedSizeWithTag(4, value.sex)
          + ProtoAdapter.STRING.encodedSizeWithTag(5, value.description)
          + ProtoAdapter.BOOL.encodedSizeWithTag(6, value.isSystem)
          + ProtoAdapter.UINT32.encodedSizeWithTag(7, value.mainLevel)
          + VipInfo.ADAPTER.encodedSizeWithTag(8, value.vipInfo)
          + UserRanking.ADAPTER.encodedSizeWithTag(9, value.ranking)
          + HonorInfo.ADAPTER.encodedSizeWithTag(10, value.honorInfo)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, UserInfo value) throws IOException {
      ProtoAdapter.UINT32.encodeWithTag(writer, 1, value.userID);
      ProtoAdapter.STRING.encodeWithTag(writer, 2, value.nickName);
      ProtoAdapter.STRING.encodeWithTag(writer, 3, value.avatar);
      ESex.ADAPTER.encodeWithTag(writer, 4, value.sex);
      ProtoAdapter.STRING.encodeWithTag(writer, 5, value.description);
      ProtoAdapter.BOOL.encodeWithTag(writer, 6, value.isSystem);
      ProtoAdapter.UINT32.encodeWithTag(writer, 7, value.mainLevel);
      VipInfo.ADAPTER.encodeWithTag(writer, 8, value.vipInfo);
      UserRanking.ADAPTER.encodeWithTag(writer, 9, value.ranking);
      HonorInfo.ADAPTER.encodeWithTag(writer, 10, value.honorInfo);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public UserInfo decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.setUserID(ProtoAdapter.UINT32.decode(reader)); break;
          case 2: builder.setNickName(ProtoAdapter.STRING.decode(reader)); break;
          case 3: builder.setAvatar(ProtoAdapter.STRING.decode(reader)); break;
          case 4: {
            try {
              builder.setSex(ESex.ADAPTER.decode(reader));
            } catch (ProtoAdapter.EnumConstantNotFoundException e) {
              builder.addUnknownField(tag, FieldEncoding.VARINT, (long) e.value);
            }
            break;
          }
          case 5: builder.setDescription(ProtoAdapter.STRING.decode(reader)); break;
          case 6: builder.setIsSystem(ProtoAdapter.BOOL.decode(reader)); break;
          case 7: builder.setMainLevel(ProtoAdapter.UINT32.decode(reader)); break;
          case 8: builder.setVipInfo(VipInfo.ADAPTER.decode(reader)); break;
          case 9: builder.setRanking(UserRanking.ADAPTER.decode(reader)); break;
          case 10: builder.setHonorInfo(HonorInfo.ADAPTER.decode(reader)); break;
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
    public UserInfo redact(UserInfo value) {
      Builder builder = value.newBuilder();
      if (builder.vipInfo != null) builder.vipInfo = VipInfo.ADAPTER.redact(builder.vipInfo);
      if (builder.ranking != null) builder.ranking = UserRanking.ADAPTER.redact(builder.ranking);
      if (builder.honorInfo != null) builder.honorInfo = HonorInfo.ADAPTER.redact(builder.honorInfo);
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}
