package com.github.sandornemeth;

import java.io.IOException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AppAT {

    @Test
    public void hasHealthCheck() throws IOException {
        HttpGet httpGet = new HttpGet("http://app:8080/health");

        CloseableHttpClient client = HttpClientBuilder.create().build();
        CloseableHttpResponse response = client.execute(httpGet);

        assertThat(response.getStatusLine().getStatusCode(), is(200));
    }

}
