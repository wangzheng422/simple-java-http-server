package com.example.httpservice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

@RestController
public class MyController {

  private static final Logger LOGGER = LogManager.getLogger(MyController.class);

  private final Random random = new Random();

  @GetMapping("/sendRequest")
  public ResponseEntity<String> sendRequest() throws InterruptedException {
    LOGGER.info("sendRequest");
    int sleepTime = random.nextInt(200);
    Thread.sleep(sleepTime);
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

}