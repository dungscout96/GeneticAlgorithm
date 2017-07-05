package robots.twoDim;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Display2DMazeInfo extends JPanel{
	
	public static final int SCALE = 4;
	public static final int SIDE = 256;
	public static final int IMG_OFFSET = 10;
	public static final int TEXT_X_OFFSET = 10;
	public static final int TEXT_Y_OFFSET = SCALE*SIDE + 25;
	public static final int SIZE = SCALE*SIDE + 75;
	
	private BufferedImage img;
	private String label;
	private ArrayList<PuzzInfo> infoArr;
	public Display2DMazeInfo(){
		this.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		img = new BufferedImage(SCALE*SIDE, SCALE*SIDE, BufferedImage.TYPE_INT_ARGB);
		label = "";
		infoArr = new ArrayList<PuzzInfo>(0xFFFF);
		try {
			Scanner fileIn  = new Scanner(new File("all2D.csv"));
			PuzzInfo unfit = new PuzzInfo();
			for(int i = 0; i < 0xFFFF; i++){
				String line = fileIn.nextLine();
				String[] parts = line.split(",");
				
				//
				// 0: index
				// 1: fitness
				// 2: number of steps
				// 3: number of walls
				// 4: number of barriers
				// 5: number of bots
				int item = 1;
				int x = SCALE*(i % SIDE);
				int y = SCALE*(i / SIDE);
				int color = 0xFFFFFFFF; //white
				PuzzInfo current = unfit;
				if(parts.length > 1){
					current = new PuzzInfo();
					current.fitness = Integer.parseInt(parts[1]);
					current.steps = Integer.parseInt(parts[2]);
					current.walls = Integer.parseInt(parts[3]);
					current.barrs = Integer.parseInt(parts[4]);
					current.bots = Integer.parseInt(parts[5]);
				}
				infoArr.add(current);
				for(int k = 0; k < SCALE; k++){
					for(int j = 0; j < SCALE; j++){
						img.setRGB(x+k, y+j, current.getColor());
					}
				}
				
			}//end for
			fileIn.close();
			System.out.println("Done reading data.");
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		addMouseMotionListener(new MouseMotionAdapter(){
			public void mouseMoved(MouseEvent e){
				int x = (e.getX()-IMG_OFFSET);
				int y = (e.getY()-IMG_OFFSET);
				if(x >= 0 && x < img.getWidth() && y >= 0 && y < img.getHeight()){
					x /= SCALE;
					y /= SCALE;
					int index = (y << 8) + x;
					PuzzInfo pi = infoArr.get(index);
					label = String.format("%d (%d, %d) -> %s", index, x, y, pi);
				}
				else {
					label = "";
				}
				repaint();
			}
		});
		
	}
	
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		g.drawImage(img, IMG_OFFSET, IMG_OFFSET, this);
		g.drawString(label, TEXT_X_OFFSET, TEXT_Y_OFFSET);
	}
	
	class PuzzInfo {
		public int fitness;
		public int steps;
		public int walls;
		public int bots;
		public int barrs;
		
		public String toString(){
			return String.format("%d, %d, %d, %d, %d", fitness, steps, walls, barrs, bots);
		}
		
		public int getColor(){
			if(fitness == 185){
				return 0xFFFF0000; //red
			}
			else if(fitness > 0){
				int value = 256-fitness;
				return 0xFF000000 + (value << 16) + (value << 8) + (value);
			}
			else {
				return 0xFFFFFFFF; //white
			}
		}
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("2D Maze info");
		frame.setSize(SIZE, SIZE);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(new Display2DMazeInfo());
		frame.setVisible(true);

	}

}
