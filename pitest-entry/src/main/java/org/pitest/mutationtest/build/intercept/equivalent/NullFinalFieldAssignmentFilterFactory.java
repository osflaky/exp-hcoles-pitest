package org.pitest.mutationtest.build.intercept.equivalent;

import org.pitest.mutationtest.build.InterceptorParameters;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.plugin.Feature;

public class NullFinalFieldAssignmentFilterFactory implements MutationInterceptorFactory {

    @Override
    public String description() {
        return "Final field null assignment equivalent mutant filter";
    }

    @Override
    public Feature provides() {
        return Feature.named("NULLFINALS")
                .withOnByDefault(true)
                .withDescription("Filters equivalent mutations to null final field assignments");
    }

    @Override
    public MutationInterceptor createInterceptor(InterceptorParameters params) {
        return new NullFinalFieldAssignmentFilter();
    }

}
