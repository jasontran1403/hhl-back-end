package com.hhl.service;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.StorageClient;
import com.hhl.user.ExnessRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ImageUploadService {
	private final ExnessRepository exRepo;
	
	public String uploadImage(MultipartFile file, String fileName) {
		try {
		    if (FirebaseApp.getApps().isEmpty()) {
		        InputStream serviceAccount = ImageUploadService.class.getResourceAsStream("/private-key.json");

		        FirebaseOptions options = new FirebaseOptions.Builder()
		            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
		            .setStorageBucket("hedging-1d816.appspot.com")
		            .build();

		        FirebaseApp.initializeApp(options);
		    }
		    
		    Bucket bucket = StorageClient.getInstance().bucket();

		    // Xác định kiểu MIME cho tệp (image/png)
		    String contentType = "image/png";

		    // Tạo tệp mới trên Firebase Storage
		    Blob blob = bucket.create(fileName, file.getInputStream(), contentType);
		    long expiration = 1000000000000L;
		    String token = blob.signUrl(expiration, TimeUnit.MILLISECONDS).toString();
		    return token;
		} catch (Exception e) {
		    e.printStackTrace();
		    return null;
		}
	}
	

}
