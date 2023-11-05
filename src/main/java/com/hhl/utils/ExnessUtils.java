package com.hhl.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hhl.dto.AuthResponse;
import com.hhl.dto.DataItem;
import com.hhl.dto.LoginRequest;
import com.hhl.service.CommissionService;
import com.hhl.service.ExnessService;
import com.hhl.service.UserService;
import com.hhl.user.Commission;
import com.hhl.user.Exness;

@RestController
public class ExnessUtils {
	@Autowired
	ExnessService exService;
	@Autowired
	UserService userSerivce;
	@Autowired
	CommissionService commissService;

	public void getIB() throws JsonMappingException, JsonProcessingException {
		
	}
}
