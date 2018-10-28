package com.indexrecyclerview.comparator;

import com.indexrecyclerview.model.EntityWrapper;
import com.indexrecyclerview.model.IndexableEntity;

import java.util.Comparator;

/**
 * Created by YoKey on 16/10/14.
 */
public class InitialComparator<T extends IndexableEntity> implements Comparator<EntityWrapper<T>> {
    @Override
    public int compare(EntityWrapper<T> lhs, EntityWrapper<T> rhs) {
        return lhs.getIndex().compareTo(rhs.getIndex());
    }
}
