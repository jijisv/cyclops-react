package com.aol.cyclops.internal.stream.spliterators;

import com.aol.cyclops.util.ExceptionSoftener;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;

public class RecoverSpliterator<T, X extends Throwable> implements Spliterator<T> {
    private final Spliterator<T> source;
    private final Function<? super X, ? extends T> fn;
    private final Class<X> type;

    public RecoverSpliterator(Spliterator<T> source,  Function<? super X, ? extends T> fn, Class<X> type) {
        this.source=source;
        this.fn = fn;
        this.type = type;
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        try {
            boolean b = source.tryAdvance(action);
            return b;
        } catch(Throwable t) {
            if (type.isAssignableFrom(t.getClass())) {
                action.accept(fn.apply((X)t));
                return true;
            }
            throw ExceptionSoftener.throwSoftenedException(t);
        }

    }

    @Override
    public Spliterator<T> trySplit() {
        return this;
    }

    @Override
    public long estimateSize() {
        return source.estimateSize();
    }

    @Override
    public int characteristics() {
        return source.characteristics() & ~(Spliterator.SORTED | Spliterator.DISTINCT);
    }
}