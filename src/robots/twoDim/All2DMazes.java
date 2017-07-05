package robots.twoDim;

import java.util.*;
import java.io.*;

public class All2DMazes {

	/*
Total fit: 3847
Average fitness: 92.870322
Highest fitness: 185
First with highest: 2308
Number with highest: 8
Longest: 17
First with longest: 2308
Number with longest: 8
	 */


	public static void main(String[] args) throws Exception {
		boolean write = false;
		String filename = "all2D.csv";

		PrintWriter outFile = null;

		if(write){
			outFile = new PrintWriter(filename);
		}
		//print out the fitness of all 2^16 2D mazes
		//5x5 puzzle

		MazeDescriptor2D desc = new MazeDescriptor2D(5, 5, new byte[]{0, 24}, new byte[]{18, 6});
		MazeRepresentation2D.MAZE_DESC = desc;

		//4x4 puzzle
		//MazeDescriptor2D desc = MazeRepresentation2D.MAZE_DESC;

		BitSet bs = new BitSet(desc.size);
		int countFit = 0;

		int maxMaze = -1;
		int maxVal = -1;
		int countMax = 0;

		int longMaze = -1;
		int longVal = -1;
		int countLong = 0;

		int sumFitness = 0;

		int total = 1 << desc.size;
		for(int i = 0; i < total; i++){
			//for(int i = 0x5104; i <= 0x5104+16; i++){
			if(i % 100000 == 0)
				System.err.printf("Done %d\n", i);
			bs.clear();
			int p = 1;
			for(int d = 0; d < desc.size; d++){
				if((i & p) != 0){
					bs.set(d);
				}
				p <<= 1;
			}

			MazeRepresentation2D mr = new MazeRepresentation2D(bs);

			if(!mr.solve()){
				//no solution
				if(write){
					outFile.printf("%d,,,,,\n", i);
				}
			}
			else {
				countFit++;
				MazeState2D sol = mr.getSolution();
				int fit = mr.getScore();
				int steps = sol.getSteps();
				sumFitness += fit;

				//most fit
				if(fit > maxVal){
					maxVal = fit;
					maxMaze = i;
					countMax = 1;
				}
				else if(fit == maxVal){
					countMax++;
				}

				//longest
				if(steps > longVal){
					longVal = steps;
					longMaze = i;
					countLong=1;
				}
				else if(steps == longVal){
					countLong++;
				}
				if(write){
					outFile.printf("%d,%d,%d,%d,%d,%d\n", 
							i,
							fit,
							steps,
							sol.getHitWalls(),
							sol.getHitBarrier(),
							sol.getHitBots()
							);
				}

				//System.out.println(bs);
				//convert int to bitset
			}

		} //end for loop
		System.out.printf("Total fit: %d\n", countFit++);
		System.out.printf("Average fitness: %f\n", ((double)sumFitness)/countFit);

		System.out.printf("Highest fitness: %d\n", maxVal);
		System.out.printf("First with highest: %d\n", maxMaze);
		System.out.printf("Number with highest: %d\n", countMax);


		System.out.printf("Longest: %d\n", longVal);
		System.out.printf("First with longest: %d\n", longMaze);
		System.out.printf("Number with longest: %d\n", countLong);
		if(write){
			outFile.close();
		}
	}

}
