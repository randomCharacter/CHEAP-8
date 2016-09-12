/*
 * Copyright (c) 2016 Mario Perić
 *
 * See LICENSE for more info
*/
package Emulator;

import java.awt.*;

/**
 * Class that enables multiple screen colors.
 */
public class ScreenType {

    // Possible screen types
    private static final int BLACK_AND_WHITE = 0;
    private static final int POWER_SHELL = 1;
    private static final int RADAR = 2;
    private static final int INVERTED = 3;

    // Type of screen
    private int type;
    // Color for pixels turned on
    private Color colorOn;
    // Color for pixels turned off
    private Color colorOff;

    /**
     * Default constructor for the class.
     *
     * @param type type of the screen
     */
    public ScreenType (int type) {
        this.type = type;
        setColors();
    }

    /**
     * @return color of pixels turned on
     */
    public Color getColorOn() {
        return colorOn;
    }

    /**
     * @return color of pixels turned off
     */
    public Color getColorOff() {
        return colorOff;
    }

    /**
     * Sets the colors depending on selected type.
     */
    private void setColors() {
        switch (type) {
            case BLACK_AND_WHITE:
                colorOn = Color.white;
                colorOff = Color.black;
                break;
            case POWER_SHELL:
                colorOn = Color.yellow;
                colorOff = Color.blue;
                break;
            case RADAR:
                colorOn = Color.white;
                colorOff = Color.green;
                break;
            case INVERTED:
                colorOn = Color.black;
                colorOff = Color.white;
                break;
            default:
                colorOn = Color.white;
                colorOff = Color.black;
                break;
        }
    }
}
