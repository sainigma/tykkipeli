/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.components.templates;

import game.components.Animator;
import game.components.GameObject;
import game.utils.PID;
import game.utils.Vector3d;

/**
 *
 * @author suominka
 */
public class Mortar extends GameObject {
    private float viewportScale;
    long start = System.currentTimeMillis();
    
    private float elevationMaxSpeed = 10f;
    private float traversalMaxSpeed = 100f;
    private float traversalCoeff = 0.025f;
    private float elevationTarget = 0;
    private float traverseTarget = 0;
    private PID elevationControl = new PID(1f, 0f, 2f, 1f);
    private PID traversalControl = new PID(0.25f, 0f, 0.005f, 1f);
    
    public Animator animator;
    
    private float traversal = 0;
    private float elevationWheelRot = 0;

    private float[] gunLimits = {-22, 65};
    private Vector3d[] cradleLimits = {
        new Vector3d(710, 236),
        new Vector3d(500, 204.5)
    };
    
    private GameObject mount;
    private GameObject gun;
    private GameObject elevationGear;
    private GameObject elevationWheel;
    private GameObject cradle;
    private GameObject craneWheel;
    private GameObject craneGear;
    private GameObject mountGrooves;
    
    public Mortar(String name, float viewportScale) {
        super(name);
        this.viewportScale = viewportScale;
        init();
    }
    
    private void spawnChildren() {
        mount = new GameObject("mortarstand", "mortar/jalusta.png", new Vector3d(0), viewportScale) { };
        mountGrooves = new GameObject("mortargrooves", "mortar/urat.png", new Vector3d(512, 512, 0), viewportScale * 0.58f) { };
        gun = new GameObject("mortartube", "mortar/tykki.png", new Vector3d(512, 512, 1), viewportScale) { };
        cradle = new GameObject("mortarcradle", "mortar/karry.png", new Vector3d(398, 147, -1), viewportScale) { };
        elevationWheel = new GameObject("mortarwheel", "mortar/ruori.png", new Vector3d(128, 128, -3), viewportScale) { };
        elevationGear = new GameObject("mortargear", "mortar/ratas.png", new Vector3d(64, 64, -2), viewportScale) { };
        craneWheel = elevationWheel.clone();
        craneGear = elevationGear.clone();
    }
    private void appendChildren() {
        cradle.append(gun);
        cradle.append(elevationGear);
        cradle.append(elevationWheel);
        mount.append(cradle);
        mount.append(craneWheel);
        mount.append(craneGear);
        mount.append(mountGrooves);
        append(mount);
    }
    
    private void initAnimator() {
        animator = new Animator();
        animator.loadAnimation("mortar/firing");
        animator.bindDriver("cradle", this);
    }
    
    private void init() {   
        spawnChildren();
        appendChildren();
        initAnimator();
        
        elevationWheel.translate(20, 106);
        elevationGear.translate(50, 90);
        cradle.translate(1065f * viewportScale, 355f * viewportScale);
        
        craneWheel.translate(545, 385);
        craneGear.translate(495, 398);
        
        cradle.translate(-210, -0.15f * 210);

        mountGrooves.translate(997 * viewportScale, (730) * viewportScale);
        mountGrooves.setVertexOffset(
                new float[]{235, -30},
                new float[]{-220, 80},
                new float[]{220, 80},
                new float[]{-235, -30}
        );

        //setElevationTarget(60);
        //setTraversal(45);
        setCradle(0);
        //setTraverseTarget(95);
    }
    
    private float getControl(PID controller, float target, float current, float maxSpeed, float coeff) {
        float error = target - current;
        float control = (float) controller.getControl(error, dt);
        if (control > maxSpeed) {
            control = maxSpeed;
        } else if (control < -maxSpeed) { 
            control = -maxSpeed;
        }
        //System.out.println(control);
        if (Math.abs(control) < 0.1f) {
            controller.deactivate();
        }
        return control * coeff;
    }
    
    private void elevate() {
        if (!elevationControl.isActive()) {
            return;
        }
        addElevation(getControl(
                elevationControl,
                elevationTarget,
                getElevation(),
                elevationMaxSpeed,
                1f
        ));
    }
    
    private void traverse() {
        if (!traversalControl.isActive()) {
            return;
        }
        addTraversal(getControl(
                traversalControl,
                traverseTarget,
                getLocalTraversal(),
                traversalMaxSpeed,
                traversalCoeff
        ));
    }
    public void drive(String target, double value) {
        if (target.equals("cradle")) {
            setCradle((float) value);
        }
    }
    
    public float getElevationFactor() {
        return (float) Math.pow(Math.cos(Math.PI * (getElevation() - 10) / 180), 2);
    }
    
    public void setCradle(float t) {
        cradle.setPosition(new Vector3d().lerp(cradleLimits[0], cradleLimits[1], t * getElevationFactor()));
    }
    
    public float getElevation() {
        return -gun.rotation;
    }
    
    public void setElevationTarget(float r) {
        if (r >= gunLimits[0] && r <= gunLimits[1]) {
            elevationTarget = r;
        } else if (r > gunLimits[1]) {
            elevationTarget = gunLimits[1];
        } else if (r < gunLimits[0]) {
            elevationTarget = gunLimits[0];
        }
        if (!elevationControl.isActive()) {
            elevationControl.activate();            
        }

    }
    public void setTraverseTarget(float r) {
        traverseTarget = r;
        if (!traversalControl.isActive()) {
            traversalControl.activate();            
        }
    }

    public void addTraversal(float r) {
        setTraversal(traversal + r);
    }
    private void setTraversal(float r) {
        traversal = r;
        mountGrooves.setTexOffset(getTraversal() / 60, 0);
    }
    public float getLocalTraversal() {
        return traversal;
    }
    public float getTraversal() {
        return traversal % 360;
    }

    public void addElevation(float r) {
        setElevation(-elevationWheelRot + r);
        //System.out.println(elevationTarget + " " + getElevation());
    }
    public void setTrueElevation(float r) {
        float gunRot = -r;
        float gearRot = -gunRot / (12f / 142f);
        elevationWheelRot = -gearRot / (12f / 60f);
        elevationWheel.setRotation(elevationWheelRot);
        elevationGear.setRotation(gearRot);
        gun.setRotation(gunRot);
    }
    private void setElevation(float r) {
        elevationWheelRot = -r;
        float gearRot = -elevationWheelRot * (12f / 60f);
        float gunRot = -gearRot * (12f / 142f);
        if (getElevation() > gunLimits[0] && getElevation() < gunLimits[1]) {
            elevationWheel.setRotation(elevationWheelRot);
            elevationGear.setRotation(gearRot);
            gun.setRotation(gunRot);            
        }
    }
    
    private double dt = 0;
    private long lastTime = System.nanoTime();
    public void update() {
        long time = System.nanoTime();
        dt = (double) (time - lastTime) / 1000000;
        //System.out.println(dt);
        //float t = (float) (Math.cos((System.currentTimeMillis() - start) * 0.001) * 0.5f + 0.5f);
        //setCradle(t);
        elevate();
        traverse();
        animator.animate(dt);
        lastTime = time;
    }
    public void forcedUpdate(double deltatime) {
        dt = deltatime;
        elevate();
        traverse();
    }

    public void addToElevationTarget(float f) {
        setElevationTarget(elevationTarget + f);
    }

    public void addToTraverseTarget(float f) {
        setTraverseTarget(traverseTarget + f);
    }
}
