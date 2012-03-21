package com.flurdy.twitter;

import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private final Pattern httpMatcher = Pattern.compile("^https?:\\/\\/");
    private final Pattern nospaceMatcher = Pattern.compile("[^ ]");

    private final String tweets = "{\n\"results\":[\n" +
            "{\"from_user\":\"leonidas\",\"entities\":{\"urls\":[{\"expanded_url\":\"http://www.example.com\"}]}}," +
            "{\"from_user\":\"zico\",\"entities\":{\"urls\":[{\"expanded_url\":\"http://www.example.org\"}]}}," +
            "{\"from_user\":\"socrates\",\"entities\":{\"urls\":[{\"expanded_url\":\"http://www.example.net\"}]},\"created_at\":\"2012-12-24\"}," +
            "{\"from_user\":\"rivelino\",\"entities\":{\"urls\":[{\"expanded_url\":\"http://www.example.com/blah\"}]}}," +
            "{\"from_user\":\"garrincha\",\"entities\":{\"urls\":[{\"expanded_url\":\"http://www.example.com/foobar\"}]},\"created_at\":\"2012-12-24\"}" +
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
    public void testTweetsFound()  {
        int tweetCount = new HashTagSearch("ford",5).parseNumberOfTweetsFound(tweets);
        assertEquals(5,tweetCount);
    }

    @Test(timeout=1000)
    public void testNoTweetsFound()  {
        String noTweets = "{\"results\":[]}";
        int tweetCount = new HashTagSearch("basketball",5).parseNumberOfTweetsFound(noTweets);
        assertEquals(0,tweetCount);
    }



    @Test(timeout=1000)
//    @Ignore
    public void retrieveUrlsFromMockedApi()  {
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplate.getForObject(anyString(), any(Class.class), anyMap())).thenReturn(tweets);
        HashTagSearch hashTagSearch = new HashTagSearch(restTemplate,"football",5);
        Set<String> urls = hashTagSearch.searchForUrls();
        assertEquals(5,urls.size());
        for( String url : urls ){
            assertTrue(startsWithHttp(url));
        }
    }

    @Test(timeout=1000)
    public void testSimpleApiMock()  {
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplate.getForObject(anyString(), any(Class.class), anyMap())).thenReturn(tweets);
        HashTagSearch hashTagSearch = new HashTagSearch(restTemplate,"football",5);
        String json = hashTagSearch.findTweetsWithHashTag(1);
        Set<String> urls = hashTagSearch.parseUrlsFromTweets(json);
        assertEquals(5,urls.size());
        for( String url : urls ){
            assertTrue(startsWithHttp(url));
        }
    }

    @Test(timeout=1000)
//    @Ignore
    public void testUrlsAreInOrder()  {
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
    public void containsValidUrls()  {
        Set<String> urls = new HashTagSearch("morning",3).parseUrlsFromTweets(tweets);
        for(String url : urls){
            assert startsWithHttp(url) : "Url does not start with http";
            assert containsNoSpaces(url) : "Url contains spaces";
        }
    }

    @Test(timeout=1000)
    @Ignore
    public void catchInvalidUrls1()  {
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
    public void catchInvalidUrls2()  {
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
    public void findUniqueUrls()  {
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
