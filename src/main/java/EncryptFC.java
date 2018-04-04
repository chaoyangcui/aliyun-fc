import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import com.aliyun.fc.runtime.Context;
import com.aliyun.fc.runtime.StreamRequestHandler;
import utils.BaseRSAUtils;
import utils.FCUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Eric
 * @date 2018/1/24 11:57
 * Description
 */
public class EncryptFC implements StreamRequestHandler {

    private static final String PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDYGSDkvE00yWEgoknBIe1zOGomiFi3aabUUdn+fLCH4hNcM+Z8hjgB9R2nusGq+WkWupXnt3BO4QItmyWj+iJ7sbjcPl2UzbGL6B43I58TuPomaxq8G4FpknzCciO1ErI+ttgcW8lQWaSp6rLBgzSe28gR7cf9lWlaYBI6z9pM3wIDAQAB";
    // 测试用的PublicKey
    // private static final String PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCzJszjvGQlbTmBd6OYC2k-CorZMwf-BU39pWjK4gGtxisIsZfqyFfJgBT3p0KNTuYsFFfLAGpjZjrColkDfcQe-26x6lNYUcOoGgiZXmTxUxHYpUAe40-i_U1qiQf-ZKU2CYiqbrEaszufGMEbphM7zipNMtI4tvYtcvgCUZ0TFQIDAQAB";
    private static final String Path_PARAM_ID = "yuanshi";
    private static final String Query_PARAM_ENCTYPE = "encType";

    private static final String KEY_ID = "id";
    private static final String KEY_ENCRYPTTYPE = "encType";

    @Override
    public void handleRequest(
            InputStream inputStream, OutputStream outputStream, Context context) throws IOException {

        Map<String, String> paramMap = getParamMap(inputStream);
        String id = paramMap.get(KEY_ID);
        String encryptType = paramMap.get(KEY_ENCRYPTTYPE);

        String output;
        if (EncryptType.Base64.name().toLowerCase().equals(encryptType)) {
            output = getOutput(BaseRSAUtils.base64Decode(id));
        } else {
            output = getOutput(id);
        }

        outputStream.write(output.getBytes());
    }

    /**
     * 封装输出参数
     * @param param 输入参数
     * @return 结构化output
     */
    private String getOutput(String param) {
        return getOutput(param.getBytes());
    }
    private String getOutput(byte[] bytes) {
        String output;
        try {
            output = encrypt(bytes);
        } catch (Exception e) {
            output = "Encrypt Error: " + e.toString();
        }
        return FCUtils.fmtOutput(output);
    }


    public static String encrypt(byte[] bytes) throws Exception{
        return BaseRSAUtils.encryptByPublicKey(bytes, PUBLIC_KEY);
    }



    private static Map<String, String> getParamMap(InputStream inputStream) {
        String inputStr = FCUtils.inputStream2Str(inputStream);
        JSONObject inJsonObject = JSON.parseObject(inputStr);
        return new HashMap<String, String>() {{
            put(KEY_ID, getPathParameter(inJsonObject, Path_PARAM_ID));
            put(KEY_ENCRYPTTYPE, getQueryParameter(inJsonObject, Query_PARAM_ENCTYPE));
        }};
    }

    private static String getPathParameter(JSONObject jsonObject, String key) {
        String pathParameters = jsonObject.getString("pathParameters");
        JSONObject pathParametersJson = JSON.parseObject(pathParameters);
        return pathParametersJson.getString(key); // 此key与api网管配置相关
    }

    private static String getQueryParameter(JSONObject jsonObject, String key) {
        String pathParameters = jsonObject.getString("queryParameters");
        JSONObject pathParametersJson = JSON.parseObject(pathParameters);
        return pathParametersJson.getString(key); // 此key与api网管配置相关
    }

    enum  EncryptType {
        Base64
    }
}