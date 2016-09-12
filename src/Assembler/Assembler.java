/*
 * Copyright (c) 2016 Mario Perić
 *
 * See LICENSE for more info
*/
package Assembler;

import java.io.*;
import java.util.ArrayList;

/**
 * Main assembler class
 */
public class Assembler {

    // Name of the output file if none is specified
    private static final String DEFAULT_OUTPUT_FILE_NAME = "a.c8";

    // Errors
    private static final String ERROR_COMMAND = "Unrecognized command at line ";
    private static final String ERROR_FORMAT = "Wrong format at line ";
    private static final String ERROR_ARGS = "Wrong argument(s) at line ";
    private static final String ERROR_UNKNOWN = "Unknown error at line ";
    private static final String ERROR_IN_FILE = "Error opening file ";
    private static final String ERROR_OUT_FILE = "Error creating file ";

    /**
     * Converts input assembly to the executable file.
     * If output file is not given default value is used.
     *
     * @param inFile Name of the file containing assembly
     * @param outFile Name of the executable file
     */
    public void assemble(String inFile, String outFile) {
        // If output file is not set it is generated with default name
        if (outFile == null) {
            outFile = DEFAULT_OUTPUT_FILE_NAME;
        }

        ArrayList<String> assembly = readFile(inFile);
        ArrayList<Short> opcodes = generateCode(assembly);

        try {
            DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(outFile));
            for (int i = 0; i < opcodes.size(); i++) {
                outputStream.writeByte((int) opcodes.get(i));
            }
        } catch (IOException e) {
            System.out.println(ERROR_OUT_FILE + outFile);
            System.exit(2);
        }
    }

    /**
     * Reads the file and stores it in <code>ArrayList</code>.
     *
     * @param file Name of the file to be read
     * @return <code>ArrayList</code> containing the file
     */
    private ArrayList<String> readFile(String file) {
        ArrayList<String> list = new ArrayList<>();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                list.add(line);
            }
        } catch (IOException e) {
            System.out.println(ERROR_IN_FILE);
            System.exit(2);
        }

        return list;
    }

    /**
     * Converts assembly text to executable code.
     *
     * @param assembly <code>ArrayList</code> containing assembly text
     * @return <code>ArrayList</code> containing executable code
     */
    private ArrayList<Short> generateCode(ArrayList<String> assembly) {
        ArrayList<Short> opcodes = new ArrayList<>();

        for (int i = 0; i < assembly.size(); i++) {
            String ln = assembly.get(i);

            // Remove comments
            if (ln.contains("#")) {
                ln = ln.substring(0, ln.indexOf("#"));
            }

            // Remove tabulators
            ln = ln.replace("\t", " ");
            ln = ln.replaceAll(" +", " ");
            ln = ln.trim().toUpperCase();

            // Empty line
            if (ln.isEmpty()) {
                continue;
            }

            String[] command = ln.split(" ");
            for (int j = 0; j < command.length; j++) {
                command[j] = command[j].trim();
                command[j] = command[j].replace(",", "");
            }

            try {
                int reg, reg1, reg2, val, location;
                switch (command[0]) {
                    case "CLS":
                        opcodes.add((short) 0x00);
                        opcodes.add((short) 0xE0);
                        break;
                    case "RET":
                        opcodes.add((short) 0x00);
                        opcodes.add((short) 0xEE);
                        break;
                    case "JP":
                        if (command[1].equals("V0")) {
                            location = (parse(command[2]) & 0xFFF);
                            opcodes.add((short) (0xB0 | (location >> 8)));
                            opcodes.add((short) (location & 0xFF));
                        } else {
                            location = (parse(command[1]) & 0xFFF);
                            opcodes.add((short) (0x10 | (location >> 8)));
                            opcodes.add((short) (location & 0xFF));
                        }
                        break;
                    case "CALL":
                        location = (parse(command[1]) & 0xFFF);
                        opcodes.add((short) (0x20 | location >> 8));
                        opcodes.add((short) (location & 0xFF));
                        break;
                    case "SE":
                        if (command[2].startsWith("V")) {
                            reg1 = parseReg(command[1]);
                            reg2 = parseReg(command[2]);
                            opcodes.add((short) (0x50 | reg1));
                            opcodes.add((short) (reg2 << 4));
                        } else {
                            reg = parseReg(command[1]);
                            val = parse(command[2]);
                            opcodes.add((short) (0x30 | reg));
                            opcodes.add((short) val);
                        }
                        break;
                    case "SNE":
                        if (command[2].startsWith("V")) {
                            reg1 = parseReg(command[1]);
                            reg2 = parseReg(command[2]);
                            opcodes.add((short) (0x90 | reg1));
                            opcodes.add((short) (reg2 << 4));
                        } else {
                            reg = parseReg(command[1]);
                            val = parse(command[2]);
                            opcodes.add((short) (0x40 | reg));
                            opcodes.add((short) val);
                        }
                        break;
                    case "LD":
                        if (command[1].startsWith("V")) {
                            if (command[2].startsWith("V")) {
                                reg1 = parseReg(command[1]);
                                reg2 = parseReg(command[2]);
                                opcodes.add((short) (0x80 | reg1));
                                opcodes.add((short) (reg2 << 4));
                            } else if (command[2].equals("DT")) {
                                reg = parseReg(command[1]);
                                opcodes.add((short) (0xF0 | reg));
                                opcodes.add((short) 0x07);
                            } else if (command[2].equals("K")) {
                                reg = parseReg(command[1]);
                                opcodes.add((short) (0xF0 | reg));
                                opcodes.add((short) 0x0A);
                            } else if (command[2].equals("[I]")) {
                                reg = parseReg(command[1]);
                                opcodes.add((short) (0xF0 | reg));
                                opcodes.add((short) 0x65);
                            } else {
                                reg = parseReg(command[1]);
                                val = parse(command[2]);
                                opcodes.add((short) (0x60 | reg));
                                opcodes.add((short) val);
                            }
                        } else if (command[1].equals("I")) {
                            location = parse(command[2]);
                            opcodes.add((short) (0xA0 | (location >> 8)));
                            opcodes.add((short) (0xFF & location));
                        } else if (command[1].equals("DT")) {
                            reg = parseReg(command[2]);
                            opcodes.add((short) (0xF0 | reg));
                            opcodes.add((short) 0x15);
                        } else if (command[1].equals("ST")) {
                            reg = parseReg(command[2]);
                            opcodes.add((short) (0xF0 | reg));
                            opcodes.add((short) 0x18);
                        } else if (command[1].equals("F")) {
                            reg = parseReg(command[2]);
                            opcodes.add((short) (0xF0 | reg));
                            opcodes.add((short) 0x29);
                        } else if (command[1].equals("B")) {
                            reg = parseReg(command[2]);
                            opcodes.add((short) (0xF0 | reg));
                            opcodes.add((short) 0x33);
                        } else if (command[1].equals("[I]")) {
                            reg = parseReg(command[2]);
                            opcodes.add((short) (0xF0 | reg));
                            opcodes.add((short) 0x55);
                        } else {
                            exitWithError(ERROR_COMMAND, i + 1);
                        }
                        break;
                    case "ADD":
                        if (command[1].startsWith("V")) {
                            if (command[2].startsWith("V")) {
                                reg1 = parseReg(command[1]);
                                reg2 = parseReg(command[2]);
                                opcodes.add((short) (0x80 | reg1));
                                opcodes.add((short) (0x04 | (reg2 << 4)));
                            } else {
                                reg = parseReg(command[1]);
                                val = parse(command[2]);
                                opcodes.add((short) (0x70 | reg));
                                opcodes.add((short) val);
                            }
                        } else if (command[1].equals("I")) {
                            reg = parseReg(command[1]);
                            opcodes.add((short) (0xF0 | reg));
                            opcodes.add((short) 0x1E);
                        } else {
                            exitWithError(ERROR_COMMAND, i + 1);
                        }
                        break;
                    case "OR":
                        reg1 = parseReg(command[1]);
                        reg2 = parseReg(command[2]);
                        opcodes.add((short) (0x80 | reg1));
                        opcodes.add((short) (0x01 | (reg2 << 4)));
                        break;
                    case "AND":
                        reg1 = parseReg(command[1]);
                        reg2 = parseReg(command[2]);
                        opcodes.add((short) (0x80 | reg1));
                        opcodes.add((short) (0x02 | (reg2 << 4)));
                        break;
                    case "XOR":
                        reg1 = parseReg(command[1]);
                        reg2 = parseReg(command[2]);
                        opcodes.add((short) (0x80 | reg1));
                        opcodes.add((short) (0x03 | (reg2 << 4)));
                        break;
                    case "SUB":
                        reg1 = parseReg(command[1]);
                        reg2 = parseReg(command[2]);
                        opcodes.add((short) (0x80 | reg1));
                        opcodes.add((short) (0x05 | (reg2 << 4)));
                        break;
                    case "SHR":
                        reg1 = parseReg(command[1]);
                        reg2 = parseReg(command[2]);
                        opcodes.add((short) (0x80 | reg1));
                        opcodes.add((short) (0x06 | (reg2 << 4)));
                        break;
                    case "SUBN":
                        reg1 = parseReg(command[1]);
                        reg2 = parseReg(command[2]);
                        opcodes.add((short) (0x80 | reg1));
                        opcodes.add((short) (0x07 | (reg2 << 4)));
                        break;
                    case "SHL":
                        reg1 = parseReg(command[1]);
                        reg2 = parseReg(command[2]);
                        opcodes.add((short) (0x80 | reg1));
                        opcodes.add((short) (0x08 | (reg2 << 4)));
                        break;
                    case "RND":
                        reg = parseReg(command[1]);
                        val = parse(command[2]);
                        opcodes.add((short) (0xC0 | reg));
                        opcodes.add((short) val);
                        break;
                    case "DRW":
                        reg1 = parseReg(command[1]);
                        reg2 = parseReg(command[2]);
                        val = parse(command[3]);
                        opcodes.add((short) (0xD0 | reg1));
                        opcodes.add((short) ((reg2 << 4) | (val & 0x0F)));
                        break;
                    case "SKP":
                        reg = parseReg(command[1]);
                        opcodes.add((short) (0xE0 | reg));
                        opcodes.add((short) 0x9E);
                        break;
                    case "SKNP":
                        reg = parseReg(command[1]);
                        opcodes.add((short) (0xE0 | reg));
                        opcodes.add((short) 0xA1);
                        break;
                    case "SYS":
                        val = parse(command[1]);
                        opcodes.add((short) ((0x0F00 & val) >> 8));
                        opcodes.add((short) ((0x00FF) & val));
                        break;
                    case "FILL":
                        val = parse(command[1]);
                        opcodes.add((short) ((0xFF00 & val) >> 8));
                        opcodes.add((short) (0x00FF & val));
                        break;
                    default:
                        exitWithError(ERROR_COMMAND, i + 1);
                }
            } catch (NumberFormatException e) {
                exitWithError(ERROR_FORMAT, i + 1);
            } catch (ArrayIndexOutOfBoundsException e) {
                exitWithError(ERROR_ARGS, i + 1);
            } catch (Exception e) {
                exitWithError(ERROR_UNKNOWN, i + 1);
            }
        }
        return opcodes;
    }

    /**
     * Converts string to number with base 2, 8, 10 or 16
     * depending on prefix.
     *
     * @param s <code>String</code> to be converted
     * @return Converted number
     */
    private int parse(String s) {
        if (s.startsWith("0b") || s.startsWith("0B")) {
            return Integer.parseInt(s.substring(2), 2);
        } else if (s.startsWith("0x") || s.startsWith("0X")) {
            return Integer.parseInt(s.substring(2), 16);
        } else if (s.startsWith("0")) {
            return Integer.parseInt(s.substring(1), 8);
        } else {
            return Integer.parseInt(s);
        }
    }

    /**
     * Converts register name to equivalent number representation.
     *
     * @param s Register name
     * @return Number representation of the register
     */
    private int parseReg(String s) {
        // Checks name register
        if (s.charAt(0) != 'V') {
            throw new NumberFormatException("Wrong register name");
        }
        return Integer.parseInt(s.substring(1), 16);
    }

    /**
     * Exits program with error.
     *
     * @param type Type of the error
     * @param line Number of line containing the error.
     */
    private void exitWithError(String type, int line) {
        System.out.println(type + line);
        System.exit(1);
    }
}
