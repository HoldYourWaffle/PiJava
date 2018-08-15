import jimbo.pijava.blinkt.BlinktController;
import java.awt.Color;

public class BlinktExample {
	
	public static void main(String[] args) throws InterruptedException {
		System.out.println("Starting demo"); //the VM can take some time to load on the pi so we print a nice little heads up that we're starting
		
		BlinktController blinkt = new BlinktController(); //create the controller
		blinkt.setBrightness(.1F); //we don't want to blind ourselves
		
		boolean movingForward = true;
		int i = 1;
		
		while (true) {
			blinkt.clear(); //clear any previous state
			
			blinkt.set(i-1, Color.RED); //set the left pixel to red
			blinkt.set(i, 0, 255, 0); //set the middle pixel to green
			blinkt.set(i+1, Color.BLUE, 1); //set the  last pixel to blue with full brightness
			
			blinkt.push(); //push state to the GPIO pins
			
			//Some back and forth controlling
			if (movingForward) i++;
			else i--;
			
			if (i >= 7) {
				i = 5;
				movingForward = false;
			} else if (i <= 0) {
				i = 2;
				movingForward = true;
			}
			
			Thread.sleep(250);
		}
	}
	
}
