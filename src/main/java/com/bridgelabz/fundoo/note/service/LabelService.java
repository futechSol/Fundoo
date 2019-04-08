package com.bridgelabz.fundoo.note.service;

import java.util.Set;

import com.bridgelabz.fundoo.note.dto.LabelDTO;
import com.bridgelabz.fundoo.note.model.Label;
import com.bridgelabz.fundoo.response.Response;

public interface LabelService {
	   Label create(LabelDTO labelDTO, String userToken);
	   Response update(LabelDTO labelDTO, long id, String userToken);
	   Response delete(long id, String userToken);
	   Set<Label> getAllLabels(String userToken);
	  // Label getLabelByName(String userToken,String name);
	   public Label getLabel(long id, String userToken) ;
}
