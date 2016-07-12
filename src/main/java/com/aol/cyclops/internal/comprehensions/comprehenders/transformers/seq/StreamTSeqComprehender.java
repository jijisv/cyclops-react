public interface ForFlux {

		static <T1, T2, T3, R1, R2, R3, R> Flux<R> each4(Flux<? extends T1> value1,
				Function<? super T1, ? extends Flux<R1>> value2,
				BiFunction<? super T1, ? super R1, ? extends Flux<R2>> value3,
				TriFunction<? super T1, ? super R1, ? super R2, ? extends Flux<R3>> value4,
				QuadFunction<? super T1, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

			return AnyM.ofSeq(For.anyM(flux(value1)).anyM(a -> flux(value2.apply(a))).anyM(a -> b -> flux(value3.apply(a, b)))
							.anyM(a -> b -> c -> flux(value4.apply(a, b, c))).yield4(yieldingFunction).unwrap())
					.unwrap();

		}

		static <T1, T2, T3, R1, R2, R3, R> Flux<R> each4(Flux<? extends T1> value1,
				Function<? super T1, ? extends Flux<R1>> value2,
				BiFunction<? super T1, ? super R1, ? extends Flux<R2>> value3,
				TriFunction<? super T1, ? super R1, ? super R2, ? extends Flux<R3>> value4,
				QuadFunction<? super T1, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
				QuadFunction<? super T1, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

			return AnyM.ofSeq(For.anyM(flux(value1)).anyM(a -> flux(value2.apply(a)))
					.anyM(a -> b -> flux(value3.apply(a, b))).anyM(a -> b -> c -> flux(value4.apply(a, b, c)))
					.filter(a -> b -> c -> d -> filterFunction.apply(a, b, c, d)).yield4(yieldingFunction).unwrap())
					.unwrap();

		}

		static <T1, T2, R1, R2, R> Flux<R> each3(Flux<? extends T1> value1,
				Function<? super T1, ? extends Flux<R1>> value2,
				BiFunction<? super T1, ? super R1, ? extends Flux<R2>> value3,
				TriFunction<? super T1, ? super R1, ? super R2, ? extends R> yieldingFunction) {

			return AnyM.ofSeq(For.anyM(flux(value1)).anyM(a -> flux(value2.apply(a)))
					.anyM(a -> b -> flux(value3.apply(a, b))).yield3(yieldingFunction).unwrap()).unwrap();
			

		}

		static <T1, T2, R1, R2, R> Flux<R> each3(Flux<? extends T1> value1,
				Function<? super T1, ? extends Flux<R1>> value2,
				BiFunction<? super T1, ? super R1, ? extends Flux<R2>> value3,
				TriFunction<? super T1, ? super R1, ? super R2, Boolean> filterFunction,
				TriFunction<? super T1, ? super R1, ? super R2, ? extends R> yieldingFunction) {

			return AnyM
					.ofSeq(For.anyM(flux(value1)).anyM(a -> flux(value2.apply(a))).anyM(a -> b -> flux(value3.apply(a, b)))
							.filter(a -> b -> c -> filterFunction.apply(a, b, c)).yield3(yieldingFunction).unwrap())
					.unwrap();
			

		}

		static <T, R1, R> Flux<R> each2(Flux<? extends T> value1, Function<? super T, Flux<R1>> value2,
				BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

			return AnyM.ofSeq(For.anyM(flux(value1)).anyM(a -> flux(value2.apply(a))).yield2(yieldingFunction).unwrap())
					.unwrap();
			

		}

		static <T, R1, R> Flux<R> each2(Flux<? extends T> value1, Function<? super T, ? extends Flux<R1>> value2,
				BiFunction<? super T, ? super R1, Boolean> filterFunction,
				BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

			return AnyM.ofSeq(For.anyM(flux(value1)).anyM(a -> flux(value2.apply(a)))
					.filter(a -> b -> filterFunction.apply(a, b)).yield2(yieldingFunction).unwrap()).unwrap();
			

		}
	}