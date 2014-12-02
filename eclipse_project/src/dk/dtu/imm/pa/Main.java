package dk.dtu.imm.pa;

import java.io.IOException;

import dk.dtu.imm.pa.analyzer.FlowGraph;
import dk.dtu.imm.pa.analyzer.objects.CodeLine;
import dk.dtu.imm.pa.analyzer.objects.Program;

/**
 * ProgramAnalyzer
 * - receives the file path of the source code to analyze, and performs the given analysis techniques
 *   on it.
 */


import java.awt.EventQueue;
import java.awt.GridLayout;

import javax.swing.*;

class Main {

    private static void display() {
        String[] items = {"Interval Analysis", "Detection of Signs"};
        JComboBox combo = new JComboBox(items);
        JTextField field1 = new JTextField("./samples/benchmark.lang");
        JPanel panel = new JPanel(new GridLayout(0, 1));
        
        panel.add(new JLabel("Type of buffer overflow check:"));
        
        panel.add(combo);
        
        panel.add(new JLabel("File to analyze:"));
        panel.add(field1);

        
        int result = JOptionPane.showConfirmDialog(null, panel, "Program Analysis",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
        	String analysis="";
        	if(combo.getSelectedIndex() == 1){
        		analysis = "DS";
        	}else{
        		analysis = "IA";
        	}
        	
        	try {
				runAnalysis(field1.getText(), analysis);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        } else {
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                display();
            }
        });
    }
    
    public static void runAnalysis(String srcFile, String analysis) throws Exception {    	
    	// Create program structure of lines of code (CodeLines)
    	Program program;
    	try {
    		program = new Program(srcFile);
    		
    	    // Print program
    	    System.out.println();
    	    System.out.println(program.toString());
    	    System.out.println();
    	    
    	    FlowGraph fg = new FlowGraph(program);
    	    
    	    System.out.println("Init:");
    	    System.out.println(fg.getInitLabels());
    	    System.out.println();
    	    System.out.println("Labels:");
    	    System.out.println(fg.getProgramLabels());
    	    System.out.println();
    	    System.out.println("Program flow:");
    	    System.out.println(fg.getProgramFlow());
    	    System.out.println();
    	    
    	    
    	    fg.calculateBufferOverflow(analysis);

    	} catch (IOException e) {
    		System.err.println("Error trying to parse source file (" + srcFile + "):");
    		System.err.println(e.getMessage());
    	}
      }

}
