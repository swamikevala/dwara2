package org.ishafoundation.dwaraapi.db.attributeconverter.jsonbody;

import java.io.IOException;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.ishafoundation.dwaraapi.db.model.transactional.json.ArtifactVolumeDetails;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Converter(autoApply = true)
public class ArtifactVolumeDetailsAttributeConverter implements AttributeConverter<ArtifactVolumeDetails, String> {

	private static ObjectMapper mapper;

	static {
		// To avoid instantiating ObjectMapper again and again.
		mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
	}

	@Override
	public String convertToDatabaseColumn(ArtifactVolumeDetails data) {
		if (null == data) {
			return null;
		}

		try {
			return mapper.writeValueAsString(data);

		} catch (IOException e) {
			throw new IllegalArgumentException("Error converting VolumeDetails to JSON", e);
		}
	}

	@Override
	public ArtifactVolumeDetails convertToEntityAttribute(String s) {
		if (null == s) {
			return null;
		}

		try {
			return mapper.readValue(s, new TypeReference<ArtifactVolumeDetails>() {
			});

		} catch (IOException e) {
			throw new IllegalArgumentException("Error converting JSON to VolumeDetails", e);
		}
	}
}