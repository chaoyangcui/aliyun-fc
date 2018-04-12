package utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import com.aliyuncs.fc.client.FunctionComputeClient;
import com.aliyuncs.fc.constants.Const;
import com.aliyuncs.fc.request.InvokeFunctionRequest;
import com.aliyuncs.fc.response.InvokeFunctionResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.util.Base64;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Eric
 * @since 2018/3/22 15:29
 */
public class FCUtils {

    /**
     * 封装函数接口输出数据格式
     *
     * @param output 需要包装格式的输出数据
     * @return 固定格式的数据
     */
    public static String fmtOutput(String output) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("isBase64Encoded", false);
        jsonObject.put("statusCode", 200);
        jsonObject.put(
                "headers",
                new HashMap<String, String>() {
                    {
                        put("x-custom-header", "no");
                    }
                });
        jsonObject.put("body", output);
        return jsonObject.toJSONString();
    }

    /**
     * 获取输入中的参数数据
     *
     * @param inputStream 输入流
     * @return 输入参数数据
     */
    public static String inputStream2Str(InputStream inputStream) {
        StringBuilder inBuilder = new StringBuilder();
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            bufferedReader.lines().forEach(inBuilder::append);
        } catch (Exception e) {
            inBuilder.append("");
        }
        return inBuilder.toString();
    }

    public static JSONObject getInBodyParam(InputStream inputStream) {
        JSONObject jsonObject = FCUtils.inputStream2JsonObject(inputStream);
        String base64Body = jsonObject.getString("body");

        String bodyStr = new String(Base64.getDecoder().decode(base64Body));
        // test
        /*String bodyStr = "{\"mp4\":[\"https://iccfgtest0001.oss-cn-shanghai.aliyuncs.com/input.mp4\",\"https://iccfgtest0001.oss-cn-shanghai.aliyuncs.com/input.mp4\"],\"mp3\":\"https://iccfgtest0001.oss-cn-shanghai.aliyuncs.com/music.mp3\"}";
        String fileName = MD5.md5(bodyStr);*/

        return FCUtils.toJsonObject(bodyStr);
    }

    public static JSONObject toJsonObject(String jsonStr) {
        return JSON.parseObject(jsonStr);
    }

    public static JSONObject inputStream2JsonObject(InputStream inputStream) {
        return JSON.parseObject(inputStream2Str(inputStream));
    }

    public static String getStackTrace(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }

    /**
     * 根据OSS地址获取文件的object key
     *
     * @param ossUrl 文件的oss地址
     * @param fileSuffix 文件的后缀
     * @return 文件对应OSS Object的key
     */
    public static String getOssObjectKey(final String ossUrl, final String fileSuffix) {
        String objectKey = "";

        String regex = "/\\w+";
        if (null != fileSuffix && !"".equals(fileSuffix)) {
            regex = regex + "\\." + fileSuffix;
        }

        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(ossUrl);
        while (matcher.find()) {
            objectKey = matcher.group().replace("/", "");
        }

        return objectKey;
    }

    public static void out(OutputStream output, String out) throws IOException {
        String fmtOut = fmtOutput(out);
        output.write(fmtOut.getBytes());
    }

    public static JSONObject getTestParam() {
        String bodyStr =
                "{\"mp4\":[\"https://iccfgtest0001.oss-cn-shanghai.aliyuncs.com/input.mp4\",\"https://iccfgtest0001.oss-cn-shanghai.aliyuncs.com/input.mp4\"],\"mp3\":\"https://iccfgtest0001.oss-cn-shanghai.aliyuncs.com/bg_music.mp3\"}";
        return JSON.parseObject(bodyStr);
    }

    /**
     * 通过代码调用函数计算
     * @param funName 函数名称
     * @param payload 参数
     * @param invokeType 调用模式: use async mode if <code>Const.INVOCATION_TYPE_ASYNC</code>, otherwise use sync mode
     * @return InvokeFunctionResponse
     */
    public static InvokeFunctionResponse funCall(
            final String funName, final String payload, final String invokeType) {
        // Invoke the function with a string as function event parameter, Sync mode
        InvokeFunctionRequest invkReq = new InvokeFunctionRequest(SERVICE_NAME, funName);
        invkReq.setPayload(payload.getBytes());

        if (Const.INVOCATION_TYPE_ASYNC.equals(invokeType)) {
            // Invoke the function, Async mode
            invkReq.setInvocationType(Const.INVOCATION_TYPE_ASYNC);
            InvokeFunctionResponse invkResp = fcClient.invokeFunction(invkReq);
            if (HttpURLConnection.HTTP_ACCEPTED == invkResp.getStatus()) {
                System.out.println(
                        "Async invocation has been queued for execution, request ID: "
                                + invkResp.getRequestId());
            } else {
                System.out.println("Async invocation was not accepted");
            }
            return invkResp;
        } else {
            // Invoke the function, Sync mode
            InvokeFunctionResponse invkResp = fcClient.invokeFunction(invkReq);
            String logResult = invkResp.getLogResult();
            System.out.println(logResult);
            System.out.println(new String(invkResp.getContent()));
            return invkResp;
        }
    }

    public static boolean isBlank(CharSequence cs) {
        int strLen;
        if (cs != null && (strLen = cs.length()) != 0) {
            for(int i = 0; i < strLen; ++i) {
                if (!Character.isWhitespace(cs.charAt(i))) {
                    return false;
                }
            }

            return true;
        } else {
            return true;
        }
    }

    // service name
    private static final String SERVICE_NAME = "ffmpeg-demo";

    private static FunctionComputeClient fcClient;

    static {
        String REGION = "cn-hangzhou";
        String accessKey = "accessKey";
        String accessSecretKey = "accessSecretKey";
        String accountId = "accountId";

        // Initialize FC client
        fcClient = new FunctionComputeClient(REGION, accountId, accessKey, accessSecretKey);
    }
}
