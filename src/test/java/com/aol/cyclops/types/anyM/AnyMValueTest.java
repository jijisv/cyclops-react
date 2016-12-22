package com.aol.cyclops.types.anyM;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import cyclops.monads.AnyM;
import cyclops.control.Maybe;
import cyclops.stream.ReactiveSeq;
import cyclops.collections.ListX;;

public class AnyMValueTest {

	@Test
	public void testFlatMapFirst() {
		List actualList = AnyM.fromMaybe(Maybe.just(10)).flatMapIterable(i->AnyM.fromList(ListX.of(i,20,30))).stream().toList();
		assertEquals(actualList.size(), 1);
		assertEquals(actualList.get(0), 10);
	}
	
	@Test
	public void testFlatMapFirst2() {
		List actualList = AnyM.fromMaybe(Maybe.just(-100)).flatMapIterable(i->AnyM.fromStream(ReactiveSeq.of(i,20,30))).stream().toList();
		assertEquals(actualList.size(), 1);
		assertEquals(actualList.get(0), -100);
	}
	
	@Test
	public void testFlatMapFirst3() {
		List actualList = AnyM.fromList(new ArrayList()).flatMap(i -> AnyM.fromStream(ReactiveSeq.of(i,20,30))).stream().toList();
		assertEquals(actualList.size(), 0);
	}
}
