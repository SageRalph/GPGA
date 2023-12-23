package GeneticAlgorithm;
import java.util.Random;

/**
 * @author Sage Ralph <SageJeanRalph@Gmail.com>
 */
public class Chromosome {

private static int rangeMod = 0;
private static int geneNo = -1;
public static int min, max;

private String genes;
// Values are calculated when first needed, this makes repeated Chromosome 
// creation (typically when failing validation) much quicker.
private boolean fitnessOutdated = true;
private boolean valueOutdated = true;
private double fitness;
private int value;

/**
 * Creates a new Chromosome with the genes provided. No validation is performed.
 * @param genes
 */
public Chromosome(String genes) {
    this.genes = genes;
}

/**
 * Creates a new chromosome with random genes (bounded by the range)
 */
public Chromosome() {

    // Only on creation of first Chromosome
    if (geneNo == -1) {

        // Shift range so min is always encoded as binary 0
        rangeMod = 0 - min;

        // Calculate nessesary gene number     
        String maxBinary = binaryString(max);
        geneNo = maxBinary.length();
    }

    // Pick random value
    Random random = new Random();
    value = random.nextInt((max - min) + 1) + min;
    this.genes = binaryString(value);
}

public static void reset() {
    geneNo = -1;
}

public String genes() {
    return genes;
}

private static String binaryString(int value) {

    String binary = Integer.toBinaryString(value + rangeMod);

    // prepend 0s to match length
    while (binary.length() < geneNo) {
        binary = "0" + binary;
    }
    return binary;
}

/**
 * @return This chromosomes fitness
 */
public double fitness() {

    // Allow caching of fitness for efficiency
    if (fitnessOutdated) {
        fitness = Main.fitnessFunction(value());
        fitnessOutdated = false;
    }
    return fitness;
}

/**
 * @return The value represented by this chromosome
 */
public int value() {

    // Allow caching of value for efficiency
    if (valueOutdated) {
        value = Integer.parseInt(genes, 2) - rangeMod;
        valueOutdated = false;
    }
    return value;
}

/**
 * Mates two chromosomes with a random crossover point.
 * @param c1 Chromosome 1
 * @param c2 Chromosome 2
 * @return The children of Chromosomes 1 and 2
 */
public static Chromosome[] mate(Chromosome c1, Chromosome c2) {
    Random random = new Random();
    Chromosome[] children = new Chromosome[2];

    // Keep trying diffrent crossover points until both children are valid
    do {
        int crossoverPoint = random.nextInt(c1.genes.length() - 2);

        String c1p1 = c1.genes.substring(0, crossoverPoint);
        String c1p2 = c1.genes.substring(crossoverPoint);
        String c2p1 = c2.genes.substring(0, crossoverPoint);
        String c2p2 = c2.genes.substring(crossoverPoint);

        children[0] = new Chromosome(c1p1 + c2p2);
        children[1] = new Chromosome(c2p1 + c1p2);

    } while (!(children[0].valid() && children[1].valid()));

    return children;
}

public void mutate(int bit) {
    String originalGenes = genes.substring(0); // Get value, not pointer

    Character nb = genes.charAt(bit) == '0' ? '1' : '0';
    genes = genes.substring(0, bit) + nb + genes.substring(bit + 1);

    valueOutdated = true;
    fitnessOutdated = true;

    if (!valid()) {
        // TODO shouldn't just give up
        genes = originalGenes;
    }
}

@Override
public String toString() {
    return genes + " (" + value() + ")";
}

public boolean valid() {
    return (value() >= min && value() <= max);
}

}
