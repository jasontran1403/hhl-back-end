package com.hhl.service;

import com.hhl.dto.PrevRequest;
import com.hhl.dto.PreviousMonthResponse;
import com.hhl.user.Prev;

public interface PrevService {
	PreviousMonthResponse findPrevByEmail(String email);
	void updatePrev(PrevRequest request);
	Prev findByExnessId(String exnessid);
	Prev initPrev(String email);
	void updatePrevData(String exnessId, double balance, double profit, double deposit, double withdraw);
}
