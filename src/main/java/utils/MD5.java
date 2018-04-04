package utils;

import java.security.MessageDigest;

public class MD5
{

    public MD5()
    {
    }

    public static String md5(String str)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            byte b[] = md.digest();
            StringBuffer sb = new StringBuffer();
            for(int i = 0; i < b.length; i++)
            {
                int v = b[i];
                v = v >= 0 ? v : 256 + v;
                String cc = Integer.toHexString(v);
                if(cc.length() == 1)
                    sb.append('0');
                sb.append(cc);
            }

            return sb.toString();
        }
        catch(Exception exception)
        {
            return "";
        }
    }
}
