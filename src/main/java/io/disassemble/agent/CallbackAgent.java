package io.disassemble.agent;

import io.disassemble.agent.callback.CallFactory;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.instrument.Instrumentation;
import java.util.function.Predicate;

/**
 * @author Tyler Sedlar
 * @since 1/23/2016
 */
public class CallbackAgent extends Agent {

    private static CallbackAgent instance;

    private final Predicate<ClassNode> classPredicate;
    private final Predicate<MethodNode> methodPredicate;

    public CallbackAgent(Predicate<ClassNode> classPredicate, Predicate<MethodNode> methodPredicate) {
        super(CallbackAgent.class);
        this.classPredicate = classPredicate;
        this.methodPredicate = methodPredicate;
        CallbackAgent.instance = this;
    }

    @Override
    public void modify(ClassNode cn) {
        if (classPredicate.test(cn)) {
            cn.methods.stream()
                    .filter(methodPredicate::test)
                    .forEach(mn -> CallFactory.establishCallback(cn, mn));
        }
    }

    public static void premain(String args, Instrumentation instrumentation) {
        instrumentation.addTransformer(new CallbackAgent(instance.classPredicate, instance.methodPredicate));
    }

    public static void agentmain(String args, Instrumentation instrumentation) {
        instrumentation.addTransformer(new CallbackAgent(instance.classPredicate, instance.methodPredicate));
    }
}