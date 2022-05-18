# jSmartPower3
Java GUI for SmartPower3 odroid programmable supply, on github

## Introduction

SmartPower3 is a dual channel programmable power supply (aka PSU) designed to power and test USB devices manufactured by Odroid. Its main use is to monitor power consuption of single board computers while testing power efficiency of the latter.

The PSU has is built around an ESP32 device and has open source firmware. Firmware update can be user triggered with general purpose Python tool for ESP32 devices.

The device has a front panel to operate the channels and to setup serial logging speed. It can be connected to a PC with an USB cable, and it shows up ad serial device. Interacting with the serial device it is possible to receive logged data and to setup the wireless connection as an alternate logging source to usb. Please note that using the front panel you cannot setup wireless connection, it is possible only using serial port. However, logging frequency must and can be enabled only using the front panel. Wireless logging happens via UDP, provided the recipient's local ip address is known in advance and does not change over time, hence using dhcp is unadvised.

*jSmartPower3* is a graphical interface which handles all the logging with interactive plots and simply the use of UDP logging.

* SmartPower3 wiki: https://wiki.odroid.com/accessory/power_supply_battery/smartpower3
* SmartPower3 fw: https://github.com/hardkernel/smartpower3

![Alt text](images/demo-1.png?raw=true)

## Warning

Before you begin, when you connect the device using USB port, make sure to set the serial's baudrate to maximum (921600) using the front panel as explained in the wiki linked above. The gui only set the baudrate of receiving serial port, while device's baudrate (sender) can be set only from the device itself.

## Usage
```
git clone https://github.com/VirtuContraFurore/jSmartPower3.git
java -jar jSmartPower3/release/jSmartPower-1.0.jar
```
## What it does
* Real time plot! Select the refresh rate as *lowest* as possible to minize graphical overhead. If the gui slow down when you are logging for many minutes try to select a coarser time scale. Avoid to select millisecond timescale with high update interval (like 5ms) as will display unnecessary details at the cost of wasting resources in rendering. Don't worry, when plotting to file everything will be saved according to received data, even if the plots are coarse.
* Simplyfied wireless setup! Just click the buttons in the WIFI STATUS panel to modify UDP logging, but make sure to the the serial port connected AND opened, as wireless setup goes through the serial port.
* 

## How to crop  from displayed graph
1. make sure to have only one plot displayed
2. make sure to have the plot hold ON enabled
3. zoom to the region you want to crop by dragging and releasing mouse
4. press crop file button -> data will be saved locally in the output folder to '{timestamp}_crop.csv'
5. PRO TIP: if you want to save *everything* you see on plot and not just a zoomed portion, you can skip point 3 and save the WHOLE data set! (This is especially usefull if you forgot to enable file capturing and notice only later when data have already been acquired but not saved on file)
![Alt text](images/crop.png?raw=true)

# Acknoledgements
 * xChart: https://knowm.org/open-source/xchart/
 * jSerialComm: https://github.com/Fazecast/jSerialComm
