package dk.dtu.imm.pa.analyzer.objects;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;

import dk.dtu.imm.pa.analyzer.BooleanExpression;
import dk.dtu.imm.pa.analyzer.parser.TheLangLexer;
import dk.dtu.imm.pa.analyzer.parser.TheLangParser;

public class Program extends LinkedList<CodeLine> {
	
	private VariableStore globalVariables = new VariableStore();

	public VariableStore getGlobalVariables() {
		return globalVariables;
	}

	public void setGlobalVariables(VariableStore globalVariables) {
		this.globalVariables = globalVariables;
	}

	/**
	 * Default value for serialization
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Program structure, defining the program usage and modification of variables, and control flow
	 * @param srcFile: the path to the file to be used as source
	 * @throws IOException
	 */
	public Program(String srcFile) throws IOException{
		super();
		
		this.parseSourceFile(srcFile);
		
		this.linkFlow();
		this.removeReferencesToIndirectParents();
		this.parseBooleanExpressions();
	}
	
	private void parseBooleanExpressions(){
		for(CodeLine l: this){
			ArrayList<Tree> insideParentheses = null;
			if(l.isIfStatement() || l.isWhileStatement()){
				insideParentheses = new ArrayList<Tree>();
				boolean leftParenthesesFound = false;
				for(Tree element : l.getElements()){
					if(leftParenthesesFound){
						if(element.getType() != TheLangLexer.RPAREN){
							insideParentheses.add(element);
						} else {
							break;
						}
					} else if(element.getType() == TheLangLexer.LPAREN){
						leftParenthesesFound = true;
					}
				}
			}
			if(insideParentheses != null){
				l.setBooleanExpression(new BooleanExpression(insideParentheses));
			}
		}
	}
	
	/**
	 * Parses a source file into the program object
	 * @param srcFile: the path to the file to be parsed
	 * @throws IOException
	 */
	private void parseSourceFile(String srcFile) throws IOException{
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
	    
	    // Auxiliary CodeLine for the loop
	    CodeLine currentCodeLine = new CodeLine(globalVariables);
	    
	    // Line where the last element was located
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
	    		// if it isn't, instantiate and store a new one, add element, and update line number    		
	        	currentCodeLine = new CodeLine(globalVariables, child.getLine());
	        	this.add(currentCodeLine);
	        	
	        	currentCodeLine.addElement(child);
	        	
	        	currentLineNumber = child.getLine();
	    	}    	
	    }
	}
	

	/**
	 * Returns the program, with comments about variable use/modification,
	 * conditionals, and loop structure.
	 */
	public String toString(){
		StringBuilder programString = new StringBuilder();
		for(CodeLine codeLine: this){
			programString.append(codeLine.toString());
		}
		
		return programString.toString();
	}
	
	/**
	 * Generate looping structure within the array, with each CodeLine referencing its loop parent
	 */
	private void linkFlow(){
		// State information: parent of the CodeLine (line statement that issued the loop where we are into)
	    CodeLine loopParentCodeLine = null;
	    CodeLine ifParentCodeLine = null;
	    CodeLine elseParentCodeLine = null;
	    
	    // Keeping track of stack level
	    int stackLevel = 0;
	    
	    // Generate looping structure
	    for(CodeLine codeLine : this){
	    	// Set to null, or current parents 
	    	codeLine.setLoopParent(loopParentCodeLine);
	    	codeLine.setIfParent(ifParentCodeLine);
			codeLine.setElseParent(elseParentCodeLine);
			
			codeLine.setStackLevel(stackLevel);
	    	
	    	if(codeLine.isWhileStatement()){
	    		stackLevel++;
	    		
	    		loopParentCodeLine = codeLine;
	    		
	    	} else if(codeLine.isEndOfWhileStatement()){
	    		stackLevel--;
	    		codeLine.setStackLevel(stackLevel);
	    		
	    		// Go up one level
	    		loopParentCodeLine = loopParentCodeLine.getLoopParent();
	    		
	    		// Set end of the while, on the while CodeLine
	    		codeLine.getLoopParent().setEndOfWhileCodeLine(this.get(codeLine.getLineNumber() - 2));
	    		
	    		// End of while is already out of the loop
	    		codeLine.setLoopParent(loopParentCodeLine);
	    	} else if (codeLine.isIfStatement()) {
	    		stackLevel++;
	    		
				ifParentCodeLine = codeLine;
				elseParentCodeLine = null;
			} else if (codeLine.isElseStatement()) {
				// "else" is in same scope as its corresponding "if"
				codeLine.setIfParent(ifParentCodeLine.getIfParent());
				codeLine.setElseParent(ifParentCodeLine.getElseParent());

				elseParentCodeLine = codeLine;
				
				codeLine.setStackLevel(stackLevel - 1);
	    	} else if (codeLine.isFiStatement()) {
	    		stackLevel--;
	    		codeLine.setStackLevel(stackLevel);
	    		codeLine.setBeginningOfIfInFi(ifParentCodeLine);
	    		
	    		ifParentCodeLine = ifParentCodeLine.getIfParent();
	    	    elseParentCodeLine = elseParentCodeLine.getElseParent();
	    	    codeLine.setIfParent(ifParentCodeLine);
	    	    codeLine.setElseParent(elseParentCodeLine);
			}
	    }
	}
	
	/**
	 * Removes the references to indirect parents (whether conditional or loop)
	 */
	private void removeReferencesToIndirectParents(){
		for(CodeLine codeLine : this){
			int ifParentLineNumber   = codeLine.getIfParent()   == null ? -1 : codeLine.getIfParent().getLineNumber();
			int loopParentLineNumber = codeLine.getLoopParent() == null ? -1 : codeLine.getLoopParent().getLineNumber();
			
			if(loopParentLineNumber > ifParentLineNumber){
				codeLine.setIfParent(null);
				codeLine.setElseParent(null);
			} else if (loopParentLineNumber < ifParentLineNumber){
				codeLine.setLoopParent(null);
			}
		}
	}
}
