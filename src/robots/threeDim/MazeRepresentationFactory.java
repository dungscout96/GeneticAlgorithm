package robots.threeDim;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
//package edu.gettysburg.cs.careen;

import genetic.ChromosomeFactory;

/**
 *
 * @author cpresser
 */
public class MazeRepresentationFactory extends ChromosomeFactory<MazeRepresentation>{
    public MazeRepresentation createChromosome(){
        return new MazeRepresentation();
    }
    
    public MazeRepresentation createRandomChromosome(double prob){
        MazeRepresentation maze = createChromosome();
        maze.randomize(prob);
        return maze;
    }
    
}
