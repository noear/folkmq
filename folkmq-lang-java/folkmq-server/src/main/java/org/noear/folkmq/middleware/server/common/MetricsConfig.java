package org.noear.folkmq.server.pro.common;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;

/**
 * @author noear
 * @since 1.0
 */
@Configuration
public class MetricsConfig {
    public static boolean isStandalone;
    @Bean
    public void custom(MeterRegistry registry) {
        //添加几个 meter
        new JvmMemoryMetrics().bindTo(registry);
        new JvmThreadMetrics().bindTo(registry);
        new ProcessorMetrics().bindTo(registry);
    }
}
