package com.example.http2.controller;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.client.http.HttpClientTransportOverHTTP2;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class JettyTestController {

    private static final ExecutorService executor = Executors.newFixedThreadPool(10, Thread::new);

    @GetMapping("/jetty/http1")
    public long testHttp1() throws Exception {
        long start = System.currentTimeMillis();
        HttpClient client = new HttpClient(new SslContextFactory.Client());
        client.start();

        ContentResponse pageResponse = client.GET("https://http1.golang.org/gophertiles");

        System.out.println("Page response status code: " + pageResponse.getStatus());
        System.out.println("Page response headers: " + pageResponse.getHeaders());
        String responseBody = new String(pageResponse.getContent());
        System.out.println(responseBody);

        List<Future<?>> futures = new ArrayList<>();
        AtomicInteger atomicInt = new AtomicInteger(1);

        Document doc = Jsoup.parse(responseBody);
        Elements imgs = doc.select("img[width=32]"); // img with widt

        // Send request on a separate thread for each image in the page,
        imgs.forEach(img -> {
            var image = img.attr("src");
            Future<?> imgFuture = executor.submit(() -> {
                try {
                    ContentResponse imageResponse = client.GET("https://http1.golang.org" + image);
                    System.out.println("[" + atomicInt.getAndIncrement() + "] Loaded " + image + ", status code: " + imageResponse.getStatus() + ", protocol: " + imageResponse.getVersion());
                } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                    System.out.println("Error loading image " + image + ": " + ex.getMessage());
                }
            });

            futures.add(imgFuture);
            System.out.println("Adding future for image " + image);
        });

        // Wait for image loads to be completed
        futures.forEach(f -> {
            try {
                f.get();
            } catch (InterruptedException | ExecutionException ex) {
                System.out.println("Exception during loading images: " + ex.getMessage());
            }

        });

        long end = System.currentTimeMillis();
        System.out.println("Total load time: " + (end - start) + " ms");
        System.out.println(atomicInt.get() - 1 + " images loaded");

        return end - start;
    }

    @GetMapping("/jetty/http2")
    public long testHttp2() throws Exception {
        long start = System.currentTimeMillis();

        HTTP2Client h2Client = new HTTP2Client();
        HttpClientTransportOverHTTP2 transport = new HttpClientTransportOverHTTP2(h2Client);

        HttpClient client = new HttpClient(transport, new SslContextFactory.Client());
        client.start();

        ContentResponse pageResponse = client.GET("https://http2.golang.org/gophertiles");

        System.out.println("Page response status code: " + pageResponse.getStatus());
        System.out.println("Page response headers: " + pageResponse.getHeaders());
        String responseBody = new String(pageResponse.getContent());
        System.out.println(responseBody);

        List<Future<?>> futures = new ArrayList<>();
        AtomicInteger atomicInt = new AtomicInteger(1);

        Document doc = Jsoup.parse(responseBody);
        Elements imgs = doc.select("img[width=32]"); // img with widt

        // Send request on a separate thread for each image in the page,
        imgs.forEach(img -> {
            var image = img.attr("src");
            Future<?> imgFuture = executor.submit(() -> {
                try {
                    ContentResponse imageResponse = client.GET("https://http2.golang.org" + image);
                    System.out.println("[" + atomicInt.getAndIncrement() + "] Loaded " + image + ", status code: " + imageResponse.getStatus() + ", protocol: " + imageResponse.getVersion());
                } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                    System.out.println("Error loading image " + image + ": " + ex.getMessage());
                }
            });

            futures.add(imgFuture);
            System.out.println("Adding future for image " + image);
        });

        // Wait for image loads to be completed
        futures.forEach(f -> {
            try {
                f.get();
            } catch (InterruptedException | ExecutionException ex) {
                System.out.println("Exception during loading images: " + ex.getMessage());
            }

        });

        long end = System.currentTimeMillis();
        System.out.println("Total load time: " + (end - start) + " ms");
        System.out.println(atomicInt.get() - 1 + " images loaded");

        return end - start;
    }
}
