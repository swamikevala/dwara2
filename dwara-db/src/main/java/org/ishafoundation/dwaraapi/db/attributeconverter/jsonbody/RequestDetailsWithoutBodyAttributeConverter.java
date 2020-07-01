package org.ishafoundation.dwaraapi.db.attributeconverter.jsonbody;

import java.io.IOException;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

//@Converter(autoApply = true)
public class RequestDetailsWithoutBodyAttributeConverter implements AttributeConverter<RequestDetails, String> {

	private static ObjectMapper mapper;

	static {
		// To avoid instantiating ObjectMapper again and again.
		mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
	}

	@Override
	public String convertToDatabaseColumn(RequestDetails data) {
		if (null == data) {
			return null;
		}

		try {
			if(data.getBody() != null)
				return mapper.writeValueAsString(data.getBody());
			else
				return mapper.writeValueAsString(data);

		} catch (IOException e) {
			throw new IllegalArgumentException("Error converting RequestDetails to JSON", e);
		}
	}

	@Override
	public RequestDetails convertToEntityAttribute(String s) {
		if (null == s) {
			return null;
		}

		try {
			return mapper.readValue(s, new TypeReference<RequestDetails>() {
			});
		} catch (IOException e) {
			try {
				JsonNode body = mapper.readValue(s, JsonNode.class);
				RequestDetails requestDetails = new RequestDetails();
				requestDetails.setBody(body);
				return requestDetails;
			}catch (Exception e2) {
				throw new IllegalArgumentException("Error converting JSON to RequestDetails", e2);
			}
		}
	}
}