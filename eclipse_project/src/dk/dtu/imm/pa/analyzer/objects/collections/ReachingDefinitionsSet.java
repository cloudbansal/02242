package dk.dtu.imm.pa.analyzer.objects.collections;

import java.util.ArrayList;

import dk.dtu.imm.pa.analyzer.LabelSet;
import dk.dtu.imm.pa.analyzer.objects.Variable;

public class ReachingDefinitionsSet extends ArrayList<ReachingDefinition> {

	/**
	 * Default variable for serialization
	 */
	private static final long serialVersionUID = 1L;
	
	public ReachingDefinitionsSet(ReachingDefinitionsSet rds){
		super(rds);
	}
	
	public ReachingDefinitionsSet() {
		super();
	}

	public ReachingDefinitionsSet addition(ReachingDefinitionsSet reachingDefinitionsSet){
		ReachingDefinitionsSet newReachingDefinitionsSet = new ReachingDefinitionsSet(this);
		
		for(ReachingDefinition rd: reachingDefinitionsSet){
			if(! newReachingDefinitionsSet.contains(rd)){
				newReachingDefinitionsSet.add(rd);
			}
		}
		
		return newReachingDefinitionsSet;
	}
	
	public ReachingDefinitionsSet removal(ReachingDefinitionsSet reachingDefinitionsSet){
		ReachingDefinitionsSet newReachingDefinitionsSet = new ReachingDefinitionsSet(this);
		
		newReachingDefinitionsSet.removeAll(reachingDefinitionsSet);
		
		return newReachingDefinitionsSet;
	}
	
	public String toString(){
		String result = "{";
		for(ReachingDefinition rd : this){
			result += rd.toString();
			result += ", ";
		}
		result += "}";
		return result;
	}
	
	public boolean contains(ReachingDefinitionsSet rds){
        boolean result = true;
        
        for(ReachingDefinition rd : rds){
            if(!this.contains(rd)){
                result = false;
            }
        }
        
        return result;
    }
    
	public boolean contains(ReachingDefinition rd){
	    boolean result = false;
	    
	    for(ReachingDefinition ourRd : this){
	        if(ourRd.equals(rd)){
	            result = true;
	        }
	    }
	    
	    return result;
	}
	
	public ArrayList<Integer> getAllLinesOfVariable(Variable v){
		ArrayList<Integer> result = new ArrayList<Integer>();
		
		for(ReachingDefinition rd : this){
			if(rd.getName().equals(v.getName())){
				if(rd.getLine() != -1){
					result.add(rd.getLine());
				}
			}
		}
		
		return result;
	}

}
