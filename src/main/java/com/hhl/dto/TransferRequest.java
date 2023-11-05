package com.hhl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferRequest {
	private String exnessId;
	private double balance;
	private double profit;
	private double withdraw;
	private double deposit;
}
