### Problem
Description

Write a program that can parse a file containing flow log data and maps each row to a tag based on a lookup table. 
The lookup table is defined as a csv file, and it has 3 columns, dstport,protocol,tag. 
The dstport and protocol combination decide what tag can be applied.

### Run Application
This is a gradle project. Before running the application, ensure you have set the `JAVA_HOME` environment variable to point to your Java 17 installation.
To run the application, you can use the following command:
```
gradle run
```
or you can directly go to [FlowLogTagger](org.example.FlowLogTagger) and run the main method.