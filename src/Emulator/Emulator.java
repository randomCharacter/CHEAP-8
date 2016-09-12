/*
 * Copyright (c) 2016 Mario Perić
 *
 * See LICENSE for more info
*/
package Emulator;

import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Emulator.Emulator class.
 */
public class Emulator {

    // Name of emulator
    private static final String EMULATOR_TITTLE = "CHEAP-8";
    // Number of buffers to use
    private static final int BUFFER_NUMBER = 2;

    // Emulated CPU
    private Cpu cpu;
    // Emulated screen
    private Screen screen;
    // Canvas
    private Canvas canvas;
    // Frame that contains the image
    private JFrame frame;

    /**
     * Builder class for an emulator object.
     */
    public static class Builder {
        // Emulator.Screen scaling
        private int scale;
        // Path to the ROM file
        private String rom;
        // Time of one cycle in milliseconds
        private int cycleTime;
        // Type of the screen
        private int screenType;

        /**
         * Default constructor.
         */
        public Builder() {
            this.scale = Screen.DEFAULT_SCALE;
            this.rom = null;
            cycleTime = Cpu.DEFAULT_CYCLE_TIME;
        }

        /**
         * Sets scale of the Emulator.Emulator.
         *
         * @param scale scale to be set
         * @return the Builder for the Emulator.Emulator
         */
        public Builder setScale(int scale) {
            this.scale = scale;
            return this;
        }

        /**
         * Sets the ROM of the Emulator.Emulator.
         *
         * @param rom path to the ROM on disk
         * @return the Builder for the Emulator.Emulator
         */
        public Builder setRom(String rom) {
            this.rom = rom;
            return this;
        }

        /**
         * Sets the cycle time of the Emulator.Emulator.
         *
         * @param cycleTime the cycle time to be set
         * @return the Builder for the Emulator.Emulator
         */
        public Builder setCycleTime(int cycleTime) {
            this.cycleTime = cycleTime;
            return this;
        }

        /**
         * Sets the type of the screen.
         *
         * @param type type of the screen
         * @return the Builder for the Emulator.Emulator
         */
        public Builder setScreenType(int type) {
            this.screenType = type;
            return this;
        }

        /**
         * Builds Emulator.Emulator.
         *
         * @return new Emulator.Emulator object
         */
        public Emulator build() {
            return new Emulator(this);
        }
    }

    /**
     * Default Emulator.Emulator constructor.
     *
     * @param builder Instance of builder class
     */
    private Emulator (Builder builder) {
        Keyboard keyboard = new Keyboard();
        Memory memory = new Memory();
        ScreenType screenType = new ScreenType(builder.screenType);
        screen = new Screen(builder.scale, screenType);

        cpu = new Cpu(memory, screen, keyboard);
        cpu.setCpuCycleTime(builder.cycleTime);

        if (builder.rom != null) {
            if (!memory.loadRom(builder.rom)) {
                System.out.println("Error loading ROM");
                System.exit(2);
            }
        } else {
            cpu.setCpuPaused(true);
        }

        initEmulator(screen);
        canvas.addKeyListener(keyboard);
    }

    /**
     * Starts the emulation.
     */
    public void start() {
        cpu.start();
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            public void run() {
                refreshScreen();
            }
        };
        timer.scheduleAtFixedRate(task, 0, 33);
    }

    /**
     * Puts updated graphics to the screen.
     */
    public void refreshScreen() {
        Graphics2D graphics;
        if (canvas.getBufferStrategy() == null) {
            throw new NullPointerException("No graphics");
        } else {
            graphics = (Graphics2D) canvas.getBufferStrategy().getDrawGraphics();
        }
        graphics.drawImage(screen.getImage(), null, 0, 0);
        graphics.dispose();
        canvas.getBufferStrategy().show();
    }

    /**
     * Initializes emulator frame.
     *
     * @param screen Emulated screen to be used for emulation
     */
    public void initEmulator(Screen screen) {
        frame = new JFrame(EMULATOR_TITTLE);

        int width = screen.getWidth() * screen.getScale();
        int height = screen.getHeight() * screen.getScale();

        JPanel panel = (JPanel) frame.getContentPane();
        panel.setPreferredSize(new Dimension(width, height));
        panel.setLayout(null);

        canvas = new Canvas();
        canvas.setBounds(0, 0, width, height);
        canvas.setIgnoreRepaint(true);

        panel.add(canvas);

        frame.setMenuBar(null);
        frame.pack();
        frame.setResizable(false);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        canvas.createBufferStrategy(BUFFER_NUMBER);
        canvas.setFocusable(true);
        canvas.requestFocus();
    }



}
