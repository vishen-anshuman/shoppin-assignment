package org.shoppin.service;

import org.shoppin.helper.WebCrawlerHelper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CrawlerService {
    public Map<String, Set<String>> extractProductUrls(String urls) {
        Set<String> urlSet = new HashSet<>(List.of(urls.split(",")));
        Map<String, Set<String>> map = new HashMap<>();
        for (String url : urlSet) {
            if (url.startsWith("http://") || url.startsWith("https://")) {
                WebCrawlerHelper helper = new WebCrawlerHelper();
                map.put(url, helper.crawlSearch(url));
            }
        }
        return map;
    }
}
