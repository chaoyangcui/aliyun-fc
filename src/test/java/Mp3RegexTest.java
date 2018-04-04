import utils.FCUtils;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Eric
 * @since 2018/3/26 15:07
 */
public class Mp3RegexTest {

    public static void main(String[] args) {
        String mp3url = "https://iccfgtest0001.oss-cn-shanghai.aliyuncs.com/music_test.mp3";
        System.out.println(FCUtils.getOssObjectKey(mp3url, "MP3"));

        System.out.println(String.format("%1$s %1$s", "Hello", "World"));
    }

}
