package com.bridgelabz.fundoo.note.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.bridgelabz.fundoo.exception.LabelException;
import com.bridgelabz.fundoo.exception.UserException;
import com.bridgelabz.fundoo.note.dto.LabelDTO;
import com.bridgelabz.fundoo.note.model.Label;
import com.bridgelabz.fundoo.note.model.Note;
import com.bridgelabz.fundoo.note.repository.LabelRepository;
import com.bridgelabz.fundoo.note.repository.NoteRepository;
import com.bridgelabz.fundoo.response.Response;
import com.bridgelabz.fundoo.user.model.User;
import com.bridgelabz.fundoo.user.repository.UserRepository;
import com.bridgelabz.fundoo.util.ResponseInfo;
import com.bridgelabz.fundoo.util.TokenGenerator;

@Service
@PropertySource("classpath:status.properties")
public class LabelServiceImpl implements LabelService {
	@Autowired
	private TokenGenerator tokenGenerator;
	@Autowired
	private LabelRepository labelRepository;
	@Autowired
	private NoteRepository noteRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired 
	private ModelMapper modelMapper;
	@Autowired
	private Environment environment;
	private Response response;

	@Override
	public Label create(LabelDTO labelDTO, String userToken) throws UserException, LabelException{
		long userId = tokenGenerator.retrieveIdFromToken(userToken);
		Optional<User> opUser = userRepository.findById(userId);
		if(!(opUser.isPresent() && opUser.get().isVerified()))
			throw new UserException(Integer.parseInt(environment.getProperty("status.login.errorCode")),environment.getProperty("status.user.existError"));
		User user = opUser.get();
		if(labelRepository.findByNameAndUserAllIgnoreCase(labelDTO.getName().toLowerCase(), user).isPresent())
			throw new LabelException(environment.getProperty("status.label.duplicate.error"), Integer.parseInt(environment.getProperty("status.label.errorCode")));
		Label label = modelMapper.map(labelDTO, Label.class);
		label.setName(label.getName());
		label.setUser(user);
		label.setNotes(new HashSet<>());
		label.setCreatedDate(LocalDateTime.now());
		label.setModifiedDate(LocalDateTime.now());
		label = labelRepository.save(label);
		if(label == null)
			throw new LabelException(environment.getProperty("status.label.create.error"), Integer.parseInt(environment.getProperty("status.label.errorCode")));
		return label;
	}

	@Override
	public Response update(LabelDTO labelDTO, long id, String userToken) {
		long userId = tokenGenerator.retrieveIdFromToken(userToken);
		Optional<User> opUser = userRepository.findById(userId);
		if(!(opUser.isPresent() && opUser.get().isVerified()))
			return ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.login.errorCode")),environment.getProperty("status.user.existErrorr"));
		//check for non-existence
		if(!labelRepository.findByIdAndUser(id, opUser.get()).isPresent())
			return  ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.label.errorCode")), environment.getProperty("status.label.exists.error"));
		//check for duplicate entry
		if(labelRepository.findByNameAndUserAllIgnoreCase(labelDTO.getName(), opUser.get()).isPresent())
			response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.label.errorCode")), environment.getProperty("status.label.duplicate.error"));
		Label label = labelRepository.findByIdAndUser(id,opUser.get()).get();
		modelMapper.map(labelDTO, label);
		//label.setName(label.getName());
		label.setModifiedDate(LocalDateTime.now());
		label = labelRepository.save(label);
		if(label == null)
			response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.label.errorCode")), environment.getProperty("status.label.update.error"));
		else
			response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.success.code")),environment.getProperty("status.label.update.success"));
		return response;
	}

	@Override
	public Response delete(long id, String userToken) {
		long userId = tokenGenerator.retrieveIdFromToken(userToken);
		Optional<User> opUser = userRepository.findById(userId);
		if(!(opUser.isPresent() && opUser.get().isVerified()))
			return  ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.login.errorCode")),environment.getProperty("status.user.existError"));
		//check for non-existence
		Optional<Label> opLabel = labelRepository.findByIdAndUser(id, opUser.get());
		if(!opLabel.isPresent())
			return  ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.label.errorCode")), environment.getProperty("status.label.exists.error"));
		//remove the associated notes
		Label label = opLabel.get(); 
		Set<Note> associatedNotes = label.getNotes();
		for(Note n : associatedNotes) 
		{ 
			n.removeLabel(label);
			noteRepository.save(n);
		}
		labelRepository.deleteById(id);

		//check deletion is successful or not
		if(labelRepository.findByIdAndUser(id, opUser.get()).isPresent())
			response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.label.errorCode")),environment.getProperty("status.label.delete.error"));
		else
			response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.success.code")), environment.getProperty("status.label.delete.success"));
		return response;
	}

	@Override
	public Set<Label> getAllLabels(String userToken) throws UserException{
		long userId = tokenGenerator.retrieveIdFromToken(userToken);
		Optional<User> opUser = userRepository.findById(userId);
		if(!(opUser.isPresent() && opUser.get().isVerified()))
			throw new UserException(Integer.parseInt(environment.getProperty("status.login.errorCode")),environment.getProperty("status.user.existError"));
		return labelRepository.findAllByUser(opUser.get());
	}

	public Label getLabel(long id, String userToken) {
		long userId = tokenGenerator.retrieveIdFromToken(userToken);
		Optional<User> opUser = userRepository.findById(userId);
		Label label = labelRepository.findByIdAndUser(id, opUser.get()).get();
		return label;
	}
}
