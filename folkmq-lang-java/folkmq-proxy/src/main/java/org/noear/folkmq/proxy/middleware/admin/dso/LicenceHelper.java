package org.noear.folkmq.proxy.middleware.admin.dso;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * @author noear
 * @since 2.7
 */
public class LicenceHelper {
    private static final String ALGORITHM = "RSA";
    private static final String SIGN_ALGORITHM = "MD5withRSA";

    private static String CODE_SPLIT = "#";
    private static String CODE_SK = "3xv11BQB5hY%*X!V";


    /**
     * 签名验证
     */
    public static boolean signVerifyFromBase64(String data, String base64SignedData, String base64PublicKey) throws Exception {
        return signVerify(data.getBytes(StandardCharsets.UTF_8), Base64.getDecoder().decode(base64SignedData), base64PublicKey);
    }

    /**
     * 签名验证
     */
    public static boolean signVerify(byte[] data, byte[] signedData, String base64PublicKey) throws Exception {
        // 得到公钥
        PublicKey publicKey = getPublicKey(base64PublicKey);

        Signature signature = Signature.getInstance(SIGN_ALGORITHM);
        signature.initVerify(publicKey);
        signature.update(data);

        return signature.verify(signedData);
    }

    /**
     * 获取公钥
     */
    public static PublicKey getPublicKey(String base64PublicKey) throws Exception {
        // 得到公钥
        byte[] keyBytes = Base64.getDecoder().decode(base64PublicKey.getBytes());
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(keyBytes));

        return publicKey;
    }

    /**
     * 解码
     */
    public static String decode(String base64EncryptedData, String key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");

        cipher.init(Cipher.DECRYPT_MODE, keySpec);

        byte[] encrypted = Base64.getDecoder().decode(base64EncryptedData);
        byte[] decrypted = cipher.doFinal(encrypted);

        return new String(decrypted, StandardCharsets.UTF_8);
    }

    //////////////////


    /**
     * 许可证解码
     */
    public static String licenceDecode(String encodedSource, String base64PublicKey) throws Exception {
        String licenceSource = decode(encodedSource, CODE_SK);
        String[] tmp = licenceSource.split(CODE_SPLIT);

        if (tmp.length == 2) {
            String source = tmp[0];
            String signedSource = tmp[1];

            if (signVerifyFromBase64(source, signedSource, base64PublicKey)) {
                return source;
            } else {
                throw new IllegalStateException("Invalid license signature");
            }
        } else {
            throw new IllegalStateException("Invalid license");
        }
    }
}