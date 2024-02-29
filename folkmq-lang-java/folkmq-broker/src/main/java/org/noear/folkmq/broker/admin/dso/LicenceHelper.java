package org.noear.folkmq.broker.admin.dso;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * @author noear
 * @since 2.7
 */
public class LicenceHelper {
    private static final String ALGORITHM = "RSA";
    private static final String SIGN_ALGORITHM = "MD5withRSA";

    private static final String PUBLICK_KEY = "PUBLICK_KEY";
    private static final String PRIVATE_KEY = "PRIVATE_KEY";

    private static String CODE_SPLIT = "#";
    private static String CODE_SK = "3xv11BQB5hY%*X!V";

    private static final int KEY_SIZE = 512;

    /**
     * 生成秘钥对
     */
    public static Map<String, Object> genKeyPair() throws NoSuchAlgorithmException {
        Map<String, Object> keyMap = new HashMap<>();

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
        keyPairGenerator.initialize(KEY_SIZE);

        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        keyMap.put(PUBLICK_KEY, publicKey);
        keyMap.put(PRIVATE_KEY, privateKey);

        return keyMap;
    }

    /**
     * 获取公钥
     */
    public static String getPublicKey(Map<String, Object> keyMap) {
        Key key = (Key) keyMap.get(PUBLICK_KEY);
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    /**
     * 获取私钥
     */
    public static String getPrivateKey(Map<String, Object> keyMap) {
        Key key = (Key) keyMap.get(PRIVATE_KEY);
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }


    /**
     * 签名并转为 base64
     */
    public static String signToBase64(String data, String base64PrivateKey) throws Exception {
        byte[] tmp = sign(data.getBytes(StandardCharsets.UTF_8), base64PrivateKey);
        return Base64.getEncoder().encodeToString(tmp);
    }

    /**
     * 签名
     */
    public static byte[] sign(byte[] data, String base64PrivateKey) throws Exception {
        // 获取私钥秘钥字节数组
        PrivateKey privateKey = getPrivateKey(base64PrivateKey);

        Signature signature = Signature.getInstance(SIGN_ALGORITHM);
        signature.initSign(privateKey);
        signature.update(data);

        return signature.sign();
    }

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
     * 获取私钥
     */
    public static PrivateKey getPrivateKey(String base64PrivateKey) throws Exception {
        // 得到私钥
        byte[] keyBytes = Base64.getDecoder().decode(base64PrivateKey.getBytes());
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(keyBytes));

        return privateKey;
    }

    /**
     * 编码
     */
    public static String encode(String data, String key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);

        byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

        return Base64.getEncoder().encodeToString(encrypted);
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
     * 许可证编码
     */
    public static String licenceEncode(String source, String base64PrivateKey) throws Exception {
        String signedSource = signToBase64(source, base64PrivateKey);
        return encode(source + CODE_SPLIT + signedSource, CODE_SK);
    }

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