package com.flurdy.twitter;

import java.io.IOException;
import java.util.Set;

public interface ITwitterSearch {
    Set<String> searchForUrls() throws IOException;
}
