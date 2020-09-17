package org.ishafoundation.dwaraapi.storage.storagetype.tape;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

@Component
public class DeviceLockFactory {

	private final ConcurrentMap<String, DeviceLock> map;

	public DeviceLockFactory() {
		this.map = new ConcurrentHashMap<String, DeviceLock>();
	}

	
	public DeviceLock getDeviceLock(String deviceName) {
		return this.map.computeIfAbsent(deviceName, DeviceLock::new);
	}
}