package com.hhl.service;

import java.util.List;

import com.hhl.user.Transaction;

public interface TransactionService {
	List<Transaction> findTransactionByEmail(String email);
	Transaction saveTransaction(Transaction transaction);
	List<Transaction> findByAmountAndTimeAndExness(double amount, long time, String exness);
	List<Transaction> findByExness(String exness);
}
