package com.github.sandornemeth;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.awaitility.Awaitility;
import org.hamcrest.CoreMatchers;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class AppIT {

    private static Jedis jedis;
    private static Connection rabbitConnection;

    @BeforeClass
    public static void setUpClass() throws Exception {
        String redisHost = System.getProperty("redis.host", "redis");
        String rabbitMqHost = System.getProperty("rabbit.host", "rabbitmq");
        jedis = new Jedis(redisHost);
        ConnectionFactory rabbitConnFactory = new ConnectionFactory();
        rabbitConnFactory.setHost(rabbitMqHost);
        rabbitConnection = rabbitConnFactory.newConnection();
    }

    @Before
    public void setUp() {
        jedis.keys("*").forEach(jedis::del);
    }

    @Test
    public void readsFromMqAndStoresInRedis() throws IOException {
        Channel channel = rabbitConnection.createChannel();
        channel.basicPublish("kv-store-exchange", "kv-store",
                MessageProperties.PERSISTENT_TEXT_PLAIN,
                "itest-one:test".getBytes());

        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> {
            return jedis.get("itest-one");
        }, CoreMatchers.equalTo("test"));
    }

    @Test
    public void readsFromRedisViaHttp() throws IOException {
        jedis.set("itest-two", "test-value");
        CloseableHttpClient client = HttpClientBuilder.create().build();
        HttpGet get = new HttpGet("http://app:8080/get/itest-two");
        CloseableHttpResponse response = client.execute(get);
        assertThat(response.getStatusLine().getStatusCode(), is(200));
        String responseString =
                IOUtils.toString(response.getEntity().getContent());
        assertThat(responseString, is("test-value"));
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        jedis.close();
        rabbitConnection.close();
    }
}
