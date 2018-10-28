package com.indexrecyclerview.model;

/**
 * Created by YoKey on 16/10/6.
 */
public class EntityWrapper<T> {

    // 标题和内容
    public static final int TYPE_TITLE = Integer.MAX_VALUE - 1;
    public static final int TYPE_CONTENT = Integer.MAX_VALUE;

    //
    public static final int TYPE_HEADER = 1;
    public static final int TYPE_FOOTER = 2;

    private String index;
    private String indexTitle;
    private String pinyin;
    private String indexByField;
    private T data;
    private int originalPosition = -1;
    private int itemType = TYPE_CONTENT;
    private int headerFooterType;

    public EntityWrapper() {
    }

    public EntityWrapper(String index, int itemType) {
        this.index = index;
        this.indexTitle = index;
        this.pinyin = index;
        this.itemType = itemType;
    }


    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getIndexTitle() {
        return indexTitle;
    }

    public void setIndexTitle(String indexTitle) {
        this.indexTitle = indexTitle;
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    public String getIndexByField() {
        return indexByField;
    }

    public void setIndexByField(String indexByField) {
        this.indexByField = indexByField;
    }

    public int getOriginalPosition() {
        return originalPosition;
    }

    public void setOriginalPosition(int originalPosition) {
        this.originalPosition = originalPosition;
    }

    public int getItemType() {
        return itemType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public int getHeaderFooterType() {
        return headerFooterType;
    }

    public void setHeaderFooterType(int headerFooterType) {
        this.headerFooterType = headerFooterType;
    }

    public boolean isTitle() {
        return itemType == TYPE_TITLE;
    }

    public boolean isContent() {
        return itemType == TYPE_CONTENT;
    }

    public boolean isHeader() {
        return headerFooterType == TYPE_HEADER;
    }

    public boolean isFooter() {
        return headerFooterType == TYPE_FOOTER;
    }
}