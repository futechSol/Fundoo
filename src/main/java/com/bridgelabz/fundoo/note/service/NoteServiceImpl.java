package com.bridgelabz.fundoo.note.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import com.bridgelabz.fundoo.exception.NoteException;
import com.bridgelabz.fundoo.exception.UserException;
import com.bridgelabz.fundoo.note.dto.LabelDTO;
import com.bridgelabz.fundoo.note.dto.NoteDTO;
import com.bridgelabz.fundoo.note.dto.ReminderDTO;
import com.bridgelabz.fundoo.note.model.Label;
import com.bridgelabz.fundoo.note.model.Note;
import com.bridgelabz.fundoo.note.model.NoteContainer;
import com.bridgelabz.fundoo.note.model.NoteOperation;
import com.bridgelabz.fundoo.note.repository.LabelRepository;
import com.bridgelabz.fundoo.note.repository.NoteRepository;
import com.bridgelabz.fundoo.response.Response;
import com.bridgelabz.fundoo.user.model.User;
import com.bridgelabz.fundoo.user.repository.UserRepository;
import com.bridgelabz.fundoo.user.service.MailService;
import com.bridgelabz.fundoo.user.service.MessagePublisher;
import com.bridgelabz.fundoo.util.ResponseInfo;
import com.bridgelabz.fundoo.util.TokenGenerator;

@Service
@PropertySource("classpath:status.properties")
public class NoteServiceImpl implements NoteService {

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
	private MessagePublisher messagePublisher;
	@Autowired
	private MailService mailService;
	@Autowired
	private NoteElasticSearch noteElasticSearch;
    private NoteContainer noteContainer;
	private Response response;

	@Override
	public Response create(NoteDTO noteDTO, String userToken) {
		long userId = tokenGenerator.retrieveIdFromToken(userToken);
		Optional<User> opUser = userRepository.findById(userId);
		if (!(opUser.isPresent() && opUser.get().isVerified()))
			return response = ResponseInfo.getResponse(
					Integer.parseInt(environment.getProperty("status.login.errorCode")),
					environment.getProperty("status.user.existError"));
		User user = opUser.get();
		Note note = modelMapper.map(noteDTO, Note.class);
		note.setUser(user);
		note.setColor("white");
		note.setCreatedDate(LocalDateTime.now());
		note.setModifiedDate(LocalDateTime.now());
		note = noteRepository.save(note);

		if (note == null) {
			response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.note.errorCode")),
					environment.getProperty("status.note.errorMessage"));
		}else {
			noteContainer = new NoteContainer();
			noteContainer.setNote(note);
			noteContainer.setNoteOperation(NoteOperation.CREATE);
			messagePublisher.publishNoteData(noteContainer);
			response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.success.code")),
					environment.getProperty("status.note.create.success"));
		}
		return response;
	}

	@Override
	public Response update(NoteDTO noteDTO, String userToken, long noteId) {
		long userId = tokenGenerator.retrieveIdFromToken(userToken);
		Optional<User> opUser = userRepository.findById(userId);
		if (!(opUser.isPresent() && opUser.get().isVerified()))
			return response = ResponseInfo.getResponse(
					Integer.parseInt(environment.getProperty("status.login.errorCode")),
					environment.getProperty("status.user.existError"));
		User user = opUser.get();
		List<Note> collaboratedNotes = user.getCollaboratedNotes();
		Note noteToUpdate = null;
		if (noteRepository.findByIdAndUser(noteId, user).isPresent()) {
			noteToUpdate = noteRepository.findByIdAndUser(noteId, user).get();
		} else if (collaboratedNotes != null && collaboratedNotes.size() > 0) // collaborated notes
		{
			for (Note note : collaboratedNotes) {
				if (note.getId() == noteId) {
					noteToUpdate = note;
					break;
				}
			}
		} else if (noteToUpdate == null) {
			return response = ResponseInfo.getResponse(
					Integer.parseInt(environment.getProperty("status.note.errorCode")),
					environment.getProperty("status.note.exists.error"));
		}
		if (!(noteDTO.getTitle() == null || noteDTO.getTitle().equals("")))
			noteToUpdate.setTitle(noteDTO.getTitle());
		if (!(noteDTO.getDescription() == null || noteDTO.getDescription().equals("")))
			noteToUpdate.setDescription(noteDTO.getDescription());
		noteToUpdate.setModifiedDate(LocalDateTime.now());
		noteToUpdate = noteRepository.save(noteToUpdate);
		if (noteToUpdate == null)
			response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.note.errorCode")),
					environment.getProperty("status.note.update.error"));
		else {
			noteContainer = new NoteContainer();
			noteContainer.setNote(noteToUpdate);
			noteContainer.setNoteOperation(NoteOperation.UPDATE);
			messagePublisher.publishNoteData(noteContainer);
			response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.success.code")),
					environment.getProperty("status.note.update.success"));
		}
			return response;
	}

	@Override
	public Response delete(String userToken, long noteId) {
		long userId = tokenGenerator.retrieveIdFromToken(userToken);
		Optional<User> opUser = userRepository.findById(userId);
		if (!(opUser.isPresent() && opUser.get().isVerified()))
			return response = ResponseInfo.getResponse(
					Integer.parseInt(environment.getProperty("status.login.errorCode")),
					environment.getProperty("status.user.existError"));
		User user = opUser.get();
        Optional<Note> opNote = noteRepository.findByIdAndUser(noteId, user);
		if (!opNote.isPresent())
			return response = ResponseInfo.getResponse(
					Integer.parseInt(environment.getProperty("status.note.errorCode")),
					environment.getProperty("status.note.exists.error"));
		noteRepository.deleteById(noteId);
		if (noteRepository.findByIdAndUser(noteId, user).isPresent())
			response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.note.errorCode")),
					environment.getProperty("status.note.delete.error"));
		else {
			noteContainer = new NoteContainer();
			noteContainer.setNote(opNote.get());
			noteContainer.setNoteOperation(NoteOperation.DELETE);
			messagePublisher.publishNoteData(noteContainer);

			response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.success.code")),
					environment.getProperty("status.note.delete.success"));
		}
		return response;
	}

	@Override
	public Object getNote(String userToken, long noteId) throws NoteException, UserException {
		long userId = tokenGenerator.retrieveIdFromToken(userToken);
		Optional<User> opUser = userRepository.findById(userId);
		if (!(opUser.isPresent() && opUser.get().isVerified()))
			throw new UserException(Integer.parseInt(environment.getProperty("status.login.errorCode")),
					environment.getProperty("status.user.existError"));
		User user = opUser.get();
		Optional<Note> note = noteRepository.findByIdAndUser(noteId, user);
		if (!note.isPresent())
			throw new NoteException(Integer.parseInt(environment.getProperty("status.note.errorCode")),
					environment.getProperty("status.note.exists.error"));
		//return note.get();
		return noteElasticSearch.getNoteById(String.valueOf(noteId));
	}

	@Override
	public List<Note> getAllNotes(String userToken) throws UserException {
		long userId = tokenGenerator.retrieveIdFromToken(userToken);
		Optional<User> opUser = userRepository.findById(userId);
		if (!(opUser.isPresent() && opUser.get().isVerified()))
			throw new UserException(Integer.parseInt(environment.getProperty("status.login.errorCode")),
					environment.getProperty("status.user.existError"));
		User user = opUser.get();
		List<Note> allNotes = noteRepository.findAllByUser(user).stream()
				.filter(u -> !(u.isArchived() || u.isTrashed())).collect(Collectors.toList());
		List<Note> collabNotes = user.getCollaboratedNotes();
		allNotes.addAll(collabNotes);
		return allNotes;
	}

	@Override
	public Response pinNote(String userToken, long noteId) {
		long userId = tokenGenerator.retrieveIdFromToken(userToken);
		Optional<User> opUser = userRepository.findById(userId);
		if (!(opUser.isPresent() && opUser.get().isVerified()))
			return response = ResponseInfo.getResponse(
					Integer.parseInt(environment.getProperty("status.login.errorCode")),
					environment.getProperty("status.user.existError"));
		User user = opUser.get();
		if (!noteRepository.findByIdAndUser(noteId, user).isPresent())
			return response = ResponseInfo.getResponse(
					Integer.parseInt(environment.getProperty("status.note.errorCode")),
					environment.getProperty("status.note.exists.error"));
		Note note = noteRepository.findByIdAndUser(noteId, user).get();
		note.setPinned(!note.isPinned());
		note = noteRepository.save(note);
		if (note == null)
			response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.note.errorCode")),
					environment.getProperty("status.note.pinned.error"));
		else {
			noteContainer = new NoteContainer();
			noteContainer.setNote(note);
			noteContainer.setNoteOperation(NoteOperation.UPDATE);
			messagePublisher.publishNoteData(noteContainer);
			response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.success.code")),
					environment.getProperty("status.note.pinned.success"));
		}
			return response;
	}

	@Override
	public Response trashNote(String userToken, long noteId) {
		long userId = tokenGenerator.retrieveIdFromToken(userToken);
		Optional<User> opUser = userRepository.findById(userId);
		if (!(opUser.isPresent() && opUser.get().isVerified()))
			return response = ResponseInfo.getResponse(
					Integer.parseInt(environment.getProperty("status.login.errorCode")),
					environment.getProperty("status.user.existError"));
		User user = opUser.get();
		if (!noteRepository.findByIdAndUser(noteId, user).isPresent())
			return response = ResponseInfo.getResponse(
					Integer.parseInt(environment.getProperty("status.note.errorCode")),
					environment.getProperty("status.note.exists.error"));
		Note note = noteRepository.findByIdAndUser(noteId, user).get();
		note.setTrashed(!note.isTrashed());
		note = noteRepository.save(note);
		if (note == null)
			response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.note.errorCode")),
					environment.getProperty("status.note.trashed.error"));
		else {
			noteContainer = new NoteContainer();
			noteContainer.setNote(note);
			noteContainer.setNoteOperation(NoteOperation.UPDATE);
			messagePublisher.publishNoteData(noteContainer);
			response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.success.code")),
					environment.getProperty("status.note.trashed.success"));
		}
			return response;
	}

	@Override
	public Response archiveNote(String userToken, long noteId) {
		long userId = tokenGenerator.retrieveIdFromToken(userToken);
		Optional<User> opUser = userRepository.findById(userId);
		if (!(opUser.isPresent() && opUser.get().isVerified()))
			return response = ResponseInfo.getResponse(
					Integer.parseInt(environment.getProperty("status.login.errorCode")),
					environment.getProperty("status.user.existError"));
		User user = opUser.get();
		if (!noteRepository.findByIdAndUser(noteId, user).isPresent())
			return response = ResponseInfo.getResponse(
					Integer.parseInt(environment.getProperty("status.note.errorCode")),
					environment.getProperty("status.note.exists.error"));
		Note note = noteRepository.findByIdAndUser(noteId, user).get();
		note.setArchived(!note.isArchived());
		note = noteRepository.save(note);
		if (note == null)
			response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.note.errorCode")),
					environment.getProperty("status.note.archived.error"));
		else {
			noteContainer = new NoteContainer();
			noteContainer.setNote(note);
			noteContainer.setNoteOperation(NoteOperation.UPDATE);
			messagePublisher.publishNoteData(noteContainer);
			response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.success.code")),
					environment.getProperty("status.note.archived.success"));
		}
		return response;
	}
	@Override
	public Response addColor(String userToken, long noteId, String color) {
		long userId = tokenGenerator.retrieveIdFromToken(userToken);
		Optional<User> opUser = userRepository.findById(userId);
		if (!(opUser.isPresent() && opUser.get().isVerified()))
			return response = ResponseInfo.getResponse(
					Integer.parseInt(environment.getProperty("status.login.errorCode")),
					environment.getProperty("status.user.existError"));
		User user = opUser.get();
		if (!noteRepository.findByIdAndUser(noteId, user).isPresent())
			return response = ResponseInfo.getResponse(
					Integer.parseInt(environment.getProperty("status.note.errorCode")),
					environment.getProperty("status.note.exists.error"));
		Note note = noteRepository.findByIdAndUser(noteId, user).get();
		note.setColor(color);
		note = noteRepository.save(note);
		if (note == null)
			response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.note.errorCode")),
					environment.getProperty("status.note.color.error"));
		else {
			noteContainer = new NoteContainer();
			noteContainer.setNote(note);
			noteContainer.setNoteOperation(NoteOperation.UPDATE);
			messagePublisher.publishNoteData(noteContainer);
			response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.success.code")),
					environment.getProperty("status.note.color.success"));
		}
		return response;
	}

	@Override
	public Response addLabel(String userToken, long noteId, LabelDTO labelDTO) {
		long userId = tokenGenerator.retrieveIdFromToken(userToken);
		Optional<User> opUser = userRepository.findById(userId);
		if (!(opUser.isPresent() && opUser.get().isVerified()))
			return response = ResponseInfo.getResponse(
					Integer.parseInt(environment.getProperty("status.login.errorCode")),
					environment.getProperty("status.user.existError"));
		User user = opUser.get();
		if (!noteRepository.findByIdAndUser(noteId, user).isPresent())
			return response = ResponseInfo.getResponse(
					Integer.parseInt(environment.getProperty("status.note.errorCode")),
					environment.getProperty("status.note.exists.error"));
		Note note = noteRepository.findByIdAndUser(noteId, user).get();
		Label label = modelMapper.map(labelDTO, Label.class);
		Optional<Label> opLabel = labelRepository.findByNameAndUserAllIgnoreCase(labelDTO.getName(), user);
		if (opLabel.isPresent()) {
			label = opLabel.get();
		} else {// add a new label
			label.setCreatedDate(LocalDateTime.now());
			label.setModifiedDate(LocalDateTime.now());
			label.setUser(user);
			label.addNote(note);// cehck it
			label = labelRepository.save(label);
		}
		note.addLabel(label);
		note = noteRepository.save(note);
		if (note == null)
			response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.note.errorCode")),
					environment.getProperty("status.note.update.error"));
		else {
			noteContainer = new NoteContainer();
			noteContainer.setNote(note);
			noteContainer.setNoteOperation(NoteOperation.UPDATE);
			messagePublisher.publishNoteData(noteContainer);
			response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.success.code")),
					environment.getProperty("status.note.addLabel.success"));
		}
			return response;
	}

	@Override
	public Response removeLabel(String userToken, long noteId, LabelDTO labelDTO) {
		long userId = tokenGenerator.retrieveIdFromToken(userToken);
		Optional<User> opUser = userRepository.findById(userId);
		if (!(opUser.isPresent() && opUser.get().isVerified()))
			return response = ResponseInfo.getResponse(
					Integer.parseInt(environment.getProperty("status.login.errorCode")),
					environment.getProperty("status.user.existError"));
		User user = opUser.get();
		if (!noteRepository.findByIdAndUser(noteId, user).isPresent())
			return response = ResponseInfo.getResponse(
					Integer.parseInt(environment.getProperty("status.note.errorCode")),
					environment.getProperty("status.note.exists.error"));
		Note note = noteRepository.findByIdAndUser(noteId, user).get();
		Label label = labelRepository.findByNameAndUserAllIgnoreCase(labelDTO.getName(), user).get();
		note.removeLabel(label);
		note = noteRepository.save(note);
		if (note == null)
			response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.note.errorCode")),
					environment.getProperty("status.note.update.error"));
		else {
			noteContainer = new NoteContainer();
			noteContainer.setNote(note);
			noteContainer.setNoteOperation(NoteOperation.UPDATE);
			messagePublisher.publishNoteData(noteContainer);
			response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.success.code")),
					environment.getProperty("status.note.removeLabel.success"));
		}
		return response;
	}

	public Response addReminder(String userToken, long noteId, ReminderDTO reminderDTO) {
		long userId = tokenGenerator.retrieveIdFromToken(userToken);
		User user = userRepository.findById(userId).get();
		Note note = noteRepository.findByIdAndUser(noteId, user).get();
		note.setReminder(reminderDTO.getReminder());
		note.setRepeatReminder(reminderDTO.getRepeatReminder());
		note = noteRepository.save(note);
		if (note == null)
			response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.note.errorCode")),
					environment.getProperty("status.note.update.error"));
		else {
			noteContainer = new NoteContainer();
			noteContainer.setNote(note);
			noteContainer.setNoteOperation(NoteOperation.UPDATE);
			messagePublisher.publishNoteData(noteContainer);
			response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.success.code")),
					environment.getProperty("status.note.addReminder.success"));
		}
		return response;
	}

	public Response removeReminder(String userToken, long noteId) {
		long userId = tokenGenerator.retrieveIdFromToken(userToken);
		User user = userRepository.findById(userId).get();
		Note note = noteRepository.findByIdAndUser(noteId, user).get();
		note.setReminder(null);
		note.setRepeatReminder(null);
		note = noteRepository.save(note);
		if (note == null)
			response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.note.errorCode")),
					environment.getProperty("status.note.update.error"));
		else {
			noteContainer = new NoteContainer();
			noteContainer.setNote(note);
			noteContainer.setNoteOperation(NoteOperation.UPDATE);
			messagePublisher.publishNoteData(noteContainer);
			response = ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.success.code")),
					environment.getProperty("status.note.removeReminder.success"));
		}
		return response;
	}

	@Override
	public Response addCollaborator(long noteId, String userToken, String email) {
		long userId = tokenGenerator.retrieveIdFromToken(userToken);
		User user = userRepository.findById(userId).get();
		Note note;
		if (noteRepository.findByIdAndUser(noteId, user).isPresent())
			note = noteRepository.findByIdAndUser(noteId, user).get();
		else// its a collaborated note shared by other user
			note = user.getCollaboratedNotes().stream().filter(n -> n.getId() == noteId).findFirst().get();
		Optional<User> opUser = userRepository.findByEmail(email);
		if (opUser.isPresent() && opUser.get().isVerified()) {
			User collaboratedUser = userRepository.findByEmail(email).get();
			if (note.getCollaboratedUsers().contains(collaboratedUser))
				throw new UserException(Integer.parseInt(environment.getProperty("status.collaborator.errorCode")),
						environment.getProperty("status.collaborator.duplicateUser"));
			note.addCollaboratedUser(collaboratedUser);
			collaboratedUser.addCollaboratedNote(note);
			note = noteRepository.save(note);
			userRepository.save(collaboratedUser);
			String message = "Note details \n note title = " + note.getTitle() + "\n description = "
					+ note.getDescription();
			SimpleMailMessage mail = mailService.getMailMessageObject(user.getEmail(),
					user.getFirstName() + " shared a note with you : " + note.getTitle(), message);
			messagePublisher.publishUserMail(mail);
			//updating the corresponding note in ElasticSearch
			noteContainer = new NoteContainer();
			noteContainer.setNote(note);
			noteContainer.setNoteOperation(NoteOperation.UPDATE);
			messagePublisher.publishNoteData(noteContainer);
			return ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.success.code")),
					environment.getProperty("status.collaborator.success"));
		}
		// user not yet verified
		else if (opUser.isPresent() && !opUser.get().isVerified())
			throw new UserException(Integer.parseInt(environment.getProperty("status.login.errorCode")),
					environment.getProperty("status.login.unVerifiedUser"));
		else // throw user doesn't exists
			throw new UserException(Integer.parseInt(environment.getProperty("status.user.errorCode")),
					environment.getProperty("status.user.existError"));
	}

	@Override
	public Response removeCollaborator(long noteId, String userToken, String email) {
		long userId = tokenGenerator.retrieveIdFromToken(userToken);
		User user = userRepository.findById(userId).get();
		Note note;
		if (noteRepository.findByIdAndUser(noteId, user).isPresent())// owner note
			note = noteRepository.findByIdAndUser(noteId, user).get();
		else// its a collaborated note shared by other user
			note = user.getCollaboratedNotes().stream().filter(n -> n.getId() == noteId).findFirst().get();
		User collaboratedUser = userRepository.findByEmail(email).get();
		if (collaboratedUser.getId() != note.getUser().getId()) {
			collaboratedUser.removeCollaboratedNote(note);
			userRepository.save(collaboratedUser);
			note.removeCollaboratedUser(collaboratedUser);
			note = noteRepository.save(note);
			//updating the corresponding note in ElasticSearch
			noteContainer = new NoteContainer();
			noteContainer.setNote(note);
			noteContainer.setNoteOperation(NoteOperation.UPDATE);
			messagePublisher.publishNoteData(noteContainer);
			return ResponseInfo.getResponse(Integer.parseInt(environment.getProperty("status.success.code")),
					environment.getProperty("status.collaborator.remove.success"));
		}
		throw new UserException(Integer.parseInt(environment.getProperty("status.collaborator.errorCode")),
				environment.getProperty("status.collaborator.remove.error"));
	}
	
	public List<Note> searchNotes(String query) {
		return noteElasticSearch.searchNoteByAnyText(query);
	}
}
