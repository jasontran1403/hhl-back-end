package com.hhl.service;

import java.util.List;
import java.util.Optional;

import com.hhl.dto.PreviousMonthResponse;
import com.hhl.user.Exness;
import com.hhl.user.User;

public interface ExnessService {
	User findUserByExness(String exnessId);
	Optional<Exness> findByExnessId(String exnessId);
	PreviousMonthResponse findByEmail(String email);
	PreviousMonthResponse findByExness(String exness);
	void updateTotalProfit(String exnessId, double amount);
	double getBalanceByEmail(String email);
	List<Exness> getAllExnessByBranch();
	List<Exness> getByUser(User user);
	void activeExness(String exnessId);
	void lockAll();
	void setRank(String exnessId, String level, String time);
	void setMessage(String exnessId, String error);
	void transferCash(String exnessId, double amount);
}
