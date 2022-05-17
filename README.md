# jSmartPower3
Java GUI for SmartPower3 odroid programmable supply, on github

![Alt text](images/demo-1.png?raw=true)

# Usage
```
java -jar release/jSmartPower-x.y.jar
```

# How to crop  from displayed graph
1. make sure to have only one plot displayed
2. make sure to have the plot hold ON enabled
3. zoom to the region you want to crop by dragging and releasing mouse
4. press crop file button -> data will be saved locally in the output folder to '{timestamp}_crop.csv'
![Alt text](images/crop.png?raw=true)

# Acknoledgements
 * xChart: https://knowm.org/open-source/xchart/
 * jSerialComm: https://github.com/Fazecast/jSerialComm
