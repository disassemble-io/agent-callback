/**
 * @author Tyler Sedlar
 * @since 1/23/16
 */
public class Boot {

    private static void method(int i) {
        System.out.println("test - " + i);
    }

    public static void main(String[] args) {
        new Thread(() -> {
            for (int i = 0; i < 20; i++) {
                try {
                    method(i);
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
