package org.ishafoundation.dwaraapi.db.attributeconverter.jsonbody;

import java.io.IOException;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.ishafoundation.dwaraapi.db.model.transactional.json.VolumeDetails;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Converter(autoApply = true)
public class VolumeDetailsAttributeConverter implements AttributeConverter<VolumeDetails, String> {

	private static ObjectMapper mapper;

	static {
		// To avoid instantiating ObjectMapper again and again.
		mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
	}

	@Override
	public String convertToDatabaseColumn(VolumeDetails data) {
		if (null == data) {
			// You may return null if you prefer that style
			return "{}";
		}

		try {
			return mapper.writeValueAsString(data);

		} catch (IOException e) {
			throw new IllegalArgumentException("Error converting VolumeDetails to JSON", e);
		}
	}

	@Override
	public VolumeDetails convertToEntityAttribute(String s) {
		if (null == s) {
			// You may return null if you prefer that style
			return new VolumeDetails();
		}

		try {
			return mapper.readValue(s, new TypeReference<VolumeDetails>() {
			});

		} catch (IOException e) {
			throw new IllegalArgumentException("Error converting JSON to VolumeDetails", e);
		}
	}
}