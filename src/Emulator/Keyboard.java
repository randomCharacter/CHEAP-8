/*
 * Copyright (c) 2016 Mario Perić
 *
 * See LICENSE for more info
*/
package Emulator;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Emulates the keyboard of CHIP-8.
 *
 * Created by random on 16.08.2016.
 */
public class Keyboard extends KeyAdapter implements IKeyboard {

    // Quit key
    private static final int KEY_QUIT = KeyEvent.VK_ESCAPE;

    //Emulator.Keyboard mapped
    private static final int[] keyMap = {
            KeyEvent.VK_X, // 0
            KeyEvent.VK_1, // 1
            KeyEvent.VK_2, // 2
            KeyEvent.VK_3, // 3
            KeyEvent.VK_Q, // 4
            KeyEvent.VK_W, // 5
            KeyEvent.VK_E, // 6
            KeyEvent.VK_A, // 7
            KeyEvent.VK_S, // 8
            KeyEvent.VK_D, // 9
            KeyEvent.VK_Z, // A
            KeyEvent.VK_C, // B
            KeyEvent.VK_4, // C
            KeyEvent.VK_R, // D
            KeyEvent.VK_F, // D
            KeyEvent.VK_V // D
    };

    // Pressed key, -1 if none
    private int keyPressed = -1;

    /**
     * Maps keys from physical keyboard to virtual one.
     *
     * @param key Physical pressed key
     * @return Virtual pressed key
     */
    private int mapKey(int key) {
        for (int i = 0; i < keyMap.length; i++) {
            if (keyMap[i] == key) {
                return i;
            }
        }
        return -1;
    }

    /**
     * @return Currently pressed key
     */
    public int getKeyPressed() {
        return keyPressed;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        super.keyPressed(e);

        if (e.getKeyCode() == KEY_QUIT) {
            System.exit(0);
        }

        keyPressed = mapKey(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        super.keyReleased(e);

        keyPressed = -1;
    }
}
