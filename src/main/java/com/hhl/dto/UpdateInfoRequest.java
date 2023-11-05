package com.hhl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateInfoRequest {
	private String email;
	private String firstName;
	private String lastName;
	private String bio;
}
