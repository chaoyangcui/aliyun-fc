package utils;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.OSSObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Eric
 * @since 2018/3/26 16:19
 */
public class OSSUtil {
    private static final String endpoint = "https://oss-cn-shanghai.aliyuncs.com";
    private static final String accessKeyId = "accessKeyId";
    private static final String secretAccessKey = "secretAccessKey";

    public static final String BUCKET_NAME = "iccfgtest0001";

    public static OSSClient getOSSClient() {
        return getOSSClient(endpoint, accessKeyId, secretAccessKey);
    }

    public static OSSClient getOSSClient(String endpoint, String accessKeyId, String secretAccessKey) {
        return new OSSClient(endpoint, accessKeyId, secretAccessKey);
    }

    /**
     * 从OSS中取文件并写入到相应文件中
     * @param ossClient OSSClient
     * @param objKey OSS Object Key
     * @param toFile toFile
     * @return toFile.exists();
     */
    public static boolean getAndWrite2File(OSSClient ossClient, final String objKey, File toFile) {
        InputStream inStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            OSSObject ossObject = ossClient.getObject(OSSUtil.BUCKET_NAME, objKey);
            inStream = ossObject.getObjectContent();
            fileOutputStream = new FileOutputStream(toFile);
            byte[] tmp = new byte[1024];
            int len;
            while ((len = inStream.read(tmp)) >= 0) {
                fileOutputStream.write(tmp, 0, len);
            }
            fileOutputStream.flush();
        } catch (Exception e) {
            FCUtils.getStackTrace(e);
            return false;
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException ignore) {}
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException ignore) {}
            }
        }

        return toFile.exists();
    }

}
