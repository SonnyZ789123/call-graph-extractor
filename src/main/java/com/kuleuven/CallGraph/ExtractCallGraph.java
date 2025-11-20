package com.kuleuven.CallGraph;

import com.kuleuven.metrics.CallGraphConstructionAlgorithm;
import sootup.callgraph.CallGraph;
import sootup.callgraph.CallGraphAlgorithm;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.callgraph.RapidTypeAnalysisAlgorithm;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class ExtractCallGraph {
    public CallGraph extract(String classPath, String fullyQualifiedMethodSignature, CallGraphConstructionAlgorithm algorithmChoice) {
        // Load classes from the given classpath
        AnalysisInputLocation inputLocation = new JavaClassPathAnalysisInputLocation(classPath);
        JavaView view = new JavaView(inputLocation);

        /*
         * Create the exact method signature in SootUp form.
         * If this does not match exactly, SootUp cannot find the method.
         * The fully qualified method signature is expected to be in the format:
         * <packageName.classType: void main(java.lang.String[])>
         */
        MethodSignature methodSignature = view.getIdentifierFactory().parseMethodSignature(fullyQualifiedMethodSignature);

        // Select call graph analysis algorithm
        CallGraphAlgorithm cgAlgorithm = switch (algorithmChoice) {
            case CHA -> new ClassHierarchyAnalysisAlgorithm(view); // Fast, over-approximates
            case RTA -> new RapidTypeAnalysisAlgorithm(view);      // More precise, slower
        };

        return cgAlgorithm.initialize(Collections.singletonList(methodSignature));
    }

    public CallGraph extractForTestClasses(String classPath, String pathToTestClasses, CallGraphConstructionAlgorithm algorithmChoice) {
        AnalysisInputLocation inputLocationTestClasses = new JavaClassPathAnalysisInputLocation(pathToTestClasses);
        AnalysisInputLocation inputLocationSource = new JavaClassPathAnalysisInputLocation(classPath);

        JavaView testClassesView = new JavaView(inputLocationTestClasses);

        Stream<JavaSootClass> testClasses = testClassesView.getClasses();
        List<MethodSignature> allTestCases = new ArrayList<>();
        testClasses.forEach(c -> {
            c.getMethods().forEach(m -> {
                allTestCases.add(m.getSignature());
            });
        });

        JavaView view = new JavaView(List.of(inputLocationTestClasses, inputLocationSource));

        // Select call graph analysis algorithm
        CallGraphAlgorithm cgAlgorithm = switch (algorithmChoice) {
            case CHA -> new ClassHierarchyAnalysisAlgorithm(view); // Fast, over-approximates
            case RTA -> new RapidTypeAnalysisAlgorithm(view);      // More precise, slower
        };

        return cgAlgorithm.initialize(allTestCases);
    }
}
