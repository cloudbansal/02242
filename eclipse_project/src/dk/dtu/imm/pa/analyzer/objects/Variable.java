package dk.dtu.imm.pa.analyzer.objects;

public class Variable {
	private final String name;
	private final int line;
	
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
	
	/**
	 * Return variable name when printed
	 */
	public String toString(){
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
}
