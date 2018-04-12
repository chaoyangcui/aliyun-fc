package utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import fmpeg.HechengService.Position;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Eric
 * @since 2018/3/23 14:59
 */
public class FfmpegUtil {

    public static boolean ffmpeg() {
        try {
            List<String> list = new ArrayList<>();
            list.add("/bin/sh");
            list.add("-c");
            // list.add("ls");
            list.add(
                    "ffmpeg -f concat -safe 0 -protocol_whitelist \"file,http,https,tcp,tls\" -i list.txt -filter_complex \"[0:v]pad=0:0:0:0[vout]\" -map [vout] -filter_complex \"[0:a]volume=volume=0[aout]\" -map [aout] temp.mp4");
            Process process = commandWin(list);

            final InputStream is1 = process.getInputStream();
            final StringBuilder buf1 = new StringBuilder();
            new Thread(
                            () -> {
                                BufferedReader br = new BufferedReader(new InputStreamReader(is1));
                                br.lines().forEach(line -> buf1.append(line).append("\n"));
                            })
                    .start(); // 启动单独的线程来清空p.getInputStream()的缓冲区
            System.out.println("Result1: " + buf1);

            InputStream is2 = process.getErrorStream();
            BufferedReader br2 = new BufferedReader(new InputStreamReader(is2));
            StringBuilder buf2 = new StringBuilder(); // 保存输出结果流
            // String line;
            // while((line = br2.readLine()) != null) buf2.append(line).append("\n");
            br2.lines().forEach(line -> buf2.append(line).append("\n"));
            System.out.println("Result2：" + buf2);

            System.out.println(process.isAlive());
        } catch (IOException ignore) {
            return false;
        }
        return true;
    }

    public static String getTestHechengCommand() {
        String command =
                "ffmpeg -i \"111.png\" \\"
                        + "-i mygirl.mp4 -i mygirl.mp4 -i mygirl.mp4 -i mygirl.mp4 \\"
                        + "-filter_complex \"\\"
                        + "[0:v]scale=w=640:h=-2[baseimg]; \\"
                        + "[1:v]scale=w=320:h=-2,pad=0:0:0:0[l1one];[baseimg][l1one]overlay=x=0:y=0[joinpoint1]; \\"
                        + "[2:v]scale=w=320:h=-2,pad=0:0:0:0[l1two];[joinpoint1][l1two]overlay=x=320:y=0[joinpoint2]; \\"
                        + "[3:v]scale=w=320:h=-2,pad=0:0:0:0[l1three];[joinpoint2][l1three]overlay=x=0:y=H-overlay_h[joinpoint3]; \\"
                        + "[4:v]scale=w=320:h=-2,pad=0:0:0:0[l1four];[joinpoint3][l1four]overlay=x=W-overlay_w:y=H-overlay_h \\"
                        + "[out]\" -map [out] \\"
                        + "-filter_complex \" \\"
                        + "[1:a]volume=volume=1[audio0];[2:a]volume=volume=1[audio1];[3:a]volume=volume=1[audio2];[4:a]volume=volume=1[audio3]; \\"
                        + "[audio0][audio1][audio2][audio3]amix=inputs=4:duration=first:dropout_transition=2 \\"
                        + "[aout]\" -map [aout] \\"
                        + "-force_key_frames \"expr:gte(t,n_forced*5)\" -c:v h264 -c:a aac -bufsize 2560k -qscale 0.01 synthesis.mp4";
        System.out.println(command);
        return command;
    }

    public static String getHechengCommand(JSONObject jsonObject, String bgPicUrl, String tmpfilePath, String... videos) {
        if (videos == null) {
            return "";
        }
        Position position = Position.buildPosition(jsonObject);
        int len = videos.length;
        StringBuilder commandBuilder = new StringBuilder();
        commandBuilder.append(String.format("ffmpeg -i \"%s\" ", bgPicUrl));
        for (String video : videos) {
            commandBuilder.append(String.format("-i %s ", video));
        }
        commandBuilder.append("-filter_complex \" ");
        commandBuilder.append(
                String.format("[0:v]scale=w=%s:h=%s[joinpoint0]; ", Position.W, Position.H));
        if (len == 1) {
            commandBuilder.append(
                    String.format(
                            "[1:v]scale=w=%s:h=%s[l1one];[joinpoint0][l1one]overlay=x=0:y=0 ",
                            position.width, position.height));
        } else {
            for (int i = 1; i <= len; i++) {
                String x = "0", y = "0";
                switch (i) {
                    case 1:x = position.x1;y = position.y1;break;
                    case 2:x = position.x2;y = position.y2;break;
                    case 3:x = position.x3;y = position.y3;break;
                    case 4:x = position.x4;y = position.y4;break;
                    default:break;
                }
                if (i == len) { // 最后一个filter
                    commandBuilder.append(
                            String.format(
                                    "[%1$d:v]scale=w=%5$s:h=%6$s[l1%1$d];[joinpoint%2$d][l1%1$d]overlay=x=%3$s:y=%4$s ",
                                    i, i - 1, x, y, position.width, position.height));
                } else {
                    commandBuilder.append(
                            String.format(
                                    "[%1$d:v]scale=w=%5$s:h=%6$s[l1%1$d];[joinpoint%2$d][l1%1$d]overlay=x=%3$s:y=%4$s[joinpoint%1$d]; ",
                                    i, i - 1, x, y, position.width, position.height));
                }
            }
        }

        commandBuilder.append("[out]\" -map [out] ");
        commandBuilder.append("-filter_complex \" ");
        for (int i = 0; i < len; i++) {
            commandBuilder.append(String.format("[%d:a]volume=volume=0[audio%d];", i + 1, i));
        }
        for (int i = 0; i < len; i++) {
            commandBuilder.append(String.format("[audio%d]", i));
        }
        commandBuilder.append(
                String.format("amix=inputs=%d:duration=first:dropout_transition=2 ", len));
        commandBuilder.append("[aout]\" -map [aout] ");
        // commandBuilder.append(String.format("-force_key_frames \"expr:gte(t,n_forced*5)\" -c:v
        // h264 -c:a aac -bufsize 2560k -qscale 0.01 %s", tmpfilePath));
        commandBuilder.append(tmpfilePath);

        String command = commandBuilder.toString();
        System.out.println(command);
        return command;
    }

    /**
     * 生成视频连接txt list.txt
     *
     * @param fileName 生成的文件名称
     * @param videos 视频
     */
    public static void createConnectFile(String fileName, JSONArray videos) {
        if (videos == null || videos.size() == 0) return;
        AtomicInteger index = new AtomicInteger();
        videos.forEach(
                video -> {
                    if (index.getAndIncrement() == 0)
                        shellWithOutput(
                                "echo file \'" + videos.get(0).toString() + "\' > " + fileName);
                    else shellWithOutput("echo file \'" + video.toString() + "\' >> " + fileName);
                });
    }

    public static Process commandWin(List<String> command) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        Process process = processBuilder.command(command).start();
        return process;
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

    public static String commandWithOutput(List<String> command) {
        StringBuilder builder = new StringBuilder();
        try {
            Process process = commandWin(command);

            final InputStream is1 = process.getInputStream();
            final StringBuilder buf1 = new StringBuilder();
            new Thread(
                            () -> {
                                BufferedReader br = new BufferedReader(new InputStreamReader(is1));
                                br.lines().forEach(line -> buf1.append(line).append("\n"));
                            })
                    .start(); // 启动单独的线程来清空p.getInputStream()的缓冲区
            builder.append(buf1);

            InputStream is2 = process.getErrorStream();
            BufferedReader br2 = new BufferedReader(new InputStreamReader(is2));
            StringBuilder buf2 = new StringBuilder(); // 保存输出结果流
            br2.lines().forEach(line -> buf2.append(line).append("\n"));
            builder.append(buf2);

            System.out.println(process.isAlive());
        } catch (IOException ignore) {
            return "";
        }
        return builder.toString();
    }
}
