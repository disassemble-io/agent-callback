import io.disassemble.agent.CallbackAgent;
import io.disassemble.agent.callback.CallFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author Tyler Sedlar
 * @since 1/23/16
 */
public class JarCaller {

    private static void executeJar(String jarPath, String[] args) throws Exception {
        JarFile jar = new JarFile(jarPath);
        String main = jar.getManifest().getMainAttributes().getValue("Main-Class");
        if (main != null) {
            URLClassLoader loader = new URLClassLoader(new URL[] {new File(jarPath).toURI().toURL()});
            Class<?> mainClass = loader.loadClass(main);
            Method mainMethod = mainClass.getMethod("main", String[].class);
            mainMethod.invoke(null, new Object[] {args});
        }
    }

    public static void main(String[] args) throws Exception {
        new CallbackAgent(
                cn -> cn.name.contains("Boot"),
                mn -> mn.name.equals("method")
        ).attach();
        CallFactory.setCallDelay(2000L);
        CallFactory.setInvokePredicate(callback -> {
            String methodKey = callback.methodKey;
            Object[] methodArgs = callback.args;
            System.out.println("call: " + methodKey + " @ " + Arrays.deepToString(methodArgs));
            return true;
        });
        executeJar("./src/test/Boot.jar", args);
    }
}
