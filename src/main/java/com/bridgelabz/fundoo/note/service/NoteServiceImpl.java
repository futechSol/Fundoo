package com.bridgelabz.fundoo.note.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.bridgelabz.fundoo.exception.NoteException;
import com.bridgelabz.fundoo.exception.UserException;
import com.bridgelabz.fundoo.note.dto.LabelDTO;
import com.bridgelabz.fundoo.note.dto.NoteDTO;
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
public class NoteServiceImpl implements NoteService{
	@Autowired
	private TokenGenerator tokenGenerator;
	@Autowired
	private NoteRepository noteRepository;
	@Autowired
	private LabelRepository labelRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired 
	private ModelMapper modelMapper;
	@Autowired
	private Environment environment;
	private Response response;
	@Override
	public Response create(NoteDTO noteDTO, String userToken) {
		long userId = tokenGenerator.retrieveIdFromToken(userToken);
		Optional<User> opUser = userRepository.findById(userId);
		if(!(opUser.isPresent() && opUser.get().isVerified()))
			return response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.login.errorCode")), environment.getProperty("status.user.existError"));
		User user = opUser.get();
		Note note = modelMapper.map(noteDTO, Note.class);
		note.setUser(user);
		note.setCreatedDate(LocalDateTime.now());
		note.setModifiedDate(LocalDateTime.now());
		note = noteRepository.save(note);
		if(note == null)
			response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.note.errorCode")), environment.getProperty("status.note.errorMessage"));
		else
			response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.success.code")),
				environment.getProperty("status.note.create.success"));
		return response;
	}

	@Override
	public Response update(NoteDTO noteDTO, String userToken, long noteId) {
		long userId = tokenGenerator.retrieveIdFromToken(userToken);
		Optional<User> opUser = userRepository.findById(userId);
		if(!(opUser.isPresent() && opUser.get().isVerified()))
			return response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.login.errorCode")), environment.getProperty("status.user.existError"));
		User user = opUser.get();
		if(!noteRepository.findByIdAndUser(noteId, user).isPresent())
			return response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.note.errorCode")), environment.getProperty("status.note.exists.error"));
		Note note = noteRepository.findByIdAndUser(noteId, user).get();
		modelMapper.map(noteDTO, note);
		note.setModifiedDate(LocalDateTime.now());
		note = noteRepository.save(note);
		if(note == null)
			response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.note.errorCode")), environment.getProperty("status.note.update.error"));
		else 
			response =  ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.success.code")),
				environment.getProperty("status.note.update.success"));
		return response;
	}

	@Override
	public Response delete(String userToken, long noteId){
		long userId = tokenGenerator.retrieveIdFromToken(userToken);
		Optional<User> opUser = userRepository.findById(userId);
		if(!(opUser.isPresent() && opUser.get().isVerified()))
			return response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.login.errorCode")), environment.getProperty("status.user.existError"));
		User user = opUser.get();

		if(!noteRepository.findByIdAndUser(noteId, user).isPresent())
			return response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.note.errorCode")), environment.getProperty("status.note.exists.error"));
		noteRepository.deleteById(noteId);
		if(noteRepository.findByIdAndUser(noteId, user).isPresent())
			response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.note.errorCode")), environment.getProperty("status.note.delete.error"));
		else
			response =  ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.success.code")),environment.getProperty("status.note.delete.success"));
		return response;
	}

	@Override
	public Note getNote(String userToken, long  noteId) throws NoteException, UserException{
		long userId = tokenGenerator.retrieveIdFromToken(userToken);
		Optional<User> opUser = userRepository.findById(userId);
		if(!(opUser.isPresent() && opUser.get().isVerified()))
			throw new UserException(Integer.parseInt(environment.getProperty("status.login.errorCode")),environment.getProperty("status.user.existError"));
		User user = opUser.get();
		Optional<Note> note = noteRepository.findByIdAndUser(noteId,user);
		if(!note.isPresent())
			throw new NoteException(Integer.parseInt(environment.getProperty("status.note.errorCode")), environment.getProperty("status.note.exists.error"));
		return note.get();
	}

	@Override
	public List<Note> getAllNotes(String userToken) throws UserException{
		long userId = tokenGenerator.retrieveIdFromToken(userToken);
		Optional<User> opUser = userRepository.findById(userId);
		if(!(opUser.isPresent() && opUser.get().isVerified()))
			throw new UserException(Integer.parseInt(environment.getProperty("status.login.errorCode")), environment.getProperty("status.user.existError"));
		User user = opUser.get();
		List<Note> allNotes = noteRepository.findAllByUser(user);
		return allNotes;
	}

	@Override
	public Response pinNote(String userToken, long noteId) {
		long userId = tokenGenerator.retrieveIdFromToken(userToken);
		Optional<User> opUser = userRepository.findById(userId);
		if(!(opUser.isPresent() && opUser.get().isVerified()))
			return response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.login.errorCode")), environment.getProperty("status.user.existError"));
		User user = opUser.get();
		if(!noteRepository.findByIdAndUser(noteId,user).isPresent())
			return response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.note.errorCode")), environment.getProperty("status.note.exists.error"));
		Note note = noteRepository.findByIdAndUser(noteId,user).get();
		note.setPinned(!note.isPinned());
		note =	noteRepository.save(note);
		if(note == null)
			response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.note.errorCode")),environment.getProperty("status.note.pinned.error"));
		else
			response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.success.code")),
				environment.getProperty("status.note.pinned.success"));
		return response;
	}

	@Override
	public Response trashNote(String userToken, long noteId) {
		long userId = tokenGenerator.retrieveIdFromToken(userToken);
		Optional<User> opUser = userRepository.findById(userId);
		if(!(opUser.isPresent() && opUser.get().isVerified()))
			return response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.login.errorCode")), environment.getProperty("status.user.existError"));
		User user = opUser.get();
		if(!noteRepository.findByIdAndUser(noteId,user).isPresent())
			return response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.note.errorCode")),environment.getProperty("status.note.exists.error"));
		Note note = noteRepository.findByIdAndUser(noteId,user).get();
		note.setTrashed(!note.isTrashed());
		note = noteRepository.save(note);
		if(note == null)
			response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.note.errorCode")),environment.getProperty("status.note.trashed.error"));
		else
			response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.success.code")),
				environment.getProperty("status.note.trashed.success"));
		return response;
	}

	@Override
	public Response archiveNote(String userToken, long noteId) {
		long userId = tokenGenerator.retrieveIdFromToken(userToken);
		Optional<User> opUser = userRepository.findById(userId);
		if(!(opUser.isPresent() && opUser.get().isVerified()))
			return response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.login.errorCode")),environment.getProperty("status.user.existError"));
		User user = opUser.get();
		if(!noteRepository.findByIdAndUser(noteId,user).isPresent())
			return response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.note.errorCode")), environment.getProperty("status.note.exists.error"));
		Note note = noteRepository.findByIdAndUser(noteId,user).get();
		note.setArchived(!note.isArchived());
		note = noteRepository.save(note);
		if(note == null)
			response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.note.errorCode")),environment.getProperty("status.note.exists.error"));
		else
			response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.success.code")),
				environment.getProperty("status.note.archived.success"));
		return response;
	}

	@Override
	public Response addLabel(String userToken, long noteId,  LabelDTO labelDTO) {
		long userId = tokenGenerator.retrieveIdFromToken(userToken);
		Optional<User> opUser = userRepository.findById(userId);
		if(!(opUser.isPresent() && opUser.get().isVerified()))
			return response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.login.errorCode")),environment.getProperty("status.user.existError"));
		User user = opUser.get();
		if(!noteRepository.findByIdAndUser(noteId,user).isPresent())
			return response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.note.errorCode")), environment.getProperty("status.note.exists.error"));
		Note note = noteRepository.findByIdAndUser(noteId,user).get();
		Label label = modelMapper.map(labelDTO, Label.class);
		Optional<Label> opLabel = labelRepository.findByNameAndUserAllIgnoreCase(labelDTO.getName(), user);
		if(opLabel.isPresent()) {
			label = opLabel.get();
		}
		else {//add a new label
			label.setCreatedDate(LocalDateTime.now());
			label.setModifiedDate(LocalDateTime.now());
			label.setUser(user);
			label.addNote(note);//cehck it
			label = labelRepository.save(label);
		}
		note.addLabel(label);
		note = noteRepository.save(note);
		if(note == null)
			response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.note.errorCode")),environment.getProperty("status.note.update.error"));
		else
			response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.success.code")),
				environment.getProperty("status.note.addLabel.success"));
		return response;
	}

	@Override
	public Response removeLabel(String userToken, long noteId, LabelDTO labelDTO) {
		long userId = tokenGenerator.retrieveIdFromToken(userToken);
		Optional<User> opUser = userRepository.findById(userId);
		if(!(opUser.isPresent() && opUser.get().isVerified()))
			return response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.login.errorCode")),environment.getProperty("status.user.existError"));
		User user = opUser.get();
		if(!noteRepository.findByIdAndUser(noteId,user).isPresent())
			return response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.note.errorCode")), environment.getProperty("status.note.exists.error"));
		Note note = noteRepository.findByIdAndUser(noteId,user).get();
		Label label = labelRepository.findByNameAndUserAllIgnoreCase(labelDTO.getName(), user).get();
		note.removeLabel(label);
		note = noteRepository.save(note);
		if(note == null)
			response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.note.errorCode")),environment.getProperty("status.note.update.error"));
		else
			response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.success.code")),
				environment.getProperty("status.note.removeLabel.success"));
		return response;
	}
	
	public Response addReminder(String userToken, long noteId,String reminder) {
		long userId = tokenGenerator.retrieveIdFromToken(userToken);
		User user = userRepository.findById(userId).get();
		Note note = noteRepository.findByIdAndUser(noteId,user).get();
		note.setReminder(reminder);
		note = noteRepository.save(note);
		if(note == null)
			response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.note.errorCode")),environment.getProperty("status.note.update.error"));
		else {
			response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.success.code")),
				environment.getProperty("status.note.addReminder.success"));
		}
		return response;
	}
	
	public Response removeReminder(String userToken, long noteId) {
		long userId = tokenGenerator.retrieveIdFromToken(userToken);
		User user = userRepository.findById(userId).get();
		Note note = noteRepository.findByIdAndUser(noteId,user).get();
		note.setReminder(null);
		note = noteRepository.save(note);
		if(note == null)
			response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.note.errorCode")),environment.getProperty("status.note.update.error"));
		else {
			response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.success.code")),
				environment.getProperty("status.note.removeReminder.success"));
		}
		return response;
	}
}