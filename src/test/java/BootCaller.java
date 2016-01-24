import me.sedlar.agent.CallbackAgent;
import me.sedlar.agent.callback.CallFactory;

import java.util.Arrays;

/**
 * @author Tyler Sedlar
 * @since 1/23/16
 */
public class BootCaller {

    public static void main(String[] args) {
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
        Boot.main(args);
    }
}
