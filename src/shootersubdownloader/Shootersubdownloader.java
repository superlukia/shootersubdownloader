/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package shootersubdownloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.mozilla.universalchardet.UniversalDetector;

/**
 *
 * @author superlukia
 */
public class Shootersubdownloader {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        File f = new File("F:\\downloads\\Game.of.Thrones.S06E06.Hybrid.720p.HDTV.x264-DON.mkv");

        down(f);
    }

    private static void down(File f) throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        String url = String.format("https://www.shooter.cn/api/subapi.php?filehash=%s&pathinfo=%s&format=json", computefilehash(f), f.getName());
        System.out.println(url);
        HttpGet request = new HttpGet(url);
        CloseableHttpResponse r = httpclient.execute(request);
        System.out.println(r.getStatusLine());
        HttpEntity e = r.getEntity();
        String s = EntityUtils.toString(e);
        System.out.println(s);
        JSONArray json = JSONArray.fromObject(s);
//        JSONObject json = JSONObject.fromObject(s);
        System.out.println(json.size());
        for (int i = 0; i < json.size(); i++) {
            System.out.println(i);
            JSONObject obj = json.getJSONObject(i);
            JSONArray fs = obj.getJSONArray("Files");
            String downurl = fs.getJSONObject(0).getString("Link");
            HttpGet r2 = new HttpGet(downurl);
            CloseableHttpResponse res2 = httpclient.execute(r2);
//            Header[] headers = res2.getAllHeaders();
//            for(Header h:headers){
//                System.out.println(h.getName());
//                System.out.println(h.getValue());
//            }
            Header header = res2.getFirstHeader("Content-Disposition");
            String sig = "filename=";
            String v = header.getValue();
            String fn = v.substring(v.indexOf(sig) + sig.length());
            HttpEntity e2 = res2.getEntity();
            File outf = new File(fn);
            FileOutputStream fos = new FileOutputStream(outf);
            e2.writeTo(fos);
            
            System.out.println(filecharsetdetect.FileCharsetDetect.detect(outf));
//            res2.getEntity().writeTo(new FileOutputStream(fn));
            System.out.println(fn);
            res2.close();
        }

        r.close();
        httpclient.close();
    }


    static String computefilehash(File f) throws Exception {
        if (!f.exists() || f.length() < 8 * 1024) {
            return null;
        }
        long l = f.length();
        long[] offset = new long[4];
        offset[3] = l - 8 * 1024;
        offset[2] = l / 3;
        offset[1] = l / 3 * 2;
        offset[0] = 4 * 1024;
        byte[] bBuf = new byte[1024 * 4];
        RandomAccessFile raf = new RandomAccessFile(f, "r");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            raf.seek(offset[i]);
            int readlen = raf.read(bBuf, 0, 4 * 1024);
            String md5 = md5(bBuf);
            if (sb.length() != 0) {
                sb.append("%3B");
            }
            sb.append(md5);
        }
        raf.close();
        return sb.toString();
    }

    static String md5(byte[] bs) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(bs);
            BigInteger number = new BigInteger(1, messageDigest);
            String hashtext = number.toString(16);
            // Now we need to zero pad it if you actually want the full 32 chars.
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger number = new BigInteger(1, messageDigest);
            String hashtext = number.toString(16);
            // Now we need to zero pad it if you actually want the full 32 chars.
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writesrt(JSONArray fs) {

    }
}
