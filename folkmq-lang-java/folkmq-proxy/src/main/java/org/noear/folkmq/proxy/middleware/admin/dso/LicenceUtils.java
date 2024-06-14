package org.noear.folkmq.proxy.middleware.admin.dso;

import org.noear.folkmq.proxy.middleware.common.MqConfigNames;
import org.noear.snack.core.utils.DateUtil;
import org.noear.solon.Utils;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * @author noear
 * @since 1.0
 */
public class LicenceUtils {

    private static LicenceUtils global = new LicenceUtils();

    public static LicenceUtils getGlobal() {
        return global;
    }

    private static final String publicKey = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAI6+FX3DPmY0/dLXOOiVJwBhllQ6a34+8/WKS77L7BB9Ch3oyqXA41zqO3vQM2COIZDTxKSgPuRkOlFaKptoG0cCAwEAAQ==";

    private LicenceDo licenceInfo = new LicenceDo();

    /**
     * 串号
     */
    public String getSn() {
        return licenceInfo.sn;
    }

    /**
     * 产品版本
     */
    public int getEdition() {
        return licenceInfo.edition;
    }

    /**
     * 产品版本名字
     */
    public String getEditionName() {
        if (licenceInfo.edition == 23) {
            return "Enterprise Ultimate Edition";
        } else if (licenceInfo.edition == 22) {
            return "Enterprise Premium Edition";
        } else if (licenceInfo.edition == 21) {
            return "Enterprise Standard edition";
        } else if (licenceInfo.edition > 0) {
            return "Unknown Edition";
        } else {
            return "Community Edition";
        }
    }

    /**
     * 订阅者
     */
    public String getSubscribe() {
        return licenceInfo.subscribe;
    }

    /**
     * 订阅月数
     */
    public int getMonths() {
        return licenceInfo.months;
    }

    /**
     * 订阅月数字符串模式
     */
    public String getMonthsStr() {
        if (licenceInfo.months >= 36) {
            return "永久";
        } else {
            return licenceInfo.months + "个月";
        }
    }

    /**
     * 版本号
     */
    public String getVersion() {
        return licenceInfo.version;
    }

    /**
     * 客户
     */
    public String getConsumer() {
        return licenceInfo.consumer;
    }

    /**
     * 是否有效
     */
    public boolean isValid() {
        return licenceInfo.isValid;
    }

    /**
     * 是否过期
     */
    public boolean isExpired() {
        if (licenceInfo.months >= 36) {
            return false;
        }

        if(isValid() == false){
            //如果无效，则为过期
            return true;
        }

        try {
            LocalDate subscribeDate = DateUtil.parse(licenceInfo.subscribe).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            LocalDate tmp = subscribeDate.plusMonths(licenceInfo.months);
            //当前时间大于授权时间
            return LocalDate.now().compareTo(tmp) > 0;
        } catch (Throwable e) {
            return true;
        }
    }

    /**
     * 描述
     */
    public String getDescription() {
        return licenceInfo.description;
    }


    /**
     * 加载（解析）
     */
    public boolean load() {
        String licenceEncoded = ConfigUtils.get(MqConfigNames.folkmq_licence);

        return load(licenceEncoded);
    }

    /**
     * 加载（解析）
     */
    public boolean load(String licenceEncoded) {
        if (Utils.isEmpty(licenceEncoded)) {
            return false;
        }

        LicenceDo tmp = new LicenceDo();
        tmp.isValid = false;

        try {
            String licenceStr = LicenceHelper.licenceDecode(licenceEncoded, publicKey);
            String[] licence = licenceStr.split(",");

            if (licence.length >= 6) {
                tmp.sn = licence[0];
                tmp.edition = Integer.parseInt(licence[1]);
                tmp.version = licence[2];
                tmp.subscribe = licence[3];
                tmp.months = Integer.parseInt(licence[4]);
                tmp.consumer = licence[5];

                if (tmp.edition > 20 && tmp.edition < 30) {
                    //0 Community Edition, 21.Enterprise Standard edition, 22 Enterprise Premium Edition, 23 Enterprise Ultimate Edition

                    licenceInfo = tmp;
                    licenceInfo.isValid = true;

                    StringBuilder buf = new StringBuilder();
                    buf.append("Licence (for FolkMQ): ");
                    buf.append("SN=").append(getSn()).append(", ");
                    buf.append("E=").append(getEditionName()).append(", ");
                    buf.append("T=").append(getSubscribe()).append(", ");
                    buf.append("M=").append(getMonths());

                    licenceInfo.description = buf.toString();
                }
            }

        } catch (Throwable e) {

        }

        if (tmp.isValid) {
            ConfigUtils.set(MqConfigNames.folkmq_licence, licenceEncoded);
        } else {
            licenceInfo.description = "Licence (for FolkMQ): Unauthorized (with legal risks)";
        }

        return tmp.isValid;
    }
}