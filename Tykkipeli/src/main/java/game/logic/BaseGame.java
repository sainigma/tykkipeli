/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.logic;

import game.components.Level;
import game.utils.InputManager;
import game.utils.Renderer;

/**
 *
 * @author suominka
 */
public class BaseGame {
    Level level = null;
    InputManager inputs = null;
    Renderer renderer = null;
    
    public BaseGame(Level level) {
        this.level = level;
        this.renderer = renderer;
    }
    
    public void setRenderer(Renderer renderer) {
        this.renderer = renderer;
    }
    
    public void setInputManager(InputManager inputs) {
        this.inputs = inputs;
    }
    
    private long lastTime = System.nanoTime();
    private double deltatimeMillis;
    public void update() {
        long time = System.nanoTime() / 1000000;
        deltatimeMillis = (double) (time - lastTime);
        lastTime = time;
        if (inputs == null) {
            return;
        }
        float framerateCoeff = (float) (deltatimeMillis / 16f); //1 when 60fps
        float speedModifier = framerateCoeff;
        if (inputs.keyDown("hasten")) {
            speedModifier *= 2f;
        } else if (inputs.keyDown("dawdle")) {
            speedModifier *= 0.5f;
        }
        if (inputs.keyDown("gunUp")) {
            level.mortar.addToElevationTarget(0.1f * speedModifier);
        } else if (inputs.keyDown("gunDown")) {
            level.mortar.addToElevationTarget(-0.1f * speedModifier);
        }
    }
}