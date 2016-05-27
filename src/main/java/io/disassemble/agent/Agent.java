package io.disassemble.agent;

import com.sun.tools.attach.VirtualMachine;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.security.ProtectionDomain;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * @author Tyler Sedlar
 * @since 1/23/2016
 */
public abstract class Agent implements ClassFileTransformer {

    private Class<?> agent;

    private String pid;
    private VirtualMachine vm;

    private boolean detached = false;

    private String getPid() {
        RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
        String pid = bean.getName();
        if (pid.contains("@")) {
            pid = pid.substring(0, pid.indexOf("@"));
        }
        return pid;
    }

    public Agent(Class<?> agent) {
        this.agent = agent;
    }

    public void attach() {
        try {
            this.pid = getPid();
            Manifest manifest = new Manifest();
            File jar = File.createTempFile("agent-" + pid, ".jar");
            jar.deleteOnExit();
            Attributes attributes = manifest.getMainAttributes();
            attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
            attributes.put(new Attributes.Name("Agent-Class"), agent.getName());
            attributes.put(new Attributes.Name("Can-Retransform-Classes"), "true");
            attributes.put(new Attributes.Name("Can-Redefine-Classes"), "true");
            try (JarOutputStream output = new JarOutputStream(new FileOutputStream(jar), manifest)) {
                output.flush();
            }
            this.vm = VirtualMachine.attach(pid);
            vm.loadAgent(jar.getAbsolutePath());
            System.out.println("Agent#" + pid + " attached");
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    detached = true;
                    vm.detach();
                    System.out.println("Agent#" + pid + " detached");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));
        } catch (Exception e) {
            throw new IllegalArgumentException("Not an valid agent");
        }
    }

    public void detach() {
        detached = true;
    }

    public boolean attached() {
        return !detached;
    }

    public abstract void modify(ClassNode cn);

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> redefine, ProtectionDomain domain,
                            byte[] buffer) throws IllegalClassFormatException {
        ClassNode cn = new ClassNode();
        ClassReader reader = new ClassReader(buffer);
        reader.accept(cn, 0);
        modify(cn);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cn.accept(writer);
        return writer.toByteArray();
    }
}