package dk.dtu.imm.pa.analyzer.objects;

import java.util.Collection;
import java.util.LinkedList;

/**
 * A store where to save the variables of the program, ensuring that there are no duplicates
 *
 */
public class VariableStore extends LinkedList<Variable> {
    /**
	 * Default value for serialization
	 */
	private static final long serialVersionUID = 1L;

	@Override
    public boolean add(Variable e) {
        if (this.contains(e)) {
        	this.findByName(e.getName()).setArray(e.isArray());
        	this.findByName(e.getName()).setArrayLength(e.getArrayLength());

            return false;
        }
        else {
            return super.add(e);
        }
    }

    @Override
    public boolean addAll(Collection<? extends Variable> collection) {
        Collection<Variable> copy = new LinkedList<Variable>(collection);
        copy.removeAll(this);
        return super.addAll(copy);
    }

    @Override
    public boolean addAll(int index, Collection<? extends Variable> collection) {
        Collection<Variable> copy = new LinkedList<Variable>(collection);
        copy.removeAll(this);
        return super.addAll(index, copy);
    }

    @Override
    public void add(int index, Variable element) {
        if (this.contains(element)) {
            return;
        }
        else {
            super.add(index, element);
        }
    }
    
    /**
     * Find a variable in the store given its name
     * @param name: the name of the variable to find
     * @return the matched variable in the store, or null
     */
    public Variable findByName(String name){
    	for(Variable v: this){
    		if(v.getName().equals(name)){
    			return v;
    		}
    	}
    	return null;
    }
} 