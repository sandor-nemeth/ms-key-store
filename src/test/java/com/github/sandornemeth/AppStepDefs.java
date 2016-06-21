package com.github.sandornemeth;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.awaitility.Awaitility;
import org.hamcrest.CoreMatchers;
import redis.clients.jedis.Jedis;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author sandornemeth
 */
public class AppStepDefs {

    Connection rabbitConnection;
    Jedis jedis;
    String key;
    String value;

    @Before
    public void setUp() throws Exception {
        String rabbitMqHost = System.getProperty("rabbit.host", "rabbitmq");
        String redisHost = System.getProperty("redis.host", "redis");
        jedis = new Jedis(redisHost);
        ConnectionFactory rabbitConnFactory = new ConnectionFactory();
        rabbitConnFactory.setHost(rabbitMqHost);
        rabbitConnection = rabbitConnFactory.newConnection();
    }

    @Given("^a key (.*) and a value (.*)$")
    public void aKeyAtTestkeyAndAValueAtTestvalue(String key, String value)
            throws Throwable {
        this.key = key;
        this.value = value;
    }

    @When("^I store the key$")
    public void iStoreTheKey() throws Throwable {
        String msg = key + ":" + value;
        Channel channel = rabbitConnection.createChannel();
        channel.basicPublish("kv-store-exchange", "kv-store",
                MessageProperties.PERSISTENT_TEXT_PLAIN, msg.getBytes());
        channel.close();
    }

    @Then("^I can retrieve the value via the HTTP API$")
    public void iCanRetrieveTheValueViaTheHTTPAPI() throws Throwable {
        CloseableHttpClient client = HttpClientBuilder.create().build();
        CloseableHttpResponse response =
                client.execute(new HttpGet("http://app:8080/get/" + key));
        assertThat(response.getStatusLine().getStatusCode(), is(200));
        assertThat(IOUtils.toString(response.getEntity().getContent()),
                is(value));
    }

    @After
    public void tearDown() throws Exception {
        rabbitConnection.close();
        jedis.close();
    }
}
