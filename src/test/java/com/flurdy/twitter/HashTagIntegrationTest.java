package com.flurdy.twitter;

import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;
import static org.junit.Assume.*;

public class HashTagIntegrationTest  {

    private final Pattern httpMatcher = Pattern.compile("^https?:\\/\\/");
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private final String TWITTER_URL ="http://search.twitter.com/search.json" +
            "?q={q}&amp;rpp={rpp}&amp;result_type={result_type}&amp;include_entities={include_entities}";

    @Test(timeout=5000)
//    @Ignore
    public void testTwitterApi(){
        RestTemplate restTemplate = new RestTemplate();
        final Map<String, String> parameters = new HashMap<String, String>(){{
            put("q", "#github");
            put("rpp", ""+10);
            put("result_type", "recent");
            put("include_entities","true");
        }};
        try{
            final String response = restTemplate.getForObject(TWITTER_URL, String.class, parameters);
            if(log.isDebugEnabled()) log.debug("Json returned: " + response);
        } catch (HttpClientErrorException exception){
            log.warn(exception.getMessage());
            log.warn(exception.getResponseBodyAsString());
//            log.warn("Twitter request failed",exception);
//            throw exception;
            fail(exception.getMessage());
        }
    }

    @Test(timeout=5000)
    public void testTweetCountParser(){
        RestTemplate restTemplate = new RestTemplate();
        final Map<String, String> parameters = new HashMap<String, String>(){{
            put("q", "#github");
            put("rpp", ""+10);
            put("result_type", "recent");
            put("include_entities","true");
        }};
        try{
            final String response = restTemplate.getForObject(TWITTER_URL, String.class, parameters);
//            if(log.isDebugEnabled()) log.debug("Json returned: " + response);
            int tweetCount = new HashTagSearch("football",10).parseNumberOfTweetsFound(response);
            assertEquals(10,tweetCount);
        } catch (HttpClientErrorException exception){
            log.warn(exception.getMessage());
            log.warn(exception.getResponseBodyAsString());
            fail(exception.getMessage());
        }
    }

    @Test(timeout=5000)
//    @Ignore
    public void findUrlsFromTwitter()  {
        Set<String> tweets = new HashTagSearch("manchester",10).searchForUrls();
        assertEquals(10, tweets.size());
    }

    @Test(timeout=5000)
//    @Ignore
    public void find100ishUrlsFromTwitter()  {
        Set<String> tweets = new HashTagSearch("california",100).searchForUrls();
        assumeTrue(tweets.size()==100);
    }

    @Test(timeout=5000)
//    @Ignore
    public void findMoreThan20UrlsFromTwitter()  {
        Set<String> tweets = new HashTagSearch("messi",100).searchForUrls();
        assertTrue(tweets.size()>20);
    }

    @Test(timeout=5000)
    @Ignore
    public void checkTwitterUrlsAreHttp()  {
        Set<String> tweets = new HashTagSearch("football",100).searchForUrls();
        assertEquals(100, tweets.size());
        for( String url : tweets ){
            assertTrue(startsWithHttp(url));
        }
    }

    private boolean startsWithHttp(String url){
        return httpMatcher.matcher(url).find();
    }


}
