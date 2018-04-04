import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import utils.FCUtils;

import java.util.Base64;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Eric
 * @since 2018/3/24 21:23
 */
public class JSONTest {

    public static void main(String[] args) {
        String jsonstr = "{\"body\":\"ew0KICAibXA0IjogWw0KICAgICJodHRwczovL2FsaXl1bi5jb20vaW5wdXQxLm1wNCIsDQogICAgImh0dHBzOi8vYWxpeXVuLmNvbS9pbnB1dDIubXA0Ig0KICBdLA0KICAibXAzIjogImh0dHBzOi8vYWxpeXVuLmNvbS9pbnB1dC5tcDMiDQp9\",\"headers\":{\"X-Ca-Api-Gateway\":\"A706A6CC-3E09-4C69-BAA5-AD68B81D1F79\",\"X-Forwarded-For\":\"116.247.74.26\",\"Content-Type\":\"application/json\"},\"httpMethod\":\"POST\",\"isBase64Encoded\":true,\"path\":\"/ffmpeg/vlink\",\"pathParameters\":{},\"queryParameters\":{}}";
        JSONObject jsonObject = JSON.parseObject(jsonstr);
        String base64Body = jsonObject.getString("body");

        String bodyStr = new String(Base64.getDecoder().decode(base64Body));
        JSONObject body = FCUtils.toJsonObject(bodyStr);
        System.out.println(body.getString("mp3"));
        System.out.println(body.getJSONArray("mp4"));
        body.getJSONArray("mp4").forEach(System.out::println);
    }

}
