package com.hhl.auth;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hhl.dto.MessageRequest;
import com.hhl.service.BalanceService;
import com.hhl.service.CommissionService;
import com.hhl.service.ExnessService;
import com.hhl.service.HistoryService;
import com.hhl.service.MessageService;
import com.hhl.service.PrevService;
import com.hhl.service.ProfitService;
import com.hhl.service.TransactionService;
import com.hhl.service.UserService;
import com.hhl.user.Exness;
import com.hhl.user.ExnessRepository;
import com.hhl.user.ExnessTransactionRepository;
import com.hhl.user.User;
import com.hhl.user.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin("*")
@RequiredArgsConstructor
public class AuthenticationController {
	private final UserRepository userRepo;
	private final AuthenticationService service;
	private final HistoryService hisService;
	private final PrevService prevService;
	private final ExnessTransactionRepository exTranRepo;
	private final UserService userService;
	private final ExnessService exService;
	private final MessageService messService;
	private final ExnessRepository exRepo;
	private final TransactionService tranService;
	private final ProfitService proService;
	private final BalanceService balanceService;
	private final CommissionService commissService;
//	private final TelegramBot tele;
//	private final Long chatId = Long.parseLong("-4095776689");
//	
//	@GetMapping("/test/{message}")
//	public String testtele(@PathVariable("message") String message) {
//		tele.sendMessageToChat(chatId, message);
//		return "OK";
//	}

	@GetMapping("/lock-all")
	public ResponseEntity<String> lockAll() {
		List<Exness> exnesses = exRepo.findAll();
		for (Exness item : exnesses) {
			item.setActive(false);
			exRepo.save(item);
		}
		return ResponseEntity.ok("OK");
	}

	@GetMapping("/test/{email}")
	public ResponseEntity<Double> test(@PathVariable("email") String email) {
		double result = service.getTotalSales(email) / 100;
		
		// them message khi duoc duyet thanh cong
		return ResponseEntity.ok(result);
	}

	@PostMapping("/register")
	public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request) {
		System.out.println("OK");
		return ResponseEntity.ok(service.register(request));
	}

	@PostMapping("/authenticate")
	public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
		return ResponseEntity.ok(service.authenticate(request));
	}

	@PostMapping("/getCode")
	public ResponseEntity<String> getCode(@RequestBody RefferalRequest request) {
		return ResponseEntity.ok(service.generateCode(request.getEmail()));
	}

	@PostMapping("/forgot-password")
	public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
		return ResponseEntity.ok(service.forgotPassword(request));
	}

	@PostMapping("/logout")
	public ResponseEntity<String> logout(@RequestBody LogoutRequest request) {
		return ResponseEntity.ok(service.logout(request.getAccess_token()));
	}

	@PostMapping("/refresh-token")
	public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
		service.refreshToken(request, response);
	}

	private HashMap<Integer, String> getNetWorkToLisa(String exness) {
		HashMap<Integer, String> listNetWorks = new HashMap<>();
		try {
			Optional<Exness> exnessF0 = exRepo.findByExness(exness);
			int level = 1;

			String userF1 = exnessF0.get().getUser().getRefferal();
			listNetWorks.put(level, userF1);
			level++;
			String userPointer = userF1;

			do {
				Optional<User> user = userRepo.findByEmail(userPointer);
				if (user.isEmpty()) {
					break;
				}
				if (!user.get().getRefferal().equals("")) {
					listNetWorks.put(level, user.get().getRefferal());
				}

				userPointer = user.get().getRefferal();
				level++;
			} while (!userPointer.equals("lisa@gmail.com") && level <= 5);
		} catch (Exception e) {
			return new HashMap<>();
		}

		return listNetWorks;
	}
}
