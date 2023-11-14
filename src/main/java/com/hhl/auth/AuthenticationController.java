package com.hhl.auth;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
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
import com.hhl.dto.TelegramBot;
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
	private final TelegramBot tele;
	private final Long chatId = Long.parseLong("-4095776689");

	@GetMapping("/test/{exnessId}")
	public double test(@PathVariable("exnessId") String exnessId) {
		List<User> userHHLBranch = userService.getUsersByBranchName("HHL");
		double totalSales = service.getTotalSalesByExness(exnessId) / 100;

		return totalSales;
	}

	@GetMapping("/endpoint1")
	public ResponseEntity<String> testtele() {
		DecimalFormat df = new DecimalFormat("#.##");
		List<User> usersHHLBranch = userService.getUsersByBranchName("HHL");
		for (User userHHL : usersHHLBranch) {
			String email = userHHL.getEmail();
			User user = userRepo.findByEmail(email).get();
			List<Exness> exnesses = exService.getByUser(user);
			for (Exness exness : exnesses) {
				double profit = proService.getTotalProfitLastMonth(exness.getExness());
				double result = service.getTotalSalesByExness(exness.getExness()) / 100;
				if (profit > 0) {
					String message = "[Exness ID#" + exness.getExness()
							+ "] vui lòng chuyển đến Exness ID#52166788 (email:Lucas9.ho@gmail.com) ";
					if (exness.isSet()) {
						if (exness.getLevel() == 1) {
							message += "30% lợi nhuận của bạn, là: " + df.format(0.3 * profit) + " USC";
							exness.setReason("Chuyển 30% lợi nhuận của bạn, là:" + df.format(0.3 * profit)
									+ " USC đến Exness ID#52166788 (email:Lucas9.ho@gmail.com)!");
						} else if (exness.getLevel() == 2) {
							message += "20% lợi nhuận của bạn, là: " + df.format(0.2 * profit) + " USC";
							exness.setReason("Chuyển 20% lợi nhuận của bạn, là:" + df.format(0.3 * profit)
									+ " USC đến Exness ID#52166788 (email:Lucas9.ho@gmail.com)!");
						} else if (exness.getLevel() == 3) {
							message += "10% lợi nhuận của bạn, là: " + df.format(0.1 * profit) + " USC";
							exness.setReason("Chuyển 10% lợi nhuận của bạn, là:" + df.format(0.3 * profit)
									+ " USC đến Exness ID#52166788 (email:Lucas9.ho@gmail.com)!");
						} else if (exness.getLevel() == 4) {
							message = "[Exness ID#" + exness.getExness()
									+ " cấp bậc là 4, bạn không cần chia sẻ lợi nhuận!";
						}
					} else {
						if (result < 20_000) {
							exness.setLevel(1);
							exness.setReason("Chuyển 30% lợi nhuận của bạn, là:" + df.format(0.3 * profit)
									+ " USC đến Exness ID#52166788 (email:Lucas9.ho@gmail.com)!");
							message += "30% lợi nhuận của bạn, là: " + df.format(0.3 * profit) + " USC";
						} else if (result >= 20_000 && result <= 50_000) {
							exness.setLevel(2);
							exness.setReason("Chuyển 20% lợi nhuận của bạn, là:" + df.format(0.3 * profit)
									+ " USC đến Exness ID#52166788 (email:Lucas9.ho@gmail.com)!");
							message += "20% lợi nhuận của bạn, là: " + df.format(0.2 * profit) + " USC";
						} else if (result >= 50_000 && result <= 100_000) {
							exness.setLevel(3);
							message += "10% lợi nhuận của bạn, là: " + df.format(0.1 * profit) + " USC";
							exness.setReason("Chuyển 10% lợi nhuận của bạn, là:" + df.format(0.3 * profit)
									+ " USC đến Exness ID#52166788 (email:Lucas9.ho@gmail.com)!");
						} else if (result > 100_000) {
							exness.setLevel(4);
							message = "[Exness ID#" + exness.getExness()
									+ " cấp bậc là 4, bạn không cần chia sẻ lợi nhuận!";
						}
					}

					exRepo.save(exness);
					userRepo.save(user);

					MessageRequest popup = new MessageRequest();
					popup.setMessage(message);
					popup.setEmail(user.getEmail());
					messService.saveMessage(popup);
				}
			}

			List<Exness> exnessesToLock = exService.getAllExnessByBranch();
			for (Exness item : exnessesToLock) {
				if (item.getLevel() < 4) {
					item.setActive(false);
					exRepo.save(item);
				}
			}
		}
		return ResponseEntity.ok("OK");
	}

	@GetMapping("/endpoint2")
	public ResponseEntity<String> endpoint2() {
		List<Exness> exnesses = exService.getAllExnessByBranch();
		String message = "";
		for (Exness item : exnesses) {
			if (!item.isActive()) {
				message += "Exness ID: " + item.getExness() + " chưa chuyển chia sẻ lợi nhuận tháng trước!\n";

				MessageRequest popup = new MessageRequest();
				popup.setMessage("Bạn chưa chuyển chia sẻ lợi nhuận tháng trước, tài khoản của bạn sẽ bị tắt BOT!");
				popup.setEmail(item.getUser().getEmail());
				messService.saveMessage(popup);
			}
		}

		if (!message.equals("")) {
			tele.sendMessageToChat(chatId, message);
		}

		return ResponseEntity.ok("OK");
	}

	@GetMapping("/lock-all")
	public ResponseEntity<String> lockAll() {
		List<Exness> exnesses = exRepo.findAll();
		for (Exness item : exnesses) {
			item.setActive(false);
			exRepo.save(item);
		}
		return ResponseEntity.ok("OK");
	}

	@PostMapping("/register")
	public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request) {
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
