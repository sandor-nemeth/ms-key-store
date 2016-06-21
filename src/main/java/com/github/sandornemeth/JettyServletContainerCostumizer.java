package com.github.sandornemeth;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jetty9.InstrumentedHandler;
import org.eclipse.jetty.server.Server;
import org.springframework.boot.context.embedded
        .ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded
        .EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.jetty
        .JettyEmbeddedServletContainerFactory;

/**
 * {@link EmbeddedServletContainerCustomizer} implementation providing
 * Dropwizard metrics for the embedded Jetty in spring-boot.
 * <p>
 * By default prefixes every metric with <i>jetty9</i>.
 * </p>
 * <p>
 * Usage:
 * </p>
 * <pre>
 *     <code>
 *          @Bean public EmbeddedServletContainerCustomizer metricCustomizer
 *          ({@link MetricRegistry} registry) {
 *              return new JettyServlectContainerCostumizer(registry,
 *              "a-prefix");
 *          }
 *     </code>
 * </pre>
 *
 * @author sandornemeth
 */
public class JettyServletContainerCostumizer implements
        EmbeddedServletContainerCustomizer {

    private static final String DEFAULT_PREFIX = "jetty9";

    private MetricRegistry registry;
    private String prefix;

    /**
     * Constructor.
     *
     * @param registry the metric registry
     */
    public JettyServletContainerCostumizer(MetricRegistry registry) {
        this(registry, DEFAULT_PREFIX);
    }

    /**
     * Constructor.
     *
     * @param registry the metric registry
     * @param prefix   the prefix to be used
     */
    public JettyServletContainerCostumizer(MetricRegistry registry,
            String prefix) {
        this.registry = registry;
        this.prefix = prefix;
    }

    @Override
    public void customize(ConfigurableEmbeddedServletContainer container) {
        JettyEmbeddedServletContainerFactory factory =
                (JettyEmbeddedServletContainerFactory) container;
        factory.addServerCustomizers(this::customize);
    }

    protected void customize(Server server) {
        InstrumentedHandler handler =
                new InstrumentedHandler(registry, prefix);
        handler.setHandler(server.getHandler());
        server.setHandler(handler);
    }
}
