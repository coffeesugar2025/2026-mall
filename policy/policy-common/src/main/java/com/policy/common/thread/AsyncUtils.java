package com.policy.common.thread;

import com.policy.common.collection.CollUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Stream;


public class AsyncUtils {
    public static <T, OUT> List<OUT> batch(ExecutorService executorService, List<T> list, int aFew, BatchExecutor<T, OUT> batchExecutor) {
        List<List<T>> lists = CollUtils.subList(list, aFew);
        if (lists.isEmpty()) {
            return Collections.emptyList();
        }
        return batch(executorService, lists, batchExecutor);
    }

    public static <T, OUT> List<OUT> batch(ExecutorService executorService, List<List<T>> lists, BatchExecutor<T, OUT> batchExecutor) {
        List<Future<List<OUT>>> futures = new ArrayList<>();
        for (Iterator<List<T>> iterator = lists.iterator(); iterator.hasNext(); ) {
            List<T> list = iterator.next();
            Future<List<OUT>> future = executorService.submit(() -> {
                List<OUT> outs = new ArrayList<>(list.size());
                for (T in : list) {
                    OUT out = null;
                    try {
                        out = batchExecutor.async(in);
                    } catch (Exception e) {
                        e.printStackTrace();
                        batchExecutor.onError(in, e);
                    }
                    outs.add(out);
                }
                return outs;
            });
            futures.add(future);
        }

        List<OUT> values = null;

        for (Future<List<OUT>> future : futures) {
            try {
                List<OUT> value = future.get();
                if (values == null) {
                    values = value;
                    continue;
                }
                values.addAll(value);
            } catch (Exception e) {
                e.printStackTrace();

                Stream.<Future<List<OUT>>>of(future).filter(f -> !f.isDone()).forEach(m -> m.cancel(true));


                return null;
            }
        }
        return values;
    }

    @SafeVarargs
    public static <OUT> List<OUT> merge(ExecutorService executorService, boolean ignoreException, Concurrent<OUT>... concurrents) {
        List<Future<OUT>> futures = new ArrayList<>();
        for (Concurrent<OUT> concurrent : concurrents) {
            Future<OUT> future = executorService.submit(() -> {
                OUT out = null;
                try {
                    out = concurrent.async();
                } catch (Exception e) {
                    if (ignoreException) {
                        e.printStackTrace();
                    } else {
                        throw e;
                    }
                }
                return  out;
            });

            futures.add(future);
        }
        List<OUT> values = new ArrayList<>();
        for (Future<OUT> future : futures) {
            try {
                OUT value = future.get();
                values.add(value);
            } catch (Exception e) {
                e.printStackTrace();

                Stream.<Future<OUT>>of(future).filter(f -> !f.isDone()).forEach(m -> m.cancel(true));


                return null;
            }
        }
        return values;
    }
}
