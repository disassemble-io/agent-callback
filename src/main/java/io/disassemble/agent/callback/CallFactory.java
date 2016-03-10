package io.disassemble.agent.callback;

import jdk.internal.org.objectweb.asm.Type;
import jdk.internal.org.objectweb.asm.commons.GeneratorAdapter;
import jdk.internal.org.objectweb.asm.commons.Method;
import jdk.internal.org.objectweb.asm.tree.AbstractInsnNode;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;
import jdk.internal.org.objectweb.asm.tree.VarInsnNode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * @author Tyler Sedlar
 * @since 1/23/2016
 */
public class CallFactory {

    private static HashMap<String, Long> calls = new HashMap<>();
    private static long last = -1;
    private static long callDelay = 0L;
    private static Predicate<InvokeCallback> predicate;

    public static void setCallDelay(long callDelay) {
        CallFactory.callDelay = callDelay;
    }

    public static long getLastCall() {
        return last;
    }

    public static void setInvokePredicate(Predicate<InvokeCallback> predicate) {
        CallFactory.predicate = predicate;
    }

    private static long millis() {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
    }

    public static void testInvoke(String key, Object... args) {
        long now = millis();
        if (!calls.containsKey(key) || (now - calls.get(key)) > callDelay) {
            last = now;
            if (predicate != null) {
                predicate.test(new InvokeCallback(key, args));
            }
            calls.put(key, now);
        }
    }

    public static void establishCallback(ClassNode cn, MethodNode mn) {
        if (!mn.desc.startsWith("()")) {
            MethodNode blank = new MethodNode();
            Method method = new Method(mn.name, mn.desc);
            GeneratorAdapter adapter = new GeneratorAdapter(mn.access, method, blank);
            adapter.visitCode();
            adapter.push(cn.name + "." + mn.name + "  " + mn.desc);
            adapter.loadArgArray();
            adapter.invokeStatic(Type.getType(CallFactory.class), new Method("testInvoke",
                    "(Ljava/lang/String;[Ljava/lang/Object;)V"));
            for (AbstractInsnNode ain : mn.instructions.toArray()) {
                if (ain instanceof VarInsnNode) {
                    mn.instructions.insertBefore(ain, blank.instructions);
                    return;
                }
            }
        }
    }
}