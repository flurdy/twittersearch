package com.flurdy.twitter;

import java.io.IOException;
import java.util.*;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.perf4j.LoggingStopWatch;
import org.perf4j.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

public class HashTagSearch implements ITwitterSearch{

    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    private static final String TWITTER_URL ="http://search.twitter.com/search.json";
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

    public Set<String> searchForUrls()  {
        return findUrls(1,new LinkedHashSet<String>());
    }

    private Set<String> findUrls(int page,Set<String> existingUrls)   {
        final String tweets = findTweetsWithHashTag(page);
        if( parseNumberOfTweetsFound(tweets) > 0 ){
            final Set<String> newUrls = parseUrlsFromTweets(tweets);
            existingUrls = addNewUrls(newUrls, existingUrls);
            return existingUrls.size()<returnSize
                    ? findUrls(page++,existingUrls)
                    : existingUrls;   
        } else {
            return existingUrls;
        }
    }


    private Set<String> addNewUrls(Set<String> newUrls, Set<String> existingUrls) {
        for(String url : newUrls){
            if(!existingUrls.contains(url)){
                existingUrls.add(url);
            }
        }
        return existingUrls;
    }

    protected Set<String> parseUrlsFromTweets(String tweets) {
        try{
            StopWatch stopWatch = new LoggingStopWatch("parseUrlsFromTweets main");
            final Set<String> urls = new LinkedHashSet<String>();
            final JsonParser jsonParser = new JsonFactory().createJsonParser(tweets);
              jsonParser.nextToken();
                while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                    final String fieldName = jsonParser.getCurrentName();
                    jsonParser.nextToken();
                    if ("results".equals(fieldName )) {
                        while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                            jsonParser.nextToken();
                            final String resultsField = jsonParser.getCurrentName();
                            if ("entities".equals(resultsField)) {
                                while (jsonParser.nextToken() != JsonToken.END_ARRAY  ) {
                                    jsonParser.nextToken();
                                    final String entityField = jsonParser.getCurrentName();
                                    if ("urls".equals(entityField)) {
                                        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                                            final String urlsField = jsonParser.getCurrentName();
                                           jsonParser.nextToken();
                                            if ("expanded_url".equals(urlsField)) {
                                                final String url = jsonParser.getText();
                                                if(!urls.contains(url)){
                                                    urls.add(url);
                                                }
    //                                            if(log.isDebugEnabled()) log.debug("added url: " + url);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                jsonParser.close();
                stopWatch.stop();
            return urls;
        } catch (IOException exception){
            log.error("JSON parsing failed",exception);
            throw new IllegalArgumentException("JSON parsing failed");
        }
    }


    protected int parseNumberOfTweetsFound(String tweets) {
        try{
            StopWatch stopWatch = new LoggingStopWatch("parseNumberOfTweetsFound");
            int tweetCount = 0;
            final JsonParser jsonParser = new JsonFactory().createJsonParser(tweets);
            jsonParser.nextToken();
            while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                final String fieldName = jsonParser.getCurrentName();
                jsonParser.nextToken();
                if ("results".equals(fieldName )) {
                    while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                        jsonParser.nextToken();
                        final String resultsField = jsonParser.getCurrentName();
                        if ("entities".equals(resultsField)) {
                            tweetCount++;
                        }
                    }
                }
            }
            jsonParser.close();
            stopWatch.stop();
            return tweetCount;
        } catch (IOException exception){
            log.error("JSON parsing failed",exception);
            throw new IllegalArgumentException("JSON parsing failed");
        }
            
    }

    protected String findTweetsWithHashTag(final int page){
        final Map<String, String> parameters = new HashMap<String, String>(){{
            put("q", hashTag);
            put("rrp", ""+returnSize);
            put("result_type", "recent");
            put("include_entities","true");
            put("page",""+page);
        }};
        try{
            final String response = restTemplate.getForObject(TWITTER_URL, String.class, parameters);
            if(log.isDebugEnabled()) log.debug("Json returned: " + response);
            return response;
        } catch (HttpClientErrorException exception){
            log.warn("Twitter request failed",exception);
            throw new IllegalStateException("Twitter API not accepting request:"+exception.getMessage());
        }
    } 
    

}