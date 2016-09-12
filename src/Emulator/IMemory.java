/*
 * Copyright (c) 2016 Mario Perić
 *
 * See LICENSE for more info
*/
package Emulator;

interface IMemory {

    short getByte(int location);

    void setByte(int location, short value);

    boolean loadRom(String filePath);

    void closeRom();

    boolean isRomLoaded();
}
