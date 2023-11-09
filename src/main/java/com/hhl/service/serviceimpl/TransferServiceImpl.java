package com.hhl.service.serviceimpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hhl.service.TransferService;
import com.hhl.user.Transfer;
import com.hhl.user.TransferRepository;

@Service
public class TransferServiceImpl implements TransferService{
	@Autowired
	TransferRepository transRepo;

	@Override
	public List<Transfer> findAllTransferByEmail(String email) {
		// TODO Auto-generated method stub
		return transRepo.findAllTransferByReceiver(email);
	}

}
