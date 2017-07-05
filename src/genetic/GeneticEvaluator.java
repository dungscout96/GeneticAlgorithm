package genetic;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
//package edu.gettysburg.cs.careen;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author cpresser
 */
public class GeneticEvaluator<C extends GeneticChromosome, F extends ChromosomeFactory<C>>{
 
    public static final int DEFAULT_POPULATION = 200;
    public static final int DEFAULT_GENERATIONS = 50;
    public static final int SAME_BITS = 4;
    public static final int KEEP_TOTAL = DEFAULT_POPULATION/2;
    public static final int PARENTS = 4;

    public static final double DEFAULT_BIT_PROB = 0.5; //0.3;
    
    private F factory;
    private ArrayList<C> population;
    private int total_population;
    private int total_generations;
    private int numParents;
    private int sameBits;
    private int keepPop;
    private boolean logging;
    private PrintWriter popWriter;
    private PrintWriter statWriter;
    private boolean singleThread = false;
    //private boolean fitEnding = false;
    //private int endFitness;
    
    //best fitness so far
    //private int bestFitness;
    
    //first generation that the best appeared
    //private int bestFirstGen;

    public GeneticEvaluator(F factory, 
    		int popTotal, 
    		int gens,
    		int parents,
    		int keep,
    		int bits,
    		String filename,
    		boolean single) throws FileNotFoundException {
    	this(factory, popTotal, gens, parents, keep, bits, filename);
    	singleThread = single;
    }
    
/*
    public GeneticEvaluator(F factory, 
    		int popTotal, 
    		int gens,
    		int parents,
    		int keep,
    		int bits,
    		String filename,
    		boolean single,
    		int endFitness) throws FileNotFoundException {
    	this(factory, popTotal, gens, parents, keep, bits, filename);
    	singleThread = single;
    	this.endFitness = endFitness;
    	this.fitEnding = true;
    }
    */
    public GeneticEvaluator(F factory, 
    		int popTotal, 
    		int gens,
    		int parents,
    		int keep,
    		int bits,
    		String filename) throws FileNotFoundException {
        this.factory = factory;
    	total_population = popTotal;
    	total_generations = gens;
    	sameBits = bits;
    	numParents = parents;
    	keepPop = keep;
    	
        //1. create a random population
        population = new ArrayList<C>();
        
        for(int i = 0; i < total_population; i++){
            population.add(factory.createRandomChromosome(DEFAULT_BIT_PROB));
        }
        
        if(filename != null){
        	logging = true;
        	String info = String.format("%d_%d_%d_%d_%d_", 
					    popTotal, 
					    total_generations, 
					    numParents, 
					    keepPop, 
					    sameBits);
        	popWriter = new PrintWriter("pop_" + info + filename + ".csv");
        	statWriter = new PrintWriter("stat_" + info +  filename + ".csv");
        }
    
    }
    
    public GeneticEvaluator(F factory, int popTotal, String filename) throws FileNotFoundException {
    	this(factory, popTotal, DEFAULT_GENERATIONS, PARENTS, KEEP_TOTAL, SAME_BITS, filename);
 
    }
    

    public GeneticEvaluator(F factory, String filename) throws FileNotFoundException {
    	this(factory, DEFAULT_POPULATION, filename);
    }
    
    public GeneticEvaluator(F factory, int popTotal){
        this.factory = factory;
        this.total_population = popTotal;
        //1. create a random population
        population = new ArrayList<C>();
        
        for(int i = 0; i < total_population; i++){
            population.add(factory.createRandomChromosome(DEFAULT_BIT_PROB));
        }
        
        logging = false;
    }
    
    public GeneticEvaluator(F factory){
        this(factory, DEFAULT_POPULATION);
    }
    
    public void evolve(){
        boolean done = false;
        int generation = 0;
        
        /*
        int[] firstGen = null;
        
        if(fitEnding){
        	firstGen = new int[endFitness+1];
        }
        */
        
        if(logging ) //&& !fitEnding)
        	statWriter.println("Gen,Num Pos Fit, Avg Positive Fitness,Max Fitness,Dups Removed,Weak Removed,New Children,Eval Time, Mgmt Time, Total Time, Avg Eval Time");
        while(!done){
            System.err.println("Generation: " + generation);
        	if(logging)
        		System.out.println("----------------------------------------");
            long t0 = Calendar.getInstance().getTimeInMillis();
            //2. evaluate the population of n chromosomes.
            if(singleThread){
            	evaluateAllSingleThread();
            }
            else {
                evaluateAll();
            	
            }
            
            long t1 = Calendar.getInstance().getTimeInMillis();

            if(logging){ // && !fitEnding){
            	popWriter.printf("%d,", generation);
            	statWriter.printf("%d,", generation);
            }
            
            //population fitnesses:
            int sum = 0;
            int count = 0;
            for(int i = 0; i < population.size(); i++){
            	 if(logging  && i % 10 == 0) // if(logging  && !fitEnding && i % 10 == 0)
                    System.out.println();
                int fit = population.get(i).getFitness();
                if(fit >= 0){
                	sum += fit;
                	count++;
                }
                if(logging)// && !fitEnding)
            		System.out.printf("\t%d", fit);
                if(logging){// && !fitEnding){
                	popWriter.printf("%d,", fit);
                }
            }
            if(logging)
        		System.out.println();
            
            
            //3. find the best
            C best = getBest();
            
            /*
            if(best.getFitness() > this.bestFitness){
            	this.bestFitness = best.getFitness();
            	this.bestFirstGen = generation;
            	firstGen[this.bestFitness] = generation;
            }
            
            if(logging && !fitEnding){
                popWriter.println();
                //write out stats
                statWriter.printf("%d,", count);
                statWriter.printf("%f,", ((double)sum)/count);
                statWriter.printf("%d,", best.getFitness());
            }
            */
            
            //4. remove "duplicates" (chromosomes that differ in at most d bits from the best one)
            //removeDuplicates(best);
            
            //5. Selection: get rid of the weakest (up to w)
            removeWeakest();

            //6. Reproduction: have the rest crossover to make new ones
                //select k chromosomes and randomly select bits from amongst them.
            //7. mutate the new ones (with probability p)
            reproduce();

            //8. Ending condition?
                //number of generations
                //percent improvement
            generation++;
            long t2 = Calendar.getInstance().getTimeInMillis();
            /*
            if(fitEnding){
            	done = (endFitness <= best.getFitness());
            }
            else {
            	done = (generation > total_generations);
            }
            */
            done = (generation > total_generations);
            if(logging){
            	System.out.printf("Completed generation: %d\n", generation);
            	System.out.printf("Time Evaluating: %d milliseconds.\n", t1 - t0); 
            	System.out.printf("Time Managing: %d milliseconds.\n", t2 - t1); 
            	System.out.printf("Time Total: %d milliseconds.\n", t2 - t0);
            }
            long avgTime = 0;
            for(C c: population){
                avgTime += c.getTime();
            }
            avgTime /= population.size();
            if(logging){
            	System.out.printf("Average Evaluation Time (entire population): %d milliseconds.\n", 
                    avgTime);
                System.out.println("Best maze: " + best);
            }
            
            if(logging){ // && !fitEnding){
            	statWriter.printf("%d,%d,%d,%d\n", t1-t0, t2-t2, t2-t0, avgTime);
            }
            
            //print first gen fitnesses
            /*
            if(logging){ && fitEnding){
            	System.out.printf("%10s %10s\n", "Fitness", "Generation");
            	for(int i = 0; i < firstGen.length; i++){
            		if(firstGen[i] > 0)
            			System.out.printf("%10d %10d\n", i, firstGen[i]);
            	}
            }
            */
            //9. repeat from step 2.
        } //end while(!done)
        
        
        if(logging){
        	popWriter.close();
        	statWriter.close();
        }
    }
    
    public C getBest(){
        return Collections.max(population);
    }
    
    protected void evaluateAllSingleThread(){
    	for(C c: population){
    		c.calculateFitness();
    	}
    }
    
    protected void evaluateAll(){
    	/*
        for(C c: population){
            c.calculateFitness();
        }
    	 */



    	//Create thread pool --Why didn't this speed things up?
    	ExecutorService executor = Executors.newFixedThreadPool(8);//Executors.newCachedThreadPool();
    	//ExecutorService executor = Executors.newCachedThreadPool();

    	//start threads

    	for(C c: population){
    		executor.execute(c);
    	}
    	//shutdown
    	executor.shutdown();

    	try {
    		//wait
    		executor.awaitTermination(1, TimeUnit.DAYS);
    	} catch (InterruptedException ex) {
    	}

        
    }
    
    private void removeDuplicates(C best){
        Iterator<C> it = population.iterator();
        int total = 0;
        while(it.hasNext()){
            if(best.distance(it.next()) < sameBits){
                it.remove();
                total++;
            }
        }
        if(logging)
    		System.out.printf("Duplicates removed: %d.\n", total);
        
        if(logging) // && !fitEnding) 
        	statWriter.printf("%d,", total);
        
        //add best back in
        population.add(best);
    }
    
    
    //consider removing all "unfit" individuals and at least currentPop - KEEP_TOTAL
    private void removeWeakest(){
        int currentPop = population.size();
        int total = 0;
        for(int i = 0; i < (currentPop - keepPop); i++){
            population.remove(Collections.min(population));
            total++; //didn't have to keep track, but wanted flexibility to change
        }
        if(logging)
    		System.out.printf("Weakest removed: %d.\n", total);
        if(logging) // && !fitEnding) 
        	statWriter.printf("%d,", total);
      
    }
    
    private void reproduce(){
        int currentPop = population.size();
        int makeNew = total_population - population.size();
        int total = 0;
        for(int i = 0; i < makeNew; i++){
            ArrayList<C> parents = new ArrayList<C>(numParents);
            for(int p = 0; p < numParents; p++){
            	//limiting the index to current population keeps me from using new individuals as parents
                int index = TheRandom.getInstance().nextInt(currentPop);
                parents.add(population.get(index));
            }
            C gen = factory.crossover(parents);
            gen.mutate();
            
            population.add(gen);
            total++;
        }
        if(logging)
    		System.out.printf("New children: %d.\n", total);
        if(logging)// && !fitEnding) 
        	statWriter.printf("%d,", total);
    }

    public void printPopulation(){
    	if(!logging) return;
    	for(GeneticChromosome c: population){
    		System.out.println("______________________________________");
    		System.out.println(c);
    	}
    }
    
    /*
    //get the earliest generation that this was found
    public int getFirstGenerationOfBest(){
    	return this.bestFirstGen;
    }
    */

}
