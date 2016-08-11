package com.aol.cyclops.react;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;

import com.aol.cyclops.types.futurestream.ConfigurableStream;
import com.aol.cyclops.util.ExceptionSoftener;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class StageWithResults<RS, U> {

    private final Executor taskExecutor;

    private final ConfigurableStream<U, Object> stage;
    @Getter
    private final RS results;

    public StageWithResults(ConfigurableStream<U, Object> stage, RS results) {

        this.taskExecutor = stage.getTaskExecutor();
        this.stage = stage;
        this.results = results;
    }

    /**
     * This method allows the SimpleReact Executor to be reused by JDK parallel streams. It is best used when
     * collectResults and block are called explicitly for finer grained control over the blocking conditions.
     * 
     * @param fn Function that contains parallelStream code to be executed by the SimpleReact ForkJoinPool (if configured)
     */
    public <R> R submit(Function<RS, R> fn) {
        return submit(() -> fn.apply(this.results));
    }

    /**
     * This method allows the SimpleReact Executor to be reused by JDK parallel streams
     * 
     * @param callable that contains code
     */
    public <T> T submit(Callable<T> callable) {
        if (taskExecutor instanceof ForkJoinPool) {
            log.debug("Submited callable to SimpleReact ForkJoinPool. JDK ParallelStreams will reuse SimpleReact ForkJoinPool.");
            try {

                return ((ForkJoinPool) taskExecutor).submit(callable)
                                                    .get();
            } catch (ExecutionException e) {
                throw ExceptionSoftener.throwSoftenedException(e);

            } catch (InterruptedException e) {
                Thread.currentThread()
                      .interrupt();
                throw ExceptionSoftener.throwSoftenedException(e);

            }
        }
        try {
            log.debug("Submited callable but do not have a ForkJoinPool. JDK ParallelStreams will use Common ForkJoinPool not SimpleReact Executor.");
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(
                                       e);
        }
    }

}
