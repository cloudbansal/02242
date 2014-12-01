package dk.dtu.imm.pa.analyzer.objects;

public class ArrayUse{
	CodeLine label;
	Variable varIndex;
	int		 intIndex;
	
	public ArrayUse(CodeLine label) {
		super();
		this.label = label;
	}
	
	public boolean isVarIndex(){
		return (varIndex != null);
	}
	public CodeLine getLabel() {
		return label;
	}
	public void setLabel(CodeLine label) {
		this.label = label;
	}
	public Variable getVarIndex() {
		return varIndex;
	}
	public void setVarIndex(Variable varIndex) {
		this.varIndex = varIndex;
	}
	public int getIntIndex() {
		return intIndex;
	}
	public void setIntIndex(int intIndex) {
		this.intIndex = intIndex;
	}
	
	public String toString(){
		return "(" + this.label.getLineNumber() + ", " + (this.isVarIndex()? this.varIndex.getName():this.intIndex) +")";
	}
}