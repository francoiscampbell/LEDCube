/*
 * Represents one static frame of the LED cube
 * Each frame is packaged into 49 bytes for upload, with the
 * 7 least significant bits representing each LED on or off
 * 
 * This method of packaging is the simplest and fastest way of getting the information
 * to the microcontroller, without making it work too hard at decoding, so it can
 * use all its processing power and time rendering the animation
 */

package com.frank.ledcubegui2;

public class CubeFrame {
	private boolean[][][] data;
	private byte[] byteFrame;
	private int duration;

	public enum FrameDirection {
		PLUSX, PLUSY, PLUSZ, MINUSX, MINUSY, MINUSZ
	}

	private static final int CUBESIZE = MainGUI.CUBESIZE;

	public CubeFrame() {
		data = new boolean[CUBESIZE][CUBESIZE][CUBESIZE];
		this.duration = 0;
	}

	public CubeFrame(boolean[][][] data, int duration) {
		this.data = data;
		this.duration = duration;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	// From Processing source code
	static public final int constrain(int amt, int low, int high) {
		return (amt < low) ? low : ((amt > high) ? high : amt);
	}

	public void toggleBit(int x, int y, int z) {
		data[x][y][z] = !data[x][y][z];
	}

	public boolean bitActive(int x, int y, int z) {
		return data[x][y][z];
	}

	public void clear() {
		data = new boolean[CUBESIZE][CUBESIZE][CUBESIZE];
		duration = 0;
	}

	public CubeFrame shifted(FrameDirection direction) {
		boolean[][][] newData = new boolean[CUBESIZE][CUBESIZE][CUBESIZE];
		switch (direction) {
		case PLUSX:
			for (int x = CUBESIZE - 1; x > 0; x--) {
				for (int y = 0; y < CUBESIZE; y++) {
					for (int z = 0; z < CUBESIZE; z++) {
						newData[x][y][z] = data[x - 1][y][z];
					}
				}
			}
			break;
		case PLUSY:
			for (int x = 0; x < CUBESIZE; x++) {
				for (int y = CUBESIZE - 1; y > 0; y--) {
					for (int z = 0; z < CUBESIZE; z++) {
						newData[x][y][z] = data[x][y - 1][z];
					}
				}
			}
			break;
		case PLUSZ:
			for (int x = 0; x < CUBESIZE; x++) {
				for (int y = 0; y < CUBESIZE; y++) {
					for (int z = CUBESIZE - 1; z > 0; z--) {
						newData[x][y][z] = data[x][y][z - 1];
					}
				}
			}
			break;
		case MINUSX:
			for (int x = 0; x < CUBESIZE - 1; x++) {
				for (int y = 0; y < CUBESIZE; y++) {
					for (int z = 0; z < CUBESIZE; z++) {
						newData[x][y][z] = data[x + 1][y][z];
					}
				}
			}
			break;
		case MINUSY:
			for (int x = 0; x < CUBESIZE; x++) {
				for (int y = 0; y < CUBESIZE - 1; y++) {
					for (int z = 0; z < CUBESIZE; z++) {
						newData[x][y][z] = data[x][y + 1][z];
					}
				}
			}
			break;
		case MINUSZ:
			for (int x = 0; x < CUBESIZE; x++) {
				for (int y = 0; y < CUBESIZE; y++) {
					for (int z = 0; z < CUBESIZE - 1; z++) {
						newData[x][y][z] = data[x][y][z + 1];
					}
				}
			}
			break;
		}

		return new CubeFrame(newData, duration);
	}

	public byte[] getDataForUpload() {
		byteFrame = new byte[CUBESIZE * CUBESIZE];
		for (int z = 0; z < CUBESIZE; z++) {
			for (int y = 0; y < CUBESIZE; y++) {
				for (int x = 0; x < CUBESIZE; x++) {
					if (data[x][y][z]) {
						byteFrame[(z * CUBESIZE) + y] |= (1 << x);
					}
				}
			}
		}
		encodeDuration();
		return byteFrame;
	}

	private void encodeDuration() {
		int mask = 1;
		for (int col = 0; col < 12; col++) {
			if ((duration & mask) > 0) {
				byteFrame[col] |= 128;
			}
			mask <<= 1;
		}
	}
}