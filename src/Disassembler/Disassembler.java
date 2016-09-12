/*
 * Copyright (c) 2016 Mario Perić
 *
 * See LICENSE for more info
*/
package Disassembler;

import java.io.*;

/**
 * Main disassembler class
 */
public class Disassembler {
    // Maximum size a rom can have
    private static final int MAX_ROM_SIZE = 0x800;
    // Header of disassembled file
    private static final String HEADER_TEXT = "# Decompiled with PEACH-8 Disassembler";
    // Size of loaded ROM
    private int size;
    // Byte array representation of ROM
    private short[] rom = new short[MAX_ROM_SIZE];

    /**
     * Reads the ROM into memory.
     *
     * @param filePath Path to the ROM on disk
     * @return <code>true</code> if process is successful, <code>false</code> if not
     */
    public boolean readRom(String filePath) {
        int location = 0x0;
        try {
            FileInputStream stream = new FileInputStream(filePath);
            int nextByte;
            while ((nextByte = stream.read()) != -1) {
                if (location > MAX_ROM_SIZE) {
                    return false;
                } else {
                    rom[location++] = (short) nextByte;
                }
            }
        } catch (IOException e) {
            return false;
        }
        size = location;
        return true;
    }

    /**
     * Disassembles ROM and saves it to disk.
     *
     * @param outFile Path of exit file
     */
    public void disassemble(String outFile) {
        if (outFile == null) {
            outFile = "out.S";
        }
        try {
            PrintWriter writer = new PrintWriter(outFile, "UTF-8");

            writer.println(HEADER_TEXT);

            for (int i = 0; i < size; i += 2) {
                int opcode = rom[i];
                opcode <<= 8;
                opcode += rom[i + 1];
                int command = (opcode & 0xF000) >> 12;
                String operation;
                int reg, reg1, reg2, val, location;

                switch (command) {
                    case 0x0:
                        switch (opcode & 0x00FF) {
                            case 0xE0:
                                operation = "CLS";
                                break;
                            case 0xEE:
                                operation = "RET";
                                break;
                            default:
                                operation = "SYS 0x" + intToHex(opcode & 0x0FFF, 3);
                                break;
                        }
                        break;
                    case 0x1:
                        location = opcode & 0x0FFF;
                        operation = "JP   0x" + intToHex(location, 3);
                        break;
                    case 0x2:
                        location = opcode & 0x0FFF;
                        operation = "CALL 0x" + intToHex(location, 3);
                        break;
                    case 0x3:
                        reg = (opcode & 0x0F00) >> 8;
                        val = (opcode & 0x00FF);
                        operation = "SE   V" + intToHex(reg, 1) + ", 0x" + intToHex(val, 2);
                        break;
                    case 0x4:
                        reg = (opcode & 0x0F00) >> 8;
                        val = (opcode & 0x00FF);
                        operation = "SNE  V" + intToHex(reg, 1) + ", 0x" + intToHex(val, 2);
                        break;
                    case 0x5:
                        reg1 = (opcode & 0x0F00) >> 8;
                        reg2 = (opcode & 0x00F0) >> 4;
                        operation = "SE   V" + intToHex(reg1, 1) + ", V" + intToHex(reg2, 2);
                        break;
                    case 0x6:
                        reg = (opcode & 0x0F00) >> 8;
                        val = (opcode & 0x00FF);
                        operation = "LD   V" + intToHex(reg, 1) + ", 0x" + intToHex(val, 2);
                        break;
                    case 0x7:
                        reg = (opcode & 0x0F00) >> 8;
                        val = (opcode & 0x00FF);
                        operation = "ADD  V" + intToHex(reg, 1) + ", 0x" + intToHex(val, 2);
                        break;
                    case 0x8:
                        reg1 = (opcode & 0x0F00) >> 8;
                        reg2 = (opcode & 0x00F0) >> 4;
                        switch (opcode & 0x000F) {
                            case 0x0:
                                operation = "LD   V" + intToHex(reg1, 1) + ", V" + intToHex(reg2, 1);
                                break;
                            case 0x1:
                                operation = "OR   V" + intToHex(reg1, 1) + ", V" + intToHex(reg2, 1);
                                break;
                            case 0x2:
                                operation = "AND  V" + intToHex(reg1, 1) + ", V" + intToHex(reg2, 1);
                                break;
                            case 0x3:
                                operation = "XOR  V" + intToHex(reg1, 1) + ", V" + intToHex(reg2, 1);
                                break;
                            case 0x4:
                                operation = "ADD  V" + intToHex(reg1, 1) + ", V" + intToHex(reg2, 1);
                                break;
                            case 0x5:
                                operation = "SUB  V" + intToHex(reg1, 1) + ", V" + intToHex(reg2, 1);
                                break;
                            case 0x6:
                                operation = "SHR  V" + intToHex(reg1, 1);
                                break;
                            case 0x7:
                                operation = "SUBN V" + intToHex(reg1, 1) + ", V" + intToHex(reg2, 1);
                                break;
                            case 0xE:
                                operation = "SHL  V" + intToHex(reg1, 1);
                                break;
                            default:
                                operation = "FILL 0x" + intToHex(opcode, 4);
                                break;
                        }
                        break;
                    case 0x9:
                        reg1 = (opcode & 0x0F00) >> 8;
                        reg2 = (opcode & 0x00F0) >> 4;
                        operation = "SNE  V" + intToHex(reg1, 1) + ", V" + intToHex(reg2, 1);
                        break;
                    case 0xA:
                        location = (opcode & 0x0FFF);
                        operation = "LD   I, 0x" + intToHex(location, 3);
                        break;
                    case 0xB:
                        location = (opcode & 0x0FFF);
                        operation = "JP   V0, 0x" + intToHex(location, 3);
                        break;
                    case 0xC:
                        reg = (opcode & 0x0F00) >> 8;
                        val = (opcode & 0x00FF);
                        operation = "RND  V" + intToHex(reg, 1) + ", 0x" + intToHex(val, 2);
                        break;
                    case 0xD:
                        reg1 = (opcode & 0x0F00) >> 8;
                        reg2 = (opcode & 0x00F0) >> 4;
                        val = (opcode & 0x000F);
                        operation = "DRW  V" + intToHex(reg1, 1) + ", V" + intToHex(reg2, 1) + ", 0x" + intToHex(val, 2);
                        break;
                    case 0xE:
                        switch (opcode & 0x00FF) {
                            case 0x9E:
                                reg = (opcode & 0x0F00) >> 8;
                                operation = "SKP  V" + intToHex(reg, 1);
                                break;
                            case 0xA1:
                                reg = (opcode & 0x0F00) >> 8;
                                operation = "SKNP V" + intToHex(reg, 1);
                                break;
                            default:
                                operation = "FILL 0x" + intToHex(opcode, 4);
                                break;
                        }
                        break;
                    case 0xF:
                        reg = (opcode & 0x0F00) >> 8;
                        switch (opcode & 0x00FF) {
                            case 0x07:
                                operation = "LD   V" + intToHex(reg, 1) + ", DT";
                                break;
                            case 0x0A:
                                operation = "LD   V" + intToHex(reg, 1) + ", K";
                                break;
                            case 0x15:
                                operation = "LD   DT, V" + intToHex(reg, 1);
                                break;
                            case 0x18:
                                operation = "LD   ST, V" + intToHex(reg, 1);
                                break;
                            case 0x1E:
                                operation = "ADD  I, V" + intToHex(reg, 1);
                                break;
                            case 0x29:
                                operation = "LD   F, V" + intToHex(reg, 1);
                                break;
                            case 0x33:
                                operation = "LD   B, V" + intToHex(reg, 1);
                                break;
                            case 0x55:
                                operation = "LD   [I], V" + intToHex(reg, 1);
                                break;
                            case 0x65:
                                operation = "LD   V" + intToHex(reg, 1) + " [I]";
                                break;
                            default:
                                operation = "FILL 0x" + intToHex(opcode, 4);
                                break;
                        }
                        break;
                    default:
                        operation = "FILL 0x" + intToHex(opcode, 4);
                        break;
                }

                writer.println(operation);
            }
            writer.close();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            System.out.println("Unable to open file");
            System.exit(1);
        }

    }

    /**
     * Converts int to String in hex form with given number of digits.
     *
     * @param num Number to be converted
     * @param digits Number of digits in string form
     * @return Converted String
     */
    private String intToHex(int num, int digits) {
        return String.format("%0" + digits + "x", num);
    }
}
