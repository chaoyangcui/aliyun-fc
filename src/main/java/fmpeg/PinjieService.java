package fmpeg;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.aliyun.fc.runtime.FunctionComputeLogger;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.PutObjectResult;
import utils.FCUtils;
import utils.FfmpegUtil;
import utils.MD5;
import utils.OSSUtil;

import java.io.File;

/**
 * 视频拼接
 * @author Eric
 * @since 2018/4/8 10:01
 *     <p>Created by IntelliJ IDEA.
 */
public class PinjieService implements FfmpegService {

    /**
     * 视频拼接, %1$s 连接内容文件 %2$s temp视频文件
     *
     * <p>concat file内容:
     *
     * <pre>
     * file 'https://aliyun.com/input1.mp4'
     * file 'https://aliyun.com/input2.mp4'
     * </pre>
     *
     * <p>视频文件(由于是在aliyun函数计算环境中,目录只能为<code>/tmp</code>下面):
     *
     * <pre>
     * /tmp/temp.mp4
     * </pre>
     */
    private static final String VIDEO_LINK =
            "ffmpeg -f concat -safe 0 -protocol_whitelist \"file,http,https,tcp,tls\" -i %s -filter_complex '[0:v]pad=0:0:0:0[vout]' -map [vout] -filter_complex \"[0:a]volume=volume=0[aout]\" -map [aout] %s";

    @Override
    public JSONObject process(JSONObject paramBody, FunctionComputeLogger logger) {
        final JSONObject outBody = new JSONObject();
        outBody.put("success", true);

        OSSClient ossClient = null;
        String fileName = "";
        String outfileName = "";
        try {
            // 获取参数,并转为json格式
            paramBody.put("note", "msg from service-pinjie");
            // JSONObject paramBody = getTestParam();
            fileName = MD5.md5(paramBody.toJSONString());

            // ffmpeg concat 文件
            final String listfileName = "/tmp/" + fileName + ".txt";
            // 视频连接tmp文件
            final String tempfileName = "/tmp/" + fileName + ".mp4";

            // 输出视频文件名称
            // outfileName = fileName + "_out";
            outfileName = paramBody.getString("outfileName");
            // outBody.put("outUrl", String.format(OUT_VIDEO_FMT, outfileName));

            // 输出视频文件Path
            final String outVideoPath = "/tmp/" + outfileName + ".mp4";
            // 视频背景音乐
            final String bgMusicFilePath = "/tmp/" + fileName + ".mp3";

            // 视频,背景音乐参数
            final String music = paramBody.getString("mp3");
            final JSONArray videos = paramBody.getJSONArray("mp4");

            ossClient = OSSUtil.getOSSClient();
            // 第一步,连接视频
            // 生成ffmpet concat视频连接文件
            FfmpegUtil.createConnectFile(listfileName, videos);
            outBody.put("step1", "第一步,连接视频");

            // 生成连接视频
            String videoLinkCmd = String.format(VIDEO_LINK, listfileName, tempfileName);
            outBody.put("videoLinkCmd", videoLinkCmd);
            String step2 = FfmpegUtil.shellWithOutput(videoLinkCmd);
            outBody.put("step2", step2);
            logger.info("Pinjie step2: " + step2);

            // 第二步,为视频加背景音乐
            String musicObjKey = FCUtils.getOssObjectKey(music, "mp3");
            // 进行加背景音乐的命令时音乐文件需要在本地,所以先下载音乐文件到本地
            OSSUtil.getAndWrite2File(ossClient, musicObjKey, new File(bgMusicFilePath));
            // 添加背景音乐
            String step3 =
                    FfmpegUtil.shellWithOutput(
                            String.format(
                                    ADD_BG_MUSIC_TO_VIDEO,
                                    tempfileName,
                                    bgMusicFilePath,
                                    outVideoPath));
            outBody.put("step3", step3);
            logger.info("Pinjie step3: " + step3);

            String lstmp = FfmpegUtil.shellWithOutput("ls /tmp/");
            outBody.put("lstmp", lstmp);
            logger.info("Pinjie ls /tmp: " + lstmp);
            // 第三步, 将生成的视频上传至OSS并获取对应url地址
            File file = new File(outVideoPath);
            // outBody = tempfileName + ": " + file.exists();
            String objKey;
            PutObjectResult objectResult =
                    ossClient.putObject(OSSUtil.BUCKET_NAME, objKey = (outfileName + ".mp4"), file);
            outBody.put("step4", objectResult.getRequestId());
            logger.info("Pinjie step4: " + objectResult.getRequestId());
        } catch (Exception e) {
            outBody.put("success", false);
            outBody.put("srv_err_message", FCUtils.getStackTrace(e));
        } finally {
            try {
                if (null != ossClient) ossClient.shutdown();
                // FfmpegUtil.shellWithOutput("ls /tmp/" + fileName + "*");
                // 删除错过过程中生成的文件
                if (!"".equals(fileName)) {
                    FfmpegUtil.shellWithOutput("rm -rf /tmp/" + fileName + "*");
                }
                // 最终输出的文件要单独删除
                if (!"".equals(outfileName)) {
                    FfmpegUtil.shellWithOutput("rm -rf /tmp/" + outfileName + "*");
                }
            } catch (Exception e) {
                outBody.put("success", false);
                outBody.put("srv_err_message_final", FCUtils.getStackTrace(e));
            }
        }

        return outBody;
    }
}
