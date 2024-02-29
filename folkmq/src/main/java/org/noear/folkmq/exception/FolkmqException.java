package org.noear.folkmq.exception;

/**
 * 异常
 *
 * @author noear
 * @since 1.0
 */
public class FolkmqException extends RuntimeException {
    public FolkmqException(String message) {
        super(message);
    }

    public FolkmqException(Throwable cause) {
        super(cause);
    }
}
