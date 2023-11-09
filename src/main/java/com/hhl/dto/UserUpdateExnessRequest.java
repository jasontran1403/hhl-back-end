package com.hhl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateExnessRequest {
	private String email;
	private String exness;
	private String server;
	private String password;
	private String passview;
	private String code;
}
