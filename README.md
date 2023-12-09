# Sequential Pinch Force Task
A presentation software for the Sequence Pinch Force Task. The experiment consists of a reference bar whose height varies based on a configured sequence. 
The participant needs to match the height of the reference bar using a second bar that is driven by the applied pinch force on a force plate. The pinch force data is generated by a force plate connected via a serial COM port (over USB).

## Task design
This task is modeled on previous work, including the following (most relevant) references:
- https://pubmed.ncbi.nlm.nih.gov/22623914/
- https://pubmed.ncbi.nlm.nih.gov/34704176/
- https://pubmed.ncbi.nlm.nih.gov/33885965/

## Build
The application is built with Maven 3

You can use your system's version of Maven or you can use the included Maven wrapper if you don't have Maven installed

To use the wrapper, run the following command from the root of the repo

`./mvnw clean package`

This will generate a runnable jar in the `target` folder

## Run
This application uses Java 17. Make sure it is installed before running the application.

- if you are running this on a linux system, you will require explicit access to the incoming data from ports {`/dev/ttyACM0`; `/dev/ttyACM1`}. This can be granted by the _system administrator_ providing the user with access to the `dialout` group with `sudo usermod -a -G dialout theUserNameHere`

### Installing Java17 (linux)
Java 11 may still be the default for Ubuntu-based OSs, so you must install it explicitly

```
sudo apt install openjdk-17-jre  openjdk-17-jdk
```

Check that the proper installation is active by running `java --version`

Once a JDK or JRE is installed, run

`java -jar <jarFile>`

For example, if running a locally built version

`java -jar target/spft-1.0-SNAPSHOT-jar-with-dependencies.jar`

This will start a window where you can load a [configuration file](#configuration-file) for a session.
There is a list of [runtime flags](#runtime-flags) that let you customize some aspects of the application

### Configuration file
A configuration file is a YAML file that sets all the parameters for a single session.

The configuration file contains the details about the session, a list of blocks and a list of trials. Each block has a
configuration for its parameters and a list of trials that reference a particular trial configuration from the top-level list of trials

For a sample configuration, check [this file](src/test/configs/sample1.yml)

#### Parameters
`sessionName`: A user-friendly name for the session. Only visible by the experimenter  
`outputSuffix`: Part of the [output file](#output)'s name  
`interBlockInterval`: A value in milliseconds to wait between consecutive blocks. There's no IBI before the first block
or after the last one  
`forceProportionRange`: What proportion of the Maximum Voluntary Contraction (MVC) to limit the force range to  
`triggers`: [Optional] A list or single key that needs to be pressed to start the session after the experimenter has manually
started it. This is useful when there's an external devices that needs to synchronize execution, such as an MRI scanner.
If the parameter is missing, the session will start as soon as the experimenter clicks on start  
`colours`: [Optional] A hex colour code (without '#') to use for the `leftReference`, `leftForce`, `rightReference` or `rightForce` bars  
`blocks`: The list of blocks in the sessions  
`blocks.name`: A friendly name of a particular block. Seen only by the experimenter  
`blocks.instructions`: A string giving the instructions to the participant of the block that is about to start  
`blocks.instructionsDuration`: The duration in milliseconds that the instructions will be on screen  
`blocks.feedback`: A string giving feedback to the participant at the end of the block  
`blocks.feedbackDuration`: The duration in milliseconds that the feedback will be on screen  
`blocks.interTrialInterval`: The duration in milliseconds of the delay between trials  
`blocks.trials`: A list of references to the sequences that define each trial. The referenced sequence has to exist in
the top-level pool of sequences  
`sequences`: A pool of sequences that can be referenced in blocks' trials. Each sequence has the values for the reference
bar and a frequency in Hz that defines the speed of the sequence 

### Runtime flags
Runtime flags are JVM System Properties that control some behaviour of the application
To pass a system property, use the `-Dproperty=value` syntax before the jar name that's standard in java applications. For
example, to enable debug mode: `java -jar -Ddebug=true target/spft-1.0-SNAPSHOT-jar-with-dependencies.jar`

`debug`: Enables extra verbose logging and panel coloured backgrounds to see where each panel ends  
`spft.forceData.smoothWindowSize`: The number of samples in the averaging window to calculate the height of the force bar. Default: 1  
`spft.ui.font.size`: Font size for the instructions and feedback text. Default: calculated relative to screen's vertical resolution
`spft.ui.bars.minHeight`: Minimum height of the bars, in pixels. Default: 20   
`spft.ui.bars.maxHeight`: Maximum height of the bars, in pixels. Default: calculated relative to screen's vertical resolution   
`spft.ui.bars.width`: Bars width in pixels. Default: calculated relative to screen's horizontal resolution  
`spft.ui.bars.separation`: Separation between the reference and the force bar of a single hand. Default: calculated relative to screen's horizontal resolution  
`spft.ui.bars.referenceOutside`: Boolean flag to indicate if the reference bar of each hand should be on the inside or outside.
Default:  false, the reference bars are inside  


## Experiment Structure
The highest level of a run is a _session_. Each session is specified by its own [config file](#configuration-file) and generates a single [output file](#output).  
A session is composed of _blocks_. Each block is separated from the next block by an inter-block interval. A block starts
with instructions of what the participant is supposed to do in that particular block and ends with feedback for that block.    
A block is itself subdivided into _trials_. Each trial is separated from the next trial by an inter-trial interval. A trial 
is a sequence of values that the participant is supposed to match. If a trial references a sequence with `valuesLeft` and
`valuesRight`, the trial is considered a bi-manual trial, this will use 4 bars (left, right reference & left right force)

## Output
The output of a session is a YAML file that is self-contained. This means that to process it, you don't need to use the
original configuration file. All the configuration parameters needed for processing should be included in the output

### Fields
`sessionName`: The name of the session as specified in the configuration file  
`startTime`: The date and wall time in UTC when the session started. This is the time when the experimenter clicked start, *not*
when the trigger was received  
`configurationFile`: The full path of the configuration file used  
`configurationChecksum`: The MD5 checksum of the configuration file. This is useful in case the configuration file is modified across runs  
`participantId`: An ID of the participant provided by the experimenter when starting the session  
`maximum*VoluntaryContraction`: A value entered by the experimenter when starting the session based on the results of the
first run of the experiment  
`blocks`: A list of every block and its trials   
`blocks.startTimestamp`: The start of the block using a CPU clock. Its value is meaningless in absolute terms, it is only
relative to the other times in the session  
`blocks.trials`: A list of the presentation values and their actual timestamps using a CPU clock  
`blocks.endTimestamp`: The end of the block using a CPU clock  
`devices`: A list of hardware force devices with each element containing the full stream of data starting when one of the triggers is received  
`triggers`: A lif of triggers received throughout the session

## Implementation notes
### Force bar height
The height of the force bar controlled by the device is calculated in the method `com.github.neuralabc.spft.ui.BarsPanel.changeForceHeight` found [here](https://github.com/neuralabc/spft-java/blob/ef91a783441606002bbc7ccd13dfa1539469e3a0/src/main/java/com/github/neuralabc/spft/ui/BarsPanel.java#L145).
It is a function of the raw value from the device, the MVC for that hand, the max and min force range proportion and the min and max height in pixels of the bar.

It is probably simpler to look at the code but a (less precise) description in natural language would be that the height
of the bar is the MVC-normalized force value (raw/mvc) linearly projected to a normalized range between the min and max force range 

- `((deviceForceValue / MVC) - forceRangeMin)/(forceRangeMax-forceRangeMin)`
- where:
  - MVC = `maximumLeftVoluntaryContraction` in output yml
  - deviceForceValue = `values` under `devices` in output yml
- deviceForceValue can be converted to actual force with the following:
  - for these sensors it is ~ 90.8. To determine it, we would need another calibrated sensor
  - values measure a range of 0-50000g, such that if you press with 9.81N it will return the value 1000
  - sensors are calibrated on plug-in to correct whatever random offset there is to 0 -> **there _must_ be no pressure on the sensor when it is plugged in!**
