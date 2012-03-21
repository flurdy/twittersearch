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
import org.springframework.web.client.RestTemplate;

public class HashTagSearch implements ITwitterSearch{

    private Logger log = LoggerFactory.getLogger(this.getClass());
    
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

    public Set<String> searchForUrls() throws IOException {
        return findUrls(1,new LinkedHashSet<String>());
    }

    // TODO handle if no more tweets found
    private Set<String> findUrls(int page,Set<String> existingUrls) throws IOException {
        final String tweets = findTweetsWithHashTag(page);
        final Set<String> newUrls = parseUrlsFromTweets(tweets);
        existingUrls = addUrls(newUrls, existingUrls);
        return existingUrls.size()<100 
                ? findUrls(page++,existingUrls)
                : existingUrls;
    }

    private Set<String> addUrls(Set<String> newUrls, Set<String> existingUrls) {
        for(String url : newUrls){
            if(!existingUrls.contains(url)){
                existingUrls.add(url);
            }
        }
        return existingUrls;
    }

    protected Set<String> parseUrlsFromTweets(String tweets) throws IOException {
        StopWatch stopWatch = new LoggingStopWatch("parseUrlsFromTweets main");
//        StopWatch factoryStopWatch = new LoggingStopWatch("parseUrlsFromTweets factory");
        final Set<String> urls = new LinkedHashSet<String>();
        final JsonParser jsonParser = new JsonFactory().createJsonParser(tweets);
//        factoryStopWatch.lap("lap 1");
//        try {
          jsonParser.nextToken();
//            if(log.isDebugEnabled()) log.debug("token 1: " + jsonParser.getCurrentName());
//        factoryStopWatch.lap("lap 2");
            while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                final String fieldName = jsonParser.getCurrentName();
//                if(log.isDebugEnabled()) log.debug("token 2: " + jsonParser.getCurrentName());
                jsonParser.nextToken();
//                if(log.isDebugEnabled()) log.debug("token 2.5: " + jsonParser.getCurrentName());
//                factoryStopWatch.lap("lap result");
                if ("results".equals(fieldName )) {
                    while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
//                        if(log.isDebugEnabled()) log.debug("token 3: " + jsonParser.getCurrentName());
                        jsonParser.nextToken();
                        final String resultsField = jsonParser.getCurrentName();
//                        if(log.isDebugEnabled()) log.debug("token 3.5: " + jsonParser.getCurrentName());
//                        factoryStopWatch.lap("lap entities");
                        if ("entities".equals(resultsField)) {
                            while (jsonParser.nextToken() != JsonToken.END_ARRAY  ) {
//                                if(log.isDebugEnabled()) log.debug("token 4: " + jsonParser.getCurrentName());
                                jsonParser.nextToken();
                                final String entityField = jsonParser.getCurrentName();
//                                if(log.isDebugEnabled()) log.debug("token 4.5: " + jsonParser.getCurrentName());
//                                factoryStopWatch.lap("lap urls");
                                if ("urls".equals(entityField)) {
                                    while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                                        final String urlsField = jsonParser.getCurrentName();
//                                        if(log.isDebugEnabled()) log.debug("token 5: " + jsonParser.getCurrentName());
                                       jsonParser.nextToken();
//                                        if(log.isDebugEnabled()) log.debug("token 5.5: " + jsonParser.getCurrentName());
//                                        factoryStopWatch.lap("lap url");
                                        if ("expanded_url".equals(urlsField)) {
                                            final String url = jsonParser.getText();
                                            if(!urls.contains(url)){
                                                urls.add(url);
                                            }
//                                            factoryStopWatch.lap("lap added: "+url);
//                                            if(log.isDebugEnabled()) log.debug("added url: " + url);
                                        }
                                    }
//                                } else {
//                                    if(log.isDebugEnabled()) log.debug("Not urls: " + entityField);
                                }
                            }
//                        } else {
//                            if(log.isDebugEnabled()) log.debug("Not entities: " + resultsField);
                        }
                    }
//                }    else {
//                    if(log.isDebugEnabled()) log.debug("Not results: " + fieldName);
                }
//                stopWatch.lap("ROOT Loop");
            }
            jsonParser.close();
            stopWatch.stop();
//         factoryStopWatch.stop();

//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        return urls;
    }


    private String findTweetsWithHashTag(final int page){
        final Map<String, String> parameters = new HashMap<String, String>(){{
            put("q", hashTag);
            put("rrp", ""+returnSize);
            put("result_type", "recent");
            put("include_entities","true");
            put("page",""+page);
        }};
        
        // TODO: query rest api
        
        restTemplate.getForObject(TWITTER_URL, String.class, parameters);
        
        return null;
    } 
    

}