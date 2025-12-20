#!/bin/bash

# Hard-coded script to test the control flow graph generation for the library-application project

set -e

mvn -q -DskipTests=true package
mvn exec:java -Dexec.mainClass="com.kuleuven.coverage.ControlFlowGraphCoverage" -Dexec.args="./target/classes/ \"<com.kuleuven._examples.Foo: int foo(int)>\""

echo "✅ Done!"