package com.example.httpservice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.sun.management.HotSpotDiagnosticMXBean;
import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.web.bind.annotation.GetMapping;

@RestController
public class MyController {

    private static final Logger LOGGER = LogManager.getLogger(MyController.class);

    private final Random random = new Random();

    private static final List<byte[]> memoryConsumers = new ArrayList<>();

    @GetMapping("/sendRequest")
    public ResponseEntity<String> sendRequest() throws InterruptedException {
        LOGGER.info("sendRequest");
        // int sleepTime = random.nextInt(200);
        // Thread.sleep(sleepTime);

        int maxThreads = Integer.parseInt(System.getenv().getOrDefault("WZH_MAX_THREADS", "1000"));
        int timeoutMins = Integer.parseInt(System.getenv().getOrDefault("WZH_TIMEOUT_MINS", "1"));
        LOGGER.info("Max threads: " + maxThreads);

        ExecutorService executorService = Executors.newFixedThreadPool(maxThreads);
        try {
            for (int i = 0; i < maxThreads; i++) {
                executorService.submit(() -> {
                    try {
                        LOGGER.info(Thread.currentThread().getName());
                        sendHttpRequest();
                    } catch (Exception e) {
                        // handle exception
                        e.printStackTrace();
                    }
                });
            }
            executorService.shutdown();
            if (!executorService.awaitTermination(timeoutMins, TimeUnit.MINUTES)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            executorService.shutdownNow();
        }

        String response = sendHttpRequest();
        return ResponseEntity.ok(response);
    }

    private String sendHttpRequest() {
        RestTemplate restTemplate = new RestTemplate();
        String backendUrl = System.getenv("WZH_URL");
        ResponseEntity<String> response = restTemplate.getForEntity(backendUrl, String.class);
        LOGGER.info("Response status code: " + response.getStatusCode());
        return response.getBody();
    }

    @GetMapping("/dumpheap")
    public ResponseEntity<String> dumpHeapApi() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String dateTime = LocalDateTime.now().format(dtf);
        String dump_dir = System.getenv().getOrDefault("HEAP_DUMP_DIR", "/wzh-log");
        String dump_path = dump_dir + "/heap-dump_" + dateTime + ".hprof";
        dumpHeap(dump_path, true);
        return ResponseEntity.ok("Heap dump created\n");
    }

    private void dumpHeap(String filePath, boolean live) {
        try {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            HotSpotDiagnosticMXBean mxBean = ManagementFactory.newPlatformMXBeanProxy(
                    server, "com.sun.management:type=HotSpotDiagnostic", HotSpotDiagnosticMXBean.class);
            mxBean.dumpHeap(filePath, live);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/crashapi")
    public void crashApi() {
        // throw new RuntimeException("Crashing the app\n");
        // new Thread(() -> {
        //     throw new RuntimeException("Crashing the app\n");
        // }).start();
        System.exit(1);
    }

    @GetMapping("/consume-memory")
    public ResponseEntity<String> consumeMemory(@RequestParam("size") String size) {
        long bytesToAllocate = parseSizeToBytes(size);
        if (bytesToAllocate <= 0) {
            return ResponseEntity.badRequest().body("Invalid size format");
        }
        try {
            byte[] block = new byte[(int) bytesToAllocate];
            memoryConsumers.add(block);
            return ResponseEntity.ok("Allocated " + size + " of memory");
        } catch (OutOfMemoryError e) {
            return ResponseEntity.internalServerError().body("Not enough memory available");
        }
    }

    @GetMapping("/release-memory")
    public ResponseEntity<String> releaseMemory() {
        memoryConsumers.clear();
        System.gc(); // Suggest garbage collection, but it's not guaranteed to run immediately
        return ResponseEntity.ok("Memory released");
    }

    private long parseSizeToBytes(String size) {
        long factor = 1;
        size = size.toUpperCase();
        if (size.endsWith("GB")) {
            factor = 1024 * 1024 * 1024;
        } else if (size.endsWith("MB")) {
            factor = 1024 * 1024;
        } else if (size.endsWith("KB")) {
            factor = 1024;
        }
        try {
            return Long.parseLong(size.replaceAll("[^0-9]", "")) * factor;
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}