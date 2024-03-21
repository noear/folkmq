package org.noear.folkmq.middleware.broker.admin.dso;

import org.noear.folkmq.middleware.broker.common.ConfigNames;
import org.noear.snack.core.utils.DateUtil;
import org.noear.solon.Solon;
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

    private String sn;
    private String version;
    private int edition;
    private String subscribe;
    private int months;
    private String consumer;

    private boolean isValid;
    private String description;

    public String getSn() {
        return sn;
    }

    public int getEdition() {
        return edition;
    }

    public String getEditionName() {
        if (edition == 23) {
            return "Enterprise Ultimate Edition";
        } else if (edition == 22) {
            return "Enterprise Premium Edition";
        } else if (edition == 21) {
            return "Enterprise Standard edition";
        } else if (edition > 0) {
            return "Unknown Edition";
        } else {
            return "Community Edition";
        }
    }

    public String getSubscribe() {
        return subscribe;
    }

    public int getMonths() {
        return months;
    }

    public String getMonthsStr() {
        if (months >= 36) {
            return "永久";
        } else {
            return months + "个月";
        }
    }

    public String getVersion() {
        return version;
    }

    public String getConsumer() {
        return consumer;
    }

    public boolean isValid() {
        return isValid;
    }

    public boolean isExpired() {
        if (months >= 36) {
            return false;
        }

        try {
            LocalDate subscribeDate = DateUtil.parse(subscribe).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            LocalDate tmp = subscribeDate.plusMonths(months);
            //当前时间大于授权时间
            return LocalDate.now().compareTo(tmp) > 0;
        } catch (Throwable e) {
            return true;
        }
    }

    public String getDescription() {
        if (description == null) {
            try {
                String appLicence = Solon.cfg().get(ConfigNames.folkmq_licence, "");

                if (Utils.isNotEmpty(appLicence)) {
                    String licenceStr = LicenceHelper.licenceDecode(appLicence, publicKey);
                    String[] licence = licenceStr.split(",");

                    if (licence.length >= 6) {
                        // 0:sn,1:e,2:v,3:t,4:m,5:c
                        sn = licence[0];
                        edition = Integer.parseInt(licence[1]);
                        version = licence[2];
                        subscribe = licence[3];
                        months = Integer.parseInt(licence[4]);
                        consumer = licence[5];

                        if (edition > 0) {
                            //sn,e,t,m,v,c  //e=0 Community Edition, 1 Professional Edition, 2 Enterprise Edition

                            StringBuilder buf = new StringBuilder();
                            buf.append("Licence (for FolkMQ): ");
                            buf.append("SN=").append(getSn()).append(", ");
                            buf.append("E=").append(getEditionName()).append(", ");
                            buf.append("T=").append(getSubscribe()).append(", ");
                            buf.append("M=").append(getMonths());

                            isValid = true;
                            description = buf.toString();
                        }
                    }
                }
            } catch (Throwable e) {

            }

            if (description == null) {
                description = "Licence (for FolkMQ): Unauthorized (with legal risks)";
            }
        }

        return description;
    }
}
