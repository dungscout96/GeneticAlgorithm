package robots.twoDim;
import java.util.BitSet;

public class SolutionUtil2D {

	public static void main(String[] args) {
		//Given a maze as a string, convert it to a bit array, solve it and output some information
		//String maze = "[[0,1,0,0], [0,0,0,0], [0,0,0,1], [1,0,0,0] ]";
		//String maze = "[[0,0,0,1], [1,0,0,0], [0,0,0,0], [0,0,1,0] ]";
		//String maze = "[[0,1,0,1],[0,0,0,1],[0,0,0,0],[0,1,0,0] ]";

		//5x5 different size maze
    	MazeRepresentation2D.MAZE_DESC = new MazeDescriptor2D(5, 5,
    			new byte[]{0, 24}, new byte[]{18, 6});
    	//String maze = "[[0,1,0,0,0],[0,0,0,0,0],[0,0,0,0,0],[0,0,0,0,1],[1,0,0,0,0]]";
    	//String maze = "[[0, 0, 1, 1, 0 ],[1, 0, 0, 0, 1 ],[0, 0, 0, 0, 0 ],[0, 0, 1, 0, 0 ],[1, 1, 0, 0, 0 ]]";
		
    	//18984
    	//String maze = "0 0 0 0 0 0 0 0 0 0 1 0 0 1 0 1 0 0 0 1 0 1 0 0 0";
    	
    	//4215814
    	String maze = "0 0 1 0 0 0 0 0 0 0 1 0 1 0 1 0 0 0 0 0 0 0 1 1 0";
    	
    	BitSet bs = convert(maze);
		
		
		MazeRepresentation2D mr = new MazeRepresentation2D(bs);

		System.out.printf("\nMaze:\n%s\n", mr.toStringJSArray());
		
		printPositionGrid(mr);
		printGridList(mr);
		
		
		if(!mr.solve()){
			System.out.println("No solution");
			return;
		}
		
		MazeState2D sol = mr.getSolution();
		
		System.out.printf("%15s: %10d\n", "Fitness", mr.getScore());
		System.out.printf("%15s: %10d\n", "Steps", sol.getSteps());
		System.out.printf("%15s: %10d\n", "Hit Walls", sol.getHitWalls());
		System.out.printf("%15s: %10d\n", "Hit Barriers", sol.getHitBarrier());
		System.out.printf("%15s: %10d\n", "Hit Robots", sol.getHitBots());
		
		//print it and its solution
		System.out.printf("\n\n%s\n\n", mr);
		
	}
	
	public static BitSet convert(String puzzle){
		

		//System.out.println(puzzle);
		
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
		BitSet bs = new BitSet(MazeRepresentation2D.MAZE_DESC.size);
		
		int index = 0;
		for(int i = 0; i < digits.length; i++){

			if(!digits[i].isEmpty()){
				if(digits[i].equals("1")){
					bs.set(index);
				}
				index++;
			}
		}

		System.out.println(bs);
		
		return bs;
		
	}
	
	public static void printPositionGrid(MazeRepresentation2D mr){

		for(int y = 0; y < MazeRepresentation2D.MAZE_DESC.y_max; y++){
			for(int x = 0; x < MazeRepresentation2D.MAZE_DESC.x_max; x++){
				System.out.printf("%5d", mr.positionToIndex(x, y));
			}
			System.out.println();
		}
	}

	public static void printGridList(MazeRepresentation2D mr){
		for(int i = 0; i < MazeRepresentation2D.MAZE_DESC.size; i++){
			System.out.printf("%4d: (%2d, %2d)\n", i, mr.getX(i), mr.getY(i));
		}
	}
}
