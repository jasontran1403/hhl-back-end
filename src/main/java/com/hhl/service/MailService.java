package com.hhl.service;

import com.hhl.dto.EmailDto;

import jakarta.mail.MessagingException;

public interface MailService {
	void send(EmailDto mail) throws MessagingException;
}
