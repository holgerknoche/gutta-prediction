# Contents of this Archive
This archive contains the proof-of-concept implementation of our approach for fast what-if analyses to support migration to microservices.
In this document, we describe the prerequisites for using the implementation as well as instructions to replicate the performance benchmarks from our paper, and to use the prototypical user interface.

# Prerequisites
To use this proof-of-concept implementation, Java 21 or higher must be installed.
For the following instructions, we assume that the archive has been extracted to a directory.

# Running the Benchmarks
The archive contains a shell script named `run-benchmarks.sh` to run all benchmarks conveniently with a given number of warmup and timed iterations.
For our paper, we ran the benchmarks with one warmup iteration and 100 timed iterations, which can be achieved by running `run-benchmarks.sh 1 100`.
The benchmarks will print the duration of each timed iteration in milliseconds, and compute the average duration and the standard deviation after the last timed iteration.

# Interactive Use
The user interface is opened automatically when running the JAR `gutta-prediction-1.0-SNAPSHOT.jar`, e.g., by clicking on it or running it from the command line with `java -jar`.
Instructions for running different scenarios from the paper are given below.

## Overview of the User Interface

### Loading Traces and a Deployment Model
The first step in all examples is to load the trace data and the corresponding deployment model.
This can be achieved by performing the following steps in the main window:

1. Select *Load Traces...* from the *Traces* menu to load the trace data from a given file.
2. Select *Load Deployment Model...* from the *Traces* menu to load the deployment model from a given file.

Afterwards, the available use cases as well as the deployment model will be shown in the main window.

### Exploring Traces
Double-clicking on a use case in the main window opens a new window with a list of the individual traces for this use case.
A single click on one of the traces shows a simple visualization of the trace.
No simulation is performed for this view; therefore, no overlays are shown.
Double-clicking on the trace opens the *trace analysis window* for the selected trace.

### Analysis of a Single Trace
Individual traces are analyzed in the *trace analysis window*.
This window consists of three major parts:
In the top left corner, the previously loaded deployment model is shown, the (possibly empty) scenario model in the top right corner.
Only the scenario model can be edited.
Analysis results are shown in the tabs in the lower half of the window.
The two buttons in the toolbar at the bottom allow to perform an analysis or to reset the scenario to its initial state.
The following results are shown in the tabs:

- The *Trace View* tab shows the visualization of the trace, including overlays. The visualization can be saved to an SVG file from the context menu.
- The *Events* tab lists all events in the trace
- The *Remote Calls* tab lists all remote calls in the trace
- The *Consistency Issues* tab shows all consistency issues in the trace and denotes whether they were introduced or removed by the scenario
- The *Entity Writes* tab shows all entity writes with their outcome (committed or reverted), and whether this outcome has changed

### Analysis per Use Case
Analyses on the level of the use cases can be performed from the main window.
The *Analysis* menu gives access to the following analyses:

- Overhead change analysis
- Consistency change analysis

Both analyses are performed in a similar way to the analysis of a single trace.
The only exception is that the overhead change analysis allows to provide a significance level for the significance test.
The results of the analysis are shown as a table for each use case.
Double-clicking on a use case opens an overview of all associated traces with their individual results, and double-clicking on a trace opens the trace analysis window for the selected trace.

# Examples

## The Car Insurance Example
Traces and the deployment model for the car insurance example are located in the files `paper-example.dat` and `paper-example.deploymentmodel` in the `data` directory.

Suggested analyses for this data are to simulate the decomposition on the two traces of the contract creation use case.
For this purpose, open the *trace analysis window* for the first use case, and paste the contents from the file `paper-example.scenariomodel` into the appropriate text area.
Then, press the *Analyze Scenario* button.
A variant of this scenario is to change the transaction propagation as follows, which produces the trace visualization from our paper:

```
remote "Car Insurance" -> "Common Contracts" [
  overhead = 10
  transactionPropagation = SUBORDINATE
]
```

The second trace includes the abort of a transaction, which leads to a difference in committed and reverted writes due to the scenario.

## The Benchmark Examples
Traces and the deployment models for the benchmarks are located in the files `batch-traces.dat` and `batch-traces.deploymentmodel` as well as `random-traces.dat` and `random-traces.deploymentmodel` in the `data` directory.

A suggested analysis is to run a overhead change analysis on the random traces by pasting the contents from the file `random-traces.scenariomodel`.
This results in a significant change to the overhead in use cases one and three, but not to use case two.