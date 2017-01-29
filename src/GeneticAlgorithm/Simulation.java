package GeneticAlgorithm;
import java.util.ArrayList;
import java.util.Random;

/**
 * @author David Ralph <David.M.L.Ralph@Gmail.com>
 */
public class Simulation {

private final int rangeMin, rangeMax, populationSize, maxGenerations, elitism;
private final double mutationChance;
private final boolean sigmaScaling, genDetail, genSummary, runSummary;
private final String simNo;
private String out = "";

public Simulation(
        String simNo,
        int rangeMin,
        int rangeMax,
        int populationSize,
        int maxGenerations,
        int elitism,
        double mutationChance,
        boolean sigmaScaling,
        boolean genDetail,
        boolean genSummary,
        boolean runSummary) {
    this.simNo = simNo;
    this.rangeMin = rangeMin;
    this.rangeMax = rangeMax;
    this.populationSize = populationSize;
    this.maxGenerations = maxGenerations;
    this.elitism = elitism;
    this.mutationChance = mutationChance;
    this.sigmaScaling = sigmaScaling;
    this.genDetail = genDetail;
    this.genSummary = genSummary;
    this.runSummary = runSummary;
}

public int run() {

    Chromosome.min = rangeMin;
    Chromosome.max = rangeMax;

    // Create random population for generation 0
    Generation population = new Generation(populationSize);

    if (genSummary) {
        out += ("\nRun " + simNo
                + " Generation 0 (initial population)"
                + describePopulation(population) + "\n");
    }

    int bestGeneration = 0;
    double bestRunningFitness = population.mostFit().fitness();

    // Simulate
    for (int i = 1; i <= maxGenerations; i++) { // TODO more stopping conditions

        population = evolve(population);

        if (genSummary) {
            out += ("\n\nRun " + simNo + " Generation " + i);
            out += (describePopulation(population));
        }

        double bestCurrentFitness = population.mostFit().fitness();
        if (bestCurrentFitness > bestRunningFitness) {
            bestRunningFitness = bestCurrentFitness;
            bestGeneration = i;

            if (genSummary) {
                out += ("\nBest solution improved!\n");
            }
        } else {

            if (genSummary) {
                out
                        += ("\nBest solution has not improved "
                        + "since generation " + bestGeneration + "\n");
            }
        }

        if (Main.correctSolution(population.mostFit().fitness())) {
            break;
        }
    }

    if (runSummary) {
        out += ("\n"
                + "\n======================================"
                + "\nRun " + simNo + " complete, best generation = " + bestGeneration
                + "\nBest value = " + population.mostFit().toString()
                + " with fitness " + population.mostFit().fitness()
                + "\n======================================"
                + "\n\n\n");
    }

    if (!out.equals("")) {
        GUI.output(out);
    }

    return bestGeneration;
}

/**
 * Outputs a description of the current state of the population to the console.
 * @param population
 */
private static String describePopulation(Generation population) {
    Chromosome best = population.mostFit();
    return ("\n"
            + "-------------------------------------------------"
            + "\n"
            + "Population = " + population.toString()
            + "\n"
            + "Fitness"
            + ": Total = " + population.totalFitness(false)
            + ", Average = " + population.averageFitness(false)
            + "\n"
            + "Best solution = " + best.toString()
            + " with fitness " + best.fitness()
            + "\n"
            + "-------------------------------------------------");
}

/**
 * Applies one evolutionary step (new generation) to the current population to
 * produce a new generation.
 * @param currentPop The current generation
 * @return The next generation
 */
private Generation evolve(Generation currentPop) {

    if (genDetail) {
        out += "\n\n - - - - - - - - - - - - - - - - "
                + " Running iteration "
                + " - - - - - - - - - - - - - - - -";
    }

    // Array to hold new population members
    Chromosome[] offspring = new Chromosome[currentPop.popSize()];
    ArrayList<Chromosome> matingPool;

    // Apply elitism if nessesary
    if (elitism > 0) {
        applyElitism(currentPop, offspring);
    }

    // Fill mating pool via selection method (Currently only FPS)
    matingPool = fitnessProportionalSelection(currentPop);

    // Produce the non-elites of the next generation through mating
    mate(matingPool, offspring);

    // Apply mutation if nessesary
    if (mutationChance > 0) {
        applyMutation(offspring);
    }

    if (genDetail) {
        out += "\n";
    }

    return new Generation(offspring);
}

/**
 * Applies fitness proportional selection to currentPop to produce a mating
 * pool.
 * @param currentPop The current population
 * @return The mating pool
 */
private ArrayList<Chromosome> fitnessProportionalSelection(Generation currentPop) {

    if (genDetail) {
        out += "\n - - - - - - - - - "
                + " Fitness Proportional Selection "
                + " - - - - - - - - - -";
    }

    Chromosome[] existing = currentPop.getPopulation();
    int popSize = existing.length;

    double[] fitness = currentPop.getFitnessArray(sigmaScaling);
    double[] matingProbs = new double[popSize];

    // Determine proportions of total fitness
    double totalFitness = currentPop.totalFitness(sigmaScaling);
    for (int i = 0; i < popSize; i++) {

        matingProbs[i] = fitness[i] / totalFitness;

        if (genDetail) {
            String proportion
                    = Double.toString(matingProbs[i]).substring(0, 3); // 2dp

            out += "\nChromosome  " + existing[i].toString()
                    + " : Fitness = " + Math.round(fitness[i])
                    + " : Proportion = " + proportion;

        }
    }

    // Build mating pool
    ArrayList<Chromosome> matingPool = new ArrayList<>();

    // Select mating pool members based on matingProbs
    for (int i = elitism; i < popSize; i++) {
        int decider = selectFromProb(matingProbs);
        Chromosome selected = existing[decider];
        matingPool.add(selected);
    }

    return matingPool;
}

/**
 * Randomly mates members of matingPool to produce offspring.
 * @param matingPool The population of parents to mate
 * @param offspring Array in which to store children, only indexes >= elitism
 * will be filled
 */
private void mate(ArrayList<Chromosome> matingPool, Chromosome[] offspring) {

    if (genDetail) {
        out += "\n - - - - - - - - - - - - - - - - - - - - - - "
                + " Mating "
                + " - - - - - - - - - - - - - - - - - - - - - -";
    }

    // Produce offspring
    int n = elitism; // Number of chromosomes currently in next gen
    Random random = new Random();
    while (!matingPool.isEmpty()) {

        // Select parents from mating pool
        // Randomly pick first parent
        int r1 = random.nextInt(matingPool.size());
        Chromosome p1 = matingPool.get(r1);

        // Identify viable mates
        ArrayList<Chromosome> options = new ArrayList<>();
        for (Chromosome ch : matingPool) {
            if (!ch.genes().equals(p1.genes())) {
                options.add(ch);
            }
        }

        if (options.isEmpty()) {

            // Add remaining candidates to next gen if no viable mates
            if (genDetail) {
                out
                        += "\nNo viable mating pairs; "
                        + "cloning remaining parents";
            }
            for (Chromosome ch : matingPool) {
                offspring[n] = new Chromosome(ch.genes());
                n++;

                if (genDetail) {
                    out += "\nCloning chromosome " + ch.toString();
                }
            }
            matingPool.clear();

        } else {

            // Pick randomly out of potentials
            int r2 = random.nextInt(options.size());
            Chromosome p2 = options.get(r2);

            // Mate
            Chromosome[] siblings = Chromosome.mate(p1, p2);
            offspring[n] = siblings[0];
            offspring[n + 1] = siblings[1];
            n += 2;

            // Remove parents from mating pool
            matingPool.remove(r1);
            matingPool.remove(r2);

            if (genDetail) {
                out += "\nMating chromosome  "
                        + p1.toString()
                        + "  with  "
                        + p2.toString();
            }
        }
    }
}

/**
 * Preserves the best members of currentPop, mixing them with offspring.
 * Offspring will be filled from index 0 to elitism.
 * @param currentPop The current population
 * @param offspring An array containing the preserved elites
 */
private void applyElitism(Generation currentPop, Chromosome[] offspring) {

    if (genDetail) {
        out += "\n - - - - - - - - - - - - - - - - - "
                + " Applying Elitism "
                + " - - - - - - - - - - - - - - - - -";
    }

    Chromosome[] fittest = currentPop.fittest();

    for (int i = 0; i < elitism; i++) {
        // Preserve best members intact
        offspring[i] = fittest[i];

        if (genDetail) {
            out += "\nPreserving chromosome " + offspring[i].toString()
                    + ", fitness = " + offspring[i].fitness();
        }
    }

}

/**
 * Applies mutation chance to Chromosomes in population.
 * @param population The population to mutate
 */
private void applyMutation(Chromosome[] population) {

    if (genDetail) {
        out += "\n - - - - - - - - - - - - - - - - - - - - - - "
                + " Mutate "
                + " - - - - - - - - - - - - - - - - - - - - - -";
    }

    boolean mutationOccured = false;

    for (int i = elitism; i < population.length; i++) { // Don't mutate elites
        Chromosome ch = population[i];

        for (int bit = 0; bit < ch.genes().length(); bit++) {
            // Apply mutation chance to every gene/bit
            if (Math.random() < mutationChance) {

                if (genDetail) {
                    out
                            += "\nChromosome " + ch.toString()
                            + " mutated gene " + bit;
                }

                ch.mutate(bit);
                mutationOccured = true;

                if (genDetail) {
                    out += "\nnew value: " + ch.toString();
                }
            }
        }
    }
    if (genDetail && !mutationOccured) {
        out += "\nNo mutations occured";
    }
}

/**
 * Randomly selects a value from an array of probabilities. Will return -1 if
 * the input probabilities are invalid.
 * @param probs An array of probabilities.
 * @return The selected index
 */
public static int selectFromProb(double[] probs) {
    double p = Math.random();
    double cumulativeProbability = 0.0;
    for (int i = 0; i < probs.length; i++) {
        cumulativeProbability += probs[i];
        if (p <= cumulativeProbability) {
            return i;
        }
    }
    return -1; // inputs are invalid!
}

}
