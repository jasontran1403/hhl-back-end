package com.hhl.service;

import java.util.List;

import com.hhl.user.Profit;

public interface ProfitService {

	List<Profit> findByAmountAndTimeAndExness(double amount, long time, String exness);
	double getTotalProfitLastMonth(String exnessId);
}
