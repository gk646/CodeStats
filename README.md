## Code Stats

CodeStats is an opensource statistics plugin for JetBrains IDE's with focus on a modern feel and look, performance and a good feature set.


<img src="pictures/overview.png" alt="The startup screen of CodeStats" width="970" height="300">


### Why choose Code Stats?

- **Open Source**
    - the source code is open for review and contribution eliminating the risk of malicious use of data
- Speed and Efficiency
    - Optimized for speed it runs faster than other popular statistic plugins
- Features
    - Tracks file size, blank lines and comments and documentation and more!
- Integration
    - CodeStats is nicely integrated with the IDE with its own toolwindow and settings menu
- Customization 
    - Allows for file-type whitelisting, excluding filetypes, excluding folders and more!

### How it works



#### Components

All components are registered inside the `plugin.xml` like the tool window, listeners and settings menu.
Saving and retrieving the configuration is all done automatically through the JetBrains `@State` annotation and ecosystem.


#### Parsing

For parsing CodeStats uses the standard Java `Files.walkFileTree` to allow for IDE settings independent parsing of the project directory. Alternatives like the `ProjectRootManager.getInstance(project)getFileIndex().iterateContent()` iterates only on all included source directories which could bring conflicts if you want statistics on such a directory but want to exclude it in the IDE.

If the files type is not excluded it gets parsed depending on whether its type is included in the separate tab setting or not. This is to optimize parsing of non source files that do not follow comment or documentation rules. 

#### Non-Source Files

**Filetypes not included in the separate tab setting are considered binary data and as such non-text and non-source files.**
Such files get parsed based on their size to avoid a `OutOfMemoryException`. Files with more than 50mb are read in chunks with a `BufferedInputStream`, smaller files are read with `Files.readAllBytes` in one go. In both cases every byte is checked to be equal to 10 as it's the ASCII code for the line feed character `\n`. This should still give some information about these files in a general way even if the concept of lines does not apply to binary formats.

#### Source Files

**All types included in the separate tab settings are initially handled as source files in UTF-8 encoding.** Should there be an error converting them with this encoding the non-source file parsing is still applied as mentioned above.
