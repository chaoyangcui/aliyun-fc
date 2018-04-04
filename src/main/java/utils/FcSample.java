package utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.aliyuncs.fc.client.FunctionComputeClient;
import com.aliyuncs.fc.constants.Const;
import com.aliyuncs.fc.request.InvokeFunctionRequest;
import com.aliyuncs.fc.response.InvokeFunctionResponse;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Base64;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Eric
 * @since 2018/4/3 17:58
 */
public class FcSample {
    // private static final String CODE_DIR = "/tmp/fc_code";
    private static final String REGION = "cn-hangzhou";
    private static final String SERVICE_NAME = "ffmpeg-demo";
    private static final String FUNCTION_NAME = "ffmpeg-fc";

    public static void main(final String[] args) throws IOException {
        System.setProperty("ACCESS_KEY", "ACCESS_KEY");
        System.setProperty("SECRET_KEY", "SECRET_KEY");
        System.setProperty("ACCOUNT_ID", "ACCOUNT_ID");
        // System.setProperty("ROLE", "LTAIs5i1MH2c56fm");
        String accessKey = System.getProperty("ACCESS_KEY");
        String accessSecretKey = System.getProperty("SECRET_KEY");
        String accountId = System.getProperty("ACCOUNT_ID");
        // String role = System.getProperty("ROLE");
        // Initialize FC client
        FunctionComputeClient fcClient =
                new FunctionComputeClient(REGION, accountId, accessKey, accessSecretKey);

        // Create a service
        /*CreateServiceRequest csReq = new CreateServiceRequest();
        csReq.setServiceName(SERVICE_NAME);
        csReq.setDescription("FC test service");
        csReq.setRole(role);
        CreateServiceResponse csResp = fcClient.createService(csReq);
        System.out.println("Created service, request ID " + csResp.getRequestId());*/
        // Create a function
        /*CreateFunctionRequest cfReq = new CreateFunctionRequest(SERVICE_NAME);
        cfReq.setFunctionName(FUNCTION_NAME);
        cfReq.setDescription("Function for test");
        cfReq.setMemorySize(128);
        cfReq.setHandler("hello_world.handler");
        cfReq.setRuntime("nodejs6");
        Code code = new Code().setDir(CODE_DIR);
        cfReq.setCode(code);
        cfReq.setTimeout(10);
        CreateFunctionResponse cfResp = fcClient.createFunction(cfReq);
        System.out.println("Created function, request ID " + cfResp.getRequestId());*/

        // Invoke the function with a string as function event parameter, Sync mode
        InvokeFunctionRequest invkReq = new InvokeFunctionRequest(SERVICE_NAME, FUNCTION_NAME);

        // String payload = "Hello FunctionCompute!";
        // invkReq.setPayload(payload.getBytes());
        JSONObject payload = new JSONObject();
        /*
                {
          "mp4": [
            "https://iccfgtest0001.oss-cn-shanghai.aliyuncs.com/input.mp4",
            "https://iccfgtest0001.oss-cn-shanghai.aliyuncs.com/input.mp4"
          ],
          "mp3": "https://iccfgtest0001.oss-cn-shanghai.aliyuncs.com/bg_music.mp3",
          "wait": true, // 是否等待
          "seconds": 30, // 等待时间,单位 s
          "clean": true // 是否清除生成的MP3,MP4文件
        }
                 */
        payload.put(
                "mp4",
                new JSONArray() {
                    {
                        add("https://iccfgtest0001.oss-cn-shanghai.aliyuncs.com/input.mp4");
                        add("https://iccfgtest0001.oss-cn-shanghai.aliyuncs.com/input.mp4");
                    }
                });
        payload.put("mp3", "https://iccfgtest0001.oss-cn-shanghai.aliyuncs.com/bg_music.mp3");
        payload.put("wait", true);
        payload.put("clean", true);
        String jsonString = payload.toJSONString();
        System.out.println(jsonString);
        invkReq.setPayload(Base64.getEncoder().encode(payload.toJSONString().getBytes()));

        InvokeFunctionResponse invkResp = fcClient.invokeFunction(invkReq);
        String logResult = invkResp.getLogResult();
        System.out.println(logResult);
        System.out.println(new String(invkResp.getContent()));

        // Invoke the function, Async mode
        invkReq.setInvocationType(Const.INVOCATION_TYPE_ASYNC);
        invkResp = fcClient.invokeFunction(invkReq);
        if (HttpURLConnection.HTTP_ACCEPTED == invkResp.getStatus()) {
            System.out.println(
                    "Async invocation has been queued for execution, request ID: "
                            + invkResp.getRequestId());
        } else {
            System.out.println("Async invocation was not accepted");
        }
        // Delete the function
        /*DeleteFunctionRequest dfReq = new DeleteFunctionRequest(SERVICE_NAME, FUNCTION_NAME);
        DeleteFunctionResponse dfResp = fcClient.deleteFunction(dfReq);
        System.out.println("Deleted function, request ID " + dfResp.getRequestId());*/
        // Delete the service
        /*DeleteServiceRequest dsReq = new DeleteServiceRequest(SERVICE_NAME);
        DeleteServiceResponse dsResp = fcClient.deleteService(dsReq);
        System.out.println("Deleted service, request ID " + dsResp.getRequestId());*/
    }
}
