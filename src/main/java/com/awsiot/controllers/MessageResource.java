package com.awsiot.controllers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import com.awsiot.entities.Message;
import com.awsiot.services.MessageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import static com.awsiot.util.ServiceUtil.*;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api")
public class MessageResource {

	@Autowired
	private MessageService service;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private RestTemplate restTemplate;

	private Map<String, String> tokenMap;

	@GetMapping(path = SLASH + SERVICE)
	public ResponseEntity<?> serviceStatus() {
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PostMapping(path = SLASH + SENSOR_DATA, consumes = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<?> storeMessage(@RequestBody Message message) {
		log.info(MESSAGE + COLON_SPACE + message);
		if (message == null || message.getDeviceid() == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		try {
			service.saveMessage(message);
			return new ResponseEntity<>(HttpStatus.CREATED);
		} catch (Exception e) {
			log.error(EXCEPTION_DETAILS + e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping
	public ResponseEntity<?> confirmationToken(@Param(CONFIRMATION_TOKEN) String confirmationToken,
			@RequestHeader HttpHeaders httpHeaders, @RequestBody String httpRequestBody) {
		log.info("-----------------------------------------");
		log.info(CONFIRMATION_TOKEN + COLON_SPACE + confirmationToken);
		log.info("-----------------------------------------");
		httpHeaders.forEach((header, list) -> {
			log.info(header + COLON_SPACE + list);
		});
		log.info("-------------------------------------------");
		log.info(httpRequestBody);
		if (tokenMap == null) {
			tokenMap = new ConcurrentHashMap<String, String>();
		}
		tokenMap.put(CONFIRMATION_TOKEN, confirmationToken);
		try {
			String enableUrl = objectMapper.readTree(httpRequestBody).get(ENABLE_URL).asText();
			ResponseEntity<?> response = restTemplate.getForEntity(enableUrl, String.class);
			if(response.getStatusCode().is2xxSuccessful()) {
				log.info(STATUS_CODE + COLON_SPACE + response.getStatusCode());
				tokenMap.put(ENABLE_URL_CALL_STATUS, SUCCESS);
			} else {
				log.error(STATUS_CODE + COLON_SPACE + response.getStatusCode());
				tokenMap.put(ENABLE_URL_CALL_STATUS, FAILED);
			}
			//WebClient.create(enableUrl).get().retrieve().onStatus(HttpStatus.OK.ordinal(), exceptionFunction -> {
			//});
			log.info(ENABLE_URL + COLON_SPACE + enableUrl);
		} catch (JsonProcessingException e) {
			log.error(EXCEPTION_DETAILS + e);
			e.printStackTrace();
		}
		
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@GetMapping(path = SLASH + CONFIRMATION_TOKEN)
	public ResponseEntity<?> getConfirmationToken() {
		if (tokenMap != null && tokenMap.containsKey(CONFIRMATION_TOKEN)) {
			return new ResponseEntity<>(tokenMap.get(CONFIRMATION_TOKEN), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(NO_CONFIRMATION_TOKEN, HttpStatus.NO_CONTENT);
		}
	}
	
	@GetMapping(path = SLASH + ENABLE_URL_CALL_STATUS)
	public ResponseEntity<?> getEnableUrlCallStatus() {
		if(tokenMap != null && tokenMap.containsKey(ENABLE_URL_CALL_STATUS)) {
			return new ResponseEntity<>(tokenMap.get(ENABLE_URL_CALL_STATUS), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(tokenMap.get(ENABLE_URL_CALL_STATUS), HttpStatus.NO_CONTENT);
		}
	}
	
}
