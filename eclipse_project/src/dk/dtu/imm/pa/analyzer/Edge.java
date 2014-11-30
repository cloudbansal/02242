package dk.dtu.imm.pa.analyzer;

import dk.dtu.imm.pa.analyzer.objects.CodeLine;

public class Edge {
	private CodeLine source;
	private CodeLine destination;
	
	public Edge(CodeLine source, CodeLine destination){
		this.source = source;
		this.destination = destination;
	}
	
	public CodeLine getSource() {
		return source;
	}

	public CodeLine getDestination() {
		return destination;
	}

	public String toString(){
		String srcLine;
		String destLine;
		if(source == null){
			srcLine = "null";
		} else {
			srcLine = "" + source.getLineNumber();
		}
		if(destination == null){
			destLine = "null";
		} else {
			destLine = "" +  destination.getLineNumber();
		}
		return "(" + srcLine + ", " + destLine + ")";
	}
	
	public boolean equals(Object o){
		return  (o instanceof Edge)
				&&
				( (Edge) o ).getSource().getLineNumber() == this.getSource().getLineNumber()
				&&
				( (Edge) o ).getDestination().getLineNumber() == this.getDestination().getLineNumber();
	}
}
