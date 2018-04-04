package fc;

import com.aliyun.fc.runtime.Context;
import com.aliyun.fc.runtime.StreamRequestHandler;
import utils.FfmpegUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Eric
 * @since 2018/4/4 15:10
 * <p>Created by IntelliJ IDEA.
 */
public class ListfileTmp implements StreamRequestHandler {
    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        String lstmp = FfmpegUtil.shellWithOutput("ls /tmp/");
        outputStream.write(lstmp.getBytes());
    }
}
