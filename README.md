# GPGA
A custom implementation of a genetic algorithm simulator in Java. The program allows for custom fitness functions and generates a detailed output log of the simulation, making it an ideal learning tool.

The GUI allows for input of a custom fitness function (currently 1 dimensional only), as well as selection of population size (population is always constant between generations), elitism, mutation chance, and optional sigma scaling.

A number of stopping conditions are supported, including 'known best' and max-generations. The program utilises a thread pool with an optional number of workers.
