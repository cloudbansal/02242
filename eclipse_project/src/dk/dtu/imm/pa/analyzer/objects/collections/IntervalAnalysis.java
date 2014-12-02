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
	
	public IntervalAnalysis(IntervalAnalysis ias) {
		super();
		this.name = ias.getName();
		this.firstElement = ias.getFirstElement();
		this.lastElement = ias.getLastElement();
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
			if(Double.isNaN(ia.getFirstElement()) || Double.isNaN(ia.getLastElement()))
			{
				result = true;
			}
			else if(ia.getFirstElement() >= this.getFirstElement()){
				if(ia.getLastElement() <= this.getLastElement()){
					result = true;
				}
			}
		}
		return result;
	}
	
	public void add(IntervalAnalysis ia){
		if(ia.getName().equals(this.name)){
			if(Double.isNaN(this.firstElement) || Double.isNaN(this.lastElement)){
				this.firstElement = ia.getFirstElement();
				this.lastElement  = ia.getLastElement();
			} else if ( !Double.isNaN(ia.firstElement) &&  !Double.isNaN(ia.lastElement)){
				this.firstElement  = Math.min(this.firstElement, ia.getFirstElement());
				this.lastElement   = Math.max(this.lastElement, ia.getLastElement());
			}
		}
	}
	
	public void substract(IntervalAnalysis ia){
		if(ia.getName().equals(this.name)){
			if( Double.isNaN(this.firstElement) || Double.isNaN( this.lastElement)){
				return;
			} else if ( !Double.isNaN(ia.firstElement) &&  !Double.isNaN(ia.lastElement)){
				if(ia.firstElement <= this.firstElement && ia.lastElement >= this.lastElement){
					this.firstElement = Double.NaN;
					this.lastElement  = Double.NaN;
				} else if(ia.firstElement <= this.firstElement && ia.lastElement < this.lastElement){
					if(ia.lastElement >= this.firstElement){
						this.firstElement = Double.NEGATIVE_INFINITY;
						this.lastElement  = ia.firstElement - 1;
					} 
				} else if(ia.firstElement > this.firstElement && ia.lastElement > this.lastElement){
					if(ia.firstElement < this.lastElement){
						this.firstElement = ia.lastElement + 1;
						this.lastElement  = Double.POSITIVE_INFINITY;
					}
				} else {
					this.firstElement  = Math.max(this.firstElement, ia.getFirstElement());
					this.lastElement   = Math.min(this.lastElement, ia.getLastElement());
				}
			}
		}
	}
	
	public String toString(){
		return("("+this.name+", ["+this.firstElement+","+this.lastElement+"])");
	}
	
}

