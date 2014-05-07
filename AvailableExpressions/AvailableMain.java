package AvailableExpressions;

import java.util.Iterator;
import java.util.Map;

import soot.Body;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootMethod;
import soot.Transform;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Targets;
import soot.options.Options;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.scalar.FlowSet;
import soot.util.cfgcmd.CFGToDotGraph;
import soot.util.dot.DotGraph;
import CombinedAnalysis.CombinedUnitGraph;

public class AvailableMain {
	private static int count;
	private static int threadCount;
	public static CombinedUnitGraph combinedCFG;
	
	protected static void performAvailableExpressionsAnalysis(SootMethod m) {
		Body b = m.getActiveBody();
		BriefUnitGraph g = new BriefUnitGraph(b);
		AvailableExpressions a;
		
		CFGToDotGraph cfgtdg = new CFGToDotGraph();
		DotGraph dg = cfgtdg.drawCFG(g, b);
		dg.plot("old-cfg-" + m.getName() + count + DotGraph.DOT_EXTENSION);
		count++;
			    
	    if (m.getName().equals("run") && count == threadCount-1 ) {
	    	combinedCFG = new CombinedUnitGraph(b);
	    	combinedCFG.addLockUnlockUnitsForThread1();
	    }
	    else if (count == threadCount) {
	    	combinedCFG.addGraph(g);
	    	combinedCFG.addLockUnlockUnitsForThread2();
	    	combinedCFG.addUnlockToLockEdges();
	    	
	    	CFGToDotGraph combinedCfgtdg = new CFGToDotGraph();
			DotGraph combinedDg = combinedCfgtdg.drawCFG(combinedCFG, null);
			dg.plot("combined-cfg" + DotGraph.DOT_EXTENSION);
			
			/* 
			   command to get the .png for the dot file is
			   dot -Tpng filename.dot -o outfile.png
			*/
			
			a = new AvailableExpressions(combinedCFG);
			b = combinedCFG.getBody();
			
			Iterator sIt = b.getUnits().iterator();
		    while( sIt.hasNext() ) {
		        Stmt s = (Stmt) sIt.next();
		        FlowSet availableExpressionsIn = (FlowSet) a.getFlowBefore(s);
		        FlowSet availableExpressionsOut = (FlowSet) a.getFlowAfter(s);
		        System.out.println("Statement " + s);
		        System.out.println("IN " + availableExpressionsIn);
		        System.out.println("OUT" + availableExpressionsOut);
		        System.out.println("*************************************************");
		    }
	    }
    }
	
	private static void printCallGraph(CallGraph cg, SootMethod m) {
		Iterator targets = new Targets(cg.edgesOutOf(m));
	    while (targets.hasNext()) {
	        SootMethod trgt = (SootMethod)targets.next();
	        //System.out.println(m.getName() + " -> " + trgt.getName() + ";");
	        if (trgt.getName().equals("run") && count < threadCount) performAvailableExpressionsAnalysis(trgt);
	    }
	}

	public static void main(String[] args) {
		count = 0;
		threadCount = 2;
		
	    if(args.length == 0) System.exit(-1);
	    PackManager.v().getPack("wjtp").
	    add(new Transform("wjtp.liveVariables", new SceneTransformer() {
			
			@Override
			protected void internalTransform (String phaseName, Map options) {
				CHATransformer.v().transform();
				System.out.println("Entering CFG transformer");
			    System.out.println("phaseName = " + phaseName);
			    System.out.println("options = " + options);
				CallGraph cg = Scene.v().getCallGraph();
			    SootMethod m = Scene.v().getMainClass().getMethodByName("main");
			    printCallGraph(cg, m);
			    performAvailableExpressionsAnalysis(m);
			    System.out.println("Exiting CFG transformer");
		  	}
		}));
	    
	    Options.v().set_main_class("AvailableExpressionsInput");
	    Options.v().set_whole_program(true);
	    soot.Main.main(args);
  }
}
