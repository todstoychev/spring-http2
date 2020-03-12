package com.example.http2.controller;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/async")
public class AsyncTestController {
    @GetMapping("/http1")
    public Long http1() throws IOException, InterruptedException {
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

        List<CompletableFuture<HttpResponse<String>>> futures = new ArrayList<>();
        AtomicInteger atomicInt = new AtomicInteger(1);

        Document doc = Jsoup.parse(responseBody);
        Elements imgs = doc.select("img[width=32]"); // img with width=32

        // Send request on a separate thread for each image in the page,
        imgs.forEach(img -> {
            var image = img.attr("src");
            HttpRequest imgRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://http1.golang.org" + image))
                    .build();
            CompletableFuture<HttpResponse<String>> imgResponse = httpClient.sendAsync(imgRequest, HttpResponse.BodyHandlers.ofString());

            futures.add(imgResponse);
            System.out.println("Adding future for image " + image);
        });

        futures.forEach(f -> {
            try {
                HttpResponse<String> response = f.get();
                System.out.println("Status code: " + response.statusCode() + ", protocol: " + response.version());
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
    public Long http2() throws IOException, InterruptedException {
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

        List<CompletableFuture<HttpResponse<String>>> futures = new ArrayList<>();
        AtomicInteger atomicInt = new AtomicInteger(1);

        Document doc = Jsoup.parse(responseBody);
        Elements imgs = doc.select("img[width=32]"); // img with width=32

        // Send request on a separate thread for each image in the page,
        imgs.forEach(img -> {
            var image = img.attr("src");
            HttpRequest imgRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://http2.golang.org" + image))
                    .build();
            CompletableFuture<HttpResponse<String>> imgResponse = httpClient.sendAsync(imgRequest, HttpResponse.BodyHandlers.ofString());

            futures.add(imgResponse);
            System.out.println("Adding future for image " + image);
        });

        futures.forEach(f -> {
            try {
                HttpResponse<String> response = f.get();
                System.out.println("Status code: " + response.statusCode() + ", protocol: " + response.version());
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
