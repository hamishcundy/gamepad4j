/*
 * @Copyright: Marcel Schoen, Switzerland, 2013, All Rights Reserved.
 */

package com.gamepad4j.test;


/**
 * ...
 *
 * @author Marcel Schoen
 * @version $Revision: $
 */
public class GamepadTest {
	
	public static void main(String[] args) {
		GamepadTest gamepadTest = new GamepadTest();
		gamepadTest.runTest();
	}

	private void runTest() {
		new GamepadTestwindow().setVisible(true);;
	}
}