package com.gdx.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.HdpiUtils;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.gdx.game.GdxGame;
import com.gdx.game.manager.ResourceManager;

public class MinimapScreen extends BaseScreen {

    private static final String TAG = MinimapScreen.class.getSimpleName();
    private float camMoveSpeed = 10;
    private final static float SCALE = 32f;
    private final static float INV_SCALE = 1.f/SCALE;
    private final static float VP_WIDTH = 1280 * INV_SCALE;
    private final static float VP_HEIGHT = 720 * INV_SCALE;
    private final OrthogonalTiledMapRenderer miniRenderer;
    private ExtendViewport gameViewport = new ExtendViewport(VP_WIDTH, VP_HEIGHT, gdxGame.getGameScreen().gameCam);
    private ShapeRenderer renderer = new ShapeRenderer();
    private Vector3 centre = new Vector3();
    private Vector2 mapSize = new Vector2();
    private OrthographicCamera gameCamera = gdxGame.getGameScreen().gameCam;

    private OrthographicCamera miniCamera;
    private MiniMapViewport miniViewport;
    private Vector2 miniSize = new Vector2();

    public MinimapScreen (GdxGame gdxGame, ResourceManager resourceManager) {
        super(gdxGame, resourceManager);

        float aspect = mapSize.x/mapSize.y;
        miniSize.set(aspect, 1).scl(4);
        Vector2 normMiniSize = new Vector2();
        normMiniSize.set(miniSize).scl(1/30f);
        miniCamera = new OrthographicCamera();
        miniViewport = new MiniMapViewport(miniSize.x, miniSize.y, miniCamera);
        miniViewport.setMiniMapBounds(1-normMiniSize.x, 0f, normMiniSize.x, normMiniSize.y);
        miniRenderer = new OrthogonalTiledMapRenderer(gdxGame.getGameScreen().getIsland() , miniSize.x/mapSize.x * INV_SCALE, gdxGame.getBatch());
    }

    @Override public void render (float delta) {
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        processInput(delta);
        gameViewport.apply();
        renderer.setProjectionMatrix(gameCamera.combined);
        renderer.begin(ShapeRenderer.ShapeType.Filled);
        renderer.setColor(Color.MAGENTA);
        renderer.rect(gameCamera.position.x-.5f, gameCamera.position.y-.5f, 1, 1);
        renderer.end();
        renderMiniMap(delta);
    }

    private void renderMiniMap (float delta) {
        Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
        miniViewport.apply();
        HdpiUtils.glScissor(miniViewport.getScreenX(), miniViewport.getScreenY(), miniViewport.getScreenWidth(), miniViewport.getScreenHeight());
        miniCamera.position.set(miniViewport.getWorldWidth()/2, miniViewport.getWorldHeight()/2, 0);
        miniCamera.update();
        Gdx.gl.glClearColor(0, 1, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        miniRenderer.setView(miniCamera);
        miniRenderer.render();
        float width = gameViewport.getWorldWidth() * gameCamera.zoom;
        float height = gameViewport.getWorldHeight() * gameCamera.zoom;
        float x = gameCamera.position.x -width/2;
        float y = gameCamera.position.y -height/2;
        renderer.setProjectionMatrix(miniCamera.combined);
        renderer.begin(ShapeRenderer.ShapeType.Line);
        renderer.setColor(Color.MAGENTA);
        float unit = miniSize.x/mapSize.x;
        float cx = gameCamera.position.x ;
        float cy = gameCamera.position.y;
        renderer.rect(cx * unit - unit/2, cy * unit - unit/2, unit, unit);
        renderer.setColor(Color.CYAN);

        renderer.rect(x * unit, y * unit, width * unit, height * unit);
        renderer.end();
        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
    }

    private void processInput (float delta) {
        float scale = 1;
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
            scale = 5;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) {
            gameCamera.position.y += camMoveSpeed * delta * scale;
        } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) {
            gameCamera.position.y -= camMoveSpeed * delta * scale;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
            gameCamera.position.x -= camMoveSpeed * delta * scale;
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            gameCamera.position.x += camMoveSpeed * delta * scale;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
            gameCamera.zoom = MathUtils.clamp(gameCamera.zoom + gameCamera.zoom*0.01f, 0.1f, 3f);
        } else if (Gdx.input.isKeyPressed(Input.Keys.E)) {
            gameCamera.zoom = MathUtils.clamp(gameCamera.zoom - gameCamera.zoom*0.01f, 0.1f, 3f);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            Gdx.app.log(TAG, "Reset");
            gameCamera.position.set(centre);
            gameCamera.zoom = 1;
        }
        gameCamera.update();
    }

    @Override
    public void resize (int width, int height) {
        super.resize(width, height);
        miniViewport.update(width, height, true);
    }

    protected static class MiniMapViewport extends Viewport {
        private Rectangle scale = new Rectangle();
        public MiniMapViewport (float worldWidth, float worldHeight, Camera camera) {
            setWorldSize(worldWidth, worldHeight);
            setCamera(camera);
            scale.set(0, 0, worldWidth * 0.1f, worldWidth * 0.1f);
        }

        public void setMiniMapBounds (float x, float y, float width, float height) {
            scale.set(x, y, width, height);
        }

        @Override
        public void update (int screenWidth, int screenHeight, boolean centerCamera) {
            // TODO this is garbage
            float aspect = screenWidth/(float)screenHeight;
            int viewportWidth = Math.round(screenWidth * scale.width);
            int viewportHeight = Math.round(screenHeight * scale.height * aspect);
            int viewportX = Math.round(screenWidth * scale.x);
            int viewportY = Math.round(screenHeight * scale.y * aspect);

            setScreenBounds(viewportX, viewportY, viewportWidth, viewportHeight);
            apply(centerCamera);
        }
    }
}
