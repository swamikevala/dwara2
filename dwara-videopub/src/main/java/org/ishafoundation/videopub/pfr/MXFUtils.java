package org.ishafoundation.videopub.pfr;

import java.nio.ByteBuffer;

import tv.amwa.maj.exception.IllegalPropertyValueException;
import tv.amwa.maj.exception.InsufficientSpaceException;
import tv.amwa.maj.io.mxf.UL;
import tv.amwa.maj.record.DateStruct;
import tv.amwa.maj.record.TimeStamp;
import tv.amwa.maj.record.TimeStruct;

public class MXFUtils {

	public MXFUtils() {}
	
	public final static void getLocalSetPropertyValueBytes(UL key, ByteBuffer buffer, short localTag,  byte[] value) throws NullPointerException, IllegalPropertyValueException, InsufficientSpaceException {
		
		short readLocalTag;
		short readLocalLength;
		int oldPos = buffer.position();
		
		while(buffer.remaining() > 0) {
			
			readLocalTag = buffer.getShort();
			readLocalLength = buffer.getShort();
			
			if (readLocalTag != localTag) {
				buffer.position(buffer.position() + readLocalLength);
			} else {
				if (value.length != readLocalLength) {
					throw new IllegalArgumentException("Value length mismatch");
				} else {
					buffer.get(value);
					break;
				}
			}
		}
		buffer.position(oldPos);
	}
	
		
	public final static void updateLocalSetPropertyValueBytes(UL key, ByteBuffer buffer, short localTag, byte[] value) throws NullPointerException, IllegalPropertyValueException, InsufficientSpaceException {
		
		short readLocalTag;
		short readLocalLength;
		int oldPos = buffer.position();
		
		while(buffer.remaining() > 0) {
			
			readLocalTag = buffer.getShort();
			readLocalLength = buffer.getShort();
			
			if (readLocalTag != localTag) {
				buffer.position(buffer.position() + readLocalLength);
			} else {
				if (value.length != readLocalLength) {
					throw new IllegalArgumentException("Value length mismatch");
				} else {
					buffer.put(value);  
				}
			}
		}
		buffer.position(oldPos);
	}
	
	
	public final static byte[] timestampToBytes(TimeStamp ts) {
		
		DateStruct date = ts.getDate();
		TimeStruct time = ts.getTime();
		
		short year = date.getYear();
		byte month = date.getMonth();
		byte day = date.getDay();
		
		byte hour = time.getHour();
		byte minute = time.getMinute();
		byte second = time.getSecond();
		byte fraction = time.getFraction();
		
		byte[] tsBytes = ByteBuffer.allocate(8).putShort(year).put(month).put(day).put(hour).put(minute).put(second).put(fraction).array();
		return tsBytes;	
	}
}
