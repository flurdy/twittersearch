package com.flurdy.twitter;

import java.io.IOException;
import java.util.Set;

public class TwitterSearch {
    
    public static void main(String args[]){
        if(args.length==1 && !"".equals(args[0].trim()) ){
            final HashTagSearch hashTagSearch = new HashTagSearch(args[0],100);
            Set<String> urlTweets = null;
            try {
                urlTweets = hashTagSearch.searchForUrls();
            } catch (Exception exception) {
                System.err.println("Unfortunately the application threw an error: "+exception.getMessage());
            }
            int i = 1;
            for(String url : urlTweets){
                System.out.println(i + ": " +url);
                i++;
            }
        } else {
            throw new IllegalArgumentException("1 and only 1 proper hashtag argument expected");
        }
    }

}
