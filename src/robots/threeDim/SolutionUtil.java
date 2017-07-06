package robots.threeDim;
import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Scanner;

public class SolutionUtil {

	public static void main(String[] args) {
		//Given a maze as a string, convert it to a bit array, solve it and output some information
/*
		String maze = "[[0,0,0,0], [1,0,1,1], [0,0,0,0], [0,0,0,0] ],[[1,0,0,0], [0,0,0,0], [0,0,0,1], [1,0,0,0] ],[[0,0,1,0], [0,0,1,1], [0,0,0,0], [0,0,1,0] ],[[0,0,0,0], [1,0,0,0], [0,0,0,0], [0,1,0,0] ]";

		Fitness:        397
        Steps:         35
        Hit Walls:         12
        Hit Barriers:         11
        Hit Robots:         12
*/

        //String maze = "[[0,0,1,1], [1,1,1,0], [0,1,0,0], [0,1,0,0] ],[[1,0,0,0], [1,1,0,1], [1,0,1,0], [0,0,0,1] ],[[1,0,1,1], [1,0,1,0], [1,1,0,0], [1,1,0,1] ],[[0,1,0,0], [0,0,0,0], [1,1,1,1], [0,0,0,1] ]";
        SolutionUtil su = new SolutionUtil();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(args[0]));
            BufferedWriter writer = new BufferedWriter(new FileWriter(args[1]));
            String maze;
            int i = 1;
            while ((maze = reader.readLine()) != null) {
                writer.write("Maze" + i++ + ": " + maze);
                writer.newLine();
                writer.newLine();

                BitSet bs = convert(maze);


                MazeRepresentation mr = new MazeRepresentation(bs);

                // solve the puzzle
                long t0 = Calendar.getInstance().getTimeInMillis();
                if(!mr.solve()){
                    System.out.println("No solution");
                    return;
                }
                long t1 = Calendar.getInstance().getTimeInMillis();

                MazeState start_state = mr.getStartState();
                MazeState sol = mr.getSolution();

                int total_repeated = 0;
                for (int index = 0; index < mr.repeated.length; ++index) {
                    total_repeated += mr.repeated[index];
                }

                int avg_dist_to_goal_of_root = su.getAverageDistanceToGoal(start_state, 0);

                String repeated = String.format("%s: %10d\n", "Number of times Total repeated", total_repeated);
                String fitness = String.format("%15s: %10d\n", "Fitness", mr.getScore());
                String steps = String.format("%15s: %10d\n", "Steps", sol.getSteps());
                String walls = String.format("%15s: %10d\n", "Hit Walls", sol.getHitWalls());
                String barriers = String.format("%15s: %10d\n", "Hit Barriers", sol.getHitBarrier());
                String bots = String.format("%15s: %10d\n", "Hit Robots", sol.getHitBots());

                //print it and its solution
                writer.write("Start state third child: " + start_state.getChildren().get(2).toString());
                writer.newLine();
                writer.write("Start state first child's parent should be start state: " + start_state.getChild(0).getParent());
                writer.newLine();
                writer.write("Start state first child step should be 1: " + start_state.getChild(0).getSteps());
                writer.newLine();
                writer.write("Average distance to goal of root: " + avg_dist_to_goal_of_root);
                writer.newLine();
                for (int index = 0; index < start_state.getChildren().size(); ++index) {
                    writer.write("Average distance to goal of child " + index + ": " + su.getAverageDistanceToGoal(start_state.getChild(index), 1));
                    writer.newLine();
                }
                writer.write("Solving time: " + (t1-t0));
                writer.newLine();
                writer.write(repeated);

                writer.newLine();
                writer.write("Number of times a ball repeats a position: \n");
                writer.write("Queue added: " + mr.counter + " times\n");
                writer.write("(Format: (x,y,z): number_of_times_ball_move_to_this_position)\n");
                writer.write(su.toStringRepeatMoveArray(mr.times_balls_repeat_position, mr));
                writer.newLine();

                writer.write(fitness);
                writer.write(steps);
                writer.write(walls);
                writer.write(barriers);
                writer.write(bots);
                writer.write("--------------------------------------------");
                writer.newLine();
                //System.out.printf("\n\n%s\n\n", mr);
            }
            reader.close();
            writer.close();
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println(e.getStackTrace());
            System.exit(1);
        }

	}
	
	public static BitSet convert(String puzzle){
		//puzzle = puzzle.replaceAll("[,]", "");
		puzzle = puzzle.replace('[', ' ');
		puzzle = puzzle.replace(']', ' ');
		puzzle = puzzle.replace(',', ' ');
		
		String[] digits = puzzle.split(" ");
	
	/*
		for(String s: digits){
			System.out.printf("<%s>", s);
		}
		System.out.println();
		*/
		BitSet bs = new BitSet(64);
		
		int index = 0;
		for(int i = 0; i < digits.length; i++){

			if(!digits[i].isEmpty()){
				if(digits[i].equals("1")){
					bs.set(index);
				}
				index++;
			}
		}

		//System.out.println(bs);
		
		return bs;
		
	}

	public String toStringRepeatMoveArray(int[][] repeatMoveArray, MazeRepresentation maze) {
        String str = "";
            for (int i = 0; i < 4; ++i) {
                str += String.format("Ball %d: \n", i);
                int[] repeatMoveArrayOfRobot = repeatMoveArray[i];
                for (int index = 0; index < repeatMoveArrayOfRobot.length; ++index) {
                    if (repeatMoveArrayOfRobot[index] > 0) {
                        int x = maze.getX(index);
                        int y = maze.getY(index);
                        int z = maze.getZ(index);
                        str += String.format("(%d,%d,%d): %d, ", x, y, z, repeatMoveArrayOfRobot[index]);
                    }
                }
                str += "\n";
            }
        return str;
    }

    private int getAverageDistanceToGoal(MazeState state, int dist) {
	    int sum_dist = getDistanceToGoal(state, dist)*10000000;
	    int num_child = state.getChildren().size() > 0 ? state.getChildren().size() : 1;
	    if (num_child > 10000) {
            System.out.println(num_child);
        }
        return sum_dist/num_child;
    }

    private int getDistanceToGoal(MazeState state, int dist) {
        if (!state.getChildren().isEmpty()) {
            int numChild = state.getChildren().size();
            int sum = 0;
            for (int i = 0; i < numChild; ++i) {
                sum += getDistanceToGoal(state.getChild(i), dist + 1);
            }
            //return sum / numChild;
            return sum;
        }
        else {
            if (state.isGoalState()) {
                return dist;
            }
            // the path that leads to death end should have value 0
            else {
                return 0;
            }
        }
    }
}
