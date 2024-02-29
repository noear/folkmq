package org.noear.folkmq.solon;

import org.noear.folkmq.client.*;
import org.noear.solon.Solon;
import org.noear.solon.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 转到 Handler 接口协议的 Listener
 *
 * @author noear
 * @since 1.2
 */
public class MqSolonListener implements MqConsumeListener {
    private static final Logger log = LoggerFactory.getLogger(MqSolonListener.class);


    @Override
    public void consume(MqMessageReceived message) throws Exception {
        if (Utils.isEmpty(message.getTag())) {
            log.warn("This message is missing route, tid={}", message.getTid());
            return;
        }

        try {
            MqSolonContext ctx = new MqSolonContext((MqMessageReceivedImpl) message);

            Solon.app().tryHandle(ctx);

            if (ctx.getHandled() || ctx.status() != 404) {
                ctx.commit();
            } else {
                message.response(new MqAlarm("No message handler found! like code=404"));
            }
        } catch (Throwable e) {
            //context 初始化时，可能会出错
            //
            log.warn(e.getMessage(), e);

            message.response(new MqAlarm("Message handler error: " + e.getMessage()));
        }
    }
}
