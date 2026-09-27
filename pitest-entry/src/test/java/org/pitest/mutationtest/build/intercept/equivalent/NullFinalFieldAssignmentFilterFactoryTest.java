package org.pitest.mutationtest.build.intercept.equivalent;

import org.junit.Test;
import org.pitest.mutationtest.engine.gregor.mutators.experimental.MemberVariableMutator;
import org.pitest.verifier.interceptors.FactoryVerifier;
import org.pitest.verifier.interceptors.InterceptorVerifier;
import org.pitest.verifier.interceptors.VerifierStart;


public class NullFinalFieldAssignmentFilterFactoryTest {
    NullFinalFieldAssignmentFilterFactory underTest = new NullFinalFieldAssignmentFilterFactory();

    InterceptorVerifier v = VerifierStart.forInterceptorFactory(underTest)
            .usingMutator(new MemberVariableMutator());

    @Test
    public void isOnChain() {
        FactoryVerifier.confirmFactory(underTest)
                .isOnChain();
    }

    @Test
    public void isOnByDefault() {
        FactoryVerifier.confirmFactory(underTest)
                .isOnByDefault();
    }

    @Test
    public void filtersNullFinalMutantsFromTheMemberVariableMutator() {
        v.forClass(HasConditionalFinalVariableNullAssignment.class)
                .forAnyCode()
                .mutantsAreGenerated()
                .nMutantsAreFiltered(1)
                .verify();
    }

    @Test
    public void doesntFilterOtherCode() {
        v.forClass(NullFinalFieldAssignmentFilterFactoryTest.class)
                .forAnyCode()
                .mutantsAreGenerated()
                .noMutantsAreFiltered()
                .verify();
    }
}