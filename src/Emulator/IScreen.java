/*
 * Copyright (c) 2016 Mario Perić
 *
 * See LICENSE for more info
*/
package Emulator;

public interface IScreen {

    void clearScreen();

    void markPixel(int x, int y, boolean on);

    int getHeight();
    int getWidth();
    int getScale();

}
