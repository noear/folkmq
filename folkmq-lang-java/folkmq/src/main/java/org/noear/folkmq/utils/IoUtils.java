package org.noear.folkmq.utils;

import org.noear.socketd.utils.StrUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * 输入输出工具
 *
 * @author noear
 * @since 1.0
 */
public class IoUtils {
    /**
     * 将输入流转换为字符串
     *
     * @param ins     输入流
     * @param charset 字符集
     */
    public static String transferToString(InputStream ins, String charset) throws IOException {
        if (ins == null) {
            return null;
        }

        ByteArrayOutputStream outs = transferTo(ins, new ByteArrayOutputStream());

        if (StrUtils.isEmpty(charset)) {
            return outs.toString();
        } else {
            return outs.toString(charset);
        }
    }

    /**
     * 将输入流转换为byte数组
     *
     * @param ins 输入流
     */
    public static byte[] transferToBytes(InputStream ins) throws IOException {
        if (ins == null) {
            return null;
        }

        return transferTo(ins, new ByteArrayOutputStream()).toByteArray();
    }

    /**
     * 将输入流转换为输出流
     *
     * @param ins 输入流
     * @param out 输出流
     */
    public static <T extends OutputStream> T transferTo(InputStream ins, T out) throws IOException {
        if (ins == null || out == null) {
            return null;
        }

        int len = 0;
        byte[] buf = new byte[512];
        while ((len = ins.read(buf)) != -1) {
            out.write(buf, 0, len);
        }

        return out;
    }

    /**
     * 将输入流转换为输出流
     *
     * @param ins    输入流
     * @param out    输出流
     * @param start  开始位
     * @param length 长度
     */
    public static <T extends OutputStream> T transferTo(InputStream ins, T out, long start, long length) throws IOException {
        int len = 0;
        byte[] buf = new byte[512];
        int bufMax = buf.length;
        if (length < bufMax) {
            bufMax = (int) length;
        }

        if (start > 0) {
            ins.skip(start);
        }

        while ((len = ins.read(buf, 0, bufMax)) != -1) {
            out.write(buf, 0, len);

            length -= len;
            if (bufMax > length) {
                bufMax = (int) length;

                if (bufMax == 0) {
                    break;
                }
            }
        }

        return out;
    }


    /**
     * 读取文件
     */
    public static String readFile(File file) throws IOException {
        try (InputStream input = new FileInputStream(file)) {
            byte[] bytes = transferToBytes(input);

            //解压
            byte[] contentBytes = bytes;
            return new String(contentBytes, StandardCharsets.UTF_8);
        }
    }

    /**
     * 保存文件
     */
    public static void saveFile(File file, String content) throws IOException {
        //压缩
        byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
        byte[] bytes = contentBytes;

        try (ByteArrayInputStream input = new ByteArrayInputStream(bytes);
             OutputStream out = new FileOutputStream(file)) {
            transferTo(input, out);
        }
    }
}
