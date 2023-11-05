package com.hhl.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.hhl.service.ExnessService;
import com.hhl.user.Exness;
import com.hhl.user.User;
import com.hhl.user.UserRepository;

public class TelegramBot extends TelegramLongPollingBot {
	private Optional<Exness> exness = null;
	@Autowired
	ExnessService exService;

	@Autowired
	UserRepository userRepo;

	long longID;

	private enum BotState {
		NONE, WAITING_FOR_ID, WAITING_FOR_HASH, WAITING_FOR_ID_CHECK, WAITING_FOR_EXNESS, WAITING_FOR_CONFIRM_ACTIVE
	}

	private BotState botState = BotState.NONE;

	@Override
	public String getBotUsername() {
		return "Hhl_alert_bot"; // Thay thế bằng username của bot
//		return "test_hedging_bot";
	}

	@Override
	public String getBotToken() {
		return "6552150295:AAGIZ2tHbCkLPxArnKauUp4kNiaPFqrOkA4"; // Thay thế bằng API Token của bot
//		return "6982812905:AAEctq3sdabNF-yXaHW0FmiyR45nrhlz08I";
	}

	// Phương thức gửi tin nhắn
	public void sendMessageToChat(long chatId, String message) {
		SendMessage sendMessage = new SendMessage();
		sendMessage.setChatId(String.valueOf(chatId));
		sendMessage.setText(message);
		try {
			execute(sendMessage);
		} catch (TelegramApiException e) {
			System.out.println(e.getMessage());
		}
	}

	@Override
	public void onUpdateReceived(Update update) {
		if (update.hasMessage() && update.getMessage().hasText()) {

			String messageText = update.getMessage().getText();
			long chatId = update.getMessage().getChatId();
			if (update.getMessage().getFrom().getUserName().equals("jasontran14")
					|| update.getMessage().getFrom().getUserName().equals("Mrlucas9")
					|| update.getMessage().getFrom().getUserName().equals("MrLPP")) {
				if (messageText.equals("/start")) { // Ví dụ: Khi người dùng gửi "/start"
					sendMenu(String.valueOf(chatId));
				} else if (messageText.equals("/getall")
						|| messageText.equals("Danh sách các Exness ID chưa chuyển tiền")) {
					String message = "";
					List<User> users = userRepo.getUsersByBranchName("HHL");
					List<Exness> exnesses = new ArrayList<>();
					for (User item : users) {
						List<Exness> listExness = exService.getByUser(item);
						for (Exness exnessItem : listExness) {
							if (!exnessItem.isActive()) {
								exnesses.add(exnessItem);
							}
						}
					}
					for (Exness exnessToSend : exnesses) {
						message += "Exness ID: " + exnessToSend.getExness() + " chưa thanh toán!\n";
					}
					sendMessageToChat(chatId, message);
				} else if (messageText.equals("Khoá tất cả các tài khoản Exness")) {
					exService.lockAll();
					sendMenu(String.valueOf(chatId));
				} else if (messageText.equals("/checkexness") || messageText.equals("Mở khoá tài khoản Exness")) {
					botState = BotState.WAITING_FOR_EXNESS;
					sendMessageToChat(chatId, "Nhập Exness ID cần mở: ");
				} else if (botState == BotState.WAITING_FOR_EXNESS) {
					if (messageText.equals("Thoát")) {
						botState = BotState.NONE; // Thoát khỏi trạng thái nhập ID
						sendMenu(String.valueOf(chatId)); // Gửi lại menu chính cho người dùng
						return;
					}
					String id = messageText;
					exness = exService.findByExnessId(id);
					if (exness.isEmpty()) {
						sendMessageToChat(chatId, "This exness id is not existed!");
					} else {
						if (!exness.get().isActive()) {
							if (exness.get().getMessage() == null) {
								sendMessageToChat(chatId, "This exness id " + exness.get().getExness()
										+ " hadn't uploaded transaction image!");
								sendMenu(String.valueOf(chatId));
								botState = BotState.NONE;
							} else {
								SendPhoto sendPhoto = new SendPhoto();
								sendPhoto.setChatId(String.valueOf(chatId));
								sendPhoto.setPhoto(new InputFile(exness.get().getMessage()));

								try {
									execute(sendPhoto);
								} catch (TelegramApiException e) {
									e.printStackTrace();
								}
								sendForm(String.valueOf(chatId));
								botState = BotState.WAITING_FOR_CONFIRM_ACTIVE;
							}
						} else {
							sendMessageToChat(chatId, "This exness id " + exness.get().getExness() + " is running");
							sendMenu(String.valueOf(chatId));
							botState = BotState.NONE;
						}
					}
				} else if (botState == BotState.WAITING_FOR_CONFIRM_ACTIVE) {
					if (messageText.equals("Thoát")) {
						botState = BotState.NONE;
						sendMenu(String.valueOf(chatId));
					} else if (messageText.equals("Đồng ý")) {
						exService.activeExness(exness.get().getExness());
						sendMessageToChat(chatId, "Active exness id: " + exness.get().getExness() + " successful!");
						botState = BotState.NONE;
						sendMenu(String.valueOf(chatId));
					}

				} else if (messageText.equals("/checkexness") || messageText.equals("Kiểm tra Exness ID")) {
					botState = BotState.WAITING_FOR_ID;
					sendMessageToChat(chatId, "Nhập Exness ID cần tìm: ");
				} else if (botState == BotState.WAITING_FOR_ID) {
					if (messageText.equals("Thoát")) {
						botState = BotState.NONE; // Thoát khỏi trạng thái nhập ID
						sendMenu(String.valueOf(chatId)); // Gửi lại menu chính cho người dùng
						return;
					}
					// Xử lý ID và chuyển sang trạng thái chờ nhập Hash
					String id = messageText;
					try {
						longID = Long.parseLong(id);
					} catch (Exception e) {
						sendMessageToChat(chatId, "Exness ID#" + longID + " không hợp lệ!");
						return;
					}

					exness = exService.findByExnessId(id);
					if (exness.isPresent()) {
						String message = "Exness ID: " + exness.get().getExness() + "\nServer: "
								+ exness.get().getServer() + "\nPassword: " + exness.get().getPassword()
								+ "\nPassview: " + exness.get().getPassview();
						sendMessageToChat(chatId, message);
					} else {
						sendMessageToChat(chatId, "Exness ID#" + id + " không tồn tại!");
					}
				} else if (messageText.equals("/validateexness")
						|| messageText.equals("Kiểm tra tính hợp lệ của Exness ID")) {
					botState = BotState.WAITING_FOR_ID_CHECK;
					sendMessageToChat(chatId, "Nhập Exness ID cần kiểm tra: ");
				} else if (botState == BotState.WAITING_FOR_ID_CHECK) {
					if (messageText.equals("Thoát")) {
						botState = BotState.NONE; // Thoát khỏi trạng thái nhập ID
						sendMenu(String.valueOf(chatId)); // Gửi lại menu chính cho người dùng
						return;
					}
					// Xử lý ID và chuyển sang trạng thái chờ nhập Hash
					String id = messageText;
					try {
						longID = Long.parseLong(id);
					} catch (Exception e) {
						sendMessageToChat(chatId, "Exness ID#" + longID + " không hợp lệ!");
						return;
					}

					boolean result = validateExness(id);
					sendMessageToChat(chatId, result == true ? "YES" : "NO");
				} else if (messageText.equals("Thoát")) {
					botState = BotState.NONE;
					sendMenu(String.valueOf(chatId));
				} else {
					sendMessageToChat(chatId, "Xin lỗi, chức năng bạn chọn không tồn tại!");
				}
			} else {
				sendMessageToChat(chatId, "Bạn không có quyền điều khiển bot trong group này!");
			}
		}
	}

	public void sendForm(String chatId) {
		SendMessage message = new SendMessage();
		message.setChatId(chatId);
		message.setText("Chọn thao tác:");

		// Tạo hàng cho nút thứ nhất
		KeyboardRow row1 = new KeyboardRow();
		row1.add("Đồng ý");
		row1.add("Thoát");

		// Thêm cả hai hàng vào bàn phím
		List<KeyboardRow> keyboard = new ArrayList<>();
		keyboard.add(row1);

		// Thêm hàng vào bàn phím
		ReplyKeyboardMarkup replyMarkup = new ReplyKeyboardMarkup();
		replyMarkup.setKeyboard(keyboard);
		message.setReplyMarkup(replyMarkup);

		try {
			execute(message);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

	public void sendMenu(String chatId) {
		SendMessage message = new SendMessage();
		message.setChatId(chatId);
		message.setText("Vui lòng chọn chức năng trên menu:");

		// Tạo hàng cho nút thứ nhất
		KeyboardRow row1 = new KeyboardRow();
		row1.add("Kiểm tra Exness ID");

		KeyboardRow row2 = new KeyboardRow();
		row1.add("Danh sách các Exness ID chưa chuyển tiền");

		// Tạo hàng cho nút thứ hai
		KeyboardRow row3 = new KeyboardRow();
		row2.add("Mở khoá tài khoản Exness");
		row2.add("Khoá tất cả các tài khoản Exness");

		// Tạo hàng cho nút thứ ba
		row3.add("Kiểm tra tính hợp lệ của Exness ID");

		// Tạo hàng cho nút thứ ba
		KeyboardRow row5 = new KeyboardRow();
		row3.add("Thoát");

		// Thêm cả hai hàng vào bàn phím
		List<KeyboardRow> keyboard = new ArrayList<>();
		keyboard.add(row1);
		keyboard.add(row2);
		keyboard.add(row3);
		keyboard.add(row5);
		// Thêm hàng vào bàn phím
		ReplyKeyboardMarkup replyMarkup = new ReplyKeyboardMarkup();
		replyMarkup.setKeyboard(keyboard);
		message.setReplyMarkup(replyMarkup);

		try {
			execute(message);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

	public boolean validateExness(String exnessId) {
		// check co trong he thong hay
		// curl -X GET --header 'Accept: application/json' --header 'Authorization: JWT
		// eyJhbGciOiJSUzI1NiIsImtpZCI6InVzZXIiLCJ0eXAiOiJKV1QifQ.eyJqdGkiOiIyNGNiZDE0OTk0ZDg0ZjRkODk3OGE2YjY3YmQ4YTFmMiIsImV4cCI6MTY5ODY3MDAyNywiaXNzIjoiQXV0aGVudGljYXRpb24iLCJpYXQiOjE2OTg2NDg0MjcsInN1YiI6IjViYjhhYWE5MjExYTQwMTRiOGZiYjViNjNmYmY5NDA1IiwiYXVkIjpbInBhcnRuZXJzaGlwIl0sImFkZGl0aW9uYWxfcGFyYW1zIjp7IndsX2lkIjoiODcxMWI4YWEtY2M2OC00MTNhLTgwMzQtYzI3MTZhMmNlMTRhIn19.BrCE3O2ZoOllnX_ee5gxOynzxvZQLBZA5c9nQqP0EO8mSym3GLGU4wb_asJba1BshZT78jaTxEeIbttsxPN_-o_MMmDw41kNAvLnYxbESr9K4kXLY64UUUAGxGQt0szzZStNZXjj_a3ze5VReiE6zSg59apox-fgOFnepUhBW-dv7ah1STMw-4bvE-0JvqD0Fss_9_Yx7s5ElVrzpSJPV2dMaGcUh_A7eWxa_DdDBvQOJ7fXaQ8_jGsWxtcpFDCK1iW6pGVJAQL_5kWTAsP_Qx_JHr0UYI8FokyDXuZ7qJXRQcK-UQdbwy6PNqL-wKi1xe5s74iY4OOKsXfAiSch4AbTIa6JTRJXkegx78vZ0GzFIj5SntszY6kQ5PjPmjTm4P35hVWIKhoFAKPOpt23MjaD0g2PkSQRD8sVNhO0AKSA4Z1k-0h6ec94FaA9iR1Kz0bpdgzV6vZB702gcijm-fxLp0_xDTRhFJffOWrNP7JAA3MpFZMdsps3HHMTfc2TVG1w6BBdCw-pGHqyUOaId54riFskhK__4JLB4uRDnKy0Gn_liiHHCrYSbYYWuGv9ZLh0zwA1m8pBi8IlXd0YC03RLtRY0AOdeN9Km1lvCCrmzm8ZrmJlthk30wlud4KbJlOzogkgq2ULhU0gLFaujguHuiBrEYue64R-lDCBh-E'
		// 'https://my.exnessaffiliates.com/api/reports/clients/?client_account=117057472'

		String url = "https://my.exnessaffiliates.com/api/v2/auth/";

		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");
		headers.set("Accept", "application/json");

		LoginRequest loginRequest = new LoginRequest();
		loginRequest.setLogin("Long_phan@ymail.com");
		loginRequest.setPassword("Xitrum11");

		HttpEntity<LoginRequest> request = new HttpEntity<>(loginRequest, headers);

		ResponseEntity<AuthResponse> responseEntity = new RestTemplate().exchange(url, HttpMethod.POST, request,
				AuthResponse.class);
		if (responseEntity.getStatusCode().is2xxSuccessful()) {
			AuthResponse authResponse = responseEntity.getBody();
			String token = authResponse.getToken();

			// Gọi API khác với token
			// Ví dụ: Gửi yêu cầu GET đến một API sử dụng token
			String apiUrl = "https://my.exnessaffiliates.com/api/reports/clients/?client_account=" + exnessId;

			HttpHeaders headersWithToken = new HttpHeaders();
			headersWithToken.set("Authorization", "JWT " + token);

			HttpEntity<String> requestWithToken = new HttpEntity<>(headersWithToken);

			ResponseEntity<String> apiResponse = new RestTemplate().exchange(apiUrl, HttpMethod.GET, requestWithToken,
					String.class);
			String json = apiResponse.getBody();
			if (json.contains("data\":[]")) {
				return false;
			} else {
				return true;
			}
		}

		return true;
	}
}
