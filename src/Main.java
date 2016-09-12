/*
 * Copyright (c) 2016 Mario Perić
 *
 * See LICENSE for more info
*/

import Assembler.Assembler;
import Disassembler.Disassembler;
import Emulator.Emulator;

/**
 * Main class for program execution.
 *
 * Created by random on 15.08.2016.
 */
public class Main {

    /**
     * Prints the help.
     */
    private static void printHelp() {
        System.out.println("usage: [-h display help] [-s scale factor]" +
                "\n      [-d CPU time delay between commands] [-t theme] ROM\n");
        System.out.println("THEMES:\n" +
                "0: BLACK-WHITE\n" +
                "1: POWERSHELL\n" +
                "2: RADAR\n" +
                "3: INVERTED\n");
        System.out.println();
        System.out.println("Assembler usage: --asm [-o output file name] input\n");
        System.out.println("Disassembler usage: --dasm [-o output file name] ROM\n");
    }

    public static void main(String[] args) {

        boolean romSet = false;
        String inFile = null;
        String outFile = null;

        //Disassembler
        if (args[0].equals("--dasm")) {
            Disassembler dasm = new Disassembler();

            int i = 1;
            while (i < args.length) {
                if (args[i].equals("-o") || args[i].equals("/o")) {
                    outFile = args[++i];
                } else if (args[i].equals("-h")) {
                    printHelp();
                } else {
                    if (!romSet) {
                        dasm.readRom(args[i]);
                        romSet = true;
                    }
                }
                i++;
            }

            if (romSet) {
                dasm.disassemble(outFile);
            } else {
                System.out.println("ROM not specified!");
            }

            return;

        } else if (args[0].equals("--asm")) {
            Assembler asm = new Assembler();
            int i = 1;
            while (i < args.length) {
                if (args[i].equals("-o") || args[i].equals("/o")) {
                    outFile = args[++i];
                } else if (args[i].equals("-h")) {
                    printHelp();
                } else {
                    if (inFile == null) {
                        inFile = args[i];
                    }
                }
                i++;
            }

            if (inFile != null) {
                asm.assemble(inFile, outFile);
            } else {
                System.out.println("No input file!");
            }

            return;

        } else {

            Emulator.Builder builder = new Emulator.Builder();

            int i = 0;
            while (i < args.length) {
                if (args[i].equals("-h")) {
                    printHelp();
                    // Time delay
                } else if (args[i].equals("-d") || args[i].equals("/d")) {
                    int delay = Integer.parseInt(args[++i]);
                    if (delay > 0) {
                        builder.setCycleTime(delay);
                    } else {
                        throw new IllegalArgumentException("Invalid argument " + args[i - 1] + args[i]);
                    }
                    // Theme
                } else if (args[i].equals("-t") || args[i].equals("/t")) {
                    int type = Integer.parseInt(args[++i]);
                    builder.setScreenType(type);
                    // Scale factor
                } else if (args[i].equals("-s") || args[i].equals("/s")) {
                    int scale = Integer.parseInt(args[++i]);
                    if (scale > 0) {
                        builder.setScale(scale);
                    } else {
                        throw new IllegalArgumentException("Invalid argument " + args[i - 1] + args[i]);
                    }
                    // Unsupported argument
                } else if (args[i].charAt(0) == '-') {
                    throw new IllegalArgumentException("Invalid argument " + args[i]);
                    // ROM
                } else {
                    if (!romSet) {
                        builder.setRom(args[i]);
                        romSet = true;
                    }
                }
                i++;
            }

            if (romSet) {
                Emulator emulator = builder.build();
                emulator.start();
            } else {
                System.out.println("ROM not specified!");
            }
        }
    }
}
