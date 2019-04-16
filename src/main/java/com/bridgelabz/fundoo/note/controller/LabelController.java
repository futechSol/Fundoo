package com.bridgelabz.fundoo.note.controller;

import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.bridgelabz.fundoo.exception.LabelException;
import com.bridgelabz.fundoo.exception.UserException;
import com.bridgelabz.fundoo.note.dto.LabelDTO;
import com.bridgelabz.fundoo.note.service.LabelService;
import com.bridgelabz.fundoo.response.Response;
import com.bridgelabz.fundoo.util.ResponseInfo;

@RestController
@RequestMapping("/notes/labels")
public class LabelController {
	private static final Logger logger = LoggerFactory.getLogger(LabelController.class);
	@Autowired
	private LabelService labelService;

	@PostMapping
	public ResponseEntity<Object> addLabel(@Valid @RequestBody LabelDTO labelDTO, @RequestHeader String token) {
		logger.info("LabelDTO : " +  labelDTO);
		logger.info("token : " +  token);
		Object response;
		try {
			if(labelDTO.getName().equals(""))
				response = ResponseInfo.getResponse(-800, "label name can't not be empty");
			else
				response = labelService.create(labelDTO, token);
		}
		catch(UserException e) {
			response = ResponseInfo.getResponse(e.getErrorCode(),e.getMessage());
		}
		catch(LabelException e) {
			response = ResponseInfo.getResponse(e.getErrorCode(),e.getMessage());
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PutMapping("/{id}")
	public ResponseEntity<Response> updateLabel(@Valid @RequestBody LabelDTO labelDTO, @PathVariable long id,@RequestHeader String token) {
		logger.info("LabelDTO : " +  labelDTO);
		logger.info("token : " +  token);
		logger.info("id : " +  id);
		Response response = null;
		if(labelDTO.getName().equals(""))
			response = ResponseInfo.getResponse(-800, "label name can't not be empty");
		else
		    response =labelService.update(labelDTO, id, token);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Response> deleteLabel(@PathVariable long id,@RequestHeader String token) {
		logger.info("token : " +  token);
		logger.info("id : " +  id);
		Response response =labelService.delete(id, token);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping()
	public ResponseEntity<Object> getAllLabelsOfUser(@RequestHeader String token) {
		logger.info("token : " +  token);
		Object response;
		try {
			response = labelService.getAllLabels(token);
		}
		catch(UserException e) {
			response = ResponseInfo.getResponse(e.getErrorCode(),e.getMessage());
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/{id}")
	public ResponseEntity<Object> getLabel(@PathVariable long id, @RequestHeader String token) {
		logger.info("token : " +  token);
		Object response;
		try {
			response = labelService.getLabel(id, token);
		}
		catch(UserException e) {
			response = ResponseInfo.getResponse(e.getErrorCode(),e.getMessage());
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
