package jams.message.bus.impl;

import jams.message.bus.device.DeviceID;

public class DeviceNotFoundException extends RuntimeException {

	private DeviceID id;
	
	public DeviceNotFoundException(DeviceID id) {

		super();
		this.id = id;
	}

}
