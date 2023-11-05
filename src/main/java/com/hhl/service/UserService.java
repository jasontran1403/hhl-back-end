package com.hhl.service;

import java.util.List;

import com.hhl.dto.InfoResponse;
import com.hhl.user.Commission;
import com.hhl.user.Exness;
import com.hhl.user.Transaction;
import com.hhl.user.User;

public interface UserService {
	InfoResponse getInfoByExnessId(String exnessId, long from, long to);
	InfoResponse getAllInfoByEmail(String email, long from, long to);
	boolean saveProfit(String exnessId, double amount, long time);
	Commission saveCommission(String exnessId, double amount, long time);
	boolean saveBalance(String exnessId, double amount, long time);
	Transaction saveTransaction(String exnessId, double amount, long time);
	Exness updateBalanceExness(String exness, double amount);
	void updateTotalProfit(String exnessId, double amount);
	void updateTotalCommission(User user, double amount);
	List<User> getUsersByBranchName(String branchName);
}
