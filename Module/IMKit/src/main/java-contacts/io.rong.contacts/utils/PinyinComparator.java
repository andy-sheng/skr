package io.rong.contacts.utils;

import com.common.core.userinfo.UserInfo;

import java.util.Comparator;

/**
 *
 * @author
 *
 */
public class PinyinComparator implements Comparator<UserInfo> {


    public static PinyinComparator instance = null;

    public static PinyinComparator getInstance() {
        if (instance == null) {
            instance = new PinyinComparator();
        }
        return instance;
    }

    public int compare(UserInfo o1, UserInfo o2) {
        if (o1.getLetter().equals("@")
                || o2.getLetter().equals("#")) {
            return -1;
        } else if (o1.getLetter().equals("#")
                   || o2.getLetter().equals("@")) {
            return 1;
        } else {
            return o1.getLetter().compareTo(o2.getLetter());
        }
    }

}
