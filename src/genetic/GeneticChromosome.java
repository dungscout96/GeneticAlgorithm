package genetic;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
//package edu.gettysburg.cs.careen;

import java.io.Serializable;
import java.util.Calendar;

/**
 *
 * @author cpresser
 */
public abstract class GeneticChromosome 
    implements Comparable<GeneticChromosome>, Runnable, Serializable
{
    public static double MUTATION_PROBABILITY = 0.05;
    
    protected int fitness = Integer.MIN_VALUE/2;
    protected long time = 0L;
     
    public int distance(GeneticChromosome other){
        int dist = 0;
        
        for(int i = 0; i < length(); i++){
            if(this.getBit(i) != other.getBit(i))
                dist++;
        }
        
        return dist;
    }
    
    public void mutate(){
        for(int i = 0; i < length(); i++){
            if(TheRandom.getInstance().nextDouble() < 
                    MUTATION_PROBABILITY){
                setBit(i, !getBit(i));
            }
        }
    }
    
    public void run(){
        long t0 = Calendar.getInstance().getTimeInMillis();
        try{
            calculateFitness();
        }
        catch(Throwable t){
            t.printStackTrace();
        }
        long t1 = Calendar.getInstance().getTimeInMillis();
        time = t1-t0;
    } 
    
    public long getTime(){
        return time;
    }
    
    public int getFitness(){
        return fitness;
    }
    
    @Override
    public int compareTo(GeneticChromosome other){
        return this.fitness - other.fitness;
    }
    
    public int getBitsAsInt(int low, int high){
    	int result = 0;
    	int shift = 1;
    	for(int i = low; i <= high; i++){
    		if(getBit(i)){
    			result += shift;
    		}
    		shift <<= 1;
    	}
    	return result;
    }

    
    
    public abstract int calculateFitness();
    public abstract int length();
    public abstract boolean getBit(int index);
    public abstract void setBit(int index, boolean value);

}
