package dk.dtu.imm.pa.analyzer;

import java.util.ArrayList;
import java.util.Iterator;

public class EdgeSet extends ArrayList<Edge> {
	/**
	 * Default value for serialization
	 */
	private static final long serialVersionUID = 1L;

	public String toString(){
		StringBuilder sb = new StringBuilder();
   
		sb.append("{");
	    for (Iterator<Edge> it = this.iterator(); it.hasNext();){
	    	sb.append(it.next().toString());
	    	if(it.hasNext()){
	    		sb.append(", ");
	    	}
	    }
	    
		sb.append("}");
		return sb.toString();
	}
	
	public Edge findByLineNumbers(int sourceLineNumber, int destinationLineNumber){
		for(Edge e : this){
			if(
					e.getSource().getLineNumber() == sourceLineNumber
					&&
					e.getDestination().getLineNumber() == destinationLineNumber
					){
				return e;
			}
		}
		return null;
	}
}
