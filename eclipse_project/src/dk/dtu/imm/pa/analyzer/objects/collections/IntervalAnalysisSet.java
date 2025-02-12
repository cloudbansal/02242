package dk.dtu.imm.pa.analyzer.objects.collections;

import java.util.ArrayList;

public class IntervalAnalysisSet extends ArrayList<IntervalAnalysis> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public IntervalAnalysisSet(IntervalAnalysisSet ias){
		super(ias);
	}
	
	public IntervalAnalysisSet(){
		super();
	}
	
	public boolean contains(IntervalAnalysisSet ias){
		boolean result = true;
		
		for(IntervalAnalysis ia : ias){
			if(!this.contains(ia)){
				result = false;
			}
		}
		
		return result;
	}
	
	public boolean contains(IntervalAnalysis ia){
		boolean result = false;
		
		for(IntervalAnalysis ourIa : this){
			if(ourIa.contains(ia)){
				result = true;
			}
		}
		
		return result;
	}
	
//	public IntervalAnalysisSet addition(IntervalAnalysisSet ias){
//		IntervalAnalysisSet newIntervalAnalysisSet = new IntervalAnalysisSet(this);
//		
//		for (IntervalAnalysis ia : ias){
//			this.add(ia);
//		}
//		
//		return newIntervalAnalysisSet;
//	}
//	
//	public IntervalAnalysisSet removal(IntervalAnalysisSet ias){
//		IntervalAnalysisSet newIntervalAnalysisSet = new IntervalAnalysisSet(this);
//		
//		for (IntervalAnalysis ia : ias){
//			this.remove(this.getByName(ia.getName()));
//		}
//		
//		return newIntervalAnalysisSet;
//	}
	
	public IntervalAnalysisSet addition(IntervalAnalysisSet intervalAnalysisSet){
		IntervalAnalysisSet newIntervalAnalysisSet = new IntervalAnalysisSet(this);
		for(IntervalAnalysis is : intervalAnalysisSet) {
			if(newIntervalAnalysisSet.containsVariableName(is.getName())){
				newIntervalAnalysisSet.getByVariableName(is.getName()).add(is);
			} else {
				newIntervalAnalysisSet.add(is);
			}
		}
		
		return newIntervalAnalysisSet;
	}
	
	public IntervalAnalysisSet removal(IntervalAnalysisSet intervalAnalysisSet){
		IntervalAnalysisSet newIntervalAnalysisSet = new IntervalAnalysisSet(this);
		
		for(IntervalAnalysis is : intervalAnalysisSet) {
			if(newIntervalAnalysisSet.containsVariableName(is.getName())){
				
				IntervalAnalysis temp = newIntervalAnalysisSet.getByVariableName(is.getName());
				temp.substract(is);
			} 
		}
		
		return newIntervalAnalysisSet;
	}
	
	
	public IntervalAnalysis getByVariableName(String name){
		for(IntervalAnalysis ia : this){
			if(ia.getName().equals(name)){
				return ia;
			}
		}
		return null;
	}
	
	public boolean containsVariableName(String name){
		for(IntervalAnalysis ia : this) {
			if(ia.getName().equals(name)){
				return true;
			}
		}
		return false;
	}	
}
