package com.github.sandornemeth;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jetty9.InstrumentedHandler;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.codahale.metrics.logback.InstrumentedAppender;
import org.eclipse.jetty.server.Server;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded
        .EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.jetty
        .JettyEmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.jetty.JettyServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Hello world!
 */
@SpringBootApplication
public class App {
    final static String queueName = "kv-store";

    @Autowired
    RabbitTemplate rabbitTemplate;

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    StringRedisTemplate template(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    @Bean
    public MetricRegistry metricRegistry() {
        MetricRegistry registry = new MetricRegistry();
        registry.register("jvm", new MemoryUsageGaugeSet());
        registry.register("threads", new ThreadStatesGaugeSet());
        registry.register("gc", new GarbageCollectorMetricSet());

        LoggerContext factory =
                (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger root = factory.getLogger(Logger.ROOT_LOGGER_NAME);

        InstrumentedAppender instrumentedAppender =
                new InstrumentedAppender(registry);
        instrumentedAppender.setContext(root.getLoggerContext());
        instrumentedAppender.start();
        return registry;
    }

    @Configuration
    protected static class JettyConfig {

        @Autowired
        private MetricRegistry registry;

        @Bean
        public JettyServerCustomizer containerCustomizer() {

            return server -> {
                InstrumentedHandler handler =
                        new InstrumentedHandler(registry, "jetty9");
                handler.setHandler(server.getHandler());
                server.setHandler(handler);
            };
        }

        @Bean
        public EmbeddedServletContainerCustomizer
        servletContainerCustomizer() {
            return container -> {
                JettyEmbeddedServletContainerFactory factory =
                        (JettyEmbeddedServletContainerFactory) container;
                factory.addServerCustomizers(containerCustomizer());
            };
        }
    }

    @Configuration
    protected static class RabbitMQConfig {

        @Autowired
        HelloService helloService;

        @Bean
        Queue queue() {
            return new Queue(queueName, false);
        }

        @Bean
        TopicExchange exchange() {
            return new TopicExchange("kv-store-exchange");
        }

        @Bean
        Binding binding(Queue queue, TopicExchange exchange) {
            return BindingBuilder.bind(queue).to(exchange).with(queueName);
        }

        @Bean
        SimpleMessageListenerContainer container(
                ConnectionFactory connectionFactory,
                MessageListenerAdapter listenerAdapter) {
            SimpleMessageListenerContainer container =
                    new SimpleMessageListenerContainer();
            container.setConnectionFactory(connectionFactory);
            container.setQueueNames(queueName);
            container.setMessageListener(listenerAdapter);
            return container;
        }

        @Bean
        MessageListenerAdapter messageListenerAdapter() {
            MessageListenerAdapter adapter = new MessageListenerAdapter
                    (helloService, "receiveMessage");
            adapter.setMessageConverter(new SimpleMessageConverter());
            return adapter;
        }

    }

}
