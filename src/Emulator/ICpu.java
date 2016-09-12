/*
 * Copyright (c) 2016 Mario Perić
 *
 * See LICENSE for more info
*/
package Emulator;

interface ICpu {

    void executeNextCommand();
    void execute(int opcode);

    // 0x0XXX
    void clearScreen();
    void returnFromSubrutine();

    // 0x1XXX
    void jumpToAddress(int location);

    // 0x2XXX
    void callSubroutine(int location);

    // 0x3XXX
    void skipIfRegisterEqualValue(int reg, int val);

    // 0x4XXX
    void skipIfRegisterNotEqualValue(int reg, int val);

    // 0x5XXX
    void skipIfRegisterEqualRegister(int reg1, int reg2);

    // 0x6XXX
    void setRegisterToValue(int reg, int val);

    // 0x7XXX
    void addValueToRegister(int reg, int val);

    // 0x8XXX
    void registerMove(int reg1, int reg2);
    void registerOr(int reg1, int reg2);
    void registerAnd(int reg1, int reg2);
    void registerXor(int reg1, int reg2);
    void registerAdd(int reg1, int reg2);
    void registerSubN(int reg1, int reg2);
    void registerShiftRight(int reg1);
    void registerSub(int reg1, int reg2);
    void registerShiftLeft(int reg1);

    // 0x9XXX
    void skipIfRegisterNotEqualRegister(int reg1, int reg2);

    // 0xAXXX
    void setIToAddress(int location);

    // 0xBXXX
    void jumpToAddressPlusV0(int location);

    // 0xCXXX
    void setRegisterToRandomValue(int reg, int val);

    // 0xDXXX
    void drawSprite(int reg1, int reg2, int val);

    // 0xEXXX
    void skipIfKeyPressed(int reg);
    void skipIfKeyNotPressed(int reg);

    // 0xFXXX
    void setRegisterToDelayTimer(int reg);
    void waitForKey(int reg);
    void setDelayTimer(int reg);
    void setSoundTimer(int reg);
    void addRegisterToI(int reg);
    void loadIWithSprite(int reg);
    void storeDecimalInMemory(int reg);
    void storeRegistersInMemory(int reg);
    void storeMemoryToRegisters(int reg);

}
