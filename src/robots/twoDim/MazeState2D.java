package robots.twoDim;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Formatter;
import java.util.PriorityQueue;

public class MazeState2D implements Comparable<MazeState2D> {

	/*
	 * 			0	1	2	3
	 * 			4	5	6	7
	 * 			8	9	10	11
	 * 			12	13	14	15
	 */
	
	//Replace constants
	/*
	//start locations
	public static final byte[] R_START = {0, 15};
	//finish location
	public static final byte[] R_DEST = {10, 5};
	
    // total possible states the robots can be in
    // note some are illegal (e.g.  where two or more robots are in the same location)
    // each robot is in one of 16 different locations (2^4)
    // there are four of them: 2^4 * 2^4 = 2^8
    public static final int TOTAL_STATES = 0xFF;  //2^8
    public static final int INIT_H = 4;
    */
    
	//four possible direction to move
	public static final int[][] DIRECTIONS = {
			{1, 0}, {-1, 0}, {0, 1}, {0, -1}
	};
	
	//indices of the reverse directions in DIRECTIONS
	public static final int[] OPPOSITE_DIR_IND = {1, 0, 3, 2};
	

    //indices of x, y direction into the direction vector
    public static final int DIR_X = 0;
    public static final int DIR_Y = 1;
    

    
    //don't know what this is for. It does not appear to be used anywhere
    public static final int MAX_STEPS = 20;
    
    //public static boolean[] used = new boolean[TOTAL_STATES];
    public static final int DIR_MOVE_BITS = 2;  //direction represented by 2 bits
    public static final int DIR_MOVE_MASK = 3;  //mask 11 (to get the 2 bit direction)
    
    //instance variables
    //representation is where the obstacles are
    protected MazeRepresentation2D maze;
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
    
    //some statistics
    protected int hitWalls;
    protected int hitBots;
    protected int hitBarrier;
    
    public MazeState2D(MazeRepresentation2D maze){
        this.maze = maze;
        hitWalls = 0;
        hitBots = 0;
        hitBarrier = 0;
        steps = 0;
        robots = MazeRepresentation2D.MAZE_DESC.r_start.clone();
        h = MazeRepresentation2D.MAZE_DESC.init_h;  //getHeuristic();//INIT_H;
        moves = new ArrayList<Integer>();
 
    }
    

    
    public MazeState2D(MazeState2D copy, int robotIdx, int dir) {
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
    }
    


    //expand this state, put the results in the priority queue
    public void next(PriorityQueue<MazeState2D> queue, BitSet used) {
        //a new state for each robot, for each direction it can move
        for (int r = 0; r < robots.length; r++) {
            for (int d = 0; d < DIRECTIONS.length; d++) {
                MazeState2D next = new MazeState2D(this, r, d);
                //not a repeated state
                if (!used.get(next.getStateID())) {
                    used.set(next.getStateID());
                    queue.add(next);
                }
            }
        }
    }


    //the positions of the two robots uniquely identify the state.
    public int getStateID() {
        int result = 0;
        for (int i = 0; i < robots.length; i++) {
            result = result << MazeRepresentation2D.MAZE_DESC.pos_bits;
            result |= robots[i];
        }
        return result;
    }

    @Override
    public int compareTo(MazeState2D other) {
        return (steps + h) - (other.steps + other.h);
    }

    public int getHeuristic() {
        int h_new = 0;
        int mask = MazeRepresentation2D.MAZE_DESC.y_mask;
        for (int j = 0; j < 2; j++) {  //for each direction
            for (int i = 0; i < robots.length; i++) { //for each robot
            	// count how many coordinates are wrong
                if (((robots[i] ^ MazeRepresentation2D.MAZE_DESC.r_dest[i]) & mask) != 0) {
                    h_new++;
                }
            }
            mask = MazeRepresentation2D.MAZE_DESC.x_mask 
            		<< MazeRepresentation2D.MAZE_DESC.x_shift;//<<= 2;
        }
        return h_new;
    }
    
    public void moveRobot(int robotIdx, int dir) {
        int x = maze.getX(robots[robotIdx]);
        int y = maze.getY(robots[robotIdx]);

        //if(maze.outOfBounds(x, y, z)){
        //	return;
        //}

        boolean done = false;
        while (!done) {
            //check next spot
            x += DIRECTIONS[dir][DIR_X];
            y += DIRECTIONS[dir][DIR_Y];

            //in bounds
            if (maze.outOfBounds(x, y)) {
                //don't move the robot
                this.hitWalls++;
                done = true;
            } else if (maze.hasBarrier(x, y)) {
                //hit a barrier
                this.hitBarrier++;
                done = true;
            } else {
            	//check if we hit another robot
                for (int r = 0; r < robots.length; r++) {
                	//for every other robot
                    if (r != robotIdx) {
                        int pos = maze.positionToIndex(x, y);
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

        //move the robot there
        robots[robotIdx] = (byte) maze.positionToIndex(x, y);
        moves.add((robotIdx << DIR_MOVE_BITS) | dir);
    }

    public boolean isGoalState() {
        boolean result = true;
        for (int r = 0; r < robots.length; r++) {
            result = result && (robots[r] == MazeRepresentation2D.MAZE_DESC.r_dest[r]);
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
    
    public String toGridString(){
        Formatter f = new Formatter();
        
        
        
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
	
}
