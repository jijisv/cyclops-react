package cyclops.typeclasses;

import com.aol.cyclops2.hkt.Higher;
import cyclops.collections.immutable.VectorX;
import cyclops.collections.mutable.ListX;
import cyclops.monads.Witness;
import cyclops.monads.Witness.list;
import cyclops.monads.Witness.reactiveSeq;
import cyclops.monads.Witness.vectorX;
import cyclops.stream.ReactiveSeq;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;


public class NaturalTransformationTest {

    NaturalTransformation<reactiveSeq,list> streamToList = new NaturalTransformation<reactiveSeq, list>() {
        @Override
        public <T> Higher<list, T> apply(Higher<reactiveSeq, T> a) {
            return a.convert(ReactiveSeq::narrowK).toListX();
        }
    };
    NaturalTransformation<list,vectorX> listToVectorX = new NaturalTransformation<list, vectorX>() {
        @Override
        public <T> Higher<vectorX, T> apply(Higher<list, T> a) {
            return a.convert(ListX::narrowK).to().vectorX();
        }
    };

    @Test
    public void streamToList(){
        assertThat(streamToList.apply(ReactiveSeq.of(1,2,3)),equalTo(ListX.of(1,2,3)));
    }
    @Test
    public void streamToListAndThenToVectorX(){
        assertThat(streamToList.andThen(listToVectorX).apply(ReactiveSeq.of(1,2,3)),equalTo(VectorX.of(1,2,3)));
    }
    @Test
    public void compose(){
        assertThat(listToVectorX.compose(streamToList).apply(ReactiveSeq.of(1,2,3)),equalTo(VectorX.of(1,2,3)));
    }
}