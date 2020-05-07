# SingularityBenchmarks
This repo contains the source codes of all benchmarks used in Singularity evaluation (work in progress)

## Build
To build all cost-instrumented jar files, run
```
ant instr
```
The original version of those jars will be put under `out/artifact/original`, and the instrumented version of those jars will be put under `out/artifact/instrumented`. Instrumentation is performed using [this tool](https://github.com/grievejia/CostInstrument).
