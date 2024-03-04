package org.noear.solon.extend.impl;

import org.noear.folkmq.broker.admin.dso.LicenceUtils;
import org.noear.solon.core.util.LicenceUtil;

/**
 * @author noear
 * @since 2.7
 */
public class LicenceUtilExt extends LicenceUtil {


    @Override
    public boolean isEnable() {
        return true;
    }

    @Override
    public String getDescription() {
        return LicenceUtils.getGlobal().getDescription();
    }
}
