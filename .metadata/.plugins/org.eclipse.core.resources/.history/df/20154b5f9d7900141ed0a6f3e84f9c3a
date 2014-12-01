package dk.dtu.imm.pa.analyzer.objects.collections;

public class IntervalAnalysis {
	private String name;
	private Double firstElement;
	private Double lastElement;
	
	public IntervalAnalysis(String name, Double firstElement, Double lastElement) {
		super();
		this.name = name;
		this.firstElement = firstElement;
		this.lastElement = lastElement;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Double getFirstElement() {
		return firstElement;
	}
	public void setFirstElement(Double firstElement) {
		this.firstElement = firstElement;
	}
	public Double getLastElement() {
		return lastElement;
	}
	public void setLastElement(Double lastElement) {
		this.lastElement = lastElement;
	}
	
	public boolean contains(IntervalAnalysis ia){
		boolean result = false;
		
		if(ia.getName() == this.getName()){
			if(ia.getFirstElement() >= this.getFirstElement()){
				if(ia.getLastElement() <= this.getLastElement()){
					result = true;
				}
			}
		}
		return result;
	}
	
	
}
