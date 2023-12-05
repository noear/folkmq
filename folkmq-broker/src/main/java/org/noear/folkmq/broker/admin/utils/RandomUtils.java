package org.noear.folkmq.broker.admin.utils;

import java.util.Random;

/**
 * @author noear
 * @since 1.0
 */
public class RandomUtils {
    public static String code(int size) {
        char codeTemplate[] = {
                'a', 'b', 'c', 'd', 'e', 'f',
                'g', 'h', 'j', 'k',
                'm', 'n', 'p', 'q', 'r',
                's', 't', 'u', 'v', 'w', 'x',
                'y', 'z',
                'A', 'B', 'C', 'D', 'E', 'F',
                'G', 'H', 'J', 'K', 'L',
                'M', 'N', 'P', 'Q', 'R',
                'S', 'T', 'U', 'V', 'W', 'X',
                'Y', 'Z',
                '2', '3', '4', '5',
                '6', '7', '8', '9'
        };
        int temp_size = codeTemplate.length;

        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            sb.append(codeTemplate[random.nextInt(temp_size) % temp_size]);
        }

        return sb.toString();
    }

    public static String codeExt(int size) {
        char codeTemplate[] = {
                'a', 'b', 'c', 'd', 'e', 'f',
                'g', 'h', 'i', 'j', 'k', 'l',
                'm', 'n', 'o', 'p', 'q', 'r',
                's', 't', 'u', 'v', 'w', 'x',
                'y', 'z',
                'A', 'B', 'C', 'D', 'E', 'F',
                'G', 'H', 'I', 'J', 'K', 'L',
                'M', 'N', 'O', 'P', 'Q', 'R',
                'S', 'T', 'U', 'V', 'W', 'X',
                'Y', 'Z',
                '0', '1', '2', '3', '4', '5',
                '6', '7', '8', '9'
        };
        int temp_size = codeTemplate.length;

        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            sb.append(codeTemplate[random.nextInt(temp_size) % temp_size]);
        }

        return sb.toString();
    }
}
