package com.bridgelabz.fundoo.note.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.bridgelabz.fundoo.note.model.Note;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class NoteElasticSearchImpl implements NoteElasticSearch {
	@Value("${elasticsearch.index}")
	private String index;
	@Value("${elasticsearch.type}")
	private String type;
	@Autowired
	private RestHighLevelClient restHighLevelClient;
	@Autowired
	private ObjectMapper objectMapper;
	private static final Logger logger = LoggerFactory.getLogger(NoteElasticSearchImpl.class);

	/**
	 * INSERTs a note in ElasticSearch
	 * 
	 * @param note Note instance to be inserted into ELasticSearch
	 * @return changes made to the document
	 */
	@Override
	public Result insertNote(Note note) {
		@SuppressWarnings("unchecked")
		Map<String, String> dataMap = objectMapper.convertValue(note, Map.class);
		IndexRequest indexRequest = new IndexRequest(index, type, String.valueOf(note.getId())).source(dataMap);
		IndexResponse indexResponse = null;
		try {
			indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);

		} catch (ElasticsearchException e) {
			e.getDetailedMessage();
			logger.error(e.getDetailedMessage());
		} catch (java.io.IOException ex) {
			ex.getLocalizedMessage();
			logger.error(ex.getLocalizedMessage());
		}
		return indexResponse.getResult();
	}

	/**
	 * UPDATEs a note by id in ElasticSearch
	 * 
	 * @param id   id of the note
	 * @param note Note instance to be updated
	 * @return updated document
	 */
	public Map<String, Object> updateNoteById(Note note) {
		UpdateRequest updateRequest = new UpdateRequest(index, type, note.getId() + "").fetchSource(true); // Fetch
																											// Object
																											// after its
																											// update
		Map<String, Object> error = new HashMap<>();
		error.put("Error", "Unable to update note");
		try {
			String noteJson = objectMapper.writeValueAsString(note);
			updateRequest.doc(noteJson, XContentType.JSON);
			UpdateResponse updateResponse = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
			Map<String, Object> sourceAsMap = updateResponse.getGetResult().sourceAsMap();
			return sourceAsMap;
		} catch (JsonProcessingException e) {
			e.getMessage();
			logger.error(e.getMessage());
		} catch (java.io.IOException e) {
			e.getLocalizedMessage();
			logger.error(e.getLocalizedMessage());
		}
		return error;
	}

	/**
	 * DELETEs note by id from elastic search
	 * 
	 * @param id id of the note instance to be deleted
	 * @return Result
	 */
	public Result deleteNoteById(String id) {
		DeleteRequest deleteRequest = new DeleteRequest(index, type, id);
		DeleteResponse deleteResponse = null;
		try {
			deleteResponse = restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
		} catch (java.io.IOException e) {
			e.getLocalizedMessage();
			logger.error(e.getLocalizedMessage());
		}
		return deleteResponse.getResult();
	}

	/**
	 * RETRIEVEs a note from elastic search by id
	 * 
	 * @param id id of the note
	 * @return note instance as a Map
	 */
	public Map<String, Object> getNoteById(String id) {
		GetRequest getRequest = new GetRequest(index, type, id);
		GetResponse getResponse = null;
		try {
			getResponse = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
		} catch (java.io.IOException e) {
			e.getLocalizedMessage();
		}
		Map<String, Object> sourceAsMap = getResponse.getSourceAsMap();
		return sourceAsMap;
	}

	/**
	 * SEARCHes a note by title in elastic search
	 * 
	 * @param title title of the note
	 * @return List of Note
	 */
	@Override
	public List<Note> searchNoteByTitle(String title) {
		QueryBuilder queryBuilder = QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("title", title));
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(queryBuilder);
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.source(searchSourceBuilder);
		SearchResponse response = null;
		try {
			response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		return getSearchResult(response);
	}

	private List<Note> getSearchResult(SearchResponse response) {

		SearchHit[] searchHits = response.getHits().getHits();
		List<Note> notes = new ArrayList<>();
		for (SearchHit hit : searchHits) {
			notes.add(objectMapper.convertValue(hit.getSourceAsMap(), Note.class));
		}
		return notes;
	}

	@Override
	public List<Note> searchNoteByAnyText(String queryString) {
		long userId = 0;
		QueryBuilder queryBuilder = QueryBuilders.boolQuery().should(QueryBuilders.queryStringQuery(queryString)
				.analyzeWildcard(true).field("title", 2.0f).field("description").field("labels"));

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(queryBuilder);
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.source(searchSourceBuilder);
		SearchResponse response = null;
		try {
			response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		List<Note> allNotes = getSearchResult(response);
		//List<Note> userNotes = allNotes.stream().filter(n -> n.getUser().getId() == userId).collect(Collectors.toList()); 
		//User user = 
		//List<Note> collaboratedNotes =
		 
		return allNotes;
	}
}
