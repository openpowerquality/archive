#!/bin/bash

# Install needed libs
echo "Installing dependicies from apt..."
apt-get install time libgsl0-dev libboost-dev libboost-filesystem-dev vim-nox libboost-thread-dev eject zenity

# Make sure usb mount point exists
mkdir /media/usb

# Setup fstab
echo "/dev/sda1	/media/usb	vfat	rw,defaults	0	0" >> /etc/fstab

