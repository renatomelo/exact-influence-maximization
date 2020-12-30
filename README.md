# Exact Influence Maximization

This project contains a set of Integer Linear Programing models implemented in Java using Gurobi as mathematical solver.

Libraries:
- Gurobi Optimization
- JGraphT Library (a Java library of graph theory data structures and algorithms)
- JUNG (Java Universal Network/Graph Framework)

List of Optimization Problems:
- Minimum Target Set Selection (TSS)
- Weighted Target Set Selection (WTSS)
- Maximum Active Set (MAS)

Random Graph Generative Models:
- G(n,p): Erdős–Rényi model
- Directed Scale-free Graphs model
- Kleiberg Small World model
- Barabási–Albert model

The method `preprocessing(...)` in `src/models/MinTargetSet.java` class is the implementation of the preprocessing rules proposed in my following paper:
- [Preprocessing Rules for Target Set Selection in Complex Networks](https://sol.sbc.org.br/index.php/brasnam/article/view/11167)
