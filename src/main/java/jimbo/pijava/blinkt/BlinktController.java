/*
 * Copyright (C) 2016-2017 Jim Darby.
 *
 * This software is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, If not, see
 * <http://www.gnu.org/licenses/>.
 */

package jimbo.pijava.blinkt;

import java.awt.Color;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;

/**
 * This is a derivative class of Jim Darby's APA102 & Blinkt controllers
 * 
 * @author Jim Darby
 * @author HoldYourWaffle
 */
public class BlinktController {
	/** The pin we use for the clock */
	private final GpioPinDigitalOutput clk;
	
	/** The pin we use for data */
	private final GpioPinDigitalOutput dat;
	
	/** The data for each LED in the chain */
	private final int[] data;
	
	/**
	 * Brightness (0 - 31) field used to set colors<br>
	 * <br>
	 * <b>Note that most methods ({@link #set(int, int, int, int, float) set}, {@link #setBrightness(float) setBrightness}, ...) accept a float value of 0-1,
	 * {@link #getBrightness() getBrightness} converts between these formats</b>
	 */
	private int brightness;
	
	/**
	 * Construct an Pimoroni Blinkt! controller
	 * 
	 * @param gpio The GpioController to use
	 * @param data_pin The data pin to use
	 * @param clock_pin The clock pin to use
	 */
	public BlinktController(GpioController gpio, Pin data_pin, Pin clock_pin) {
		dat = gpio.provisionDigitalOutputPin(data_pin);
        clk = gpio.provisionDigitalOutputPin(clock_pin);
		data = new int[8];
		
		reset();
		push();
	}
	
	/**
	 * Construct an Pimoroni Blinkt! controller with the default data and clock pin
	 * 
	 * @param gpio The GpioController to use
	 */
	public BlinktController(GpioController gpio) {
		this(gpio, RaspiPin.GPIO_04, RaspiPin.GPIO_05);
	}
	
	/** Construct a Pimoroni Blinkt! controller with the default {@link GpioController}, data and clock pin */
	public BlinktController() {
		this(GpioFactory.getInstance(), RaspiPin.GPIO_04, RaspiPin.GPIO_05);
	}
	
	
	
	/** Update the LED chain */
	public void push() {
		// Transmit preamble
		for (int i = 0; i < 4; ++i)
			write_byte((byte) 0);
		
		// Send data
		for (int i = 0; i < 8; ++i)
			write_led(data[i]);
		
		// And latch it
		latch();
	}
	
	
	/** Both {@link #clear()} and {@link #setBrightness(float)} to 0 */
	public void reset() {
		clear();
		setBrightness(1);
	}
	
	
	/** Clear the data for pixel n */
	public void clear(int n) {
		data[n] = 0;
	}
	
	
	/** Clear the data of all pixels */
	public void clear() {
		for (int i = 0; i < 8; ++i)
			data[i] = 0;
	}
	
	
	
	/**
	 * Set the brightness field used for {@link #set(int, int, int, int)}
	 * 
	 * @param brightness The brightness scale factor (0-1)
	 */
	public void setBrightness(float brightness) {
		if (brightness < 0 || brightness > 1) throw new IllegalArgumentException("Invalid brightness "+brightness);
		this.brightness = (int) (brightness * 31);
	}
	
	
	/** Get {@link #brightness} as a float in the range of 0-1 */
	public float getBrightness() {
		return brightness / 31F;
	}
	
	
	
	/**
	 * Set an LED to a specific red, green, blue and brightness value
	 * 
	 * @param n The LED index (0-7)
	 * @param red The red value (0-255)
	 * @param green The green value (0-255)
	 * @param blue The blue value (0-255)
	 * @param brightness The brightness (0-1)
	 * 
	 * @see #push()
	 */
	public void set(int n, int red, int green, int blue, float brightness) {
		if (n < 0 || n >= 8) throw new IllegalArgumentException("n must be larger than 0 and smaller than 8");
		if (red < 0 || red > 255) throw new IllegalArgumentException("red must be between 0 and 255");
		if (green < 0 || green > 255) throw new IllegalArgumentException("green must be between 0 and 255");
		if (blue < 0 || blue > 255) throw new IllegalArgumentException("blue must be between 0 and 255");
		
		data[n] = ((int)(brightness*31) << 24) | (red << 16) | (green << 8) | blue;
	}
	
	/**
	 * Set an LED to a specific red, green and blue value using the set {@link #setBrightness(float) default brightness}
	 * 
	 * @param n The LED index (0-7)
	 * @param red The red value (0-255)
	 * @param green The green value (0-255)
	 * @param blue The blue value (0-255)
	 * 
	 * @see #setBrightness(float)
	 * @see #push()
	 */
	public void set(int n, int red, int green, int blue) {
		set(n, red, green, blue, getBrightness());
	}
	
	/**
	 * Set an LED to the specified color
	 * 
	 * @param n The LED index (0-7)
	 * @param col The color
	 * @param brightness The brightness (0-1)
	 * 
	 * @see #push()
	 * @since 1.1
	 */
	public void set(int n, Color col, float brightness) {
		set(n, col.getRed(), col.getGreen(), col.getBlue(), brightness);
	}
	
	/**
	 * Set an LED to the specified color using the set {@link #setBrightness(float) default brightness}
	 * 
	 * @param n The LED index (0-7)
	 * @param col The color
	 * 
	 * @see #setBrightness(float)
	 * @see #push()
	 * @since 1.1
	 */
	public void set(int n, Color col) {
		set(n, col, getBrightness());
	}
	
	
	/**
	 * Set all LEDs to the specified color
	 * 
	 * @param red The red value (0-255)
	 * @param green The green value (0-255)
	 * @param blue The blue value (0-255)
	 * @param brightness The brightness (0-1)
	 * 
	 * @see #push()
	 * @since 1.2
	 */
	public void setAll(int red, int green, int blue, float brightness) {
		for (int i = 0; i <= 7; i++)
			set(i, red, green, blue, brightness);
	}
	
	/**
	 * Set all LEDs to the specified color using the set {@link #setBrightness(float) default brightness}
	 * 
	 * @param red The red value (0-255)
	 * @param green The green value (0-255)
	 * @param blue The blue value (0-255)
	 * 
	 * @see #setBrightness(float)
	 * @see #push()
	 * @since 1.2
	 */
	public void setAll(int red, int green, int blue) {
		setAll(red, green, blue, getBrightness());
	}
	
	/**
	 * Set all LEDs to the specified color
	 * 
	 * @param col The color
	 * @param brightness The brightness (0-1)
	 * 
	 * @see #push()
	 * @since 1.2
	 */
	public void setAll(Color col, float brightness) {
		setAll(col.getRed(), col.getGreen(), col.getBlue(), brightness);
	}
	
	/**
	 * Set all LEDs to the specified color using the set {@link #setBrightness(float) default brightness}
	 * 
	 * @param col The color
	 * 
	 * @see #setBrightness(float)
	 * @see #push()
	 * @since 1.2
	 */
	public void setAll(Color col) {
		setAll(col, getBrightness());
	}
	
	
	/**
	 * Set the Blinkt to a gradient going from <code>colStart</code> to <code>colEnd</code> with a brightness going from <code>brightnessStart</code> to <code>brightnessEnd</code>
	 * 
	 * @param colStart The color to start with
	 * @param colEnd The color to end with
	 * @param brightnessStart The brightness to start with (0-1)
	 * @param brightnessEnd The brightness to end with (0-1)
	 * 
	 * @see #push()
	 * @since 1.2
	 */
	public void setGradient(Color colStart, Color colEnd, float brightnessStart, float brightnessEnd) {
		float brightPerI = (brightnessEnd - brightnessStart) / 7F;
		for (int i = 0; i <= 7; i++)
			set(i, mixColors(colStart, colEnd, i/7D), brightnessStart + i*brightPerI);
	}
	
	/**
	 * Set the Blinkt to a gradient going from <code>colStart</code> to <code>colEnd</code> with a brightness of <code>brightness</code>
	 * 
	 * @param colStart The color to start with
	 * @param colEnd The color to end with
	 * @param brightness The brightness (0-1)
	 * 
	 * @see #push()
	 * @since 1.2
	 */
	public void setGradient(Color colStart, Color colEnd, float brightness) {
		setGradient(colStart, colEnd, brightness, brightness);
	}
	
	/**
	 * Set the Blinkt to a gradient with a constant <code>color</code> and a brightness going from <code>brightnessStart</code> to <code>brightnessEnd</code>
	 * 
	 * @param color The color of the gradient
	 * @param brightnessStart The brightness to start with (0-1)
	 * @param brightnessEnd The brightness to end with (0-1)
	 * 
	 * @see #push()
	 * @since 1.2
	 */
	public void setGradient(Color color, float brightnessStart, float brightnessEnd) {
		setGradient(color, color, brightnessStart, brightnessEnd);
	}
	
	/**
	 * Set the Blinkt to a gradient going from <code>colStart</code> to <code>colEnd</code> using the set {@link #setBrightness(float) default brightness}
	 * 
	 * @param colStart The color to start with
	 * @param colEnd The color to end with
	 * 
	 * @see #setBrightness(float)
	 * @see #push()
	 * @since 1.2
	 */
	public void setGradient(Color colStart, Color colEnd) {
		setGradient(colStart, colEnd, getBrightness());
	}
	
	
	/**
	 * Set the Blinkt to a gradient with the specified start & end color components and start & end brightness
	 * 
	 * @param redStart The red component to start with (0-255)
	 * @param greenStart The green component to start with (0-255)
	 * @param blueStart The blue component to start with (0-255)
	 * 
	 * @param redEnd The red component to end with (0-255)
	 * @param greenEnd The green component to end with (0-255)
	 * @param blueEnd The blue component to end with (0-255)
	 * 
	 * @param brightnessStart The brightness to start with (0-1)
	 * @param brightnessEnd The brightness to end with (0-1)
	 * 
	 * @see #push()
	 * @since 1.2
	 */
	public void setGradient(int redStart, int greenStart, int blueStart, int redEnd, int greenEnd, int blueEnd, float brightnessStart, float brightnessEnd) {
		setGradient(new Color(redStart, greenStart, blueStart), new Color(redEnd, greenEnd, blueEnd), brightnessStart, brightnessEnd);
	}
	
	/**
	 * Set the Blinkt to a gradient with the specified start & end color components and <code>brightness</code>
	 * 
	 * @param redStart The red component to start with (0-255)
	 * @param greenStart The green component to start with (0-255)
	 * @param blueStart The blue component to start with (0-255)
	 * 
	 * @param redEnd The red component to end with (0-255)
	 * @param greenEnd The green component to end with (0-255)
	 * @param blueEnd The blue component to end with (0-255)
	 * 
	 * @param brightness The brightness (0-1)
	 * 
	 * @see #push()
	 * @since 1.2
	 */
	public void setGradient(int redStart, int greenStart, int blueStart, int redEnd, int greenEnd, int blueEnd, float brightness) {
		setGradient(redStart, greenStart, blueStart, redEnd, greenEnd, blueEnd, brightness, brightness);
	}
	
	/**
	 * Set the Blinkt to a gradient with the specified (constant) color components and start & end brightness
	 * 
	 * @param red The red value (0-255)
	 * @param green The green value (0-255)
	 * @param blue The blue value (0-255)
	 * 
	 * @param brightnessStart The brightness to start with (0-1)
	 * @param brightnessEnd The brightness to end with (0-1)
	 * 
	 * @see #push()
	 * @since 1.2
	 */
	public void setGradient(int red, int green, int blue, float brightnessStart, float brightnessEnd) {
		setGradient(red, green, blue, red, green, blue, brightnessStart, brightnessEnd);
	}
	
	/**
	 * Set the Blinkt to a gradient with the specified start & end color components using the set {@link #setBrightness(float) default brightness}
	 * 
	 * @param redStart The red component to start with (0-255)
	 * @param greenStart The green component to start with (0-255)
	 * @param blueStart The blue component to start with (0-255)
	 * 
	 * @param redEnd The red component to end with (0-255)
	 * @param greenEnd The green component to end with (0-255)
	 * @param blueEnd The blue component to end with (0-255)
	 * 
	 * @see #push()
	 * @since 1.2
	 */
	public void setGradient(int redStart, int greenStart, int blueStart, int redEnd, int greenEnd, int blueEnd) {
		setGradient(redStart, greenStart, blueStart, redEnd, greenEnd, blueEnd, getBrightness());
	}
	
	
	public void setTo(int[] reds, int[] greens, int[] blues, float[] brightnesses) {
		if (!(reds.length == 8 && greens.length == 8 && blues.length == 8 && brightnesses.length == 8))
			throw new IllegalArgumentException("Invalid component arrays. All arrays must have a length of 8");
		
		for (int i=0; i <= 7; i++)
			set(i, reds[i], greens[i], blues[i], brightnesses[i]);
	}
	
	public void setTo(int[] reds, int[] greens, int[] blues, float brightness) {
		setTo(reds, greens, blues, new float[] { brightness, brightness, brightness, brightness, brightness, brightness, brightness });
	}
	
	public void setTo(int[] reds, int[] greens, int[] blues) {
		setTo(reds, greens, blues, brightness);
	}
	
	public void setTo(Color[] colors, float[] brightnesses) {
		if (colors.length != 8) throw new IllegalArgumentException("Color array should have a length of 8");
		if (brightnesses.length != 8) throw new IllegalArgumentException("Brightness array should have a length of 8");
		
		for (int i = 0; i <= 7; i++)
			set(i, colors[i], brightnesses[i]);
	}
	
	public void setTo(Color[] colors, float brightness) {
		setTo(colors, new float[] { brightness, brightness, brightness, brightness, brightness, brightness, brightness });
	}
	
	public void setTo(Color[] colors) {
		setTo(colors, brightness);
	}
	
	
	
	
	
	private Color mixColors(Color a, Color b, double percent) {
		double inverse_percent = 1.0 - percent;
		int redPart =	(int) (a.getRed()	*	percent + b.getRed()	*	inverse_percent);
		int greenPart =	(int) (a.getGreen()	*	percent + b.getGreen()	*	inverse_percent);
		int bluePart =	(int) (a.getBlue()	*	percent + b.getBlue()	*	inverse_percent);
		return new Color(redPart, greenPart, bluePart);
	}
	
	
	//================ INTERNAL STUFF ================//
	
	/** Write out a single byte. It goes out MSB first */
	private void write_byte(byte out) {
		for (int i = 7; i >= 0; --i) {
			dat.setState((out & (1 << i)) != 0);
			clk.setState(true);
			clk.setState(false);
		}
	}
	
	/** Write out a single LEDs information */
	private void write_led(int data) {
		write_byte((byte) (0xe0 | ((data >> 24) & 0x1f)));
		
		write_byte((byte) (data));
		write_byte((byte) (data >> 8));
		write_byte((byte) (data >> 16));
	}
	
	/**
	 * Latch the data into the devices. This has prompted much discussion as
	 * data sheet seems to be a work of fiction. These values seem to work.
	 * 
	 * In case of any difficulties, blame Gadgetoid: it's all his fault!
	 */
	private void latch() {
		// Transmit zeros not ones!
		dat.setState(false);
		
		// And 36 of them!
		for (int i = 0; i < 36; ++i) {
			clk.setState(true);
			clk.setState(false);
		}
	}
	
}
