package robots.twoDim;

import genetic.ChromosomeFactory;

public class MazeRepresentation2DFactory extends ChromosomeFactory<MazeRepresentation2D>{
    public MazeRepresentation2D createChromosome(){
        return new MazeRepresentation2D();
    }
    
    public MazeRepresentation2D createRandomChromosome(double prob){
        MazeRepresentation2D maze = createChromosome();
        maze.randomize(prob);
        return maze;
    }

}
