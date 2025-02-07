package dk.dtu.imm.pa.analyzer.objects;

import java.util.ArrayList;

import org.antlr.runtime.tree.*;

import dk.dtu.imm.pa.analyzer.BooleanExpression;
import dk.dtu.imm.pa.analyzer.objects.collections.DetectionOfSignsSet;
import dk.dtu.imm.pa.analyzer.objects.collections.IntervalAnalysisSet;
import dk.dtu.imm.pa.analyzer.objects.collections.ReachingDefinitionsSet;
import dk.dtu.imm.pa.analyzer.parser.TheLangLexer;

/**
 * A CodeLine is the equivalent of a line of code.
 * It contains a number of elements, that define what the line actually does.
 */
public class CodeLine {
	private VariableStore globalVariables;
    private VariableStore modifiedVariables;
    private VariableStore usedVariables;
    
    // Analysis parameters
    private ReachingDefinitionsSet entryReachingDefinitions;
    private ReachingDefinitionsSet exitReachingDefinitions;
    private DetectionOfSignsSet entryDetectionOfSigns;
    private DetectionOfSignsSet exitDetectionOfSigns;
    private IntervalAnalysisSet entryIntervalAnalysis;
    private IntervalAnalysisSet exitIntervalAnalysis;
    
    private ArrayList<Tree> elements;
    
    // Boolean expression in its case;
    private BooleanExpression booleanExpression;
    
    // Line number
    private int lineNumber;
    
    // Stack level
    private int stackLevel;
    
    // Looping info and parent CodeLine (and block end, for "while" CodeLines)
    // (the one that issued the loop where the current CodeLine is into)
    private CodeLine loopParent;
    private CodeLine endOfWhileCodeLine;
    
    // Conditional block info
    private CodeLine ifParent;
    private CodeLine elseParent;
    private CodeLine beginningOfIfInFi;

    
    private boolean isWhileStatement;
    private boolean isEndOfWhileStatement;
    private boolean isIfStatement;
    private boolean isFiStatement;
    private boolean isElseStatement;
    private boolean isDeclarationStatement;
	private boolean isProgramStatement;
	private boolean isEndStatement;
	private boolean isThenStatement;
	private boolean isReadStatement;
	private boolean isAssignmentStatement;

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
        	// Check if this is a variable declaration
        	case TheLangLexer.INT: {
        		this.isDeclarationStatement = true;
        		break;
        	}
        	// Check for integer before assign (array definition)
        	case TheLangLexer.INTEGER: {
        		boolean assignFound = false;
        		boolean bracketFound = false;
        		for(Tree t : elements){
                    if(t.getType() == TheLangLexer.ASSIGN){
                    	assignFound = true;
                    }
                    if(t.getType() == TheLangLexer.LBRACKET){
                    	bracketFound = true;
                	}
        		}
        		if(!assignFound && bracketFound && this.isDeclarationStatement){
        			Variable newArrayVariable = new Variable(this.getLineNumber(), this.elements.get(this.elements.size() - 3).getText());
        			newArrayVariable.setArray(true);
        			newArrayVariable.setArrayLength(Integer.parseInt(element.getText()));
        			
        			this.globalVariables.remove(this.globalVariables.findByName(this.elements.get(this.elements.size() - 3).getText()));
        			this.globalVariables.add(newArrayVariable);
        		}
        		break;
        	}
        	// Check if it's a read statement;
        	case TheLangLexer.READ: {
        		this.setReadStatement(true);
        		break;
        	}
        	// Check if this is a program declaration
        	case TheLangLexer.PROGRAM: {
        		this.isProgramStatement = true;
        		break;
        	}
        	// Check if this is ending the program
        	case TheLangLexer.END: {
        		this.isEndStatement = true;
        		break;
        	}
        	// Check if this is a then keyword
        	case TheLangLexer.THEN: {
        		this.isThenStatement = true;
        		break;
        	}
            // Check whether this modifies a variable
            case TheLangLexer.ASSIGN:{
                Tree elementToAssignTo = elements.get(elements.size()-2);
               
                // look for the variable name that we're modifying, in case it's an array
                for(int i = 0; i < elements.size(); i++){
                	if(elements.get(i).getType() == TheLangLexer.LBRACKET){
                		elementToAssignTo = elements.get(i - 1);
                	}
                }
                
                Variable thePreexistingVar = globalVariables.findByName(elementToAssignTo.getText());
                                                	
                modifiedVariables.add(thePreexistingVar); // it must exist, so we find it
                
                this.setAssignmentStatement(true);
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
                
                if(!openBracketFound && !assignFound && !this.isIfStatement && !this.isWhileStatement){
                	this.modifiedVariables.add(globalVariables.findByName(arrayVariableName));
                } else {
                	this.usedVariables.add(globalVariables.findByName(arrayVariableName));
                }
                break;
            }
            // Check whether this uses/declares a variable
            case TheLangLexer.IDENTIFIER:{
                // If an identifier is not the first element of the CodeLine,
                // and there is no "int" var definition, it represents a variable in use
            	// unless there's an assign.
                // Otherwise, it modifies (declares) one
            	
                Variable newVariable = this.globalVariables.findByName(element.getText());
                if(newVariable == null){
                	newVariable = new Variable(this.getLineNumber(), element.getText());
                }
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
        
        //System.out.println("Added element on line " + element.getLine() + ": " + element.getText() + " {type " + element.getType() + "}");
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
    
    public boolean isDeclarationStatement() {
        return isDeclarationStatement;
    }
    
    public boolean isProgramStatement() {
		return isProgramStatement;
	}

	public boolean isEndStatement() {
		return isEndStatement;
	}

	public boolean isThenStatement() {
		return isThenStatement;
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

    public boolean isReadStatement() {
		return isReadStatement;
	}

	public boolean isAssignmentStatement() {
		return isAssignmentStatement;
	}

	public void setAssignmentStatement(boolean isAssignmentStatement) {
		this.isAssignmentStatement = isAssignmentStatement;
	}

	public void setReadStatement(boolean isReadStatement) {
		this.isReadStatement = isReadStatement;
	}

	public boolean affectsControlFlow() {
        return (this.isWhileStatement || this.isEndOfWhileStatement || this.isIfStatement || this.isFiStatement || this.isElseStatement);
    }
    
    public int getStackLevel() {
		return stackLevel;
	}

	public void setStackLevel(int stackLevel) {
		this.stackLevel = stackLevel;
	}

	public CodeLine getBeginningOfIfInFi() {
		return beginningOfIfInFi;
	}

	public void setBeginningOfIfInFi(CodeLine beginningOfIfInFi) {
		this.beginningOfIfInFi = beginningOfIfInFi;
	}

	public String toString(){
    	StringBuilder sb = new StringBuilder();
    	
    	sb.append(this.getLineNumber()+":");
		
		sb.append("\t");
		
		for(int i = 0; i < this.stackLevel; i++){
			sb.append("\t");
		}
		
		sb.append(this.getElements().toString());
		
		if(this.affectsControlFlow()){
			sb.append("   is{");
			sb.append( this.isIfStatement() ?         "if, "    : "");
			sb.append( this.isElseStatement() ?       "else, "  : "");
			sb.append( this.isFiStatement() ?         "fi, "    : "");
			sb.append( this.isWhileStatement() ?      "while, " : "");
			sb.append( this.isEndOfWhileStatement() ? "od, "    : "");
			sb.append("}");
		}
		
		if(!this.getModifiedVariables().isEmpty()){
			sb.append("   modifies{");
			sb.append( this.getModifiedVariables().toString());
			sb.append("}");
		}
		
		if(!this.getUsedVariables().isEmpty()){
			sb.append("   uses{");
			sb.append( this.getUsedVariables().toString());
			sb.append("}");
		}
		
		if(this.getIfParent() != null)
			sb.append("   ifParentLine: "    + this.getIfParent().getLineNumber());
		
		if(this.getEndOfWhileCodeLine() != null)
			sb.append("   endOfWhileLine: "  + this.getEndOfWhileCodeLine().getLineNumber());
		
		if(this.getElseParent() != null)
			sb.append("   elseParentLine: "  + this.getElseParent().getLineNumber());
		
		if(this.getLoopParent() != null)
			sb.append("   whileParentLine: " + this.getLoopParent().getLineNumber());

		sb.append("\n");
		
		return sb.toString();
    }

	public ReachingDefinitionsSet getEntryReachingDefinitions() {
		return entryReachingDefinitions;
	}

	public void setEntryReachingDefinitions(
			ReachingDefinitionsSet entryReachingDefinitions) {
		this.entryReachingDefinitions = entryReachingDefinitions;
	}

	public ReachingDefinitionsSet getExitReachingDefinitions() {
		return exitReachingDefinitions;
	}

	public void setExitReachingDefinitions(
			ReachingDefinitionsSet exitReachingDefinitions) {
		this.exitReachingDefinitions = exitReachingDefinitions;
	}

	public DetectionOfSignsSet getEntryDetectionOfSigns() {
		return entryDetectionOfSigns;
	}

	public void setEntryDetectionOfSigns(DetectionOfSignsSet entryDetectionOfSigns) {
		this.entryDetectionOfSigns = entryDetectionOfSigns;
	}

	public DetectionOfSignsSet getExitDetectionOfSigns() {
		return exitDetectionOfSigns;
	}

	public void setExitDetectionOfSigns(DetectionOfSignsSet exitDetectionOfSigns) {
		this.exitDetectionOfSigns = exitDetectionOfSigns;
	}

	public IntervalAnalysisSet getEntryIntervalAnalysis() {
		return entryIntervalAnalysis;
	}

	public void setEntryIntervalAnalysis(IntervalAnalysisSet entryIntervalAnalysis) {
		this.entryIntervalAnalysis = entryIntervalAnalysis;
	}

	public IntervalAnalysisSet getExitIntervalAnalysis() {
		return exitIntervalAnalysis;
	}

	public void setExitIntervalAnalysis(IntervalAnalysisSet exitIntervalAnalysis) {
		this.exitIntervalAnalysis = exitIntervalAnalysis;
	}

	public VariableStore getGlobalVariables() {
		return globalVariables;
	}

	public boolean equals(Object o){
		return  (o instanceof CodeLine)
				&&
				(( (CodeLine) o ).getLineNumber() == this.getLineNumber() );
	}

	public BooleanExpression getBooleanExpression() {
		return booleanExpression;
	}

	public void setBooleanExpression(BooleanExpression booleanExpression) {
		this.booleanExpression = booleanExpression;
	}
}
