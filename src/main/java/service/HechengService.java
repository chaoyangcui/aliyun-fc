package service;

import com.aliyun.fc.runtime.Context;
import com.aliyun.fc.runtime.StreamRequestHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 视频合成函数
 * <pre>
 *     ffmpeg -loop 1 -i "RTMP_BG_PIC"  -re -stream_loop -1 -safe 0 -protocol_whitelist "file,http,https,tcp,tls" -v info -f concat -i <(for i in {1..120}; do printf "file '%s'\n" INPUT1; done)  -re -stream_loop -1 -safe 0 -protocol_whitelist "file,http,https,tcp,tls" -v info -f concat -i <(for i in {1..120}; do printf "file '%s'\n" INPUT2; done) -filter_complex "[0:v]scale=w=640:h=-2[baseimg];[1:v]scale=w=640:h=-2,pad=0:0:0:0[l1one];[baseimg][l1one]overlay=x=0:y=0[joinpoint1];[2:v]scale=w=640:h=-2,pad=0:0:0:0[l1two];[joinpoint1][l1two]overlay=x=0:y=-overlay_h[layer1];[0:v]scale=320:-2[baseimg];[layer1][baseimg]overlay=0:-overlay_h[joinpoint4];[1:v]scale=w=320:h=-2[l2one];[joinpoint4][l2one]overlay=x=0:y=-overlay_h[joinpoint5];[2:v]scale=w=320:h=-2[l2two];[joinpoint5][l2two]overlay=x=0:y=-overlay_h,zmq=bind_address=\'tcp://*:1\'[out]" -map [out] -filter_complex "[1:a]volume=volume=0.10[audio0];[2:a]volume=volume=0[audio1];[audio0][audio1]amix=inputs=2:duration=first:dropout_transition=2,azmq=bind_address=\'tcp://*:2\'[aout]" -map [aout] -force_key_frames "expr:gte(t,n_forced*5)" -c:v h264 -c:a aac -bufsize 2560k -qscale 0.01 -f flv "LIVE_URL" &> "/usr/local/tomcat/logs/live_idLOG_ID.log"
 * </pre>
 *
 * @author Eric
 * @since 2018/4/4 9:58
 *     <p>Created by IntelliJ IDEA.
 */
public class HechengService implements StreamRequestHandler {
    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
            throws IOException {}
}
