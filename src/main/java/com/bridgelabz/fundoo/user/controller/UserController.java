package com.bridgelabz.fundoo.user.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bridgelabz.fundoo.exception.UserException;
import com.bridgelabz.fundoo.response.Response;
import com.bridgelabz.fundoo.response.ResponseToken;
import com.bridgelabz.fundoo.user.dto.LoginDTO;
import com.bridgelabz.fundoo.user.dto.UserDTO;
import com.bridgelabz.fundoo.user.service.UserService;

/********************************************************************************************
 * Purpose : to handle the user requests for registration and login activities
 * 
 * @author BridgeLabz/Sudhakar
 * @version 1.0
 * @since 27-02-2019
 *********************************************************************************************/
@RestController
public class UserController {
	@Autowired
	private UserService userService;
	@Autowired
	private Environment environment;
	private static final Logger logger = LoggerFactory.getLogger(UserController.class);

	/**
	 * Registers a new User to the Fundoo App
	 * 
	 * @param userDTO
	 * @param bindingResult
	 * @return
	 */
	@PostMapping(value = "/user/register")
	public ResponseEntity<Response> register(@RequestBody UserDTO userDTO, BindingResult bindingResult) {
		logger.info("UserDTO : " + userDTO);
		logger.trace("User Registration");
		checkBindingResultError(bindingResult);
		Response response = userService.addUser(userDTO);
		return new ResponseEntity<Response>(response, HttpStatus.OK);
	}

	/**
	 * verifies the user through email notification
	 * @param token user token
	 * @return response
	 */
	@GetMapping(value = "/user/useractivation/{token}")
	public ResponseEntity<Response> userVerification(@PathVariable String token) {
		logger.info("token : " + token);
		logger.trace("User Verification");
		Response response = userService.verifyUser(token);
		return new ResponseEntity<Response>(response, HttpStatus.OK);
	}

	/**
	 * logins the user to the application
	 * @param loginDTO login credentials instance
	 * @param bindingResult errors while binding the parameters
	 * @return response
	 */
	@PostMapping(value = "/user/login")
	public ResponseEntity<ResponseToken> login(@RequestBody LoginDTO loginDTO, BindingResult bindingResult) {
		logger.info("UserDTO : " + loginDTO);
		logger.trace("User Login");
		checkBindingResultError(bindingResult);
		ResponseToken responseToken = userService.login(loginDTO);
		return new ResponseEntity<ResponseToken>(responseToken, HttpStatus.OK);
	}

	@GetMapping(value = "/user/forgotpassword")
	public ResponseEntity<Response> passwordRecovery(@RequestBody LoginDTO loginDTO, BindingResult bindingResult){
		logger.info("user email : " + loginDTO.getEmail());
		logger.trace("User Login");
		checkBindingResultError(bindingResult);
		Response response = userService.passwordRecovery(loginDTO.getEmail());
		return new ResponseEntity<Response>(response, HttpStatus.OK);
	}

	@PutMapping(value="/user/resetpassword/{token}")
	public ResponseEntity<Response> resetPassword(@RequestBody LoginDTO loginDTO, BindingResult bindingResult, @PathVariable String token){
		logger.info("user password : " + loginDTO.getEmail());
		logger.trace("reset user password");
		checkBindingResultError(bindingResult);
		Response response = userService.resetPassword(loginDTO.getPassword(), token);
		return new ResponseEntity<Response>(response, HttpStatus.OK);
	}
    
	@PostMapping("user/profilepic")
	public ResponseEntity<Response> uploadProfilePicture(@RequestPart("multipartFile") MultipartFile multipartFile, @RequestHeader String token){
		Response response = userService.uploadProfilePic(multipartFile, token);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@GetMapping("user/profilepic")
	public ResponseEntity<String> getProfilePicture(@RequestHeader String token){
		return new ResponseEntity<>(userService.getProfilePic(token),HttpStatus.OK);
	}
	
	private void checkBindingResultError(BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			logger.error("Error while binding user details");
			String statusMessge = environment.getProperty("status.bindingResult.invalidData");
			int statusCode = Integer.parseInt(environment.getProperty("status.bindingResult.errorCode"));
//			Response response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.success.code")),
//					environment.getProperty("status.forgotPassword.success"));
//			return new ResponseEntity<Response>(response, HttpStatus.OK);
			throw new UserException(statusCode, statusMessge);
		}
	}
}
