package robots.twoDim;
import java.util.Arrays;

public class MazeDescriptor2D {

	//user defined values
	public final int x_max;
	public final int y_max;
	public final byte[] r_start;
	public final byte[] r_dest;
	
	//calculated values (representation)
	public final int size;
	public final int x_shift;
	public final int y_shift;
	public final int x_mask;
	public final int y_mask;
	public final int pos_bits;
	
	//calculated values (state)
	public final int total_states;
	public final int init_h;
	
	public MazeDescriptor2D(int cols, int rows, byte[] start, byte[] dest){
		x_max = cols;
		y_max = rows;
		size = x_max*y_max;
		double div = Math.log10(2);
		//number of bits required for x and y components
		int x_width = (int)(Math.ceil(Math.log10(x_max)/div));
		int y_width = (int)(Math.ceil(Math.log10(y_max)/div));
		
		//if you store x and y together in an int how far must you shift
		// to get each value. Store y in the lower order bits,
		// so x must shift over the number of bits in y
		x_shift = y_width;
		y_shift = 0; 
		
		//number of bits given by the width all filled with 1
		x_mask = ~(0xFFFFFFFF << x_width);
		y_mask = ~(0xFFFFFFFF << y_width);
		
		//number of bits required to determine a robot's position
		pos_bits = x_width + y_width;
		
		r_start = Arrays.copyOf(start, start.length);
		r_dest = Arrays.copyOf(dest, dest.length);
		
		//number of states is the size of the board raised to the number of robots
		total_states = (int)Math.pow(size, r_start.length);
		
		//init heuristic for A*. Each robot can be off by at most 2 dimensions
		init_h = 2*r_start.length;
		
	}
	
	public String toString(){
		StringBuilder str = new StringBuilder();
		str.append(String.format("Size: (%d, %d) => %d\n", x_max, y_max, size));
		str.append(String.format("Shift amount: (%d, %d)\n", x_shift, y_shift));
		str.append(String.format("Masks: (%h, %h)\n", x_mask, y_mask));
		str.append(String.format("Position bits: %d\n", pos_bits));
		str.append(String.format("Start postions: %s\n", Arrays.toString(r_start)));
		str.append(String.format("End postions: %s\n", Arrays.toString(r_dest)));
		str.append(String.format("Total states: %d\n", total_states));
		str.append(String.format("Init Heuristic: %d\n", init_h));
		
		return str.toString();
	}
	
	//test code
	public static void main(String[] args){
		MazeDescriptor2D md1 = new MazeDescriptor2D(5, 5, new byte[]{0, 24}, new byte[]{18, 6});
		
		System.out.println(md1);
	}
	
}
