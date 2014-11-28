package dk.dtu.imm.pa;

import java.io.IOException;

/**
 * ProgramAnalyzer
 * - receives the file path of the source code to analyze, and performs the given analysis techniques
 *   on it.
 */
public class Main {
  public static void main(String args[]) throws Exception {
	// Check for source file to be received as argument
	String srcFile;
	if(args.length != 1){
		srcFile = "samples/sample.lang";
	} else {
		srcFile = args[0];
	}
	
	// Create program structure of lines of code (CodeLines)
	Program program;
	try {
		program = new Program(srcFile);
		
	    // Print program
	    System.out.println();
	    System.out.println(program.toString());
	} catch (IOException e) {
		System.err.println("Error trying to parse source file (" + srcFile + "):");
		System.err.println(e.getMessage());
	}
  }
}
