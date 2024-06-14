package org.noear.folkmq.proxy.middleware.admin.dso;

import org.noear.folkmq.utils.IoUtils;
import org.noear.snack.ONode;
import org.noear.snack.core.Feature;
import org.noear.solon.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * @author noear
 * @since 1.5
 */
public class ConfigUtils {
    private static final Logger log = LoggerFactory.getLogger(ConfigUtils.class);

    private static ONode _dom;

    private static ONode dom() {
        if (_dom == null) {
            Utils.locker().lock();

            try {
                if (_dom == null) {
                    File file = new File("./data/folkmq.json");
                    if (file.exists()) {
                        try {
                            String json = IoUtils.readFile(file);
                            _dom = ONode.loadStr(json);
                        } catch (Exception ex) {
                            log.warn(ex.getMessage(), ex);
                        }
                    }

                    if(_dom == null) {
                        _dom = new ONode();
                    }

                    _dom.options().add(Feature.PrettyFormat);
                }
            } finally {
                Utils.locker().unlock();
            }
        }

        return _dom;
    }

    private static void save() {
        if (_dom == null) {
            return;
        }

        try {
            File dir = new File("./data/");
            if (dir.exists() == false) {
                dir.mkdirs();
            }

            File file = new File("./data/folkmq.json");
            if (file.exists() == false) {
                file.createNewFile();
            }

            String json = _dom.toJson();

            IoUtils.saveFile(file, json);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

    public static void set(String key, String value) {
        dom().set(key, value);
        save();
    }

    public static String get(String key) {
        return dom().get(key).getString();
    }
}
