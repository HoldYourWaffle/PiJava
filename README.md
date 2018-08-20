[![GPLv3 license](https://img.shields.io/badge/License-GPLv3-blue.svg)](http://perso.crans.org/besson/LICENSE.html)

# blinkt4j
This is a java api for controlling the [Pimoroni Blinkt](https://shop.pimoroni.com/products/blinkt) LED-strip on the raspberry pi. It is a derivative of [Jimbo's](https://github.com/hackerjimbo/PiJava) java code for the pi, but stripped and tweaked specially for the blinkt, as well as some additional convenience methods.



## Usage
To use this you need to have at least Java 8 (for [pi4j](https://github.com/Pi4J/pi4j)) as well as the `wiringpi` package installed on your Raspberry Pi.

### Gradle
To include this api in your gradle project use [jitpack](https://jitpack.io/):
```gradle
repositories {
	maven { url 'https://jitpack.io' }
	maven { url 'https://oss.sonatype.org/content/groups/public' } //pi4j repository
}

dependencies {
	compile 'com.github.HoldYourWaffle:blinkt4j:v1.3'
}
```

## Example
Here's an example that illustrates the basic usage of this api:
```java
import java.awt.Color;
import jimbo.pijava.blinkt.BlinktController;

public class BlinktExample {
	
	public static void main(String[] args) throws InterruptedException {
		System.out.println("Starting demo"); //the VM can take some time to load on the pi so we print a nice little heads up that we're starting
		
		BlinktController blinkt = new BlinktController(); //create te controller
		blinkt.setBrightness(.1F); //we don't want to blind ourselves
		
		boolean movingForward = true;
		int i = 1;
		
		while (true) {
			blinkt.clear(); //clear any previous state
			
			blinkt.set(i-1, Color.RED); //set the left pixel to red
			blinkt.set(i, 0, 255, 0); //set the middle pixel to green
			blinkt.set(i+1, Color.BLUE, 1); //set the  last pixel to blue at full brightness
			
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
```

For futher information read the included javadocs.

If you get permission denied errors either run your program with `sudo` or add the user to the `gpio` group with: `sudo usermod -a -G gpio username` (the default user is `pi`)
