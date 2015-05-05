package com.aol.cyclops.matcher.builders;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import lombok.AllArgsConstructor;

import org.hamcrest.Matcher;
import org.jooq.lambda.Seq;

import com.aol.cyclops.lambda.utils.ImmutableClosedValue;
import com.aol.cyclops.matcher.Extractors;

/**
 * Predicate Builder for Algebraic Data Types
 * Can be used to recursively match on ADTs
 * 
 * @author johnmcclean
 *
 * @param <T>
 */
@AllArgsConstructor
public class ADTPredicateBuilder<T>{
		private final Class<T> type;
		
		
		
		Predicate toPredicate(){
			
			return t ->  Optional.of(t).map(v->type.isAssignableFrom(v.getClass())).orElse(false);
		}
		
		/**
		 * Generate a predicate that determines the provided values hold.
		 * Values can be comparison value, JDK 8 Predicate, or Hamcrest Matcher  
		 * for each Element to match on.
		 *
		 * isType will attempt to match on the type of the supplied Case class. If it matches the Case class will be 'decomposed' via it's unapply method
		 * and the Case will then attempt to match on each of the elements that make up the Case class. If the Case class implements Decomposable, that interface and it's
		 * unapply method will be used. Otherwise in Extractors it is possible to register Decomposition Funcitons that will unapply Case classes from other sources (e.g.
		 * javaslang, jADT or even Scala). If no Decomposition Function has been registered, reflection will be used to call an unapply method on the Case class if it exists.
		 * 
		 * @see com.aol.cyclops.matcher.Extractors#decompose
		 * @see com.aol.cyclops.matcher.Extractors#registerDecompositionFunction
		 * 
		 * @param values Matching rules for each element in the decomposed / unapplied user input
		 * @return A single Predicate encompassing supplied rules
		 */
		public<V> Predicate with(V... values){
			Seq<Predicate> predicates = Seq.of(values).map(nextValue->convertToPredicate(nextValue));
			ImmutableClosedValue val = new ImmutableClosedValue();
			return t -> toPredicate().test(t) && SeqUtils.seq(val.getOrSet(()->Extractors.decompose().apply(t))).count() >= values.length
					  	&& SeqUtils.seq(val.getOrSet(()->Extractors.decompose().apply(t)))
							.zip(predicates).map(tuple -> tuple.v2.test(tuple.v1))
							.allMatch(v->v==true);
		}
		private Predicate convertToPredicate(Object o){
			if(o instanceof Predicate)
				return (Predicate)o;
			if(o instanceof Matcher)
				return test -> ((Matcher)o).matches(test);
				
			return test -> Objects.equals(test,o);
		}

	
}