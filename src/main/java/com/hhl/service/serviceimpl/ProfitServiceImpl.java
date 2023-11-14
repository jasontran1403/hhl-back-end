package com.hhl.service.serviceimpl;

import java.time.YearMonth;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hhl.service.ProfitService;
import com.hhl.user.Exness;
import com.hhl.user.ExnessRepository;
import com.hhl.user.Profit;
import com.hhl.user.ProfitRepository;
import com.hhl.user.User;
import com.hhl.user.UserRepository;

@Service
public class ProfitServiceImpl implements ProfitService {
	@Autowired
	ProfitRepository proRepo;

	@Autowired
	UserRepository repository;

	@Autowired
	ExnessRepository exRepo;

	@Override
	public List<Profit> findByAmountAndTimeAndExness(double amount, long time, String exness) {
		// TODO Auto-generated method stub
		return proRepo.findByAmountAndTimeAndExness(amount, time, exness);
	}

	@Override
	public double getTotalProfitLastMonth(String exnessId) {
		// TODO Auto-generated method stub
		double totalProfits = 0.0;

		Date currentDateTime = new Date();
		TimeZone timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
		Calendar calendar = Calendar.getInstance(timeZone);
		calendar.setTime(currentDateTime);

		// Đặt thời gian thành 00:00:01
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.set(Calendar.MONTH, currentDateTime.getMonth());
		calendar.set(Calendar.HOUR_OF_DAY, 7);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		long timestampTo = calendar.getTimeInMillis() / 1000 - 86400;

		YearMonth yearMonthObject = YearMonth.of(currentDateTime.getYear(), currentDateTime.getMonth() - 1);
		int daysInMonth = yearMonthObject.lengthOfMonth(); // 28

		long timestampFrom = calendar.getTimeInMillis() / 1000 - daysInMonth * 86400 + 86400;

		Exness exness = exRepo.findByExness(exnessId).get();
		List<Profit> profits = proRepo.findByTimeAndExness(timestampFrom, timestampTo, exness.getExness());
		for (Profit profit : profits) {
			totalProfits += profit.getAmount();
		}
		return totalProfits;
	}

}
