package com.bridgelabz.fundoo.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.bridgelabz.fundoo.exception.LabelException;
import com.bridgelabz.fundoo.note.dto.LabelDTO;


public class Utility {
	private static final Logger logger = LoggerFactory.getLogger(Utility.class);
    
	/**
	 * get the server local IP address
	 * @return local IP address
	 */
	public static String getLocalHostIPaddress() {
		String localIP = "";
		try {
			InetAddress localhost = InetAddress.getLocalHost();
			localIP = localhost.getHostAddress().trim();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} 
		return localIP;
	}
	
	/**
	 * get the server public IP address
	 * @return public IP
	 */
	public static String getHostPublicIP() {
		// Find public IP address 
		String systemipaddress = ""; 
		try
		{  
			URL url_name = new URL("http://bot.whatismyipaddress.com"); 
			BufferedReader sc = new BufferedReader(new InputStreamReader(url_name.openStream())); 
			// reads system IPAddress 
			systemipaddress = sc.readLine().trim(); 
		} 
		catch (Exception e) 
		{ 
			systemipaddress = "Cannot Execute Properly"; 
		} 
		logger.info("Host Public IP : " + systemipaddress);
		logger.trace("Note Creation");
		return systemipaddress;
	}

	/**
	 * validates a 10-digit mobile number
	 * @param mobileNumber mobile number as a string 
	 * @return true if mobile number is valid else false
	 */
	public static boolean validateMobileNumber(String mobileNumber) {
		String regex = "^[7-9][0-9]{9}$";
		return mobileNumber.matches(regex);
	}

	/**
	 * validates an email
	 * @param email email string
	 * @return true of email is valid else false
	 */
	public static boolean validateEmail(String email) {
		String EMAIL_REGEX = "^[\\w-\\+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-z]{2,})$";
		Pattern pattern = Pattern.compile(EMAIL_REGEX, Pattern.CASE_INSENSITIVE);
		Matcher matcher;
		matcher = pattern.matcher(email);
		return matcher.matches();
	}

	public static void validateLabelDTO(LabelDTO label) {
		if(label.getName().equals("") || label.getName() == null)
			throw new LabelException("label should not be empty", -10);
	}
}
