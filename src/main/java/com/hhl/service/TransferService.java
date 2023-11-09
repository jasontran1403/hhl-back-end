package com.hhl.service;

import java.util.List;

import com.hhl.user.Transfer;

public interface TransferService {
	List<Transfer> findAllTransferByEmail(String email);
}
