package dk.dtu.imm.pa.analyzer.objects;

import java.util.ArrayList;

public class Variable {
	private final String name;
	private final int line;
	
	private boolean isArray;
	private int     arrayLength;
	
	private ArrayList<ArrayUse> arrayUsage;
	
	public Variable(int line, String name){
		this.name = name;
		this.line = line;
	}
	
	public String getName(){
		return this.name;
	}
	
	public int getLine(){
		return this.line;
	}
	
	public int getArrayLength() {
		return arrayLength;
	}

	public void setArrayLength(int arrayLength) {
		this.arrayLength = arrayLength;
	}
	
	/**
	 * Return variable name when printed
	 */
	public String toString(){
		if(this.isArray){
			return this.getName() + "[" + this.getArrayLength() +"]";
		}
		
		return this.getName();
	}
	
	/**
	 * Check whether a variable is the same as other
	 * @return true if both have the same name
	 */
	public boolean equals(Object o){
		return  (o instanceof Variable)
				&&
				( (Variable) o ).getName().equals(this.getName());
	}

	public boolean isArray() {
		return isArray;
	}

	public void setArray(boolean isArray) {
		this.isArray = isArray;
	}

	public ArrayList<ArrayUse> getArrayUsage() {
		return arrayUsage;
	}

	public void setArrayUsage(ArrayList<ArrayUse> arrayUsage) {
		this.arrayUsage = arrayUsage;
	}
	
}
