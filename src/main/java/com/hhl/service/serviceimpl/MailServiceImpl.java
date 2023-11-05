package com.hhl.service.serviceimpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.hhl.dto.EmailDto;
import com.hhl.service.MailService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class MailServiceImpl implements MailService{
	@Autowired
    JavaMailSender sender;

    @Override
    public void send(EmailDto mail) throws MessagingException {
        // TODO Auto-generated method stub
        // tạo message
        MimeMessage message = sender.createMimeMessage();

        // dùng helper thiết tập các thông tin
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");
        helper.setFrom(mail.getFrom());
        helper.setTo(mail.getTo());
        helper.setSubject(mail.getSubject());
        helper.setText(mail.getBody(), true);
        helper.setReplyTo(mail.getFrom());

        String[] cc = mail.getCc();
        if (cc != null && cc.length > 0) {
            helper.setCc(cc);
        }
        String[] bcc = mail.getBcc();
        if (bcc != null && bcc.length > 0) {
            helper.setBcc(bcc);
        }

        // Gửi message đến SMTP server
        sender.send(message);

    }
}
