package dk.dtu.imm.pa.analyzer.objects.collections;

import java.util.ArrayList;

import dk.dtu.imm.pa.analyzer.objects.CodeLine;

public class DetectionOfSignsSet extends ArrayList<DetectionOfSigns>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public DetectionOfSignsSet(DetectionOfSignsSet detectionOfSignsSet){
		super(detectionOfSignsSet);
	}
	
	public DetectionOfSignsSet() {
		super();
	}

	public DetectionOfSignsSet addition(DetectionOfSignsSet detectionOfSignsSet){
		DetectionOfSignsSet newDetectionOfSignsSet = new DetectionOfSignsSet(this);
		for(DetectionOfSigns ds : detectionOfSignsSet) {
			if(newDetectionOfSignsSet.containsVariableName(ds.getName())){
				newDetectionOfSignsSet.getByVariableName(ds.getName()).add(ds);
				
			} else {
				
				newDetectionOfSignsSet.add(ds);
			}
		}
		
		return newDetectionOfSignsSet;
	}
	
	public DetectionOfSignsSet removal(DetectionOfSignsSet detectionOfSignsSet){
		DetectionOfSignsSet newDetectionOfSignsSet = new DetectionOfSignsSet(this);
		
		for(DetectionOfSigns ds : detectionOfSignsSet) {
			if(newDetectionOfSignsSet.containsVariableName(ds.getName())){
				
				DetectionOfSigns temp = newDetectionOfSignsSet.getByVariableName(ds.getName());
				temp.substract(ds);
				if(!(temp.isMinus() || temp.isPlus() || temp.isZero())){
					newDetectionOfSignsSet.remove(newDetectionOfSignsSet.getByVariableName(ds.getName()));
				}
			} 
		}
		
		return newDetectionOfSignsSet;
	}
	
	public boolean containsVariableName(String name){
		for(DetectionOfSigns ds : this) {
			if(ds.getName().equals(name)){
				return true;
			}
		}
		return false;
	}
	
	public DetectionOfSigns getByVariableName(String name){
		for(DetectionOfSigns ds : this){
			if(ds.getName().equals(name)){
				return ds;
			}
		}
		return null;
	}
	
	public boolean contains(DetectionOfSignsSet detectionOfSignsSet){
		boolean result = true;
		
		for(DetectionOfSigns ds: detectionOfSignsSet ){
			result &= this.contains(ds);
		}
		
		return result;
	}
	
	public boolean contains(DetectionOfSigns detectionOfSigns){
		boolean result = false;
		
		boolean bothContainPlus  = false;
		boolean bothContainMinus = false;
		boolean bothContainZero  = false;
		boolean bothContainPlusresult  = false;
		boolean bothContainMinusresult = false;
		boolean bothContainZeroresult  = false;
		
		
		for(DetectionOfSigns ds : this ){
			if(detectionOfSigns.getName().equals(ds.getName())){
				bothContainPlus  = detectionOfSigns.isPlus()  && ds.isPlus();
				bothContainMinus = detectionOfSigns.isMinus() && ds.isMinus();
				bothContainZero  = detectionOfSigns.isZero()  && ds.isZero();
				
				bothContainPlusresult  = bothContainPlus  == detectionOfSigns.isPlus();
				bothContainMinusresult = bothContainMinus == detectionOfSigns.isMinus();
				bothContainZeroresult  = bothContainZero  == detectionOfSigns.isZero();
			}
		}
		
		result = bothContainPlusresult && bothContainMinusresult && bothContainZeroresult;
		
		return result;
	}
	
}
