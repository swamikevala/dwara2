package org.ishafoundation.dwaraapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.api.resp.dwarahover.*;
import org.ishafoundation.dwaraapi.db.dao.master.jointables.ExtensionFiletypeDao;
import org.ishafoundation.dwaraapi.db.keys.ExtensionFiletypeKey;
import org.ishafoundation.dwaraapi.db.model.master.jointables.ExtensionFiletype;
import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.ishafoundation.dwaraapi.exception.ExceptionType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Component
public class DwaraHoverService extends DwaraService {

	private static final Logger logger = LoggerFactory.getLogger(DwaraHoverService.class);

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	ExtensionFiletypeDao extensionFiletypeDao;

	ObjectMapper objectMapper = new ObjectMapper();

	int totalSizeTranscripts;



	/**
	 * Get the Data based on the given Search Criteria
	 *
	 * @param searchWords
	 * @param type
	 * @param category
	 * @param contentType
	 * @param offset
	 * @param limit
	 * @return List
	 */
	public List getSearchData(List<String> searchWords, String type, String category, String contentType, int offset, int limit) {
		Set<String> userRoles= getUserRoles();
		String[] extensions;
		String[] docExtensions = {"doc", "xls", "xml"};
		String[] fileExtensions = {"mp4", "mp3"};
		String queryMode = null;
		int totalCount = 0;
		StringBuilder searchWordsBuilder = new StringBuilder();
		StringBuilder categoryBuilderFile1 = new StringBuilder();
		StringBuilder categoryBuilderFile2 = new StringBuilder();

		if (category.equalsIgnoreCase("video")) {
			extensions = extensionFiletypeDao.findAllByFiletypeId(category.toLowerCase()).stream().map(ExtensionFiletype::getId).map(ExtensionFiletypeKey::getExtensionId).toArray(String[]::new);
			for (int i = 0; i < extensions.length; i++) {
				if (i != 0) {
					categoryBuilderFile1.append(" OR ");
				}
				categoryBuilderFile1.append("file.pathname LIKE '%.").append(extensions[i]).append("'");
				if (extensions[i].equals("mp4")) {
					categoryBuilderFile2.append("file2.pathname LIKE '%.").append(extensions[i]).append("'");
				}
			}

		} else if (category.equalsIgnoreCase("audio")) {
			extensions = extensionFiletypeDao.findAllByFiletypeId(category.toLowerCase()).stream().map(ExtensionFiletype::getId).map(ExtensionFiletypeKey::getExtensionId).toArray(String[]::new);
			for (int i = 0; i < extensions.length; i++) {
				if (i != 0) {
					categoryBuilderFile1.append(" OR ");
				}
				categoryBuilderFile1.append("file.pathname LIKE '%.").append(extensions[i]).append("'");
				if (extensions[i].equals("mp3")) {
					categoryBuilderFile2.append("file2.pathname LIKE '%.").append(extensions[i]).append("'");
				}
			}

		} else if (category.equalsIgnoreCase("image")) {
			extensions = extensionFiletypeDao.findAllByFiletypeId(category.toLowerCase()).stream().map(ExtensionFiletype::getId).map(ExtensionFiletypeKey::getExtensionId).toArray(String[]::new);
			for (int i = 0; i < extensions.length; i++) {
				if (i != 0) {
					categoryBuilderFile1.append(" OR ");
				}
				categoryBuilderFile1.append("file.pathname LIKE '%.").append(extensions[i]).append("'");
			}

		}
		else if (category.equalsIgnoreCase("document")) {
			for (int i = 0; i < docExtensions.length; i++) {
				if (i != 0) {
					categoryBuilderFile1.append(" OR ");
				}
				categoryBuilderFile1.append("file.pathname LIKE '%.").append(docExtensions[i]).append("'");
			}

		} else if (category.equalsIgnoreCase("file")) {
			for (int i = 0; i < fileExtensions.length; i++) {
				if (i != 0) {
					categoryBuilderFile2.append(" OR ");
				}
					categoryBuilderFile2.append("file2.pathname LIKE '%.").append(fileExtensions[i]).append("'");
			}

		}

		if (!searchWords.isEmpty()) {
			if (searchWords.size() > 0) {
				for (int i = 0; i < searchWords.size(); i++) {
					if (i != 0) {
						if (type.equalsIgnoreCase("all")) {
							searchWordsBuilder.append(" AND ");
						} else {
							searchWordsBuilder.append(" OR ");
						}
					}
					searchWordsBuilder.append("file.pathname LIKE '%").append(searchWords.get(i)).append("%'");
				}
			}
		}

		if(contentType.equalsIgnoreCase("raw")) {
			queryMode = "artifact.artifactclass_id NOT LIKE '%edit%'";
		} else if (contentType.equalsIgnoreCase("edit")) {
			queryMode = "artifact.artifactclass_id LIKE '%edit%'";
		}
		List<Object[]> results = null;
		List<DwaraHoverFileListDTO> fileResults = new ArrayList<>();
		if(!category.equalsIgnoreCase("transcript")) {

			totalCount = (int) getTotalCountOfQuery(category, queryMode, searchWordsBuilder, categoryBuilderFile1, categoryBuilderFile2);
			if (totalCount < offset) {
				String json = "{ \"offset\" : \"" + offset + "\", \"totalCount\" : \"" + totalCount + "\" } ";

				try {
					JsonNode jsonNode = objectMapper.readTree(json);
					throw new DwaraException("The total count of search criteria is less than the given offset", ExceptionType.error, jsonNode);
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}
			}
			results = getSearchQuery(offset, limit, category, queryMode, searchWordsBuilder, categoryBuilderFile1, categoryBuilderFile2);

			ExecutorService executor = Executors.newFixedThreadPool(10);

			List<Object[]> finalResults = results;
			List<Future<?>> futures = IntStream.range(0, finalResults.size())
					.mapToObj(i -> executor.submit(() -> {
						DwaraHoverFileListDTO dwaraHoverFileListDTO = new DwaraHoverFileListDTO();
						List<String> proxyFilesForFolderQuery = new ArrayList<>();
						String pathName = (String) finalResults.get(i)[0];
						long size = ((BigInteger) finalResults.get(i)[1]).longValue();
						int id = (Integer) finalResults.get(i)[2];
						int artifact_id;
						String artifactClassId;
						String proxyPathName = null;
						if (!category.equalsIgnoreCase("folder")) {
							artifactClassId = (String) finalResults.get(i)[3];
							proxyPathName = (String) finalResults.get(i)[4];

							if (!StringUtils.isEmpty(proxyPathName)) {
								if (StringUtils.contains(artifactClassId, "priv")) {
									proxyPathName = "http://172.18.1.24/mam/private/" + proxyPathName;
								} else {
									proxyPathName = "http://172.18.1.24/mam/public/" + proxyPathName;
								}

								if (StringUtils.contains(artifactClassId, "priv")){

									for (String userRole: userRoles) {
																				if(userRole.equals("priv1")) {
											proxyFilesForFolderQuery.add(proxyPathName);

										}
									}
								} else{
									proxyFilesForFolderQuery.add(proxyPathName);
								}

									//proxyFilesForFolderQuery.add(proxyPathName);
							}

						} else {
							artifact_id = (Integer) finalResults.get(i)[3];
							artifactClassId = (String) finalResults.get(i)[4];
							String proxyForFolderQuery = "SELECT file.pathname,artifact.artifactclass_id FROM artifact JOIN file ON file.artifact_id=artifact.id WHERE artifact_ref_id=" + artifact_id + " AND file.pathname LIKE '%.mp4'";
							Query q = entityManager.createNativeQuery(proxyForFolderQuery);
							List<Object[]> folderQueryResults =  q.getResultList();

							folderQueryResults.forEach(file-> {
								String proxyName;
								if (StringUtils.contains((String) file[1], "priv")) {
									proxyName = "http://172.18.1.24/mam/private/" + file[0];
								} else {
									proxyName = "http://172.18.1.24/mam/public/" + file[0];
								}

								if (StringUtils.contains(artifactClassId, "priv")){

									for (String userRole: userRoles) {
																				if(userRole.equals("priv1")) {
											proxyFilesForFolderQuery.add(proxyName);

										}
									}
								} else{
									proxyFilesForFolderQuery.add(proxyName);
								}
								//proxyFilesForFolderQuery.add(proxyName);

							});

						}
						List<DwaraHoverTranscriptListDTO> transcriptsByPathName = getTranscriptsByPathName(pathName);
						dwaraHoverFileListDTO.setProxyPathName(proxyFilesForFolderQuery);
						dwaraHoverFileListDTO.setId(id);
						dwaraHoverFileListDTO.setSize(size);
						dwaraHoverFileListDTO.setPathName(pathName);
						dwaraHoverFileListDTO.setArtifactClass_id(artifactClassId);
						dwaraHoverFileListDTO.setTranscripts(transcriptsByPathName);

						return dwaraHoverFileListDTO;
					})).collect(Collectors.toList());

			executor.shutdown();

			for (Future<?> future : futures) {
				try {
					fileResults.add((DwaraHoverFileListDTO) future.get());
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}

		} else {
			List<DwaraHoverTranscriptListDTO> dwaraHoverTranscriptListDTOS = searchInTranscripts(searchWords, type, offset, limit);
			totalCount = totalSizeTranscripts;


			ExecutorService executor = Executors.newFixedThreadPool(10);

			List<Future<?>> futures = IntStream.range(0, dwaraHoverTranscriptListDTOS.size())
					.mapToObj(i -> executor.submit(() -> {
						DwaraHoverFileListDTO dwaraHoverFileListDTO = new DwaraHoverFileListDTO();
						List<String> proxyFilesForFolderQuery = new ArrayList<>();
						String proxyForFolderQuery = dwaraHoverTranscriptListDTOS.get(i).getSearchQuery();
						Query q = entityManager.createNativeQuery(proxyForFolderQuery);
						List<Object[]> folderQueryResults =  q.getResultList();

						folderQueryResults.forEach(file-> {
							String proxyName;
							if (StringUtils.contains((String) file[1], "priv")) {
								proxyName = "http://172.18.1.24/mam/private/" + file[0];
							} else {
								proxyName = "http://172.18.1.24/mam/public/" + file[0];
							}

							if (StringUtils.contains((String) file[1], "priv")){

								for (String userRole: userRoles) {

									if(userRole.equals("priv1")) {
										proxyFilesForFolderQuery.add(proxyName);

									}
								}
							} else{
								proxyFilesForFolderQuery.add(proxyName);
							}
							//proxyFilesForFolderQuery.add(proxyName);

						});

						DwaraHoverTranscriptListDTO transcriptsByPathName = new DwaraHoverTranscriptListDTO();
						transcriptsByPathName.setTitle(dwaraHoverTranscriptListDTOS.get(i).getTitle());
						transcriptsByPathName.setLink(dwaraHoverTranscriptListDTOS.get(i).getLink());

						dwaraHoverFileListDTO.setProxyPathName(proxyFilesForFolderQuery);
						dwaraHoverFileListDTO.setPathName(transcriptsByPathName.getTitle());
						dwaraHoverFileListDTO.setTranscripts(Arrays.asList(transcriptsByPathName));
						dwaraHoverFileListDTO.setSize(0);
						dwaraHoverFileListDTO.setId(0);

						return dwaraHoverFileListDTO;
					})).collect(Collectors.toList());

			executor.shutdown();

			for (Future<?> future : futures) {
				try {
					fileResults.add((DwaraHoverFileListDTO) future.get());
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}

		}

		Optional<List<DwaraHoverFileList>> dwaraHoverFileLists = DwaraHoverFileList.build(fileResults);

		Optional<List<DwaraHoverFileListCount>> dwaraHoverFileListCounts = null;
			if (dwaraHoverFileLists.isPresent()) {
					dwaraHoverFileListCounts = DwaraHoverFileListCount.build(dwaraHoverFileLists.get(), totalCount);
					return dwaraHoverFileListCounts.get();
			}
		 
		return dwaraHoverFileListCounts.get();
	}


	private long getTotalCountOfQuery(String category, String queryMode, StringBuilder searchWordsBuilder, StringBuilder categoryBuilderFile1, StringBuilder categoryBuilderFile2) {
		String queryCount;
		int totalCount = 0;
			if(!StringUtils.isEmpty(queryMode) ) {
				if (!StringUtils.isEmpty(categoryBuilderFile2.toString()) && !category.equalsIgnoreCase("file")) {
					queryCount = "SELECT Count(*) FROM file join artifact ON file.artifact_id = artifact.id " +
							"LEFT JOIN file As file2 ON file2.file_ref_id = file.id AND (" + categoryBuilderFile2.toString() + ")" +
							"WHERE (" + searchWordsBuilder.toString() + ") AND (" + categoryBuilderFile1.toString() + ") AND (artifact.artifactclass_id NOT LIKE '%proxy%') AND (" + queryMode + ")";
				} else if (category.equalsIgnoreCase("file")) {
					queryCount = "SELECT Count(*) from file join artifact ON file.artifact_id = artifact.id " +
							"LEFT JOIN file As file2 ON file2.file_ref_id = file.id  AND (" + categoryBuilderFile2.toString() + ") " +
							"WHERE (" + searchWordsBuilder.toString() + ") AND (artifact.artifactclass_id NOT LIKE '%proxy%')  AND (" + queryMode + ") AND file.directory=0";
				} else if (category.equalsIgnoreCase("folder")) {
					queryCount = "SELECT Count(*) from file join artifact ON file.artifact_id = artifact.id " +
							"WHERE (" + searchWordsBuilder.toString() + ") AND (artifact.artifactclass_id NOT LIKE '%proxy%')  AND (" + queryMode + ") AND (file.directory=1 AND file.pathname NOT LIKE '%/%')";
				} else {
					queryCount = "SELECT Count(*) FROM file join artifact ON file.artifact_id = artifact.id " +
							"LEFT JOIN file As file2 ON file2.file_ref_id = file.id " +
							"WHERE (" + searchWordsBuilder.toString() + ") AND (" + categoryBuilderFile1.toString() + ") AND (artifact.artifactclass_id NOT LIKE '%proxy%')  AND (" + queryMode + ")";
				}
			} else {
				if (!StringUtils.isEmpty(categoryBuilderFile2.toString()) && !category.equalsIgnoreCase("file")) {
					queryCount = "SELECT Count(*) FROM file join artifact ON file.artifact_id = artifact.id " +
							"LEFT JOIN file As file2 ON file2.file_ref_id = file.id AND (" + categoryBuilderFile2.toString() + ")" +
							"WHERE (" + searchWordsBuilder.toString() + ") AND (" + categoryBuilderFile1.toString() + ") AND (artifact.artifactclass_id NOT LIKE '%proxy%')";
				} else if (category.equalsIgnoreCase("file")) {
					queryCount = "SELECT Count(*) from file join artifact ON file.artifact_id = artifact.id " +
							"LEFT JOIN file As file2 ON file2.file_ref_id = file.id  AND (" + categoryBuilderFile2.toString() + ") " +
							"WHERE (" + searchWordsBuilder.toString() + ") AND (artifact.artifactclass_id NOT LIKE '%proxy%') AND file.directory=0";
				} else if (category.equalsIgnoreCase("folder")) {
					queryCount = "SELECT Count(*) from file join artifact ON file.artifact_id = artifact.id " +
							"WHERE (" + searchWordsBuilder.toString() + ") AND (artifact.artifactclass_id NOT LIKE '%proxy%') AND (file.directory=1 AND file.pathname NOT LIKE '%/%')";
				} else {
					queryCount = "SELECT Count(*) FROM file join artifact ON file.artifact_id = artifact.id " +
							"LEFT JOIN file As file2 ON file2.file_ref_id = file.id " +
							"WHERE (" + searchWordsBuilder.toString() + ") AND (" + categoryBuilderFile1.toString() + ") AND (artifact.artifactclass_id NOT LIKE '%proxy%')";
				}
			}

			queryCount += " AND file.deleted=0";

			Query qCount = entityManager.createNativeQuery(queryCount);
			totalCount = ((BigInteger) qCount.getResultList().get(0)).intValue();

		return totalCount;
	}

	private List getSearchQuery(int offset, int limit, String category, String queryMode, StringBuilder searchWordsBuilder, StringBuilder categoryBuilderFile1, StringBuilder categoryBuilderFile2) {
		String query;
			if(!StringUtils.isEmpty(queryMode) ) {
				if (!StringUtils.isEmpty(categoryBuilderFile2.toString()) && !category.equalsIgnoreCase("file")) {
					query = "SELECT file.pathname,file.size,file.id,artifact.artifactclass_id,file2.pathname AS proxy_path_name from file join artifact ON file.artifact_id = artifact.id " +
							"LEFT JOIN file As file2 ON file2.file_ref_id = file.id AND (" + categoryBuilderFile2.toString() + ") " +
							"WHERE (" + searchWordsBuilder.toString() + ") AND (" + categoryBuilderFile1.toString() + ") AND (artifact.artifactclass_id NOT LIKE '%proxy%')  AND (" + queryMode + ") ";
				} else if (category.equalsIgnoreCase("file")) {
					query = "SELECT file.pathname,file.size,file.id,artifact.artifactclass_id,file2.pathname AS proxy_path_name from file join artifact ON file.artifact_id = artifact.id " +
							"LEFT JOIN file As file2 ON file2.file_ref_id = file.id  AND (" + categoryBuilderFile2.toString() + ") " +
							"WHERE (" + searchWordsBuilder.toString() + ") AND (artifact.artifactclass_id NOT LIKE '%proxy%')  AND (" + queryMode + ") AND file.directory=0 ";
				} else if (category.equalsIgnoreCase("folder")) {
					query = "SELECT file.pathname,file.size,file.id,file.artifact_id,artifact.artifactclass_id AS proxy_path_name from file join artifact ON file.artifact_id = artifact.id " +
							"WHERE (" + searchWordsBuilder.toString() + ") AND (artifact.artifactclass_id NOT LIKE '%proxy%')  AND (" + queryMode + ") AND (file.directory=1 AND file.pathname NOT LIKE '%/%') ";
				} else {
					query = "SELECT file.pathname,file.size,file.id,artifact.artifactclass_id,file2.pathname AS proxy_path_name from file join artifact ON file.artifact_id = artifact.id " +
							"LEFT JOIN file As file2 ON file2.file_ref_id = file.id " +
							"WHERE (" + searchWordsBuilder.toString() + ") AND (" + categoryBuilderFile1.toString() + ") AND (artifact.artifactclass_id NOT LIKE '%proxy%')  AND (" + queryMode + ") ";
				}
			} else {
				if (!StringUtils.isEmpty(categoryBuilderFile2.toString()) && !category.equalsIgnoreCase("file")) {
					query = "SELECT file.pathname,file.size,file.id,artifact.artifactclass_id,file2.pathname AS proxy_path_name from file join artifact ON file.artifact_id = artifact.id " +
							"LEFT JOIN file As file2 ON file2.file_ref_id = file.id AND (" + categoryBuilderFile2.toString() + ") " +
							"WHERE (" + searchWordsBuilder.toString() + ") AND (" + categoryBuilderFile1.toString() + ") AND (artifact.artifactclass_id NOT LIKE '%proxy%') ";
				} else if (category.equalsIgnoreCase("file")) {
					query = "SELECT file.pathname,file.size,file.id,artifact.artifactclass_id,file2.pathname AS proxy_path_name from file join artifact ON file.artifact_id = artifact.id " +
							"LEFT JOIN file As file2 ON file2.file_ref_id = file.id  AND (" + categoryBuilderFile2.toString() + ") " +
							"WHERE (" + searchWordsBuilder.toString() + ") AND (artifact.artifactclass_id NOT LIKE '%proxy%') AND file.directory=0 ";
				} else if (category.equalsIgnoreCase("folder")) {
					query = "SELECT file.pathname,file.size,file.id,file.artifact_id,artifact.artifactclass_id AS proxy_path_name from file join artifact ON file.artifact_id = artifact.id " +
							"WHERE (" + searchWordsBuilder.toString() + ") AND (artifact.artifactclass_id NOT LIKE '%proxy%') AND (file.directory=1 AND file.pathname NOT LIKE '%/%') ";
				} else {
					query = "SELECT file.pathname,file.size,file.id,artifact.artifactclass_id,file2.pathname AS proxy_path_name from file join artifact ON file.artifact_id = artifact.id " +
							"LEFT JOIN file As file2 ON file2.file_ref_id = file.id " +
							"WHERE (" + searchWordsBuilder.toString() + ") AND (" + categoryBuilderFile1.toString() + ") AND (artifact.artifactclass_id NOT LIKE '%proxy%') ";
				}
			}

			query += " AND file.deleted=0 order by artifact.id desc LIMIT " + offset + ", " + limit;

		Query q = entityManager.createNativeQuery(query);
		return q.getResultList();
	}

	private List<DwaraHoverTranscriptListDTO> getTranscriptsByPathName(String filename) {

		final String transcriptTitleURL = "http://172.18.1.22:8090/rest/api/content/search?cql=(type=page%20and%20title~'";
		final int transcriptTitleLimit = 100;
		final String transcriptURLAuthorization = "Basic c2FtZWVyLmRhc2g6aG9seQ==";
		final String transcriptLinkURL = "http://art.iyc.ishafoundation.org";

		List<DwaraHoverTranscriptListDTO> output = new ArrayList<>();
		Pattern pattern = Pattern.compile("(\\d{1,2}-[a-zA-Z]{3}-\\d{2,4})");
		Matcher matcher = pattern.matcher(filename);
		String title;
		if (matcher.find()) {
			title = matcher.group(1);
		} else {
			return Collections.emptyList();
		}

		try {
			Unirest.setTimeouts(0, 0);
			HttpResponse<com.mashape.unirest.http.JsonNode>	response = Unirest.get(transcriptTitleURL + title + "')&limit=" + transcriptTitleLimit)
					.header("Authorization", transcriptURLAuthorization).asJson();

			JSONArray dataArray = (JSONArray) response.getBody().getObject().get("results");
			for (Object data : dataArray) {
				JSONObject jsonObject = (JSONObject) data;
				String transcriptTitle = jsonObject.get("title").toString();
				String transcriptURL = transcriptLinkURL + jsonObject.getJSONObject("_links").get("webui").toString();

				DwaraHoverTranscriptListDTO dwaraHoverTranscriptListDTO = new DwaraHoverTranscriptListDTO();
				dwaraHoverTranscriptListDTO.setTitle(transcriptTitle);
				dwaraHoverTranscriptListDTO.setLink(transcriptURL);
				output.add(dwaraHoverTranscriptListDTO);
			}
		} catch (UnirestException e) {
			// e.printStackTrace();
		}
		return output;
	}

	private List<DwaraHoverTranscriptListDTO> searchInTranscripts(List<String> searchWords, String type, int offset, int limit) {
		final String transcriptTitleURL = "http://172.18.1.22:8090/rest/api/content/search?cql=(type=page%20and";
		final String transcriptURLAuthorization = "Basic c2FtZWVyLmRhc2g6aG9seQ==";
		final String transcriptLinkURL = "http://art.iyc.ishafoundation.org";
		List<DwaraHoverTranscriptListDTO> output = new ArrayList<>();
		StringBuilder searchItemBuilder = new StringBuilder();
		String searchOperator;
		if (type.equalsIgnoreCase("all")) {
			searchOperator = "AND";
		} else {
			searchOperator = "OR";
		}

		// Separate terms based on spaces
		if (searchWords.size() > 1) {
			searchItemBuilder.append("%20(");
			// Split it based on space
			for (String searchItem : searchWords) {
				if (searchItemBuilder.toString().equals("%20(")) {
					searchItemBuilder.append("text~").append("'").append(searchItem).append("'");
				} else {
					searchItemBuilder.append("%20").append(searchOperator).append("%20text~").append("'").append(searchItem).append("'");
				}
			}
			searchItemBuilder.append(")");
		} else {
			searchItemBuilder.append("%20text~").append("'").append(searchWords.get(0)).append("'");
		}

		HttpResponse<com.mashape.unirest.http.JsonNode> response = null;
		try {
			response = Unirest.get(transcriptTitleURL + searchItemBuilder.toString() + ")&limit=" + limit + "&start=" + offset).header("Authorization", transcriptURLAuthorization).asJson();
		} catch (UnirestException e) {
			e.printStackTrace();
		}


		JSONArray dataArray = (JSONArray) response.getBody().getObject().get("results");
		totalSizeTranscripts = (int) response.getBody().getObject().get("totalSize");

		ExecutorService executor = Executors.newFixedThreadPool(10);

		List<Future<?>> futures = IntStream.range(0, dataArray.length())
				.mapToObj(i -> executor.submit(() -> {
					String transcriptQuery;
					JSONObject jsonObject =  dataArray.getJSONObject(i);
					String transcriptTitle = jsonObject.get("title").toString();
					String transcriptURL = transcriptLinkURL + jsonObject.getJSONObject("_links").get("webui").toString();

					DwaraHoverTranscriptListDTO dwaraHoverTranscriptListDTO = new DwaraHoverTranscriptListDTO();
					dwaraHoverTranscriptListDTO.setTitle(transcriptTitle);
					dwaraHoverTranscriptListDTO.setLink(transcriptURL);

					transcriptQuery = getTranscriptSearchQuery(transcriptTitle);

					dwaraHoverTranscriptListDTO.setSearchQuery(transcriptQuery);
					return dwaraHoverTranscriptListDTO;
				})).collect(Collectors.toList());

		executor.shutdown();

		for (Future<?> future : futures) {
			try {
				output.add((DwaraHoverTranscriptListDTO) future.get());
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}

		return output;
	}

	private static String alternateDate(String date) {
		String alternateDate = "";
		String[] dateItems = date.split("-");
		if (dateItems[2].length() == 4) {
			alternateDate = dateItems[0] + "-" + dateItems[1] + "-" + dateItems[2].substring(2);
		} else {
			alternateDate = dateItems[0] + "-" + dateItems[1] + "-" + "20"+dateItems[2] ;
		}
		return alternateDate;
	}

	private static boolean isNumeric(String strNum) {
		if (StringUtils.isEmpty(strNum)) {
			return false;
		}
		try {
			double d = Double.parseDouble(strNum);
			return true;
		} catch (NumberFormatException nfe) {
			return false;
		}

	}

	private String getTranscriptSearchQuery(String transcriptTitle) {
		String regexd = transcriptTitle.replaceFirst("^[A-Z]{0,2}[0-9]{1,4}[\\-_\\s]", "");
		String date = "";
		String alternatedDate = "";
		String removedDateBeforeAfter = regexd;
		String transcriptQuery;

		Pattern pattern = Pattern.compile("([0-9]{1,2}\\-[A-Za-z]{3}\\-[0-9]{2,4}).*$");
		Matcher matcher = pattern.matcher(regexd);
		if (matcher.find()) {
			// replace first number with "number" and second number with the first
			removedDateBeforeAfter = matcher.replaceFirst("$1");
			date = matcher.group(1);
			alternatedDate = alternateDate(date);
			// 2.5remove the date from the regex string
			removedDateBeforeAfter = removedDateBeforeAfter.replace(date, "");
		}
		// Split the queryname into array by _ and -
		String[] queryArray = removedDateBeforeAfter.replaceAll("\\s+"," ").replace(" ", "-").replace("_","-").split("-");
		List<String> queryList = new ArrayList<String>(Arrays.asList(queryArray));
		queryList.removeAll(Arrays.asList("", null));

		queryArray = queryList.toArray(new String[] {});

		StringBuilder builder = new StringBuilder();
		boolean firstItem = false;

		StockWords stopWord = new StockWords();
		for(String queryItem : queryArray) {
			// Check if the query item is a stop word. Escape it in case it is.
			if (stopWord.getStopWords().contains(queryItem.toLowerCase())) {
				continue;
			}
			else if (queryItem.trim().replaceAll("[^A-z0-9\\-_]", "") == "") {
				continue;
			}
			else if (isNumeric(queryItem)) {
				continue;
			}
			else {
				if(firstItem) {
					builder.append(" OR ");
				}
				firstItem = true;
				builder.append("pathname like ").append("'%").append(queryItem).append("%'");
			}
		}

		if (date != "") {
			transcriptQuery = "SELECT file.pathname, artifact.artifactclass_id FROM file JOIN artifact ON file.artifact_id = artifact.id WHERE (" + builder.toString() + ")" + " AND (pathname like '%" + date + "%' OR pathname like '%" + alternatedDate + "_%') AND (file_ref_id IS NOT null AND pathname LIKE '%.mp4') LIMIT 500";
		}
		else  {
			transcriptQuery = "SELECT file.pathname, artifact.artifactclass_id FROM file JOIN artifact ON file.artifact_id = artifact.id WHERE (" + builder.toString() + ") AND (file_ref_id IS NOT null AND pathname LIKE '%.mp4') LIMIT 500" ;
		}
		return transcriptQuery;
	}

}

