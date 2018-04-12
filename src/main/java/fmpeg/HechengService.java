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

import static utils.FCUtils.isBlank;

/**
 * 视频合成
 * @author Eric
 * @since 2018/4/8 10:32
 *     <p>Created by IntelliJ IDEA.
 */
public class HechengService implements FfmpegService {

    private static final String bgPicUrl =
            "https://iccfgtest0001.oss-cn-shanghai.aliyuncs.com/111.png";

    @Override
    public JSONObject process(JSONObject paramBody, FunctionComputeLogger logger) {
        final JSONObject outBody = new JSONObject();
        outBody.put("success", true);

        OSSClient ossClient = null;
        String fileName = "";
        String outfileName = "";
        try {
            // 获取参数,并转为json格式
            paramBody.put("note", "msg from service-hecheng");
            // JSONObject paramBody = getTestParam();
            fileName = MD5.md5(paramBody.toJSONString());

            // 视频组合tmp文件
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
            Object positionObj = paramBody.get("position");
            JSONObject position = new JSONObject();
            if (positionObj != null) {
                position = paramBody.getJSONObject("position");
            }
            String bgUrl = paramBody.getString("bgUrl");
            bgUrl = isBlank(bgUrl) ? bgPicUrl : bgUrl;

            ossClient = OSSUtil.getOSSClient();
            // 第一步,合成视频
            // 生成合成视频命令
            String command =
                    FfmpegUtil.getHechengCommand(
                            position, bgUrl, tempfileName, videos.toArray(new String[0]));
            outBody.put("step1", "第一步,合成命令");
            outBody.put("command", command);
            logger.info("Hecheng Command: " + command);

            // 生成合成视频
            // String videoLinkCmd = String.format(VIDEO_HECHENG, listfileName, tempfileName);
            String step2 = FfmpegUtil.shellWithOutput(command);
            outBody.put("step2", step2);
            logger.info("Hecheng step2: " + step2);

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
            logger.info("Hecheng step3: " + step3);

            String lstmp = FfmpegUtil.shellWithOutput("ls /tmp/");
            outBody.put("lstmp", lstmp);
            logger.info("Hecheng ls /tmp: " + lstmp);
            // 第三步, 将生成的视频上传至OSS并获取对应url地址
            File file = new File(outVideoPath);
            // outBody = tempfileName + ": " + file.exists();
            String objKey;
            PutObjectResult objectResult =
                    ossClient.putObject(OSSUtil.BUCKET_NAME, objKey = (outfileName + ".mp4"), file);
            outBody.put("step4", objectResult.getRequestId());
            logger.info("Hecheng step4: " + objectResult.getRequestId());
        } catch (Exception e) {
            outBody.put("success", false);
            String errMsg = FCUtils.getStackTrace(e);
            outBody.put("srv_err_message", errMsg);
            logger.error("srv_err_message" + errMsg);
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
                String errMsg = FCUtils.getStackTrace(e);
                outBody.put("srv_err_message_final", errMsg);
                logger.error("srv_err_message_final" + errMsg);
            }
        }

        return outBody;
    }

    public static class Position {
        public static String W = "750";
        public static String H = "1448";
        public String width = "370";
        public String height = "510";
        String top = "150";
        String xGap = "5";
        String yGap = "5";
        public String x1 = "0", x3 = "0", y1 = top, y2 = top;
        public String x2 = width + "+" + xGap;
        public String y3 = "overlay_h+" + yGap + "+" + top;
        public String x4 = "overlay_w+" + xGap;
        public String y4 = "overlay_h+" + yGap + "+" + top;

        public static Position buildPosition(JSONObject jsonObject) {
            Position position = new Position();
            position.width =
                    isBlank(jsonObject.getString("width"))
                            ? position.width
                            : jsonObject.getString("width");
            position.height =
                    isBlank(jsonObject.getString("height"))
                            ? position.height
                            : jsonObject.getString("height");
            position.top =
                    isBlank(jsonObject.getString("top"))
                            ? position.top
                            : jsonObject.getString("top");
            position.xGap =
                    isBlank(jsonObject.getString("xGap"))
                            ? position.xGap
                            : jsonObject.getString("xGap");
            position.yGap =
                    isBlank(jsonObject.getString("yGap"))
                            ? position.yGap
                            : jsonObject.getString("yGap");

            position.x1 = "0";
            position.x3 = "0";
            position.y1 = position.top;
            position.y2 = position.top;
            position.x2 = position.width + "+" + position.xGap;
            position.y3 = "overlay_h+" + position.yGap + "+" + position.top;
            position.x4 = "overlay_w+" + position.xGap;
            position.y4 = "overlay_h+" + position.yGap + "+" + position.top;

            return position;
        }

    }
}
