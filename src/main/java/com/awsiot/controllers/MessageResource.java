package com.awsiot.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.awsiot.entities.Message;
import com.awsiot.services.MessageService;

@RestController
@RequestMapping("/api")
public class MessageResource {
	
	@Autowired
	private MessageService service;
	
	@GetMapping(path = "/service")
	public ResponseEntity<?> serviceStatus() {
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@PostMapping(path = "/sensor-data", consumes = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> storeMessage(@RequestBody Message message) {
		try {
			service.saveMessage(message);
			return new ResponseEntity<>(HttpStatus.CREATED);
		}catch (Exception e) {
			System.out.println("exception details: " + e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
