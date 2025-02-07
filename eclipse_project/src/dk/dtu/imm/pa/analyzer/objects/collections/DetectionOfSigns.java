package dk.dtu.imm.pa.analyzer.objects.collections;

public class DetectionOfSigns {
	private String name;
	private boolean plus;
	private boolean minus;
	private boolean zero;	

	public DetectionOfSigns(String name, boolean plus, boolean minus, boolean zero) {
		super();
		this.name = name;
		this.plus = plus;
		this.minus = minus;
		this.zero = zero;
	}
	
	public DetectionOfSigns(DetectionOfSigns ds){
		super();
		this.name = ds.getName();
		this.plus = ds.isPlus();
		this.minus = ds.isMinus();
		this.zero = ds.isZero();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isPlus() {
		return plus;
	}

	public void setPlus(boolean plus) {
		this.plus = plus;
	}

	public boolean isMinus() {
		return minus;
	}

	public void setMinus(boolean minus) {
		this.minus = minus;
	}

	public boolean isZero() {
		return zero;
	}

	public void setZero(boolean zero) {
		this.zero = zero;
	}
	
	public void add(DetectionOfSigns detectionOfSigns){
		this.plus  = this.plus  || detectionOfSigns.isPlus();
		this.minus = this.minus || detectionOfSigns.isMinus();
		this.zero  = this.zero  || detectionOfSigns.isZero();
	}
	
	public void substract(DetectionOfSigns detectionOfSigns){
		this.plus  = this.plus  && !detectionOfSigns.isPlus();
		this.minus = this.minus && !detectionOfSigns.isMinus();
		this.zero  = this.zero  && !detectionOfSigns.isZero();
	}
	
	public String toString(){
		return "(" + this.name + ", +:" + this.plus + ", -:" + this.minus + ", 0:" + this.zero +")";
	}

}
