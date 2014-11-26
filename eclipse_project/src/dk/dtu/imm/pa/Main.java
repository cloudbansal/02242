package dk.dtu.imm.pa;


import java.util.ArrayList;

import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;

import antlr.collections.List;
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
	ArrayList<CodeLine> program = new ArrayList<CodeLine>();
	
	// Parse source file into a tree
    TheLangLexer lex = new TheLangLexer(new ANTLRFileStream(srcFile));
    CommonTokenStream tokens = new CommonTokenStream(lex);
    TheLangParser parser = new TheLangParser(tokens);
    
    CommonTree codeTree = null;

    // Run the parser over the given source file
    try {
      TheLangParser.program_return parserResult = parser.program();
      if (parserResult != null) {
        codeTree = (CommonTree) parserResult.getTree();
      }
    } catch (RecognitionException e) {
      e.printStackTrace();
      System.exit(-1);
    }
    
    CodeLine currentCodeLine = new CodeLine();
    int currentLineNumber = -1;

    // Iterate over the parsed program
    for(int i = 0; i < codeTree.getChildCount(); i++){
    	// Get the next grammar element
    	Tree child = codeTree.getChild(i);
    	
    	// Verify whether the grammar element is on the same line
    	if(child.getLine() == currentLineNumber){
    		// if it is, add it to the previous CodeLine
    		currentCodeLine.addElement(child);
    	} else {
    		// if it isn't, instantiate new CodeLine, add element, and update line number
        	currentCodeLine = new CodeLine(child.getLine());
        	
        	currentCodeLine.addElement(child);
        	
        	currentLineNumber = child.getLine();
    	}
    	    	
    	program.add(currentCodeLine);
    }
 
    System.out.println(codeTree.toStringTree());
  }
}
