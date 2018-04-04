import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Eric
 * @since 2018/3/27 14:39
 */
public class ThreadTest {
    private static ExecutorService executorService = Executors.newFixedThreadPool(10);


    public static void main(String[] args) {
        Runnable runnable = () -> {
            try {
                Thread.sleep(1000 * 30L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("inner thread last");
        };
        executorService.submit(runnable);

        System.out.println("main thread will exit");
    }

}
