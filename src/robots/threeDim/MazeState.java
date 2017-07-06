package robots.threeDim;
//package edu.gettysburg.cs.careen;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Formatter;
import java.util.PriorityQueue;

public class MazeState implements Comparable<MazeState> {

    public static int nStates;

    public static final byte[] R_START = {0, 60, 15, 51};  //start positions, order of balls: blue, red, green, yellow
    public static final byte[] R_DEST = {42, 22, 37, 25};  //end positions
                                        //{63, 3, 48, 12}; 
    
    //there are six directions to go
    public static final int[][] DIRECTIONS = {
        {1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}
    };
    
    //indicate the opposite direction for each direction
    // e.g. The direction at index 0 {1, 0, 0} has its opposite at index 1, {-1, 0, 0}
    //      and vice versa
    public static final int[] OPPOSITE_DIR_IND = {1, 0, 3, 2, 5, 4};
    
    //indices of x, y, z direction into the direction vector
    public static final int DIR_X = 0;
    public static final int DIR_Y = 1;
    public static final int DIR_Z = 2;
    
    // total possible states the robots can be in
    // note some are illegal (e.g.  where two or more robots are in the same location)
    public static final int TOTAL_STATES = 0xFFFFFF;  //2^24
    public static final int INIT_H = 12;
    public static final int MAX_STEPS = 20;
    //public static boolean[] used = new boolean[TOTAL_STATES];
    public static final int DIR_MOVE_BITS = 3;  //move 3 bits at a time
    public static final int DIR_MOVE_MASK = 7;  //mask 111 (what the heck is this for?)
    
    //instance variables
    // search tree info
    protected MazeState parent;
    protected ArrayList<MazeState> children;

    //representation is where the obstacles are
    protected MazeRepresentation maze;
    protected int steps;
    
    //default heuristic value
    protected int h;
    
    //the position of each robot is represented by a byte (actually 6 bit)
    // indicating x, y, z location
    // position (1, 0, 3) in binary is (01, 00, 11)
    // which is given by the 6 bits 010011 (note z bits are the least significant)
    // The translation from binary is handled in MazRepresentation
    protected byte[] robots;
    
    //robot moves required to get to this maze state
    protected ArrayList<Integer> moves;
    
    //some statistics to calculate score
    protected int hitWalls;
    protected int hitBots;
    protected int hitBarrier;


    public MazeState(MazeRepresentation maze) {
        this.maze = maze;
        hitWalls = 0;
        hitBots = 0;
        hitBarrier = 0;
        steps = 0;
        robots = R_START.clone();
        h = INIT_H;  //getHeuristic();//INIT_H;
        moves = new ArrayList<Integer>();
        parent = null;
        children = new ArrayList<MazeState>();
    }

    public MazeState(MazeState copy, MazeState parent, int robotIdx, int dir) {
        this.maze = copy.maze;
        this.hitWalls = copy.hitWalls;
        this.hitBots = copy.hitBots;
        this.hitBarrier = copy.hitBarrier;
        this.steps = copy.steps + 1;
        robots = copy.robots.clone();
        moves = (ArrayList<Integer>) copy.moves.clone();
        //modify robots
        moveRobot(robotIdx, dir);
        //recalculate h
        h = getHeuristic();

        this.parent = parent;
        children = new ArrayList<MazeState>();
    }

    //expand this state, put the results in the priority queue
    public void next(PriorityQueue<MazeState> queue, BitSet used) {
        //a new state for each robot, for each direction it can move
        for (int r = 0; r < robots.length; r++) {
            for (int d = 0; d < DIRECTIONS.length; d++) {
                MazeState next = new MazeState(this, this, r, d); // this child knew who its parent is
                int stateID = next.getStateID();
                //not a repeated state
                if (!used.get(stateID)) {
                    used.set(stateID);
                    queue.add(next);
                    maze.setnStatesGenerated(maze.getnStatesGenerated()+1);
                    // add the new state to the list of children of the current state
                    addChild(next);
                }
                else { // check the tendency of going to loop
                    maze.repeated[next.getStateID()]++;
                }
            }
        }
    }

    //the positions of the four robots uniquely identify the state.
    public int getStateID() {
        int result = 0;
        for (int i = 0; i < robots.length; i++) {
            result = result << MazeRepresentation.POS_BITS;
            result |= robots[i];
        }
        return result;
    }

    @Override
    public int compareTo(MazeState other) {
        return (steps + h) - (other.steps + other.h);
    }

    public int getHeuristic() {
        int h_new = 0;
        int mask = MazeRepresentation.MASK;
        for (int j = 0; j < 3; j++) {  //for each direction
            for (int i = 0; i < robots.length; i++) { //for each robot
            	// count how many coordinates are wrong
                if (((robots[i] ^ R_DEST[i]) & mask) != 0) {
                    h_new++;
                }
            }
            mask <<= 2;
        }
        return h_new;
    }

    public void moveRobot(int robotIdx, int dir) {
        int x = maze.getX(robots[robotIdx]);
        int y = maze.getY(robots[robotIdx]);
        int z = maze.getZ(robots[robotIdx]);

        //if(maze.outOfBounds(x, y, z)){
        //	return;
        //}

        boolean done = false;
        while (!done) {
            //check next spot
            x += DIRECTIONS[dir][DIR_X];
            y += DIRECTIONS[dir][DIR_Y];
            z += DIRECTIONS[dir][DIR_Z];

            //in bounds
            if (maze.outOfBounds(x, y, z)) {
                //don't move the robot
                this.hitWalls++;
                done = true;
            } else if (maze.hasBarrier(x, y, z)) {
                //hit a barrier
                this.hitBarrier++;
                done = true;
            } else {
            	//check if we hit another robot
                for (int r = 0; r < robots.length; r++) {
                	//for every other robot
                    if (r != robotIdx) {
                        int pos = maze.positionToIndex(x, y, z);
                        if (robots[r] == pos) {
                            done = true;
                            this.hitBots++;
                        }
                    }
                }
            }
        }

        //back up one spot (off of collision)
        x -= DIRECTIONS[dir][DIR_X];
        y -= DIRECTIONS[dir][DIR_Y];
        z -= DIRECTIONS[dir][DIR_Z];

        //move the robot there
        robots[robotIdx] = (byte) maze.positionToIndex(x, y, z);
        moves.add((robotIdx << DIR_MOVE_BITS) | dir);
    }

    public boolean isGoalState() {
        boolean result = true;
        for (int r = 0; r < robots.length; r++) {
            result = result && (robots[r] == R_DEST[r]);
        }
        return result;
    }

    public String toString() {
        Formatter f = new Formatter();
        f.format("ID=%6H\t", getStateID());
        for (int i = 0; i < robots.length; i++) {
            f.format(" R%d %s\t", i, maze.indexToString(robots[i]));
        }
        f.format("%d + %d=%d\n", steps, h, (steps + h));

        for (int m : moves) {
            int r = (m >> DIR_MOVE_BITS);
            int d = m & DIR_MOVE_MASK;
            f.format(" [%d,%d],", r, d);
        }

        return f.out().toString();
    }

    //count the number of times a robot moves one direction and its next move is
    //the opposite direction. This will be used to score the solution.
    public int countSolutionReversals() {
        int result = 0;

        //for each move
        for (int i = 0; i < moves.size(); i++) {
            int m1 = moves.get(i);
            int r1 = (m1 >> DIR_MOVE_BITS);
            int d1 = m1 & DIR_MOVE_MASK;
            int j = i + 1;
            boolean done = false;
            //check whether this robot's next move is in the opposite direction
            while (!done) {
                if (j >= moves.size()) {
                    done = true;
                } else {
                    int m2 = moves.get(j);
                    int r2 = (m2 >> DIR_MOVE_BITS);
                    if (r1 == r2) {
                        done = true;
                        int d2 = m2 & DIR_MOVE_MASK;
                        if (d2 == OPPOSITE_DIR_IND[d1]) {
                            result++;
                        }
                    }
                    j++;
                }
            }


        }

        return result;
    }

	public int getSteps() {
		return steps;
	}

	public int getHitWalls() {
		return hitWalls;
	}

	public int getHitBots() {
		return hitBots;
	}

	public int getHitBarrier() {
		return hitBarrier;
	}

	public void addChild(MazeState s) {
        children.add(s);
    }

    public MazeState getParent() {return parent;}

    public MazeState getChild(int index) {
        return children.get(index);
    }

    public ArrayList<MazeState> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<MazeState> c) {
        children = (ArrayList<MazeState>) c.clone();
    }
}
