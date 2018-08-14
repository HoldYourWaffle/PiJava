[![GPLv3 license](https://img.shields.io/badge/License-GPLv3-blue.svg)](http://perso.crans.org/besson/LICENSE.html)

# blinkt4j
This is a java api for controlling the [Pimoroni Blinkt](https://shop.pimoroni.com/products/blinkt) LED-strip on the raspberry pi. It is a derivative of [Jimbo's](https://github.com/hackerjimbo/PiJava) java code for the pi, but stripped and tweaked specially for the blinkt, as well as some additional convenience methods.



## Usage
To use this you need to have at least java 8 (for [pi4j](https://github.com/Pi4J/pi4j)) as well as the `wiringpi` package installed on your Raspberry Pi.

### Gradle
To include this api in your gradle project use [jitpack](https://jitpack.io/):
```gradle
repositories {
	maven { url 'https://jitpack.io' }
}

dependencies {
	compile 'com.github.HoldYourWaffle:blinkt4j:v1.0'
}
```

## Example
Here's an example that illustrates the basic usage of this api:
```java
import jimbo.pijava.blinkt.BlinktController;

public class BlinktTest {
	
	public static void main(String[] args) throws InterruptedException {
		BlinktController blinkt = new BlinktController(); //create the controller
		blinkt.setBrightness(.1F); //we don't want to blind ourselves
		
		int i = 0;
		while (true) {
			blinkt.clear(); //clear any previous state
			blinkt.set(i, 0, 255, 0); //set LED number 'i' to green
			blinkt.push(); //push the new state to the blinkt
			
			i++;
			if (i > 7) i = 0;
			
			Thread.sleep(500);
		}
	}
	
}
```

For futher information you can read the included javadocs.

If you get permission denied errors either run your program with `sudo` or add the user to the `gpio` group with: `sudo usermod -a -G gpio username` (the default user is `pi`)
