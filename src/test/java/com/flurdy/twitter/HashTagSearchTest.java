package com.flurdy.twitter;


import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;


public class HashTagSearchTest {

    @Test
    public void find100Urls(){
        Set<String> tweets = new HashTagSearch("football",100).searchForUrls();
        assertEquals(100, tweets.size());
    }

    /***
     * Not extensive url validation
     */
    @Test
    public void searchResultContainsValidUrls(){
        Set<String> urls = new HashTagSearch("brazil",5).searchForUrls();
        for(String url : urls){
            assert startsWithHttp(url) : "Url does not start with http";
            assert containsNoSpaces(url) : "Url contains spaces";
        }
    }

    private boolean startsWithHttp(String url){
        return url.matches("^https?:\\/\\/");
    }

    private boolean containsNoSpaces(String url){
        return url.matches("[^ ]");
    }

//    @Test
//    public void findFewTweetsForObscureHashTag(){
//        fail();
//    }

    @Test
    public void findUniqueUrls(){
        Set<String> uniqueUrls = new HashSet<String>(10);
        for(String url : new HashTagSearch("elvis",10).searchForUrls() ){
            if( uniqueUrls.contains(url) ) {
                fail("Url is not unique");
            } else {
                uniqueUrls.add(url);
            }
        }
    }

    
//    @Test
//    public void allTweetsContainsHashTag(){
//        fail();
//    }

}
