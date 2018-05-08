# Copyright 2017, Gurobi Optimization, Inc.

PLATFORM = linux64
JSRC     = src/
BIN		 = bin/
CLASSDIR = -classpath lib/gurobi.jar:.
JFLAG    = -d . $(CLASSDIR)

all: Callback.class Dense.class Diet.class Facility.class Feasopt.class \
     Fixanddive.class Lp.class Lpmethod.class Lpmod.class Mip1.class \
     Mip2.class Params.class Piecewise.class Qcp.class Qp.class \
     Sensitivity.class Sos.class Sudoku.class TSP.class Tune.class \
     Workforce1.class Workforce2.class Workforce3.class Workforce4.class \
     Workforce5.class Multiobj.class Poolsearch.class Genconstr.class

run: run_java

run_java: Callback Dense Diet Facility Feasopt Fixanddive Lp Lpmethod Lpmod \
          Mip1 Mip2 Params Piecewise Qcp Qp Sensitivity Sos Sudoku TSP Tune \
          Workforce1 Workforce2 Workforce3 Workforce4 Workforce5 Multiobj \
          Poolsearch Genconstr

%.class: $(JSRC)/%.java
	javac $(JFLAG) $<


Mip1: Mip1.class
	java $(CLASSDIR): Mip1
Tsp: TSP.class
	java $(CLASSDIR): TSP 50
Facility: Facility.class
	java $(CLASSDIR): Facility
MaximumActiveSet: MaximumActiveSet.class
	java $(CLASSDIR): MaximumActiveSet
MinimumTargetSet: MinimumTargetSet.class
	java $(CLASSDIR): MinimumTargetSet

clean:
	rm -rf *.class *.log *.rlp *.lp *.bas *.ilp *.mps
