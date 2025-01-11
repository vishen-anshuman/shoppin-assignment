package org.shoppin.controller;

import org.shoppin.service.CrawlerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping(value = "/crawler", produces = "application/json")
public class Crawler {
    private final CrawlerService crawlerService;

    public Crawler(CrawlerService crawlerService) {
        this.crawlerService = crawlerService;
    }

    /**
     * urls will be a string of comma-separated values of domains
     */
    @GetMapping()
    public Map<String, Set<String>> crawl(@RequestParam(required = false) String urls) {
        return crawlerService.extractProductUrls(urls);
    }
}
