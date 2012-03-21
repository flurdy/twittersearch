package com.flurdy.twitter;

import java.util.*;
import org.springframework.web.client.RestTemplate;

public class HashTagSearch implements ITwitterSearch{

    private static final String TWITTER_URL ="http://search.twitter.com";
    private final String hashTag;
    private final int returnSize;
    private final RestTemplate restTemplate;

    public HashTagSearch(String hashTag,int returnSize) {
        this.restTemplate = new RestTemplate();
        this.hashTag = hashTag;
        this.returnSize = returnSize;
    }
    public HashTagSearch(RestTemplate restTemplate,String hashTag,int returnSize) {
        this.restTemplate = restTemplate;
        this.hashTag = hashTag;
        this.returnSize = returnSize;
    }

    public Set<String> searchForUrls() {
        Map<String, String> parameters = new HashMap<String, String>(){{
            put("q", hashTag);
            put("rrp", ""+returnSize);
            put("result_type", "recent");
            put("include_entities","true");
        }};

        restTemplate.getForObject(TWITTER_URL, String.class, parameters);

        // JACKSON parse

        return null;
    }

}
