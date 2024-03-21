package org.noear.folkmq.middleware.broker.admin.utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * @author noear
 * @since 1.0
 */

public class ImageUtils {
    /*
     * 获取验证码图片
     */
    public static BufferedImage getValidationImage(String validation) {
        return getValidationImage(validation, 90, 40);
    }

    public static BufferedImage getValidationImage(String validation, int imgWidth, int imgHeight) {

        BufferedImage buffImg = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = buffImg.createGraphics();


        Random random = new Random();

        // 将图像填充为白色
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, imgWidth, imgHeight);

        // 创建字体
        Font font = new Font("sans-serif", Font.BOLD, 20);
        // 设置字体
        g.setFont(font);

        int red = 0, green = 0, blue = 0;

        for (int i = 0; i < validation.length(); i++) {
            // 取出待绘制字符
            String stringToDraw = "" + validation.charAt(i);
            // 随机颜色
            red = random.nextInt(155) + 50;
            green = random.nextInt(155) + 50;
            blue = random.nextInt(155) + 50;


            //坐标
            int x = i * 20 + 5;
            int y = 25 + random.nextInt(10) * (random.nextInt(2) % 2 == 0 ? 1 : -1);
            g.setColor(new Color(red,green,blue));


            g.drawString(stringToDraw, x, y);
        }

        //画些直线
        int LINE_MAX_COUNT = 3;
        int x1 = 0, y1 = 0, x2 = 0, y2 = 0;
        for (int i = 0; i < LINE_MAX_COUNT; i++) {
            // 坐标
            x1 = random.nextInt(imgWidth);
            y1 = random.nextInt(imgHeight);
            x2 = random.nextInt(imgWidth);
            y2 = random.nextInt(imgHeight);

            // 颜色
            red = random.nextInt(155) + 50;
            green = random.nextInt(155) + 50;
            blue = random.nextInt(155) + 50;

            g.setColor(new Color(red,green,blue));

            g.drawLine(x1,y1,x2,y2);
        }

        return buffImg;
    }
}

