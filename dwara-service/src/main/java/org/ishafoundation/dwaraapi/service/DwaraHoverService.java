package org.ishafoundation.dwaraapi.service;

import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.api.resp.dwarahover.DwaraHoverFileList;
import org.ishafoundation.dwaraapi.api.resp.dwarahover.DwaraHoverFileListCount;
import org.ishafoundation.dwaraapi.api.resp.dwarahover.DwaraHoverFileListDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Component
public class DwaraHoverService extends DwaraService {

	private static final Logger logger = LoggerFactory.getLogger(DwaraHoverService.class);
	String[] videoExtensions = {"mp4", "mov"};
	String[] audioExtensions = {"mp3"};
	String[] docExtensions = {"doc", "xls"};
	String[] fileExtensions = {"mp4", "mp3"};
	@PersistenceContext
	private EntityManager entityManager;

	public List getSearchData(List<String> searchWords, String type, String category, int offset, int limit) {
		String query, queryCount;
		int totalCount = 0;
		StringBuilder searchWordsBuilder = new StringBuilder();
		StringBuilder categoryBuilderFile1 = new StringBuilder();
		StringBuilder categoryBuilderFile2 = new StringBuilder();
		if (category.equalsIgnoreCase("video")) {
			for (int i = 0; i < videoExtensions.length; i++) {
				if (i != 0 && i >= searchWords.size() - 1) {
					categoryBuilderFile1.append(" OR ");
					if (fileExtensions[i].equals("mp4")) {
						categoryBuilderFile2.append(" OR ");
					}
				}
				categoryBuilderFile1.append("file1.pathname LIKE '%.").append(videoExtensions[i]).append("'");
				if (fileExtensions[i].equals("mp4")) {
					categoryBuilderFile2.append("file2.pathname LIKE '%.").append(videoExtensions[i]).append("'");
				}
			}

		} else if (category.equalsIgnoreCase("audio")) {
			for (int i = 0; i < audioExtensions.length; i++) {
				if (i != 0 && i >= searchWords.size() - 1) {
					categoryBuilderFile1.append(" OR ");
					if (fileExtensions[i].equals("mp3")) {
						categoryBuilderFile2.append(" OR ");
					}
				}
				categoryBuilderFile1.append("file1.pathname LIKE '%.").append(audioExtensions[i]).append("'");
				if (fileExtensions[i].equals("mp3")) {
					categoryBuilderFile2.append("file2.pathname LIKE '%.").append(audioExtensions[i]).append("'");
				}
			}

		} else if (category.equalsIgnoreCase("document")) {
			for (int i = 0; i < docExtensions.length; i++) {
				if (i != 0 && i >= searchWords.size() - 1) {
					categoryBuilderFile1.append(" OR ");
				}
				categoryBuilderFile1.append("file1.pathname LIKE '%.").append(docExtensions[i]).append("'");
			}

		} else if (category.equalsIgnoreCase("file")) {
			for (int i = 0; i < fileExtensions.length; i++) {
				if (i != 0 && i >= searchWords.size() - 1) {
					if (fileExtensions[i].equals("mp4")) {
						categoryBuilderFile2.append(" OR ");
					}
				}
				if (fileExtensions[i].equals("mp4")) {
					categoryBuilderFile2.append("file2.pathname LIKE '%.").append(fileExtensions[i]).append("'");
				}
			}

		}

		if (!StringUtils.isEmpty((CharSequence) searchWords)) {
			if (searchWords.size() > 0) {
				for (int i = 0; i < searchWords.size(); i++) {
					if (i != 0 && i >= searchWords.size() - 1) {
						if (type.equalsIgnoreCase("all")) {
							searchWordsBuilder.append(" AND ");
						} else {
							searchWordsBuilder.append(" OR ");
						}
					}
					searchWordsBuilder.append("file1.pathname LIKE '%").append(searchWords.get(i)).append("%'");
				}
			}
		}

		totalCount = (int) getTotalCountOfQuery(searchWords, offset, category, searchWordsBuilder, categoryBuilderFile1, categoryBuilderFile2);
		List<Object[]> results = getSearchQuery(searchWords, offset, limit, category, searchWordsBuilder, categoryBuilderFile1, categoryBuilderFile2);

		List<DwaraHoverFileListDTO> fileResults = new ArrayList<>();
		results.forEach(entry -> {
			DwaraHoverFileListDTO dwaraHoverFileListDTO = new DwaraHoverFileListDTO();
			String pathName = (String) entry[0];
			long size = ((BigInteger) entry[1]).longValue();
			int id = (Integer) entry[2];
			String artifactClassId = (String) entry[3];
			if (!category.equalsIgnoreCase("folder")) {
				String proxyPathName = (String) entry[4];

				if (!StringUtils.isEmpty(proxyPathName)) {
					if (StringUtils.contains(artifactClassId, "priv")) {
						proxyPathName = "http://172.18.1.24/mam/private/" + proxyPathName;
					} else {
						proxyPathName = "http://172.18.1.24/mam/public/" + proxyPathName;
					}
				}
				dwaraHoverFileListDTO.setProxyPathName(proxyPathName);
			}


			dwaraHoverFileListDTO.setId(id);
			dwaraHoverFileListDTO.setSize(size);
			dwaraHoverFileListDTO.setPathName(pathName);
			dwaraHoverFileListDTO.setArtifactClass_id(artifactClassId);


			fileResults.add(dwaraHoverFileListDTO);
		});

		Optional<List<DwaraHoverFileList>> dwaraHoverFileLists = DwaraHoverFileList.build(fileResults);

		if (offset == 0) {
			if (dwaraHoverFileLists.isPresent()) {
				Optional<List<DwaraHoverFileListCount>> dwaraHoverFileListCounts = DwaraHoverFileListCount.build(dwaraHoverFileLists.get(), totalCount);
				return dwaraHoverFileListCounts.get();
			}
		} else {
			if (dwaraHoverFileLists.isPresent()) {
				return dwaraHoverFileLists.get();
			}
		}
		return dwaraHoverFileLists.get();
	}


	private long getTotalCountOfQuery(List searchWords, int offset, String category, StringBuilder searchWordsBuilder, StringBuilder categoryBuilderFile1, StringBuilder categoryBuilderFile2) {
		String queryCount = null;
		int totalCount = 0;
		if (offset == 0 && !StringUtils.isEmpty((CharSequence) searchWords)) {
			if (searchWords.size() > 0) {
				if (!StringUtils.isEmpty(categoryBuilderFile2.toString()) && !category.equalsIgnoreCase("file")) {
					queryCount = "SELECT Count(*) FROM file1 join artifact1 ON file1.artifact_id = artifact1.id " +
							"LEFT JOIN file1 As file2 ON file2.file_ref_id = file1.id AND (" + categoryBuilderFile2.toString() + ")" +
							"WHERE (" + searchWordsBuilder.toString() + ") AND (" + categoryBuilderFile1.toString() + ") AND (artifact1.artifactclass_id NOT LIKE '%proxy%')";
				} else if (category.equalsIgnoreCase("file")) {
					queryCount = "SELECT Count(*) from file1 join artifact1 ON file1.artifact_id = artifact1.id " +
							"LEFT JOIN file1 As file2 ON file2.file_ref_id = file1.id  AND (" + categoryBuilderFile2.toString() + ") " +
							"WHERE (" + searchWordsBuilder.toString() + ") AND (" + categoryBuilderFile1.toString() + ") AND (artifact1.artifactclass_id NOT LIKE '%proxy%') AND file1.directory=0";
				} else if (category.equalsIgnoreCase("folder")) {
					queryCount = "SELECT Count(*) from file1 join artifact1 ON file1.artifact_id = artifact1.id " +
							"WHERE (" + searchWordsBuilder.toString() + ") AND (artifact1.artifactclass_id NOT LIKE '%proxy%') AND (file1.directory=1 AND file1.pathname NOT LIKE '%/%')";
				} else {
					queryCount = "SELECT Count(*) FROM file1 join artifact1 ON file1.artifact_id = artifact1.id " +
							"LEFT JOIN file1 As file2 ON file2.file_ref_id = file1.id " +
							"WHERE (" + searchWordsBuilder.toString() + ") AND (" + categoryBuilderFile1.toString() + ") AND (artifact1.artifactclass_id NOT LIKE '%proxy%')";
				}
			}

		} else if (offset == 0) {
			if (!StringUtils.isEmpty(categoryBuilderFile2.toString()) && !category.equalsIgnoreCase("file")) {
				queryCount = "SELECT COUNT(*) from file1 join artifact1 ON file1.artifact_id = artifact1.id " +
						"LEFT JOIN file1 As file2 ON file2.file_ref_id = file1.id AND (" + categoryBuilderFile2.toString() + ")" +
						"WHERE (" + categoryBuilderFile1.toString() + ") AND (artifact1.artifactclass_id NOT LIKE '%proxy%')";
			} else if (category.equalsIgnoreCase("file")) {
				queryCount = "SELECT Count(*) from file1 join artifact1 ON file1.artifact_id = artifact1.id " +
						"LEFT JOIN file1 As file2 ON file2.file_ref_id = file1.id  AND (" + categoryBuilderFile2.toString() + ") " +
						"WHERE (" + categoryBuilderFile1.toString() + ") AND (artifact1.artifactclass_id NOT LIKE '%proxy%') AND file1.directory=0";
			} else if (category.equalsIgnoreCase("folder")) {
				queryCount = "SELECT Count(*) from file1 join artifact1 ON file1.artifact_id = artifact1.id " +
						"WHERE (artifact1.artifactclass_id NOT LIKE '%proxy%') AND (file1.directory=1 AND file1.pathname NOT LIKE '%/%')";
			} else {
				queryCount = "SELECT COUNT(*) from file1 join artifact1 ON file1.artifact_id = artifact1.id " +
						"LEFT JOIN file1 As file2 ON file2.file_ref_id = file1.id " +
						"WHERE (" + categoryBuilderFile1.toString() + ") AND (artifact1.artifactclass_id NOT LIKE '%proxy%')";
			}

		}
		Query qCount = entityManager.createNativeQuery(queryCount);
		totalCount = ((BigInteger) qCount.getResultList().get(0)).intValue();

		return totalCount;
	}

	private List getSearchQuery(List searchWords, int offset, int limit, String category, StringBuilder searchWordsBuilder, StringBuilder categoryBuilderFile1, StringBuilder categoryBuilderFile2) {
		String query;
		if (!StringUtils.isEmpty((CharSequence) searchWords)) {
			if (!StringUtils.isEmpty(categoryBuilderFile2.toString()) && !category.equalsIgnoreCase("file")) {
				query = "SELECT file1.pathname,file1.size,file1.id,artifact1.artifactclass_id,file2.pathname AS proxy_path_name from file1 join artifact1 ON file1.artifact_id = artifact1.id " +
						"LEFT JOIN file1 As file2 ON file2.file_ref_id = file1.id AND (" + categoryBuilderFile2.toString() + ") " +
						"WHERE (" + searchWordsBuilder.toString() + ") AND (" + categoryBuilderFile1.toString() + ") AND (artifact1.artifactclass_id NOT LIKE '%proxy%') " +
						"LIMIT " + offset + ", " + limit;
			} else if (category.equalsIgnoreCase("file")) {
				query = "SELECT file1.pathname,file1.size,file1.id,artifact1.artifactclass_id,file2.pathname AS proxy_path_name from file1 join artifact1 ON file1.artifact_id = artifact1.id " +
						"LEFT JOIN file1 As file2 ON file2.file_ref_id = file1.id  AND (" + categoryBuilderFile2.toString() + ") " +
						"WHERE (" + searchWordsBuilder.toString() + ") AND (artifact1.artifactclass_id NOT LIKE '%proxy%') AND file1.directory=0 " +
						"LIMIT " + offset + ", " + limit;
			} else if (category.equalsIgnoreCase("folder")) {
				query = "SELECT file1.pathname,file1.size,file1.id,artifact1.artifactclass_id AS proxy_path_name from file1 join artifact1 ON file1.artifact_id = artifact1.id " +
						"WHERE (" + searchWordsBuilder.toString() + ") AND (artifact1.artifactclass_id NOT LIKE '%proxy%') AND (file1.directory=1 AND file1.pathname NOT LIKE '%/%') " +
						"LIMIT " + offset + ", " + limit;
			} else {
				query = "SELECT file1.pathname,file1.size,file1.id,artifact1.artifactclass_id,file2.pathname AS proxy_path_name from file1 join artifact1 ON file1.artifact_id = artifact1.id " +
						"LEFT JOIN file1 As file2 ON file2.file_ref_id = file1.id " +
						"WHERE (" + searchWordsBuilder.toString() + ") AND (" + categoryBuilderFile1.toString() + ") AND (artifact1.artifactclass_id NOT LIKE '%proxy%') " +
						"LIMIT " + offset + ", " + limit;
			}


		} else {
			if (!StringUtils.isEmpty(categoryBuilderFile2.toString()) && !category.equalsIgnoreCase("file")) {
				query = "SELECT file1.pathname,file1.size,file1.id,artifact1.artifactclass_id,file2.pathname AS proxy_path_name from file1 join artifact1 ON file1.artifact_id = artifact1.id " +
						"LEFT JOIN file1 As file2 ON file2.file_ref_id = file1.id  AND (" + categoryBuilderFile2.toString() + ") " +
						"WHERE (" + categoryBuilderFile1.toString() + ") AND (artifact1.artifactclass_id NOT LIKE '%proxy%') " +
						"LIMIT " + offset + ", " + limit;
			} else if (category.equalsIgnoreCase("file")) {
				query = "SELECT file1.pathname,file1.size,file1.id,artifact1.artifactclass_id,file2.pathname AS proxy_path_name from file1 join artifact1 ON file1.artifact_id = artifact1.id " +
						"LEFT JOIN file1 As file2 ON file2.file_ref_id = file1.id  AND (" + categoryBuilderFile2.toString() + ") " +
						"WHERE (artifact1.artifactclass_id NOT LIKE '%proxy%')  AND file1.directory=0 " +
						"LIMIT " + offset + ", " + limit;
			} else if (category.equalsIgnoreCase("folder")) {
				query = "SELECT file1.pathname,file1.size,file1.id,artifact1.artifactclass_id AS proxy_path_name from file1 join artifact1 ON file1.artifact_id = artifact1.id " +
						"WHERE (artifact1.artifactclass_id NOT LIKE '%proxy%')  AND (file1.directory=1 AND file1.pathname NOT LIKE '%/%') " +
						"LIMIT " + offset + ", " + limit;
			} else {
				query = "SELECT file1.pathname,file1.size,file1.id,artifact1.artifactclass_id,file2.pathname AS proxy_path_name from file1 join artifact1 ON file1.artifact_id = artifact1.id " +
						"LEFT JOIN file1 As file2 ON file2.file_ref_id = file1.id " +
						"WHERE (" + categoryBuilderFile1.toString() + ") AND (artifact1.artifactclass_id NOT LIKE '%proxy%') " +
						"LIMIT " + offset + ", " + limit;
			}
		}
		Query q = entityManager.createNativeQuery(query);
		return q.getResultList();
	}

}

