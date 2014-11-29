package dk.dtu.imm.pa.analyzer.objects;

import java.util.ArrayList;

import org.antlr.runtime.tree.*;

import dk.dtu.imm.pa.analyzer.parser.TheLangLexer;

/**
 * A CodeLine is the equivalent of a line of code.
 * It contains a number of elements, that define what the line actually does.
 */
public class CodeLine {
	private VariableStore globalVariables;
    private VariableStore modifiedVariables;
    private VariableStore usedVariables;
    
    private ArrayList<Tree> elements;
    
    // Line number
    private int lineNumber;
    
    // Looping info and parent CodeLine (and block end, for "while" CodeLines)
    // (the one that issued the loop where the current CodeLine is into)
    private CodeLine loopParent;
    private CodeLine endOfWhileCodeLine;
    
    // Conditional block info
    private CodeLine ifParent;
    private CodeLine elseParent;
    
    private boolean isWhileStatement;
    private boolean isEndOfWhileStatement;
    private boolean isIfStatement;
    private boolean isFiStatement;
    private boolean isElseStatement;

    /**
     * Default constructor
     */
    public CodeLine(VariableStore globalVariables){
    	this.globalVariables   = globalVariables;
    	
        this.modifiedVariables = new VariableStore();
        this.usedVariables     = new VariableStore();
        this.elements          = new ArrayList<Tree>();
    }
    
    /**
     * Creates a new CodeLine based on the line number
     * @param lineNumber the line number
     */
    public CodeLine(VariableStore globalVariables, int lineNumber){
        this(globalVariables);
        this.lineNumber = lineNumber;
    }
    
    /**
     * Adds an element to the line
     * @param element the element to be added
     */
    public void addElement(Tree element){
    	// First we add the element, before we process
        elements.add(element);
        
        int type = element.getType();
        
        switch(type){
            // Check whether this modifies a variable
            case TheLangLexer.ASSIGN:{
                Tree elementToAssignTo = elements.get(elements.size()-2);
               
                // look for the variable name that we're modifying, in case it's an array
                for(int i = 0; i < elements.size(); i++){
                	if(elements.get(i).getType() == TheLangLexer.LBRACKET){
                		elementToAssignTo = elements.get(i - 1);
                	}
                }
                                	
                modifiedVariables.add(globalVariables.findByName(elementToAssignTo.getText())); // it must exist, so we find it
                break;
            }
            // Check for array declaration
            case TheLangLexer.LBRACKET: {
            	// This can mean that either we're using the array, or that we're declaring one
            	// For this reason, we check for the previous elements
                boolean assignFound = false;
                boolean openBracketFound = false;

                for(Tree t : elements){
                    if(t.getType() == TheLangLexer.ASSIGN){
                    	assignFound = true;
                    } // if we find an open bracket, that is not this current element
                    if((t.getType() == TheLangLexer.LBRACKET) && !(t.equals(element))){
                    	openBracketFound = true;
                    }
                }
                
                String arrayVariableName = elements.get(elements.size() - 2).getText();
                
                // If we find a declaration, and no assign sign, it's a new array declaration
                // remove the preceding element modified Variable, and substitute with ArrayVariable
                Variable newVariable = new Variable(this.getLineNumber(), arrayVariableName);
                
                if(!openBracketFound && !assignFound && !this.isIfStatement && !this.isWhileStatement){
                	this.modifiedVariables.add(newVariable);
                } else {
                	this.usedVariables.add(globalVariables.findByName(arrayVariableName));
                }
                globalVariables.add(newVariable);
                break;
            }
            // Check whether this uses/declares a variable
            case TheLangLexer.IDENTIFIER:{
                // If an identifier is not the first element of the CodeLine,
                // and there is no "int" var definition, it represents a variable in use
            	// unless there's an assign.
                // Otherwise, it modifies (declares) one
            	
                Variable newVariable = new Variable(this.getLineNumber(), element.getText());
                if(elements.size() > 1){
                    boolean intDeclarationFound = false;
                    boolean assignFound = false;

                    for(Tree t : elements){
                        if(t.getType() == TheLangLexer.INT){
                            intDeclarationFound = true;
                        }
                        if(t.getType() == TheLangLexer.ASSIGN){
                        	assignFound = true;
                        }
                    }
                    
                    if(intDeclarationFound && !assignFound){
                        modifiedVariables.add(newVariable);
                    } else {
                        usedVariables.add(newVariable);
                    }
                    
                    // We store it in the program variables
                    globalVariables.add(newVariable);
                } else {
                	modifiedVariables.add(newVariable);
                	globalVariables.add(newVariable);
                }
                break;
            }
            // Check whether this creates a loop
            case TheLangLexer.WHILE:{
                this.isWhileStatement = true;
                break;
            }
            // Check whether this ends a loop
            case TheLangLexer.OD:{
                this.isEndOfWhileStatement = true;
                break;
            }
            // Check whether this is an if structure
            case TheLangLexer.IF:{
                this.isIfStatement = true;
                break;
            }
            // Check whether this ends if structure
            case TheLangLexer.FI:{
                this.isFiStatement = true;
                break;
            }
            // Check whether this is an else structure
            case TheLangLexer.ELSE:{
                this.isElseStatement = true;
                break;
            }
        }
        
        System.out.println("Added element on line " + element.getLine() + ": " + element.getText() + " {type " + element.getType() + "}");
    }
    
    public VariableStore getModifiedVariables() {
        return modifiedVariables;
    }

    public VariableStore getUsedVariables() {
        return usedVariables;
    }

    public ArrayList<Tree> getElements() {
        return elements;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public boolean isWhileStatement() {
        return isWhileStatement;
    }

    public boolean isEndOfWhileStatement() {
        return isEndOfWhileStatement;
    }

    public CodeLine getLoopParent() {
        return loopParent;
    }

    public void setLoopParent(CodeLine loopParent) {
        this.loopParent = loopParent;
    }

    public CodeLine getEndOfWhileCodeLine() {
        return endOfWhileCodeLine;
    }

    public void setEndOfWhileCodeLine(CodeLine endOfWhileCodeLine) {
        this.endOfWhileCodeLine = endOfWhileCodeLine;
    }

    public boolean isIfStatement() {
        return isIfStatement;
    }

    public boolean isFiStatement() {
        return isFiStatement;
    }

    public boolean isElseStatement() {
        return isElseStatement;
    }

    public CodeLine getIfParent() {
        return ifParent;
    }

    public void setIfParent(CodeLine ifParent) {
        this.ifParent = ifParent;
    }

    public CodeLine getElseParent() {
        return elseParent;
    }

    public void setElseParent(CodeLine elseParent) {
        this.elseParent = elseParent;
    }

    public boolean affectsControlFlow() {
        return (this.isWhileStatement || this.isEndOfWhileStatement || this.isIfStatement || this.isFiStatement || this.isElseStatement);
    }
}