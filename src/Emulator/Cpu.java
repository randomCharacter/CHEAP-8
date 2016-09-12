/*
 * Copyright (c) 2016 Mario Perić
 *
 * See LICENSE for more info
*/
package Emulator;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Emulates CHIP-8 CPU.
 *
 * Created by random on 15.08.2016.
 */
public class Cpu extends Thread implements ICpu {

    // Number of 8-bit registers
    private static final int NUM_REGISTERS = 16;
    // Delay time
    private static final int KEY_DELAY = 300;
    // Delay clock
    private static final int TIMER_DELAY = 5;
    // Location of ROM
    private static final int PC_START = 0x200;
    // Location of stack
    private static final int STACK_START = 0x52;
    // Default CPU cycle time
    protected static final int DEFAULT_CYCLE_TIME = 1;

    // Random number generator
    private Random random;
    // Program memory
    private Memory memory;
    // Emulated screen
    private Screen screen;
    // Emulated keyboard
    private Keyboard keyboard;
    // Emulated Midi device
    private MidiChannel midiChannel;

    // Registers
    private short[] regV = new short[NUM_REGISTERS];
    private int regI;
    private int regStack;

    // Timers
    private short timerDelay;
    private short timerSound;

    // Program counter
    private int pc = PC_START;

    // CPU alive indicator
    private boolean cpuAlive;

    // CPU paused indicator
    private boolean cpuPaused;

    // CPU cycle time
    private int cpuCycleTime;

    // Operation for Debug
    private String operation;

    /**
     * Default constructor for the class.
     *
     * @param memory The instance of memory to be used
     * @param screen The instance of screen
     * @param keyboard The instance of keyboard to be used
     */
    public Cpu(Memory memory, Screen screen, Keyboard keyboard) {
        this.random = new Random();
        this.memory = memory;
        this.screen = screen;
        this.keyboard = keyboard;

        Timer timer = new Timer("Timer");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                decrementTimers();
            }
        }, TIMER_DELAY, TIMER_DELAY);

        try {
            Synthesizer synthesizer = MidiSystem.getSynthesizer();
            synthesizer.open();
            midiChannel = synthesizer.getChannels()[0];
        } catch (MidiUnavailableException e) {
            System.out.println("WARNING: Midi unavailable, continuing with no sound!");
        }

        cpuPaused = false;
        cpuAlive = true;
        cpuCycleTime = DEFAULT_CYCLE_TIME;

        reset();

    }

    /**
     * Emulates computer restart.
     */
    private void reset() {
        regV = new short[NUM_REGISTERS];
        pc = PC_START;
        regStack = STACK_START;
        regI = 0;
        timerDelay = 0;
        timerSound = 0;
        if (screen != null) {
            screen.clearScreen();
        }
    }

    /**
     * Decrements delay and sound timers.
     */
    private void decrementTimers() {
        if (timerSound > 0) {
            timerSound--;
            midiChannel.noteOn(60, 50);
        }
        if (timerDelay > 0) {
            timerDelay--;
        }
        if (timerSound == 0 && midiChannel != null) {
            midiChannel.noteOff(60);
        }
    }

    /**
     * Sets the CPU to paused/not paused mode
     *
     * @param paused <code>true</code> if CPU is paused
     */
    public void setCpuPaused(boolean paused) {
        cpuPaused = paused;
    }

    /**
     * Sets CPU cycle time to the new value
     * @param cycleTime New CPU cycle time
     */
    public void setCpuCycleTime(int cycleTime) {
        cpuCycleTime = cycleTime;
    }

    /**
     * Reads command from the memory, executes it
     * and increases program counter so that next
     * command can be run.
     */
    public void executeNextCommand() {
        int opcode = memory.getByte(pc);
        opcode = (opcode & 0xFF) << 8;
        opcode += memory.getByte(pc + 1);
        opcode = (opcode & 0xFFFF);
        pc += 2;
        execute(opcode);
    }

    /**
     * Executes given opcode.
     *
     * @param opcode Operation code to be executed
     */
    public void execute(int opcode) {
        int command = (opcode & 0xF000) >> 12;
        int reg, reg1, reg2, val, location;
        switch (command) {
            case 0x0:
                switch (opcode & 0x00FF) {
                    case 0xE0: // Clears the screen.
                        clearScreen();
                        operation = "CLS";
                        break;
                    case 0xEE: // Returns from a subroutine.
                        returnFromSubrutine();
                        operation = "RET";
                        break;
                    default:
                        System.out.println("Unsuported command: " + String.format("0x%08X", opcode) + " Address: " + String.format("0x%08X", pc));
                        throw new IllegalArgumentException("Unsuported command: " + String.format("0x%08X", opcode) + " Address: " + String.format("0x%08X", pc));
                }
                break;
            case 0x1: // Jumps to address.
                location = opcode & 0x0FFF;
                jumpToAddress(location);
                operation = "JUMP " + location;
                break;
            case 0x2: // Calls subroutine.
                location = opcode & 0x0FFF;
                callSubroutine(location);
                operation = "CALL " + location;
                break;
            case 0x3: // Skips the next instruction if register equals value.
                reg = (opcode & 0x0F00) >> 8;
                val = (opcode & 0x00FF);
                skipIfRegisterEqualValue(reg, val);
                operation = "SE V" + reg + ", " + val;
                break;
            case 0x4: // Skips the next instruction if register doesn't equal value.
                reg = (opcode & 0x0F00) >> 8;
                val = (opcode & 0x00FF);
                skipIfRegisterNotEqualValue(reg, val);
                operation = "SNE V" + reg + ", " + val;
                break;
            case 0x5: // Skips the next instruction if register equals register.
                reg1 = (opcode & 0x0F00) >> 8;
                reg2 = (opcode & 0x00F0) >> 4;
                skipIfRegisterEqualRegister(reg1, reg2);
                operation = "SE V" + reg1 + ", V" + reg2;
                break;
            case 0x6: // Sets register to a value.
                reg = (opcode & 0x0F00) >> 8;
                val = (opcode & 0x00FF);
                setRegisterToValue(reg, val);
                operation = "LOAD V" + reg + ", " + val;
                break;
            case 0x7: // Adds value to register.
                reg = (opcode & 0x0F00) >> 8;
                val = (opcode & 0x00FF);
                addValueToRegister(reg, val);
                operation = "ADD V" + reg + ", " + val;
                break;
            case 0x8:
                reg1 = (opcode & 0x0F00) >> 8;
                reg2 = (opcode & 0x00F0) >> 4;
                switch (opcode & 0x000F) {
                    case 0x0: // Sets reg1 to the value of reg2.
                        registerMove(reg1, reg2);
                        operation = "MOV V" + reg1 + ", V" + reg2;
                        break;
                    case 0x1: // Sets reg1 to the value of reg1 or reg2.
                        registerOr(reg1, reg2);
                        operation = "OR V" + reg1 + ", V" + reg2;
                        break;
                    case 0x2: // Sets reg1 to the value of reg1 and reg2.
                        registerAnd(reg1, reg2);
                        operation = "AND V" + reg1 + ", V" + reg2;
                        break;
                    case 0x3: // Sets reg1 to the value of reg1 xor reg2.
                        registerXor(reg1, reg2);
                        operation = "XOR V" + reg1 + ", V" + reg2;
                        break;
                    case 0x4: // Adds reg2 to reg1. VF is set to 1 when there's a carry, and to 0 when there isn't.
                        registerAdd(reg1, reg2);
                        operation = "ADD V" + reg1 + ", V" + reg2;
                        break;
                    case 0x5: // reg2 is subtracted from reg1. VF is set to 0 when there's a borrow, and 1 when there isn't.
                        registerSubN(reg1, reg2);
                        operation = "SUB1 V" + reg1 + ", V" + reg2;
                        break;
                    case 0x6: // Shifts reg1 right by one. VF is set to the value of the least significant bit of reg1 before the shift.
                        registerShiftRight(reg1);
                        operation = "SHR V" + reg1;
                        break;
                    case 0x7: // Sets reg1 to reg2 minus reg1. VF is set to 0 when there's a borrow, and 1 when there isn't.
                        registerSub(reg1, reg2);
                        operation = "SUB2 V" + reg1 + ", V" + reg2;
                        break;
                    case 0xE: // Shifts VX left by one. VF is set to the value of the most significant bit of VX before the shift
                        registerShiftLeft(reg1);
                        operation = "SHL V" + reg1;
                        break;
                    default:
                        System.out.println("Unsupported command: " + String.format("0x%08X", opcode) + " Address: " + String.format("0x%08X", pc));
                        throw new IllegalArgumentException("Unsupported command: " + String.format("0x%08X", opcode) + " Address: " + String.format("0x%08X", pc));
                }
                break;
            case 0x9: // Skips the next instruction if register doesn't equal register.
                reg1 = (opcode & 0x0F00) >> 8;
                reg2 = (opcode & 0x00F0) >> 4;
                skipIfRegisterNotEqualRegister(reg1, reg2);
                operation = "SKNE V" + reg1 + ", V" + reg2;
                break;
            case 0xA: // Sets I to the address.
                location = (opcode & 0x0FFF);
                setIToAddress(location);
                operation = "LOAD I, " + location;
                break;
            case 0xB: // Jumps to the address plus V0.
                location = (opcode & 0x0FFF);
                jumpToAddressPlusV0(location);
                operation = "JUMP V0 + " + location;
                break;
            case 0xC: // Sets register to the result of a bitwise and operation on a random number and value.
                reg = (opcode & 0x0F00) >> 8;
                val = (opcode & 0x00FF);
                setRegisterToRandomValue(reg, val);
                operation = "RAND V" + reg;
                break;
            case 0xD: // Draws a sprite at coordinate (VX, VY) that has a width of 8 pixels and a height of N pixels.
                reg1 = (opcode & 0x0F00) >> 8;
                reg2 = (opcode & 0x00F0) >> 4;
                val = (opcode & 0x000F);
                drawSprite(reg1, reg2, val);
                operation = "DRAW V" + reg1 + ", V" + reg2;
                break;
            case 0xE:
                switch (opcode & 0x00FF) {
                    case 0x9E: // Skips the next instruction if the key stored in register is pressed.
                        reg = (opcode & 0x0F00) >> 8;
                        skipIfKeyPressed(reg);
                        operation = "SKPR V" + reg;
                        break;
                    case 0xA1: // Skips the next instruction if the key stored in register isn't pressed.
                        reg = (opcode & 0x0F00) >> 8;
                        skipIfKeyNotPressed(reg);
                        operation = "SKUP V" + reg;
                        break;
                    default:
                        System.out.println("Unsuported command: " + String.format("0x%08X", opcode) + " Address: " + String.format("0x%08X", pc));
                        throw new IllegalArgumentException("Unsuported command: " + String.format("0x%08X", opcode) + " Address: " + String.format("0x%08X", pc));
                }
                break;
            case 0xF:
                reg = (opcode & 0x0F00) >> 8;
                switch (opcode & 0x00FF) {
                    case 0x07: // Sets register to the value of the delay timer.
                        setRegisterToDelayTimer(reg);
                        operation = "LOAD V" + reg + ", DELAY";
                        break;
                    case 0x0A: // A key press is awaited, and then stored in register.
                        waitForKey(reg);
                        operation = "WAIT V" + reg;
                        break;
                    case 0x15: // Sets the delay timer to register value.
                        setDelayTimer(reg);
                        operation = "LOAD DELAY, V" + reg;
                        break;
                    case 0x18: // Sets the sound timer to register value.
                        setSoundTimer(reg);
                        operation = "LOAD SOUND, V" + reg;
                        break;
                    case 0x1E: // Adds VX to I. [3]<- Wikipedia
                        addRegisterToI(reg);
                        operation = "ADD I, V" + reg;
                        break;
                    case 0x29: // Sets I to the location of the sprite for the character in VX. Characters 0-F (in hexadecimal) are represented by a 4x5 font.
                        loadIWithSprite(reg);
                        operation = "LVS V" + reg;
                        break;
                    case 0x33: // Stores the binary-coded decimal representation of VX, with the most significant of three digits at the address in I, the middle digit at I plus 1, and the least significant digit at I plus 2.
                        storeDecimalInMemory(reg);
                        operation = "BCD V" + reg;
                        break;
                    case 0x55: // Stores V0 to VX (including VX) in memory starting at address I.[4]
                        storeRegistersInMemory(reg);
                        operation = "STOR " + reg;
                        break;
                    case 0x65: // Fills V0 to VX (including VX) with values from memory starting at address I. [4]
                        storeMemoryToRegisters(reg);
                        operation = "READ " + reg;
                        break;
                    default:
                        System.out.println("Unsuported command: " + String.format("0x%08X", opcode) + " Address: " + String.format("0x%08X", pc));
                        throw new IllegalArgumentException("Unsuported command: " + String.format("0x%08X", opcode) + " Address: " + String.format("0x%08X", pc));
                }
                break;
                default:
                    System.out.println("Unsuported command: " + String.format("0x%08X", opcode) + " Address: " + String.format("0x%08X", pc));
                    throw new IllegalArgumentException("Unsuported command: " + String.format("0x%08X", opcode) + " Address: " + String.format("0x%08X", pc));
        }
    }

    /**
     * Clears the screen.
     */
    public void clearScreen() {
        screen.clearScreen();
    }

    /**
     * Returns from subroutine.
     */
    public void returnFromSubrutine() {
        regStack--;
        pc = memory.getByte(regStack) << 8;
        regStack--;
        pc += memory.getByte(regStack);
    }

    /**
     * Jumps to given address location.
     *
     * @param location Location to be jumped to
     */
    public void jumpToAddress(int location) {
        pc = location;
    }

    /**
     * Calls subroutine at given address location.
     *
     * @param location Location subroutine will be called from
     */
    public void callSubroutine(int location) {
        memory.setByte(regStack, (short)(pc & 0x00FF));
        regStack++;
        memory.setByte(regStack, (short) ((pc & 0xFF00) >> 8));
        regStack++;
        pc = location;
    }

    /**
     * Skips the next instruction if register is equal to value.
     *
     * @param reg Number of register
     * @param val Value to be compared to
     */
    public void skipIfRegisterEqualValue(int reg, int val) {
        if (regV[reg] == val) {
            pc += 2;
        }
    }

    /**
     * Skips the next instruction if register is not equal to value.
     *
     * @param reg Number of register
     * @param val Value to be compared to
     */
    public void skipIfRegisterNotEqualValue(int reg, int val) {
        if (regV[reg] != val) {
            pc += 2;
        }
    }

    /**
     * Skips the next instruction if both registers have same value.
     *
     * @param reg1 Number of first register
     * @param reg2 Number of second register
     */
    public void skipIfRegisterEqualRegister(int reg1, int reg2) {
        if (regV[reg1] == regV[reg2]) {
            pc += 2;
        }
    }

    /**
     * Sets register to given value.
     *
     * @param reg Number of register
     * @param val Value to be set to
     */
    public void setRegisterToValue(int reg, int val) {
        regV[reg] = (short)val;
    }

    /**
     * Add given value to the register.
     *
     * @param reg Number of register
     * @param val Value to be added
     */
    public void addValueToRegister(int reg, int val) {
        regV[reg] = (short) ((regV[reg] + val) & 0xFF);
    }

    /**
     * Sets first register to the value of second.
     *
     * @param reg1 First register, stores value
     * @param reg2 Second register
     */
    public void registerMove(int reg1, int reg2) {
        regV[reg1] = regV[reg2];
    }

    /**
     * Calculate value of logical or of two registers.
     * Store result in first.
     *
     * @param reg1 First register, stores value
     * @param reg2 Second register
     */
    public void registerOr(int reg1, int reg2) {
        regV[reg1] |= regV[reg2];
    }

    /**
     * Calculate value of logical and of two registers.
     * Store result in first.
     *
     * @param reg1 First register, stores value
     * @param reg2 Second register
     */
    public void registerAnd(int reg1, int reg2) {
        regV[reg1] &= regV[reg2];
    }

    /**
     * Calculate value of logical xor of two registers.
     * Store result in first register.
     *
     * @param reg1 First register, stores value
     * @param reg2 Second register
     */
    public void registerXor(int reg1, int reg2) {
        regV[reg1] ^= regV[reg2];
    }

    /**
     * Add values of second register to the value of first.
     * Stores result in first register.
     * VF is set to 1 when there's a carry,
     * and to 0 when there isn't.
     *
     * @param reg1 First register number, stores value
     * @param reg2 Second register number
     */
    public void registerAdd(int reg1, int reg2) {
        if (regV[reg1] + regV[reg2] > 0xFF) {
            regV[0xF] = 0x01;
        } else {
            regV[0xF] = 0x00;
        }
        regV[reg1] = (short) ((regV[reg1] + regV[reg2]) & 0xFF);
    }

    /**
     * Subtracts value of second register from the value of first.
     * Stores result in first register.
     * VF is set to 0 when there's a borrow,
     * and 1 when there isn't.
     *
     * @param reg1 First register number, stores value
     * @param reg2 Second register number
     */
    public void registerSubN(int reg1, int reg2) {
        if (regV[reg1] > regV[reg2]) {
            regV[0xF] = 1;
        } else {
            regV[0xF] = 0;
        }
        regV[reg1] = (short) ((regV[reg1] - regV[reg2]) & 0xFF);
    }

    /**
     * Shifts register right by one.
     * VF is set to the value of the least significant
     * bit of VX before the shift.
     *
     * @param reg Number of register to be shifted
     */
    public void registerShiftRight(int reg) {
        regV[0xF] = (short) (regV[reg] & 0x01);
        regV[reg] = (short) (regV[reg] >> 1);
    }

    /**
     * Subtracts value of second register from the value of first.
     * Stores result in first register.
     * VF is set to 1 when there's a borrow,
     * and 0 when there isn't.
     *
     * @param reg1 First register number, stores value
     * @param reg2 Second register number
     */
    public void registerSub(int reg1, int reg2) {
        if (regV[reg1] < regV[reg2]) {
            regV[0xF] = 1;
        } else {
            regV[0xF] = 0;
        }
        regV[reg1] = (short) ((regV[reg1] - regV[reg2]) & 0xFF);
    }

    /**
     * Shifts register left by one.
     * VF is set to the value of the least significant
     * bit of VX before the shift.
     *
     * @param reg Number of register to be shifted
     */
    public void registerShiftLeft(int reg) {
        regV[0xF] = (short) (regV[reg] & 0x80);
        regV[reg] = (short) (regV[reg] << 1);
    }

    /**
     * Skips the next instruction if value of first register
     * is not equal to the value of second.
     *
     * @param reg1 Number of first register
     * @param reg2 Number of second register
     */
    public void skipIfRegisterNotEqualRegister(int reg1, int reg2) {
        if (regV[reg1] != regV[reg2]) {
            pc += 2;
        }
    }

    /**
     * Sets index register to given location.
     *
     * @param location Emulator.Memory location
     */
    public void setIToAddress(int location) {
        regI = location;
    }

    /**
     * Jumps to the given location plus the value
     * of register V0.
     *
     * @param location Base memory location
     */
    public void jumpToAddressPlusV0(int location) {
        pc = (location + regV[0]) & 0x0FFF;
    }

    /**
     * Sets register to the result of a bitwise and
     * operation on a random number and given value.
     *
     * @param reg Number of register
     * @param val Given value
     */
    public void setRegisterToRandomValue(int reg, int val) {
        regV[reg] = (short) (val & random.nextInt(0xFF));
    }

    /**
     * Draws a sprite at coordinate (register 1, register 2) that
     * has a width of 8 pixels and a height given by value.
     * Each row of 8 pixels is read as bit-coded starting from memory location I;.
     * I value does not change after the execution of this instruction.
     * As described above, VF is set to 1 if any screen pixels are flipped
     * from set to unset when the sprite is drawn, and to 0 if that does not happen.
     *
     * @param reg1 Number of register holding first coordinate
     * @param reg2 Number of register holding second coordinate
     * @param val Value of height
     */
    public void drawSprite(int reg1, int reg2, int val) {
        regV[0xF] = 0;

        for (int j = 0; j < val; j++) {

            short colorByte = memory.getByte(regI+ j);
            int y = regV[reg2] + j;
            y = y % screen.getHeight();

            int mask = 0x80;

            for (int i = 0; i < 8; i++) {
                int x = regV[reg1] + i;
                x = x % screen.getWidth();

                boolean turnOn = (colorByte & mask) > 0;
                boolean currentOn = screen.isPixelOn(x, y);

                if (turnOn && currentOn) {
                    regV[0xF] |= 1;
                    turnOn = false;
                } else if (!turnOn && currentOn) {
                    turnOn = true;
                }

                screen.markPixel(x, y, turnOn);
                mask = mask >> 1;
            }
        }
    }

    /**
     * Skips the next instruction if key stored in register is pressed.
     *
     * @param reg Number of register to check for key
     */
    public void skipIfKeyPressed(int reg) {
        if (keyboard.getKeyPressed() == regV[reg]) {
            pc += 2;
        }
    }

    /**
     * Skips the next instruction if key stored in register is not pressed.
     *
     * @param reg Number of register to check for key
     */
    public void skipIfKeyNotPressed(int reg) {
        if (keyboard.getKeyPressed() != regV[reg]) {
            pc += 2;
        }
    }

    /**
     * Sets register value to the value of the delay timer.
     *
     * @param reg Number of register
     */
    public void setRegisterToDelayTimer(int reg) {
        regV[reg] = timerDelay;
    }

    /**
     * Waits for key to be pressed and stops execution of program.
     * Pressed key is stored in register.
     *
     * @param reg Number of register to put key in
     */
    public void waitForKey(int reg) {
        int key = keyboard.getKeyPressed();
        while (key == -1) {
            try {
                Thread.sleep(KEY_DELAY);
            } catch (InterruptedException e) {
                System.out.println("Error while waiting for key");
            }
            key = keyboard.getKeyPressed();
        }

        regV[reg] = (short) key;
    }

    /**
     * Sets the value of delay timer to the
     * value stored in register.
     *
     * @param reg Number of register
     */
    public void setDelayTimer(int reg) {
        timerDelay = regV[reg];
    }

    /**
     * Sets the value of sound timer to the
     * value stored in register.
     *
     * @param reg Number of register
     */
    public void setSoundTimer(int reg) {
        timerSound = regV[reg];
    }

    /**
     * Adds register value to the value of index register.
     * Result is stored in index register.
     *
     * @param reg Number of register
     */
    public void addRegisterToI(int reg) {
        regI = regI + regV[reg];
    }

    /**
     * Sets index register to the location of the sprite for the
     * character in register. Characters 0-F (in hexadecimal)
     * are represented by a 4x5 font.
     *
     * @param reg Number of register
     */
    public void loadIWithSprite(int reg) {
        regI = regV[reg] * 5;
    }

    /**
     * Stores the binary-coded decimal representation of VX,
     * with the most significant of three digits at the address in I,
     * the middle digit at I plus 1,
     * and the least significant digit at I plus 2.
     *
     * @param reg Number of register
     */
    public void storeDecimalInMemory(int reg) {
        memory.setByte(regI, (short) (regV[reg] / 100));
        memory.setByte(regI + 1, (short) ((regV[reg] % 100) / 10));
        memory.setByte(regI + 2, (short) ((regV[reg] % 100) % 10));
    }

    /**
     * Stores values of all V register up to the given one
     * to the memory starting from location in index register.
     *
     * @param reg Number of register
     */
    public void storeRegistersInMemory(int reg) {
        for (int i = 0; i <= reg; i++) {
            memory.setByte(regI + i, regV[i]);
        }
    }

    /**
     * Reads values from location stored in index register
     * and stores it in registers uo to given one.
     *
     * @param reg Number of register
     */
    public void storeMemoryToRegisters(int reg) {
        for (int i = 0; i <= reg; i++) {
            regV[i] = memory.getByte(regI + i);
        }
    }

    /**
     * Runs the code in loop.
     */
    public void run() {
        while(cpuAlive) {
            if (!cpuPaused) {
                executeNextCommand();
                try {
                    sleep(cpuCycleTime);
                } catch (InterruptedException e) {
                    System.out.println("CPU sleep interrupted");
                }
            } else {
                try {
                    sleep(KEY_DELAY);
                } catch (InterruptedException e) {
                    System.out.println("CPU sleep interrupted");
                }
            }
        }
    }
}
