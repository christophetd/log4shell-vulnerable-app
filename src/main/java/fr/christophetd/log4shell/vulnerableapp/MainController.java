package fr.christophetd.log4shell.vulnerableapp;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@RestController
public class MainController {

  private static final org.apache.log4j.Logger logger1 = org.apache.log4j.Logger.getLogger("HelloWorld1");
  private static final Logger logger2 = LogManager.getLogger("HelloWorld2");

    @Value("${log4j.version:2}")
    private String log4j_version;

    @GetMapping("/")
    public String index(@RequestHeader("X-Api-Version") String apiVersion) {
      if ("1".equals(log4j_version)) {
        logger1.info("Hello from log4j v1 " + apiVersion);
      } else {
        logger2.info("Received a request for API version using log4j v2 " + apiVersion);
      }
      return "Hello, world!";
    }

}
