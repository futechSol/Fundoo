package com.bridgelabz.fundoo.note.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
import com.bridgelabz.fundoo.user.service.MessageConsumer;
import com.bridgelabz.fundoo.user.service.MessagePublisher;
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
	@Autowired
	private MessagePublisher messagePublisherImpl;
	@Autowired
	private MessageConsumer messageConsumer;
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
		//note.setColor("");
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
		List<Note>collaboratedNotes = user.getCollaboratedNotes();
		Note noteToUpdate = null;
		if(noteRepository.findByIdAndUser(noteId, user).isPresent()) {
			noteToUpdate = noteRepository.findByIdAndUser(noteId, user).get();
		}
		else if(collaboratedNotes != null && collaboratedNotes.size() > 0) //collaborated notes
		{
			for(Note note : collaboratedNotes) {
				if(note.getId() == noteId) {
					noteToUpdate = note;
					break;
				}
			}
		}
		else if(noteToUpdate == null){
			return response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.note.errorCode")), environment.getProperty("status.note.exists.error"));
		}
		if(!(noteDTO.getTitle() == null || noteDTO.getTitle().equals("")))
			noteToUpdate.setTitle(noteDTO.getTitle());
		if(!(noteDTO.getDescription() == null || noteDTO.getDescription().equals("")))
			noteToUpdate.setDescription(noteDTO.getDescription());
		noteToUpdate.setModifiedDate(LocalDateTime.now());
		noteToUpdate = noteRepository.save(noteToUpdate);
		if(noteToUpdate == null)
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
		List<Note> allNotes = noteRepository.findAllByUser(user).stream().filter(u -> !(u.isArchived() || u.isTrashed())).collect(Collectors.toList());
		List<Note> collabNotes = user.getCollaboratedNotes();
		allNotes.addAll(collabNotes);
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
	@Override
	public Response addCollaborator(long noteId , String userToken, String email) {
		long ownerId = tokenGenerator.retrieveIdFromToken(userToken);
		User ownerUser = userRepository.findById(ownerId).get();
		Note note = noteRepository.findByIdAndUser(noteId,ownerUser).get();
		Optional<User> optionalUser =userRepository.findByEmail(email);
		if(optionalUser.isPresent() && optionalUser.get().isVerified()) {
			User user = userRepository.findByEmail(email).get();
			if(note.getCollaboratedUsers().contains(user))
				throw new UserException(Integer.parseInt(environment.getProperty("status.collaborator.errorCode")), environment.getProperty("status.collaborator.duplicateUser"));
			note.addCollaboratedUser(user);
			user.addCollaboratedNote(note);
			noteRepository.save(note);
			userRepository.save(user);
			messageConsumer.emailDetails(user, user.getFirstName() +" shared a note with you : "+note.getTitle());
			String message = "Note details \n note title = "+ note.getTitle()+"\n description = " +note.getDescription();
			messagePublisherImpl.publishMessage(message);
			return ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.success.code")),
					environment.getProperty("status.collaborator.success"));
		}
		//user not yet verified
		else if(optionalUser.isPresent() && !optionalUser.get().isVerified())
			throw new UserException(Integer.parseInt(environment.getProperty("status.login.errorCode")), environment.getProperty("status.login.unVerifiedUser"));
		else //throw user doesn't exists
			throw new UserException(Integer.parseInt(environment.getProperty("status.user.errorCode")), environment.getProperty("status.user.existError"));
	}
	@Override
	public Response removeCollaborator(long noteId , String userToken, String email) {
		long ownerId = tokenGenerator.retrieveIdFromToken(userToken);
		User ownerUser = userRepository.findById(ownerId).get();
		Note note = noteRepository.findByIdAndUser(noteId,ownerUser).get();
		User collaboratedUser = userRepository.findByEmail(email).get();
		if(collaboratedUser.getId() != ownerId ) {
			note.removeCollaboratedUser(collaboratedUser);
			userRepository.save(collaboratedUser);
			collaboratedUser.removeCollaboratedNote(note);
			noteRepository.save(note);
			return ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.success.code")),
					environment.getProperty("status.collaborator.remove.success"));
		}
		throw new UserException(Integer.parseInt(environment.getProperty("status.collaborator.errorCode")), environment.getProperty("status.collaborator.remove.error"));
	}
}
