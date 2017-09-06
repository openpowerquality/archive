OPQ OHM1
======
The original power quality monitor designed around the Raspberry Pi and MSP430AFE.

LICENCE:
--------
Copyright (C)  2013  Open Power Quality.
    Permission is granted to copy, distribute and/or modify this document
    distribution under the terms of the GNU Free Documentation License, Version 1.3
    or any later version published by the Free Software Foundation;
    
Breakdown of directory structure:
---------------------------------
* Schematics: This folder contains schematics and layout files for OHM1 Power quality monitor.Gerbers, bill of materials and simulations are provided as well, so anyone can recreate this device. Schematics and Layout are performed in Mentor Graphics Pads design suit. A free viewer is available from Mentor Graphics website.
* Firmware: This folder contains code for the MSP430AFE. Firmware is written in C using TI’s code composer studio. MSP430SAFE is programed via JTAG using TI's standard header pinout. A programer for this IC can be obtained from TI, Olimex, or online retailers. The most most economical way of programing this board is using the MSP430 Launchpad.
* Software: This folder contains the software distribution for the Raspberry Pi. It is written in C and requires GNU Linux, GNU scientific library, boost library suit, and of course a working compiler.

###Manufacturing the PCB:

Gerbers alone are sufficient for manufacturing the PCB. The PCB is 2 layers with top layer silk screen and component placement.

###Preparing a Raspberry PI:
First step is getting a RPI Debian distribution on an SD card. Luckily Raspberry pi foundation provides a debian distribution. Next prerequisits must be installed. A simple apt-get call takes care of that:

`apt-get install git libgsl0-dev pkg-config build-essentials libboost-dev libboost-system-dev libboost-thread-dev`

Finally check out this distribution via git. Browse to: Gen1/Software/rpid. Run

`make`

followed by

`make install`
