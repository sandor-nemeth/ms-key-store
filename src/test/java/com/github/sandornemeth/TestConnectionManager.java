package com.github.sandornemeth;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import redis.clients.jedis.Jedis;

public class TestConnectionManager implements AutoCloseable {

    private Jedis jedis;
    private CloseableHttpClient httpClient;
    private Connection rabbitConnection;
    private Channel rabbitChannel;

    public TestConnectionManager() throws Exception {
        String redisHost = System.getProperty("redis.host", "redis");
        String rabbitMqHost = System.getProperty("rabbit.host", "rabbitmq");
        jedis = new Jedis(redisHost);
        ConnectionFactory rabbitConnFactory = new ConnectionFactory();
        rabbitConnFactory.setHost(rabbitMqHost);
        rabbitConnection = rabbitConnFactory.newConnection();
        rabbitChannel = rabbitConnection.createChannel();
        httpClient = HttpClientBuilder.create().build();
    }

    public void sendViaRabbit(String message) throws Exception {
        rabbitChannel.basicPublish("kv-store-exchange", "kv-store",
                MessageProperties.PERSISTENT_TEXT_PLAIN,
                message.getBytes());
    }

    public void setRedisKey(String key, String value) {
        jedis.set(key, value);
    }

    public String getFromRedis(String key) {
        return jedis.get(key);
    }

    public CloseableHttpResponse httpGetRequest(String url) throws Exception {
        return httpClient.execute(new HttpGet(url));
    }

    @Override
    public void close() throws Exception {
        jedis.close();
        httpClient.close();
        rabbitChannel.close();
        rabbitConnection.close();
    }


}
