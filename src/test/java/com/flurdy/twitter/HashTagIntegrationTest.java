package com.flurdy.twitter;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class HashTagIntegrationTest  {

    private final Pattern httpMatcher = Pattern.compile("^https?:\\/\\/");

    @Test
    @Ignore
    public void find100UrlsFromTwitter() throws IOException {
        Set<String> tweets = new HashTagSearch("football",100).searchForUrls();
        assertEquals(100, tweets.size());
    }

    @Test
    @Ignore
    public void checkTwitterUrlsAreHttp() throws IOException {
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
