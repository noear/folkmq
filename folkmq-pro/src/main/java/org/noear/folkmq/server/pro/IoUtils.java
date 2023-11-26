package org.noear.folkmq.server.pro;

import java.io.*;

/**
 * @author noear
 * @since 1.0
 */
public class IoUtils {
    public static String readFile(File file) throws IOException {
        StringBuilder buf = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                buf.append(line);
                buf.append(System.lineSeparator());
            }
        }

        return buf.toString();
    }

    public static void saveFile(File file, String content) throws IOException {
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(content);
        }
    }
}
