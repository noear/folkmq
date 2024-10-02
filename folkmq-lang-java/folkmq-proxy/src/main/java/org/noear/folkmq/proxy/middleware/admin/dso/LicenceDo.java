package org.noear.folkmq.proxy.middleware.admin.dso;

/**
 * @author noear
 * @since 1.5
 */
public class LicenceDo {
    public String sn;
    public String version;
    public int edition;
    public String subscribe;
    public int months;
    public String consumer;
    public int tps;

    public boolean isValid = false;
    public String description = "Licence (for FolkMQ): Unauthorized (with legal risks)";
}
