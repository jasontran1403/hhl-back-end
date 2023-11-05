package com.hhl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PrevRequest {
	private String email;
	private double balance;
	private double commission;
	private double deposit;
	private double withdraw;
}
