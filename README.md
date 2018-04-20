# The game
http://cs.gettysburg.edu/~cpresser/web3d/x3dom/MazeMockUp2.html

# GeneticAlgorithm
To create a Genetic Algorithm for a problem.
1. Create a subclass of GeneticChromosome.
2. Create a subclass of ChromosomeFactory.
3. Create a main program to run the simulation.



1. Create a subclass of GeneticChromosome.
	Override the following abstract methods:
    public abstract int calculateFitness();
    public abstract int length();
    public abstract boolean getBit(int index);
    public abstract void setBit(int index, boolean value);

2. Create a subclass of ChromosomeFactory.
	Override the following abstract methods:
	//create a default chromosome
    public abstract T createChromosome();
    
    //create a chromosome with random components set where the 
    //parameter is the probability that a bit is set to 1
    public abstract T createRandomChromosome(double probability);
    
    //override crossover to define different ways to handle the crossover operation
    // the default is random

3. Create a main program to run the simulation.
	a. Construct a GeneticEvaluator object.
	b. Call the evolve method on that object.
