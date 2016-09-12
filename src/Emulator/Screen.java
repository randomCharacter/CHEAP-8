/*
 * Copyright (c) 2016 Mario Perić
 *
 * See LICENSE for more info
*/
package Emulator;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Emulates screen of CHIP-8.
 *
 * Created by random on 16.08.2016.
 */
public class Screen implements IScreen {

    // Default height of the screen in pixels
    private static final int DEFAULT_HEIGHT = 32;
    // Default width of the screen in pixels
    private static final int DEFAULT_WIDTH = 64;
    // Default scale factor
    protected static final int DEFAULT_SCALE = 4;
    // Default color for pixels turned on
    private static final Color DEFAULT_COLOR_ON = Color.white;
    // Default color for pixels turned off
    private static final Color DEFAULT_COLOR_OFF = Color.black;

    // Scale factor of the screen
    private int scale;
    // Width of the screen in pixels
    private int height;
    // Height of the screen in pixels
    private int width;
    // Color of pixels turned on
    private Color colorOn;
    // Color of pixels turned off
    private Color colorOff;
    // Emulator.Screen image information
    private BufferedImage image;

    /**
     * Main constructor. If any value is wrong default value will be applied.
     *
     * @param scale The scale of the screen, must be greater then 1,
     *              0 for default
     * @param width The width of the screen, must be >= default value,
     *              0 for default
     * @param height The height of the screen, must be >= default value,
     *               0 for default
     * @param colorOn The color of pixels turned on, null for default
     * @param colorOff The color of pixels turned off, null for default
     */
    public Screen(int scale, int width, int height, Color colorOn, Color colorOff) {
        this.scale = scale < DEFAULT_SCALE ? DEFAULT_SCALE : scale;
        this.width = width < DEFAULT_WIDTH ? DEFAULT_WIDTH : width;
        this.height = height < DEFAULT_HEIGHT ? DEFAULT_HEIGHT : height;
        this.colorOn = colorOn == null ? DEFAULT_COLOR_ON : colorOn;
        this.colorOff = colorOff == null ? DEFAULT_COLOR_OFF : colorOff;

        image = new BufferedImage(width * scale, height * scale, BufferedImage.TYPE_4BYTE_ABGR);
    }

    /**
     * Default constructor.
     *
     * @param scale The scale of the screen, must be greater then 1,
     *              0 for default
     * @param type Type of the screen
     */
    public Screen(int scale, ScreenType type) {
        this(scale,DEFAULT_WIDTH, DEFAULT_HEIGHT, type.getColorOn(), type.getColorOff());
    }

    /**
     * Clears the screen
     */
    public void clearScreen() {
        Graphics2D graphics2D = image.createGraphics();
        graphics2D.setColor(colorOff);
        graphics2D.fillRect(0, 0, width * scale, height * scale);
        graphics2D.dispose();
    }

    /**
     * Turns the pixel with given coordinates on or off
     *
     * @param x The x coordinate of pixel
     * @param y The y coordinate of pixel
     * @param on If <code>true</code> pixel is turned on,
     *           otherwise it is turned off
     */
    public void markPixel(int x, int y, boolean on) {
        Graphics2D graphics2D = image.createGraphics();
        Color color = on ? colorOn : colorOff;
        graphics2D.setColor(color);
        graphics2D.fillRect(x * scale, y * scale, scale, scale);
        graphics2D.dispose();
    }

    /**
     * @return The height of the screen
     */
    public int getHeight() {
        return this.height;
    }

    /**
     * @return The width of the screen
     */
    public int getWidth() {
        return this.width;
    }

    /**
     * @return The scale of the screen
     */
    public int getScale() {
        return this.scale;
    }

    /**
     * @return The BufferedImage of the screen
     */
    public BufferedImage getImage() {
        return this.image;
    }

    /**
     * Checks if pixel on given coordinates is turned on,
     *
     * @param x The x coordinate of pixel
     * @param y The y coordinate of pixel
     * @return <code>true</code> if pixel is set to on,
     *          <code>false</code> if it is set to off
     */
    public boolean isPixelOn(int x, int y) {
        return new Color(image.getRGB(x * scale, y * scale), true).equals(colorOn);
    }
}
