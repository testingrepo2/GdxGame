package com.gdx.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.gdx.game.GdxGame;
import com.gdx.game.manager.ResourceManager;

public class MinimapScreen extends BaseScreen {

    private Rectangle overall_viewport, main_viewport, minimap_viewport, detail_viewport;
    private OrthographicCamera overall_cam, main_cam, minimap_cam, detail_cam;
    // Window w/h, level w/h, main camera (viewport) w, minimap (viewport) h. All units in pixels.
    float w, h, main_width, minimap_height, minimap_width;
    float level_width = 100;
    float level_height = 100;

    public MinimapScreen (GdxGame gdxGame, ResourceManager resourceManager) {
        super(gdxGame, resourceManager);

        minimap_height = Gdx.graphics.getHeight() * 10;
        minimap_width = Gdx.graphics.getWidth() * 10;
        minimap_viewport = new Rectangle(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0, 0);
        minimap_cam = new OrthographicCamera(level_width*5, level_height*5);
        minimap_cam.translate(level_width / 2.0f, level_height / 2.0f, 0.0f);
    }

    @Override public void render (float delta) {
        minimap_cam.update();
/*
        GL20 gl = Gdx.graphics.getGL20();
        gl.glClearColor(1, 1, 1, 1);
        gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Draw minimap sprites
        gl.glViewport((int) minimap_viewport.x, (int) minimap_viewport.y,
                (int) minimap_viewport.width, (int) minimap_viewport.height);*/
        gdxGame.getGameScreen().drawGame(minimap_cam);
    }

    @Override
    public void resize (int width, int height) {
        // reconfig minimap viewport/ cam
        minimap_height = (w - main_width) * (level_height / level_width);
        minimap_viewport.x = main_width;
        minimap_viewport.y = h - minimap_height;
        minimap_viewport.width = w - main_width;
        minimap_viewport.height = minimap_height;
        //minimap_cam = new OrthographicCamera(level_width, level_height);
        minimap_cam.position.scl(0.0f);
        minimap_cam.translate(level_width / 2.0f, level_height / 2.0f, 0.0f);
    }
}
