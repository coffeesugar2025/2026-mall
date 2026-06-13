package com.policy.common.collection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;


public class CollUtils {
    public static <T> List<List<T>> subList(List<T> list, int aFew) {
        if (aFew <= 0) {
            throw new IndexOutOfBoundsException();
        }
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        List<List<T>> arrayList = new ArrayList<>();
        int size = list.size();
        int i;
        for (i = 0; i < size; i += aFew) {
            List<T> subList = list.subList(i, Math.min(i + aFew, size));
            arrayList.add(subList);
        }
        return arrayList;
    }


    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }
}
