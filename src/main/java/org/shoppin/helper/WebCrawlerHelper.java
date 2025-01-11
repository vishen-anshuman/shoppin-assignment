package org.shoppin.helper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class WebCrawlerHelper {

    private final Set<String> visitedUrls = new HashSet<>();
    private final Set<String> productUrls = new HashSet<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);


    // These are some of the common patterns
    // reference -https://dev.to/jacobandrewsky/understanding-url-resolving-in-e-commerce-4mg7#
    private static final Pattern[] PRODUCT_PATTERNS = {
            Pattern.compile("^/[pc]/[a-zA-Z0-9-]+$"), // /[p/c]/${product}
            Pattern.compile("^/[a-zA-Z0-9-]+$"), // /${product}
            Pattern.compile("^/p-[a-zA-Z0-9-]+$"), // /p-${product}
            Pattern.compile("^/[a-zA-Z0-9-]+-p$"), // /${product}-p
            Pattern.compile("^/[a-zA-Z0-9-]+-p-[a-zA-Z0-9-]+$"), // /any-text-p-${product}
            Pattern.compile("^/[a-zA-Z0-9-]+-p[a-zA-Z0-9-]+$"), // /any-text-p${product}
            Pattern.compile("^/category/product/[a-zA-Z0-9-]+$"), // /category/product/${product}
            Pattern.compile("^/category/[a-zA-Z0-9-]+/product/[a-zA-Z0-9-]+$"), // /category/subcategory/product/${product}
            Pattern.compile("^/(product|item|p)/") ///product/, /item/, /p/
    };

    public Set<String> crawlSearch(String url) {

        try {
            crawl(url);
            shutdownExecutor();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return productUrls;
    }

    private void crawl(String url) throws IOException {
        if (visitedUrls.contains(url)) {
            return;
        }
        visitedUrls.add(url);
        Document doc;
        try {
            doc = Jsoup.connect(url)
                    .timeout(100000)
                    .get();
        }catch (Exception e) {
            return;
        }
        System.out.println("Visiting: " + url);
        List<CompletableFuture<Void>> futures = doc.select("a[href]").stream()
                .map(link -> {
                    String linkHref = link.attr("href");
                    if (isProductUrl(linkHref)) {
                        synchronized (productUrls) {
                            productUrls.add(linkHref);
                        }
                    } else if (linkHref.startsWith("http") && !visitedUrls.contains(linkHref)) {
                        return CompletableFuture.runAsync(() -> {
                            try {
                                crawl(linkHref);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }, executorService);
                    }

                    return null;
                })
                .filter(Objects::nonNull)
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    public static boolean isProductUrl(String urlPath) {
        for (Pattern pattern : PRODUCT_PATTERNS) {
            if (pattern.matcher(urlPath).matches()) {
                return true;
            }
        }
        return false;
    }

    private void shutdownExecutor() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }
}


