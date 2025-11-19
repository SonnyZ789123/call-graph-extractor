package com.kuleuven.CallGraph;

import sootup.callgraph.CallGraph;
import sootup.core.signatures.MethodSignature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ProjectMethodFilter {
    private List<String> projectPrefixes;
    private final List<String> blacklistPrefixes = List.of(
            "java.",
            "javax.",
            "sun.",
            "com.sun.",
            "jdk.",
            "org.w3c.",
            "org.xml.",
            "org.omg.",
            "org.junit.",
            "org.testng.",
            "org.hamcrest.",
            "org.mockito.",
            "org.apache.",
            "com.google.",
            "com.fasterxml.",
            "org.slf4j.",
            "ch.qos.logback."
    );

    public ProjectMethodFilter() {
        this.projectPrefixes = Collections.emptyList();
    }

    public ProjectMethodFilter(List<String> projectPrefixes) {
        this.projectPrefixes = new ArrayList<>(projectPrefixes);
    }

    public Set<MethodSignature> filterMethods(Set<MethodSignature> methodSignatures) {
        return methodSignatures.stream()
                .filter(this::isProjectMethod)
                .collect(Collectors.toSet());
    }

    public Set<CallGraph.Call> filterCalls(Set<CallGraph.Call> calls) {
        return calls.stream()
                .filter(call -> isProjectMethod(call.sourceMethodSignature()) && isProjectMethod(call.targetMethodSignature()))
                .collect(Collectors.toSet());
    }

    private boolean isProjectMethod(MethodSignature sig) {
        String className = sig.getDeclClassType().getFullyQualifiedName();

        if (!projectPrefixes.isEmpty()) {
            for (String prefix : projectPrefixes) {
                if (className.startsWith(prefix)) {
                    return true;
                }
            }
        } else {
            for (String prefix : blacklistPrefixes) {
                if (className.startsWith(prefix)) {
                    return false;
                }
            }
        }

        return true;
    }
}
