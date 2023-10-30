package com.company.pnrservices.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/test-connect")
    @CrossOrigin
    public ResponseEntity<Object> connect() {
        System.out.println("!!!connected");
        return ResponseEntity.ok("Connected");
    }

}
