package dk.dtu.imm.pa;

import java.io.IOException;

import dk.dtu.imm.pa.analyzer.FlowGraph;
import dk.dtu.imm.pa.analyzer.objects.CodeLine;
import dk.dtu.imm.pa.analyzer.objects.Program;

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
		srcFile = "samples/reportExample.lang";
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
	    System.out.println();
	    
	    FlowGraph fg = new FlowGraph(program);
	    
	    System.out.println("Init:");
	    System.out.println(fg.getInitLabels());
	    System.out.println();
	    System.out.println("Labels:");
	    System.out.println(fg.getProgramLabels());
	    System.out.println();
	    System.out.println("Program flow:");
	    System.out.println(fg.getProgramFlow());
	    System.out.println();
	    System.out.println("Reaching definitions set, per label:");
//	    for(CodeLine cl : program){
//	    	System.out.println(cl);
//	    	System.out.println("entry: " + cl.getEntryReachingDefinitions() + " - exit: " + cl.getExitReachingDefinitions());
//	    	System.out.println();
//	    	System.out.println();
//	    }
	    System.out.println("Detection of Sign set, per label:");
	    for(CodeLine cl : program){
	    	System.out.println(cl);
	    	System.out.println("entry: " + cl.getEntryDetectionOfSigns() + " \n exit: " + cl.getExitDetectionOfSigns());
	    	System.out.println();
	    	System.out.println();
	    }

	} catch (IOException e) {
		System.err.println("Error trying to parse source file (" + srcFile + "):");
		System.err.println(e.getMessage());
	}
  }
}
