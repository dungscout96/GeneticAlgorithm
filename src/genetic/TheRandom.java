package genetic;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
//package edu.gettysburg.cs.careen;

import java.util.Random;

/**
 *
 * @author cpresser
 */
public class TheRandom {
    static Random rand;
    static long seed = 0L;
    
    public static void setSeed(long s){
        seed = s;
    }
    
    public static Random getInstance(){
        if(rand == null){
            rand = new Random();
            if(seed == 0L){
                seed = rand.nextLong();
                System.out.printf("Random seed: %d\n", seed);
            }
            rand.setSeed(seed);
        }
        return rand;
    }
}
