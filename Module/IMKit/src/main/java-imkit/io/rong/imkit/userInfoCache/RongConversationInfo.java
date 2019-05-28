//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.userInfoCache;

import android.net.Uri;

public class RongConversationInfo {
    private String conversationType;
    private String id;
    private String name;
    private Uri uri;

    public RongConversationInfo(String type, String id, String name, Uri uri) {
        this.conversationType = type;
        this.id = id;
        this.name = name;
        this.uri = uri;
    }

    public String getConversationType() {
        return this.conversationType;
    }

    public void setConversationType(String conversationType) {
        this.conversationType = conversationType;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Uri getUri() {
        return this.uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
