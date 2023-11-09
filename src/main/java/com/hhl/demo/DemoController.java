package com.hhl.demo;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.zxing.WriterException;
import com.hhl.auth.AuthenticationService;
import com.hhl.auth.RefferalRequest;
import com.hhl.auth.UpdateExnessRequest;
import com.hhl.auth.UpdateRefRequest;
import com.hhl.auth.UpdateRefResponse;
import com.hhl.dto.ChangePasswordRequest;
import com.hhl.dto.ExnessResponse;
import com.hhl.dto.InfoResponse;
import com.hhl.dto.NetworkDto;
import com.hhl.dto.PreviousMonthResponse;
import com.hhl.dto.TelegramBot;
import com.hhl.dto.TwoFARequest;
import com.hhl.dto.UpdateInfoRequest;
import com.hhl.dto.UserUpdateExnessRequest;
import com.hhl.exception.NotFoundException;
import com.hhl.service.CommissionService;
import com.hhl.service.ExnessService;
import com.hhl.service.HistoryService;
import com.hhl.service.ImageUploadService;
import com.hhl.service.MessageService;
import com.hhl.service.PrevService;
import com.hhl.service.TransactionService;
import com.hhl.service.TransferService;
import com.hhl.service.UserService;
import com.hhl.user.Exness;
import com.hhl.user.ExnessRepository;
import com.hhl.user.History;
import com.hhl.user.Message;
import com.hhl.user.Transaction;
import com.hhl.user.Transfer;
import com.hhl.user.User;
import com.hhl.user.UserRepository;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.exceptions.CodeGenerationException;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrDataFactory;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/secured")
@CrossOrigin("*")
@Hidden
@RequiredArgsConstructor
public class DemoController {
	private final UserRepository userRepo;
	private final AuthenticationService service;
	private final UserService userService;
	private final MessageService messService;
	private final ExnessService exService;
	private final ExnessRepository exRepo;
	private final HistoryService hisService;
	private final PrevService prevService;
	private final SecretGenerator secretGenerator;
	private final PasswordEncoder passwordEncoder;
	private final QrDataFactory qrDataFactory;
	private final QrGenerator qrGenerator;
	private final TransactionService transactionService;
	private final CommissionService commissService;
	private final ImageUploadService uploadService;
	private final TransferService transferService;
	private final TelegramBot tele;
	private final Long chatId = Long.parseLong("-4095776689");

	@GetMapping("/get-all-exness/{email}")
	public ResponseEntity<List<ExnessResponse>> get(@PathVariable("email") String email) {
		List<ExnessResponse> listExness = service.getUserExnessByEmail(email);
		return ResponseEntity.ok(listExness);
	}

	@GetMapping("/get-exness-info/{exnessId}")
	public ResponseEntity<ExnessResponse> find(@PathVariable("exnessId") String exnessId) {
		ExnessResponse exness = service.findExnessInfoByExnessId(exnessId);
		return ResponseEntity.ok(exness);
	}

	@PostMapping("/edit-exness")
	public ResponseEntity<UpdateRefResponse> edit(@RequestBody UserUpdateExnessRequest request) {
		UpdateRefResponse result = new UpdateRefResponse();
		Optional<User> user = userRepo.findByEmail(request.getEmail());
		TimeProvider timeProvider = new SystemTimeProvider();
		CodeGenerator codeGenerator = new DefaultCodeGenerator();
		DefaultCodeVerifier verify = new DefaultCodeVerifier(codeGenerator, timeProvider);
		verify.setAllowedTimePeriodDiscrepancy(0);

		if (verify.isValidCode(user.get().getSecret(), request.getCode())) {
			result = service.editExness(request.getEmail(), request.getExness(), request.getServer(),
					request.getPassword(), request.getPassview());
		} else {
			result.setMessage("Sai 2FA");
			result.setStatus(404);
		}

		return ResponseEntity.ok(result);
	}

	@PostMapping("/upload-transaction")
	public ResponseEntity<String> uploadTransaction(@RequestParam("file") MultipartFile file,
			@RequestParam("exness") String exness) {
		Optional<Exness> exnessQuery = exService.findByExnessId(exness);
		if (exnessQuery.isEmpty()) {
			throw new NotFoundException("This exness is not existed!");
		}
		String fileName = "transaction_exxness_id" + exness;
		String url = uploadService.uploadImage(file, fileName);

		if (url != null) {
			if (exnessQuery.get().isActive()) {
				return ResponseEntity.ok("Tài khoản đang hoạt động, không cần upload ảnh mới!");
			}
			exnessQuery.get().setMessage(url);
			exRepo.save(exnessQuery.get());

			String message = "Exness ID: " + exness + " cập nhật ảnh chuyển tiền thành công!";
			tele.sendMessageToChat(chatId, message);
			return ResponseEntity.ok(message);
		} else {
			return ResponseEntity.ok("Error");
		}
	}

	@GetMapping
	public ResponseEntity<String> sayHello() {
		return ResponseEntity.ok("Hello from secured endpoint");
	}

	@GetMapping("/get-total-commission/{email}")
	public ResponseEntity<Double> getTotalCommission(@PathVariable("email") String email) {
		double totalCommission = 0.0;
		if (email.equalsIgnoreCase("trantuongthuy@gmail.com")) {
			totalCommission = commissService.getTotalCommission();
		} else {
			throw new NotFoundException("You cann't invoke to this information!");
		}
		return ResponseEntity.ok(totalCommission);
	}

	@GetMapping("/get-prev-data/{email}")
	public ResponseEntity<PreviousMonthResponse> getPreviousMonthData(@PathVariable("email") String email) {
		PreviousMonthResponse result = new PreviousMonthResponse();
		if (email.contains("@")) {
			result = exService.findByEmail(email);
		} else {
			result = exService.findByExness(email);
		}

		return ResponseEntity.ok(result);
	}

	@GetMapping("/showQR/{email}")
	public List<String> generate2FA(@PathVariable("email") String email)
			throws QrGenerationException, WriterException, IOException, CodeGenerationException {
		Optional<User> user = userRepo.findByEmail(email);
		QrData data = qrDataFactory.newBuilder().label(user.get().getEmail()).secret(user.get().getSecret())
				.issuer("Something Application").period(30).build();

		String qrCodeImage = getDataUriForImage(qrGenerator.generate(data), qrGenerator.getImageMimeType());
		List<String> info2FA = new ArrayList<>();
		String isEnabled = "";
		if (user.get().isMfaEnabled()) {
			isEnabled = "true";
		} else {
			isEnabled = "false";
		}
		info2FA.add(isEnabled);
		info2FA.add(qrCodeImage);

		return info2FA;
	}

	@PostMapping("/enable")
	public String enabled(@RequestBody TwoFARequest request) {
		Optional<User> user = userRepo.findByEmail(request.getEmail());
		TimeProvider timeProvider = new SystemTimeProvider();
		CodeGenerator codeGenerator = new DefaultCodeGenerator();
		DefaultCodeVerifier verify = new DefaultCodeVerifier(codeGenerator, timeProvider);
		verify.setAllowedTimePeriodDiscrepancy(0);

		if (verify.isValidCode(user.get().getSecret(), request.getCode())) {
			user.get().setMfaEnabled(true);
			userRepo.save(user.get());
			return "Enabled Success";
		} else {
			return "Enabled Failed";
		}
	}

	@PostMapping("/disable")
	public String disabled(@RequestBody TwoFARequest request) {
		Optional<User> user = userRepo.findByEmail(request.getEmail());
		TimeProvider timeProvider = new SystemTimeProvider();
		CodeGenerator codeGenerator = new DefaultCodeGenerator();
		DefaultCodeVerifier verify = new DefaultCodeVerifier(codeGenerator, timeProvider);
		verify.setAllowedTimePeriodDiscrepancy(0);

		if (verify.isValidCode(user.get().getSecret(), request.getCode())) {
			// xóa secret 2fa
			String secret = secretGenerator.generate();

			user.get().setMfaEnabled(false);
			user.get().setSecret(secret);
			userRepo.save(user.get());
			return "Disabled Success";
		} else {
			return "Disabled Failed";
		}
	}

	@GetMapping("/get-all-transfer/{email}")
	public ResponseEntity<List<Transfer>> getTransfer(@PathVariable("email") String email) {
		List<Transfer> results = transferService.findAllTransferByEmail(email);
		
		return ResponseEntity.ok(results);
	}

	@GetMapping("/get-info-by-exness/exness={exnessId}&from={from}&to={to}")
	public ResponseEntity<InfoResponse> getInfoByExness(@PathVariable("exnessId") String exnessId,
			@PathVariable("from") long from, @PathVariable("to") long to) {
		InfoResponse result = new InfoResponse();
		if (exnessId.contains("@")) {
			result = userService.getAllInfoByEmail(exnessId, from, to);
		} else {
			result = userService.getInfoByExnessId(exnessId, from, to);
		}

		return ResponseEntity.ok(result);
	}

	@GetMapping("/getNetwork/{email}")
	public ResponseEntity<List<NetworkDto>> getNetworkLevel(@PathVariable("email") String email) {
		int level = 1;
		int root = 1;
		List<NetworkDto> network = new ArrayList<>();
		getUserNetwork(email, level, root, network);

		Collections.sort(network);
		return ResponseEntity.ok(network);
	}

	@GetMapping("/get-message/email={email}")
	public ResponseEntity<List<Message>> getMessage(@PathVariable("email") String email) {
		List<Message> listMessages = messService.findMessagesByEmail(email);
		return ResponseEntity.ok(listMessages);
	}

	@GetMapping("/toggle-message/id={id}")
	public ResponseEntity<String> toggleMessage(@PathVariable("id") long id) {
		messService.toggleMessageStatus(id);
		return ResponseEntity.ok("OK");
	}

	@PostMapping("/edit-info")
	public ResponseEntity<String> editInfo(@RequestBody UpdateInfoRequest request) {
		service.editInfo(request);
		return ResponseEntity.ok("OK");
	}

	private void getUserNetwork(String email, int desiredLevel, int currentLevel, List<NetworkDto> network) {
		if (currentLevel <= desiredLevel) {
			List<User> users = userRepo.findByRefferal(email);
			if (users.isEmpty()) {
				return;
			}

			for (User user : users) {
				String uploadDirectory = "src/main/resources/assets/avatar";
				Path uploadPath = Path.of(uploadDirectory);
				String defaultFileName = "avatar_user_default.png";
				// Xây dựng tên tệp dựa trên id
				String fileName = "avatar_user_id_" + user.getId() + ".png";
				Path filePath = uploadPath.resolve(fileName);
				byte[] imageBytes = null;
				if (!Files.exists(filePath)) {
					filePath = uploadPath.resolve(defaultFileName);
				}

				try {
					imageBytes = Files.readAllBytes(filePath);
				} catch (IOException e) {
					e.printStackTrace();
				}

				network.add(new NetworkDto(user.getEmail(), email, imageBytes, currentLevel));
				getUserNetwork(user.getEmail(), desiredLevel, currentLevel + 1, network);
			}
		}
	}

	@PostMapping("/update-ref")
	public ResponseEntity<UpdateRefResponse> updateRef(@RequestBody UpdateRefRequest request) {
		return ResponseEntity.ok(service.updateRef(request.getCurrent(), request.getCode()));
	}

	@PostMapping("/update-exness")
	public ResponseEntity<UpdateRefResponse> updateExness(@RequestBody UpdateExnessRequest request) {
		return ResponseEntity.ok(service.updateExness(request.getEmail(), request.getExness(), request.getServer(),
				request.getPassword(), request.getPassview(), request.getType()));
	}

	@GetMapping("/get-exness/exness={exness}")
	public ResponseEntity<Exness> getExnessByExnessid(@PathVariable("exness") String exness) {
		return ResponseEntity.ok(exService.findByExnessId(exness).orElse(null));
	}

	@GetMapping("/get-exness/{email}")
	public ResponseEntity<List<String>> getExnessByEmail(@PathVariable("email") String email) {
		return ResponseEntity.ok(service.getExnessByEmail(email));
	}

	@PostMapping("/get-info")
	public ResponseEntity<HashMap<String, String>> getInfo(@RequestBody RefferalRequest request) {
		return ResponseEntity.ok(service.getInfo(request.getEmail()));
	}

	@PostMapping("/upload-avatar")
	public ResponseEntity<byte[]> uploadAvatar(@RequestParam("file") MultipartFile file,
			@RequestParam("email") String email) {
		User user = userRepo.findByEmail(email).get();

		try {
			// Kiểm tra kiểu MIME của tệp
			String contentType = file.getContentType();
			if (!contentType.startsWith("image")) {
				throw new NotFoundException("No image found");
			}

			// Lấy đường dẫn đến thư mục lưu trữ avatar (src/main/resources/assets/avatar)
			String uploadDirectory = "src/main/resources/assets/avatar";
			Path uploadPath = Path.of(uploadDirectory);

			// Tạo thư mục nếu nó chưa tồn tại
			if (!Files.exists(uploadPath)) {
				Files.createDirectories(uploadPath);
			}

			// Lấy tên tệp từ MultipartFile
			String fileName = "avatar_user_id_" + user.getId() + ".png";
			Path filePath = uploadPath.resolve(fileName);

			// Lưu tệp vào thư mục
			Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

			// Trả về thông báo thành công
			// Đọc nội dung tệp ảnh
			byte[] imageBytes = Files.readAllBytes(filePath);
			return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG) // Đặt kiểu MIME cho ảnh (png hoặc phù hợp với
																		// định dạng ảnh của bạn)
					.body(imageBytes);
		} catch (IOException e) {
			return ResponseEntity.notFound().build();
		}
	}

	@GetMapping("/avatar/{email}")
	public ResponseEntity<byte[]> getAvatar(@PathVariable("email") String email) {
		// Lấy đường dẫn đến thư mục lưu trữ avatar (src/main/resources/assets/avatar)
		String uploadDirectory = "src/main/resources/assets/avatar";
		Path uploadPath = Path.of(uploadDirectory);

		User user = userRepo.findByEmail(email).get();
		// Xây dựng tên tệp dựa trên id
		String fileName = "avatar_user_id_" + user.getId() + ".png";
		Path filePath = uploadPath.resolve(fileName);

		try {
			// Đọc nội dung tệp ảnh
			byte[] imageBytes = Files.readAllBytes(filePath);
			return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG) // Đặt kiểu MIME cho ảnh (png hoặc phù hợp với
																		// định dạng ảnh của bạn)
					.body(imageBytes);
		} catch (IOException e) {
			return ResponseEntity.notFound().build();
		}
	}

	@GetMapping("/transaction-image/{exness}")
	public ResponseEntity<byte[]> getTransactionImage(@PathVariable("exness") String exness) {
		// Lấy đường dẫn đến thư mục lưu trữ avatar (src/main/resources/assets/avatar)
		String uploadDirectory = "src/main/resources/assets/transaction";
		Path uploadPath = Path.of(uploadDirectory);

		// Xây dựng tên tệp dựa trên id
		String fileName = "transaction_exness_id_" + exness + ".png";
		Path filePath = uploadPath.resolve(fileName);

		try {
			// Đọc nội dung tệp ảnh
			byte[] imageBytes = Files.readAllBytes(filePath);
			return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG) // Đặt kiểu MIME cho ảnh (png hoặc phù hợp với
																		// định dạng ảnh của bạn)
					.body(imageBytes);
		} catch (IOException e) {
			return ResponseEntity.notFound().build();
		}
	}

	@PostMapping("/upload-banner")
	public ResponseEntity<byte[]> uploadBanner(@RequestParam("file") MultipartFile file) {
		try {
			// Kiểm tra kiểu MIME của tệp
			String contentType = file.getContentType();
			if (!contentType.startsWith("image")) {
				throw new NotFoundException("No image found");
			}

			// Lấy đường dẫn đến thư mục lưu trữ avatar (src/main/resources/assets/avatar)
			String uploadDirectory = "src/main/resources/assets/banner";
			Path uploadPath = Path.of(uploadDirectory);

			// Tạo thư mục nếu nó chưa tồn tại
			if (!Files.exists(uploadPath)) {
				Files.createDirectories(uploadPath);
			}

			// Lấy tên tệp từ MultipartFile
			String fileName = "banner.png";
			Path filePath = uploadPath.resolve(fileName);

			// Lưu tệp vào thư mục
			Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

			// Trả về thông báo thành công
			// Đọc nội dung tệp ảnh
			byte[] imageBytes = Files.readAllBytes(filePath);
			return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG) // Đặt kiểu MIME cho ảnh (png hoặc phù hợp với
																		// định dạng ảnh của bạn)
					.body(imageBytes);
		} catch (IOException e) {
			return ResponseEntity.notFound().build();
		}
	}

	@GetMapping("/banner")
	public ResponseEntity<byte[]> getBanner() {
		// Lấy đường dẫn đến thư mục lưu trữ avatar (src/main/resources/assets/avatar)
		String uploadDirectory = "src/main/resources/assets/banner";
		Path uploadPath = Path.of(uploadDirectory);

		// Xây dựng tên tệp dựa trên id
		String fileName = "banner.png";
		Path filePath = uploadPath.resolve(fileName);

		try {
			// Đọc nội dung tệp ảnh
			byte[] imageBytes = Files.readAllBytes(filePath);
			return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG) // Đặt kiểu MIME cho ảnh (png hoặc phù hợp với
																		// định dạng ảnh của bạn)
					.body(imageBytes);
		} catch (IOException e) {
			return ResponseEntity.notFound().build();
		}
	}

	@PostMapping("/change-password")
	public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest request) {
		Optional<User> user = userRepo.findByEmail(request.getEmail());
		if (user.isEmpty()) {
			return ResponseEntity.ok("Tài khoản không tồn tại!");
		}

		TimeProvider timeProvider = new SystemTimeProvider();
		CodeGenerator codeGenerator = new DefaultCodeGenerator();
		DefaultCodeVerifier verify = new DefaultCodeVerifier(codeGenerator, timeProvider);
		verify.setAllowedTimePeriodDiscrepancy(0);

		if (verify.isValidCode(user.get().getSecret(), request.getCode())) {
			user.get().setPassword(passwordEncoder.encode(request.getPassword()));
			userRepo.save(user.get());
			return ResponseEntity.ok("Thay đổi mật khẩu thành công!");
		} else {
			return ResponseEntity.ok("Mã 2FA không chính xác!");
		}
	}

	@GetMapping("/get-transaction/email={email}")
	public ResponseEntity<List<Transaction>> getTransactionByEmail(@PathVariable("email") String email) {
		return ResponseEntity.ok(transactionService.findTransactionByEmail(email));
	}

	@GetMapping("/get-history/email={email}")
	public ResponseEntity<List<History>> getHistoryByEmail(@PathVariable("email") String email) {
		return ResponseEntity.ok(hisService.findHistoryByEmail(email));
	}

	private String getCellValueAsString(Cell cell) {
		if (cell == null) {
			return "Ô dữ liệu trống!";
		}

		switch (cell.getCellType()) {
		case STRING:
			return cell.getStringCellValue();
		case NUMERIC:
			return String.valueOf(cell.getNumericCellValue());
		case BOOLEAN:
			return String.valueOf(cell.getBooleanCellValue());
		default:
			return "Lỗi! Không thể đọc dữ liệu";
		}
	}
}
