# CHEAP-8
[![Build Status](https://travis-ci.org/randomCharacter/CHEAP-8.svg?branch=master)](https://travis-ci.org/randomCharacter/CHEAP-8)

Simple CHIP-8 emulator, assembler and disassembler written in Java.


## Building
In order to build project make sure you have installed:   
- [JAVA JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html)   
- [ANT](https://ant.apache.org/bindownload.cgi)   
   
Simply run `ant` while in project directory   

## Emulator

### Running
The compiled file is in `binary` directory, so simply run 
`java -jar binary/CHEAP-8.jar [-h] [display help] [-s] [scale factor]" [-t] [CPU time delay between commands] [-d] [display type] Path_to_rom`
Arguments in `[]` brackets are optional

### Keyboard
![keyboard_image](http://www.raduangelescu.com/images/keymapping.png)

### Themes
Currently there are 4 supported themes

#### Black-white
![black_white](http://image.prntscr.com/image/d4eaedccdc59479ca008a67960e78299.png)

#### PowerShell
![power_shell](http://image.prntscr.com/image/25e7418b2fc9478ea75e62206d8bfa8d.png)

#### Radar
![radar](http://image.prntscr.com/image/cd3a48083c8945159305528f5c59c64e.png)

#### Inverted
![inverted](http://image.prntscr.com/image/e530458de46c4e668332c3baace47222.png)

## Assembler

### Running
`java -jar binary/CHEAP-8.jar --asm [-o output file] Path_to_rom`

### List of commands
For list of supported commands and help with syntax see [CHIP-8 Technical Reference](http://devernay.free.fr/hacks/chip8/C8TECH10.HTM)   
For comments in assembly files use `#`

## Disassembler

### Runing
`java -jar binary/CHEAP-8.jar --dasm [-o output file] Path_to_rom`

## Suggestions
If you want to build your own emulator I suggest you also check out:   
[CHIP-8 Wikipedia page](https://en.wikipedia.org/wiki/CHIP-8)   
[CHIP-8 Technical Reference](http://devernay.free.fr/hacks/chip8/C8TECH10.HTM)   
[craigthomas' emulator](https://github.com/craigthomas/Chip8C)   

## License
[MIT License](https://opensource.org/licenses/MIT) © Mario Perić
