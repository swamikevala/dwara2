package org.ishafoundation.dwaraapi.storage.storagetype.tape;

import java.util.Objects;

public class DeviceLock {

	private final String deviceName;

	public DeviceLock(String deviceName) {
		this.deviceName = deviceName;
	}

	public String getDeviceName() {
		return deviceName;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		DeviceLock driveLock = (DeviceLock) o;
		return Objects.equals(deviceName, driveLock.deviceName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(deviceName);
	}
}