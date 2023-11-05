package com.hhl.service;

import java.util.List;

import com.hhl.dto.MessageRequest;
import com.hhl.user.Message;

public interface MessageService {
	Message saveMessage(MessageRequest message);
	List<Message> findMessagesByEmail(String email);
	void toggleMessageStatus(long id);
}
