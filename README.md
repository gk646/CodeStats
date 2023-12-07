![Downloads](https://img.shields.io/jetbrains/plugin/d/22328-codestats)
![Version](https://img.shields.io/jetbrains/plugin/v/22328-codestats)
![Rating](https://img.shields.io/jetbrains/plugin/r/rating/22328-codestats)
[![JetBrains IntelliJ Platform SDK Docs](https://jb.gg/badges/docs.svg)](https://plugins.jetbrains.com/docs/intellij)

## Code Stats

### Download the addon from the official [JetBrains Plugin Website](https://plugins.jetbrains.com/plugin/22328-codestats)

CodeStats is an opensource statistics plugin for JetBrains IDE's with focus on a modern look and feel, performance and a good feature set.


<img src="pictures/overview.png" alt="The startup screen of CodeStats" width="970" height="300">


### Why choose Code Stats?

- **Open Source**
    - the source code is open for review and contribution eliminating the risk of malicious use of data
- Speed and Efficiency
    - Optimized for speed it runs faster than other popular statistic plugins
- Features
    - Shows file size, blank lines, comments, documentation and shows you a timeline of past statistics
- Integration
    - CodeStats is nicely integrated with the IDE with its own toolwindow and settings menu
- Customization 
    - Allows for file-type whitelisting, excluding filetypes, excluding folders and choosing text encodings!

### How it works

#### TimeLine

CodeStats always logs current values for its timepoints, even if settings for included folders or filetypes change between points.

- **UI**:
  - **Y-Axis Options**: Toggle between CODE lines and total lines of the project using the top buttons.
  - **Data Points**: Choose between commit data points or generic ones.
  - **X-Axis Scaling**: Scales according to the total time difference between the first and last data point. It may range from days, weeks, half-months to month intervals.

- **Editing Points**:
  - **Storage**: All CodeStats settings are project-specific and saved in the `.idea/workspace.xml` file.
  - **Access**: Use `STR+F` inside the file and search for `CodeStats` to find each timepoint with its attributes and timestamp.
  - **Timestamp Info**: The timestamp is in UNIX-millis based on your current time zone: `ZonedDateTime.now().toInstant().toEpochMilli()`.

- **Generic Points**:
  - CodeStats replaces the most recent point when opened, unless the new point is in a different half of the day from the last one. For example, opening at 11:59 AM and again at 12:01 PM results in two points. Otherwise, the previous point updates.

- **Commit Points**:
  - On each successful commit a CodeStats refreshes and creates a commit timepoint. This is done non-intrusive via `CheckinHandlerFactory`.


#### Parsing

For parsing CodeStats uses the standard Java `Files.walkFileTree` to allow for IDE settings independent parsing of the project directory. Alternatives like the `ProjectRootManager.getInstance(project)getFileIndex().iterateContent()` iterates only on all included source directories which could bring conflicts if you want statistics on such a directory but want to exclude it in the IDE.

If the files type is not excluded it gets parsed depending on whether its type is included in the separate tab setting or not. This is to optimize parsing of non source files that do not follow comment or documentation rules. 

#### Non-Source Files

**Filetypes not included in the separate tab setting are considered binary data and as such non-text and non-source files.**  
Such files get parsed based on their size to avoid a `OutOfMemoryException`. Files with more than 50mb are read in chunks with a `BufferedInputStream`, smaller files are read with `Files.readAllBytes` in one go. In both cases every byte is checked to be equal to 10 as it's the ASCII code for the line feed character `\n`. This should still give some information about these files in a general way even if the concept of lines does not apply to binary formats.

#### Source Files

**All types included in the separate tab settings are initially handled as source files in the chosen encoding (default UTF-8).**  
Should there be an error converting them with this encoding the non-source file parsing is still applied as mentioned above.

#### Components

- **Registration**: Components like the tool window, listeners, and settings menu are registered in the `plugin.xml`.
- **Configuration**: JetBrains' `@State` annotation and ecosystem automate saving and retrieving configurations.
