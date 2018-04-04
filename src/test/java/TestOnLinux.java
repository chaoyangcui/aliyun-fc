import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Eric
 * @date 2018/1/25 10:22
 * Description
 */
public class TestOnLinux {

    public static void main(String[] args) throws Exception {

        ProcessBuilder processBuilder = new ProcessBuilder();
        List<String> list = new ArrayList<>();
        list.add("/bin/sh");
        list.add("-c");
        list.add("echo 111222");
        // list.add("ffmpeg -f concat -safe 0 -protocol_whitelist \"file,http,https,tcp,tls\" -i list.txt -filter_complex \"[0:v]pad=0:0:0:0[vout]\" -map [vout] -filter_complex \"[0:a]volume=volume=0[aout]\" -map [aout] temp.mp4");
        Process process = processBuilder.command(list).start();

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

        /*StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            reader.lines().forEach(l -> builder.append(l).append("\n"));
        } catch (Exception ignored) {
        }
        System.out.println(builder.toString());*/
    }

}
