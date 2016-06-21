package com.github.sandornemeth;

import java.util.concurrent.TimeUnit;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.awaitility.Awaitility;
import org.hamcrest.CoreMatchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AppIT {

    private static TestConnectionManager connectionManager;

    @BeforeClass
    public static void setUpClass() throws Exception {
        connectionManager = new TestConnectionManager();
    }

    @Test
    public void readsFromMqAndStoresInRedis() throws Exception {
        connectionManager.sendViaRabbit("itest-one:test");
        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .until(() -> connectionManager.getFromRedis("itest-one"),
                        CoreMatchers.equalTo("test"));
    }

    @Test
    public void readsFromRedisViaHttp() throws Exception {
        connectionManager.setRedisKey("itest-two", "test-value");
        CloseableHttpResponse response = connectionManager.httpGetRequest
                ("http://app:8080/get/itest-two");
        assertThat(response.getStatusLine().getStatusCode(), is(200));
        String responseString =
                IOUtils.toString(response.getEntity().getContent());
        assertThat(responseString, is("test-value"));
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        connectionManager.close();
    }
}
