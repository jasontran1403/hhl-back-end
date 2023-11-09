package com.hhl;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.hhl.auth.AuthenticationService;
import com.hhl.dto.MessageRequest;
import com.hhl.dto.TelegramBot;
import com.hhl.service.ExnessService;
import com.hhl.service.MessageService;
import com.hhl.service.ProfitService;
import com.hhl.service.UserService;
import com.hhl.user.Exness;
import com.hhl.user.ExnessRepository;
import com.hhl.user.User;
import com.hhl.user.UserRepository;

import lombok.RequiredArgsConstructor;

@SpringBootApplication
@RequiredArgsConstructor
@EnableScheduling
public class SecurityApplication {
	private final UserRepository userRepo;
	private final ExnessRepository exRepo;
	private final ProfitService proService;
	private final AuthenticationService service;
	private final MessageService messService;
	private final ExnessService exService;
	private final UserService userService;
	private final TelegramBot tele;
	private final Long chatId = Long.parseLong("-4095776689");

	public static void main(String[] args) {
		SpringApplication.run(SecurityApplication.class, args);
	}

	// Chuyen status ve inactive
	@Scheduled(cron = "0 30 7 1 * ?", zone = "GMT+7:00")
	public void toggle() {
		List<User> usersHHLBranch = userService.getUsersByBranchName("HHL");
		for (User userHHL : usersHHLBranch) {
			String email = userHHL.getEmail();
			double result = service.getTotalSales(email) / 100;
			User user = userRepo.findByEmail(email).get();
			List<Exness> exnesses = exService.getByUser(user);
			for (Exness exness : exnesses) {
				double profit = proService.getTotalProfitLastMonth(exness.getExness());
				if (profit > 0) {
					String message = "Vui lòng chuyển đến Exness ID: 52166788 (email:Lucas9.ho@gmail.com) ";
					if (exness.isSet()) {
						if (exness.getLevel() == 1) {
							message += "30% lợi nhuận của bạn, là: " + 0.3 * profit;
						} else if (exness.getLevel() == 2) {
							message += "20% lợi nhuận của bạn, là: " + 0.2 * profit;
						} else if (exness.getLevel() == 3) {
							message += "10% lợi nhuận của bạn, là: " + 0.1 * profit;
						} else if (exness.getLevel() == 4) {
							message += "Cấp bậc của bạn đang là 4, bạn không cần chia sẻ lợi nhuận!";
						}
					} else {
						if (result < 20_000) {
							exness.setLevel(1);
							message += "30% lợi nhuận của bạn, là: " + 0.3 * profit;
						} else if (result >= 20_000 && result <= 50_000) {
							exness.setLevel(2);
							message += "20% lợi nhuận của bạn, là: " + 0.2 * profit;
						} else if (result >= 50_000 && result <= 100_000) {
							exness.setLevel(3);
							message += "10% lợi nhuận của bạn, là: " + 0.1 * profit;
						} else if (result > 100_000) {
							exness.setLevel(4);
							message += "Cấp bậc của bạn đang là 4, bạn không cần chia sẻ lợi nhuận!";
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
	}

	@Scheduled(cron = "0 0 7 5 * ?", zone = "GMT+7:00")
	public void sendMessage() {
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
	}

	@Bean
	public CommandLineRunner commandLineRunner(

			AuthenticationService service) {
		return args -> {
//			var admin = RegisterRequest.builder()
//					.firstname("Admin")
//					.lastname("Admin")
//					.email("admin@gmail.com")
//					.password("123")
//					.role(ADMIN)
//					.build();
//			System.out.println("Admin token: " + service.register(admin).getAccessToken());
//
//			var manager = RegisterRequest.builder()
//					.firstname("Admin")
//					.lastname("Admin")
//					.email("manager@gmail.com")
//					.password("password")
//					.role(MANAGER)
//					.build();
//			System.out.println("Manager token: " + service.register(manager).getAccessToken());

		};
	}
}
