/*
 * Copyright (c) 2016 Mario Perić
 *
 * See LICENSE for more info
*/
package Emulator;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Emulates the memory of CHIP-8.
 *
 * Created by random on 14.08.2016.
 */
public class Memory implements IMemory {

    // Size of memory
    private static final int MEMORY_SIZE = 0x1000;
    // Offset for ROM
    private static final int ROM_OFFSET = 0x200;
    // Emulator.Memory locations
    private short[] memory;
    // ROM loaded indicator
    private boolean romLoaded;

    /**
     * Default constructor for Emulator.Memory object
     */
    public Memory() {
        this.memory = new short[MEMORY_SIZE];
        loadFonts();
        romLoaded = false;
    }

    /**
     * Clears whole memory
     */
    private void clearMemory() {
        for (int i = 0; i < MEMORY_SIZE; i++) {
            memory[i] = 0x0;
        }
    }

    /**
     * Gets the short from given location
     *
     * @param location Emulator.Memory location to be read from
     * @return Content of given memory location
     */
    public short getByte(int location) {
        if (location >= MEMORY_SIZE || location < 0) {
            throw new IllegalArgumentException("Segmentation fault, tried to access " + location);
        }
        return (short)(memory[location] & 0xFF);
    }

    /**
     * Sets the short at given location to given value.
     *
     * @param location Emulator.Memory location to be written to
     * @param value Value to be written to given location
     */
    public void setByte(int location, short value) {
        if (location >= MEMORY_SIZE || location < 0) {
            throw new IllegalArgumentException("Segmentation fault");
        }
        memory[location] = value;
    }

    /**
     * Loads the ROM into the memory.
     *
     * @param filePath Location of ROM on the disk
     * @return True if operation is successful, false otherwise
     */
    public boolean loadRom(String filePath) {
        try {
            FileInputStream fileInputStream = new FileInputStream(filePath);
            int location = ROM_OFFSET;
            int nextByte;
            while ((nextByte = fileInputStream.read()) != -1) {
                if (location < MEMORY_SIZE) {
                    setByte(location++, (short)nextByte);
                } else {
                    fileInputStream.close();
                    romLoaded = false;
                    throw new IllegalArgumentException("Segmentation fault");
                }
            }
            fileInputStream.close();
            romLoaded = true;
            return true;
        } catch (IOException e) {
            romLoaded = false;
            System.out.println("ERROR: Unable to open file!");
            return false;
        }
    }

    /**
     * Loads fonts into the memory.
     */
    public void loadFonts() {

        final char[] smallFont = {
                0xf9,0x99,0xf2,0x62,0x27,
                0xf1,0xf8,0xff,0x1f,0x1f,
                0x99,0xf1,0x1f,0x8f,0x1f,
                0xf8,0xf9,0xff,0x12,0x44,
                0xf9,0xf9,0xff,0x9f,0x1f,
                0xf9,0xf9,0x9e,0x9e,0x9e,
                0xf8,0x88,0xfe,0x99,0x9e,
                0xf8,0xf8,0xff,0x8f,0x88 };

        for (short i=0; i<40; i++) {
            setByte((short)(i*2),   (byte)((smallFont[i]&0xf0))); // First hex
            setByte((short)(i*2+1), (byte)(smallFont[i]<<4));     // Second hex
        }

        final char[] SuperFont = {
                0x3C,0x7E, 0xE7,0xC3, 0xC3,0xC3, 0xC3,0xE7, 0x7E,0x3C,
                0x18,0x38, 0x58,0x18, 0x18,0x18, 0x18,0x18, 0x18,0x3C,
                0x3E,0x7F, 0xC3,0x06, 0x0C,0x18, 0x30,0x60, 0xFF,0xFF,
                0x3C,0x7E, 0xC3,0x03, 0x0E,0x0E, 0x03,0xC3, 0x7E,0x3C,
                0x06,0x0E, 0x1E,0x36, 0x66,0xC6, 0xFF,0xFF, 0x06,0x06,
                0xFF,0xFF, 0xC0,0xC0, 0xFC,0xFE, 0x03,0xC3, 0x7E,0x3C,
                0x3E,0x7C, 0xC0,0xC0, 0xFC,0xFE, 0xC3,0xC3, 0x7E,0x3C,
                0xFF,0xFF, 0x03,0x06, 0x0C,0x18, 0x30,0x60, 0x60,0x60,
                0x3C,0x7E, 0xC3,0xC3, 0x7E,0x7E, 0xC3,0xC3, 0x7E,0x3C,
                0x3C,0x7E, 0xC3,0xC3, 0x7F,0x3F, 0x03,0x03, 0x3E,0x7C };

        for(int i=0; i<100; i++) {
            setByte(0x50+i, (byte)SuperFont[i]);
        }
    }

    /**
     * Closes the ROM and clears the memory for next one.
     */
    public void closeRom() {
        clearMemory();
        romLoaded = false;
    }

    /**
     * @return <code>true</code> if ROM is loaded, <code>false</code> otherwise
     */
    public boolean isRomLoaded() {
        return romLoaded;
    }

}
