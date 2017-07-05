package genetic;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
//package edu.gettysburg.cs.careen;

/**
 *
 * @author cpresser
 */
public class GeneticMultithreadEvaluator{
    protected void evaluateAll(){
        /*
        for(C c: population){
            c.calculateFitness();
        }
        */
        
    }
    
    //Which is better? 
    //A) Creating a CachedThreadPool (or FixedThreadPool) and running each evluation as a different Runnable
    //  requires creating an object for each item in the population
    
    //B) Same as above, but for several evaluations at a time (e.g 10 or 100)
    
    //C) Create a bunch of threads that just idle (wait) when population management is going on and engage
    //  and work between them. These thread each would work on fixed objects e.g 0..9, 10..19 etc
    
    //D) Threads that work on a bunch of objects, then request more when they are done.
    //  requires load balancing and more synchronization
    
    //E) Single threaded
    
    //F) Networked with the best of these. (Although distributing the objects in the network is
    //  almost exactly the same problem.
    
    //This is an interesting question?
    
    //What variables to consider?
    //1. Random seed value (at least constant, better to test a bunch of values)
    //2. Number of objects in population.
    //3. Evaluation method (above)
    //4. 
    
    
    
    
}
