package org.noear.folkmq.broker.admin.dso;

import org.noear.snack.ONode;
import org.noear.solon.Solon;
import org.noear.solon.Utils;
import org.noear.solon.cloud.utils.http.HttpUtils;
import org.noear.solon.core.handle.Result;

/**
 * @author noear
 * @since 1.0
 */
public class LicenceUtils {
    private static String licence = null;
    private static int isAuthorized = 0;
    private static String subscribeDate;
    private static int subscribeMonths;
    private static String consumer;

    public static String getLicence() {
        if (licence == null) {
            licence = Solon.cfg().get("folkmq.licence", "");
        }

        return licence;
    }

    public static String getLicence2() {
        StringBuilder buf = new StringBuilder();
        String[] ary = getLicence().split("-");
        for (String s : ary) {
            if (s.length() > 8) {
                buf.append(s.substring(0, s.length() - 6) + "******");
            } else if (s.length() > 6) {
                buf.append(s.substring(0, s.length() - 4) + "****");
            } else {
                buf.append(s.substring(0, s.length() - 2) + "**");
            }
            buf.append("-");
        }
        buf.setLength(buf.length() - 1);
        return buf.toString();
    }

    public static boolean isValid() {
        if (Utils.isEmpty(getLicence()) || getLicence().length() != 36) {
            return false;
        } else {
            return true;
        }
    }

    public static int isAuthorized() {
        return isAuthorized;
    }

    public static int getSubscribeMonths() {
        return subscribeMonths;
    }

    public static String getSubscribeDate() {
        return subscribeDate;
    }

    public static String getConsumer() {
        return consumer;
    }

    public static Result auth() {
        try {
            String json = HttpUtils.http("https://folkmq.noear.org/licence/auth")
                    .data("licence", LicenceUtils.getLicence())
                    .post();

            ONode oNode = ONode.loadStr(json);
            int code = oNode.get("code").getInt();
            String description = oNode.get("description").getString();

            if (code == 200) {
                isAuthorized = 1;
                subscribeDate = oNode.get("data").get("subscribe_date").getString();
                subscribeMonths = oNode.get("data").get("subscribe_months").getInt();
                consumer = oNode.get("data").get("consumer").getString();

                return Result.succeed(description);
            } else {
                if (code == 401) {
                    isAuthorized = -1;
                } else {
                    isAuthorized = 0;
                }


                return Result.failure(code, description);
            }
        } catch (Exception e) {
            isAuthorized = 0;
            return Result.failure(400, "检测出错：" + e.getMessage());
        }
    }
}
