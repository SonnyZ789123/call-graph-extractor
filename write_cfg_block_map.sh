#!/bin/bash

set -e

mvn -q -DskipTests=true package
mvn exec:java -Dexec.mainClass="com.kuleuven.coverage.ControlFlowGraphCoverage" -Dexec.args="../../jdart-examples/out/production/jdart-examples \"<test.ControlFlowGraph.Test: int foo(int)>\""

echo "✅ Done!"