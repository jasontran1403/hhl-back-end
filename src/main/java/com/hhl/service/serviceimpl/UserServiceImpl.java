package com.hhl.service.serviceimpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hhl.dto.InfoResponse;
import com.hhl.exception.NotFoundException;
import com.hhl.service.UserService;
import com.hhl.user.Balance;
import com.hhl.user.BalanceRepository;
import com.hhl.user.Commission;
import com.hhl.user.CommissionRepository;
import com.hhl.user.Exness;
import com.hhl.user.ExnessRepository;
import com.hhl.user.Profit;
import com.hhl.user.ProfitRepository;
import com.hhl.user.Transaction;
import com.hhl.user.TransactionRepository;
import com.hhl.user.User;
import com.hhl.user.UserRepository;

@Service
public class UserServiceImpl implements UserService {
	@Autowired
	UserRepository userRepo;

	@Autowired
	ProfitRepository proRepo;

	@Autowired
	CommissionRepository commissRepo;

	@Autowired
	ExnessRepository exRepo;

	@Autowired
	BalanceRepository balanceRepo;

	@Autowired
	TransactionRepository transRepo;

	@Override
	public InfoResponse getInfoByExnessId(String exnessId, long from, long to) {
		Optional<Exness> exness = exRepo.findByExness(exnessId);
		if (exness.isEmpty()) {
			throw new NotFoundException("This ExnessID " + exnessId + " is not existed!");
		}

		List<Profit> profits = new ArrayList<>();
		List<Commission> commissions = new ArrayList<>();

		if (from > 0 && to > 0) {
			profits = proRepo.getCommissionByExnessIdAndTime(exnessId, from, to);
			commissions = commissRepo.getCommissionByExnessIdAndTime(exnessId, from, to);
		} else {
			profits = proRepo.getCommissionByExnessId(exnessId);
			commissions = commissRepo.getCommissionByExnessId(exnessId);
		}
		InfoResponse result = new InfoResponse();
		result.setProfit(exness.get().getBalance());
		result.setCommission(exness.get().getUser().getCash());
		result.setProfits(profits);
		result.setRank(exness.get().getLevel());
		result.setCommissions(commissions);
		List<Balance> balances = balanceRepo.findByExnessByTime(exness.get().getExness(), from, to);
		result.setBalances(balances);

		return result;
	}

	@Override
	public boolean saveProfit(String exnessId, double amount, long time) {
		// TODO Auto-generated method stub
		Profit profit = new Profit();
		profit.setExnessId(exnessId);
		profit.setAmount(amount);
		profit.setTime(time);
		
		List<Profit> profits = proRepo.findByTimeAndExness(time, exnessId);
		if (profits.size() != 0) {
			return false;
		} else {
			proRepo.save(profit);
			return true;
		}
		
	}

	@Override
	public Commission saveCommission(String exnessId, double amount, long time) {
		// TODO Auto-generated method stub
		Commission commission = new Commission();
		commission.setExnessId(exnessId);
		commission.setAmount(amount);
		commission.setTime(time);
		return commissRepo.save(commission);
	}

	@Override
	public boolean saveBalance(String exnessId, double amount, long time) {
		// TODO Auto-generated method stub
		Balance balance = new Balance();
		Exness exness = exRepo.findByExness(exnessId).get();
		balance.setExnessId(exness.getExness());
		balance.setAmount(amount);
		balance.setTime(time);
		
		List<Balance> balances = balanceRepo.findByTimeAndExness(time, exnessId);
		if (balances.size() != 0) {
			return false;
		} else {
			balanceRepo.save(balance);
			return true;
		}
		
	}

	@Override
	public Transaction saveTransaction(String exnessId, double amount, long time) {
		// TODO Auto-generated method stub
		String type = "";
		if (amount > 0) {
			type = "Deposit";
		} else {
			type = "Withdraw";
		}
		Transaction transaction = new Transaction();
		transaction.setExnessId(exnessId);
		transaction.setAmount(amount);
		transaction.setType(type);
		transaction.setTime(time);
		return transRepo.save(transaction);
	}

	@Override
	public InfoResponse getAllInfoByEmail(String email, long from, long to) {
		// TODO Auto-generated method stub
		Optional<User> user = userRepo.findByEmail(email);
		if (user.isEmpty()) {
			throw new NotFoundException("This user with email " + email + " is not existed!");
		}

		List<Profit> profits = new ArrayList<>();
		List<Commission> commissions = new ArrayList<>();
		List<Balance> balances = new ArrayList<>();
		double balance = 0.0;

		if (from > 0 && to > 0) {
			for (Exness exness : user.get().getExnessList()) {
				balance += exness.getBalance();
				List<Profit> profitsFromCriteria = proRepo.getCommissionByExnessIdAndTime(exness.getExness(), from, to);
				if (profitsFromCriteria.size() > 0) {
					for (Profit profit : profitsFromCriteria) {
						profits.add(profit);
					}
				}

				List<Commission> commissionsFromCriteria = commissRepo
						.getCommissionByExnessIdAndTime(exness.getExness(), from, to);
				if (commissionsFromCriteria.size() > 0) {
					for (Commission commission : commissionsFromCriteria) {
						commissions.add(commission);
					}
				}
				
				List<Balance> balanceFromCriteria = balanceRepo.findByExnessByTime(exness.getExness(), from, to);
				if (balanceFromCriteria.size() > 0) {
					for (Balance balanceItem : balanceFromCriteria) {
						balances.add(balanceItem);
					}
				}
				
			}
		}
		
		InfoResponse result = new InfoResponse();
		result.setProfit(balance);
		result.setCommission(user.get().getCash());
		result.setProfits(profits);
		result.setCommissions(commissions);
		result.setBalances(balances);
		result.setRank(-1);
		
		
		// TODO Auto-generated method stub
		return result;
	}

	@Override
	public Exness updateBalanceExness(String exness, double amount) {
		// TODO Auto-generated method stub
		Exness item = exRepo.findByExness(exness).get();
		item.setBalance(amount);
		return exRepo.save(item);
	}

	@Override
	public void updateTotalProfit(String exnessId, double amount) {
		// TODO Auto-generated method stub
		Exness exness = exRepo.findByExness(exnessId).get();
		User user = exness.getUser();
		user.setPrev(user.getPrev() + amount);
		userRepo.save(user);
	}

	@Override
	public void updateTotalCommission(User user, double amount) {
		// TODO Auto-generated method stub
		user.setCommission(user.getCommission() + amount);
		userRepo.save(user);
	}

	@Override
	public List<User> getUsersByBranchName(String branchName) {
		// TODO Auto-generated method stub
		return userRepo.getUsersByBranchName(branchName);
	}
}



