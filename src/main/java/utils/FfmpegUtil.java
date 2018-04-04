package utils;

import com.alibaba.fastjson.JSONArray;

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

    public static boolean ffmpeg(String[] args) {
        try {
            List<String> list = new ArrayList<>();
            list.add("/bin/sh");
            list.add("-c");
            // list.add("ls");
            list.add("ffmpeg -f concat -safe 0 -protocol_whitelist \"file,http,https,tcp,tls\" -i list.txt -filter_complex \"[0:v]pad=0:0:0:0[vout]\" -map [vout] -filter_complex \"[0:a]volume=volume=0[aout]\" -map [aout] temp.mp4");
            Process process = commandWin(list);

            final InputStream is1 = process.getInputStream();
            final StringBuilder buf1 = new StringBuilder();
            new Thread(() -> {
                BufferedReader br = new BufferedReader(new InputStreamReader(is1));
                br.lines().forEach(line -> buf1.append(line).append("\n"));
            }).start(); // 启动单独的线程来清空p.getInputStream()的缓冲区
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

    public static void createConnectFile(String fileName, JSONArray videos) {
        if (videos == null || videos.size() == 0) return;
        AtomicInteger index = new AtomicInteger();
        videos.forEach(video -> {
            if (index.getAndIncrement() == 0) shellWithOutput("echo file \'" + videos.get(0).toString() + "\' > " + fileName);
            else shellWithOutput("echo file \'" + video.toString() + "\' >> " + fileName);
        });
    }

    public static Process commandWin(List<String> command) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        Process process = processBuilder.command(command).start();
        return process;
    }

    public static String shellWithOutput(String command) {
        String[] commands = {
                "/bin/bash", "-c", command
        };

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
            new Thread(() -> {
                BufferedReader br = new BufferedReader(new InputStreamReader(is1));
                br.lines().forEach(line -> buf1.append(line).append("\n"));
            }).start(); // 启动单独的线程来清空p.getInputStream()的缓冲区
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
