package dk.dtu.imm.pa.analyzer.objects.collections;

public class ReachingDefinition {
	private String name;
	private int line;
	
	public ReachingDefinition(String name, int line){
		this.name = name;
		this.line = line;
	}
	
	public boolean equals(Object o){
		return  (o instanceof ReachingDefinition)
				&&
				( (ReachingDefinition) o ).getName().equals(this.getName())
				&&
				(( (ReachingDefinition) o ).getLine() == this.getLine());
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}
}
