package org.ishafoundation.dwaraapi.db.attributeconverter.jsonbody;

import java.io.IOException;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Converter(autoApply = true)
public class SubrequestDetailsAttributeConverter implements AttributeConverter<RequestDetails, String> {

	private static ObjectMapper mapper;

	static {
		// To avoid instantiating ObjectMapper again and again.
		mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
	}

	@Override
	public String convertToDatabaseColumn(RequestDetails data) {
		if (null == data) {
			// You may return null if you prefer that style
			return "{}";
		}

		try {
			return mapper.writeValueAsString(data);

		} catch (IOException e) {
			throw new IllegalArgumentException("Error converting SubrequestDetails to JSON", e);
		}
	}

	@Override
	public RequestDetails convertToEntityAttribute(String s) {
		if (null == s) {
			// You may return null if you prefer that style
			return new RequestDetails();
		}

		try {
			return mapper.readValue(s, new TypeReference<RequestDetails>() {
			});

		} catch (IOException e) {
			throw new IllegalArgumentException("Error converting JSON to SubrequestDetails", e);
		}
	}
}