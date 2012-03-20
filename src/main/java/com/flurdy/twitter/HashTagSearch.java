package com.flurdy.twitter;

import java.util.Set;

public class HashTagSearch implements ITwitterSearch{

    private String hashTag;
    private int returnSize;

    public HashTagSearch(String hashTag,int returnSize) {
        this.hashTag = hashTag;
        this.returnSize = returnSize;
    }

    public Set<String> searchForUrls() {


        return null;
    }

}
