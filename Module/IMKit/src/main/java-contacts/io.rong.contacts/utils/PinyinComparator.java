package io.rong.contacts.utils;


import com.common.core.userinfo.UserInfoModel;

import java.util.Comparator;

/**
 *
 * @author
 *
 */
public class PinyinComparator implements Comparator<UserInfoModel> {


    public static PinyinComparator instance = null;

    public static PinyinComparator getInstance() {
        if (instance == null) {
            instance = new PinyinComparator();
        }
        return instance;
    }

    public int compare(UserInfoModel o1, UserInfoModel o2) {
        if (o1.getLetter().equals("@")
                || o2.getLetter().equals("#")) {
            return -1;
        } else if (o1.getLetter().equals("#q")
                   || o2.getLetter().equals("@")) {
            return 1;
        } else {
            return o1.getLetter().compareTo(o2.getLetter());
        }
    }

}
