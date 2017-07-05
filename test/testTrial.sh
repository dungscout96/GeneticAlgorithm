#!/bin/bash

POP=20
GEN=20
PARENTS=4
KEEP=4
BITS=1

java -cp ../out/production/GeneticAlgorithmNewFitness -Xmx6g robots.threeDim.MazeSolver $POP $GEN $PARENTS $KEEP $BITS > testTrial1NewFitness5.txt 2> error1.txt
#java -cp ../bin -Xmx4g robots.threeDim.MazeSolver $POP $GEN $:PARENTS $KEEP $BITS > testTrial2.txt 2> error2.txt
