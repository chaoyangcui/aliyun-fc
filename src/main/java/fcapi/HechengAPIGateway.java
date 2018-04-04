package fcapi;

import com.aliyun.fc.runtime.Context;
import com.aliyun.fc.runtime.StreamRequestHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 视频合成网关入口
 *
 * @author Eric
 * @since 2018/4/4 10:01
 *     <p>Created by IntelliJ IDEA.
 */
public class HechengAPIGateway implements StreamRequestHandler {
    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
            throws IOException {}
}
