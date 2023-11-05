package com.hhl.service;

import com.hhl.user.Commission;

public interface CommissionService {
	Commission saveCommission(Commission commission); 
	double getTotalCommission();
}
