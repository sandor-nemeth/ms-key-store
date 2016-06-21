package com.github.sandornemeth;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * A simple controller.
 *
 * @author sandornemeth
 */
@RestController
public class HelloService {

    private StringRedisTemplate redis;

    @Autowired
    public HelloService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @RequestMapping(value = "/get/{key}", method = RequestMethod.GET)
    public String getValue(@PathVariable("key") String key) {
        return redis.opsForValue().get(key);
    }

    public void receiveMessage(String message) {
        String msg = String.valueOf(message);
        String[] split = msg.split(":");
        String key = split[0];
        String value = split.length > 1 ? split[1] : "";
        redis.opsForValue().set(key, value);
    }
}

