/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.logic.controllers;

import game.components.Text;
import game.utils.InputManager;
import game.components.templates.Mortar;
import game.components.templates.ReloadScreen;

/**
 *
 * @author suominka
 */
public class ReloadLogic {
    private Magazine magazine;
    private String[] warheads = {"light", "medium", "heavy"};
    private int[] cartouches = {3, 2, 1};
    
    private int reloadIndex = 0;
    private int prevReloadIndex = 0;
    private boolean allowRoll = true;
    private int prevWarhead = 0;
    private int prevCartouche = 2;
    private boolean firstSelected = false;
    
    private boolean reloadUpdate = true;
    private boolean reloadFinished = true;
    private boolean blockMovement = false;
    
    private Text messenger;
    
    Projectile currentProjectile;
    InputManager inputs = null;
    MortarLogic mortarLogic;
    ReloadScreen reloadScreen;
    Mortar mortar;
    
    public ReloadLogic(MortarLogic mortarLogic, Mortar mortar, ReloadScreen reloadScreen) {
        this.mortarLogic = mortarLogic;
        this.reloadScreen = reloadScreen;
        this.mortar = mortar;
        this.magazine = new Magazine(12, 6, 3, 54);
    }
    
    public void setMessenger(Text messenger) {
        this.messenger = messenger;
    }
    
    private void setMessage(String message) {
        if (messenger == null) {
            return;
        }
        messenger.setContent(message);
    }
    
    private void displayGrenadeStatus() {
        setMessage("Valitse kranaatti\n" + magazine.warHeadStatus());
    }
    
    private void displayChargeStatus() {
        setMessage("Valitse latauspanos\n" + magazine.chargeStatus());
    }
    
    public void setMagazine(int light, int medium, int heavy, int charges) {
        this.magazine = new Magazine(light, medium, heavy, charges);
    }
    
    public Magazine getMagazine() {
        return magazine;
    }
    private int selectorDirection;
    private void reloadSelector(int length) {
        if (inputs.keyDownOnce("left")) {
            reloadIndex -= 1;
            reloadUpdate = true;
            selectorDirection = -1;
        } else if (inputs.keyDownOnce("right")) {
            reloadIndex += 1;
            reloadUpdate = true;
            selectorDirection = 1;
        }
        if (allowRoll) {
            rollReloadIndex(length);
        } else {
            if (reloadIndex >= length) {
                reloadIndex = length - 1;
            } else if (reloadIndex < 0) {
                reloadIndex = 0;
            }
        }
    }
    
    private void rollReloadIndex(int length) {
        if (reloadIndex >= length) {
            reloadIndex = 0;
        } else if (reloadIndex < 0) {
            reloadIndex = length - 1;
        }                    
    }
    
    private void activateSelector() {
        if (!firstSelected && (inputs.keyDownOnce("left") || inputs.keyDownOnce("right"))) {
            firstSelected = true;
            reloadScreen.setWarhead(reloadIndex);
        }
    }
    
    private void chooseProjectile() {
        if (reloadUpdate) {
            while (!magazine.warheadAvailable(reloadIndex)) {
                reloadIndex += selectorDirection;
                rollReloadIndex(warheads.length);
            }
            reloadUpdate = false;
            reloadScreen.setWarhead(reloadIndex);
        }
        activateSelector();
        reloadSelector(warheads.length);
        if (inputs.keyDownOnce("ok") && firstSelected) {
            confirm(false);
        }
        if (inputs.keyDownOnce("previous")) {
            cancel(true);
        }
    }
    
    private void confirm(boolean finishReload) {
        if (!finishReload) {
            currentProjectile = new Projectile(magazine.getWarhead(reloadIndex), 0);
            if (!currentProjectile.initOk()) {
                currentProjectile = null;
            }
            prevWarhead = reloadIndex;
            reloadIndex = prevCartouche;
            allowRoll = false;
            reloadUpdate = true;
        } else {
            currentProjectile.addCartouches(magazine.getCartouche(cartouches[reloadIndex]));
            reloadScreen.exit();
            prevCartouche = reloadIndex;
            reload();
        }
    }
    
    private void cancel(boolean cancelReload) {
        if (cancelReload) {
            setMessage("");
            reset();
        } else {
            prevCartouche = reloadIndex;
            magazine.addWarhead(prevWarhead);
            reloadScreen.setCharges(0);
            currentProjectile = null;
            allowRoll = true;
            displayGrenadeStatus();            
        }
    }
    
    private void chooseCartouches() {
        if (reloadUpdate) {
            while (cartouches[reloadIndex] > magazine.cartouchesLeft() && reloadIndex < 3) {
                reloadIndex++;
            }
            reloadUpdate = false;
            reloadScreen.setCharges(3 - reloadIndex);
            displayChargeStatus();       
        }
        reloadSelector(cartouches.length);
        
        if (inputs.keyDownOnce("ok")) {
            confirm(true);
        }
        if (inputs.keyDownOnce("previous")) {
            cancel(false);
        }
    }
    
    private void reload() {
        if (mortarLogic.addProjectile(currentProjectile)) {
            setMessage("Tulivalmis!");
            System.out.println(magazine);
            reloadFinished = true;
            blockMovement = false;
            mortar.setInclinometer(true);
        }
    }
    
    public boolean isReloadFinished() {
        return reloadFinished;
    }
    
    public void resetProjectile() {
        currentProjectile = null;
    }
    
    public Projectile getProjectile() {
        return currentProjectile;
    }
    
    public void setInputManager(InputManager inputs) {
        this.inputs = inputs;
    }
    
    public boolean isMovementBlocked() {
        return blockMovement;
    }
    public boolean isEmpty() {
        return magazine.isEmpty();
    }
    private boolean clearForReload() {
        if (currentProjectile != null && reloadFinished) {
            setMessage("Tykki on jo ladattu!");
            return false;
        } else if (!reloadFinished) {
            return false;
        }
        if (magazine.isEmpty()) {
            blockMovement = false;
            return false;
        }
        return true;
    }
    public void startReload() {
        if (clearForReload()) {
            reloadScreen.enter();
            reloadScreen.setCharges(0);
            reloadScreen.setWarhead(3);

            firstSelected = false;
            allowRoll = true;
            displayGrenadeStatus();
            reloadUpdate = false;
            blockMovement = true;
            reloadFinished = false;
            mortar.setElevationTarget(0f);
            mortar.setInclinometer(false);

            reloadIndex = prevWarhead;   
        }
    }
    
    public void reloadControls() {
        if (inputs.keyDownOnce("reload")) {
            startReload();
        } else if (!blockMovement) {
            return;
        }
        if (currentProjectile == null) {
            chooseProjectile();
        } else if (!reloadFinished) {
            chooseCartouches();
        }
    }
    
    public void reset() {
        blockMovement = false;
        currentProjectile = null;
        reloadFinished = true;
        reloadScreen.exit();
    }
    
    public void setProjectile(int warhead, int cartouches) {
        float weight = magazine.getWarhead(warhead);
        cartouches = magazine.getCartouche(cartouches);
        if (weight > 0 && cartouches > 0) {
            currentProjectile = new Projectile(weight, cartouches);            
        }
    }
}
