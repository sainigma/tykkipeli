/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.components.animation;

import game.components.animation.Clip;
import game.components.animation.Frame;
import game.utils.JSONLoader;
import java.util.HashMap;
import org.json.*;

/**
 *
 * @author suominka
 */
public class Animation {
    
    private String name;
    private double framerate = 60;
    private int length = 0;
    private boolean linearInterpolation = false;
    private int currentFrame = 0;
    private boolean playing = false;
    private boolean loop = false;
    private HashMap<String, Clip> driverClips;
    
    public Animation(String path) {
        driverClips = new HashMap<>();
        name = path;
        loadAnimation(path);
    }
    
    private void loadAnimation(String path) {
        JSONObject obj = new JSONLoader("assets/animations/").read(path);

        framerate = obj.getInt("framerate");
        length = obj.getInt("length") + 1;
        linearInterpolation = obj.getString("interpolation").equals("linear");

        JSONObject drivers = obj.getJSONObject("drivers");
        loadClips(driverClips, drivers);
    }    
    
    private void loadClips(HashMap clips, JSONObject data) {
        for (String key : data.keySet()) {
            Clip clip = new Clip();
            clip.setLinearInterpolation(linearInterpolation);
            driverClips.put(key, clip);
            JSONArray frames = data.getJSONArray(key);
            for (Object frame : frames) {
                JSONArray framearr = (JSONArray) frame;
                if (framearr.length() == 2) {
                    Frame newFrame = new Frame(framearr.getInt(0), framearr.getDouble(1));
                    clip.addFrame(newFrame);
                } else if (framearr.length() == 3) {
                    float values[] = {framearr.getFloat(1), framearr.getFloat(2)};
                    Frame newFrame = new Frame(framearr.getInt(0), values);
                    clip.addFrame(newFrame);
                }
            }
        }
    }
    
    public Frame getDriverFrame(String name) {
        if (!driverClips.containsKey(name)) {
            return null;
        }
        Clip clip = driverClips.get(name);
        return clip.getFrame(currentFrame);
    }
    
    public void advance() {
        currentFrame += 1;
        if (currentFrame >= length) {
            if (loop) {
                reset();
            } else {
                stop();
            }
        }
    }
    
    public void play() {
        reset();
        playing = true;
        loop = false;
    }
    public void playForever() {
        playing = true;
        loop = true;
    }
    
    public void pause() {
        playing = false;
    }
    
    public void stop() {
        playing = false;
        currentFrame = 0;
    }
    
    private void resetClips(HashMap<String, Clip> clips) {
        for (String key : clips.keySet()) {
            clips.get(key).reset();
        }
    }
    
    public void reset() {
        currentFrame = 0;
        resetClips(driverClips);
    }
    
    public boolean isPlaying() {
        return playing;
    }
}