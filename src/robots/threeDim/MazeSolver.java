package robots.threeDim;
//package edu.gettysburg.cs.careen;

import java.util.Calendar;

import genetic.GeneticEvaluator;

public class MazeSolver {

    public static void main(String[] args) {
        //TheRandom.setSeed(-5614583290598599120L);
        //buildOne();
    	
        try {
			geneticPopulation(args);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    //create a population of solutions using a genetic algorithm
    public static void geneticPopulation(String[] args) throws Exception{
    	try {
    		if(args.length == 0){
    			usage();
    			System.exit(0);
    		}
    		Calendar today = Calendar.getInstance();
    		MazeRepresentationFactory factory = new MazeRepresentationFactory();
    		GeneticEvaluator<MazeRepresentation, MazeRepresentationFactory> evaluator =
    				new GeneticEvaluator<MazeRepresentation, MazeRepresentationFactory>(factory,
    						Integer.parseInt(args[0]),
    						Integer.parseInt(args[1]),
    						Integer.parseInt(args[2]),
    						Integer.parseInt(args[3]),
    						Integer.parseInt(args[4]),
    						String.format("%1$tm-%1$td-%1$tY_%1$tH-%1$tM-%1$tS", today));


    		long t0 = Calendar.getInstance().getTimeInMillis();

    		evaluator.evolve();


    		long t1 = Calendar.getInstance().getTimeInMillis();


    		MazeRepresentation maze = evaluator.getBest();
    		if (!maze.hasSolution()) {
    			System.out.println("No Solution");
    		} else {
    			System.out.println("Solution found: " + maze);
    		}

    		System.out.printf("Time solving: %d milliseconds.\n", t1 - t0);
    		evaluator.printPopulation();
    	}
    	catch(NumberFormatException nfe){
    		usage();
    	}
    	catch(ArrayIndexOutOfBoundsException aioobe){
    		usage();

    	}
    }

    public static void usage(){
		System.out.println("Usage: geneticPopulation pop gen parents bits keep");
		System.out.println("\tpop: population size");
		System.out.println("\tgen: number of generations");
		System.out.println("\tparents: number of parents required to produce offspring");
		System.out.println("\tkeep: number of individuals that survive to next generation");
		System.out.println("\tbits: hamming distance required to consider two individuals to be \"equal\".");
		//System.out.println("\t");
    	
    }
    
    
    public static void buildOne() {
        //just a test
        MazeRepresentation maze = new MazeRepresentation();
        maze.randomize(0.3);

        System.out.println(maze);

        long t0 = Calendar.getInstance().getTimeInMillis();

        boolean solved = maze.solve();
        long t1 = Calendar.getInstance().getTimeInMillis();
        if (!solved) {
            System.out.println("No Solution");
        } else {
            System.out.println("Solution found: " + maze.getScore());
            System.out.println(maze.getSolution());
        }
        System.out.printf("Time solving: %d milliseconds.\n", t1 - t0);
    }
}
