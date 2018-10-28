package com.indexrecyclerview.comparator;

import android.support.annotation.NonNull;

import com.common.utils.U;
import com.indexrecyclerview.model.EntityWrapper;
import com.indexrecyclerview.model.IndexableEntity;

import java.util.Comparator;

/**
 * Created by YoKey on 16/10/7.
 */
public class PinyinComparator<T extends IndexableEntity> implements Comparator<EntityWrapper<T>> {

    @Override
    public int compare(EntityWrapper<T> lhs, EntityWrapper<T> rhs) {
        String lhsIndexName = lhs.getIndexByField();
        String rhsIndexName = rhs.getIndexByField();

        if (lhsIndexName == null) {
            lhsIndexName = "";
        }
        if (rhsIndexName == null) {
            rhsIndexName = "";
        }
        return compareIndexName(lhsIndexName.trim(), rhsIndexName.trim());
    }

    private int compareIndexName(String lhs, String rhs) {
        int index = 0;

        String lhsWord = getWord(lhs, index);
        String rhsWord = getWord(rhs, index);
        while (lhsWord.equals(rhsWord) && !lhsWord.equals("")) {
            index++;
            lhsWord = getWord(lhs, index);
            rhsWord = getWord(rhs, index);
        }
        return lhsWord.compareTo(rhsWord);
    }

    @NonNull
    private String getWord(String indexName, int index) {
        if (indexName.length() < (index + 1)) return "";
        String firstWord;
        if (U.getPinyinUtils().matchingPolyphone(indexName)) {
            String hanzi = U.getPinyinUtils().getPolyphoneRealHanzi(indexName);
            firstWord = U.getPinyinUtils().getPingYin(hanzi.substring(index, index + 1));
        } else {
            firstWord = U.getPinyinUtils().getPingYin(indexName.substring(index, index + 1));
        }
        return firstWord;
    }
}
