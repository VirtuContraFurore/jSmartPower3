# jSmartPower3
Java GUI for SmartPower3 odroid programmable supply, on github

## Introduction

SmartPower3 is a dual channel programmable power supply (aka PSU) designed to power and test USB devices manufactured by Odroid. Its main use is to monitor power consuption of single board computers while testing power efficiency of the latter.

The PSU has is built around an ESP32 device and has open source firmware. Firmware update can be user triggered with general purpose Python tool for ESP32 devices.

The device has a front panel to operate the channels and to setup serial logging speed. It can be connected to a PC with an USB cable, and it shows up ad serial device. Interacting with the serial device it is possible to receive logged data and to setup the wireless connection as an alternate logging source to usb. Please note that using the front panel you cannot setup wireless connection, it is possible only using serial port. However, logging frequency must and can be enabled only using the front panel. Wireless logging happens via UDP, provided the recipient's local ip address is known in advance and does not change over time, hence using dhcp is unadvised.

* SmartPower3 wiki: https://wiki.odroid.com/accessory/power_supply_battery/smartpower3
* SmartPower3 fw: https://github.com/hardkernel/smartpower3

![Alt text](images/demo-1.png?raw=true)


## Usage
```
java -jar release/jSmartPower-x.y.jar
```

## How to crop  from displayed graph
1. make sure to have only one plot displayed
2. make sure to have the plot hold ON enabled
3. zoom to the region you want to crop by dragging and releasing mouse
4. press crop file button -> data will be saved locally in the output folder to '{timestamp}_crop.csv'
![Alt text](images/crop.png?raw=true)

# Acknoledgements
 * xChart: https://knowm.org/open-source/xchart/
 * jSerialComm: https://github.com/Fazecast/jSerialComm
