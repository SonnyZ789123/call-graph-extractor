#!/bin/bash

# Hard-coded script to test the control flow graph generation for the library-application project

set -e

mvn -q -DskipTests=true package
mvn exec:java -Dexec.mainClass="com.kuleuven.ControlFlowGraph.MainControlFlowGraphGenerator" -Dexec.args="/Users/yoran/dev/library-application/target/classes \"<com.kuleuven.library.domain.Book: double getReducedPrice()>\""

echo "✅ Done!"