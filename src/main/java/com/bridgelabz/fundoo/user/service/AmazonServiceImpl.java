package com.bridgelabz.fundoo.user.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;
import javax.annotation.PostConstruct;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.bridgelabz.fundoo.response.Response;
import com.bridgelabz.fundoo.user.model.User;
import com.bridgelabz.fundoo.user.repository.UserRepository;
import com.bridgelabz.fundoo.util.ResponseInfo;


@Service
@PropertySource({"classpath:status.properties", "classpath:application.properties"})
public class AmazonServiceImpl implements AmazonService {
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private Environment environment;

	private AmazonS3 amazonS3Client; 
	
	@Value("${aws.s3.accesskey}")
	private String accessKey;
	@Value("${aws.s3.secretKey}")
	private String secretKey; 
	@Value("${aws.s3.bucket}")
	private String bucketName;
	@Value("${aws.s3.endpointUrl}")
	private String endpointUrl;

	@PostConstruct
	private void initializeAmazonClient() {
		//this.amazonS3Client = new AmazonS3Client(new BasicAWSCredentials(ConstantUtils.ACCESS_KEY, ConstantUtils.SECRET_KEY));
		BasicAWSCredentials creds = new BasicAWSCredentials(this.accessKey, this.secretKey);
		amazonS3Client = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(creds)).withRegion(Regions.AP_SOUTH_1).build();
	}
	@Override
	public Response uploadFile(MultipartFile multipartFile, Long userId) {
		try {
			File file = convertFromMultipartFileToFile(multipartFile);
			String fileName = generateUniqueFileName(multipartFile);
			//fileUrl = endpointUrl + "/" + bucketName + "/" + fileName;
			User user = userRepository.findById(userId).get();
			//delete the old profile pic if it exists
			deleteProfilePicFromS3Bucket(user.getProfilePic());
			uploadImageFileTos3bucket(fileName, file);
			user.setProfilePic(fileName);
			userRepository.save(user);
			file.delete();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.success.code")), environment.getProperty("status.user.uploadPic.success"));
	}

	public String getProfilePicFromS3Bucket(Long userId) {
		User user = userRepository.findById(userId).get();
		String fileName = user.getProfilePic();
		//S3Object s3image = amazonS3Client.getObject(new GetObjectRequest(bucketName, fileName));
		//convert S3Object(image) file to Base64 string and return
		//return convertImageToBase64(s3image);
		return getSignedImgUrl(fileName);
	}
	private String getSignedImgUrl(String key) {
        try {
            GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, key);
            generatePresignedUrlRequest.setMethod(HttpMethod.GET);
            generatePresignedUrlRequest.setExpiration(DateTime.now().plusMinutes(2).toDate());
            URL signedUrl = amazonS3Client.generatePresignedUrl(generatePresignedUrlRequest);
            return signedUrl.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "error while generating url";
        }
    }
	/**
	 * converts MultiPartFile to File
	 * @param multipartFile
	 * @return File
	 * @throws IOException
	 */
	private File convertFromMultipartFileToFile(MultipartFile multipartFile) throws IOException {
		File file = new File(multipartFile.getOriginalFilename());
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(multipartFile.getBytes());
		fos.close();
		return file;
	}

	/**
	 * generates unique name to the uploaded file
	 * @param multiPart
	 * @return uniqueFileName
	 */
	private String generateUniqueFileName(MultipartFile multiPart) {
		return UUID.randomUUID().toString();
	}

	/**
	 * upload files to AWS S3 bucket
	 * @param fileName key for image in S3 Bucket
	 * @param file file to be uploaded
	 */
	private void uploadImageFileTos3bucket(String fileName, File file) {
		amazonS3Client.putObject(bucketName, fileName, file);
		//putObject(new PutObjectRequest(bucketName, fileName, file).withCannedAcl(CannedAccessControlList.PublicRead));
	}

	private void deleteProfilePicFromS3Bucket(String fileName) {
		if(fileName != null && !fileName.equals("")) { 
		amazonS3Client.deleteObject(new DeleteObjectRequest(bucketName, fileName));
		}
	}

	/*
	 * private String getFileName(String fileURL) { return
	 * fileURL.substring(fileURL.lastIndexOf("/")+1); }
	 */

	/*
	 * private String convertImageToBase64(S3Object s3obj) { String encodedfile =
	 * null; byte[] bytes; try { S3ObjectInputStream in = s3obj.getObjectContent();
	 * bytes = IOUtils.toByteArray(in); encodedfile =
	 * Base64.getEncoder().encodeToString(bytes); in.close(); } catch (IOException
	 * e) { e.printStackTrace(); } return encodedfile; }
	 */
}
