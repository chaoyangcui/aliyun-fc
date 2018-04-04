import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Eric
 * @since 2018/3/23 15:15
 */
public class Lambda {

    public static void main(String[] args) {
        List<String> list = new ArrayList<String>() {{
            add("a");
            add("b");
            add("c");
            add("d");
        }};

        AtomicInteger num = new AtomicInteger();
        list.forEach(e -> {
            if (num.getAndIncrement() == 0) {
                return;
            }
            System.out.println(e);
        });
    }

}
