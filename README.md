LEDCube
=======

Application to control my 7x7x7 LED cube through USB virtual serial port. This is my personal application, so it's only ever been run on my Mac, I haven't even tried running it on Windows. The application requires the Java serial library (librxtxSerial.jnilib) to be installed in /Library/Java/Extensions, and the Processing Serial Library, which I don't know if I'm allowed to distribute, but you can find it in the Processing IDE source.
 
The software works as-is, but is very simple and not very foolproof. For example, if a serial port is not chosen at startup, the application will crash. I'm in the process of building v2 of the physical cube, and I will update the software when the cube PCB arrives and is fully assembled. The point of this software was not to have a permanent cube control application, but rather to be able to light up arbitrary LEDs in the cube as simply as possible, hence the 343 checkboxes, each representing an LED.
  
The source is very comment-sparse as I wrote this application quickly to get the cube into a working state as fast as possible. I haven't put this on my Github (EDIT Jan 24 2014: I've decided to put it on my Github anyways...) because I don't think it's ready for a public release. If I ever do release the new version publicly,  I will add relevant comments where they are needed.
  
Essentially, the user chooses which LEDs to light and for how long, and this consists of a frame (like a display frame). Many frames can be chained together and uploaded to the ATmega microcontroller. The list of frames can be saved to file in order to save cool-looking patterns. The frame limit is due the micro's 2K of memory. The next version of the software will feature two-way communication between the micro and the application, enabling an unlimited number of frames and continuous animation.
 
