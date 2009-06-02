package com.garagegames.torque.tide;

import console.Console;
import console.Output;
import console.Shell;


public class TideShell extends Shell{

	public TideShell() {
		super("TideShell");
	}

	public TideShell(String name) {
		super(name);
		
	}

	public void printInfoMessage (Console console) {
		console.getOutput().print(console.getPlainColor(), "Welcome to the TIDE console!");
	}
	
	public void execute(Console console, String input, Output output, Output error,
			String command) {

		Tide tide = Tide.getInstance();
		if(tide != null)
		{
			// send command to the engine :)
			tide.sendConsoleCmd(command);
			output.print(console.getInfoColor(), "Sending command: " + command);
			output.commandDone();
		}
	}

}
