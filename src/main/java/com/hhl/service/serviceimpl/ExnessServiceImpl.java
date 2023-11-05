package com.hhl.service.serviceimpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.hhl.dto.PreviousMonthResponse;
import com.hhl.service.ExnessService;
import com.hhl.user.Exness;
import com.hhl.user.ExnessRepository;
import com.hhl.user.Transaction;
import com.hhl.user.TransactionRepository;
import com.hhl.user.User;
import com.hhl.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExnessServiceImpl implements ExnessService {
	private final ExnessRepository exRepo;
	private final UserRepository userRepo;
	private final TransactionRepository tranRepo;

	@Override
	public User findUserByExness(String exnessId) {
		// TODO Auto-generated method stub
		return exRepo.findByExness(exnessId).get().getUser();
	}

	@Override
	public PreviousMonthResponse findByEmail(String email) {
		// TODO Auto-generated method stub
		User user = userRepo.findByEmail(email).get();
		PreviousMonthResponse result = new PreviousMonthResponse();
		double withdrawAmounts = 0.0, depositAmounts = 0.0, profit = 0.0;
		
		for (Exness exness : user.getExnessList()) {
			result.setBalance(result.getBalance() + exness.getPrevBalance());
			profit += exness.getTotalProfit();
			List<Transaction> transactions = tranRepo.findTransactionByExnessId(exness.getExness());
			for (Transaction tran : transactions) {
				if (tran.getType().equals("Deposit")) {
					depositAmounts += tran.getAmount();
				} else if (tran.getType().equals("Withdraw")) {
					withdrawAmounts += tran.getAmount();
				}
			}
		}
		result.setProfit(profit);
		result.setDeposit(depositAmounts);
		result.setWithdraw(withdrawAmounts);
		return result;
	}

	@Override
	public PreviousMonthResponse findByExness(String exness) {
		// TODO Auto-generated method stub
		PreviousMonthResponse result = new PreviousMonthResponse();
		Exness item = exRepo.findByExness(exness).get();
		double withdrawAmounts = 0.0, depositAmounts = 0.0, profit = 0.0;
		profit += item.getTotalProfit();
		List<Transaction> transactions = tranRepo.findTransactionByExnessId(item.getExness());
		for (Transaction tran : transactions) {
			if (tran.getType().equals("Deposit")) {
				depositAmounts += tran.getAmount();
			} else if (tran.getType().equals("Withdraw")) {
				withdrawAmounts += tran.getAmount();
			}
		}
		result.setProfit(profit);
		result.setDeposit(depositAmounts);
		result.setWithdraw(withdrawAmounts);
		result.setBalance(result.getBalance() + item.getPrevBalance());
		return result;
	}

	@Override
	public void updateTotalProfit(String exnessId, double amount) {
		// TODO Auto-generated method stub
		Exness exness = exRepo.findByExness(exnessId).get();
		exness.setTotalProfit(exness.getTotalProfit() + amount);
		exRepo.save(exness);
	}

	@Override
	public Optional<Exness> findByExnessId(String exnessId) {
		// TODO Auto-generated method stub
		Optional<Exness> exness = exRepo.findByExness(exnessId);
		return exness;
	}

	@Override
	public double getBalanceByEmail(String email) {
		// TODO Auto-generated method stub
		User user = userRepo.getByEmail(email);
		List<Exness> exnesses = exRepo.findByUser(user);
		double balance = 0;
		for (Exness item : exnesses) {
			balance += item.getBalance();
		}
		return balance;
	}

	@Override
	public List<Exness> getAllExnessByBranch() {
		// TODO Auto-generated method stub
		List<User> users = userRepo.findAll();
		List<Exness> results = new ArrayList<>();
		for (User user : users) {
			if (user.getBranchName().equals("HHL")) {
				List<Exness> exnesses = exRepo.findByUser(user);
				for (Exness exness : exnesses) {
					results.add(exness);
				}
			}
		}
		return results;
	}

	@Override
	public List<Exness> getByUser(User user) {
		// TODO Auto-generated method stub
		return exRepo.findByUser(user);
	}

	@Override
	public void activeExness(String exnessId) {
		// TODO Auto-generated method stub
		Exness exness = exRepo.findByExness(exnessId).get();
		exness.setActive(true);
		exness.setMessage("");
		exRepo.save(exness);
	}

	@Override
	public void lockAll() {
		// TODO Auto-generated method stub
		List<User> users = userRepo.findAll();
		for (User user : users) {
			if (user.getBranchName().equals("HHL")) {
				List<Exness> exnesses = exRepo.findByUser(user);
				for (Exness exness : exnesses) {
					exness.setActive(false);
					exRepo.save(exness);
				}
			}
		}
	}

}
