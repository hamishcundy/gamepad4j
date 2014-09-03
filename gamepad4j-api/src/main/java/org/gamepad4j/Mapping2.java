/*
 * @Copyright: Marcel Schoen, Switzerland, 2014, All Rights Reserved.
 */

package org.gamepad4j;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.gamepad4j.util.Log;

/**
 * ...
 *
 * @author Marcel Schoen
 * @version $Revision: $
 */
public class Mapping2 {

	public static enum MappingType {
		BUTTON,
		TRIGGER_AXIS,
		DPAD_AXIS,
		STICK_AXIS
	}
	
	/** Stores digital button mappings. */
	private static Map<Long, Map<Integer, String>> buttonMapId = new HashMap<Long, Map<Integer, String>>();
	
	/** Stores trigger mappings. */
	private static Map<Long, Map<Integer, String>> triggerAxisMapId = new HashMap<Long, Map<Integer, String>>();
	
	/** Stores d-pad mappings. */
	private static Map<Long, Map<Integer, String>> dpadAxisMapId = new HashMap<Long, Map<Integer, String>>();
	
	/** Stores stick mappings. */
	private static Map<Long, Map<Integer, String>> stickAxisMapId = new HashMap<Long, Map<Integer, String>>();

	/** Stores the default button text labels. */
	private static Properties defaultLabels = new Properties();

	/** Stores the default label for each button of each device type. */
	private static Map<Long, Map<ButtonID, String>> defaultButtonLabelMap = new HashMap<Long, Map<ButtonID, String>>();

	/** Stores the default label for each trigger of each device type. */
	private static Map<Long, Map<TriggerID, String>> defaultTriggerLabelMap = new HashMap<Long, Map<TriggerID, String>>();
	
	/** Stores the label key for each button of each device type. */
	private static Map<Long, Map<ButtonID, String>> buttonLabelKeyMap = new HashMap<Long, Map<ButtonID, String>>();
	
	/** Stores the label key for each trigger of each device type. */
	private static Map<Long, Map<TriggerID, String>> triggerLabelKeyMap = new HashMap<Long, Map<TriggerID, String>>();

	/**
	 * Initializes the mappings from the resource properties.
	 */
	public static void initializeFromResources() {
		try {
			// Read the default text labels
			InputStream in = Mapping.class.getResourceAsStream("/mappings/default-labels.properties");
			defaultLabels.load(in);
			in.close();
			
			// Read the mappings for the various pads
			in = Mapping.class.getResourceAsStream("/mappings/mapping-files.properties");
			Properties fileListProps = new Properties();
			fileListProps.load(in);
			for(Object fileListName : fileListProps.values()) {
				String propertyFileName = (String)fileListName;
				Log.log("> processing mapping file: " + propertyFileName);
				InputStream propIn = Mapping.class.getResourceAsStream(propertyFileName);
				Properties mappingProps = new Properties();
				mappingProps.load(propIn);
				
				addMappings(mappingProps);
			}
			in.close();
		} catch(Exception ex) {
			ex.printStackTrace();
			throw new IllegalStateException("Failed to process mappings from resources: " + ex);
		}
	}

	/**
	 * 
	 * @param type
	 * @param properties
	 */
	private static void addMappings(Properties properties) {
		long deviceTypeIdentifier = extractDeviceIdentifier(properties);
		
		defaultButtonLabelMap.put(deviceTypeIdentifier, new HashMap<ButtonID, String>());
		buttonLabelKeyMap.put(deviceTypeIdentifier, new HashMap<ButtonID, String>());
		triggerLabelKeyMap.put(deviceTypeIdentifier, new HashMap<TriggerID, String>());
		defaultTriggerLabelMap.put(deviceTypeIdentifier, new HashMap<TriggerID, String>());
		
		Enumeration<Object> keys = properties.keys();
		while(keys.hasMoreElements()) {
			String key = (String)keys.nextElement();
			String namePart = key.substring(key.indexOf(".") + 1);
			String value = properties.getProperty(key);
			if(key.startsWith("button.")) {
				addMapping(MappingType.BUTTON, namePart, value, deviceTypeIdentifier);
			} else if(key.startsWith("stick.")) {
				addMapping(MappingType.STICK_AXIS, namePart, value, deviceTypeIdentifier);
			} else if(key.startsWith("trigger.")) {
				addMapping(MappingType.TRIGGER_AXIS, namePart, value, deviceTypeIdentifier);
			} else if(key.startsWith("dpad.")) {
				addMapping(MappingType.DPAD_AXIS, namePart, value, deviceTypeIdentifier);
			} else if(key.startsWith("buttonlabel.")) {
				Map<ButtonID, String> buttonMap = getOrCreateMapForButton(defaultButtonLabelMap, deviceTypeIdentifier);
				buttonMap.put(getButtonIDfromPropertyKey(key), value);
			} else if(key.startsWith("triggerlabel.")) {
				Map<TriggerID, String> triggerMap = getOrCreateMapForTrigger(defaultTriggerLabelMap, deviceTypeIdentifier);
				triggerMap.put(getTriggerIDfromPropertyKey(key), value);
			} else if(key.startsWith("buttonlabelkey.")) {
				Map<ButtonID, String> buttonMap = getOrCreateMapForButton(buttonLabelKeyMap, deviceTypeIdentifier);
				buttonMap.put(getButtonIDfromPropertyKey(key), value);
			} else if(key.startsWith("triggerlabelkey.")) {
				Map<TriggerID, String> triggerMap = getOrCreateMapForTrigger(triggerLabelKeyMap, deviceTypeIdentifier);
				triggerMap.put(getTriggerIDfromPropertyKey(key), value);
			} 
		}
	}

	/**
	 * Get the mapping for a given button, trigger, dpad or stick axis.
	 * 
	 * @param controller The controller for which to retrieve the mapping.
	 * @param type The mapping type.
	 * @param value The number of the button or axis.
	 * @return The mapping string, or null, if none was found.
	 */
	public static String getMapping(IController controller,MappingType type, int value) {
		if(type == MappingType.BUTTON) {
			return buttonMapId.get(controller.getDeviceTypeIdentifier()).get(value);
		} else if(type == MappingType.DPAD_AXIS) {
			return dpadAxisMapId.get(controller.getDeviceTypeIdentifier()).get(value);
		} else if(type == MappingType.TRIGGER_AXIS) {
			return triggerAxisMapId.get(controller.getDeviceTypeIdentifier()).get(value);
		} else if(type == MappingType.STICK_AXIS) {
			return stickAxisMapId.get(controller.getDeviceTypeIdentifier()).get(value);
		}
		return null;
	}

	/**
	 * 
	 * @param type
	 * @param namePart
	 * @param value
	 * @param deviceTypeIdentifier
	 */
	private static void addMapping(MappingType type, String namePart, String value, long deviceTypeIdentifier) {
		if(type == MappingType.BUTTON) {
			Map<Integer, String> buttonMap = getOrCreateMapForDevice(buttonMapId, deviceTypeIdentifier);
			Log.log(">> Add mapping for BUTTON: " + namePart + "=" + value);
			buttonMap.put(intFromString(value), namePart);
		} else if(type == MappingType.TRIGGER_AXIS) {
			Map<Integer, String> triggerMap = getOrCreateMapForDevice(triggerAxisMapId, deviceTypeIdentifier);
			Log.log(">> Add mapping for TRIGGER: " + namePart + "=" + value);
			triggerMap.put(intFromString(value), namePart);
		} else if(type == MappingType.DPAD_AXIS) {
			Map<Integer, String> dpadMap = getOrCreateMapForDevice(dpadAxisMapId, deviceTypeIdentifier);
			Log.log(">> Add mapping for DPAD: " + namePart + "=" + value);
			dpadMap.put(intFromString(value), namePart);
		} else if(type == MappingType.STICK_AXIS) {
			Map<Integer, String> stickMap = getOrCreateMapForDevice(stickAxisMapId, deviceTypeIdentifier);
			Log.log(">> Add mapping for STICK: " + namePart + "=" + value);
			stickMap.put(intFromString(value), namePart);
		}
	}

	/**
	 * Converts a string value to an Integer.
	 * 
	 * @param value The string value.
	 * @return The Integer.
	 * @throws IllegalArgumentException If the given string was not a numerical value.
	 */
	private static Integer intFromString(String value) {
		try {
			return Integer.parseInt(value);
		} catch(NumberFormatException ex) {
			throw new IllegalArgumentException("Not a valid numeric value: " + value);
		}
	}

	/**
	 * Returns the hash map for the given device and mapping type, if it exists.
	 * If not, it is created.
	 *  
	 * @param idMap The map in which to look for the requested map.
	 * @param deviceTypeIdentifier The device type identifier.
	 * @return The newly created, or already existing map.
	 */
	private static Map<Integer, String> getOrCreateMapForDevice(Map<Long, Map<Integer, String>> idMap, long deviceTypeIdentifier) {
		Map<Integer, String> map = idMap.get(deviceTypeIdentifier);
		if(map == null) {
			map = new HashMap<Integer, String>();
			idMap.put(deviceTypeIdentifier, map);
		}
		return map;
	}

	/**
	 * Returns the hash map for the given device and mapping type, if it exists.
	 * If not, it is created.
	 *  
	 * @param idMap The map in which to look for the requested map.
	 * @param deviceTypeIdentifier The device type identifier.
	 * @return The newly created, or already existing map.
	 */
	private static Map<ButtonID, String> getOrCreateMapForButton(Map<Long, Map<ButtonID, String>> idMap, long deviceTypeIdentifier) {
		Map<ButtonID, String> map = idMap.get(deviceTypeIdentifier);
		if(map == null) {
			map = new HashMap<ButtonID, String>();
			idMap.put(deviceTypeIdentifier, map);
		}
		return map;
	}

	/**
	 * Returns the hash map for the given device and mapping type, if it exists.
	 * If not, it is created.
	 *  
	 * @param idMap The map in which to look for the requested map.
	 * @param deviceTypeIdentifier The device type identifier.
	 * @return The newly created, or already existing map.
	 */
	private static Map<TriggerID, String> getOrCreateMapForTrigger(Map<Long, Map<TriggerID, String>> idMap, long deviceTypeIdentifier) {
		Map<TriggerID, String> map = idMap.get(deviceTypeIdentifier);
		if(map == null) {
			map = new HashMap<TriggerID, String>();
			idMap.put(deviceTypeIdentifier, map);
		}
		return map;
	}
	
	/**
	 * Creates the device identifier by building a long value based on
	 * the vendor ID and the product ID in the given properties.
	 * 
	 * @param properties The mapping properties.
	 * @return The device identifier.
	 */
	private static long extractDeviceIdentifier(Properties properties) {
		int vendorID = -1, productID = -1;
		try {
			vendorID = Integer.parseInt(properties.getProperty("vendor.id", ""), 16);
			productID = Integer.parseInt(properties.getProperty("product.id", ""), 16);
		} catch(NumberFormatException ex) {
			// ignore
			ex.printStackTrace();
		}
		if(vendorID == -1) {
			throw new IllegalArgumentException("Invalid/missing vendor ID propery ('vendor.id') in mapping.");
		}
		if(productID == -1) {
			throw new IllegalArgumentException("Invalid/missing product ID propery ('product.id') in mapping.");
		}
		return (vendorID << 16) + productID;
	}
	
	/**
	 * Returns the number of triggers on the given controller, as
	 * defined by the mapping.
	 * 
	 * @param controller The controller.
	 * @return The number of triggers.
	 */
	public static int getNumberOfTriggers(IController controller) {
		return triggerAxisMapId.get(controller.getDeviceTypeIdentifier()).size();
	}
	
	/**
	 * Returns the number of analog sticks on the given controller, as
	 * defined by the mapping.
	 * 
	 * @param controller The controller.
	 * @return The number of sticks.
	 */
	public static int getNumberOfSticks(IController controller) {
		if(stickAxisMapId == null) {
			return 0;
		}
		return stickAxisMapId.get(controller.getDeviceTypeIdentifier()).size() / 2;
	}
	
	/**
	 * Returns the default text for the label for the given button.
	 * 
	 * @param controller The controller to which the button belongs.
	 * @param buttonID The ID of the button.
	 * @return The default text, or null, if none was defined.
	 */
	public static String getButtonLabel(IController controller, ButtonID buttonID) {
		return defaultButtonLabelMap.get(controller.getDeviceTypeIdentifier()).get(buttonID);
	}
	
	/**
	 * Returns the resource key for the label for the given button.
	 * 
	 * @param controller The controller to which the button belongs.
	 * @param buttonID The ID of the button.
	 * @return The resource key, or null, if none was defined.
	 */
	public static String getButtonLabelKey(IController controller, ButtonID buttonID) {
		return buttonLabelKeyMap.get(controller.getDeviceTypeIdentifier()).get(buttonID);
	}
	
	/**
	 * Returns the default text for the label for the given trigger.
	 * 
	 * @param controller The controller to which the trigger belongs.
	 * @param triggerID The ID of the trigger.
	 * @return The default text, or null, if none was defined.
	 */
	public static String getTriggerLabel(IController controller, TriggerID triggerID) {
		return defaultTriggerLabelMap.get(controller.getDeviceTypeIdentifier()).get(triggerID);
	}
	
	/**
	 * Returns the resource key for the label for the given trigger.
	 * 
	 * @param controller The controller to which the trigger belongs.
	 * @param triggerID The ID of the trigger.
	 * @return The resource key, or null, if none was defined.
	 */
	public static String getTriggerLabelKey(IController controller, TriggerID triggerID) {
		return triggerLabelKeyMap.get(controller.getDeviceTypeIdentifier()).get(triggerID);
	}
	
	/**
	 * Returns the default text label for the given trigger.
	 * 
	 * @param triggerID The ID of the trigger.
	 * @return The default label, or null.
	 */
	public static String getDefaultTriggerLabel(TriggerID triggerID) {
		return defaultLabels.getProperty("triggerlabel." + triggerID.name());
	}
	
	/**
	 * Returns the default text label for the given button.
	 * 
	 * @param buttonID The ID of the button.
	 * @return The default label, or null.
	 */
	public static String getDefaultButtonLabel(ButtonID buttonID) {
		return defaultLabels.getProperty("buttonlabel." + buttonID.name());
	}

	/**
	 * @param key
	 */
	public static ButtonID getButtonIDfromPropertyKey(String key) {
		String buttonIDvalue = key.substring(key.indexOf(".") + 1);
		ButtonID id = ButtonID.getButtonIDfromString(buttonIDvalue);
		if(id == null) {
			throw new IllegalArgumentException("Invalid button ID in property key '" + key + "'");
		}
		return id;
	}

	/**
	 * @param key
	 */
	public static TriggerID getTriggerIDfromPropertyKey(String key) {
		String triggerIDvalue = key.substring(key.indexOf(".") + 1);
		TriggerID id = TriggerID.getTriggerIDfromString(triggerIDvalue);
		if(id == null) {
			throw new IllegalArgumentException("Invalid trigger ID in property key '" + key + "'");
		}
		return id;
	}

	/**
	 * @param key
	 */
	public static StickID getStickIDfromPropertyKey(String key) {
		String stickIDvalue = key.substring(key.indexOf(".") + 1, key.lastIndexOf("."));
		StickID id = StickID.getStickIDfromString(stickIDvalue);
		if(id == null) {
			throw new IllegalArgumentException("Invalid stick ID '" + stickIDvalue + "' in property key '" + key + "'");
		}
		return id;
	}
	
}
