package dk.dtu.imm.pa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.antlr.runtime.tree.*;

/**
 * A CodeLine is the equivalent of a line of code.
 * It contains a number of elements, that define what the line actually does.
 */
public class CodeLine {
	private Map<String, Boolean> modifiedVariables;
	private Map<String, Boolean> usedVariables;
	private ArrayList<Tree> elements;
	private int lineNumber;
	private int lineType;

	/**
	 * Default constructor
	 */
	public CodeLine(){
		modifiedVariables = new HashMap<String, Boolean>();
		usedVariables     = new HashMap<String, Boolean>();
		elements          = new ArrayList<Tree>();
	}
	
	/**
	 * Creates a new CodeLine based on the line number
	 * @param lineNumber the line number
	 */
	public CodeLine(int lineNumber){
		this();
		this.lineNumber = lineNumber;
	}
	
	/**
	 * Adds an element to the line
	 * @param element the element to be added
	 */
	public void addElement(Tree element){
		elements.add(element);
		
		// Check whether this modifies a variable
		
		// Check whether this uses a variable
		
		// Check whether this defines the type of the CodeLine
		
		// Check whether this creates a loop
		
		System.out.println(element.getLine() + ": " + element.getText() + " {" + element.getType() + "}");
	}
	
	/**
	 * Returns whether the line statement is the start of a loop
	 * @return true if the line starts a loop
	 */
	public boolean isLoopStart(){
		return (this.elements.get(0).getType() == TheLangLexer.WHILE);
	}
	
}
