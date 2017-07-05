package genetic;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
//package edu.gettysburg.cs.careen;

import java.util.ArrayList;
/**
 *
 * @author cpresser
 */
public abstract class ChromosomeFactory<T extends GeneticChromosome> {
    public abstract T createChromosome();
    public abstract T createRandomChromosome(double probability);
    
    public T crossover(ArrayList<T> parents){
        T result = createChromosome();
        
        int numParents = parents.size();
        
        //for each bit,
        for(int i = 0; i < result.length(); i++){
        
            int n = TheRandom.getInstance().nextInt(numParents);
            
            result.setBit(i, parents.get(n).getBit(i));
            
        }
        
        return result;
    }
}
