package vlink;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.aliyun.fc.runtime.Context;
import com.aliyun.fc.runtime.StreamRequestHandler;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.PutObjectResult;
import utils.FCUtils;
import utils.FfmpegUtil;
import utils.MD5;
import utils.OSSUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Eric
 * @since 2018/3/21 17:04
 */
public class VideoLink implements StreamRequestHandler {

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

    private static ExecutorService executorService = Executors.newFixedThreadPool(5);

    public static JSONObject getInParam(InputStream inputStream) {
        JSONObject jsonObject = FCUtils.inputStream2JsonObject(inputStream);
        String base64Body = jsonObject.getString("body");

        String bodyStr = new String(Base64.getDecoder().decode(base64Body));
        // test
        /*String bodyStr = "{\"mp4\":[\"https://iccfgtest0001.oss-cn-shanghai.aliyuncs.com/input.mp4\",\"https://iccfgtest0001.oss-cn-shanghai.aliyuncs.com/input.mp4\"],\"mp3\":\"https://iccfgtest0001.oss-cn-shanghai.aliyuncs.com/music.mp3\"}";
        String fileName = MD5.md5(bodyStr);*/

        return FCUtils.toJsonObject(bodyStr);
    }

    public static JSONObject getTestParam() {
        String bodyStr = "{\"mp4\":[\"https://iccfgtest0001.oss-cn-shanghai.aliyuncs.com/input.mp4\",\"https://iccfgtest0001.oss-cn-shanghai.aliyuncs.com/input.mp4\"],\"mp3\":\"https://iccfgtest0001.oss-cn-shanghai.aliyuncs.com/music.mp3\"}";
        return JSON.parseObject(bodyStr);
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        final JSONObject outBody = new JSONObject();
        outBody.put("success", true);

        try {
            // 获取参数,并转为json格式
            JSONObject paramBody = getInParam(inputStream);
            // JSONObject paramBody = getTestParam();
            final String fileName = MD5.md5(paramBody.toJSONString());

            // ffmpeg concat 文件
            final String listfileName = "/tmp/" + fileName + ".txt";
            // 视频连接tmp文件
            final String tempfileName = "/tmp/" + fileName + ".mp4";

            // 输出视频文件名称
            final String outfileName = fileName + "_out";
            outBody.put("outUrl", String.format(OUT_VIDEO_FMT, outfileName));

            // 输出视频文件Path
            final String outVideoPath = "/tmp/" + outfileName + ".mp4";
            // 视频背景音乐
            final String bgMusicFilePath = "/tmp/" + fileName + ".mp3";

            // 视频,背景音乐参数
            final String music = paramBody.getString("mp3");
            final JSONArray videos = paramBody.getJSONArray("mp4");
            Object cleanObj = paramBody.get("clean");
            if (cleanObj != null) {
                boolean clean = Boolean.parseBoolean(cleanObj.toString());
                if (clean) {
                    FfmpegUtil.shellWithOutput("rm -rf /tmp/*.mp3 /tmp/*.mp4");
                }
            }
            Object waitObj = paramBody.get("wait");
            boolean wait = false;
            if (waitObj != null) {
                wait = Boolean.parseBoolean(waitObj.toString());
            }
            Object secsObj = paramBody.get("seconds");
            long seconds = 0L;
            if (secsObj != null) {
                seconds = Long.parseLong(secsObj.toString());
            }

            Runnable runnable = () -> {
                final OSSClient ossClient = OSSUtil.getOSSClient();
                try {
                    // 第一步,连接视频
                    // 生成ffmpet concat视频连接文件
                    FfmpegUtil.createConnectFile(listfileName, videos);
                    outBody.put("step1", "第一步,连接视频");

                    // 生成连接视频
                    String videoLinkCmd = String.format(VIDEO_LINK, listfileName, tempfileName);
                    String step2 = FfmpegUtil.shellWithOutput(videoLinkCmd);
                    outBody.put("step2", step2);

                    // 第二步,为视频加背景音乐
                    String musicObjKey = FCUtils.getOssObjectKey(music, "mp3");
                    // 进行加背景音乐的命令时音乐文件需要在本地,所以先下载音乐文件到本地
                    OSSUtil.getAndWrite2File(ossClient, musicObjKey, new File(bgMusicFilePath));
                    // 添加背景音乐
                    String step3 = FfmpegUtil.shellWithOutput(String.format(ADD_BG_MUSIC_TO_VIDEO, tempfileName, bgMusicFilePath, outVideoPath));
                    outBody.put("step3", step3);

                    // 第三步, 将生成的视频上传至OSS并获取对应url地址
                    File file = new File(outVideoPath);
                    // outBody = tempfileName + ": " + file.exists();
                    outBody.put("step4-tmp", tempfileName + ": " + file.exists());
                    String objKey;
                    PutObjectResult objectResult = ossClient.putObject(OSSUtil.BUCKET_NAME, objKey = (outfileName + ".mp4"), file);
                    outBody.put("step4", objectResult.getRequestId());

                    // 获取刚刚上传的视频的地址
                    // String objUri = ossClient.getObject(OSSUtil.BUCKET_NAME, objKey).getResponse().getUri();
                    // OSSObject ossObject = ossClient.getObject(OSSUtil.BUCKET_NAME, objKey);
                    // outBody.put("step5", String.format("RequestId:%s, Key:%s", ossObject.getRequestId(), ossObject.getKey()));
                    // String objUri = ossObject.getResponse().getUri();
                    // outBody.put("uri", objUri);
                } finally {
                    try {
                        ossClient.shutdown();
                        FfmpegUtil.shellWithOutput("ls /tmp/" + fileName + "*");
                        // 删除错过过程中生成的文件
                        FfmpegUtil.shellWithOutput("rm -rf /tmp/" + fileName + "*");
                    } catch (Exception e) {
                        outBody.put("success", false);
                        outBody.put("message_final", FCUtils.getStackTrace(e));
                    }
                }
            };
            // 另起一个线程执行任务
            // new Thread(runnable).start();
            // if (wait) Thread.sleep(1000 * 25L);

            Future<String> future = executorService.submit(runnable, "task completion.");
            if (wait) {
                String taskResult;
                if (seconds > 0) {
                    taskResult = future.get(seconds, TimeUnit.SECONDS);
                } else {
                    taskResult = future.get();
                }

                outBody.put("taskResult", taskResult);
            }
        } catch (Exception e) {
            outBody.put("success", false);
            outBody.put("message", FCUtils.getStackTrace(e));
        }

        outputStream.write(FCUtils.fmtOutput(outBody.toJSONString()).getBytes());
    }
}
