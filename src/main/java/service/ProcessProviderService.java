package service;

import com.alibaba.fastjson.JSONObject;

import com.aliyun.fc.runtime.Context;
import com.aliyun.fc.runtime.FunctionComputeLogger;
import com.aliyun.fc.runtime.StreamRequestHandler;
import fmpeg.FfmpegService;
import fmpeg.FfmpegService.ProcessType;
import utils.FCUtils;
import utils.FfmpegUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 视频拼接函数
 *
 * <pre>
 *     mkf service-video-process -h service.ProcessProviderService::handleRequest --runtime java8 -d ./classes/
 *     upf service-video-process -h service.ProcessProviderService::handleRequest --runtime java8 -d ./classes/
 *     invk service-video-process -s {\"mp4\":[\"https://iccfgtest0001.oss-cn-shanghai.aliyuncs.com/input2.mp4\",\"https://iccfgtest0001.oss-cn-shanghai.aliyuncs.com/input2.mp4\"],\"mp3\":\"https://iccfgtest0001.oss-cn-shanghai.aliyuncs.com/bg_music.mp3\",\"outfileName\":\"22f2bbcade060569621b25339d57f2b5_out\"}
 *     invk service-video-process -s {\"mp4\":[\"https://iccfgtest0001.oss-cn-shanghai.aliyuncs.com/input.mp4\",\"https://iccfgtest0001.oss-cn-shanghai.aliyuncs.com/input.mp4\"],\"mp3\":\"https://iccfgtest0001.oss-cn-shanghai.aliyuncs.com/bg_music.mp3\",\"outfileName\":\"22f2bbcade060569621b25339d57f2b5_out\",\"clean\":true,\"processType\":2}
 * </pre>
 *
 * @author Eric
 * @since 2018/4/4 9:59
 *     <p>Created by IntelliJ IDEA.
 */
public class ProcessProviderService implements StreamRequestHandler {

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
            throws IOException {
        final JSONObject outBody = new JSONObject();
        FunctionComputeLogger logger = context.getLogger();
        outBody.put("success", true);

        try {
            // 获取参数,并转为json格式
            JSONObject paramBody = FCUtils.inputStream2JsonObject(inputStream);
            logger.info("Service Param:" + paramBody.toJSONString());
            outBody.put("ServiceParam", paramBody.toJSONString());
            boolean clean = paramBody.getBooleanValue("clean");
            if (clean) {
                FfmpegUtil.shellWithOutput("rm -rf /tmp/*.mp3 /tmp/*.mp4");
            }
            int processType = paramBody.getIntValue("processType");
            JSONObject processOut;
            FfmpegService ffmpegService;
            // 拼接
            if (ProcessType.Pinjie == processType) {
                ffmpegService = new fmpeg.PinjieService();
                processOut = ffmpegService.process(paramBody, logger);
                outBody.putAll(processOut);
            }
            // 合成
            if (ProcessType.Hecheng == processType) {
                ffmpegService = new fmpeg.HechengService();
                processOut = ffmpegService.process(paramBody, logger);
                outBody.putAll(processOut);
            }
        } catch (Exception e){
            outBody.put("success", false);
            String errMsg;
            outBody.put("err_message", errMsg = FCUtils.getStackTrace(e));
            logger.error(errMsg);
        }

        outputStream.write(outBody.toJSONString().getBytes());
    }


}
