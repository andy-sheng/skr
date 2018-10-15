// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: Account.proto
package com.wali.live.proto.Account;

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
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import okio.ByteString;

public final class LoginByUuidRsp extends Message<LoginByUuidRsp, LoginByUuidRsp.Builder> {
  public static final ProtoAdapter<LoginByUuidRsp> ADAPTER = new ProtoAdapter_LoginByUuidRsp();

  private static final long serialVersionUID = 0L;

  public static final Integer DEFAULT_RETCODE = 0;

  public static final Long DEFAULT_UUID = 0L;

  public static final String DEFAULT_SERVICETOKEN = "";

  public static final String DEFAULT_SECURITYKEY = "";

  public static final String DEFAULT_PASSTOKEN = "";

  public static final String DEFAULT_NICKNAME = "";

  public static final String DEFAULT_HEADINFO = "";

  public static final Integer DEFAULT_SEX = 0;

  public static final Boolean DEFAULT_HASINNERAVATAR = false;

  public static final Boolean DEFAULT_HASINNERNICKNAME = false;

  public static final Boolean DEFAULT_HASINNERSEX = false;

  public static final String DEFAULT_ERRMSG = "";

  /**
   * 0:表示成功，返回“6011”表示服务端调用用户资料失败，服务端也拿不到用户信息，客户端需要容错；
   */
  @WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32",
      label = WireField.Label.REQUIRED
  )
  public final Integer retCode;

  /**
   * 直播用户id
   */
  @WireField(
      tag = 2,
      adapter = "com.squareup.wire.ProtoAdapter#UINT64"
  )
  public final Long uuid;

  /**
   * 用于milink传输，有效期服务器控制
   */
  @WireField(
      tag = 3,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  public final String serviceToken;

  /**
   * 用户milink数据加密
   */
  @WireField(
      tag = 4,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  public final String securityKey;

  /**
   * 可以用来换取serviceToken
   */
  @WireField(
      tag = 5,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  public final String passToken;

  /**
   * 昵称
   */
  @WireField(
      tag = 6,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  public final String nickname;

  /**
   * 头像信息
   */
  @WireField(
      tag = 7,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  public final String headinfo;

  /**
   * 性别 0未知 1男性 2女性
   */
  @WireField(
      tag = 8,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  public final Integer sex;

  /**
   * 是否有内部头像
   */
  @WireField(
      tag = 9,
      adapter = "com.squareup.wire.ProtoAdapter#BOOL"
  )
  public final Boolean hasInnerAvatar;

  /**
   * 是否有内部昵称
   */
  @WireField(
      tag = 10,
      adapter = "com.squareup.wire.ProtoAdapter#BOOL"
  )
  public final Boolean hasInnerNickname;

  /**
   * 是否有内部性别
   */
  @WireField(
      tag = 11,
      adapter = "com.squareup.wire.ProtoAdapter#BOOL"
  )
  public final Boolean hasInnerSex;

  @WireField(
      tag = 12,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  public final String errMsg;

  public LoginByUuidRsp(Integer retCode, Long uuid, String serviceToken, String securityKey,
      String passToken, String nickname, String headinfo, Integer sex, Boolean hasInnerAvatar,
      Boolean hasInnerNickname, Boolean hasInnerSex, String errMsg) {
    this(retCode, uuid, serviceToken, securityKey, passToken, nickname, headinfo, sex, hasInnerAvatar, hasInnerNickname, hasInnerSex, errMsg, ByteString.EMPTY);
  }

  public LoginByUuidRsp(Integer retCode, Long uuid, String serviceToken, String securityKey,
      String passToken, String nickname, String headinfo, Integer sex, Boolean hasInnerAvatar,
      Boolean hasInnerNickname, Boolean hasInnerSex, String errMsg, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.retCode = retCode;
    this.uuid = uuid;
    this.serviceToken = serviceToken;
    this.securityKey = securityKey;
    this.passToken = passToken;
    this.nickname = nickname;
    this.headinfo = headinfo;
    this.sex = sex;
    this.hasInnerAvatar = hasInnerAvatar;
    this.hasInnerNickname = hasInnerNickname;
    this.hasInnerSex = hasInnerSex;
    this.errMsg = errMsg;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.retCode = retCode;
    builder.uuid = uuid;
    builder.serviceToken = serviceToken;
    builder.securityKey = securityKey;
    builder.passToken = passToken;
    builder.nickname = nickname;
    builder.headinfo = headinfo;
    builder.sex = sex;
    builder.hasInnerAvatar = hasInnerAvatar;
    builder.hasInnerNickname = hasInnerNickname;
    builder.hasInnerSex = hasInnerSex;
    builder.errMsg = errMsg;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof LoginByUuidRsp)) return false;
    LoginByUuidRsp o = (LoginByUuidRsp) other;
    return unknownFields().equals(o.unknownFields())
        && retCode.equals(o.retCode)
        && Internal.equals(uuid, o.uuid)
        && Internal.equals(serviceToken, o.serviceToken)
        && Internal.equals(securityKey, o.securityKey)
        && Internal.equals(passToken, o.passToken)
        && Internal.equals(nickname, o.nickname)
        && Internal.equals(headinfo, o.headinfo)
        && Internal.equals(sex, o.sex)
        && Internal.equals(hasInnerAvatar, o.hasInnerAvatar)
        && Internal.equals(hasInnerNickname, o.hasInnerNickname)
        && Internal.equals(hasInnerSex, o.hasInnerSex)
        && Internal.equals(errMsg, o.errMsg);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + retCode.hashCode();
      result = result * 37 + (uuid != null ? uuid.hashCode() : 0);
      result = result * 37 + (serviceToken != null ? serviceToken.hashCode() : 0);
      result = result * 37 + (securityKey != null ? securityKey.hashCode() : 0);
      result = result * 37 + (passToken != null ? passToken.hashCode() : 0);
      result = result * 37 + (nickname != null ? nickname.hashCode() : 0);
      result = result * 37 + (headinfo != null ? headinfo.hashCode() : 0);
      result = result * 37 + (sex != null ? sex.hashCode() : 0);
      result = result * 37 + (hasInnerAvatar != null ? hasInnerAvatar.hashCode() : 0);
      result = result * 37 + (hasInnerNickname != null ? hasInnerNickname.hashCode() : 0);
      result = result * 37 + (hasInnerSex != null ? hasInnerSex.hashCode() : 0);
      result = result * 37 + (errMsg != null ? errMsg.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(", retCode=").append(retCode);
    if (uuid != null) builder.append(", uuid=").append(uuid);
    if (serviceToken != null) builder.append(", serviceToken=").append(serviceToken);
    if (securityKey != null) builder.append(", securityKey=").append(securityKey);
    if (passToken != null) builder.append(", passToken=").append(passToken);
    if (nickname != null) builder.append(", nickname=").append(nickname);
    if (headinfo != null) builder.append(", headinfo=").append(headinfo);
    if (sex != null) builder.append(", sex=").append(sex);
    if (hasInnerAvatar != null) builder.append(", hasInnerAvatar=").append(hasInnerAvatar);
    if (hasInnerNickname != null) builder.append(", hasInnerNickname=").append(hasInnerNickname);
    if (hasInnerSex != null) builder.append(", hasInnerSex=").append(hasInnerSex);
    if (errMsg != null) builder.append(", errMsg=").append(errMsg);
    return builder.replace(0, 2, "LoginByUuidRsp{").append('}').toString();
  }

  public static final LoginByUuidRsp parseFrom(byte[] data) throws IOException {
    LoginByUuidRsp c = null;
       c = LoginByUuidRsp.ADAPTER.decode(data);
    return c;
  }

  /**
   * 0:表示成功，返回“6011”表示服务端调用用户资料失败，服务端也拿不到用户信息，客户端需要容错；
   */
  public Integer getRetCode() {
    if(retCode==null){
        return DEFAULT_RETCODE;
    }
    return retCode;
  }

  /**
   * 直播用户id
   */
  public Long getUuid() {
    if(uuid==null){
        return DEFAULT_UUID;
    }
    return uuid;
  }

  /**
   * 用于milink传输，有效期服务器控制
   */
  public String getServiceToken() {
    if(serviceToken==null){
        return DEFAULT_SERVICETOKEN;
    }
    return serviceToken;
  }

  /**
   * 用户milink数据加密
   */
  public String getSecurityKey() {
    if(securityKey==null){
        return DEFAULT_SECURITYKEY;
    }
    return securityKey;
  }

  /**
   * 可以用来换取serviceToken
   */
  public String getPassToken() {
    if(passToken==null){
        return DEFAULT_PASSTOKEN;
    }
    return passToken;
  }

  /**
   * 昵称
   */
  public String getNickname() {
    if(nickname==null){
        return DEFAULT_NICKNAME;
    }
    return nickname;
  }

  /**
   * 头像信息
   */
  public String getHeadinfo() {
    if(headinfo==null){
        return DEFAULT_HEADINFO;
    }
    return headinfo;
  }

  /**
   * 性别 0未知 1男性 2女性
   */
  public Integer getSex() {
    if(sex==null){
        return DEFAULT_SEX;
    }
    return sex;
  }

  /**
   * 是否有内部头像
   */
  public Boolean getHasInnerAvatar() {
    if(hasInnerAvatar==null){
        return DEFAULT_HASINNERAVATAR;
    }
    return hasInnerAvatar;
  }

  /**
   * 是否有内部昵称
   */
  public Boolean getHasInnerNickname() {
    if(hasInnerNickname==null){
        return DEFAULT_HASINNERNICKNAME;
    }
    return hasInnerNickname;
  }

  /**
   * 是否有内部性别
   */
  public Boolean getHasInnerSex() {
    if(hasInnerSex==null){
        return DEFAULT_HASINNERSEX;
    }
    return hasInnerSex;
  }

  public String getErrMsg() {
    if(errMsg==null){
        return DEFAULT_ERRMSG;
    }
    return errMsg;
  }

  /**
   * 0:表示成功，返回“6011”表示服务端调用用户资料失败，服务端也拿不到用户信息，客户端需要容错；
   */
  public boolean hasRetCode() {
    return retCode!=null;
  }

  /**
   * 直播用户id
   */
  public boolean hasUuid() {
    return uuid!=null;
  }

  /**
   * 用于milink传输，有效期服务器控制
   */
  public boolean hasServiceToken() {
    return serviceToken!=null;
  }

  /**
   * 用户milink数据加密
   */
  public boolean hasSecurityKey() {
    return securityKey!=null;
  }

  /**
   * 可以用来换取serviceToken
   */
  public boolean hasPassToken() {
    return passToken!=null;
  }

  /**
   * 昵称
   */
  public boolean hasNickname() {
    return nickname!=null;
  }

  /**
   * 头像信息
   */
  public boolean hasHeadinfo() {
    return headinfo!=null;
  }

  /**
   * 性别 0未知 1男性 2女性
   */
  public boolean hasSex() {
    return sex!=null;
  }

  /**
   * 是否有内部头像
   */
  public boolean hasHasInnerAvatar() {
    return hasInnerAvatar!=null;
  }

  /**
   * 是否有内部昵称
   */
  public boolean hasHasInnerNickname() {
    return hasInnerNickname!=null;
  }

  /**
   * 是否有内部性别
   */
  public boolean hasHasInnerSex() {
    return hasInnerSex!=null;
  }

  public boolean hasErrMsg() {
    return errMsg!=null;
  }

  public static final class Builder extends Message.Builder<LoginByUuidRsp, Builder> {
    public Integer retCode;

    public Long uuid;

    public String serviceToken;

    public String securityKey;

    public String passToken;

    public String nickname;

    public String headinfo;

    public Integer sex;

    public Boolean hasInnerAvatar;

    public Boolean hasInnerNickname;

    public Boolean hasInnerSex;

    public String errMsg;

    public Builder() {
    }

    /**
     * 0:表示成功，返回“6011”表示服务端调用用户资料失败，服务端也拿不到用户信息，客户端需要容错；
     */
    public Builder setRetCode(Integer retCode) {
      this.retCode = retCode;
      return this;
    }

    /**
     * 直播用户id
     */
    public Builder setUuid(Long uuid) {
      this.uuid = uuid;
      return this;
    }

    /**
     * 用于milink传输，有效期服务器控制
     */
    public Builder setServiceToken(String serviceToken) {
      this.serviceToken = serviceToken;
      return this;
    }

    /**
     * 用户milink数据加密
     */
    public Builder setSecurityKey(String securityKey) {
      this.securityKey = securityKey;
      return this;
    }

    /**
     * 可以用来换取serviceToken
     */
    public Builder setPassToken(String passToken) {
      this.passToken = passToken;
      return this;
    }

    /**
     * 昵称
     */
    public Builder setNickname(String nickname) {
      this.nickname = nickname;
      return this;
    }

    /**
     * 头像信息
     */
    public Builder setHeadinfo(String headinfo) {
      this.headinfo = headinfo;
      return this;
    }

    /**
     * 性别 0未知 1男性 2女性
     */
    public Builder setSex(Integer sex) {
      this.sex = sex;
      return this;
    }

    /**
     * 是否有内部头像
     */
    public Builder setHasInnerAvatar(Boolean hasInnerAvatar) {
      this.hasInnerAvatar = hasInnerAvatar;
      return this;
    }

    /**
     * 是否有内部昵称
     */
    public Builder setHasInnerNickname(Boolean hasInnerNickname) {
      this.hasInnerNickname = hasInnerNickname;
      return this;
    }

    /**
     * 是否有内部性别
     */
    public Builder setHasInnerSex(Boolean hasInnerSex) {
      this.hasInnerSex = hasInnerSex;
      return this;
    }

    public Builder setErrMsg(String errMsg) {
      this.errMsg = errMsg;
      return this;
    }

    @Override
    public LoginByUuidRsp build() {
      return new LoginByUuidRsp(retCode, uuid, serviceToken, securityKey, passToken, nickname, headinfo, sex, hasInnerAvatar, hasInnerNickname, hasInnerSex, errMsg, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_LoginByUuidRsp extends ProtoAdapter<LoginByUuidRsp> {
    public ProtoAdapter_LoginByUuidRsp() {
      super(FieldEncoding.LENGTH_DELIMITED, LoginByUuidRsp.class);
    }

    @Override
    public int encodedSize(LoginByUuidRsp value) {
      return ProtoAdapter.UINT32.encodedSizeWithTag(1, value.retCode)
          + ProtoAdapter.UINT64.encodedSizeWithTag(2, value.uuid)
          + ProtoAdapter.STRING.encodedSizeWithTag(3, value.serviceToken)
          + ProtoAdapter.STRING.encodedSizeWithTag(4, value.securityKey)
          + ProtoAdapter.STRING.encodedSizeWithTag(5, value.passToken)
          + ProtoAdapter.STRING.encodedSizeWithTag(6, value.nickname)
          + ProtoAdapter.STRING.encodedSizeWithTag(7, value.headinfo)
          + ProtoAdapter.UINT32.encodedSizeWithTag(8, value.sex)
          + ProtoAdapter.BOOL.encodedSizeWithTag(9, value.hasInnerAvatar)
          + ProtoAdapter.BOOL.encodedSizeWithTag(10, value.hasInnerNickname)
          + ProtoAdapter.BOOL.encodedSizeWithTag(11, value.hasInnerSex)
          + ProtoAdapter.STRING.encodedSizeWithTag(12, value.errMsg)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, LoginByUuidRsp value) throws IOException {
      ProtoAdapter.UINT32.encodeWithTag(writer, 1, value.retCode);
      ProtoAdapter.UINT64.encodeWithTag(writer, 2, value.uuid);
      ProtoAdapter.STRING.encodeWithTag(writer, 3, value.serviceToken);
      ProtoAdapter.STRING.encodeWithTag(writer, 4, value.securityKey);
      ProtoAdapter.STRING.encodeWithTag(writer, 5, value.passToken);
      ProtoAdapter.STRING.encodeWithTag(writer, 6, value.nickname);
      ProtoAdapter.STRING.encodeWithTag(writer, 7, value.headinfo);
      ProtoAdapter.UINT32.encodeWithTag(writer, 8, value.sex);
      ProtoAdapter.BOOL.encodeWithTag(writer, 9, value.hasInnerAvatar);
      ProtoAdapter.BOOL.encodeWithTag(writer, 10, value.hasInnerNickname);
      ProtoAdapter.BOOL.encodeWithTag(writer, 11, value.hasInnerSex);
      ProtoAdapter.STRING.encodeWithTag(writer, 12, value.errMsg);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public LoginByUuidRsp decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.setRetCode(ProtoAdapter.UINT32.decode(reader)); break;
          case 2: builder.setUuid(ProtoAdapter.UINT64.decode(reader)); break;
          case 3: builder.setServiceToken(ProtoAdapter.STRING.decode(reader)); break;
          case 4: builder.setSecurityKey(ProtoAdapter.STRING.decode(reader)); break;
          case 5: builder.setPassToken(ProtoAdapter.STRING.decode(reader)); break;
          case 6: builder.setNickname(ProtoAdapter.STRING.decode(reader)); break;
          case 7: builder.setHeadinfo(ProtoAdapter.STRING.decode(reader)); break;
          case 8: builder.setSex(ProtoAdapter.UINT32.decode(reader)); break;
          case 9: builder.setHasInnerAvatar(ProtoAdapter.BOOL.decode(reader)); break;
          case 10: builder.setHasInnerNickname(ProtoAdapter.BOOL.decode(reader)); break;
          case 11: builder.setHasInnerSex(ProtoAdapter.BOOL.decode(reader)); break;
          case 12: builder.setErrMsg(ProtoAdapter.STRING.decode(reader)); break;
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
    public LoginByUuidRsp redact(LoginByUuidRsp value) {
      Builder builder = value.newBuilder();
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}
