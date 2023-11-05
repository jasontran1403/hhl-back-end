package com.hhl.dto;

import java.util.List;

import com.hhl.user.Balance;
import com.hhl.user.Commission;
import com.hhl.user.Profit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InfoResponse {
	private double profit;
	private double commission;
	private List<Profit> profits;
	private List<Commission> commissions;
	private List<Balance> balances;
}
