/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.components;

import game.components.templates.BackgroundCoast;
import game.components.templates.MapScreen;
import game.components.templates.ViewPort;
import game.components.templates.Mortar;
import game.components.templates.ReloadScreen;
/**
 * GameObject luokan implementaatio pelin päänäkymien hallitsemiseen. Sekä kartta- että pelinäkymän juuri.
 * @author suominka
 */
public class Level extends GameObject {
    long start = System.currentTimeMillis();
    
    public ViewPort gameView;
    public ViewPort mapView;
    public Mortar mortar;
    public MapScreen mapScreen;
    public ReloadScreen reloadScreen;
    public GameObject background;
    
    private boolean isFinished = false;
    private float viewportScale = 720f / 1080f;    
    public Level(String name, float viewportScale) {
        super(name);
        this.viewportScale = viewportScale;
        System.out.println(this.viewportScale);
        init();
    }
    
    public boolean isFinished() {
        return isFinished;
    }
    
    /**
     * Alustaa pelinäkymän, tykin ja taustan. Kutsutaan vain ensimmäisellä luontikerralla.
     */
    private void init() {
        initGameView();
        initMapView();

        mortar.setTrueElevation(0f);
        gameView.setVisible(true);
        mapView.setMinimized(true);
    }
    private void initGameView() {
        gameView = new ViewPort("game");        
        mortar = new Mortar("mortar", viewportScale);
        background = new BackgroundCoast("coast", 1);
        reloadScreen = new ReloadScreen("reloadScreen", viewportScale);
        
        gameView.append(mortar);
        gameView.append(background);
        gameView.append(reloadScreen);
        append(gameView);
    }
    /**
     * Alustaa karttanäkymän, kutsutaan joka kerta kun kenttä vaihtuu.
     */
    private void initMapView() {
        mapView = new ViewPort("map");
        mapScreen = new MapScreen("mapScreen", viewportScale);
        mapView.append(mapScreen);
        append(mapView);        
    }
    
    /**
     * Poistaa nykyisen karttanäkymän piirtojonosta ja alustaa uuden tilalle.
     * Kenttien vaihtamiseen tarkoitettu metodi.
     */
    public void reset() {
        remove(mapView);
        initMapView();
    }
    
    @Override
    public void update() {
        background.setRotation(mortar.getTraversal());
    }
}
