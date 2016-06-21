package com.github.sandornemeth;

import org.junit.Before;
import org.junit.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HelloServiceTest {

    private HelloService serviceUnderTest;
    private StringRedisTemplate mockRedisTemplate;
    private ValueOperations<String, String> mockValueOps;

    @Before
    public void setUp() {
        mockRedisTemplate = mock(StringRedisTemplate.class);
        mockValueOps = mock(ValueOperations.class);
        serviceUnderTest = new HelloService(mockRedisTemplate);
        when(mockRedisTemplate.opsForValue()).thenReturn(mockValueOps);
    }

    @Test
    public void storeDataInRedis() {
        doNothing().when(mockValueOps).set(anyString(), anyString());
        serviceUnderTest.receiveMessage("test:test");
    }
}