package org.noear.folkmq.server;

@FunctionalInterface
public interface MqStarter {
    void start() throws Exception;
}