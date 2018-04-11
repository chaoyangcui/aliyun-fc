package fmpeg;

import com.alibaba.fastjson.JSONObject;

import com.aliyun.fc.runtime.FunctionComputeLogger;

/**
 * @author Eric
 * @since 2018/4/8 9:56
 *     <p>Created by IntelliJ IDEA.
 */
public interface FfmpegService {
    /**
     * 添加背景声, %1$s bg.MP3文件 %2$s 输出的视频文件
     *
     * <p>tmp视频文件(由于是在aliyun函数计算环境中,目录只能为<code>/tmp</code>下面):
     *
     * <pre>
     * /tmp/temp.mp4
     * </pre>
     *
     * <p>MP3文件(由于是在aliyun函数计算环境中,目录只能为<code>/tmp</code>下面):
     *
     * <pre>
     * /tmp/music.mp3
     * </pre>
     *
     * <p>输出视频文件(由于是在aliyun函数计算环境中,目录只能为<code>/tmp</code>下面):
     *
     * <pre>
     * /tmp/out.mp4
     * </pre>
     */
    String ADD_BG_MUSIC_TO_VIDEO =
            "ffmpeg -i %s -f lavfi -i amovie='%s':loop=55 -filter_complex amix=inputs=2:duration=first %s";

    /**
     * 视频处理方式
     */
    class ProcessType {
        /**
         * 视频拼接
         */
        public static int Pinjie = 1;
        /**
         * 视频合成
         */
        public static int Hecheng = 2;
    }

    JSONObject process(JSONObject paramBody, FunctionComputeLogger logger);
}
