package com.bridgelabz.fundoo.user.service;

import com.bridgelabz.fundoo.user.model.User;

public interface MessageConsumer {
	 void recieveMessage(String msg);
	 void emailDetails(User to, String subject);
}
