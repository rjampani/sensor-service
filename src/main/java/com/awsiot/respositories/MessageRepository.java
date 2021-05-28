package com.awsiot.respositories;


import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.awsiot.entities.Message;

@Repository
public interface MessageRepository extends CrudRepository<Message, Integer> {
}
