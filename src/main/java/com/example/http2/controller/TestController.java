package com.example.http2.controller;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class TestController {

    private static final ExecutorService executor = Executors.newFixedThreadPool(10, Thread::new);

    @GetMapping("/http1")
    public long testHttp1() throws IOException, InterruptedException {

        System.out.println("Running HTTP/1.1 example...");

        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        long start = System.currentTimeMillis();

        HttpRequest pageRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://http1.golang.org/gophertiles"))
                .build();

        HttpResponse<String> pageResponse = httpClient.send(pageRequest, HttpResponse.BodyHandlers.ofString());

        System.out.println("Page response status code: " + pageResponse.statusCode());
        System.out.println("Page response headers: " + pageResponse.headers());
        String responseBody = pageResponse.body();
        System.out.println(responseBody);

        List<Future<?>> futures = new ArrayList<>();
        AtomicInteger atomicInt = new AtomicInteger(1);

        Document doc = Jsoup.parse(responseBody);
        Elements imgs = doc.select("img[width=32]"); // img with width=32

        // Send request on a separate thread for each image in the page,
        imgs.forEach(img -> {
            var image = img.attr("src");
            Future<?> imgFuture = executor.submit(() -> {
                HttpRequest imgRequest = HttpRequest.newBuilder()
                        .uri(URI.create("https://http1.golang.org" + image))
                        .build();
                try {
                    HttpResponse<String> imageResponse = httpClient.send(imgRequest, HttpResponse.BodyHandlers.ofString());
                    System.out.println("[" + atomicInt.getAndIncrement() + "] Loaded " + image + ", status code: " + imageResponse.statusCode() + ", protocol: " + imageResponse.version());
                } catch (IOException | InterruptedException ex) {
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

    @GetMapping("/http2")
    public long testHttp2() throws IOException, InterruptedException {

        System.out.println("Running HTTP/2 example...");

        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();

        long start = System.currentTimeMillis();

        HttpRequest pageRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://http2.golang.org/gophertiles"))
                .build();

        HttpResponse<String> pageResponse = httpClient.send(pageRequest, HttpResponse.BodyHandlers.ofString());

        System.out.println("Page response status code: " + pageResponse.statusCode());
        System.out.println("Page response headers: " + pageResponse.headers());
        String responseBody = pageResponse.body();
        System.out.println(responseBody);

        List<Future<?>> futures = new ArrayList<>();
        AtomicInteger atomicInt = new AtomicInteger(1);

        Document doc = Jsoup.parse(responseBody);
        Elements imgs = doc.select("img[width=32]"); // img with width=32

        // Send request on a separate thread for each image in the page,
        imgs.forEach(img -> {
            var image = img.attr("src");
            Future<?> imgFuture = executor.submit(() -> {
                HttpRequest imgRequest = HttpRequest.newBuilder()
                        .uri(URI.create("https://http2.golang.org" + image))
                        .build();
                try {
                    HttpResponse<String> imageResponse = httpClient.send(imgRequest, HttpResponse.BodyHandlers.ofString());
                    System.out.println("[" + atomicInt.getAndIncrement() + "] Loaded " + image + ", status code: " + imageResponse.statusCode() + ", protocol: " + imageResponse.version());
                } catch (IOException | InterruptedException ex) {
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
