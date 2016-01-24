package me.sedlar.agent.callback;

/**
 * @author Tyler Sedlar
 * @since 1/23/16
 */
public class InvokeCallback {

    public final String methodKey;
    public final Object[] args;

    public InvokeCallback(String methodKey, Object... args) {
        this.methodKey = methodKey;
        this.args = args;
    }
}
