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
public class Test {

    private static final String privatekey = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBALMmzOO8ZCVtOYF3o5gLaT4KitkzB_4FTf2laMriAa3GKwixl-rIV8mAFPenQo1O5iwUV8sAamNmOsKiWQN9xB77brHqU1hRw6gaCJleZPFTEdilQB7jT6L9TWqJB_5kpTYJiKpusRqzO58YwRumEzvOKk0y0ji29i1y-AJRnRMVAgMBAAECgYB0m1-nJ7tDhRl2BIpN77d7s172yrl8BLXjz-MqkvqmwiLGkNmh7u7878o1-_9cfKtiq4cNUeLted_rScKIfDbHH83SPylkppb79h5p1lwbZ8dZEtiaippn3LhtpDPxeNCAMVf6FoO-PrTWJ3dpL6YhEPy-ylfOQIgGIgythXGPLQJBAOqPwpoYqcHBC-Fh_HkZIw0S8WfLuTThzjTF6m0h127fZtt9zPXIBW865I7RKqsaiv1dqB-TaLsKG4y9br8hUosCQQDDhpCCkJYjGYawE10gaOt1bpPU_a6VqvCzbUEaWOBKHCabzN_esc5VNtmKPvab-VpAVfaY3K_HXE8yYA0wZATfAkEAoYbsYQe45zeUgdnjblVUIO25llvhp7wUL51XiV2zqKNphp9EJMFglK9-s6bw9jFqI356h3wQtDUKscnF2RMObwJBAI2E-VW3ZyPylt0cttv9dOdjaNbR4qprtpp36pYyW-zoIT3FdQgPtCNTdvMsDXzpZ1yHm9waZ5QpiR0SDpnG2wsCQA5lldstN65Sblx9fEGl114f3dh39GkU0y5l0mCMJnUBnbHRHkzVz_Zkk05qZ2YtmN0TU374ObrPkVTO4x6ohwM";
    private static final String PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCzJszjvGQlbTmBd6OYC2k-CorZMwf-BU39pWjK4gGtxisIsZfqyFfJgBT3p0KNTuYsFFfLAGpjZjrColkDfcQe-26x6lNYUcOoGgiZXmTxUxHYpUAe40-i_U1qiQf-ZKU2CYiqbrEaszufGMEbphM7zipNMtI4tvYtcvgCUZ0TFQIDAQAB";

    public static void main(String[] args) throws Exception {

        /*String decrypt = "DjowuZvCjrQYDiM2nbDTgDUtzMBEWmva4sBVET_ZKQ-Q2_HggLeFHXbgWG4ifB5uf3K-Lnc5HFJMkA-S-wGIfb2stRcmGmRNFv6QOv4YPuIYQnyDwsObnPz7xYjMRN7P-oFEXz36ZL7CrQm1Q7yf2xrwiFUiEcP3idFlkGk2P4s";
        // decrypt = utils.BaseRSAUtils.encryptByPublicKey("hello".getBytes(), PUBLIC_KEY);
        System.out.println(decrypt);
        System.out.println(utils.BaseRSAUtils.decryptByPrivateKey(decrypt, privatekey));

        System.out.println(EncryptFC.EncryptType.Base64.name().toLowerCase());*/

        // System.out.println(EncryptFC.encrypt("1005054301000001".getBytes()));

        // Process process = Runtime.getRuntime().exec(new String[]{"cmd.exe /c ffmpeg -i"});
        ProcessBuilder processBuilder = new ProcessBuilder();
        List<String> list = new ArrayList<>();
        list.add("cmd.exe");
        list.add("/c");
        // list.add("ls");
        list.add("ffmpeg -f concat -safe 0 -protocol_whitelist \"file,http,https,tcp,tls\" -i list.txt -filter_complex \"[0:v]pad=0:0:0:0[vout]\" -map [vout] -filter_complex \"[0:a]volume=volume=0[aout]\" -map [aout] temp.mp4");
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
