package dk.dtu.imm.pa.analyzer;

import java.util.ArrayList;
import java.util.Iterator;

import dk.dtu.imm.pa.analyzer.objects.CodeLine;

public class LabelSet extends ArrayList<CodeLine> {
	/**
	 * Default value for serialization
	 */
	private static final long serialVersionUID = 1L;
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
   
		sb.append("{");

	    for (Iterator<CodeLine> it = this.iterator(); it.hasNext();){
	    	sb.append(it.next().getLineNumber());
	    	if(it.hasNext()){
	    		sb.append(", ");
	    	}
	    }
	    
		sb.append("}");
		return sb.toString();
	}
}
