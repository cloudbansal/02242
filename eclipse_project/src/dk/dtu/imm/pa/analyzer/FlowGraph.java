package dk.dtu.imm.pa.analyzer;

import java.util.Iterator;

import dk.dtu.imm.pa.analyzer.objects.CodeLine;
import dk.dtu.imm.pa.analyzer.objects.Program;

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
		
		System.out.println(correspondingFiLabel);
		
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