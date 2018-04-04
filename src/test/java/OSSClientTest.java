import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.PutObjectResult;
import utils.OSSUtil;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Eric
 * @since 2018/3/26 11:21
 */
public class OSSClientTest {
    private static final String BUCKET_NAME = "iccfgtest0001";

    public static void main(String[] args) {
        OSSClient ossClient = OSSUtil.getOSSClient();
        String fileName = "ossclient_test.mp4";
        File file;
        try {
            file = new File("input2.mp4");
            PutObjectResult objectResult = ossClient.putObject(BUCKET_NAME, fileName, file);
            String objUri = objectResult.getResponse().getUri();
            System.out.println(objUri);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
