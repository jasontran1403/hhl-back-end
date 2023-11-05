package com.hhl.service;

import java.util.List;

import com.hhl.user.Balance;

public interface BalanceService {
	List<Balance> findByAmountAndTimeAndExness(double amount, long time, String exness);
}
