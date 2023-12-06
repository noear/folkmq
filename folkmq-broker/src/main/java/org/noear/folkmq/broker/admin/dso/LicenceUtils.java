package org.noear.folkmq.broker.admin.dso;

import org.noear.solon.Solon;

/**
 * @author noear
 * @since 1.0
 */
public class LicenceUtils {
    private static String licence = null;

    public static String getLicence() {
        if (licence == null) {
            licence = Solon.cfg().get("folkmq.licence", "");
        }

        return licence;
    }
}
