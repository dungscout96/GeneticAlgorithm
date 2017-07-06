package robots.threeDim;


//package edu.gettysburg.cs.careen;

import java.util.BitSet;
import java.util.Formatter;
import java.util.PriorityQueue;

import genetic.GeneticChromosome;
import genetic.TheRandom;

public class MazeRepresentation  extends GeneticChromosome {

    public static final int X_MAX = 4;
    public static final int Y_MAX = 4;
    public static final int Z_MAX = 4;
    public static final int SIZE = X_MAX * Y_MAX * Z_MAX;
    public static final int X_SHIFT = 4;
    public static final int Y_SHIFT = 2;
    public static final int Z_SHIFT = 0;
    public static final int MASK = 3;
    public static final int POS_BITS = 6;
    public static final int BOT_MULTIPLIER = 5;
    public static final int WALL_MULTIPLIER = -2;
    public static final int BARRIER_MULTIPLIER = 1;
    public static final int STEP_MULTIPLIER = 10;
    protected BitSet board; // bit only represents whether a position has wall or not
    protected MazeState start_state;
    protected MazeState solution;
    protected boolean solved;
    //protected int score;

    // for new fitness function
    public int nStatesGenerated;

    // for search tree analysis
    protected int[] repeated;
    protected int[][] times_balls_repeat_position; // row[0] is of blue, row[1] is red, row[2] is green, row[3] is yellow
    protected int counter = 0;

    public MazeRepresentation() {
        board = new BitSet(SIZE);
        solution = null;
        solved = false;
        times_balls_repeat_position = new int[4][SIZE]; // 4 balls, each ball can be in SIZE positions
    }
    
    public MazeRepresentation(BitSet maze) {
        board = (BitSet) maze.clone();
        solution = null;
        solved = false;
        times_balls_repeat_position = new int[4][SIZE]; // 4 balls, each ball can be in SIZE positions
    }

    public MazeState getSolution() {
        return solution;
    }

    public void clearTerminals() {
        //corners should really be empty
        for (int i = 0; i < MazeState.R_START.length; i++) {
            board.clear(MazeState.R_START[i]);
            board.clear(MazeState.R_DEST[i]);
        }
    }

    public void randomize(double probability) {
        for (int i = 0; i < SIZE; i++) {
            if (TheRandom.getInstance().nextDouble() < probability) {
                board.set(i);
            }
        }
        clearTerminals();
    }

    public boolean outOfBounds(int x, int y, int z) {
        if (x < 0 || y < 0 || z < 0 || x >= X_MAX || y >= Y_MAX || z >= Z_MAX) {
            return true;
        }
        return false;
    }

    public boolean positionOpen(int x, int y, int z) {
        if (x < 0 || y < 0 || z < 0 || x >= X_MAX || y >= Y_MAX || z >= Z_MAX) {
            return false;
        }

        int index = positionToIndex(x, y, z);

        return board.get(index);
    }

    public void setPosition(int x, int y, int z, boolean b) {
        if (x < 0 || y < 0 || z < 0 || x >= X_MAX || y >= Y_MAX || z >= Z_MAX) {
            return;
        }

        int index = positionToIndex(x, y, z);
        if (b) {
            board.set(index);
        } else {
            board.clear(index);
        }
    }

    public int positionToIndex(int x, int y, int z) {
        int b = 0;
        b = (x << X_SHIFT);
        b |= (y << Y_SHIFT);
        b |= (z << Z_SHIFT);
        return b;
    }

    public int getX(int position) {
        return (position >> X_SHIFT) & MASK;
    }

    public int getY(int position) {
        return (position >> Y_SHIFT) & MASK;
    }

    public int getZ(int position) {
        return (position >> Z_SHIFT) & MASK;
    }

    public boolean hasBarrier(int position) {
        return board.get(position);
    }

    public boolean hasBarrier(int x, int y, int z) {
        return board.get(positionToIndex(x, y, z));
    }

    public String indexToString(int index) {
        Formatter f = new Formatter();
        f.format("(%d, %d, %d)", getX(index), getY(index), getZ(index));
        return f.out().toString();
    }

    public String toStringDots() {
        Formatter f = new Formatter();

        for (int y = 0; y < Y_MAX; y++) {
            for (int z = 0; z < Z_MAX; z++) {
                for (int x = 0; x < X_MAX; x++) {
                    if (board.get(positionToIndex(x, y, z))) {
                        f.format("X");
                    } else {
                        f.format(".");
                    }
                }
                f.format("\t");
            }
            f.format("\n");
        }

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

        for (int x = 0; x < X_MAX; x++) {
            f.format("[");
            for (int y = 0; y < Y_MAX; y++) {
                f.format("[");
                for (int z = 0; z < Z_MAX; z++) {
                    if (board.get(positionToIndex(x, y, z))) {
                        f.format("1");
                    } else {
                        f.format("0");
                    }
                    f.format((z != Z_MAX-1)?",":"");
                }
                f.format("]");
                f.format((y != Y_MAX-1)?",":"");
                f.format(" ");
            }
            f.format("]");
            f.format((x != X_MAX-1)?",":"");
            f.format("\n");
        }

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
        //System.err.println("nStatesGeneratedBeginning: " + getnStatesGenerated());
        if(!solved){
            BitSet used = new BitSet(MazeState.TOTAL_STATES);
            repeated = new int[MazeState.TOTAL_STATES];

            MazeState state = new MazeState(this);
            start_state = state;

            PriorityQueue<MazeState> queue = new PriorityQueue<MazeState>();
            queue.add(state);

            boolean done = false;
            while (!done) {
                state = queue.poll();
                // TODO update the number of times balls have been in a specific position
                byte[] robots = state.robots.clone();
                for (int i = 0; i < robots.length; ++i) {
                    /*
                    int x = getX(robots[i]);
                    int y = getY(robots[i]);
                    int z = getZ(robots[i]);
                    */
                    //int index = positionToIndex(x,y,z);
                    int index = robots[i]; // realized that index is the same as the position of the robot
                    times_balls_repeat_position[i][index]++;
                }

                //System.out.println(state);
                if (state == null || state.isGoalState()) {
                    done = true;
                } else {
                    state.next(queue, used);
                }
                counter++;
            }
            solution = state;

            //calculate score
            if (solution == null) {
                fitness = Integer.MIN_VALUE/2;
            }
            else {
                fitness = getnStatesGenerated();
            }
            //fitness = score();
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
       return SIZE;
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

    public void setnStatesGenerated(int n) {
        nStatesGenerated = n;
    }

    public int getnStatesGenerated() {
        return nStatesGenerated;
    }

    public MazeState getStartState() {
        return start_state;
    }

}
