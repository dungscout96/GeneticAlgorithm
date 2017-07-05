package robots.twoDim;
import java.util.BitSet;
import java.util.Formatter;
import java.util.PriorityQueue;

import genetic.GeneticChromosome;
import genetic.TheRandom;

public class MazeRepresentation2D  extends GeneticChromosome {

	/*
    public static final int X_MAX = 4;
    public static final int Y_MAX = 4;
    public static final int SIZE = X_MAX * Y_MAX;
    public static final int X_SHIFT = 2;
    public static final int Y_SHIFT = 0;
    public static final int MASK = 3;
    public static final int POS_BITS = 4;
    */
	public static MazeDescriptor2D MAZE_DESC = 
			new MazeDescriptor2D(4, 4, new byte[]{0, 15}, new byte[]{10, 5});
	
    //fitness coefficients
    public static final int BOT_MULTIPLIER = 5;
    public static final int WALL_MULTIPLIER = -2;
    public static final int BARRIER_MULTIPLIER = 1;
    public static final int STEP_MULTIPLIER = 10;
    
    
    protected BitSet board;
    protected MazeState2D solution;
    protected boolean solved;
    
    public MazeRepresentation2D() {
        board = new BitSet(MAZE_DESC.size);
        solution = null;
        solved = false;
        
    }
    
    public MazeRepresentation2D(BitSet maze) {
        board = (BitSet) maze.clone();
        solution = null;
        solved = false;
    }

    public MazeState2D getSolution() {
        return solution;
    }
    
    public void clearTerminals() {
        //corners should really be empty
        for (int i = 0; i < MAZE_DESC.r_start.length; i++) {
            board.clear(MAZE_DESC.r_start[i]);
            board.clear(MAZE_DESC.r_dest[i]);
        }

    }

    public void randomize(double probability) {

        for (int i = 0; i < MAZE_DESC.size; i++) {
            if (TheRandom.getInstance().nextDouble() < probability) {
                board.set(i);
            }
        }
        clearTerminals();
    }
    
    public boolean outOfBounds(int x, int y) {
        if (x < 0 || y < 0 || x >= MAZE_DESC.x_max || y >= MAZE_DESC.y_max) {
            return true;
        }
        return false;
    }

    public boolean positionOpen(int x, int y) {
        if (x < 0 || y < 0 || x >= MAZE_DESC.x_max || y >= MAZE_DESC.y_max) {
            return false;
        }

        int index = positionToIndex(x, y);

        return board.get(index);
    }

    public void setPosition(int x, int y,  boolean b) {
        if (x < 0 || y < 0 || x >= MAZE_DESC.x_max || y >= MAZE_DESC.y_max) {
            return;
        }

        int index = positionToIndex(x, y);
        if (b) {
            board.set(index);
        } else {
            board.clear(index);
        }
    }
    

    public int positionToIndex(int x, int y) {
    	return y*MAZE_DESC.x_max + x;
    	/*
        int b = 0;
        b = (x << MAZE_DESC.x_shift);
        b |= (y << MAZE_DESC.y_shift);
        return b;
        */
    }
 

    public int getX(int position) {
        //return (position >> MAZE_DESC.x_shift) & MAZE_DESC.x_mask;
    	return position % MAZE_DESC.x_max;
    }

    public int getY(int position) {
        //return (position >> MAZE_DESC.y_shift) & MAZE_DESC.y_mask;
    	return position / MAZE_DESC.x_max;
    }
    
    public boolean hasBarrier(int position) {
        return board.get(position);
    }

    public boolean hasBarrier(int x, int y) {
        return board.get(positionToIndex(x, y));
    }

    public String indexToString(int index) {
        Formatter f = new Formatter();
        f.format("(%d, %d)", getX(index), getY(index));
        return f.out().toString();
    }
    

    @Override
    public String toString() {
        String result = toStringJSArray();
        result += "\nSolution: ("+ fitness +"): " + getSolution();
        return result;
    }
    
    public String toStringJSArray() {
        Formatter f = new Formatter();
        for (int y = 0; y < MAZE_DESC.y_max; y++) {
            f.format("[");

            for (int x = 0; x < MAZE_DESC.x_max; x++) {
                //f.format("[");
                    if (board.get(positionToIndex(x, y))) {
                        f.format("1");
                    } else {
                        f.format("0");
                    }
                //f.format("]");
                f.format((x != MAZE_DESC.x_max-1)?",":"");
                f.format(" ");
            } //end for int x
            f.format("]");
            f.format((y != MAZE_DESC.y_max-1)?",":"");
            f.format("\n");
        } //end for int y

        return f.out().toString();
    }
    
    public int getScore() {
        //return score;
        return fitness;
    }

    private int score() {
        int result = 0;

        //check if a solution can be found
        //if not, also low
        if (solution == null) {
            result = Integer.MIN_VALUE/2;
        } else {
            //lower number of obstacles is good

            //higher number of steps are good
            result += STEP_MULTIPLIER * solution.steps;

            //higher robot interaction is good-run into other robots
            result += BOT_MULTIPLIER * solution.hitBots;

            //higher barrier interaction is also good
            result += BARRIER_MULTIPLIER * solution.hitBarrier;

            //penalty for hitting walls
            result += WALL_MULTIPLIER * solution.hitWalls;

        }
        return result;
    }

    private int shortPathScore() {
        int result = 0;

        //must have a solution

        //shorter solution is better

        //higher robot interaction is better

        //higher number of robot reversals is better

        return result;
    }


    public boolean solve() {
        if(!solved){
        
            BitSet used = new BitSet(MAZE_DESC.total_states);

            MazeState2D state = new MazeState2D(this);

            PriorityQueue<MazeState2D> queue = new PriorityQueue<MazeState2D>();
            queue.add(state);

            boolean done = false;
            while (!done) {
                state = queue.poll();
                //System.out.println(state);
                if (state == null || state.isGoalState()) {
                    done = true;
                } else {
                    state.next(queue, used);
                }
            }
            solution = state;

            //calculate score
            fitness = score();
            solved = true;
        }
        //clean up
        return solution != null;
    }
    
    public boolean hasSolution(){
        return solution != null;
    }

    @Override
    public int calculateFitness() {
        solve();
        return getScore();
    }
    
    
    @Override
    public int length() {
       return MAZE_DESC.size;
    }

    @Override
    public boolean getBit(int index) {
        return board.get(index);
    }

    @Override
    public void setBit(int index, boolean value) {
        board.set(index, value);
    }

    @Override
    public void mutate(){
        super.mutate();
        //give this thing a fighting chance
        clearTerminals();
    }
    
}
