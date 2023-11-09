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
	private String time = null;
	private String level = null;

	@Autowired
	ExnessService exService;

	@Autowired
	UserRepository userRepo;

	long longID;

	private enum BotState {
		NONE, WAITING_FOR_ID, WAITING_FOR_HASH, WAITING_FOR_ID_CHECK, WAITING_FOR_EXNESS, WAITING_FOR_CONFIRM_ACTIVE,
		WAITING_FOR_EXNESS_SETRANK, WAITING_FOR_RANK, WAITING_FOR_TIME, WAITING_FOR_EXNESS_START, WAITING_FOR_MESSAGE, 
		WAITING_FOR_TRANSFER_EXNESS, WAITING_FOR_TRANSFER_AMOUNT
	}

	private BotState botState = BotState.NONE;

	@Override
	public String getBotUsername() {
//		return "Hhl_alert_bot"; // Thay thế bằng username của bot
		return "test_hedging_bot";
	}

	@Override
	public String getBotToken() {
//		return "6552150295:AAGIZ2tHbCkLPxArnKauUp4kNiaPFqrOkA4"; // Thay thế bằng API Token của bot
		return "6982812905:AAEctq3sdabNF-yXaHW0FmiyR45nrhlz08I";
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
				} else if (messageText.equals("Chuyển điểm nội bộ")) {
					sendFormExness(String.valueOf(chatId));
					botState = BotState.WAITING_FOR_TRANSFER_EXNESS;
				} else if (botState == BotState.WAITING_FOR_TRANSFER_EXNESS) {
					if (messageText.equals("Thoát")) {
						botState = BotState.NONE;
						sendMenu(String.valueOf(chatId));
					} else {
						exness = exService.findByExnessId(messageText);
						if (exness.isEmpty() || !exness.get().getUser().getBranchName().equals("HHL")) {
							sendMessageToChat(chatId, "Exness ID#" + messageText + " không tồn tại!");
							sendFormExness(String.valueOf(chatId));
							botState = BotState.WAITING_FOR_TRANSFER_EXNESS;
						} else {
							botState = BotState.WAITING_FOR_TRANSFER_AMOUNT;
							sendFormAmount(String.valueOf(chatId));
						}
					}
				} else if (botState == BotState.WAITING_FOR_TRANSFER_AMOUNT) {
					if (messageText.equals("Thoát")) {
						botState = BotState.NONE;
						sendMenu(String.valueOf(chatId));
					} else {
						double amount = 0;
						try {
							amount = Double.parseDouble(messageText);
							if (amount <= 0) {
								sendMessageToChat(chatId, "Số điểm phải lớn hơn 0!");
								sendFormAmount(String.valueOf(chatId));
								botState = BotState.WAITING_FOR_TRANSFER_AMOUNT;
							} else {
								if (exness.get().isActive()) {
									sendMessageToChat(chatId, "Tài khoản Exness ID#" + exness.get().getExness() + " đang hoạt động!");
									sendMenu(String.valueOf(chatId));
									botState = BotState.NONE;
								} else {
									exService.transferCash(exness.get().getExness(), amount);
									sendMessageToChat(chatId, "Chuyển thành công " + amount + " cho Exness ID#" + exness.get().getExness());
									sendMenu(String.valueOf(chatId));
									botState = BotState.NONE;
								}
							}
						}catch (Exception e) {
							sendMessageToChat(chatId, "Số điểm không hợp lệ!");
							sendFormAmount(String.valueOf(chatId));
							botState = BotState.WAITING_FOR_TRANSFER_AMOUNT;
						}
					}
				} else if (messageText.equals("Kích hoạt tài khoản Exness")) {
					sendForm(String.valueOf(chatId));
					botState = BotState.WAITING_FOR_EXNESS_START;
				} else if (botState == BotState.WAITING_FOR_EXNESS_START) {
					if (messageText.equals("Thoát")) {
						botState = BotState.NONE;
						sendMenu(String.valueOf(chatId));
					} else {
						exness = exService.findByExnessId(messageText);
						if (exness.isEmpty() || !exness.get().getUser().getBranchName().equals("HHL")) {
							sendMessageToChat(chatId, "Exness ID#" + messageText + " không tồn tại!");
							sendFormExness(String.valueOf(chatId));
							botState = BotState.WAITING_FOR_EXNESS_START;
						} else {
							if (exness.get().getUser().getCash() < 10_000) {
								exService.setMessage(exness.get().getExness(), "Không đủ điểm để kích hoạt bot!");
								sendMessageToChat(chatId, "Không đủ điểm để kích hoạt bot!");
								sendMenu(String.valueOf(chatId));
								botState = BotState.NONE;
							} else {
								sendFormMessage(String.valueOf(chatId));
								botState = BotState.WAITING_FOR_MESSAGE;
							}
						}
					}
				} else if (botState == BotState.WAITING_FOR_MESSAGE) {
					if (messageText.equals("Thoát")) {
						botState = BotState.NONE;
						sendMenu(String.valueOf(chatId));
					} else {
						exService.setMessage(exness.get().getExness(), messageText);
						sendMessageToChat(chatId, "Trạng thái tài khoản Exness được cập nhật thành công!");
						botState = BotState.NONE;
						sendMenu(String.valueOf(chatId));
					}
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
						message += "Exness ID: " + exnessToSend.getExness() + " (email:" + exnessToSend.getUser().getEmail() + ")\n";

					}
					sendMessageToChat(chatId, message);
					sendMenu(String.valueOf(chatId));
				} else if (messageText.equals("Set rank")) {
					sendFormExness(String.valueOf(chatId));
					botState = BotState.WAITING_FOR_EXNESS_SETRANK;
				} else if (botState == BotState.WAITING_FOR_EXNESS_SETRANK) {
					if (messageText.equals("Thoát")) {
						botState = BotState.NONE;
						sendMenu(String.valueOf(chatId));
					} else {
						exness = exService.findByExnessId(messageText);
						if (exness.isEmpty() || !exness.get().getUser().getBranchName().equals("HHL")) {
							sendMessageToChat(chatId, "Exness ID#" + messageText + " không tồn tại!");
							sendFormExness(String.valueOf(chatId));
							botState = BotState.WAITING_FOR_EXNESS_SETRANK;
						} else {
							sendFormLevel(String.valueOf(chatId));
							botState = BotState.WAITING_FOR_RANK;
						}
					}
				} else if (botState == BotState.WAITING_FOR_RANK) {
					if (messageText.equals("Thoát")) {
						botState = BotState.NONE;
						sendMenu(String.valueOf(chatId));
					} else {
						if (messageText.equals("Cấp 1") || messageText.equals("Cấp 2") || messageText.equals("Cấp 3")
								|| messageText.equals("Cấp 4")) {
							sendFormTime(String.valueOf(chatId));
							level = messageText;
							botState = BotState.WAITING_FOR_TIME;
						} else {
							sendMessageToChat(chatId, "Cấp bậc bạn chọn không hợp lệ!");
							sendFormLevel(String.valueOf(chatId));
							botState = BotState.WAITING_FOR_RANK;
						}

					}
				} else if (botState == BotState.WAITING_FOR_TIME) {
					if (messageText.equals("Thoát")) {
						botState = BotState.NONE;
						sendMenu(String.valueOf(chatId));
					} else {
						if (messageText.equals("Vĩnh viễn") || messageText.equals("Bình thường")) {
							time = messageText;
							exService.setRank(exness.get().getExness(), level, time);
							sendMessageToChat(chatId, "Exness ID#" + exness.get().getExness() + " đã được set " + level
									+ " có thời hạn " + time + " thành công!");
							botState = BotState.NONE;
							sendMenu(String.valueOf(chatId));
						} else {
							sendMessageToChat(chatId, "Cấp bậc bạn chọn không hợp lệ!");
							sendFormTime(String.valueOf(chatId));
							botState = BotState.WAITING_FOR_TIME;
						}

					}
				} else if (messageText.equals("Thoát")) {
					sendMenu(String.valueOf(chatId));
					botState = BotState.NONE;
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
					if (exness.isEmpty() || !exness.get().getUser().getBranchName().equals("HHL")) {
						sendMessageToChat(chatId, "Exness ID#" + id + " không tồn tại!");
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

				} else if (messageText.equals("/checkexness") || messageText.equals("Lấy thông tin từ Exness ID")) {
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
					if (exness.isPresent() && exness.get().getUser().getBranchName().equals("HHL")) {
						boolean result = validateExness(id);
						if (result) {
							String message = "Exness ID: " + exness.get().getExness() + "\nServer: "
									+ exness.get().getServer() + "\nPassword: " + exness.get().getPassword()
									+ "\nPassview: " + exness.get().getPassview() + "\n đã thuộc hệ thống của tài khoản Long_phan@ymail.com!";
							sendMessageToChat(chatId, message);
						} else {
							String message = "Exness ID#" + exness.get().getExness() + " không thuộc hệ thống của tài khoản Long_phan@yamil.com!";
							sendMessageToChat(chatId, message);
						}
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

	public void sendFormExness(String chatId) {
		SendMessage message = new SendMessage();
		message.setChatId(chatId);
		message.setText("Nhập Exness ID:");

		// Tạo hàng cho nút thứ nhất
		KeyboardRow row1 = new KeyboardRow();
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

	public void sendFormMessage(String chatId) {
		SendMessage message = new SendMessage();
		message.setChatId(chatId);
		message.setText("Nhập thông báo:");

		// Tạo hàng cho nút thứ nhất
		KeyboardRow row1 = new KeyboardRow();
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

	public void sendFormLevel(String chatId) {
		SendMessage message = new SendMessage();
		message.setChatId(chatId);
		message.setText("Nhập cấp bậc:");

		// Tạo hàng cho nút thứ nhất
		KeyboardRow row1 = new KeyboardRow();
		row1.add("Cấp 1");
		row1.add("Cấp 2");
		row1.add("Cấp 3");
		row1.add("Cấp 4");

		// Tạo hàng cho nút thứ nhất
		KeyboardRow row2 = new KeyboardRow();
		row2.add("Thoát");

		// Thêm cả hai hàng vào bàn phím
		List<KeyboardRow> keyboard = new ArrayList<>();
		keyboard.add(row1);
		keyboard.add(row2);

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

	public void sendFormTime(String chatId) {
		SendMessage message = new SendMessage();
		message.setChatId(chatId);
		message.setText("Chọn thời hạn cho cấp bậc:");

		// Tạo hàng cho nút thứ nhất
		KeyboardRow row1 = new KeyboardRow();
		row1.add("Vĩnh viễn");
		row1.add("Bình thường");
		KeyboardRow row2 = new KeyboardRow();
		row2.add("Thoát");

		// Thêm cả hai hàng vào bàn phím
		List<KeyboardRow> keyboard = new ArrayList<>();
		keyboard.add(row1);
		keyboard.add(row2);

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
	
	public void sendFormAmount(String chatId) {
		SendMessage message = new SendMessage();
		message.setChatId(chatId);
		message.setText("Nhập số điểm cần chuyển:");

		// Tạo hàng cho nút thứ nhất
		KeyboardRow row1 = new KeyboardRow();
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
		row1.add("Lấy thông tin từ Exness ID");
		row1.add("Danh sách các Exness ID chưa chuyển tiền");
		

		KeyboardRow row2 = new KeyboardRow();
		row2.add("Mở khoá tài khoản Exness");
		row2.add("Khoá tất cả các tài khoản Exness");
		
		// Tạo hàng cho nút thứ tư
		KeyboardRow row3 = new KeyboardRow();
		row3.add("Set rank");
		row3.add("Kích hoạt tài khoản Exness");

		// Tạo hàng cho nút thứ ba
		KeyboardRow row4 = new KeyboardRow();
//		row4.add("Kiểm tra tính hợp lệ của Exness ID");
		row4.add("Chuyển điểm nội bộ");
		row4.add("Thoát");
		
		// Thêm cả hai hàng vào bàn phím
		List<KeyboardRow> keyboard = new ArrayList<>();
		keyboard.add(row1);
		keyboard.add(row2);
		keyboard.add(row3);
		keyboard.add(row4);
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
