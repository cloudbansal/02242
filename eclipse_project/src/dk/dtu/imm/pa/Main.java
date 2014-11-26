package dk.dtu.imm.pa;


import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;

import antlr.collections.List;

public class Main {

  public static void main(String args[]) throws Exception {
	// Check for source file to be received as argument
	if(args.length != 1){
		
	}
	
	// Parse source file into a tree
    TheLangLexer lex = new TheLangLexer(new ANTLRFileStream(args[0]));
    CommonTokenStream tokens = new CommonTokenStream(lex);
    TheLangParser parser = new TheLangParser(tokens);
    
    CommonTree codeTree = null;

    try {
      TheLangParser.program_return parserResult = parser.program();
      if (parserResult != null) {
        codeTree = (CommonTree) parserResult.getTree();
      }
    } catch (RecognitionException e) {
      e.printStackTrace();
      System.exit(-1);
    }

    // Get identifier (see TheLangParser enum)
    for(int i = 0; i < codeTree.getChildCount(); i++){
    	Tree child = codeTree.getChild(i);
    	System.out.println(child.getLine() + ": " + child.getText() + " {" + child.getType() + "}");
    }
    
    
    
    
    System.out.println(codeTree.toStringTree());
  }
}