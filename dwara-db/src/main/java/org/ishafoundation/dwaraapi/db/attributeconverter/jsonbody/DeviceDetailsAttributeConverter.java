package org.ishafoundation.dwaraapi.db.attributeconverter.jsonbody;

import java.io.IOException;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.ishafoundation.dwaraapi.db.model.master.configuration.json.DeviceDetails;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Converter(autoApply = true)
public class DeviceDetailsAttributeConverter implements AttributeConverter<DeviceDetails, String> {

	private static ObjectMapper mapper;

	static {
		// To avoid instantiating ObjectMapper again and again.
		mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
	}

	@Override
	public String convertToDatabaseColumn(DeviceDetails data) {
		if (null == data) {
			// You may return null if you prefer that style
			return "{}";
		}

		try {
			return mapper.writeValueAsString(data);

		} catch (IOException e) {
			throw new IllegalArgumentException("Error converting DeviceDetails to JSON", e);
		}
	}

	@Override
	public DeviceDetails convertToEntityAttribute(String s) {
		if (null == s) {
			// You may return null if you prefer that style
			return new DeviceDetails();
		}

		try {
			return mapper.readValue(s, new TypeReference<DeviceDetails>() {
			});

		} catch (IOException e) {
			throw new IllegalArgumentException("Error converting JSON to DeviceDetails", e);
		}
	}
}