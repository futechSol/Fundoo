package com.bridgelabz.fundoo.user.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.bridgelabz.fundoo.exception.UserException;
import com.bridgelabz.fundoo.response.Response;
import com.bridgelabz.fundoo.response.ResponseToken;
import com.bridgelabz.fundoo.user.dto.LoginDTO;
import com.bridgelabz.fundoo.user.dto.UserDTO;
import com.bridgelabz.fundoo.user.model.User;
import com.bridgelabz.fundoo.user.repository.UserRepository;
import com.bridgelabz.fundoo.util.ResponseInfo;
import com.bridgelabz.fundoo.util.TokenGenerator;
import com.bridgelabz.fundoo.util.Utility;

@Service
@PropertySource({"classpath:status.properties", "classpath:application.properties"})
public class UserServiceImpl implements UserService {
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private MailService mailService;
	@Autowired
	private TokenGenerator tokenGenerator;
	@Autowired
	private ModelMapper modelMapper;
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	@Autowired
	private Environment environment;
	@Autowired
	private MessagePublisherImpl messagePublisherImpl;
	@Autowired
	private MessageConsumer messageConsumer;
	@Autowired
	private AmazonService amazonService;

	@Override
	public Response addUser(UserDTO userDTO) {
		if (isDuplicateUserByEmail(userDTO.getEmail()))
			throw new UserException(Integer.parseInt(environment.getProperty("status.register.errorCode")), environment.getProperty("status.register.duplicateEmailError"));
		// map the UserDTO to User
		User user = modelMapper.map(userDTO, User.class);
		user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
		user.setRegistrationDate(LocalDateTime.now());
		user.setModifiedDate(LocalDateTime.now());
		user.setVerified(false);
		try {
			user = userRepository.save(user);
		}
		catch(Exception e)
		{
			throw new UserException(Integer.parseInt(environment.getProperty("status.dataSaving.errorCode")),environment.getProperty("status.saveError"));
		}
		String userActivationLink = Utility.getHostPublicIP() + ":" +environment.getProperty("server.port") + "/user/useractivation/";
		userActivationLink = userActivationLink + tokenGenerator.generateUserToken(user.getId());
		//publish message to the queue in rabbitmq server
		messagePublisherImpl.publishMessage(userActivationLink);
		//consume message from the queue in rabbitmq server
		//for(int i=0;i<100000;i++);
		userActivationLink  = messageConsumer.getMessage();
		System.out.println("userActivationLink = "+userActivationLink);
		mailService.sendEmail(user.getEmail(),"User registration verification",userActivationLink);
		Response response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.success.code")),
				environment.getProperty("status.register.success"));
		return response;
	}

	@Override
	public Response verifyUser(String token) {
		Response response = null;
		long id = tokenGenerator.retrieveIdFromToken(token);
		User user = userRepository.findById(id).get();
		if (user == null)// check id is valid or not
			throw new UserException(Integer.parseInt(environment.getProperty("status.user.errorCode")),environment.getProperty("status.user.invalidUser"));
		//check for already verified or not
		if(!user.isVerified()) {
			user.setVerified(true);// set verification to true
			user.setModifiedDate(LocalDateTime.now());
			user = userRepository.save(user);// update to db
			if (user == null)
				throw new UserException(Integer.parseInt(environment.getProperty("status.dataSaving.errorCode")), environment.getProperty("status.saveError"));
			response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.success.code")),
					environment.getProperty("status.user.success"));
		}
		else
			response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.success.code")),"User is already verified");
		return response;
	}

	@Override
	public ResponseToken login(LoginDTO loginDTO) {
		Optional<User> user = userRepository.findUserByEmail(loginDTO.getEmail());
		if (user.isPresent() && bCryptPasswordEncoder.matches(loginDTO.getPassword(), user.get().getPassword())) {
			if (user.get().isVerified()) {
				String token = tokenGenerator.generateUserToken(user.get().getId());
				return ResponseInfo.getResponseToken(Integer.parseInt(environment.getProperty("status.success.code")),
						environment.getProperty("status.login.success"),token);
			}
			else // user is not verified yet
				throw new UserException(Integer.parseInt(environment.getProperty("status.login.errorCode")), environment.getProperty("status.login.unVerifiedUser"));
		}
		throw new UserException(Integer.parseInt(environment.getProperty("status.login.errorCode")), environment.getProperty("status.login.invalidUser"));
	}

	@Override
	public Response passwordRecovery(String email) {
		Optional<User> user = userRepository.findUserByEmail(email);
		if(!user.isPresent())
			throw new UserException(Integer.parseInt(environment.getProperty("status.forgotPassword.errorCode")), environment.getProperty("status.forgotPassword.invalidEmail"));
		String passwordResetLink = Utility.getHostPublicIP() + ":" +environment.getProperty("server.port")+"/user/resetpassword/";
		passwordResetLink = passwordResetLink + tokenGenerator.generateUserToken(user.get().getId());
		//publish message to the queue in rabbitmq server
		messagePublisherImpl.publishMessage(passwordResetLink);
		//consume message from the queue in rabbitmq server
		passwordResetLink  = messageConsumer.getMessage();
		mailService.sendEmail(user.get().getEmail(),"Password Recovery Link",passwordResetLink);
		Response response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.success.code")),
				environment.getProperty("status.forgotPassword.success"));
		return response;
	}

	@Override
	public Response resetPassword(String newPassword, String token) {
		long id = tokenGenerator.retrieveIdFromToken(token);
		Optional<User> user = userRepository.findById(id);
		if(!user.isPresent())
			throw new UserException(Integer.parseInt(environment.getProperty("status.user.errorCode")), environment.getProperty("status.user.invalidUser"));
		user.get().setPassword(bCryptPasswordEncoder.encode(newPassword));
		user.get().setModifiedDate(LocalDateTime.now());
		if(userRepository.save(user.get()) == null)
			throw new UserException(Integer.parseInt(environment.getProperty("status.dataSaving.errorCode")), environment.getProperty("status.saveError"));
		return ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.success.code")),environment.getProperty("status.resetPassword.success"));
	}
    
	@Override
	public boolean isDuplicateUserByEmail(String email) {
		if (userRepository.findUserByEmail(email).isPresent())
			return true;
		else
			return false;
	}

	@Override
	public Response uploadProfilePic(MultipartFile multipartFile, String token) {
		Long userId = tokenGenerator.retrieveIdFromToken(token);
		return amazonService.uploadFile(multipartFile, userId);
	}

	@Override
	public String getProfilePic(String token) {
		Long userId = tokenGenerator.retrieveIdFromToken(token);
		return amazonService.getProfilePicFromS3Bucket(userId);
	}
	
	
}
