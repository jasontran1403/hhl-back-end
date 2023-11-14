package com.hhl.demo;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hhl.auth.AuthenticationService;
import com.hhl.auth.UpdateExnessRequest;
import com.hhl.auth.UpdateRefResponse;
import com.hhl.dto.ExnessResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin("*")
@RequiredArgsConstructor
public class AdminController {
	private final AuthenticationService service;

    @GetMapping("/get-all-exness")
    public ResponseEntity<List<ExnessResponse>> get() {
    	List<ExnessResponse> listExness = service.getAllExness();
        return ResponseEntity.ok(listExness);
    }
    
    @PostMapping("/update-exness")
	public ResponseEntity<UpdateRefResponse> updateExness(@RequestBody UpdateExnessRequest request) {
		return ResponseEntity.ok(service.updateExness(request.getEmail(), request.getExness(), request.getServer(), 
				request.getPassword(), request.getPassview(), request.getRefferal(), request.getType()));
	}
    
    @GetMapping("/active-exness/{exness}")
	public ResponseEntity<String> activeExness(@PathVariable("exness") String exness) {
    	service.activeExness(exness);
		return ResponseEntity.ok("OK");
	}
    
    @DeleteMapping
    public String delete() {
        return "DELETE:: admin controller";
    }
}
