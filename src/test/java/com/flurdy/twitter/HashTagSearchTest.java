package com.flurdy.twitter;

import org.junit.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class HashTagSearchTest {

    private final Pattern httpMatcher = Pattern.compile("^https?:\\/\\/");
    private final Pattern nospaceMatcher = Pattern.compile("[^ ]");

    private final String tweets = "{\n\"results\":[\n" +
            "{\"entities\":{\"urls\":[{\"expanded_url\":\"http://www.example.com\"}]}}," +
            "{\"entities\":{\"urls\":[{\"expanded_url\":\"http://www.example.org\"}]}}," +
            "{\"entities\":{\"urls\":[{\"expanded_url\":\"http://www.example.net\"}]}}," +
            "{\"entities\":{\"urls\":[{\"expanded_url\":\"http://www.example.com/blah\"}]}}," +
            "{\"entities\":{\"urls\":[{\"expanded_url\":\"http://www.example.com/foobar\"}]}}" +
            "]}";

    @Test(timeout=1000)
    public void parseUrlsFromJson() throws IOException {
        Set<String> urls = new HashTagSearch("ford",5).parseUrlsFromTweets(tweets);
        assertEquals(5,urls.size());
        for( String url : urls ){
            assertTrue(startsWithHttp(url));
        }
    }


    @Test(timeout=1000)
    @Ignore
    public void retrieveUrlsFromMockedApi() throws IOException {
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplate.getForObject(anyString(), String.class, anyMap())).thenReturn(tweets);
        Set<String> urls = new HashTagSearch(restTemplate,"football",5).searchForUrls();
        assertEquals(5,urls.size());
        for( String url : urls ){
            assertTrue(startsWithHttp(url));
        }
    }

    @Test(timeout=1000)
//    @Ignore
    public void testUrlsAreInOrder() throws IOException {
        String orderedTweets = "{\n\"results\":[\n" +
                "{\"entities\":{\"urls\":[{\"expanded_url\":\"http://www.example.com\"}]}}," +
                "{\"entities\":{\"urls\":[{\"expanded_url\":\"http://www.example.org\"}]}}," +
                "{\"entities\":{\"urls\":[{\"expanded_url\":\"http://www.example.net\"}]}}" +
                "]\n}";
        Set<String> urls = new HashTagSearch("morning",3).parseUrlsFromTweets(orderedTweets );
        Iterator iterator = urls.iterator();
        assertEquals("http://www.example.com",iterator.next());
        assertEquals("http://www.example.org",iterator.next());
        assertEquals("http://www.example.net",iterator.next());
    }


    /***
     * Not extensive url validation
     */
    @Test(timeout=1000)
//    @Ignore
    public void containsValidUrls() throws IOException {
        Set<String> urls = new HashTagSearch("morning",3).parseUrlsFromTweets(tweets);
        for(String url : urls){
            assert startsWithHttp(url) : "Url does not start with http";
            assert containsNoSpaces(url) : "Url contains spaces";
        }
    }

    @Test(timeout=1000)
    @Ignore
    public void catchInvalidUrls1() throws IOException {
        String tweets = "{\n\"results\":[\n" +
                "{\"entities\":{\"urls\":[{\"expanded_url\":\"http://www.example.com\"}]}}," +
                "{\"entities\":{\"urls\":[{\"expanded_url\":\"htp://www.example.org\"}]}}," +
                "{\"entities\":{\"urls\":[{\"expanded_url\":\"http://www.example.net\"}]}}" +
                "]\n}";
        Set<String> urls = new HashTagSearch("morning",3).parseUrlsFromTweets(tweets);
        for(String url : urls){
            assert startsWithHttp(url) : "Url does not start with http";
            assert containsNoSpaces(url) : "Url contains spaces";
        }
        fail("Invalid URL included");
    }


    @Test(timeout=1000)
    @Ignore
    public void catchInvalidUrls2() throws IOException {
        String tweets = "{\n\"results\":[\n" +
                "{\"entities\":{\"urls\":[{\"expanded_url\":\"http://www.example.com\"}]}}," +
                "{\"entities\":{\"urls\":[{\"expanded_url\":\"www.example.org\"}]}}," +
                "{\"entities\":{\"urls\":[{\"expanded_url\":\"http://www.example.net\"}]}}" +
                "]\n}";
        Set<String> urls = new HashTagSearch("morning",3).parseUrlsFromTweets(tweets);
        for(String url : urls){
            assert startsWithHttp(url) : "Url does not start with http";
            assert containsNoSpaces(url) : "Url contains spaces";
        }
        fail("Invalid URL included");
    }

    @Test(timeout=1000)
//    @Ignore
    public void findUniqueUrls() throws IOException {
        String duplicateTweets = "{\n\"results\":[\n" +
            "{\"entities\":{\"urls\":[{\"expanded_url\":\"http://www.example.com\"}]}}," +
            "{\"entities\":{\"urls\":[{\"expanded_url\":\"http://www.example.org\"}]}}," +
            "{\"entities\":{\"urls\":[{\"expanded_url\":\"http://www.example.net\"}]}}," +
            "{\"entities\":{\"urls\":[{\"expanded_url\":\"http://www.example.org\"}]}}," +
            "{\"entities\":{\"urls\":[{\"expanded_url\":\"http://www.example.com/foobar\"}]}}" +
            "]}";

        Set<String> uniqueUrls = new HashSet<String>(10);
        Set<String> urls = new HashTagSearch("argentina",5).parseUrlsFromTweets(duplicateTweets);
        assertEquals(4,urls.size());
        for(String url : urls ){
            if( uniqueUrls.contains(url) ) {
                fail("Url is not unique");
            } else {
                uniqueUrls.add(url);
            }
        }
    }


    private boolean startsWithHttp(String url){
        return httpMatcher.matcher(url).find();
    }
    private boolean containsNoSpaces(String url){
        return nospaceMatcher.matcher(url).find();
    }

//    @Test
//    public void findFewTweetsForObscureHashTag(){
//        fail();
//    }

    
//    @Test
//    public void allTweetsContainsHashTag(){
//        fail();
//    }

}
