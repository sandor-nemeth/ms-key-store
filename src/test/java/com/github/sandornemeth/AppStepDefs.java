package com.github.sandornemeth;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AppStepDefs {

    TestConnectionManager connectionManager;
    String key;
    String value;

    @Before
    public void setUp() throws Exception {
        connectionManager = new TestConnectionManager();
    }

    @Given("^a key (.*) and a value (.*)$")
    public void aKeyAtTestkeyAndAValueAtTestvalue(String key, String value)
            throws Throwable {
        this.key = key;
        this.value = value;
    }

    @When("^I store the key$")
    public void iStoreTheKey() throws Throwable {
        connectionManager.sendViaRabbit(key + ":" + value);
    }

    @Then("^I can retrieve the value via the HTTP API$")
    public void iCanRetrieveTheValueViaTheHTTPAPI() throws Throwable {
        CloseableHttpClient client = HttpClientBuilder.create().build();
        CloseableHttpResponse response =
                connectionManager.httpGetRequest("http://app:8080/get/" + key);
        assertThat(response.getStatusLine().getStatusCode(), is(200));
        assertThat(IOUtils.toString(response.getEntity().getContent()),
                is(value));
    }

    @After
    public void tearDown() throws Exception {
        connectionManager.close();
    }
}
