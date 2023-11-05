package com.hhl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreviousMonthResponse {
	private double balance;
	private double profit;
	private double commission;
	private double deposit;
	private double withdraw;
}
