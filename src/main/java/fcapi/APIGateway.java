package fcapi;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.aliyun.fc.runtime.Context;
import com.aliyun.fc.runtime.StreamRequestHandler;
import com.aliyuncs.fc.constants.Const;
import com.aliyuncs.fc.response.InvokeFunctionResponse;
import utils.FCUtils;
import utils.MD5;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static utils.FCUtils.isBlank;

/**
 * 视频处理网关入口
 * <pre>
 *     mkf api-video-process -h fcapi.APIGateway::handleRequest --runtime java8 -d ./classes/
 *     upf api-video-process -h fcapi.APIGateway::handleRequest --runtime java8 -d ./classes/
 * </pre>
 *
 * @author Eric
 * @since 2018/4/4 10:01
 *     <p>Created by IntelliJ IDEA.
 */
public class APIGateway implements StreamRequestHandler {

    /**
     * 视频拼接, %1$s 连接内容文件 %2$s temp视频文件
     * <p>concat file内容:
     * <pre>
     * file 'https://aliyun.com/input1.mp4'
     * file 'https://aliyun.com/input2.mp4'
     * </pre>
     * <p>视频文件(由于是在aliyun函数计算环境中,目录只能为<code>/tmp</code>下面):
     * <pre>
     * /tmp/temp.mp4
     * </pre>
     */
    private static final String VIDEO_LINK = "ffmpeg -f concat -safe 0 -protocol_whitelist \"file,http,https,tcp,tls\" -i %s -filter_complex '[0:v]pad=0:0:0:0[vout]' -map [vout] -filter_complex \"[0:a]volume=volume=0[aout]\" -map [aout] %s";
    /**
     * 添加背景声, %1$s bg.MP3文件 %2$s 输出的视频文件
     * <p>tmp视频文件(由于是在aliyun函数计算环境中,目录只能为<code>/tmp</code>下面):
     * <pre>
     * /tmp/temp.mp4
     * </pre>
     * <p>MP3文件(由于是在aliyun函数计算环境中,目录只能为<code>/tmp</code>下面):
     * <pre>
     * /tmp/music.mp3
     * </pre>
     * <p>输出视频文件(由于是在aliyun函数计算环境中,目录只能为<code>/tmp</code>下面):
     * <pre>
     * /tmp/out.mp4
     * </pre>
     */
    private static final String ADD_BG_MUSIC_TO_VIDEO = "ffmpeg -i %s -f lavfi -i amovie='%s':loop=55 -filter_complex amix=inputs=2:duration=first %s";

    private static final String OUT_VIDEO_FMT = "https://iccfgtest0001.oss-cn-shanghai.aliyuncs.com/%s.mp4";

    private static final String FUN_NAME = "service-video-process";

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
            throws IOException {
        final JSONObject outBody = new JSONObject();
        outBody.put("success", true);
        boolean detailErr = false;
        try {
            // 获取参数,并转为json格式
            JSONObject paramBody = FCUtils.getInBodyParam(inputStream);
            detailErr = paramBody.getBooleanValue("detailErr");

            // 参数验证
            int processType = paramBody.getIntValue("processType");
            if (processType == 0) throw new Exception("参数 'processType' 是必须的!");
            String music = paramBody.getString("mp3");
            if (isBlank(music)) throw new Exception("参数 'mp3' 是必须的!");
            JSONArray videos = paramBody.getJSONArray("mp4");
            if (videos.size() < 1) throw new Exception("至少需要一个视频地址!");

            // JSONObject paramBody = FCUtils.getTestParam();
            final String fileName = MD5.md5(paramBody.toJSONString());

            // 输出视频文件名称
            final String outfileName = fileName + "_out";
            outBody.put("outUrl", String.format(OUT_VIDEO_FMT, outfileName));

            paramBody.put("outfileName", outfileName);
            String invkType = Const.INVOCATION_TYPE_ASYNC; // 异步调用
            // String invkType = ""; // 同步调用
            InvokeFunctionResponse response = FCUtils.funCall(FUN_NAME, paramBody.toJSONString(), invkType);

            // outBody.put("funResult", response.getHeader());
        } catch (Exception e) {
            outBody.put("success", false);
            outBody.put("err_message", e.getMessage());
            if (detailErr) outBody.put("err_detail", FCUtils.getStackTrace(e));
        }

        outputStream.write(FCUtils.fmtOutput(outBody.toJSONString()).getBytes());
    }
}
