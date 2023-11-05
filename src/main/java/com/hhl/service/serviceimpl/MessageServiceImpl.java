package com.hhl.service.serviceimpl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.hhl.dto.MessageRequest;
import com.hhl.exception.NotFoundException;
import com.hhl.service.MessageService;
import com.hhl.user.Message;
import com.hhl.user.MessageRepository;
import com.hhl.user.User;
import com.hhl.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService{
	private final UserRepository userRepo;
	private final MessageRepository messRepo;

	@Override
	public Message saveMessage(MessageRequest messageRequest) {
		// TODO Auto-generated method stub
		Optional<User> user = userRepo.findByEmail(messageRequest.getEmail());
		if (user.isEmpty()) {
			throw new NotFoundException("This user " + messageRequest.getEmail() + " is not existed!");
		}
		Message message = new Message();
		message.setTime(System.currentTimeMillis() / 1000);
		message.setMessage(messageRequest.getMessage());
		message.setRead(false);
		message.setUser(user.get());
		return messRepo.save(message);
	}

	@Override
	public List<Message> findMessagesByEmail(String email) {
		// TODO Auto-generated method stub
		User user = userRepo.findByEmail(email).get();
		return messRepo.findMessagesByUser(user.getId());
	}

	@Override
	public void toggleMessageStatus(long id) {
		// TODO Auto-generated method stub
		Message message = messRepo.findById(id).get();
		message.setRead(true);
		messRepo.save(message);
	}

}
