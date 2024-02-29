package org.noear.folkmq.broker.admin.dso;

import org.noear.folkmq.broker.common.ConfigNames;
import org.noear.snack.core.utils.DateUtil;
import org.noear.solon.Solon;
import org.noear.solon.Utils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

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
    private int edition;
    private String subscribe;
    private int months;
    private String version;
    private String consumer;

    private boolean isValid;
    private String description;

    public String getSn() {
        return sn;
    }

    public int getEdition() {
        return edition;
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
                        sn = licence[0];
                        edition = Integer.parseInt(licence[1]);
                        subscribe = licence[2];
                        months = Integer.parseInt(licence[3]);
                        version = licence[4];
                        consumer = licence[5];

                        if (edition == 1 || edition == 2) {
                            //sn,e,t,m,v,c  //e=0 Community Edition, 1 Professional Edition, 2 Enterprise Edition

                            StringBuilder buf = new StringBuilder();
                            buf.append("Licence (for FolkMQ): ");
                            buf.append("SN=").append(licence[0]).append(", ");
                            if (edition == 2) {
                                buf.append("E=Enterprise Edition, ");
                            } else {
                                buf.append("E=Professional Edition, ");
                            }
                            buf.append("S=").append(licence[2]).append(", ");
                            buf.append("M=").append(licence[3]);

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
