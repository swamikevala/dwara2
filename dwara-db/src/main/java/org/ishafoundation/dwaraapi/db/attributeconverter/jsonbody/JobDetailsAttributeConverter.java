package org.ishafoundation.dwaraapi.db.attributeconverter.jsonbody;

import java.io.IOException;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.ishafoundation.dwaraapi.db.model.transactional.json.JobDetails;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Converter(autoApply = true)
public class JobDetailsAttributeConverter implements AttributeConverter<JobDetails, String> {

	private static ObjectMapper mapper;

	static {
		// To avoid instantiating ObjectMapper again and again.
		mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
	}

	@Override
	public String convertToDatabaseColumn(JobDetails data) {
		if (null == data) {
			return null;
		}

		try {
			return mapper.writeValueAsString(data);

		} catch (IOException e) {
			throw new IllegalArgumentException("Error converting JobDetails to JSON", e);
		}
	}

	@Override
	public JobDetails convertToEntityAttribute(String s) {
		if (null == s) {
			return null;
		}

		try {
			return mapper.readValue(s, new TypeReference<JobDetails>() {
			});

		} catch (IOException e) {
			throw new IllegalArgumentException("Error converting JSON to JobDetails", e);
		}
	}
}