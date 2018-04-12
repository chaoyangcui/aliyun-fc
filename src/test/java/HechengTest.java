import com.alibaba.fastjson.JSONObject;

import utils.FfmpegUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author Eric
 * @since 2018/4/8 11:24
 *     <p>Created by IntelliJ IDEA.
 */
public class HechengTest {
    public static void main(String[] args) {
        // String command1 =
        //         "ffmpeg -f concat -safe 0 -protocol_whitelist \"file,http,https,tcp,tls\" -i
        // list.txt -filter_complex \"[0:v]pad=0:0:0:0[vout]\" -map [vout] -filter_complex
        // \"[0:a]volume=volume=0[aout]\" -map [aout] temp.mp4";
        // System.out.println(shellWithOutput(command1));
        // String command = generateHechengCommand(args);
        // String output = shellWithOutput(command);
        // System.out.println(output);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("xGap", 20);
        jsonObject.put("yGap", 20);
        String hecheng =
                FfmpegUtil.getHechengCommand(
                        jsonObject,
                        "https://iccfgtest0001.oss-cn-shanghai.aliyuncs.com/111.png",
                        "sssssss.mp4",
                        "https://iccfgtest0001.oss-cn-shanghai.aliyuncs.com/mygirl2.mp4",
                        "https://iccfgtest0001.oss-cn-shanghai.aliyuncs.com/mygirl2.mp4",
                        "https://iccfgtest0001.oss-cn-shanghai.aliyuncs.com/mygirl2.mp4",
                        "https://iccfgtest0001.oss-cn-shanghai.aliyuncs.com/mygirl2.mp4");
        System.out.println(hecheng);
    }

    static class Position {
        public static String W = "750";
        public static String H = "1448";
        // public static String width = "320";
        public static String width = "370";
        public static String xGap = "10";
        public static String yGap = "10";
        public static String x1 = "0", x3 = "0", y1 = "0", y2 = "0";
        public static String x2 = width + "+" + xGap;
        // public static String y3 = "H-overlay_h-" + yGap;
        // public static String x4 = "W-overlay_w-" + xGap;
        // public static String y4 = "H-overlay_h-" + yGap;
        public static String y3 = "overlay_h+" + yGap;
        public static String x4 = "overlay_w+" + xGap;
        public static String y4 = "overlay_h+" + yGap;
    }

    public static String generateHechengCommand(String... videos) {
        if (videos == null) {
            return "";
        }
        int len = videos.length;
        StringBuilder commandBuilder = new StringBuilder();
        commandBuilder.append("ffmpeg -i \"111.png\" ");
        /*commandBuilder.append("-i mygirl.mp4 ");
        commandBuilder.append("-i mygirl.mp4 ");
        commandBuilder.append("-i mygirl.mp4 ");
        commandBuilder.append("-i mygirl.mp4 ");*/
        for (String video : videos) {
            commandBuilder.append(String.format("-i %s ", video));
        }
        commandBuilder.append("-filter_complex \" ");
        // commandBuilder.append("[0:v]scale=w=640:h=-2[joinpoint0]; ");
        commandBuilder.append(
                String.format("[0:v]scale=w=%s:h=%s[joinpoint0]; ", Position.W, Position.H));

        /*commandBuilder.append("[1:v]scale=w=320:h=-2,pad=0:0:0:0[l1one];[joinpoint0][l1one]overlay=x=0:y=0[joinpoint1]; ");
        commandBuilder.append("[2:v]scale=w=320:h=-2,pad=0:0:0:0[l1two];[joinpoint1][l1two]overlay=x=320:y=0[joinpoint2]; ");
        commandBuilder.append("[3:v]scale=w=320:h=-2,pad=0:0:0:0[l1three];[joinpoint2][l1three]overlay=x=0:y=H-overlay_h[joinpoint3]; ");
        commandBuilder.append("[4:v]scale=w=320:h=-2,pad=0:0:0:0[l1four];[joinpoint3][l1four]overlay=x=W-overlay_w:y=H-overlay_h ");*/
        if (len == 1) {
            commandBuilder.append(
                    "[1:v]scale=w=320:h=-2,pad=0:0:0:0[l1one];[joinpoint0][l1one]overlay=x=0:y=0 ");
        } else {
            for (int i = 1; i <= len; i++) {
                String x = "0", y = "0";
                switch (i) {
                    case 1:
                        x = Position.x1;
                        y = Position.y1;
                        break;
                    case 2:
                        x = Position.x2;
                        y = Position.y2;
                        break;
                    case 3:
                        x = Position.x3;
                        y = Position.y3;
                        break;
                    case 4:
                        x = Position.x4;
                        y = Position.y4;
                        break;
                    default:
                        break;
                }
                if (i == len) { // 最后一个filter
                    commandBuilder.append(
                            String.format(
                                    "[%1$d:v]scale=w=%5$s:h=-2,pad=0:0:0:0[l1%1$d];[joinpoint%2$d][l1%1$d]overlay=x=%3$s:y=%4$s ",
                                    i, i - 1, x, y, Position.width));
                } else {
                    commandBuilder.append(
                            String.format(
                                    "[%1$d:v]scale=w=%5$s:h=-2,pad=0:0:0:0[l1%1$d];[joinpoint%2$d][l1%1$d]overlay=x=%3$s:y=%4$s[joinpoint%1$d]; ",
                                    i, i - 1, x, y, Position.width));
                }
            }
        }

        commandBuilder.append("[out]\" -map [out] ");
        commandBuilder.append("-filter_complex \" ");
        /*commandBuilder.append("[1:a]volume=volume=1[audio0];");
        commandBuilder.append("[2:a]volume=volume=1[audio1];");
        commandBuilder.append("[3:a]volume=volume=1[audio2];");
        commandBuilder.append("[4:a]volume=volume=1[audio3];");*/
        for (int i = 0; i < len; i++) {
            commandBuilder.append(String.format("[%d:a]volume=volume=1[audio%d];", i + 1, i));
        }
        // commandBuilder.append("[audio0][audio1][audio2][audio3]");
        for (int i = 0; i < len; i++) {
            commandBuilder.append(String.format("[audio%d]", i));
        }
        commandBuilder.append(
                String.format("amix=inputs=%d:duration=first:dropout_transition=2 ", len));
        commandBuilder.append("[aout]\" -map [aout] ");
        commandBuilder.append(
                "-force_key_frames \"expr:gte(t,n_forced*5)\" -c:v h264 -c:a aac -bufsize 2560k -qscale 0.01 synthesis.mp4");

        String command = commandBuilder.toString();
        System.out.println(command);
        return command;
    }

    public static String shellWithOutput(String command) {
        String[] commands = {"/bin/bash", "-c", command};

        StringBuilder builder = new StringBuilder();
        Process process;
        try {
            process = Runtime.getRuntime().exec(commands);
            InputStream is1 = process.getInputStream();
            BufferedReader br1 = new BufferedReader(new InputStreamReader(is1));
            StringBuilder buf1 = new StringBuilder(); // 保存输出结果流
            String line1;
            while ((line1 = br1.readLine()) != null) buf1.append(line1).append("\n");
            builder.append(buf1);

            InputStream is2 = process.getErrorStream();
            BufferedReader br2 = new BufferedReader(new InputStreamReader(is2));
            StringBuilder buf2 = new StringBuilder(); // 保存输出结果流
            String line2;
            while ((line2 = br2.readLine()) != null) buf2.append(line2).append("\n");
            builder.append(buf2);
        } catch (IOException e) {
            return "";
        }

        return builder.toString();
    }
}
