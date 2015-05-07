package com.aol.cyclops.trycatch;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import com.aol.cyclops.lambda.api.Comprehender;

/**
 * Comprehender for performant for-comprehensions for Try 
 * 
 * @author johnmcclean
 *
 */
public class TryComprehender implements Comprehender<Try> {

	/* 
	 * @see com.aol.cyclops.lambda.api.Comprehender#filter(java.lang.Object, java.util.function.Predicate)
	 */
	@Override
	public Object filter(Try t, Predicate p) {
		return t.filter(p);
	}

	/* 
	 * @see com.aol.cyclops.lambda.api.Comprehender#map(java.lang.Object, java.util.function.Function)
	 */
	@Override
	public Object map(Try t, Function fn) {
		return t.map(fn);
	}

	/* 
	 * @see com.aol.cyclops.lambda.api.Comprehender#flatMap(java.lang.Object, java.util.function.Function)
	 */
	@Override
	public Try flatMap(Try t, Function fn) {
		return t.flatMap(fn);
	}

	/* 
	 * @see com.aol.cyclops.lambda.api.Comprehender#instanceOfT(java.lang.Object)
	 */
	@Override
	public boolean instanceOfT(Object apply) {
		if(apply instanceof Try)
			return true;
		
		return false;
	}

	/* 
	 * @see com.aol.cyclops.lambda.api.Comprehender#of(java.lang.Object)
	 */
	@Override
	public Try of(Object o) {
		if(o instanceof Throwable)
			return Failure.of((Throwable)o);
		return Success.of(o);
	}

	/* 
	 * @see com.aol.cyclops.lambda.api.Comprehender#of()
	 */
	@Override
	public Try of() {
		return Success.of(Optional.empty());
	}

	

}
