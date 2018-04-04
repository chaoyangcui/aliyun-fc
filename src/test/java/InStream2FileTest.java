import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Eric
 * @since 2018/3/26 16:31
 */
public class InStream2FileTest {

    public static void main(String[] args) {
        // System.out.println(new File("target/test-classes/src.txt").getAbsoluteFile());
        try {
            InputStream inStream = new FileInputStream(new File("target/test-classes/src.txt"));
            FileOutputStream toFileOutStream = new FileOutputStream(new File("target/test-classes/dest.txt"));
            byte[] tmp = new byte[1024];
            int len;
            while ((len = inStream.read(tmp)) >= 0) {
                toFileOutStream.write(tmp, 0, len);
            }

            toFileOutStream.flush();

            inStream.close();
            toFileOutStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
