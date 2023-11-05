package com.hhl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExnessDto {
    private String email;
    private String exnessId;
    private String server;
    private String password;
    private String passview;
}