package org.pitest.mutationtest.build.intercept.equivalent;

import static org.pitest.bytecode.analysis.InstructionMatchers.anyInstruction;
import static org.pitest.bytecode.analysis.InstructionMatchers.notAnInstruction;
import static org.pitest.bytecode.analysis.OpcodeMatchers.ACONST_NULL;
import static org.pitest.bytecode.analysis.OpcodeMatchers.PUTFIELD;
import static org.pitest.bytecode.analysis.OpcodeMatchers.PUTSTATIC;
import static org.pitest.sequence.Result.result;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.bytecode.analysis.MethodTree;
import org.pitest.mutationtest.build.intercept.Region;
import org.pitest.mutationtest.build.intercept.RegionInterceptor;
import org.pitest.sequence.Context;
import org.pitest.sequence.Match;
import org.pitest.sequence.QueryParams;
import org.pitest.sequence.QueryStart;
import org.pitest.sequence.SequenceMatcher;
import org.pitest.sequence.Slot;
import org.pitest.sequence.SlotWrite;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class NullFinalFieldAssignmentFilter extends RegionInterceptor {

    private Set<String> finalFields;

    private static final Slot<AbstractInsnNode> MUTATED_INSTRUCTION = Slot.create(AbstractInsnNode.class);

    private final SequenceMatcher<AbstractInsnNode> matcher = QueryStart
            .any(AbstractInsnNode.class)
            .then(ACONST_NULL)
            .then(PUTFIELD.or(PUTSTATIC).and(isFinalField()).and(store(MUTATED_INSTRUCTION.write())))
            .zeroOrMore(QueryStart.match(anyInstruction()))
            .compile(QueryParams.params(AbstractInsnNode.class)
                    .withIgnores(notAnInstruction())
            );

    @Override
    public void begin(ClassTree clazz) {
        super.begin(clazz);
        finalFields = clazz.rawNode().fields.stream()
                .filter(f -> (f.access & Opcodes.ACC_FINAL) != 0)
                .map(f -> f.name)
                .collect(Collectors.toSet());
    }

    @Override
    protected List<Region> computeRegions(MethodTree method) {
        if (!method.rawNode().name.equals("<init>") && !method.rawNode().name.equals("<clinit>")) {
            return List.of();
        }

        Context context = Context.start();
        return matcher.contextMatches(method.instructions(), context).stream()
                .map(c -> new Region(c.retrieve(MUTATED_INSTRUCTION.read()).get(), c.retrieve(MUTATED_INSTRUCTION.read()).get()))
                .collect(Collectors.toList());
    }

    private Match<AbstractInsnNode> isFinalField() {
        return (c, n) -> {
            if (!(n instanceof FieldInsnNode)) {
                return result(false, c);
            }
            FieldInsnNode insn = (FieldInsnNode) n;
            return result(fieldIsFinal(insn), c);
        };
    }

    private boolean fieldIsFinal(FieldInsnNode insn) {
        return finalFields.contains(insn.name);
    }

    private static Match<AbstractInsnNode> store(SlotWrite<AbstractInsnNode> slot) {
        return (c, n) -> result(true, c.store(slot, n));
    }
}
