package com.flurdy.twitter;

import java.io.IOException;
import java.util.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.map.ObjectMapper;
import org.perf4j.LoggingStopWatch;
import org.perf4j.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

public class HashTagSearch implements ITwitterSearch{

    private Logger log = LoggerFactory.getLogger(this.getClass());

    protected static final String TWITTER_URL ="http://search.twitter.com/search.json";
    protected static final String TWITTER_QUERY =
            "?q={q}&amp;rpp={rpp}&amp;result_type={result_type}&amp;" +
            "include_entities={include_entities}";
    private final String hashTag;
    private final int returnSize;
    private final RestTemplate restTemplate;

    public HashTagSearch(RestTemplate restTemplate,String hashTag,int returnSize) {
        this.restTemplate = restTemplate;
        this.hashTag = hashTag.startsWith("#") ? hashTag : "#" + hashTag;
        this.returnSize = returnSize;
    }
    public HashTagSearch(String hashTag,int returnSize) {
        this(new RestTemplate(),hashTag,returnSize);
    }

    public Set<String> searchForUrls()  {
        return findUrls("",new LinkedHashSet<String>());
    }

    private Set<String> findUrls(String thisPage, Set<String> existingUrls)   {
        final String tweets = thisPage.equals("")
                   ? findTweetsWithHashTag()
                : findTweetsWithHashTag(thisPage);
        final int tweetCount = parseNumberOfTweetsFound(tweets);
        if( tweetCount > 0 ){
            final Set<String> newUrls = parseUrlsFromTweets(tweets);
            if( !newUrls.isEmpty() ){
                existingUrls = addNewUrls(newUrls, existingUrls);
                if( existingUrls.size()<returnSize ) {
                    final String nextPage = parseNextPage(tweets);
                    existingUrls = findUrls(nextPage,existingUrls);
                }
//            } else {
//                if(log.isDebugEnabled()) log.debug("No new URLS");
            }
//        } else {
//            if(log.isDebugEnabled()) log.debug("No tweets found");
        }
        return existingUrls;
    }


    private Set<String> addNewUrls(Set<String> newUrls, Set<String> existingUrls) {
        int matchCount = 0;
        for(String url : newUrls){
            if(existingUrls.size()>=returnSize){
                return existingUrls;
            } else if(!existingUrls.contains(url) ){
                existingUrls.add(url);
            } else {
                matchCount++;
            }
        }
        return existingUrls;
    }



    private String parseNextPage(String tweets) {
        final Set<String> urls = new LinkedHashSet<String>();
        final ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode treeNode = mapper.readTree(tweets);
            JsonNode pageNode = treeNode.path("next_page");
            return pageNode.getTextValue();
        } catch (IOException exception) {
            log.error("JSON parsing failed",exception);
            throw new IllegalArgumentException("JSON parsing failed");
        }
    }
    
    protected Set<String> parseUrlsFromTweets(String tweets) {
        final Set<String> urls = new LinkedHashSet<String>();
        try {
            final ObjectMapper mapper = new ObjectMapper();
            JsonNode treeNode = mapper.readTree(tweets);
            JsonNode resultsNode = treeNode.path("results");
            for( JsonNode tweetNode : resultsNode ){
                JsonNode entitiesNode = tweetNode.path("entities");
                JsonNode urlsNode = entitiesNode.path("urls");
                for( JsonNode urlNode : urlsNode ){
                    JsonNode expandedNode = urlNode.path("expanded_url");
                    final String url = expandedNode.getTextValue();
                    if(!urls.contains(url)){
                        urls.add(url);
//                        if(log.isDebugEnabled()) log.debug("added url: " + url);
                    }
                }
            }
        } catch (IOException exception) {
            log.error("JSON parsing failed",exception);
            throw new IllegalArgumentException("JSON parsing failed");
        }
        return urls;
    }

    protected int parseNumberOfTweetsFound(String tweets) {
        assert tweets.trim().length() > 0 : "no tweets";
        int tweetCount = 0;
        try{
            final ObjectMapper mapper = new ObjectMapper();
            JsonNode treeNode = mapper.readTree(tweets);
            JsonNode resultsNode = treeNode.path("results");
            for( JsonNode tweetNode : resultsNode ){
                tweetCount++;
            }
        } catch (IOException exception) {
            log.error("JSON parsing failed",exception);
            throw new IllegalArgumentException("JSON parsing failed");
        }
        return tweetCount;
    }
    protected String findTweetsWithHashTag(){
        final Map<String, String> parameters = new HashMap<String, String>(){{
            put("q", hashTag);
            put("rpp", ""+50);
            put("result_type", "recent");
            put("include_entities","true");
        }};
        try{
            final String response = restTemplate.getForObject(TWITTER_URL+TWITTER_QUERY, String.class, parameters);
//            if(log.isDebugEnabled()) log.debug("Json returned: " + response);
            return response;
        } catch (HttpClientErrorException exception){
            log.warn("Twitter request failed: " + exception.getResponseBodyAsString());
            throw new IllegalStateException("Twitter API not accepting request:"+exception.getMessage());
        }
    }


    protected String findTweetsWithHashTag(final String page){
        try{
            final String response = restTemplate.getForObject(TWITTER_URL+page, String.class);
            if(response==null || response.equals("")){
                throw new IllegalStateException("Twitter API did not respond properly");
            }
//            if(log.isDebugEnabled()) log.debug("Json returned: " + response);
            return response;
        } catch (HttpClientErrorException exception){
            log.warn("Twitter request failed: " + exception.getResponseBodyAsString());
            throw new IllegalStateException("Twitter API not accepting request:"+exception.getMessage());
        }
    }


}