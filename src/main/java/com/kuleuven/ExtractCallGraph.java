package com.kuleuven;

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
    public CallGraph extract(String classPath, String mainClassName, String entryMethodSignature, CallGraphConstructionAlgorithm algorithmChoice) {
        /*
         * Parse "void main(java.lang.String[])" into:
         *   returnType = "void"
         *   methodName = "main"
         *   parameters = ["java.lang.String[]"]
         */
        String returnType = entryMethodSignature.substring(0, entryMethodSignature.indexOf(' ')).trim();
        String afterReturn = entryMethodSignature.substring(entryMethodSignature.indexOf(' ') + 1).trim();
        String methodName = afterReturn.substring(0, afterReturn.indexOf('(')).trim();
        String paramList = afterReturn.substring(afterReturn.indexOf('(') + 1, afterReturn.indexOf(')')).trim();

        List<String> parameters;
        if (paramList.isEmpty()) {
            parameters = Collections.emptyList();
        } else {
            parameters = Arrays.stream(paramList.split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());
        }

        // Load classes from the given classpath
        AnalysisInputLocation inputLocation = new JavaClassPathAnalysisInputLocation(classPath);
        JavaView view = new JavaView(inputLocation);

        // Resolve the class we start from
        JavaClassType classType = view.getIdentifierFactory().getClassType(mainClassName);
        Optional<JavaSootClass> maybeClass = view.getClass(classType);

        if (maybeClass.isEmpty()) {
            System.err.println("❌ Could not load class " + mainClassName);
            System.exit(1);
        }

        JavaSootClass sootClass = maybeClass.get();

        /*
         * Create the exact method signature in SootUp form.
         * If this does not match exactly, SootUp cannot find the method.
         */
        MethodSignature methodSignature = view.getIdentifierFactory().getMethodSignature(
                classType,
                methodName,
                returnType,
                parameters
        );

        // Check if the method actually exists in the class
        MethodSubSignature mss = methodSignature.getSubSignature();
        Optional<JavaSootMethod> opt = sootClass.getMethod(mss);

        if (opt.isEmpty()) {
            System.err.println("❌ Method not found: " + entryMethodSignature);
            System.exit(1);
        }

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
