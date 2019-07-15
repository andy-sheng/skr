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

import okio.ByteString;

public final class GiftInfo extends Message<GiftInfo, GiftInfo.Builder> {
  public static final ProtoAdapter<GiftInfo> ADAPTER = new ProtoAdapter_GiftInfo();

  private static final long serialVersionUID = 0L;

  public static final Integer DEFAULT_GIFTID = 0;

  public static final String DEFAULT_GIFTNAME = "";

  public static final String DEFAULT_GIFTURL = "";

  public static final Long DEFAULT_PRICE = 0L;

  public static final String DEFAULT_SOURCEURL = "";

  public static final Integer DEFAULT_SORTID = 0;

  public static final EGiftType DEFAULT_GIFTTYPE = EGiftType.EG_Unknown;

  public static final Boolean DEFAULT_CANCONTINUE = false;

  public static final String DEFAULT_DESCRIPTION = "";

  public static final Float DEFAULT_REALPRICE = 0.0f;

  public static final Boolean DEFAULT_PLAY = false;

  public static final Integer DEFAULT_TEXTCONTINUECOUNT = 0;

  public static final EGiftDisplayType DEFAULT_DISPLAYTYPE = EGiftDisplayType.EGDT_Unknown;

  /**
   * 礼物id
   */
  @WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  private final Integer giftID;

  /**
   * 礼物名称
   */
  @WireField(
      tag = 2,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  private final String giftName;

  /**
   * 礼物的图片
   */
  @WireField(
      tag = 3,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  private final String giftURL;

  /**
   * 1000分之一钻单位 或者金币
   */
  @WireField(
      tag = 4,
      adapter = "com.squareup.wire.ProtoAdapter#UINT64"
  )
  private final Long price;

  /**
   * 礼物资源，使用效果
   */
  @WireField(
      tag = 5,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  private final String sourceURL;

  /**
   * 排序id
   */
  @WireField(
      tag = 6,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  private final Integer sortID;

  /**
   * 礼物类型，钻石礼物、金币礼物
   */
  @WireField(
      tag = 7,
      adapter = "com.zq.live.proto.Common.EGiftType#ADAPTER"
  )
  private final EGiftType giftType;

  /**
   * 是否可以连击
   */
  @WireField(
      tag = 8,
      adapter = "com.squareup.wire.ProtoAdapter#BOOL"
  )
  private final Boolean canContinue;

  /**
   * 礼物描述
   */
  @WireField(
      tag = 9,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  private final String description;

  /**
   * 真实价格
   */
  @WireField(
      tag = 10,
      adapter = "com.squareup.wire.ProtoAdapter#FLOAT"
  )
  private final Float realPrice;

  /**
   * 是否需要播放
   */
  @WireField(
      tag = 11,
      adapter = "com.squareup.wire.ProtoAdapter#BOOL"
  )
  private final Boolean play;

  @WireField(
      tag = 12,
      adapter = "com.squareup.wire.ProtoAdapter#SINT32"
  )
  private final Integer textContinueCount;

  @WireField(
      tag = 13,
      adapter = "com.zq.live.proto.Common.EGiftDisplayType#ADAPTER"
  )
  private final EGiftDisplayType displayType;

  @WireField(
      tag = 14,
      adapter = "com.zq.live.proto.Common.GiftExtraInfo#ADAPTER"
  )
  private final GiftExtraInfo extra;

  public GiftInfo(Integer giftID, String giftName, String giftURL, Long price, String sourceURL,
      Integer sortID, EGiftType giftType, Boolean canContinue, String description, Float realPrice,
      Boolean play, Integer textContinueCount, EGiftDisplayType displayType, GiftExtraInfo extra) {
    this(giftID, giftName, giftURL, price, sourceURL, sortID, giftType, canContinue, description, realPrice, play, textContinueCount, displayType, extra, ByteString.EMPTY);
  }

  public GiftInfo(Integer giftID, String giftName, String giftURL, Long price, String sourceURL,
      Integer sortID, EGiftType giftType, Boolean canContinue, String description, Float realPrice,
      Boolean play, Integer textContinueCount, EGiftDisplayType displayType, GiftExtraInfo extra,
      ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.giftID = giftID;
    this.giftName = giftName;
    this.giftURL = giftURL;
    this.price = price;
    this.sourceURL = sourceURL;
    this.sortID = sortID;
    this.giftType = giftType;
    this.canContinue = canContinue;
    this.description = description;
    this.realPrice = realPrice;
    this.play = play;
    this.textContinueCount = textContinueCount;
    this.displayType = displayType;
    this.extra = extra;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.giftID = giftID;
    builder.giftName = giftName;
    builder.giftURL = giftURL;
    builder.price = price;
    builder.sourceURL = sourceURL;
    builder.sortID = sortID;
    builder.giftType = giftType;
    builder.canContinue = canContinue;
    builder.description = description;
    builder.realPrice = realPrice;
    builder.play = play;
    builder.textContinueCount = textContinueCount;
    builder.displayType = displayType;
    builder.extra = extra;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof GiftInfo)) return false;
    GiftInfo o = (GiftInfo) other;
    return unknownFields().equals(o.unknownFields())
        && Internal.equals(giftID, o.giftID)
        && Internal.equals(giftName, o.giftName)
        && Internal.equals(giftURL, o.giftURL)
        && Internal.equals(price, o.price)
        && Internal.equals(sourceURL, o.sourceURL)
        && Internal.equals(sortID, o.sortID)
        && Internal.equals(giftType, o.giftType)
        && Internal.equals(canContinue, o.canContinue)
        && Internal.equals(description, o.description)
        && Internal.equals(realPrice, o.realPrice)
        && Internal.equals(play, o.play)
        && Internal.equals(textContinueCount, o.textContinueCount)
        && Internal.equals(displayType, o.displayType)
        && Internal.equals(extra, o.extra);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + (giftID != null ? giftID.hashCode() : 0);
      result = result * 37 + (giftName != null ? giftName.hashCode() : 0);
      result = result * 37 + (giftURL != null ? giftURL.hashCode() : 0);
      result = result * 37 + (price != null ? price.hashCode() : 0);
      result = result * 37 + (sourceURL != null ? sourceURL.hashCode() : 0);
      result = result * 37 + (sortID != null ? sortID.hashCode() : 0);
      result = result * 37 + (giftType != null ? giftType.hashCode() : 0);
      result = result * 37 + (canContinue != null ? canContinue.hashCode() : 0);
      result = result * 37 + (description != null ? description.hashCode() : 0);
      result = result * 37 + (realPrice != null ? realPrice.hashCode() : 0);
      result = result * 37 + (play != null ? play.hashCode() : 0);
      result = result * 37 + (textContinueCount != null ? textContinueCount.hashCode() : 0);
      result = result * 37 + (displayType != null ? displayType.hashCode() : 0);
      result = result * 37 + (extra != null ? extra.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (giftID != null) builder.append(", giftID=").append(giftID);
    if (giftName != null) builder.append(", giftName=").append(giftName);
    if (giftURL != null) builder.append(", giftURL=").append(giftURL);
    if (price != null) builder.append(", price=").append(price);
    if (sourceURL != null) builder.append(", sourceURL=").append(sourceURL);
    if (sortID != null) builder.append(", sortID=").append(sortID);
    if (giftType != null) builder.append(", giftType=").append(giftType);
    if (canContinue != null) builder.append(", canContinue=").append(canContinue);
    if (description != null) builder.append(", description=").append(description);
    if (realPrice != null) builder.append(", realPrice=").append(realPrice);
    if (play != null) builder.append(", play=").append(play);
    if (textContinueCount != null) builder.append(", textContinueCount=").append(textContinueCount);
    if (displayType != null) builder.append(", displayType=").append(displayType);
    if (extra != null) builder.append(", extra=").append(extra);
    return builder.replace(0, 2, "GiftInfo{").append('}').toString();
  }

  public byte[] toByteArray() {
    return GiftInfo.ADAPTER.encode(this);
  }

  public static final GiftInfo parseFrom(byte[] data) throws IOException {
    GiftInfo c = null;
       c = GiftInfo.ADAPTER.decode(data);
    return c;
  }

  /**
   * 礼物id
   */
  public Integer getGiftID() {
    if(giftID==null){
        return DEFAULT_GIFTID;
    }
    return giftID;
  }

  /**
   * 礼物名称
   */
  public String getGiftName() {
    if(giftName==null){
        return DEFAULT_GIFTNAME;
    }
    return giftName;
  }

  /**
   * 礼物的图片
   */
  public String getGiftURL() {
    if(giftURL==null){
        return DEFAULT_GIFTURL;
    }
    return giftURL;
  }

  /**
   * 1000分之一钻单位 或者金币
   */
  public Long getPrice() {
    if(price==null){
        return DEFAULT_PRICE;
    }
    return price;
  }

  /**
   * 礼物资源，使用效果
   */
  public String getSourceURL() {
    if(sourceURL==null){
        return DEFAULT_SOURCEURL;
    }
    return sourceURL;
  }

  /**
   * 排序id
   */
  public Integer getSortID() {
    if(sortID==null){
        return DEFAULT_SORTID;
    }
    return sortID;
  }

  /**
   * 礼物类型，钻石礼物、金币礼物
   */
  public EGiftType getGiftType() {
    if(giftType==null){
        return new EGiftType.Builder().build();
    }
    return giftType;
  }

  /**
   * 是否可以连击
   */
  public Boolean getCanContinue() {
    if(canContinue==null){
        return DEFAULT_CANCONTINUE;
    }
    return canContinue;
  }

  /**
   * 礼物描述
   */
  public String getDescription() {
    if(description==null){
        return DEFAULT_DESCRIPTION;
    }
    return description;
  }

  /**
   * 真实价格
   */
  public Float getRealPrice() {
    if(realPrice==null){
        return DEFAULT_REALPRICE;
    }
    return realPrice;
  }

  /**
   * 是否需要播放
   */
  public Boolean getPlay() {
    if(play==null){
        return DEFAULT_PLAY;
    }
    return play;
  }

  public Integer getTextContinueCount() {
    if(textContinueCount==null){
        return DEFAULT_TEXTCONTINUECOUNT;
    }
    return textContinueCount;
  }

  public EGiftDisplayType getDisplayType() {
    if(displayType==null){
        return new EGiftDisplayType.Builder().build();
    }
    return displayType;
  }

  public GiftExtraInfo getExtra() {
    if(extra==null){
        return new GiftExtraInfo.Builder().build();
    }
    return extra;
  }

  /**
   * 礼物id
   */
  public boolean hasGiftID() {
    return giftID!=null;
  }

  /**
   * 礼物名称
   */
  public boolean hasGiftName() {
    return giftName!=null;
  }

  /**
   * 礼物的图片
   */
  public boolean hasGiftURL() {
    return giftURL!=null;
  }

  /**
   * 1000分之一钻单位 或者金币
   */
  public boolean hasPrice() {
    return price!=null;
  }

  /**
   * 礼物资源，使用效果
   */
  public boolean hasSourceURL() {
    return sourceURL!=null;
  }

  /**
   * 排序id
   */
  public boolean hasSortID() {
    return sortID!=null;
  }

  /**
   * 礼物类型，钻石礼物、金币礼物
   */
  public boolean hasGiftType() {
    return giftType!=null;
  }

  /**
   * 是否可以连击
   */
  public boolean hasCanContinue() {
    return canContinue!=null;
  }

  /**
   * 礼物描述
   */
  public boolean hasDescription() {
    return description!=null;
  }

  /**
   * 真实价格
   */
  public boolean hasRealPrice() {
    return realPrice!=null;
  }

  /**
   * 是否需要播放
   */
  public boolean hasPlay() {
    return play!=null;
  }

  public boolean hasTextContinueCount() {
    return textContinueCount!=null;
  }

  public boolean hasDisplayType() {
    return displayType!=null;
  }

  public boolean hasExtra() {
    return extra!=null;
  }

  public static final class Builder extends Message.Builder<GiftInfo, Builder> {
    private Integer giftID;

    private String giftName;

    private String giftURL;

    private Long price;

    private String sourceURL;

    private Integer sortID;

    private EGiftType giftType;

    private Boolean canContinue;

    private String description;

    private Float realPrice;

    private Boolean play;

    private Integer textContinueCount;

    private EGiftDisplayType displayType;

    private GiftExtraInfo extra;

    public Builder() {
    }

    /**
     * 礼物id
     */
    public Builder setGiftID(Integer giftID) {
      this.giftID = giftID;
      return this;
    }

    /**
     * 礼物名称
     */
    public Builder setGiftName(String giftName) {
      this.giftName = giftName;
      return this;
    }

    /**
     * 礼物的图片
     */
    public Builder setGiftURL(String giftURL) {
      this.giftURL = giftURL;
      return this;
    }

    /**
     * 1000分之一钻单位 或者金币
     */
    public Builder setPrice(Long price) {
      this.price = price;
      return this;
    }

    /**
     * 礼物资源，使用效果
     */
    public Builder setSourceURL(String sourceURL) {
      this.sourceURL = sourceURL;
      return this;
    }

    /**
     * 排序id
     */
    public Builder setSortID(Integer sortID) {
      this.sortID = sortID;
      return this;
    }

    /**
     * 礼物类型，钻石礼物、金币礼物
     */
    public Builder setGiftType(EGiftType giftType) {
      this.giftType = giftType;
      return this;
    }

    /**
     * 是否可以连击
     */
    public Builder setCanContinue(Boolean canContinue) {
      this.canContinue = canContinue;
      return this;
    }

    /**
     * 礼物描述
     */
    public Builder setDescription(String description) {
      this.description = description;
      return this;
    }

    /**
     * 真实价格
     */
    public Builder setRealPrice(Float realPrice) {
      this.realPrice = realPrice;
      return this;
    }

    /**
     * 是否需要播放
     */
    public Builder setPlay(Boolean play) {
      this.play = play;
      return this;
    }

    public Builder setTextContinueCount(Integer textContinueCount) {
      this.textContinueCount = textContinueCount;
      return this;
    }

    public Builder setDisplayType(EGiftDisplayType displayType) {
      this.displayType = displayType;
      return this;
    }

    public Builder setExtra(GiftExtraInfo extra) {
      this.extra = extra;
      return this;
    }

    @Override
    public GiftInfo build() {
      return new GiftInfo(giftID, giftName, giftURL, price, sourceURL, sortID, giftType, canContinue, description, realPrice, play, textContinueCount, displayType, extra, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_GiftInfo extends ProtoAdapter<GiftInfo> {
    public ProtoAdapter_GiftInfo() {
      super(FieldEncoding.LENGTH_DELIMITED, GiftInfo.class);
    }

    @Override
    public int encodedSize(GiftInfo value) {
      return ProtoAdapter.UINT32.encodedSizeWithTag(1, value.giftID)
          + ProtoAdapter.STRING.encodedSizeWithTag(2, value.giftName)
          + ProtoAdapter.STRING.encodedSizeWithTag(3, value.giftURL)
          + ProtoAdapter.UINT64.encodedSizeWithTag(4, value.price)
          + ProtoAdapter.STRING.encodedSizeWithTag(5, value.sourceURL)
          + ProtoAdapter.UINT32.encodedSizeWithTag(6, value.sortID)
          + EGiftType.ADAPTER.encodedSizeWithTag(7, value.giftType)
          + ProtoAdapter.BOOL.encodedSizeWithTag(8, value.canContinue)
          + ProtoAdapter.STRING.encodedSizeWithTag(9, value.description)
          + ProtoAdapter.FLOAT.encodedSizeWithTag(10, value.realPrice)
          + ProtoAdapter.BOOL.encodedSizeWithTag(11, value.play)
          + ProtoAdapter.SINT32.encodedSizeWithTag(12, value.textContinueCount)
          + EGiftDisplayType.ADAPTER.encodedSizeWithTag(13, value.displayType)
          + GiftExtraInfo.ADAPTER.encodedSizeWithTag(14, value.extra)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, GiftInfo value) throws IOException {
      ProtoAdapter.UINT32.encodeWithTag(writer, 1, value.giftID);
      ProtoAdapter.STRING.encodeWithTag(writer, 2, value.giftName);
      ProtoAdapter.STRING.encodeWithTag(writer, 3, value.giftURL);
      ProtoAdapter.UINT64.encodeWithTag(writer, 4, value.price);
      ProtoAdapter.STRING.encodeWithTag(writer, 5, value.sourceURL);
      ProtoAdapter.UINT32.encodeWithTag(writer, 6, value.sortID);
      EGiftType.ADAPTER.encodeWithTag(writer, 7, value.giftType);
      ProtoAdapter.BOOL.encodeWithTag(writer, 8, value.canContinue);
      ProtoAdapter.STRING.encodeWithTag(writer, 9, value.description);
      ProtoAdapter.FLOAT.encodeWithTag(writer, 10, value.realPrice);
      ProtoAdapter.BOOL.encodeWithTag(writer, 11, value.play);
      ProtoAdapter.SINT32.encodeWithTag(writer, 12, value.textContinueCount);
      EGiftDisplayType.ADAPTER.encodeWithTag(writer, 13, value.displayType);
      GiftExtraInfo.ADAPTER.encodeWithTag(writer, 14, value.extra);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public GiftInfo decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.setGiftID(ProtoAdapter.UINT32.decode(reader)); break;
          case 2: builder.setGiftName(ProtoAdapter.STRING.decode(reader)); break;
          case 3: builder.setGiftURL(ProtoAdapter.STRING.decode(reader)); break;
          case 4: builder.setPrice(ProtoAdapter.UINT64.decode(reader)); break;
          case 5: builder.setSourceURL(ProtoAdapter.STRING.decode(reader)); break;
          case 6: builder.setSortID(ProtoAdapter.UINT32.decode(reader)); break;
          case 7: {
            try {
              builder.setGiftType(EGiftType.ADAPTER.decode(reader));
            } catch (EnumConstantNotFoundException e) {
              builder.addUnknownField(tag, FieldEncoding.VARINT, (long) e.value);
            }
            break;
          }
          case 8: builder.setCanContinue(ProtoAdapter.BOOL.decode(reader)); break;
          case 9: builder.setDescription(ProtoAdapter.STRING.decode(reader)); break;
          case 10: builder.setRealPrice(ProtoAdapter.FLOAT.decode(reader)); break;
          case 11: builder.setPlay(ProtoAdapter.BOOL.decode(reader)); break;
          case 12: builder.setTextContinueCount(ProtoAdapter.SINT32.decode(reader)); break;
          case 13: {
            try {
              builder.setDisplayType(EGiftDisplayType.ADAPTER.decode(reader));
            } catch (EnumConstantNotFoundException e) {
              builder.addUnknownField(tag, FieldEncoding.VARINT, (long) e.value);
            }
            break;
          }
          case 14: builder.setExtra(GiftExtraInfo.ADAPTER.decode(reader)); break;
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
    public GiftInfo redact(GiftInfo value) {
      Builder builder = value.newBuilder();
      if (builder.extra != null) builder.extra = GiftExtraInfo.ADAPTER.redact(builder.extra);
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}
