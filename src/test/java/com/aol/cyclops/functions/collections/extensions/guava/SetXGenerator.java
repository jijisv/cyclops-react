package com.aol.cyclops.functions.collections.extensions.guava;

import java.util.Set;

import cyclops.collections.SetX;
import com.google.common.collect.testing.TestStringSetGenerator;

public class SetXGenerator extends  TestStringSetGenerator {

    
    @Override
    public Set<String> create(String... elements) {
       return SetX.of(elements);
    }

}
