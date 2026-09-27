package org.pitest.mutationtest.build.intercept.equivalent;

import org.junit.Test;
import org.pitest.bytecode.analysis.OpcodeMatchers;
import org.pitest.mutationtest.engine.gregor.mutators.NullMutateEverything;
import org.pitest.verifier.interceptors.InterceptorVerifier;
import org.pitest.verifier.interceptors.VerifierStart;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Prevent equivalent mutations from being generated for final fields. The functionality
 * is logically tied to the MemberVariableMutator, which effectively assigns null to final fields,
 * but the filter isn't tied to it via code since no other built-in mutator targets fields and
 * new ones probably shouldn't.
 *
 * The filter is designed to deal with the situation where there is a conditional null write to a final
 * field. e.g.
 *
 * public class Example {
 *     private final List items; // effectively a null assignment here
 *
 *     public Example(Collection input) {
 *         if (input == null) {
 *             this.items = null;  // should not be mutated (1)
 *         } else {
 *             this.items = new ArrayList<>(input); (2)
 *         }
 *     }
 * }
 *
 * Here, the null assignment (1) cannot be removed as it would result in a compilation error. Although the same is true
 * for assignment (2), this can at least be detected by tests. As (1) is both impossible AND equivalent, it is problematic.
 *
 * A better solution would be to prevent mutation of conditional assignments to final fields, but this would require
 * more work and use of the MemberVariableMutator is already strongly discouraged
 */
public class NullFinalFieldAssignmentFilterTest {
    NullFinalFieldAssignmentFilter underTest = new NullFinalFieldAssignmentFilter();
    InterceptorVerifier v = VerifierStart.forInterceptor(underTest)
            .usingMutator(new NullMutateEverything());

    @Test
    public void filtersNullAssignmentToFinalField() {
        v.forClass(HasSimpleFinalVariableNullAssignment.class)
                .forCodeMatching(OpcodeMatchers.PUTFIELD.asPredicate())
                .mutantsAreGenerated()
                .allMutantsAreFiltered()
                .verify();
    }

    @Test
    public void doesNotFilterNonFinalFieldAssignment() {
        v.forClass(HasSimpleNonFinalVariableNullAssignment.class)
                .forCodeMatching(OpcodeMatchers.PUTFIELD.asPredicate())
                .mutantsAreGenerated()
                .noMutantsAreFiltered()
                .verify();
    }

    @Test
    public void filtersNullAssignmentToStaticFinalField() {
        v.forClass(HasStaticFinalVariableNullAssignment.class)
                .forCodeMatching(OpcodeMatchers.PUTSTATIC.asPredicate())
                .mutantsAreGenerated()
                .allMutantsAreFiltered()
                .verify();
    }

    @Test
    public void doesNotFilterStaticNonFinalFieldAssignment() {
        v.forClass(HasStaticNonFinalVariableNullAssignment.class)
                .forCodeMatching(OpcodeMatchers.PUTSTATIC.asPredicate())
                .mutantsAreGenerated()
                .noMutantsAreFiltered()
                .verify();
    }

    @Test
    public void filtersConditionalNullAssignmentToFinalField() {
        v.forClass(HasConditionalFinalVariableNullAssignment.class)
                .forCodeMatching(OpcodeMatchers.PUTFIELD.asPredicate())
                .mutantsAreGenerated()
                .nMutantsAreFiltered(1)
                .verify();
    }
}

class HasSimpleFinalVariableNullAssignment {

    private final Integer member2 = null;

    public Integer call() {
        return this.member2;
    }
}

class HasSimpleNonFinalVariableNullAssignment {

    private Integer member2 = null;

    public Integer call() {
        return this.member2;
    }
}

class HasStaticFinalVariableNullAssignment {

    private static final Integer member2 = null;

    public Integer call() {
        return member2;
    }
}

class HasStaticNonFinalVariableNullAssignment {

    private static Integer member2 = null;

    public Integer call() {
        return member2;
    }
}

class HasConditionalFinalVariableNullAssignment {
    private final List items;

    public HasConditionalFinalVariableNullAssignment(Collection input) {
        if (input == null) {
            this.items = null;
        } else {
            this.items = new ArrayList<>(input);
        }
    }
}