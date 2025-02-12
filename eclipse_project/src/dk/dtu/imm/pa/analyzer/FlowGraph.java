package dk.dtu.imm.pa.analyzer;

import java.util.ArrayList;
import java.util.Iterator;

import org.antlr.runtime.tree.Tree;

import dk.dtu.imm.pa.analyzer.objects.CodeLine;
import dk.dtu.imm.pa.analyzer.objects.Program;
import dk.dtu.imm.pa.analyzer.objects.Variable;
import dk.dtu.imm.pa.analyzer.objects.ArrayUse;
import dk.dtu.imm.pa.analyzer.objects.collections.DetectionOfSigns;
import dk.dtu.imm.pa.analyzer.objects.collections.DetectionOfSignsSet;
import dk.dtu.imm.pa.analyzer.objects.collections.IntervalAnalysis;
import dk.dtu.imm.pa.analyzer.objects.collections.IntervalAnalysisSet;
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
		
		this.generateArrayUsage();
	
		//this.calculateBufferOverflow("DS");
		
		//this.calculateBufferOverflow("IA");
				
//		this.calculateDetectionOfSigns();
		
		//this.calculateIntervalAnalysis();
		
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
	
	
	public void generateArrayUsage(){
		for(Variable v: program.getGlobalVariables()){
			if(v.isArray()){
				for(CodeLine l : programLabels){
					if(l.getModifiedVariables().contains(v) || l.getUsedVariables().contains(v)){
						for(int i = 0; i < l.getElements().size(); i++){
							Tree element = l.getElements().get(i);
							if(element.getType() == TheLangLexer.LBRACKET){
								if(l.getElements().get(i-1).getText().equals(v.getName())){
									ArrayUse arrayUse = new ArrayUse(l);
									if(l.getElements().get(i+1).getType() == TheLangLexer.INTEGER){
										arrayUse.setIntIndex(Integer.parseInt(l.getElements().get(i+1).getText()));
									} else {
										arrayUse.setVarIndex(program.getGlobalVariables().findByName(l.getElements().get(i+1).getText()));
									}
									
									if(v.getArrayUsage() == null)
										v.setArrayUsage(new ArrayList<ArrayUse>());
									
									v.getArrayUsage().add(arrayUse);
								}
							}
						}
					}
				}
			}
			// pop the declaration of the array always;
			if(v.getArrayUsage() != null){
				v.getArrayUsage().remove(0);
			}
		}
	}
	
	public void calculateBufferOverflow(String algorithm){
		LabelSet result = new LabelSet();
		if ("DS".equalsIgnoreCase(algorithm)){
			// We calculate first the DS
			this.calculateDetectionOfSigns();
			
		    System.out.println("Detection of Sign set, per label:");
		    for(CodeLine cl : programLabels){
		    	System.out.println(cl);
		    	System.out.println("entry: " + cl.getEntryDetectionOfSigns() + " \n exit: " + cl.getExitDetectionOfSigns());
		    	System.out.println();
		    	System.out.println();
		    }
			
			for(CodeLine l : programLabels){
				for(Variable v : l.getUsedVariables()){
					if(v.isArray()){
						for(ArrayUse au : v.getArrayUsage()){
							if(au.getLabel().equals(l)){
								if(au.isVarIndex()){
									for(DetectionOfSigns ds : l.getEntryDetectionOfSigns()){
										if(ds.getName().equals(au.getVarIndex().getName())){
											if(ds.isMinus()){
												result.add(l);
											}
										}
									}
								}
							}
						}
					}
				}
				
				for(Variable v : l.getModifiedVariables()){
					if(v.isArray()){
						for(ArrayUse au : v.getArrayUsage()){
							if(au.getLabel().equals(l)){
								if(au.isVarIndex()){
									for(DetectionOfSigns ds : l.getEntryDetectionOfSigns()){
										if(ds.getName().equals(au.getVarIndex().getName())){
											if(ds.isMinus()){
												result.add(l);
											}
										}
									}
								}
							}
						}
					}
				}
			}
		} else if ("IA".equalsIgnoreCase(algorithm)){
			int max = 0,
				min = 0;
			for(Variable v : program.getGlobalVariables()){
				if(v.isArray()){
					max = v.getArrayLength() - 1;
					this.calculateIntervalAnalysis(min, max);
					
					System.out.println("Interval Analysis set, per label for array: " + v.getName());
				    for(CodeLine cl : programLabels){
				    	System.out.println(cl);
				    	System.out.println("entry: " + cl.getEntryIntervalAnalysis() + " \n exit: " + cl.getExitIntervalAnalysis());
				    	System.out.println();
				    	System.out.println();
				    }
					
					for(CodeLine l : programLabels){
						for(ArrayUse au : v.getArrayUsage()){
							if(au.getLabel().equals(l)){
								if(au.isVarIndex()){
									for(IntervalAnalysis ia : l.getEntryIntervalAnalysis()){
										if(ia.getName().equals(au.getVarIndex().getName())){
											boolean isValid;
											
											isValid = ia.getFirstElement() <= max
													&& ia.getFirstElement() >= min
													&& ia.getLastElement() <= max
													&& ia.getLastElement() >= min;
											
											if(!isValid){
												result.add(l);
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
				
		for(CodeLine l: result){
			System.out.println("----------------------------------------------------------------");	
			System.out.println("Buffer overflow found at line: " + l.getLineNumber());
			System.out.println("Corresponding program slice is: ");
			System.out.println("----------------------------------------------------------------");
			for(CodeLine slice: calculateProgramSlice(l)){
				System.out.println(slice);
				System.out.println();
			}
			System.out.println("----------------------------------------------------------------");	
			System.out.println();
			System.out.println();
		}
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
	
		private ReachingDefinitionsSet getRDfromLabel(CodeLine l){
			ReachingDefinitionsSet result = new ReachingDefinitionsSet().addition(l.getEntryReachingDefinitions());
			
			ReachingDefinitionsSet kill = new ReachingDefinitionsSet();
			ReachingDefinitionsSet gen  = new ReachingDefinitionsSet();
			for(Variable v : l.getModifiedVariables()){
				boolean isArray = v.isArray();
				//Only kill if its not an array
				if(!isArray){
					for(CodeLine tempLabel : this.programLabels){
						kill.add(new ReachingDefinition(v.getName(), tempLabel.getLineNumber()));
						result = result.removal(kill);
						kill.clear();
					}
				}
			}
			
			for(Variable v : l.getModifiedVariables()){
				kill.add(new ReachingDefinition(v.getName(), -1));
				result = result.removal(kill);
				kill.clear();
				gen.add(new ReachingDefinition(v.getName(), l.getLineNumber()));
				result = result.addition(gen);
				gen.clear();
			}
			return result;
		}
	
	public LabelSet calculateProgramSlice(CodeLine labelOfInterest){
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
	
	private void calculateIntervalAnalysis(int min, int max){
		EdgeSet worklist = new EdgeSet();           // W = null
		worklist.addAll(this.programFlow);          // Initialization
		
		// Init labels set
		for(CodeLine l : this.programLabels){
			l.setEntryIntervalAnalysis(new IntervalAnalysisSet());
			l.setExitIntervalAnalysis(new IntervalAnalysisSet());
			
			if(this.initLabels.contains(l)){
				for(Variable v: l.getGlobalVariables()){
					l.getEntryIntervalAnalysis().add(new IntervalAnalysis(v.getName(), Double.NaN, Double.NaN ));
				}
			}
		}
		while(!worklist.isEmpty()){
			//System.out.println(worklist);			
			Edge e = worklist.remove(0); // pop!
			CodeLine label1 = e.getSource();
			CodeLine label2 = e.getDestination();
			
			label1.setExitIntervalAnalysis(getIAfromLabel(label1, min, max));
			if( ! label2.getEntryIntervalAnalysis().contains(label1.getExitIntervalAnalysis())){
				
				label2.setEntryIntervalAnalysis(label2.getEntryIntervalAnalysis().addition(label1.getExitIntervalAnalysis()));
				for(Edge f : this.programFlow){
					if(f.getSource().equals(label2)){
						worklist.add(f);
					}
				}
			}
		}
	}
	
	public IntervalAnalysisSet getIAfromLabel(CodeLine l, int min, int max){
		IntervalAnalysisSet result = new IntervalAnalysisSet().addition(l.getEntryIntervalAnalysis());
		
		IntervalAnalysisSet kill = new IntervalAnalysisSet();
		IntervalAnalysisSet gen  = new IntervalAnalysisSet();
		
		for(Variable v : l.getModifiedVariables()){
	        // order (variableName, plus, minus, zero)
			boolean isArray = v.isArray();
			//Only kill if its not an array
			if(!isArray){
				kill.add(new IntervalAnalysis(v.getName(), Double.NaN, Double.NaN));
				result = result.removal(kill);
				kill.clear();
			}
			
			if(l.isDeclarationStatement()){ // if var declaration, variable, false, false, true
				gen.add(new IntervalAnalysis(v.getName(), 0.0, 0.0));
				result = result.addition(gen);
				gen.clear();
			} else if(l.isReadStatement()){ // if read, variable, true, true, true
				gen.add(new IntervalAnalysis(v.getName(), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));
				result = result.addition(gen);
				gen.clear();
			} else if(l.isAssignmentStatement()){
				// calculate sign of statement
				ArrayList<Tree> labelElements = l.getElements();

				// if it's just a number on the right side, easy peasy
				if(l.getUsedVariables().isEmpty()){ // no used variables, but assignment, number
					// Find number
					int i;
					int numberOfElements = 0;
					for(i = 0; i < labelElements.size(); i++){
						if(labelElements.get(i).getType() == TheLangLexer.ASSIGN){
							numberOfElements = labelElements.size() - i - 2;
							i++;
							break;
						}
					}
					for(i = 0; i < labelElements.size(); i++){
						if(labelElements.get(i).getType() == TheLangLexer.INTEGER){
							int number = Integer.parseInt(labelElements.get(i).getText());
							if(numberOfElements == 1){
								if(number < max && number > min){
									gen.add(new IntervalAnalysis(v.getName(), (double) number, (double) number));
								} else if(number > max){
									gen.add(new IntervalAnalysis(v.getName(), (double) max + 1, Double.POSITIVE_INFINITY));
								} else if(number < min) {
									gen.add(new IntervalAnalysis(v.getName(), Double.NEGATIVE_INFINITY, (double) min - 1));		
								}
								
								result = result.addition(gen);
								gen.clear();
							}else if(numberOfElements == 2){
								if(-number < max && -number > min){
									gen.add(new IntervalAnalysis(v.getName(), (double) -number, (double) -number));
								} else if(-number > max){
									gen.add(new IntervalAnalysis(v.getName(), (double) max + 1, Double.POSITIVE_INFINITY));
								} else if(-number < min) {
									gen.add(new IntervalAnalysis(v.getName(), Double.NEGATIVE_INFINITY, (double) min - 1));		
								}
								
								result = result.addition(gen);
								gen.clear();
							}
						}
					}
				} else {
					if(l.isWhileStatement() || l.isIfStatement()){
//							System.out.println("Exit "+l.getLineNumber()+" returned is: "+result);
						return result;
					}
					// Find assignment, and parse expression at the right
					int i; // position of first elment after assign
					int numberOfElements = 0;
					for(i = 0; i < labelElements.size(); i++){
						if(labelElements.get(i).getType() == TheLangLexer.ASSIGN){
							numberOfElements = labelElements.size() - i - 2;
							i++;
							break;
						}
					}
					
					if(numberOfElements == 1){
						try {
							Integer.parseInt(labelElements.get(i).getText());
						} catch (NumberFormatException e) {
							String nameOfVariable = labelElements.get(i).getText();
							IntervalAnalysis ias = new IntervalAnalysis(l.getEntryIntervalAnalysis().getByVariableName(nameOfVariable));
							gen.add(new IntervalAnalysis(v.getName(),ias.getFirstElement(),ias.getLastElement()));
							result = result.addition(gen);
							gen.clear();
						}
					}else if(numberOfElements == 2){
						String nameOfVariable = labelElements.get(i+1).getText();
						IntervalAnalysis ias = new IntervalAnalysis(l.getEntryIntervalAnalysis().getByVariableName(nameOfVariable));
						
						Double firstElement = (double) - ias.getFirstElement();
						Double lastElement  = (double) - ias.getLastElement();

						if(firstElement < min){
							firstElement = Double.NEGATIVE_INFINITY;
						}
						if(lastElement > max){
							lastElement = Double.POSITIVE_INFINITY;
						}
						
						gen.add(new IntervalAnalysis(v.getName(),firstElement,lastElement));
						
						result = result.addition(gen);
						gen.clear();
					}else if(numberOfElements == 3){
						IntervalAnalysis ias1 = new IntervalAnalysis(v.getName(), Double.NaN, Double.NaN);
						IntervalAnalysis ias2 = new IntervalAnalysis(v.getName(), Double.NaN, Double.NaN);
						
						String nameOfVariable1 = labelElements.get(i).getText();
						String nameOfVariable2 = labelElements.get(i+2).getText();
						if (labelElements.get(i).getType() == TheLangLexer.INTEGER){
							Double value = Double.parseDouble(labelElements.get(i).getText());
							
							ias1 = new IntervalAnalysis(v.getName(), value, value);
							ias2 = new IntervalAnalysis(l.getEntryIntervalAnalysis().getByVariableName(nameOfVariable2));
						}else if(labelElements.get(i+2).getType() == TheLangLexer.INTEGER){
							Double value = Double.parseDouble(labelElements.get(i+2).getText());

							ias1 = new IntervalAnalysis(l.getEntryIntervalAnalysis().getByVariableName(nameOfVariable1));
							ias2 = new IntervalAnalysis(v.getName(), value, value);
						}else{
							ias1 = new IntervalAnalysis(l.getEntryIntervalAnalysis().getByVariableName(nameOfVariable1));
							ias2 = new IntervalAnalysis(l.getEntryIntervalAnalysis().getByVariableName(nameOfVariable2));
						}
						
						int operatorType = labelElements.get(i+1).getType();
						
						IntervalAnalysis ias = new IntervalAnalysis(v.getName(), Double.NaN, Double.NaN);
						
						switch(operatorType){
							case TheLangLexer.PLUS:{
								
								if(ias1.getFirstElement() == Double.NEGATIVE_INFINITY || ias2.getFirstElement() == Double.NEGATIVE_INFINITY){
									ias.setFirstElement(Double.NEGATIVE_INFINITY);
								} else if (ias1.getFirstElement() + ias2.getFirstElement() < min){
									ias.setFirstElement(Double.NEGATIVE_INFINITY);
								} else if (ias1.getFirstElement() + ias2.getFirstElement() > max){
									ias.setFirstElement((double) max + 1);
								} else {
									ias.setFirstElement(ias1.getFirstElement() + ias2.getFirstElement());
								}
								
								if(ias1.getLastElement() == Double.POSITIVE_INFINITY || ias2.getLastElement() == Double.POSITIVE_INFINITY){
								    ias.setLastElement(Double.POSITIVE_INFINITY);
								} else if (ias1.getLastElement() + ias2.getLastElement() > max){
								    ias.setLastElement(Double.POSITIVE_INFINITY);
								} else if (ias1.getLastElement() + ias2.getLastElement() < min){
								    ias.setLastElement((double) min - 1);
								} else {
								    ias.setLastElement(ias1.getLastElement() + ias2.getLastElement());
								}

								gen.add(ias);
								result = result.addition(gen);
								gen.clear();
								
								break;
							}
							case TheLangLexer.MINUS:{
								if(ias1.getFirstElement() == Double.NEGATIVE_INFINITY || ias2.getFirstElement() == Double.NEGATIVE_INFINITY){
								    ias.setFirstElement(Double.NEGATIVE_INFINITY);
								} else if (ias1.getFirstElement() - ias2.getFirstElement() < min){
								    ias.setFirstElement(Double.NEGATIVE_INFINITY);
								} else if (ias1.getFirstElement() - ias2.getFirstElement() > max){
								    ias.setFirstElement((double) max + 1);
								} else {
								    ias.setFirstElement(ias1.getFirstElement() - ias2.getFirstElement());
								}

								if(ias1.getLastElement() == Double.POSITIVE_INFINITY || ias2.getLastElement() == Double.POSITIVE_INFINITY){
								    ias.setLastElement(Double.POSITIVE_INFINITY);
								} else if (ias1.getLastElement() - ias2.getLastElement() > max){
								    ias.setLastElement(Double.POSITIVE_INFINITY);
								} else if (ias1.getLastElement() - ias2.getLastElement() < min){
								    ias.setLastElement((double) min - 1);
								} else {
								    ias.setLastElement(ias1.getLastElement() - ias2.getLastElement());
								}
								
								gen.add(ias);
								result = result.addition(gen);
								gen.clear();
								break;
							}
							case TheLangLexer.MUL:{
								double 	f1l1,
										f1l2,
										f2l1,
										f2l2;
								
								if(ias1.getFirstElement() == 0.0 || ias2.getFirstElement() == 0.0){
									f1l1 = 0;
								} else if ((ias1.getFirstElement() > 0 && ias2.getFirstElement() == Double.NEGATIVE_INFINITY) ||
										(ias1.getFirstElement() < 0 && ias2.getFirstElement() == Double.POSITIVE_INFINITY) ||
										(ias2.getFirstElement() > 0 && ias1.getFirstElement() == Double.NEGATIVE_INFINITY) ||
										(ias2.getFirstElement() < 0 && ias1.getFirstElement() == Double.POSITIVE_INFINITY))
								{
									f1l1 = Double.NEGATIVE_INFINITY;
								} else if ((ias1.getFirstElement() < 0 && ias2.getFirstElement() == Double.NEGATIVE_INFINITY) ||
										(ias1.getFirstElement() > 0 && ias2.getFirstElement() == Double.POSITIVE_INFINITY) ||
										(ias2.getFirstElement() < 0 && ias1.getFirstElement() == Double.NEGATIVE_INFINITY) ||
										(ias2.getFirstElement() > 0 && ias1.getFirstElement() == Double.POSITIVE_INFINITY))
								{
									f1l1 = Double.POSITIVE_INFINITY;
								} else if (ias1.getFirstElement() * ias2.getFirstElement() < min){
									f1l1 = Double.NEGATIVE_INFINITY;
								} else if (ias1.getFirstElement() * ias2.getFirstElement() > max){
									f1l1 = Double.POSITIVE_INFINITY;
								} else {
									f1l1 = ias1.getFirstElement() * ias2.getFirstElement();
								}
								
								if(ias1.getFirstElement() == 0.0 || ias2.getLastElement() == 0.0){
								    f1l2 = 0;
								} else if ((ias1.getFirstElement() > 0 && ias2.getLastElement() == Double.NEGATIVE_INFINITY) ||
								        (ias1.getFirstElement() < 0 && ias2.getLastElement() == Double.POSITIVE_INFINITY) ||
								        (ias2.getLastElement() > 0 && ias1.getFirstElement() == Double.NEGATIVE_INFINITY) ||
								        (ias2.getLastElement() < 0 && ias1.getFirstElement() == Double.POSITIVE_INFINITY))
								{
								    f1l2 = Double.NEGATIVE_INFINITY;
								} else if ((ias1.getFirstElement() < 0 && ias2.getLastElement() == Double.NEGATIVE_INFINITY) ||
								        (ias1.getFirstElement() > 0 && ias2.getLastElement() == Double.POSITIVE_INFINITY) ||
								        (ias2.getLastElement() < 0 && ias1.getFirstElement() == Double.NEGATIVE_INFINITY) ||
								        (ias2.getLastElement() > 0 && ias1.getFirstElement() == Double.POSITIVE_INFINITY))
								{
								    f1l2 = Double.POSITIVE_INFINITY;
								} else if (ias1.getFirstElement() * ias2.getLastElement() < min){
								    f1l2 = Double.NEGATIVE_INFINITY;
								} else if (ias1.getFirstElement() * ias2.getLastElement() > max){
								    f1l2 = Double.POSITIVE_INFINITY;
								} else {
								    f1l2 = ias1.getFirstElement() * ias2.getLastElement();
								}
								
								if(ias1.getLastElement() == 0.0 || ias2.getFirstElement() == 0.0){
								    f2l1 = 0;
								} else if ((ias1.getLastElement() > 0 && ias2.getFirstElement() == Double.NEGATIVE_INFINITY) ||
								        (ias1.getLastElement() < 0 && ias2.getFirstElement() == Double.POSITIVE_INFINITY) ||
								        (ias2.getFirstElement() > 0 && ias1.getLastElement() == Double.NEGATIVE_INFINITY) ||
								        (ias2.getFirstElement() < 0 && ias1.getLastElement() == Double.POSITIVE_INFINITY))
								{
								    f2l1 = Double.NEGATIVE_INFINITY;
								} else if ((ias1.getLastElement() < 0 && ias2.getFirstElement() == Double.NEGATIVE_INFINITY) ||
								        (ias1.getLastElement() > 0 && ias2.getFirstElement() == Double.POSITIVE_INFINITY) ||
								        (ias2.getFirstElement() < 0 && ias1.getLastElement() == Double.NEGATIVE_INFINITY) ||
								        (ias2.getFirstElement() > 0 && ias1.getLastElement() == Double.POSITIVE_INFINITY))
								{
								    f2l1 = Double.POSITIVE_INFINITY;
								} else if (ias1.getLastElement() * ias2.getFirstElement() < min){
								    f2l1 = Double.NEGATIVE_INFINITY;
								} else if (ias1.getLastElement() * ias2.getFirstElement() > max){
								    f2l1 = Double.POSITIVE_INFINITY;
								} else {
								    f2l1 = ias1.getLastElement() * ias2.getFirstElement();
								}
								
								if(ias1.getLastElement() == 0.0 || ias2.getLastElement() == 0.0){
								    f2l2 = 0;
								} else if ((ias1.getLastElement() > 0 && ias2.getLastElement() == Double.NEGATIVE_INFINITY) ||
								        (ias1.getLastElement() < 0 && ias2.getLastElement() == Double.POSITIVE_INFINITY) ||
								        (ias2.getLastElement() > 0 && ias1.getLastElement() == Double.NEGATIVE_INFINITY) ||
								        (ias2.getLastElement() < 0 && ias1.getLastElement() == Double.POSITIVE_INFINITY))
								{
								    f2l2 = Double.NEGATIVE_INFINITY;
								} else if ((ias1.getLastElement() < 0 && ias2.getLastElement() == Double.NEGATIVE_INFINITY) ||
								        (ias1.getLastElement() > 0 && ias2.getLastElement() == Double.POSITIVE_INFINITY) ||
								        (ias2.getLastElement() < 0 && ias1.getLastElement() == Double.NEGATIVE_INFINITY) ||
								        (ias2.getLastElement() > 0 && ias1.getLastElement() == Double.POSITIVE_INFINITY))
								{
								    f2l2 = Double.POSITIVE_INFINITY;
								} else if (ias1.getLastElement() * ias2.getLastElement() < min){
								    f2l2 = Double.NEGATIVE_INFINITY;
								} else if (ias1.getLastElement() * ias2.getLastElement() > max){
								    f2l2 = Double.POSITIVE_INFINITY;
								} else {
								    f2l2 = ias1.getLastElement() * ias2.getLastElement();
								}
								
								ias.setFirstElement(Math.min( Math.min(f2l1, f2l2), Math.min(f1l1, f1l2) ));
								ias.setLastElement(Math.max( Math.max(f2l1, f2l2), Math.max(f1l1, f1l2) ));
								
								gen.add(ias);
								result = result.addition(gen);
								gen.clear();
								break;
							}
							case TheLangLexer.DIV:{
								double 	f1l1,
										f1l2,
										f2l1,
										f2l2;
								
								if(ias2.getFirstElement() <= 0.0 && ias2.getLastElement() >= 0.0){
									ias.setFirstElement(Double.NaN);
									ias.setLastElement(Double.NaN);
								} else {
									if ((ias1.getFirstElement() == Double.POSITIVE_INFINITY && ias2.getFirstElement() == Double.POSITIVE_INFINITY)
										|| (ias1.getFirstElement() == Double.NEGATIVE_INFINITY && ias2.getFirstElement() == Double.NEGATIVE_INFINITY))
									{
										f1l1 = 1;
									} else if ((ias1.getFirstElement() == Double.NEGATIVE_INFINITY && ias2.getFirstElement() == Double.POSITIVE_INFINITY)
											|| (ias1.getFirstElement() == Double.POSITIVE_INFINITY && ias2.getFirstElement() == Double.NEGATIVE_INFINITY))
									{
										f1l1 = -1;
									} else if (ias1.getFirstElement() == 0.0
											|| ias2.getFirstElement() == Double.POSITIVE_INFINITY
											|| ias2.getFirstElement() == Double.NEGATIVE_INFINITY)
									{
										f1l1 = 0;
									} else if (ias1.getFirstElement() == Double.NEGATIVE_INFINITY)
									{
										f1l1 = Double.NEGATIVE_INFINITY;
									} else if (ias1.getFirstElement() == Double.POSITIVE_INFINITY)
									{
										f1l1 = Double.POSITIVE_INFINITY;
									} else if (ias1.getFirstElement() / ias2.getFirstElement() < min){
										f1l1 = Double.NEGATIVE_INFINITY;
									} else if (ias1.getFirstElement() / ias2.getFirstElement() > max){
										f1l1 = Double.POSITIVE_INFINITY;
									} else {
										f1l1 = (int) (ias1.getFirstElement() / ias2.getFirstElement());
									}
									
									if ((ias1.getFirstElement() == Double.POSITIVE_INFINITY && ias2.getLastElement() == Double.POSITIVE_INFINITY)
									    || (ias1.getFirstElement() == Double.NEGATIVE_INFINITY && ias2.getLastElement() == Double.NEGATIVE_INFINITY))
									{
									    f1l2 = 1;
									} else if ((ias1.getFirstElement() == Double.NEGATIVE_INFINITY && ias2.getLastElement() == Double.POSITIVE_INFINITY)
									        || (ias1.getFirstElement() == Double.POSITIVE_INFINITY && ias2.getLastElement() == Double.NEGATIVE_INFINITY))
									{
									    f1l2 = -1;
									} else if (ias1.getFirstElement() == 0.0
									        || ias2.getLastElement() == Double.POSITIVE_INFINITY
									        || ias2.getLastElement() == Double.NEGATIVE_INFINITY)
									{
									    f1l2 = 0;
									} else if (ias1.getFirstElement() == Double.NEGATIVE_INFINITY)
									{
									    f1l2 = Double.NEGATIVE_INFINITY;
									} else if (ias1.getFirstElement() == Double.POSITIVE_INFINITY)
									{
									    f1l2 = Double.POSITIVE_INFINITY;
									} else if (ias1.getFirstElement() / ias2.getLastElement() < min){
									    f1l2 = Double.NEGATIVE_INFINITY;
									} else if (ias1.getFirstElement() / ias2.getLastElement() > max){
									    f1l2 = Double.POSITIVE_INFINITY;
									} else {
									    f1l2 = (int) (ias1.getFirstElement() / ias2.getLastElement());
									}
									
									if ((ias1.getLastElement() == Double.POSITIVE_INFINITY && ias2.getFirstElement() == Double.POSITIVE_INFINITY)
									    || (ias1.getLastElement() == Double.NEGATIVE_INFINITY && ias2.getFirstElement() == Double.NEGATIVE_INFINITY))
									{
									    f2l1 = 1;
									} else if ((ias1.getLastElement() == Double.NEGATIVE_INFINITY && ias2.getFirstElement() == Double.POSITIVE_INFINITY)
									        || (ias1.getLastElement() == Double.POSITIVE_INFINITY && ias2.getFirstElement() == Double.NEGATIVE_INFINITY))
									{
									    f2l1 = -1;
									} else if (ias1.getLastElement() == 0.0
									        || ias2.getFirstElement() == Double.POSITIVE_INFINITY
									        || ias2.getFirstElement() == Double.NEGATIVE_INFINITY)
									{
									    f2l1 = 0;
									} else if (ias1.getLastElement() == Double.NEGATIVE_INFINITY)
									{
									    f2l1 = Double.NEGATIVE_INFINITY;
									} else if (ias1.getLastElement() == Double.POSITIVE_INFINITY)
									{
									    f2l1 = Double.POSITIVE_INFINITY;
									} else if (ias1.getLastElement() / ias2.getFirstElement() < min){
									    f2l1 = Double.NEGATIVE_INFINITY;
									} else if (ias1.getLastElement() / ias2.getFirstElement() > max){
									    f2l1 = Double.POSITIVE_INFINITY;
									} else {
									    f2l1 = (int) (ias1.getLastElement() / ias2.getFirstElement());
									}
									
									if ((ias1.getLastElement() == Double.POSITIVE_INFINITY && ias2.getLastElement() == Double.POSITIVE_INFINITY)
									    || (ias1.getLastElement() == Double.NEGATIVE_INFINITY && ias2.getLastElement() == Double.NEGATIVE_INFINITY))
									{
									    f2l2 = 1;
									} else if ((ias1.getLastElement() == Double.NEGATIVE_INFINITY && ias2.getLastElement() == Double.POSITIVE_INFINITY)
									        || (ias1.getLastElement() == Double.POSITIVE_INFINITY && ias2.getLastElement() == Double.NEGATIVE_INFINITY))
									{
									    f2l2 = -1;
									} else if (ias1.getLastElement() == 0.0
									        || ias2.getLastElement() == Double.POSITIVE_INFINITY
									        || ias2.getLastElement() == Double.NEGATIVE_INFINITY)
									{
									    f2l2 = 0;
									} else if (ias1.getLastElement() == Double.NEGATIVE_INFINITY)
									{
									    f2l2 = Double.NEGATIVE_INFINITY;
									} else if (ias1.getLastElement() == Double.POSITIVE_INFINITY)
									{
									    f2l2 = Double.POSITIVE_INFINITY;
									} else if (ias1.getLastElement() / ias2.getLastElement() < min){
									    f2l2 = Double.NEGATIVE_INFINITY;
									} else if (ias1.getLastElement() / ias2.getLastElement() > max){
									    f2l2 = Double.POSITIVE_INFINITY;
									} else {
									    f2l2 = (int) (ias1.getLastElement() / ias2.getLastElement());
									}
									
									ias.setFirstElement(Math.min( Math.min(f2l1, f2l2), Math.min(f1l1, f1l2) ));
									ias.setLastElement(Math.max( Math.max(f2l1, f2l2), Math.max(f1l1, f1l2) ));
								}
								
								
								
								gen.add(ias);
								result = result.addition(gen);
								gen.clear();
								break;
							}
						}
					}
				}
			}		
		}


		return result;
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
//        	System.out.println(worklist);
        	Edge e = worklist.remove(0); // pop!
            CodeLine label1 = e.getSource();
            CodeLine label2 = e.getDestination();
            
            if(label1.isIfStatement()){
            	if(label2.getElseParent() == null){ // we are in true block
            		label1.setExitDetectionOfSigns(getTrueDSfromLabel(label1));
            	} else { // we are in false block
            		label1.setExitDetectionOfSigns(getFalseDSfromLabel(label1));
            	}
            } else if (label1.isWhileStatement()){
            	if(label2.getLoopParent() == label1){ // we are in true block
            		label1.setExitDetectionOfSigns(getTrueDSfromLabel(label1));
            	} else { // we are in false block
            		label1.setExitDetectionOfSigns(getFalseDSfromLabel(label1));
            	}
            } else {
                label1.setExitDetectionOfSigns(getDSfromLabel(label1));
            }
            
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
	
	private DetectionOfSignsSet getTrueDSfromLabel(CodeLine l){
		DetectionOfSignsSet result = new DetectionOfSignsSet();
		
		DetectionOfSignsSet labelEntryDos = l.getEntryDetectionOfSigns();
		result = result.addition(labelEntryDos);
		
		BooleanExpression be = l.getBooleanExpression();
		
		DetectionOfSigns firstOperatingDos  = new DetectionOfSigns("", false, false, false);
		DetectionOfSigns secondOperatingDos = new DetectionOfSigns("", false, false, false);

		// First variable
		if(be.isFirstVariableIsNumber()){
			if(Integer.parseInt(be.getFirstVariable()) == 0){
				firstOperatingDos = new DetectionOfSigns("", false, false, true);
			} else if(be.isFirstVariableNegated()){
				firstOperatingDos = new DetectionOfSigns("", false, true, false);
			} else {
				firstOperatingDos = new DetectionOfSigns("", true, false, false);
			}
		} else {
			firstOperatingDos = new DetectionOfSigns(labelEntryDos.getByVariableName(be.getFirstVariable()));
			if(be.isFirstVariableNegated()){
				firstOperatingDos = new DetectionOfSigns(be.getFirstVariable(), firstOperatingDos.isMinus(), firstOperatingDos.isPlus(), firstOperatingDos.isZero());
			} 
		}
		
		// Second variable
		if(be.isSecondVariableIsNumber()){
			if(Integer.parseInt(be.getSecondVariable()) == 0){
				secondOperatingDos = new DetectionOfSigns("", false, false, true);
			} else if(be.isSecondVariableNegated()){
				secondOperatingDos = new DetectionOfSigns("", false, true, false);
			} else {
				secondOperatingDos = new DetectionOfSigns("", true, false, false);
			}
		} else {
			secondOperatingDos = new DetectionOfSigns(labelEntryDos.getByVariableName(be.getSecondVariable()));
			if(be.isSecondVariableNegated()){
				secondOperatingDos = new DetectionOfSigns(be.getSecondVariable(), secondOperatingDos.isMinus(), secondOperatingDos.isPlus(), secondOperatingDos.isZero());
			} 
		}
		
		boolean var1plus  = firstOperatingDos.isPlus(),
				var1minus = firstOperatingDos.isMinus(),
				var1zero  = firstOperatingDos.isZero(),
				
				var2plus  = secondOperatingDos.isPlus(), 
				var2minus = secondOperatingDos.isMinus(),
				var2zero  = secondOperatingDos.isZero();
		
		
		switch(be.getOperator()){
			case TheLangLexer.EQ: {
				var1plus  = firstOperatingDos.isPlus() && secondOperatingDos.isPlus();
				var1minus = firstOperatingDos.isMinus() && secondOperatingDos.isMinus(); 
				var1zero  = firstOperatingDos.isZero() && secondOperatingDos.isZero();
				
				var2plus  = firstOperatingDos.isPlus() && secondOperatingDos.isPlus(); 
				var2minus = firstOperatingDos.isMinus() && secondOperatingDos.isMinus(); 
				var2zero  = firstOperatingDos.isZero() && secondOperatingDos.isZero();
				break;
			}
			case TheLangLexer.NEQ: {
				var1plus  = firstOperatingDos.isPlus();
				var1minus = firstOperatingDos.isMinus();
				var1zero  = firstOperatingDos.isZero() && (secondOperatingDos.isMinus() || secondOperatingDos.isPlus());
				
				var2plus  = secondOperatingDos.isPlus(); 
				var2minus = secondOperatingDos.isMinus();
				var2zero  = secondOperatingDos.isZero() && (firstOperatingDos.isMinus() || firstOperatingDos.isPlus());			
				break;
			}
			case TheLangLexer.GT: {
				var1plus  = firstOperatingDos.isPlus();
				var1minus = firstOperatingDos.isMinus() && secondOperatingDos.isMinus();
				var1zero  = firstOperatingDos.isZero() && secondOperatingDos.isMinus();
				
				var2plus  = firstOperatingDos.isPlus() && secondOperatingDos.isPlus(); 
				var2minus = secondOperatingDos.isMinus();
				var2zero  = firstOperatingDos.isPlus() && secondOperatingDos.isZero();
				break;
			}
			case TheLangLexer.GE: {
				var1plus  = firstOperatingDos.isPlus();
				var1minus = firstOperatingDos.isMinus() && secondOperatingDos.isMinus();
				var1zero  = firstOperatingDos.isZero() && (secondOperatingDos.isPlus() || secondOperatingDos.isZero());
				
				var2plus  = secondOperatingDos.isPlus() && firstOperatingDos.isPlus(); 
				var2minus = secondOperatingDos.isMinus();
				var2zero  = (firstOperatingDos.isPlus() || firstOperatingDos.isZero()) && secondOperatingDos.isZero();
				break;
			}
			case TheLangLexer.LT: {
				var1plus  = firstOperatingDos.isPlus() && secondOperatingDos.isPlus();
				var1minus = firstOperatingDos.isMinus();
				var1zero  = firstOperatingDos.isZero() && secondOperatingDos.isPlus();
				
				var2plus  = secondOperatingDos.isPlus(); 
				var2minus = secondOperatingDos.isMinus() && firstOperatingDos.isMinus();
				var2zero  = firstOperatingDos.isMinus() && secondOperatingDos.isZero();
				break;
			}
			case TheLangLexer.LE: {
				var1plus  = secondOperatingDos.isPlus() && firstOperatingDos.isPlus();;
				var1minus = firstOperatingDos.isMinus();
				var1zero  = firstOperatingDos.isZero() && (secondOperatingDos.isPlus() || secondOperatingDos.isZero());
				
				var2plus  = secondOperatingDos.isPlus(); 
				var2minus = firstOperatingDos.isMinus() && secondOperatingDos.isMinus();
				var2zero  = secondOperatingDos.isZero() && (firstOperatingDos.isMinus() || firstOperatingDos.isZero());
				break;
			}
		}
		
		firstOperatingDos  = new DetectionOfSigns(firstOperatingDos.getName(),  var1plus, var1minus, var1zero);
		secondOperatingDos = new DetectionOfSigns(secondOperatingDos.getName(), var2plus, var2minus, var2zero);
		
		DetectionOfSignsSet kill = new DetectionOfSignsSet();
		DetectionOfSignsSet gen  = new DetectionOfSignsSet();
		
		// First variable
		if(be.isFirstVariableIsNumber()){
			kill.add(new DetectionOfSigns(secondOperatingDos.getName(), true, true, true));
			result = result.removal(kill);
			kill.clear();
			
			gen.add(secondOperatingDos);
			result = result.addition(gen);
			gen.clear();
		} else if(be.isSecondVariableIsNumber()){
			kill.add(new DetectionOfSigns(firstOperatingDos.getName(), true, true, true));
			result = result.removal(kill);
			kill.clear();
			
			gen.add(firstOperatingDos);
			result = result.addition(gen);
			gen.clear();
		} else {
			kill.add(new DetectionOfSigns(firstOperatingDos.getName(), true, true, true));
			result = result.removal(kill);
			kill.clear();
			
			kill.add(new DetectionOfSigns(secondOperatingDos.getName(), true, true, true));
			result = result.removal(kill);
			kill.clear();
			
			gen.add(firstOperatingDos);
			result = result.addition(gen);
			gen.clear();
			
			gen.add(secondOperatingDos);
			result = result.addition(gen);
			gen.clear();
		}
		
		return result;
	}
	
	private DetectionOfSignsSet getFalseDSfromLabel(CodeLine l){
		DetectionOfSignsSet result = new DetectionOfSignsSet();
		
		DetectionOfSignsSet labelEntryDos = l.getEntryDetectionOfSigns();
		result = result.addition(labelEntryDos);
		
		BooleanExpression be = l.getBooleanExpression();
		
		DetectionOfSigns firstOperatingDos  = new DetectionOfSigns("", false, false, false);
		DetectionOfSigns secondOperatingDos = new DetectionOfSigns("", false, false, false);

		// First variable
		if(be.isFirstVariableIsNumber()){
			if(Integer.parseInt(be.getFirstVariable()) == 0){
				firstOperatingDos = new DetectionOfSigns("", false, false, true);
			} else if(be.isFirstVariableNegated()){
				firstOperatingDos = new DetectionOfSigns("", false, true, false);
			} else {
				firstOperatingDos = new DetectionOfSigns("", true, false, false);
			}
		} else {
			firstOperatingDos = new DetectionOfSigns(labelEntryDos.getByVariableName(be.getFirstVariable()));
			if(be.isFirstVariableNegated()){
				firstOperatingDos = new DetectionOfSigns(be.getFirstVariable(), firstOperatingDos.isMinus(), firstOperatingDos.isPlus(), firstOperatingDos.isZero());
			} 
		}
		
		// Second variable
		if(be.isSecondVariableIsNumber()){
			if(Integer.parseInt(be.getSecondVariable()) == 0){
				secondOperatingDos = new DetectionOfSigns("", false, false, true);
			} else if(be.isSecondVariableNegated()){
				secondOperatingDos = new DetectionOfSigns("", false, true, false);
			} else {
				secondOperatingDos = new DetectionOfSigns("", true, false, false);
			}
		} else {
			secondOperatingDos = new DetectionOfSigns(labelEntryDos.getByVariableName(be.getSecondVariable()));
			if(be.isSecondVariableNegated()){
				secondOperatingDos = new DetectionOfSigns(be.getSecondVariable(), secondOperatingDos.isMinus(), secondOperatingDos.isPlus(), secondOperatingDos.isZero());
			} 
		}
		
		boolean var1plus  = firstOperatingDos.isPlus(),
				var1minus = firstOperatingDos.isMinus(),
				var1zero  = firstOperatingDos.isZero(),
				
				var2plus  = secondOperatingDos.isPlus(), 
				var2minus = secondOperatingDos.isMinus(),
				var2zero  = secondOperatingDos.isZero();
		
		
		switch(be.getOperator()){
			case TheLangLexer.EQ: {
				var1plus  = firstOperatingDos.isPlus();
				var1minus = firstOperatingDos.isMinus(); 
				var1zero  = firstOperatingDos.isZero() && (secondOperatingDos.isPlus() || secondOperatingDos.isMinus());
				
				var2plus  = firstOperatingDos.isPlus() && secondOperatingDos.isPlus(); 
				var2minus = firstOperatingDos.isMinus() && secondOperatingDos.isMinus(); 
				var2zero  = firstOperatingDos.isZero() && (secondOperatingDos.isPlus() || secondOperatingDos.isMinus());
				break;
			}
			case TheLangLexer.NEQ: {
				var1plus  = firstOperatingDos.isPlus() && secondOperatingDos.isPlus();
				var1minus = firstOperatingDos.isMinus();
				var1zero  = firstOperatingDos.isZero() && secondOperatingDos.isZero();
				
				var2plus  = firstOperatingDos.isPlus() && secondOperatingDos.isPlus(); 
				var2minus = firstOperatingDos.isMinus() && secondOperatingDos.isMinus();
				var2zero  = firstOperatingDos.isZero() && secondOperatingDos.isZero();			
				break;
			}
			case TheLangLexer.LE: {
				var1plus  = firstOperatingDos.isPlus();
				var1minus = firstOperatingDos.isMinus() && secondOperatingDos.isMinus();
				var1zero  = firstOperatingDos.isZero() && secondOperatingDos.isMinus();
				
				var2plus  = firstOperatingDos.isPlus() && secondOperatingDos.isPlus(); 
				var2minus = secondOperatingDos.isMinus();
				var2zero  = firstOperatingDos.isPlus() && secondOperatingDos.isZero();
				break;
			}
			case TheLangLexer.LT: {
				var1plus  = firstOperatingDos.isPlus();
				var1minus = firstOperatingDos.isMinus() && secondOperatingDos.isMinus();
				var1zero  = firstOperatingDos.isZero() && (secondOperatingDos.isPlus() || secondOperatingDos.isZero());
				
				var2plus  = secondOperatingDos.isPlus() && firstOperatingDos.isPlus(); 
				var2minus = secondOperatingDos.isMinus();
				var2zero  = (firstOperatingDos.isPlus() || firstOperatingDos.isZero()) && secondOperatingDos.isZero();
				break;
			}
			case TheLangLexer.GE: {
				var1plus  = firstOperatingDos.isPlus() && secondOperatingDos.isPlus();
				var1minus = firstOperatingDos.isMinus();
				var1zero  = firstOperatingDos.isZero() && secondOperatingDos.isPlus();
				
				var2plus  = secondOperatingDos.isPlus(); 
				var2minus = secondOperatingDos.isMinus() && firstOperatingDos.isMinus();
				var2zero  = firstOperatingDos.isMinus() && secondOperatingDos.isZero();
				break;
			}
			case TheLangLexer.GT: {
				var1plus  = secondOperatingDos.isPlus() && firstOperatingDos.isPlus();;
				var1minus = firstOperatingDos.isMinus();
				var1zero  = firstOperatingDos.isZero() && (secondOperatingDos.isPlus() || secondOperatingDos.isZero());
				
				var2plus  = secondOperatingDos.isPlus(); 
				var2minus = firstOperatingDos.isMinus() && secondOperatingDos.isMinus();
				var2zero  = secondOperatingDos.isZero() && (firstOperatingDos.isMinus() || firstOperatingDos.isZero());
				break;
			}
		}
		
		firstOperatingDos  = new DetectionOfSigns(firstOperatingDos.getName(),  var1plus, var1minus, var1zero);
		secondOperatingDos = new DetectionOfSigns(secondOperatingDos.getName(), var2plus, var2minus, var2zero);
		
		DetectionOfSignsSet kill = new DetectionOfSignsSet();
		DetectionOfSignsSet gen  = new DetectionOfSignsSet();
		
		// First variable
		if(be.isFirstVariableIsNumber()){
			kill.add(new DetectionOfSigns(secondOperatingDos.getName(), true, true, true));
			result = result.removal(kill);
			kill.clear();
			
			gen.add(secondOperatingDos);
			result = result.addition(gen);
			gen.clear();
		} else if(be.isSecondVariableIsNumber()){
			kill.add(new DetectionOfSigns(firstOperatingDos.getName(), true, true, true));
			result = result.removal(kill);
			kill.clear();
			
			gen.add(firstOperatingDos);
			result = result.addition(gen);
			gen.clear();
		} else {
			kill.add(new DetectionOfSigns(firstOperatingDos.getName(), true, true, true));
			result = result.removal(kill);
			kill.clear();
			
			kill.add(new DetectionOfSigns(secondOperatingDos.getName(), true, true, true));
			result = result.removal(kill);
			kill.clear();
			
			gen.add(firstOperatingDos);
			result = result.addition(gen);
			gen.clear();
			
			gen.add(secondOperatingDos);
			result = result.addition(gen);
			gen.clear();
		}
		
		return result;
	}

	private DetectionOfSignsSet getDSfromLabel(CodeLine l){
		DetectionOfSignsSet result = new DetectionOfSignsSet().addition(l.getEntryDetectionOfSigns());		
		DetectionOfSignsSet kill = new DetectionOfSignsSet();
		DetectionOfSignsSet gen = new DetectionOfSignsSet();
		// if variablehasparent  find the parent of block, and get if true or false (always true in while)
		//                       if we're in the true block, run trueFunction(), and else, falseFunction()

		// Set just contains one modified variable each time
		String modifiedVariable;
		
		for(Variable v : l.getModifiedVariables()){
	        // order (variableName, plus, minus, zero)

			boolean isArray = v.isArray();
			//Only kill if its not an array
			if(!isArray){
				kill.add(new DetectionOfSigns(v.getName(), true, true, true));
				result = result.removal(kill);
				kill.clear();
			}
			
			if(l.isDeclarationStatement()){ // if var declaration, variable, false, false, true
				gen.add(new DetectionOfSigns(v.getName(), false, false, true));
				result = result.addition(gen);
				gen.clear();
			} else if(l.isReadStatement()){ // if read, variable, true, true, true
				gen.add(new DetectionOfSigns(v.getName(), true, true, true));
				result = result.addition(gen);
				gen.clear();
			} else if(l.isAssignmentStatement()){
				// calculate sign of statement
				ArrayList<Tree> labelElements = l.getElements();

				// if it's just a number on the right side, easy peasy
				if(l.getUsedVariables().isEmpty()){ // no used variables, but assignment, number
					// Find number
					int i;
					int numberOfElements = 0;
					for(i = 0; i < labelElements.size(); i++){
						if(labelElements.get(i).getType() == TheLangLexer.ASSIGN){
							numberOfElements = labelElements.size() - i - 2;
							i++;
							break;
						}
					}
					for(i = 0; i < labelElements.size(); i++){
						if(labelElements.get(i).getType() == TheLangLexer.INTEGER){
							int number = Integer.parseInt(labelElements.get(i).getText());
							if(numberOfElements == 1){
								gen.add(new DetectionOfSigns(v.getName(), number > 0, false, number == 0));
								result = result.addition(gen);
								gen.clear();
							}else if(numberOfElements == 2){
								gen.add(new DetectionOfSigns(v.getName(), false, number > 0, number == 0));
								result = result.addition(gen);
								gen.clear();
							}
						}
					}
				} else {
					if(l.isWhileStatement() || l.isIfStatement()){
//						System.out.println("Exit "+l.getLineNumber()+" returned is: "+result);
						return result;
					}
					// Find assignment, and parse expression at the right
					int i; // position of first elment after assign
					int numberOfElements = 0;
					for(i = 0; i < labelElements.size(); i++){
						if(labelElements.get(i).getType() == TheLangLexer.ASSIGN){
							numberOfElements = labelElements.size() - i - 2;
							i++;
							break;
						}
					}
					
					if(numberOfElements == 1){
						try {
							Integer.parseInt(labelElements.get(i).getText());
						} catch (NumberFormatException e) {
							String nameOfVariable = labelElements.get(i).getText();
							DetectionOfSigns dos = new DetectionOfSigns(l.getEntryDetectionOfSigns().getByVariableName(nameOfVariable));
							gen.add(new DetectionOfSigns(v.getName(),dos.isPlus(),dos.isMinus(),dos.isZero()));
							result = result.addition(gen);
							gen.clear();
						}
					}else if(numberOfElements == 2){
						String nameOfVariable = labelElements.get(i+1).getText();
						DetectionOfSigns dos = new DetectionOfSigns(l.getEntryDetectionOfSigns().getByVariableName(nameOfVariable));
						gen.add(new DetectionOfSigns(v.getName(),!dos.isPlus(),!dos.isMinus(),dos.isZero()));
						result = result.addition(gen);
						gen.clear();
					}else if(numberOfElements == 3){
						DetectionOfSigns dos1 = new DetectionOfSigns(v.getName(), false,false,false);
						DetectionOfSigns dos2 = new DetectionOfSigns(v.getName(), false,false,false);
						
						String nameOfVariable1 = labelElements.get(i).getText();
						String nameOfVariable2 = labelElements.get(i+2).getText();
						if (labelElements.get(i).getType() == TheLangLexer.INTEGER){
							dos1 = new DetectionOfSigns(v.getName(), true,false,false);
							dos2 = new DetectionOfSigns(l.getEntryDetectionOfSigns().getByVariableName(nameOfVariable2));
						}else if(labelElements.get(i+2).getType() == TheLangLexer.INTEGER){
							dos1 = new DetectionOfSigns(l.getEntryDetectionOfSigns().getByVariableName(nameOfVariable1));
							dos2 = new DetectionOfSigns(v.getName(), true,false,false);
						}else{
							dos1 = new DetectionOfSigns(l.getEntryDetectionOfSigns().getByVariableName(nameOfVariable1));
							dos2 = new DetectionOfSigns(l.getEntryDetectionOfSigns().getByVariableName(nameOfVariable2));
						}
						
						int operatorType = labelElements.get(i+1).getType();
						
						switch(operatorType){
							case TheLangLexer.PLUS:{
								DetectionOfSigns dos = new DetectionOfSigns(v.getName(),
										(dos1.isPlus()||dos2.isPlus())
										,(dos1.isMinus()||dos2.isMinus()),
										((dos1.isPlus()&&dos2.isMinus())||(dos1.isMinus()&&dos2.isPlus())||(dos1.isZero()&&dos2.isZero())));
								gen.add(dos);
								result = result.addition(gen);
								gen.clear();
								
								break;
							}
							case TheLangLexer.MINUS:{
								DetectionOfSigns dos = new DetectionOfSigns(v.getName(),
										((dos2.isMinus()) ||(dos1.isPlus()))
										,(dos1.isMinus()||dos2.isPlus()),
										((dos1.isPlus()&&dos2.isPlus())||(dos1.isMinus()&&dos2.isMinus())||(dos1.isZero()&&dos2.isZero())));
								gen.add(dos);
								result = result.addition(gen);
								gen.clear();
								break;
							}
							case TheLangLexer.MUL:{
								DetectionOfSigns dos = new DetectionOfSigns(v.getName(),
										((dos1.isPlus()&&dos2.isPlus()))||(dos1.isMinus()&&dos2.isMinus())
										,((dos1.isPlus()&&dos2.isMinus())||(dos1.isMinus()&&dos2.isPlus())),
										(dos1.isZero()||dos2.isZero()));
								gen.add(dos);
								result = result.addition(gen);
								gen.clear();
								break;
							}
							case TheLangLexer.DIV:{
								if (dos2.isZero()){
									DetectionOfSigns dos = new DetectionOfSigns(v.getName(),false,false,false);
									gen.add(dos);
									result = result.addition(gen);
									gen.clear();
									break;
								}
								else{	
									DetectionOfSigns dos = new DetectionOfSigns(v.getName(),
											((dos1.isPlus()&&dos2.isPlus()))||(dos1.isMinus()&&dos2.isMinus())
											,((dos1.isPlus()&&dos2.isMinus())||(dos1.isMinus()&&dos2.isPlus())),
											(dos1.isZero()));
									gen.add(dos);
									result = result.addition(gen);
									gen.clear();
									break;
								}
							}
							
						}
						
					}

				}
			}		
		}
			
		return result;
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
