package com.writenbite.bisonfun.api.service;

import org.apache.commons.lang3.tuple.Pair;

public interface PairRelationHandler<L, R> {
    /**
     * @param left first element of relation
     * @param right second element of relation
     * @param priority which element should be used in case of 'tie'
     * @return proper pair of related elements
     */
    Pair<L, R> handleRelation(L left, R right, Priority priority);

    enum Priority{
        LEFT,
        RIGHT
    }
}
