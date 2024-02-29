package org.noear.folkmq.server;

@FunctionalInterface
public interface OnStart {
    void run() throws Exception;
}