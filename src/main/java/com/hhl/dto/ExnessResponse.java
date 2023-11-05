package com.hhl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExnessResponse {
	private String exnessId;
	private String server;
	private String password;
	private String passview;
	private boolean status;
	private String message;
}
