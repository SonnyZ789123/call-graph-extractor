#!/bin/bash

# Hard-coded script to test the call graph generation with coverage for the library-application project

set -e

mvn -q -DskipTests=true package
mvn exec:java -Dexec.mainClass="com.kuleuven.CallGraph.CallGraphWithCoverageGenerator" -Dexec.args="/Users/yoran/dev/library-application/target/classes /Users/yoran/dev/library-application/target/test-classes \"<com.kuleuven.library.Main: void main(java.lang.String[])>\" cha com.kuleuven"

echo "✅ Done!"
