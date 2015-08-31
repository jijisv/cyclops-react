package com.aol.simple.react.stream.traits;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import uk.co.real_logic.agrona.concurrent.ManyToOneConcurrentArrayQueue;

import com.aol.simple.react.async.future.FastFuture;
import com.aol.simple.react.collectors.lazy.EmptyCollector;
import com.aol.simple.react.collectors.lazy.IncrementalReducer;
import com.aol.simple.react.collectors.lazy.LazyResultConsumer;
import com.aol.simple.react.config.MaxActive;
import com.aol.simple.react.exceptions.SimpleReactProcessingException;
import com.aol.simple.react.stream.LazyStreamWrapper;
import com.aol.simple.react.stream.MissingValue;
import com.aol.simple.react.stream.Runner;
import com.aol.simple.react.stream.ThreadPools;
import com.aol.simple.react.stream.lazy.ParallelReductionConfig;
import com.aol.simple.react.stream.simple.SimpleReact;
import com.aol.simple.react.threads.SequentialElasticPools;

public interface LazyStream<U> extends BlockingStream<U>{
	
	LazyStreamWrapper<U> getLastActive();
	Supplier<LazyResultConsumer<U>> getLazyCollector();
	
	
	Optional<Consumer<Throwable>> getErrorHandler();
	 ParallelReductionConfig getParallelReduction();
	 MaxActive getMaxActive();
	 
	/**
	 * Trigger a lazy stream as a task on the provided Executor
	 * 
	 * @param e
	 *            Executor service to trigger lazy stream on (Stream
	 *            CompletableFutures will use Executor associated with
	 *            this Stage may not be the same one).
	 * 
	 * 
	 */
	default void runOn(Executor e) {
		new SimpleReact(e).react(() -> run(new NonCollector()));

	}

	default void runThread(Runnable r) {
		Function<FastFuture,U> safeJoin = (FastFuture cf)->(U) BlockingStreamHelper.getSafe(cf,getErrorHandler());
		new Thread(() -> new Runner(r).run(getLastActive(),new EmptyCollector(getMaxActive()))).start();

	}
	
	default Continuation runContinuation(Runnable r) {
		Function<FastFuture,U> safeJoin = (FastFuture cf)->(U) BlockingStreamHelper.getSafe(cf,getErrorHandler());
		return new Runner(r).runContinuations(getLastActive(),new EmptyCollector(getMaxActive()));

	}
	/**
	 * Trigger a lazy stream
	 */
	default void runOnCurrent() {
		
		
		run(new NonCollector());

	}
	
	/**
	 * Trigger a lazy stream
	 */
	default void run() {
		SimpleReact reactor  = SequentialElasticPools.simpleReact.nextReactor();
		reactor.react(() -> run(new NonCollector())).peek(n-> SequentialElasticPools.simpleReact.populate(reactor));
		
	}

	/**
	 * Trigger a lazy stream and return the results in the Collection created by
	 * the collector
	 * 
	 * @param collector
	 *            Supplier that creates a collection to store results in
	 * @return Collection of results
	 */
	default <A,R> R run(Collector<U,A,R> collector) {
		/**
		if(getLastActive().isSequential()){ 
			//if single threaded we can simply push from each Future into the collection to be returned
			if(collector.supplier().get()==null){
				forEach(r->{});
				return null;
			}
			A col = collector.supplier().get();
			forEach(r->collector.accumulator().accept(col,r));
			return collector.finisher().apply(col);
		
		}**/
		Function<FastFuture,U> safeJoin = (FastFuture cf)->(U) BlockingStreamHelper.getSafe(cf,getErrorHandler());
		LazyResultConsumer<U> batcher = collector.supplier().get() != null ? getLazyCollector().get().withResults( new ArrayList<>()) : 
				new EmptyCollector(this.getMaxActive());

		try {
		
			this.getLastActive().injectFutures().forEach(n -> {
				System.out.println(n);
				batcher.accept(n);
			
				
			});
		} catch (SimpleReactProcessingException e) {
			
		}
		if (collector.supplier().get() == null)
			return null;
		
		return (R)batcher.getAllResults().stream()
									.map(cf -> BlockingStreamHelper.getSafe(cf,getErrorHandler()))
									.filter(v -> v != MissingValue.MISSING_VALUE)
									.collect((Collector)collector);
		

	}
	
	default Queue<U> collectToQueue(int bounds){
		ManyToOneConcurrentArrayQueue<U> result = new ManyToOneConcurrentArrayQueue<>(bounds);
		forEach(r->result.offer(r));
		return result;
	}
	default Queue<U> collectToQueue(){
		ConcurrentLinkedQueue<U> result = new ConcurrentLinkedQueue<>();
		forEach(r->result.offer(r));
		return result;
	}
	
	default void forEach(Consumer<? super U> c){
		
		EmptyCollector collector = new EmptyCollector(this.getMaxActive());
		try {
			
			this.getLastActive().operation(f-> f.peek(c)).injectFutures().forEach( next -> {
				collector.accept(next);
				
			});
		} catch (SimpleReactProcessingException e) {
			
		}
		

	}
	
	default Optional<U> reduce(BinaryOperator<U> accumulator){
		
		if(getLastActive().isSequential()){ 
			Object[] result = {null};
			forEach(r-> {
				if(result[0]==null)
					result[0]=r;
				else{
					result[0]=accumulator.apply((U)result[0], r);
				}
			});
			return (Optional)Optional.ofNullable(result[0]);
		
		}
		//async reduction could be done with 2 threads - thread[1] forEach : addToQueue,  thread[2] stream and reduce from queue
		IncrementalReducer<U> collector = new IncrementalReducer(this.getLazyCollector().get().withResults(new ArrayList<>()), this,
			getParallelReduction());
		Optional[] result =  {Optional.empty()};
		try {
			this.getLastActive().injectFutures().forEach(next -> {

				
				collector.getConsumer().accept(next);
				if(!result[0].isPresent())
					result[0] = collector.reduce(accumulator);
				else
					result[0] = result[0].map(v ->collector.reduce((U)v,accumulator));	
				
			});
		} catch (SimpleReactProcessingException e) {
			
		}
	
		 if(result[0].isPresent())
					return result[0].map(v-> collector.reduceResults(collector.getConsumer().getAllResults(), (U)v, accumulator));
			
			return		collector.reduceResults(collector.getConsumer().getAllResults(), accumulator);
			
	}
	default U reduce(U identity, BinaryOperator<U> accumulator){
		
		IncrementalReducer<U> collector = new IncrementalReducer(this.getLazyCollector().get().withResults(new ArrayList<>()), this,
			getParallelReduction());
		Object[] result =  {identity};
		try {
			this.getLastActive().injectFutures().forEach(next -> {

				
				collector.getConsumer().accept(next);
			
				result[0] = collector.reduce((U)result[0],accumulator);	
			});
		} catch (SimpleReactProcessingException e) {
			
		}
		return collector.reduceResults(collector.getConsumer().getAllResults(), (U)result[0], accumulator);
	}
	
	default<T> T reduce(T identity, BiFunction<T,? super U,T> accumulator, BinaryOperator<T> combiner){
	
		IncrementalReducer<U> collector = new IncrementalReducer(this.getLazyCollector().get().withResults(new ArrayList<>()), this,
			getParallelReduction());
		Object[] result =  {identity};
		try {
			this.getLastActive().injectFutures().forEach(next -> {

				
				collector.getConsumer().accept(next);
				result[0] = collector.reduce((T)result[0],accumulator,combiner);	
			});
		} catch (SimpleReactProcessingException e) {
			
		}
		return collector.reduceResults(collector.getConsumer().getAllResults(),(T)result[0], accumulator,combiner);
	}
	
	default <R> R collect(Supplier<R> supplier, BiConsumer<R,? super U> accumulator, BiConsumer<R,R> combiner){
		
		return (R)this.run((Collector)Collector.of(supplier, accumulator, ( a,b)-> { combiner.accept(a, b); return a;}));
		
	}
	
}