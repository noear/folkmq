package org.noear.folkmq.proxy.middleware.admin.model;

/**
 * @author noear
 * @since 1.0
 */
public class SessionVo {
    private String name;
    private int sessionCount;

    public void setName(String name) {
        this.name = name;
    }

    public void setSessionCount(int sessionCount) {
        this.sessionCount = sessionCount;
    }

    public String getName() {
        return name;
    }

    public int getSessionCount() {
        return sessionCount;
    }
}
