package GeneticAlgorithm;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import net.objecthunter.exp4j.ExpressionBuilder;

/**
 *
 * @author David Ralph <David.M.L.Ralph@Gmail.com>
 */
public class Main extends Thread {

// GUI variables
public static boolean allowKnownSolution;
public static double knownMax, knownMin;
public static int threadCount, runCount,
        rangeMin, rangeMax, populationSize, maxGenerations, elitism;
public static double mutationChance;
public static boolean genDetail, genSummary, runSummary, sigmaScaling;
public static String fitnessFunction;

// Fields
private static final AtomicInteger simsRun = new AtomicInteger();
private static int[] bestGenerations;

/**
 * The GAs fitness function.
 * @param x The raw value indicated by the chromosome
 * @return The fitness of the chromosome
 */
public static double fitnessFunction(double x) {
    //    double result = x*x;   

    net.objecthunter.exp4j.Expression e = new ExpressionBuilder(fitnessFunction)
            .variables("x")
            .build()
            .setVariable("x", x);
    double result = e.evaluate();

    return result + knownMin;
}

/**
 * Customise this function to provide a known solution as a stopping point.
 * @param value The calculated fitness value
 * @return Whether value is a known correct solution
 */
public static boolean correctSolution(double value) {
    return allowKnownSolution && value == knownMax;
}

public static void simulate() throws InterruptedException {

    // Setup
    simsRun.set(0);
    bestGenerations = new int[runCount];
    Chromosome.reset();

    long startTime = System.currentTimeMillis();

    // Setup threads and start simulations
    Main[] threads = new Main[threadCount];
    for (int i = 0; i < threadCount; i++) {
        threads[i] = new Main();
        threads[i].start();
    }

    // Wait for simulations to finish
    for (int i = 0; i < threadCount; i++) {
        threads[i].join();
    }

    // Process results
    int averageBestGen = 0;
    int maxBestGen = 0;
    for (int i = 0; i < runCount; i++) {
        averageBestGen += bestGenerations[i];
        if (bestGenerations[i] > maxBestGen) {
            maxBestGen = bestGenerations[i];
        }
    }
    averageBestGen /= runCount;

    long timeTaken = System.currentTimeMillis() - startTime;

    // Output results
    GUI.output(""
            + runCount + " Simulations completed in " + timeTaken
            + "ms using " + threadCount + " threads"
            + "\n------------------------------------------"
            + "\nGenerations before completion:\n"
            + Arrays.toString(bestGenerations)
            + "\nAverage: " + averageBestGen + ", Max: " + maxBestGen
            + "\n\n"
    );
}

@Override
public void run() {
    int simNo = simsRun.incrementAndGet(); // Starts at 1
    while (simNo <= runCount) {
        bestGenerations[simNo - 1] = new Simulation("" + simNo,
                rangeMin,
                rangeMax,
                populationSize,
                maxGenerations,
                elitism,
                mutationChance,
                sigmaScaling,
                genDetail,
                genSummary,
                runSummary
        ).run();
        simNo = simsRun.incrementAndGet();
    }
}

/**
 *******************************************************************************
 * Command-line Interface
 * *****************************************************************************
 */
///**
// * @param args the command line arguments
// * @throws java.lang.InterruptedException
// */
//public static void main(String[] args) throws InterruptedException, IOException {
//    promptForSettings();
//
//}
//
//private static void promptForSettings() throws InterruptedException, IOException {
//
//    describeParameters();
//    
//    Scanner in = new Scanner(System.in);
//    int menuOption;
//    do {
//
//        // User selection
//        menuOption = in.nextInt();
//        switch (menuOption) {
//            case 1:
//                runCount = in.nextInt();
//                System.out.println("Run count = "+runCount);
//                break;
//            case 2:
//                populationSize = in.nextInt();
//                System.out.println("Population size = "+populationSize);
//                break;
//            case 3:
//                maxGenerations = in.nextInt();
//                System.out.println("Max generations = "+maxGenerations);
//                break;
//            case 4:
//                mutationChance = in.nextDouble() / 100;
//                System.out.println("Mutation chance = "+mutationChance*100+"%");
//                break;
//            case 5:
//                elitism = in.nextInt();
//                System.out.println("Elitism = "+elitism);
//                break;
//            case 6:
//                rangeMin = in.nextInt();
//                System.out.println("Range = [" + rangeMin + ", " + rangeMax + "]");
//                break;
//            case 7:
//                rangeMax = in.nextInt();
//                System.out.println("Range = [" + rangeMin + ", " + rangeMax + "]");
//                break;
//            case 0:
//                simulate();
//                System.out.println("Press enter to continue,"
//                        + "\ntype menu for options");
//                String entry = in.nextLine();
//                if (entry.equals("menu")) {
//                    describeParameters();
//                }
//                break;
//            default:
//                break;
//        }
//    } while (menuOption != -1);
//}
//
//private static void describeParameters() {
//    System.out.println("\n"
//            + "\nEnter number code followed by value to "
//            + "\nmodify settings. Enter 0 to run or -1 to exit"
//            + "\n----------------------------------------------"
//            + "\n1:   Total run count = " + runCount
//            + "\n2:   Population size = " + populationSize
//            + "\n3:   Max generations (per run) = " + maxGenerations
//            + "\n4:   Mutation chance (per bit) = " + mutationChance * 100 + "%"
//            + "\n5:   Elitism (Chromosomes preserved between generations) = "
//            + elitism
//            + "\n6|7: Range [" + rangeMin + ", " + rangeMax + "]"
//            + "\n"
//    );
//}
}
