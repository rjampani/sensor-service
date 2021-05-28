package com.awsiot.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.awsiot.entities.Message;
import com.awsiot.respositories.MessageRepository;

@Service
public class MessageService {

	@Autowired
	private MessageRepository repository;
	
	public Message saveMessage(Message message) {
		return repository.save(message);
	}
}
