package com.kuleuven.ICFG;

import com.kuleuven.CallGraph.ICallGraph;
import com.kuleuven.CallGraph.SootUpCallGraphWrapper;
import com.kuleuven.metrics.CallGraphConstructionAlgorithm;
import sootup.callgraph.CallGraph;
import sootup.callgraph.CallGraphAlgorithm;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.callgraph.RapidTypeAnalysisAlgorithm;
import sootup.core.graph.StmtGraph;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.views.JavaView;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public class ExtractICFG {
    public ICFG extract(String classPath, String fullyQualifiedMethodSignature, CallGraphConstructionAlgorithm algorithmChoice) {
        AnalysisInputLocation inputLocation = new JavaClassPathAnalysisInputLocation(classPath);
        JavaView view = new JavaView(inputLocation);

        MethodSignature entryMethodSignature = view.getIdentifierFactory().parseMethodSignature(fullyQualifiedMethodSignature);

        // Select call graph analysis algorithm
        CallGraphAlgorithm cgAlgorithm = switch (algorithmChoice) {
            case CHA -> new ClassHierarchyAnalysisAlgorithm(view);
            case RTA -> new RapidTypeAnalysisAlgorithm(view);
        };

        CallGraph cg = cgAlgorithm.initialize(Collections.singletonList(entryMethodSignature));

        SootUpCallGraphWrapper cgWrapper = new SootUpCallGraphWrapper(cg);

        Map<JavaSootMethod, StmtGraph<?>> methodToCFGMap = new HashMap<>();
        cgWrapper.getNodes().forEach(methodSignature -> {
            Optional<JavaSootMethod> opt = view.getMethod(methodSignature);
            if (opt.isEmpty()) {
                System.err.println("❌ Method not found: " + fullyQualifiedMethodSignature);
                return;
            }

            JavaSootMethod method = opt.get();
            StmtGraph<?> cfg = method.getBody().getStmtGraph();
            methodToCFGMap.put(method, cfg);
        });

        return new ICFG(cgWrapper, methodToCFGMap);
    }
}
