package dk.dtu.imm.pa.analyzer;

import java.util.ArrayList;
import java.util.Iterator;

import org.antlr.runtime.tree.Tree;

import dk.dtu.imm.pa.analyzer.objects.CodeLine;
import dk.dtu.imm.pa.analyzer.objects.Program;
import dk.dtu.imm.pa.analyzer.objects.Variable;
import dk.dtu.imm.pa.analyzer.objects.collections.DetectionOfSigns;
import dk.dtu.imm.pa.analyzer.objects.collections.DetectionOfSignsSet;
import dk.dtu.imm.pa.analyzer.objects.collections.ReachingDefinition;
import dk.dtu.imm.pa.analyzer.objects.collections.ReachingDefinitionsSet;
import dk.dtu.imm.pa.analyzer.parser.TheLangLexer;

public class FlowGraph {
	
	private Program  program;
	private LabelSet programLabels;
	private LabelSet initLabels;
	private EdgeSet  programFlow;
	
	public FlowGraph(Program program){
		this.program = program;
		
		this.calculateLabels();
		this.calculateInitLabels();
		this.calculateProgramFlow();
		
		this.calculateReachingDefinitions();
		
		System.out.println(this.calculateProgramSlice(programLabels.findByLineNumber(11)));
		
		this.calculateDetectionOfSigns();
		
		this.calculateIntervalAnalysis();
		
	};
	
	public LabelSet getProgramLabels() {
		return programLabels;
	}



	public LabelSet getInitLabels() {
		return initLabels;
	}



	public EdgeSet getProgramFlow() {
		return programFlow;
	}
	
	private void calculateReachingDefinitions(){ 
		EdgeSet worklist = new EdgeSet();           // W = null
		worklist.addAll(this.programFlow);          // Initialization
		
		// Init labels set
		for(CodeLine l : this.programLabels){
			l.setEntryReachingDefinitions(new ReachingDefinitionsSet());
			l.setExitReachingDefinitions(new ReachingDefinitionsSet());
			
			if(this.initLabels.contains(l)){
				for(Variable v: l.getGlobalVariables()){
					l.getEntryReachingDefinitions().add(new ReachingDefinition(v.getName(), -1 ));
				}
			}
		}
		
		while(!worklist.isEmpty()){
			Edge e = worklist.remove(0); // pop!
			CodeLine label1 = e.getSource();
			CodeLine label2 = e.getDestination();
			
			label1.setExitReachingDefinitions(getRDfromLabel(label1));
			
			if( ! label2.getEntryReachingDefinitions().contains(label1.getExitReachingDefinitions())){
				label2.setEntryReachingDefinitions(label2.getEntryReachingDefinitions().addition(label1.getExitReachingDefinitions()));
				for(Edge f : this.programFlow){
					if(f.getSource().equals(label2)){
						worklist.add(f);
					}
				}
			}
		}
	}
	
	private DetectionOfSignsSet getDSfromLabel(CodeLine l){
		DetectionOfSignsSet result = new DetectionOfSignsSet();
		// if variablehasparent  find the parent of block, and get if true or false (always true in while)
		//                       if we're in the true block, run trueFunction(), and else, falseFunction()

		// Set just contains one modified variable each time
		Variable modifiedVariable;
		
		for(Variable v : l.getModifiedVariables()){
	        // order (variableName, plus, minus, zero)

			modifiedVariable = v;
			
			DetectionOfSignsSet kill = new DetectionOfSignsSet();
			kill.add(new DetectionOfSigns(v.getName(), true, true, true));
			
			if(l.isDeclarationStatement()){ // if var declaration, variable, false, false, true
				result.add(new DetectionOfSigns(v.getName(), false, false, true));
			} else if(l.isReadStatement()){ // if read, variable, true, true, true
				result.add(new DetectionOfSigns(v.getName(), true, true, true));
			} else if(l.isAssignmentStatement()){
				// calculate sign of statement
				ArrayList<Tree> labelElements = l.getElements();

				// if it's just a number on the right side, easy peasy
				if(l.getUsedVariables().isEmpty()){ // no used variables, but assignment, number
					// Find number
					int i;
					for(i = 0; i < labelElements.size(); i++){
						if(labelElements.get(i).getType() == TheLangLexer.INTEGER){
							int number = Integer.parseInt(labelElements.get(i).getText());
							result.add(new DetectionOfSigns(v.getName(), number > 0, number < 0, number == 0));
						}
					}
				} else {
					// Find assignment, and parse expression at the right
					int i;
					boolean inRightSide = false;
					for(i = 0; i < labelElements.size(); i++){
						if(labelElements.get(i).getType() == TheLangLexer.ASSIGN){
							inRightSide = true;
							break;
						}
						if(inRightSide){
							if()
						}
					}
				}
			}		
		}

		return result;
	}
	
	private ReachingDefinitionsSet getRDfromLabel(CodeLine l){
		ReachingDefinitionsSet result = new ReachingDefinitionsSet();
		
		ReachingDefinitionsSet kill = new ReachingDefinitionsSet();
		ReachingDefinitionsSet gen  = new ReachingDefinitionsSet();
		for(Variable v : l.getModifiedVariables()){
			for(CodeLine tempLabel : this.programLabels){
				kill.add(new ReachingDefinition(v.getName(), tempLabel.getLineNumber()));
			}
		}
		
		for(Variable v : l.getModifiedVariables()){
			kill.add(new ReachingDefinition(v.getName(), -1));
			gen.add(new ReachingDefinition(v.getName(), l.getLineNumber()));
		}
				
		result = l.getEntryReachingDefinitions().removal(kill);
		result = result.addition(gen);
		
		return result;
	}
	
	private LabelSet calculateProgramSlice(CodeLine labelOfInterest){
		LabelSet result = new LabelSet();
		LabelSet worklist = new LabelSet();
		
		worklist.add(labelOfInterest);
		
		while(!worklist.isEmpty()){
			CodeLine l = worklist.remove(0);
			
			if( ! result.contains(l) ) {
				CodeLine parentLabel = null;
				
				if(l.getLoopParent() != null)
					parentLabel = l.getLoopParent();
				else if (l.getIfParent() != null)
					parentLabel = l.getIfParent();
					
				if(parentLabel != null)
					worklist.add(parentLabel);
				
				result.add(l);
				
				LabelSet slice = calculateAuxSlice(l);
				
				for(CodeLine labelInSlice : slice){
					if( ! result.contains(labelInSlice)){
						worklist.add(labelInSlice);
					}
				}
			}
		}
		
		return result;
	}
	
	private LabelSet calculateAuxSlice(CodeLine labelOfInterest){
		LabelSet result = new LabelSet();
		
		for(Variable v : labelOfInterest.getUsedVariables()){
			ArrayList<Integer> lineNumbers = labelOfInterest.getEntryReachingDefinitions().getAllLinesOfVariable(v);
			for(int i : lineNumbers){
				result.add(programLabels.findByLineNumber(i));
			}
		}
		
		return result;
	}
	
	private void calculateIntervalAnalysis(){
		
	}
	
	private void calculateDetectionOfSigns(){
		EdgeSet worklist = new EdgeSet();           // W = null
        worklist.addAll(this.programFlow);          // Initialization
        
        // Init labels set
        for(CodeLine l : this.programLabels){
            l.setEntryDetectionOfSigns(new DetectionOfSignsSet());
            l.setExitDetectionOfSigns(new DetectionOfSignsSet());
            
            if(this.initLabels.contains(l)){
                for(Variable v: l.getGlobalVariables()){
                    l.getEntryDetectionOfSigns().add(new DetectionOfSigns(v.getName(), false, false, true ));
                }
            }
        }
        
        while(!worklist.isEmpty()){
            Edge e = worklist.remove(0); // pop!
            CodeLine label1 = e.getSource();
            CodeLine label2 = e.getDestination();
            
            label1.setExitDetectionOfSigns(getDSfromLabel(label1));
            
            if( ! label2.getEntryDetectionOfSigns().contains(label1.getExitDetectionOfSigns())){
                label2.setEntryDetectionOfSigns(label2.getEntryDetectionOfSigns().addition(label1.getExitDetectionOfSigns()));
                for(Edge f : this.programFlow){
                    if(f.getSource().equals(label2)){
                        worklist.add(f);
                    }
                }
            }
        }
	}



	private void calculateLabels(){
		LabelSet set = new LabelSet();
		
		for(CodeLine label : this.program){
			if( ! (
					label.isFiStatement()
					|| ( label.isThenStatement() && ! label.isIfStatement() )
					|| label.isElseStatement()
					|| label.isEndOfWhileStatement()
					|| label.isProgramStatement()
					|| label.isEndStatement()
					)
				){
				set.add(label);
			}
		}
		
		this.programLabels = set;
	}
	
	private void calculateInitLabels() {
		LabelSet set = new LabelSet();
		
		for(CodeLine label : this.program){
			if(label.isDeclarationStatement()){
				set.add(label);
			}
		}
		
		this.initLabels = set;
	}
	
	private void calculateProgramFlow(){
		EdgeSet set = new EdgeSet();
		
		// Traverse labels				
		CodeLine currentLabel = null;
		CodeLine nextLabel    = null;
		
		for (Iterator<CodeLine> it = programLabels.iterator(); it.hasNext();){
			if(currentLabel == null) {
				currentLabel = it.next();
				nextLabel    = it.next();
			} else {
				currentLabel = nextLabel;
				nextLabel    = it.next();
			}
			
			// connecting all labels forward by default
			
			// unless they're divided by an else statement
			if((currentLabel.getIfParent() == nextLabel.getIfParent()) && (currentLabel.getElseParent() != nextLabel.getElseParent())){
				continue;
			// unless they're disconnected after a while block
			} else if (!currentLabel.isWhileStatement() && (currentLabel.getLoopParent() != nextLabel.getLoopParent())){
				continue;
			} else {
				set.add(new Edge(currentLabel, nextLabel));
			}
	    }
		
		// Traverse whole program
		for (int i = 0; i < program.size(); i++){
			currentLabel = program.get(i);
						
			if(currentLabel.isElseStatement()){				
				// Connect 'if', with first line in else block
				CodeLine followingLabel = program.get(i+1);
				set.add(new Edge(followingLabel.getIfParent(), followingLabel));						
				
				CodeLine lastLabelInTrue = null;
				for(int j = programLabels.size() - 1; j >= 0 ; j--){
					if(programLabels.get(j).getLineNumber() < currentLabel.getLineNumber()){
						lastLabelInTrue = programLabels.get(j);
						break;
					}
				}
				
				// Connect last line in true block, with first line after else block
				// it will have same if parent, but marked an else parent
				CodeLine labelAfterFi = findNextLabelAfterElseBlock(currentLabel);
				set.add(new Edge(lastLabelInTrue, labelAfterFi));
				
			} else if (currentLabel.isWhileStatement()){
				// connect last line of while blocks with their while
				set.add(new Edge(currentLabel.getEndOfWhileCodeLine(), currentLabel));
				
				CodeLine endOfWhile = currentLabel.getEndOfWhileCodeLine();
				
				// Connect while, with first line after while block
				set.add(new Edge(currentLabel, findNextLabelAfterBlock(endOfWhile)));
			}
	    }
		
		
		this.programFlow = set;
	}
	
	private CodeLine findNextLabelAfterElseBlock(CodeLine elseLabel){
		CodeLine firstLabelAfterFalse = null;
		CodeLine correspondingFiLabel = null;
		for(int j = 0; j<program.size(); j++){
			if(program.get(j).getLineNumber() > elseLabel.getLineNumber()){
				if(program.get(j).isFiStatement()){
					if(		program.get(j).getIfParent() == elseLabel.getIfParent()
							&&
							program.get(j).getElseParent() == elseLabel.getElseParent()) {
						correspondingFiLabel = program.get(j);
						break;
					}
				}
			}
		}
		
		// Now traverse down from correspondingFiLabel, and repeat process if Else is found
		firstLabelAfterFalse = findNextLabelAfterBlock(correspondingFiLabel);
				
		return firstLabelAfterFalse;
	}
	
	private CodeLine findNextLabelAfterBlock(CodeLine endBlockLabel){
		if(endBlockLabel == null){
			return null;
		}
		
		int startLine = endBlockLabel.getLineNumber();
		CodeLine nextLabelAfterBlock = null;
		
		for(int j = 0; j<program.size(); j++){
			CodeLine current = program.get(j);
			if(current.getLineNumber() > startLine){
				if(current.isElseStatement()){
					return findNextLabelAfterElseBlock(current);
				} else if (programLabels.contains(current)){
					return current;
				}
			}
		}
		
		return nextLabelAfterBlock;
	}
}
