package com.bridgelabz.fundoo.user.service;

import org.springframework.web.multipart.MultipartFile;

import com.bridgelabz.fundoo.response.Response;
import com.bridgelabz.fundoo.response.ResponseToken;
import com.bridgelabz.fundoo.user.dto.LoginDTO;
import com.bridgelabz.fundoo.user.dto.UserDTO;

/********************************************************************************************
 * Purpose : to perform the user activities for registration and login
 *           
 * @author BridgeLabz/Sudhakar
 * @version 1.0
 * @since 26-02-2019
 *********************************************************************************************/
public interface UserService {
   Response addUser(UserDTO userDTO);
   Response  verifyUser(String token);
   ResponseToken login(LoginDTO loginDTO);
   Response passwordRecovery(String email);
   Response resetPassword(String newPassword, String token);
   Response uploadProfilePic(MultipartFile  multipartFile, String token);
   String getProfilePic(String token);
   boolean isDuplicateUserByEmail(String email);
}
