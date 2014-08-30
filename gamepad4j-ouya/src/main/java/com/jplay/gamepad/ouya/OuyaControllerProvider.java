/*
 * @Copyright: Marcel Schoen, Switzerland, 2013, All Rights Reserved.
 */

package com.jplay.gamepad.ouya;

import java.util.HashMap;
import java.util.Map;

import tv.ouya.console.api.OuyaController;

import com.gamepad4j.ControllerListenerAdapter;
import com.gamepad4j.IController;
import com.gamepad4j.IControllerListener;
import com.gamepad4j.IControllerProvider;

/**
 * Provides controller instances on OUYA.
 *
 * @author Marcel Schoen
 * @version $Revision: $
 */
public class OuyaControllerProvider implements IControllerProvider {
	
	private static final int MAX_CONTROLLERS = 8;
	
	/** Stores controller listeners. */
	private ControllerListenerAdapter listeners = new ControllerListenerAdapter();

	/** Map of all connected OUYA controllers. */
	private Map<OuyaController, IController> connected = new HashMap<OuyaController, IController>();
	
	/* (non-Javadoc)
	 * @see com.gamepad4j.util.IControllerProvider#initialize()
	 */
	@Override
	public void initialize() {
		System.out.println("OUYA controller provider ready.");
		updateControllers();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.gamepad4j.util.IControllerProvider#release()
	 */
	@Override
	public void release() {
		// do nothing
	}

	/*
	 * (non-Javadoc)
	 * @see com.gamepad4j.util.IControllerProvider#checkControllers()
	 */
	@Override
	public void checkControllers() {
		boolean update = false;
		for(int ct = 0; ct < MAX_CONTROLLERS; ct++) {
			if(OuyaController.getControllerByPlayer(ct) != null) {
				if(connected.get(OuyaController.getControllerByPlayer(ct)) == null) {
					// Newly connected controller found
					update = true;
					System.out.println("Newly connected OUYA controller found.");
				}
			}
		}
		if(update) {
			updateControllers();
		}
	}

	/**
	 * Updates the array of controllers. Invoke only when
	 * an update is really necessary (because this method creates new
	 * wrapper objects and changes the map of controllers).
	 */
	private void updateControllers() {
		this.connected.clear();
		for(int ct = 0; ct < MAX_CONTROLLERS; ct++) {
			if(OuyaController.getControllerByPlayer(ct) != null) {
				OuyaController ouyaController = OuyaController.getControllerByPlayer(ct);
				IController controller = new OuyaControllerWrapper(ouyaController);
				this.connected.put(ouyaController, controller);
				for(IControllerListener listener : this.listeners.getListeners()) {
					listener.connected(controller);
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see com.gamepad4j.util.IControllerProvider#addListener(com.gamepad4j.util.IControllerListener)
	 */
	@Override
	public void addListener(IControllerListener listener) {
		this.listeners.addListener(listener);
	}

	/* (non-Javadoc)
	 * @see com.gamepad4j.util.IControllerProvider#removeListener(com.gamepad4j.util.IControllerListener)
	 */
	@Override
	public void removeListener(IControllerListener listener) {
		this.listeners.removeListener(listener);
	}

}
