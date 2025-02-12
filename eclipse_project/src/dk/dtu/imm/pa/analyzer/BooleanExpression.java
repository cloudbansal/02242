package dk.dtu.imm.pa.analyzer;

import java.util.ArrayList;

import org.antlr.runtime.tree.Tree;

import dk.dtu.imm.pa.analyzer.parser.TheLangLexer;

public class BooleanExpression {
	private String firstVariable;
	private boolean firstVariableNegated;
	private boolean firstVariableIsNumber;
	
	private String secondVariable;
	private boolean secondVariableNegated;
	private boolean secondVariableIsNumber;
	
	private int operator;
		
	public BooleanExpression(ArrayList<Tree> expressionWithoutParentheses){
		int size = expressionWithoutParentheses.size();
		
		switch(size){
			case 3:{
				this.firstVariableNegated  = false;
				this.secondVariableNegated = false;
				
				this.firstVariable = expressionWithoutParentheses.get(0).toString();
				this.secondVariable = expressionWithoutParentheses.get(2).toString();
				
				this.operator = expressionWithoutParentheses.get(1).getType();
				break;
			}
			case 4:{
				if(expressionWithoutParentheses.get(0).getType() == TheLangLexer.MINUS) {
					// first one is negated
					this.firstVariableNegated  = true;
					this.secondVariableNegated = false;
					
					this.firstVariable = expressionWithoutParentheses.get(1).toString();
					this.secondVariable = expressionWithoutParentheses.get(3).toString();
					
					this.operator = expressionWithoutParentheses.get(2).getType();
				} else {
					// second one is negated
					this.firstVariableNegated  = false;
					this.secondVariableNegated = true;
					
					this.firstVariable = expressionWithoutParentheses.get(0).toString();
					this.secondVariable = expressionWithoutParentheses.get(3).toString();
					
					this.operator = expressionWithoutParentheses.get(1).getType();
				}
				break;
			}
			case 5:{
				// both are negated
				this.firstVariableNegated  = true;
				this.secondVariableNegated = true;
				
				this.firstVariable = expressionWithoutParentheses.get(1).toString();
				this.secondVariable = expressionWithoutParentheses.get(4).toString();
				
				this.operator = expressionWithoutParentheses.get(2).getType();
				break;
			}
		}

		try {
			Integer.parseInt(this.firstVariable);
			this.firstVariableIsNumber = true;
		} catch (NumberFormatException e) {
			this.firstVariableIsNumber = false;
		}
		
		try {
			Integer.parseInt(this.secondVariable);
			this.secondVariableIsNumber = true;
		} catch (NumberFormatException e) {
			this.secondVariableIsNumber = false;
		}
	}

	public String getFirstVariable() {
		return firstVariable;
	}

	public void setFirstVariable(String firstVariable) {
		this.firstVariable = firstVariable;
	}

	public String getSecondVariable() {
		return secondVariable;
	}

	public void setSecondVariable(String secondVariable) {
		this.secondVariable = secondVariable;
	}

	public int getOperator() {
		return operator;
	}

	public void setOperator(int operator) {
		this.operator = operator;
	}

	public boolean isSecondVariableNegated() {
		return secondVariableNegated;
	}

	public void setSecondVariableNegated(boolean secondVariableNegated) {
		this.secondVariableNegated = secondVariableNegated;
	}

	public boolean isFirstVariableNegated() {
		return firstVariableNegated;
	}

	public void setFirstVariableNegated(boolean firstVariableNegated) {
		this.firstVariableNegated = firstVariableNegated;
	}

	public boolean isFirstVariableIsNumber() {
		return firstVariableIsNumber;
	}

	public void setFirstVariableIsNumber(boolean firstVariableIsNumber) {
		this.firstVariableIsNumber = firstVariableIsNumber;
	}

	public boolean isSecondVariableIsNumber() {
		return secondVariableIsNumber;
	}

	public void setSecondVariableIsNumber(boolean secondVariableIsNumber) {
		this.secondVariableIsNumber = secondVariableIsNumber;
	}
	
	public String toString(){
		String op = "";
		switch(this.operator){
			case(TheLangLexer.EQ):{
				op = "=";
				break;
			}
			case(TheLangLexer.NEQ):{
				op = "!=";
				break;
			}
			case(TheLangLexer.GE):{
				op = ">=";
				break;
			}
			case(TheLangLexer.GT):{
				op = ">";
				break;
			}
			case(TheLangLexer.LT):{
				op = "<";
				break;
			}
			case(TheLangLexer.LE):{
				op = "<=";
				break;
			}
		}
		return "(" + (this.isFirstVariableNegated() ? "-" : "") + this.firstVariable + op +
				(this.isSecondVariableNegated() ? "-" : "") + this.secondVariable + ")";
	}
}
