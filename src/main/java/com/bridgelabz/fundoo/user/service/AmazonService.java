package com.bridgelabz.fundoo.user.service;

import org.springframework.web.multipart.MultipartFile;

import com.bridgelabz.fundoo.response.Response;

public interface AmazonService {
    Response uploadFile(MultipartFile multipartFile, Long userId);
    String getProfilePicFromS3Bucket(Long userId);
}
