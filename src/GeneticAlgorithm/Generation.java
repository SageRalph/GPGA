package GeneticAlgorithm;
import java.util.Arrays;

/**
 * @author David Ralph <David.M.L.Ralph@Gmail.com>
 */
public class Generation {

private final Chromosome[] population;

/**
 * Create a new Generation from an existing population.
 * @param population
 */
public Generation(Chromosome[] population) {
    this.population = population;
}

/**
 * Randomly generate a new Generation.
 * @param populationSize
 */
public Generation(int populationSize) {

    population = new Chromosome[populationSize];
    for (int i = 0; i < populationSize; i++) {
        population[i] = new Chromosome();
    }
}

@Override
public String toString() {
    String str = "";
    for (Chromosome ch : population) {
        str += ch.genes() + " ";
    }
    return str;
}

public Chromosome[] getPopulation() {
    return population;
}

public double totalFitness(boolean sigmaScaling) {
    double[] fitArray = getFitnessArray(sigmaScaling);

    double value = 0;
    for (double fit : fitArray) {
        value += fit;
    }
    return value;
}

public double averageFitness(boolean sigmaScaling) {
    return totalFitness(sigmaScaling) / population.length;
}

public double[] getFitnessArray(boolean sigmaScaling) {

    double[] fitArray = new double[population.length];

    // Determine fitness
    for (int i = 0; i < population.length; i++) {
        fitArray[i] = population[i].fitness();
    }

    if (sigmaScaling) {
        Statistics s = new Statistics(fitArray);
        double average = s.getMean();
        double std = s.getStdDev();

        for (int i = 0; i < population.length; i++) {
            fitArray[i] = sigmaFitness(fitArray[i], average, std);
        }
    }

    return fitArray;
}

/**
 * Applies sigma scaling to fitness value provided.
 * @param fitness
 * @param average
 * @param standardDeviation
 * @return The scaled fitness value
 */
private static double sigmaFitness(
        double fitness,
        double average,
        double standardDeviation) {

    double result = standardDeviation == 0
            ? fitness
            : 1 + ((fitness - average) / (2 * standardDeviation));
    return result;
}

public Chromosome mostFit() {
    double maxFitness = -1;
    Chromosome best = null;
    for (Chromosome ch : population) {
        double fitness = ch.fitness();
        if (fitness > maxFitness) {
            maxFitness = fitness;
            best = ch;
        }
    }
    return best;
}

public Chromosome[] fittest() {
    Chromosome[] pop = population.clone();
    Arrays.sort(pop, (Chromosome c1, Chromosome c2)
            -> c1.fitness() > c2.fitness() ? -1 : 1);
    return pop;
}

public int popSize(){
    return population.length;
}

}
