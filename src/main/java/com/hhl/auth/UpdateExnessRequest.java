package com.hhl.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateExnessRequest {
	private String email;
	private String exness;
	private String server;
	private String password;
	private String passview;
	private int type;
}
