package com.hhl.service.serviceimpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hhl.service.BalanceService;
import com.hhl.user.Balance;
import com.hhl.user.BalanceRepository;

@Service
public class BalanceServiceImpl implements BalanceService{
	@Autowired
	BalanceRepository balanceRepo;

	@Override
	public List<Balance> findByAmountAndTimeAndExness(double amount, long time, String exness) {
		// TODO Auto-generated method stub
		return balanceRepo.findByAmountAndTimeAndExness(amount, time, exness);
	}

}
