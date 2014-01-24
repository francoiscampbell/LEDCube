/*
 * Uploads a CubeFrame
 * Implemented with a Runnable to make uploading a background
 * task and not block the application
 */

package com.frank.ledcubegui2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class CubeFrameUploader implements Runnable {

	private List<CubeFrame> uploadFrames;
	private OutputStream outPort;
	private InputStream inPort;

	public CubeFrameUploader(List<CubeFrame> cubeFrames, OutputStream outPort, InputStream inPort) {
		this.uploadFrames = cubeFrames;
		this.outPort = outPort;
		this.inPort = inPort;
	}

	@Override
	public void run() {
		try {
			System.out.println("Uploading animation to Arduino...");
			System.out.println("Number of frames being transmitted: " + uploadFrames.size());
			outPort.write(uploadFrames.size());
			for (CubeFrame f : uploadFrames) {
				outPort.write(f.getDataForUpload());
				long timeSent = System.currentTimeMillis();
				while (System.currentTimeMillis() < timeSent + 1000) {
					if (inPort.read() == '*') {
						System.out.println("Recieved response for frame " + uploadFrames.indexOf(f) + " after " + (System.currentTimeMillis() - timeSent) + "ms");
						break;
					}
				}
			}
		} catch (IOException e) {
			System.err.println("Upload failed");
		}
	}

}
