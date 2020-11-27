/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.logic.controllers;

/**
 *
 * @author suominka
 */
public class Magazine {
    float lightMass = 123;
    float mediumMass = 130;
    float heavyMass = 150;
    
    private int[] warheads;
    private float[] warheadWeights = {123f,130f,150f};
    private int cartouches;
    
    public Magazine(int light, int medium, int heavy, int cartouches) {
        warheads = new int[] {light,medium,heavy};
        this.cartouches = cartouches;
    }
    
    public float getWarhead(int index) {
        if (index < 0 || index >= warheads.length) {
            return -1;
        }
        if (warheads[index]>0) {
            warheads[index] -= 1;
            return warheadWeights[index];
        }
        return -1;
    }
    public int getCartouche(int amount) {
        if (cartouches - amount >= 0) {
            cartouches -= amount;
            return amount;
        } else if (cartouches > 0) {
            int ret = cartouches;
            cartouches = 0;
            return ret;
        }
        return 0;
    }
}