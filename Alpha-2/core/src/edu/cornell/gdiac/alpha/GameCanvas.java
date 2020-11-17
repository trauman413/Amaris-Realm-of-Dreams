package edu.cornell.gdiac.alpha;

import static com.badlogic.gdx.Gdx.gl20;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.*;

/**
 * Primary view class for the game, abstracting the basic graphics calls.
 *
 * This version of GameCanvas combines both 3D and 2D drawing.  As this combination
 * is complicated, and we want to hide the details, we make a lot of design decisions
 * in this canvas that are not ideal.  Do not use this canvas as an example of good
 * architecture design.
 */
public class GameCanvas {

    /** Canvas background image. */
    private Texture background;
    /** Font object for displaying images */
    private BitmapFont displayFont;
    /** Glyph layout to compute the size */
    private GlyphLayout displayLayout;

    // Constants only needed locally.
    /** Reverse the y-direction so that it is consistent with SpriteBatch */
    private static final Vector3 UP_REVERSED = new Vector3(0,-1,0);
    /** For managing the camera pan interpolation at the start of the game */
    private static final Interpolation.SwingIn SWING_IN = new Interpolation.SwingIn(0.1f);
    /** Distance from the eye to the target */
    private static final float EYE_DIST  = 400.0f;
    /** Field of view for the perspective */
    private static final float FOV = 0.7f;
    /** Near distance for perspective clipping */
    private static final float NEAR_DIST = 10.0f;
    /** Far distance for perspective clipping */
    private static final float FAR_DIST  = 500.0f;
    /** Horizontal clipping width */
    private static final int   CLIP_X = 500;
    /** Vertical clipping width */
    private static final int   CLIP_Y = 450;
    /** Multiplicative factors for initial camera pan */
    private static final float INIT_TARGET_PAN = 0.1f;
    private static final float INIT_EYE_PAN = 0.05f;
    /** Tile drawing constants */
    private static final float TILE_SIZE = 32.0f;
    private static final float TILE_DEPTH = 3.0f;
    /** Ship drawing constants */
    private static final float SHIP_SIZE = 30.0f;
    private static final float SHIP_FALL_TRANS = -16f;
    private static final float SHIP_FALL_X_SKEW = 0.04f;
    private static final float SHIP_FALL_Z_SKEW = 0.03f;
    /** Photon drawing constants */
    private static final float PHOTON_TRANS = -15f;
    private static final float PHOTON_SIZE  = 12f;
    private static final float PHOTON_DECAY = 8f;
    /** Constants for shader program locations */
    private static final String SHADER_VERTEX = "shaders/Tinted.vert";
    private static final String SHADER_FRAGMT = "shaders/Tinted.frag";
    private static final String SHADER_U_TEXT = "unTexture";
    private static final String SHADER_U_VIEWP = "unVP";
    private static final String SHADER_U_WORLD = "unWorld";
    private static final String SHADER_U_TINT = "unTint";

    // Instance attributes
    /** Value to cache window width (isf we are currently full screen) */
    int width;
    /** Value to cache window height (if we are currently full screen) */
    int height;

    /** Draws Sprite objects that should move */
    protected PolygonSpriteBatch spriteBatch;
    /** Draws Sprite objects to the background and foreground (e.g. font) */
    protected PolygonSpriteBatch constantBatch;
    /** Track whether or not we are actively drawing (for error checking) */
    private boolean active;

    // For managing the camera and perspective
    /** Orthographic camera for the SpriteBatch layer */
    private OrthographicCamera spriteCam;

    /** Affine cache for current sprite to draw */
    private Affine2 local;
    private Vector2 vertex;
    /** Cache object to handle raw textures */
    private TextureRegion holder;



    /**
     * Creates a new GameCanvas determined by the application configuration.
     *
     * Width, height, and fullscreen are taken from the LWGJApplicationConfig
     * object used to start the application.  This constructor initializes all
     * of the necessary graphics objects.
     */
    public GameCanvas() {
        // Initialize instance attributes
        active  = false;

        // Create and initialize the sprite batch
        spriteBatch = new PolygonSpriteBatch();
        constantBatch = new PolygonSpriteBatch();
        //spriteBatch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
        spriteCam = new OrthographicCamera(getWidth(),getHeight());
//        spriteCam.setToOrtho(false);
        spriteCam.position.set(spriteCam.viewportWidth / 2f, spriteCam.viewportHeight / 2f, 0);
        spriteCam.update();
        spriteBatch.setProjectionMatrix(spriteCam.combined);
        constantBatch.setProjectionMatrix(spriteCam.combined);

        local  = new Affine2();
        holder = new TextureRegion();
    }

    /**
     * Eliminate any resources that should be garbage collected manually.
     */
    public void dispose() {
        if (active) {
            Gdx.app.error("GameCanvas", "Cannot dispose while drawing active", new IllegalStateException());
            return;
        }

        // Dispose what requires a manual deletion.
        spriteBatch.dispose();
        spriteBatch = null;
        constantBatch.dispose();
        constantBatch = null;
        local  = null;
        holder = null;

        // Everything else is just garbage collected.
    }

    /**
     * Returns the width of this canvas
     *
     * This currently gets its value from Gdx.graphics.getWidth()
     *
     * @return the width of this canvas
     */
    public int getWidth() {
        return Gdx.graphics.getWidth();
    }

    /**
     * Changes the width of this canvas
     *
     * This method raises an IllegalStateException if called while drawing is
     * active (e.g. in-between a begin-end pair).
     *
     * This method has no effect if the resolution is full screen.  In that case, the
     * resolution was fixed at application startup.  However, the value is cached, should
     * we later switch to windowed mode.
     *
     * @param width the canvas width
     */
    public void setWidth(int width) {
        if (active) {
            Gdx.app.error("GameCanvas", "Cannot alter property while drawing active", new IllegalStateException());
            return;
        }
        this.width = width;
        if (!isFullscreen()) {
            Gdx.graphics.setWindowedMode(width, getHeight());
        }
        resize();
    }

    /**
     * Returns the height of this canvas
     *
     * This currently gets its value from Gdx.graphics.getHeight()
     *
     * @return the height of this canvas
     */
    public int getHeight() {
        return Gdx.graphics.getHeight();
    }

    /**
     * Changes the height of this canvas
     *
     * This method raises an IllegalStateException if called while drawing is
     * active (e.g. in-between a begin-end pair).
     *
     * This method has no effect if the resolution is full screen.  In that case, the
     * resolution was fixed at application startup.  However, the value is cached, should
     * we later switch to windowed mode.
     *
     * @param height the canvas height
     */
    public void setHeight(int height) {
        if (active) {
            Gdx.app.error("GameCanvas", "Cannot alter property while drawing active", new IllegalStateException());
            return;
        }
        this.height = height;
        if (!isFullscreen()) {
            Gdx.graphics.setWindowedMode(getWidth(), height);
        }
        resize();
    }

    /**
     * Returns the dimensions of this canvas
     *
     * @return the dimensions of this canvas
     */
    public Vector2 getSize() {
        return new Vector2(width,height);
    }

    /**
     * Changes the width and height of this canvas
     *
     * This method raises an IllegalStateException if called while drawing is
     * active (e.g. in-between a begin-end pair).
     *
     * This method has no effect if the resolution is full screen.  In that case, the
     * resolution was fixed at application startup.  However, the value is cached, should
     * we later switch to windowed mode.
     *
     * @param width the canvas width
     * @param height the canvas height
     */
    public void setSize(int width, int height) {
        if (active) {
            Gdx.app.error("GameCanvas", "Cannot alter property while drawing active", new IllegalStateException());
            return;
        }
        this.width = width;
        this.height = height;
        if (!isFullscreen()) {
            Gdx.graphics.setWindowedMode(width, height);
        }
        resize();
    }

    /**
     * Returns whether this canvas is currently fullscreen.
     *
     * @return whether this canvas is currently fullscreen.
     */
    public boolean isFullscreen() {
        return Gdx.graphics.isFullscreen();
    }

    /**
     * Clear the screen so we can start a new animation frame
     */
    public void clear() {
        // Clear the screen
        Gdx.gl.glClearColor(0.39f, 0.58f, 0.93f, 1.0f);  // Homage to the XNA years
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    /**
     * Sets whether or not this canvas should change to fullscreen.
     *
     * Changing to fullscreen will use the resolution of the application at startup.
     * It will NOT use the dimension settings of this canvas (which are for window
     * display only).
     *
     * This method raises an IllegalStateException if called while drawing is
     * active (e.g. in-between a begin-end pair).
     *
     * @param value Whether this canvas should change to fullscreen.
     */
    public void setFullscreen(boolean value) {
        if (active) {
            Gdx.app.error("GameCanvas", "Cannot alter property while drawing active", new IllegalStateException());
            return;
        }
        if (value) {
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        } else {
            Gdx.graphics.setWindowedMode(width, height);
        }
    }

    /**
     * Returns the font used to display messages.
     *
     * @return the font used to display messages.
     */
    public BitmapFont getFont() {
        return displayFont;
    }

    /**
     * Sets the font used to display messages.
     *
     * @param font the font used to display messages.
     */
    public void setFont(BitmapFont font) {
        displayFont = font;
        displayLayout = (font != null ? new GlyphLayout() : null);
    }

    /**
     * Returns the background texture for this canvas.
     *
     * The canvas fills the screen, and everything is drawn on top of the canvas.
     *
     * @return the background texture for this canvas.
     */
    public Texture getBackground() {
        return background;
    }

    /**
     * Sets the background texture for this canvas.
     *
     * The canvas fills the screen, and everything is drawn on top of the canvas.
     *
     * @param background the background texture for this canvas.
     */
    public void setBackground(Texture background) {
        this.background = background;
    }

    /**
     * Resets the SpriteBatch camera when this canvas is resized.
     *
     * If you do not call this when the window is resized, you will get
     * weird scaling issues.
     */
    public void resize() {
        // Resizing screws up the spriteBatch projection matrix
        spriteCam.setToOrtho(false,getWidth(),getHeight());
        spriteBatch.setProjectionMatrix(spriteCam.combined);
    }

    /**
     * Begins a drawing pass with no set camera.
     *
     * This method is used only during the loading screen.  You cannot draw
     * any 3D models after this method.
     */
    public void begin() {
        // We are drawing
        active = true;
        spriteBatch.begin();

        spriteBatch.setProjectionMatrix(spriteCam.combined);
       // spriteCam.setToOrtho(false, width, height);
        spriteCam.update();

        // Clear the screen and depth buffer
        setDepthState(DepthState.DEFAULT);
        // Clear the screen
        Gdx.gl.glClearColor(0.39f, 0.58f, 0.93f, 1.0f);  // Homage to the XNA years
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Draw the background
        drawBackground();

    }

    /**
     * Begins a drawing pass with the camera focused at postion (x,y)
     *
     * If eyepan is not 1, the camera will interpolate between the goal position
     * and (x,y).  This command will draw the background, no matter what else
     * is drawn this pass.
     *
     * @param x The x-coordinate of the player's ship
     * @param y The y-coordinate of the player's ship
     */
    public void begin(float x, float y) {
        // We are drawing
        active = true;

        spriteBatch.begin();
        spriteBatch.setProjectionMatrix(spriteCam.combined);
        //spriteCam.setToOrtho(false, 30, 20);
        //spriteCam.update();
       // spriteCam.setToOrtho(false, width, height);
        spriteCam.update();
        spriteCam.position.x = Math.max(x, getHeight()-85);
        if (y >= getHeight()/2.0f) {
            spriteCam.position.y = y;
        } else {
            spriteCam.position.y = getHeight()/2.0f;
        }

        // Clear the screen
        setDepthState(DepthState.DEFAULT);
        Gdx.gl.glClearColor(0.39f, 0.58f, 0.93f, 1.0f);  // Homage to the XNA years
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Draw the background
        drawBackground();
    }

    public void beginConstantBatch() {
        active = true;
        constantBatch.begin();
    }

    public void endConstantBatch() {
        constantBatch.end();
        active = false;
    }

    /**
     * Ends a drawing sequence, flushing textures to the graphics card.
     */
    public void end() {
        spriteBatch.end();
        active = false;
    }

    /**
     * Draws the background image to the screen.
     *
     * This image will not move, no matter what the camera does.  It is just "space".
     */
    private void drawBackground() {
        if (background == null) {
            return;
        }
        setDepthState(DepthState.NONE);
        setBlendState(BlendState.OPAQUE);
        setCullState(CullState.COUNTER_CLOCKWISE);

        // Only use of spritebatch in game.
        spriteBatch.begin();
        spriteBatch.draw(background, 0, 0, getWidth(), getHeight());
        spriteBatch.end();
    }

    /**
     * Draw the seamless background image.
     *
     * The background image is drawn (with NO SCALING) at position x, y.  Width-wise,
     * the image is seamlessly scrolled; when we reach the image we draw a second copy.
     *
     * To work properly, the image should be wide and high enough to fill the screen.
     *
     * @param image  Texture to draw as an overlay
     * @param x      The x-coordinate of the bottom left corner
     * @param y 	 The y-coordinate of the bottom left corner
     */
    public void drawBackground(Texture image, float x, float y) {
        if (!active) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
            return;
        }
        spriteBatch.draw(image, x,   y);
    }

    /**
     * Draws text on the screen.
     *
     * @param text The string to draw
     * @param font The font to use
     * @param x The x-coordinate of the lower-left corner
     * @param y The y-coordinate of the lower-left corner
     */
    public void drawText(String text, BitmapFont font, float x, float y) {
        if (!active) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
            return;
        }
        GlyphLayout layout = new GlyphLayout(font,text);
        font.draw(constantBatch, layout, x, y);
    }

    /**
     * Draws text centered on the screen.
     *
     * @param text The string to draw
     * @param font The font to use
     * @param offset The y-value offset from the center of the screen.
     */
    public void drawTextCentered(String text, BitmapFont font, float offset) {
        if (!active) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
            return;
        }

        GlyphLayout layout = new GlyphLayout(font, text);
        float x = (getWidth() - layout.width) / 2.0f;
        float y = (getHeight() + layout.height) / 2.0f;
        font.draw(constantBatch, layout, x, y + offset);
    }

    /**
     * Draws the tinted texture at the given position.
     *
     * The texture colors will be multiplied by the given color.  This will turn
     * any white into the given color.  Other colors will be similarly affected.
     *
     * Unless otherwise transformed by the global transform (@see begin(Affine2)),
     * the texture will be unscaled.  The bottom left of the texture will be positioned
     * at the given coordinates.
     *
     * @param image The texture to draw
     * @param x 	The x-coordinate of the bottom left corner
     * @param y 	The y-coordinate of the bottom left corner
     */
    public void draw(Texture image, float x, float y) {
        if (!active) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
            return;
        }

        spriteBatch.setColor(Color.WHITE);
        spriteBatch.draw(image, x,  y);
    }

    /**
     * Draws the tinted texture at the given position.
     *
     * The texture colors will be multiplied by the given color.  This will turn
     * any white into the given color.  Other colors will be similarly affected.
     *
     * Unless otherwise transformed by the global transform (@see begin(Affine2)),
     * the texture will be unscaled.  The bottom left of the texture will be positioned
     * at the given coordinates.
     *
     * @param image The texture to draw
     * @param tint  The color tint
     * @param x 	The x-coordinate of the bottom left corner
     * @param y 	The y-coordinate of the bottom left corner
     * @param width	The texture width
     * @param height The texture height
     */
    public void draw(Texture image, Color tint, float x, float y, float width, float height) {
        if (!active) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
            return;
        }

        spriteBatch.setColor(tint);
        spriteBatch.draw(image, x,  y, width, height);
    }

    /**
     * Draws the tinted texture at the given position.
     *
     * The texture colors will be multiplied by the given color.  This will turn
     * any white into the given color.  Other colors will be similarly affected.
     *
     * Unless otherwise transformed by the global transform (@see begin(Affine2)),
     * the texture will be unscaled.  The bottom left of the texture will be positioned
     * at the given coordinates.
     *
     * @param image The texture to draw
     * @param tint  The color tint
     * @param ox 	The x-coordinate of texture origin (in pixels)
     * @param oy 	The y-coordinate of texture origin (in pixels)
     * @param x 	The x-coordinate of the texture origin (on screen)
     * @param y 	The y-coordinate of the texture origin (on screen)
     * @param width	The texture width
     * @param height The texture height
     */
    public void draw(Texture image, Color tint, float ox, float oy, float x, float y, float width, float height) {
        if (!active) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
            return;
        }

        // Call the master drawing method (more efficient that base method)
        holder.setRegion(image);
        draw(holder, tint, x-ox, y-oy, width, height, false);
    }


    /**
     * Draws the tinted texture with the given transformations
     *
     * The texture colors will be multiplied by the given color.  This will turn
     * any white into the given color.  Other colors will be similarly affected.
     *
     * The transformations are BEFORE after the global transform (@see begin(Affine2)).
     * As a result, the specified texture origin will be applied to all transforms
     * (both the local and global).
     *
     * The local transformations in this method are applied in the following order:
     * scaling, then rotation, then translation (e.g. placement at (sx,sy)).
     *
     * @param image The texture to draw
     * @param tint  The color tint
     * @param ox 	The x-coordinate of texture origin (in pixels)
     * @param oy 	The y-coordinate of texture origin (in pixels)
     * @param x 	The x-coordinate of the texture origin (on screen)
     * @param y 	The y-coordinate of the texture origin (on screen)
     * @param angle The rotation angle (in degrees) about the origin.
     * @param sx 	The x-axis scaling factor
     * @param sy 	The y-axis scaling factor
     */
    public void draw(Texture image, Color tint, float ox, float oy,
                     float x, float y, float angle, float sx, float sy) {
        if (!active) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
            return;
        }

        // Call the master drawing method (more efficient that base method)
        holder.setRegion(image);
        draw(holder,tint,ox,oy,x,y,angle,sx,sy);
    }

    /**
     * Draws the tinted texture with the given transformations
     *
     * The texture colors will be multiplied by the given color.  This will turn
     * any white into the given color.  Other colors will be similarly affected.
     *
     * The transformations are BEFORE after the global transform (@see begin(Affine2)).
     * As a result, the specified texture origin will be applied to all transforms
     * (both the local and global).
     *
     * The local transformations in this method are applied in the following order:
     * scaling, then rotation, then translation (e.g. placement at (sx,sy)).
     *
     * @param image The texture to draw
     * @param tint  The color tint
     * @param ox 	The x-coordinate of texture origin (in pixels)
     * @param oy 	The y-coordinate of texture origin (in pixels)
     * @param transform  The image transform
     */
    public void draw(Texture image, Color tint, float ox, float oy, Affine2 transform) {
        if (!active) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
            return;
        }

        // Call the master drawing method (we have to for transforms)
        holder.setRegion(image);
        draw(holder,tint,ox,oy,transform);
    }

    /**
     * Draws the tinted texture region (filmstrip) at the given position.
     *
     * A texture region is a single texture file that can hold one or more textures.
     * It is used for filmstrip animation.
     *
     * The texture colors will be multiplied by the given color.  This will turn
     * any white into the given color.  Other colors will be similarly affected.
     *
     * Unless otherwise transformed by the global transform (@see begin(Affine2)),
     * the texture will be unscaled.  The bottom left of the texture will be positioned
     * at the given coordinates.
     *
     * @param region The texture to draw
     * @param x 	The x-coordinate of the bottom left corner
     * @param y 	The y-coordinate of the bottom left corner
     */
    public void draw(TextureRegion region, float x, float y) {
        if (!active) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
            return;
        }

        spriteBatch.setColor(Color.WHITE);
        spriteBatch.draw(region, x,  y);
    }

    /**
     * Draws the tinted texture at the given position.
     *
     * The texture colors will be multiplied by the given color.  This will turn
     * any white into the given color.  Other colors will be similarly affected.
     *
     * Unless otherwise transformed by the global transform (@see begin(Affine2)),
     * the texture will be unscaled.  The bottom left of the texture will be positioned
     * at the given coordinates.
     *
     * @param region The texture to draw
     * @param tint  The color tint
     * @param ox 	The x-coordinate of texture origin (in pixels)
     * @param oy 	The y-coordinate of texture origin (in pixels)
     * @param x 	The x-coordinate of the texture origin (on screen)
     * @param y 	The y-coordinate of the texture origin (on screen)
     * @param width	The texture width
     * @param height The texture height
     */
    public void draw(TextureRegion region, Color tint, float ox, float oy, float x, float y, float width, float height) {
        if (!active) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
            return;
        }

        // Unlike Lab 1, we can shortcut without a master drawing method
        spriteBatch.setColor(tint);
        spriteBatch.draw(region, x-ox, y-oy, width, height);
    }

    /**
     * Draws the tinted texture region (filmstrip) with the given transformations
     *
     * A texture region is a single texture file that can hold one or more textures.
     * It is used for filmstrip animation.
     *
     * The texture colors will be multiplied by the given color.  This will turn
     * any white into the given color.  Other colors will be similarly affected.
     *
     * The transformations are BEFORE after the global transform (@see begin(Affine2)).
     * As a result, the specified texture origin will be applied to all transforms
     * (both the local and global).
     *
     * The local transformations in this method are applied in the following order:
     * scaling, then rotation, then translation (e.g. placement at (sx,sy)).
     *
     * @param region The texture to draw
     * @param tint  The color tint
     * @param ox 	The x-coordinate of texture origin (in pixels)
     * @param oy 	The y-coordinate of texture origin (in pixels)
     * @param x 	The x-coordinate of the texture origin (on screen)
     * @param y 	The y-coordinate of the texture origin (on screen)
     * @param angle The rotation angle (in degrees) about the origin.
     * @param sx 	The x-axis scaling factor
     * @param sy 	The y-axis scaling factor
     */
    public void draw(TextureRegion region, Color tint, float ox, float oy,
                     float x, float y, float angle, float sx, float sy) {
        if (!active) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
            return;
        }

        // BUG: The draw command for texture regions does not work properly.
        // There is a workaround, but it will break if the bug is fixed.
        // For now, it is better to set the affine transform directly.
        computeTransform(ox,oy,x,y,angle,sx,sy);
        spriteBatch.setColor(tint);
        spriteBatch.draw(region, region.getRegionWidth(), region.getRegionHeight(), local);
    }

    /**
     * Draws the tinted texture region (filmstrip) with the given transformations
     *
     * A texture region is a single texture file that can hold one or more textures.
     * It is used for filmstrip animation.
     *
     * The texture colors will be multiplied by the given color.  This will turn
     * any white into the given color.  Other colors will be similarly affected.
     *
     * The transformations are BEFORE after the global transform (@see begin(Affine2)).
     * As a result, the specified texture origin will be applied to all transforms
     * (both the local and global).
     *
     * The local transformations in this method are applied in the following order:
     * scaling, then rotation, then translation (e.g. placement at (sx,sy)).
     *
     * @param region The texture to draw
     * @param tint  The color tint
     * @param ox 	The x-coordinate of texture origin (in pixels)
     * @param oy 	The y-coordinate of texture origin (in pixels)
     * @param x 	The x-coordinate of the texture origin (on screen)
     * @param y 	The y-coordinate of the texture origin (on screen)
     * @param angle The rotation angle (in degrees) about the origin.
     * @param sx 	The x-axis scaling factor
     * @param sy 	The y-axis scaling factor
     * @param width	The texture width
     * @param height The texture height
     */
    public void draw(TextureRegion region, Color tint, float ox, float oy,
                     float x, float y, float angle, float sx, float sy, float width, float height) {
        if (!active) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
            return;
        }

        computeTransform(ox,oy,x,y,angle,sx,sy);
        spriteBatch.setColor(tint);
        spriteBatch.draw(region, width, height, local);
    }

    /**
     * Draws the polygonal region with the given transformations
     *
     * A polygon region is a texture region with attached vertices so that it draws a
     * textured polygon. The polygon vertices are relative to the texture file.
     *
     * The texture colors will be multiplied by the given color.  This will turn
     * any white into the given color.  Other colors will be similarly affected.
     *
     * The transformations are BEFORE after the global transform (@see begin(Affine2)).
     * As a result, the specified texture origin will be applied to all transforms
     * (both the local and global).
     *
     * The local transformations in this method are applied in the following order:
     * scaling, then rotation, then translation (e.g. placement at (sx,sy)).
     *
     * @param region The polygon to draw
     * @param tint  The color tint
     * @param ox 	The x-coordinate of texture origin (in pixels)
     * @param oy 	The y-coordinate of texture origin (in pixels)
     * @param x 	The x-coordinate of the texture origin (on screen)
     * @param y 	The y-coordinate of the texture origin (on screen)
     * @param angle The rotation angle (in degrees) about the origin.
     * @param sx 	The x-axis scaling factor
     * @param sy 	The y-axis scaling factor
     */
    public void draw(PolygonRegion region, Color tint, float ox, float oy,
                     float x, float y, float angle, float sx, float sy) {
        if (!active) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
            return;
        }

        TextureRegion bounds = region.getRegion();
        spriteBatch.setColor(tint);
        spriteBatch.draw(region, x, y, ox, oy,
                bounds.getRegionWidth(), bounds.getRegionHeight(),
                sx, sy, 180.0f*angle/(float)Math.PI);
    }

    /**
     * Draws the polygonal region with the given transformations
     *
     * A polygon region is a texture region with attached vertices so that it draws a
     * textured polygon. The polygon vertices are relative to the texture file.
     *
     * The texture colors will be multiplied by the given color.  This will turn
     * any white into the given color.  Other colors will be similarly affected.
     *
     * The transformations are BEFORE after the global transform (@see begin(Affine2)).
     * As a result, the specified texture origin will be applied to all transforms
     * (both the local and global).
     *
     * The local transformations in this method are applied in the following order:
     * scaling, then rotation, then translation (e.g. placement at (sx,sy)).
     *
     * @param region The polygon to draw
     * @param tint  The color tint
     * @param ox 	The x-coordinate of texture origin (in pixels)
     * @param oy 	The y-coordinate of texture origin (in pixels)
     * @param affine  The image affine
     */
    public void draw(PolygonRegion region, Color tint, float ox, float oy, Affine2 affine) {
        if (!active) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
            return;
        }

        local.set(affine);
        local.translate(-ox,-oy);
        computeVertices(local,region.getVertices());

        spriteBatch.setColor(tint);
        spriteBatch.draw(region, 0, 0);

        // Invert and restore
        local.inv();
        computeVertices(local,region.getVertices());
    }

    /**
     * Transform the given vertices by the affine transform
     */
    private void computeVertices(Affine2 affine, float[] vertices) {
        for(int ii = 0; ii < vertices.length; ii += 2) {
            vertex.set(vertices[2*ii], vertices[2*ii+1]);
            affine.applyTo(vertex);
            vertices[2*ii  ] = vertex.x;
            vertices[2*ii+1] = vertex.y;
        }
    }

    /**
     * Compute the affine transform (and store it in local) for this image.
     *
     * @param ox 	The x-coordinate of texture origin (in pixels)
     * @param oy 	The y-coordinate of texture origin (in pixels)
     * @param x 	The x-coordinate of the texture origin (on screen)
     * @param y 	The y-coordinate of the texture origin (on screen)
     * @param angle The rotation angle (in degrees) about the origin.
     * @param sx 	The x-axis scaling factor
     * @param sy 	The y-axis scaling factor
     */
    private void computeTransform(float ox, float oy, float x, float y, float angle, float sx, float sy) {
        local.setToTranslation(x,y);
        local.rotate(180.0f*angle/(float)Math.PI);
        local.scale(sx,sy);
        local.translate(-ox,-oy);
    }

    /**
     * Draws the tinted texture with the given transformations
     *
     * The texture colors will be multiplied by the given color.  This will turn
     * any white into the given color.  Other colors will be similarly affected.
     *
     * The transformations are BEFORE after the global transform (@see begin(Affine2)).
     * As a result, the specified texture origin will be applied to all transforms
     * (both the local and global).
     *
     * The local transformations in this method are applied in the following order:
     * scaling, then rotation, then translation (e.g. placement at (sx,sy)).
     *
     * @param region The polygon to draw
     * @param tint  The color tint
     * @param ox 	The x-coordinate of texture origin (in pixels)
     * @param oy 	The y-coordinate of texture origin (in pixels)
     * @param affine  The image affine
     */
    public void draw(TextureRegion region, Color tint, float ox, float oy, Affine2 affine) {
        if (!active) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
            return;
        }

        local.set(affine);
        local.translate(-ox,-oy);
        spriteBatch.setColor(tint);
        spriteBatch.draw(region, region.getRegionWidth(), region.getRegionHeight(), local);
    }

    /**
     * Draws the tinted texture at the given position.
     *
     * The texture colors will be multiplied by the given color.  This will turn
     * any white into the given color.  Other colors will be similarly affected.
     *
     * Unless otherwise transformed by the global transform (@see begin(Affine2)),
     * the texture will be unscaled.  The bottom left of the texture will be positioned
     * at the given coordinates.
     *region
     * @param region The texture to draw
     * @param tint  The color tint
     * @param x 	The x-coordinate of the bottom left corner
     * @param y 	The y-coordinate of the bottom left corner
     * @param width	The texture width
     * @param height The texture height
     */
    public void draw(TextureRegion region, Color tint, float x, float y, float width, float height, boolean constant) {
        if (!active) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
            return;
        }

        // Unlike Lab 1, we can shortcut without a master drawing method
        if (constant) {
            constantBatch.setColor(tint);
            constantBatch.draw(region, x, y, width, height);
        } else {
            draw(region, tint, x, y, width, height);
        }
    }

    /**
     * Draws the tinted texture at the given position.
     *
     * The texture colors will be multiplied by the given color.  This will turn
     * any white into the given color.  Other colors will be similarly affected.
     *
     * Unless otherwise transformed by the global transform (@see begin(Affine2)),
     * the texture will be unscaled.  The bottom left of the texture will be positioned
     * at the given coordinates.
     *region
     * @param region The texture to draw
     * @param tint  The color tint
     * @param x 	The x-coordinate of the bottom left corner
     * @param y 	The y-coordinate of the bottom left corner
     * @param width	The texture width
     * @param height The texture height
     */
    public void draw(TextureRegion region, Color tint, float x, float y, float width, float height) {
        if (!active) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
            return;
        }

        // Unlike Lab 1, we can shortcut without a master drawing method
        spriteBatch.setColor(tint);
        spriteBatch.draw(region, x,  y, width, height);
    }

    /**
     * Draws a two-line message at the center of the screen
     *
     * The message is an overlay (like the background) and is unaffected by the
     * camera position.
     *
     * Once a message is drawn, the canvas is unable to draw any more 3D objects.
     * The user must call end().
     *
     * @param mess1 The top text to draw
     * @param mess2 The bottom text to draw
     * @param color The color to tint the font
     */
    public void drawMessage(String mess1, String mess2, Color color) {
        if (!active) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
            return;
        } else if (displayFont == null) {
            Gdx.app.error("GameCanvas", "Cannot create a message without a font", new IllegalStateException());
            return;
        }

        setDepthState(DepthState.NONE);
        setBlendState(BlendState.ALPHA_BLEND);
        setCullState(CullState.COUNTER_CLOCKWISE);

        float x, y;

        spriteBatch.begin();
        displayFont.setColor(color);

        displayLayout.setText(displayFont,mess1);
        x = (getWidth()  - displayLayout.width) / 2.0f;
        y = displayLayout.height+(getHeight() + displayLayout.height) / 2.0f;
        displayFont.draw(spriteBatch, displayLayout, x, y);

        displayLayout.setText(displayFont,mess2);
        x = (getWidth() - displayLayout.width) / 2.0f;
        y = -displayLayout.height+(getHeight() + displayLayout.height) / 2.0f;
        displayFont.draw(spriteBatch, displayLayout, x, y);
        spriteBatch.end();
    }


    /**
     * Sets the given matrix to a FOV perspective.
     *
     * The field of view matrix is computed as follows:
     *
     *        /
     *       /_
     *      /  \  <-  FOV
     * EYE /____|_____
     *
     * Let ys = cot(fov)
     * Let xs = ys / aspect
     * Let a = zfar / (znear - zfar)
     * The matrix is
     * | xs  0   0      0     |
     * | 0   ys  0      0     |
     * | 0   0   a  znear * a |
     * | 0   0  -1      0     |
     *
     * @param out Non-null matrix to store result
     * @param fov field of view y-direction in radians from center plane
     * @param aspect Width / Height
     * @param znear Near clip distance
     * @param zfar Far clip distance
     *
     * @returns Newly created matrix stored in out
     */
    private Matrix4 setToPerspectiveFOV(Matrix4 out, float fov, float aspect, float znear, float zfar) {
        float ys = (float)(1.0 / Math.tan(fov));
        float xs = ys / aspect;
        float a  = zfar / (znear - zfar);

        out.val[0 ] = xs;
        out.val[4 ] = 0.0f;
        out.val[8 ] = 0.0f;
        out.val[12] = 0.0f;

        out.val[1 ] = 0.0f;
        out.val[5 ] = ys;
        out.val[9 ] = 0.0f;
        out.val[13] = 0.0f;

        out.val[2 ] = 0.0f;
        out.val[6 ] = 0.0f;
        out.val[10] = a;
        out.val[14] = znear * a;

        out.val[3 ] = 0.0f;
        out.val[7 ] = 0.0f;
        out.val[11] = -1.0f;
        out.val[15] = 0.0f;

        return out;
    }

    /**
     * Sets the mode for blending colors on-screen.
     *
     * @param state The blending mode
     */
    private void setBlendState(BlendState state) {
        int blendMod = 0;
        int blendSrc = 0;
        int blendDst = 0;
        int blendModAlpha = 0;
        int blendSrcAlpha = 0;
        int blendDstAlpha = 0;

        switch (state) {
            case ALPHA_BLEND:
                blendMod = GL20.GL_FUNC_ADD;
                blendSrc = GL20.GL_ONE;
                blendDst = GL20.GL_ONE_MINUS_SRC_ALPHA;
                blendModAlpha = GL20.GL_FUNC_ADD;
                blendSrcAlpha = GL20.GL_ONE;
                blendDstAlpha = GL20.GL_ONE_MINUS_SRC_ALPHA;
                break;
            case NO_PREMULT:
                blendMod = GL20.GL_FUNC_ADD;
                blendSrc = GL20.GL_SRC_ALPHA;
                blendDst = GL20.GL_ONE_MINUS_SRC_ALPHA;
                blendModAlpha = GL20.GL_FUNC_ADD;
                blendSrcAlpha = GL20.GL_ONE;
                blendDstAlpha = GL20.GL_ZERO;
                break;
            case ADDITIVE:
                blendMod = GL20.GL_FUNC_ADD;
                blendSrc = GL20.GL_SRC_ALPHA;
                blendDst = GL20.GL_ONE;
                blendModAlpha = GL20.GL_FUNC_ADD;
                blendSrcAlpha = GL20.GL_ONE;
                blendDstAlpha = GL20.GL_ZERO;
                break;
            case OPAQUE:
                blendMod = GL20.GL_FUNC_ADD;
                blendSrc = GL20.GL_ONE;
                blendDst = GL20.GL_ZERO;
                blendModAlpha = GL20.GL_FUNC_ADD;
                blendSrcAlpha = GL20.GL_ONE;
                blendDstAlpha = GL20.GL_ZERO;
                break;
        }

        gl20.glBlendEquationSeparate(blendMod, blendModAlpha);
        gl20.glBlendFuncSeparate(blendSrc, blendDst, blendSrcAlpha, blendDstAlpha);
    }

    /**
     * Sets the mode for culling unwanted polygons based on depth.
     *
     * @param state The depth mode
     */
    private void setDepthState(DepthState state) {
        boolean shouldRead  = true;
        boolean shouldWrite = true;
        int depthFunc = 0;

        switch (state) {
            case NONE:
                shouldRead  = false;
                shouldWrite = false;
                depthFunc = GL20.GL_ALWAYS;
                break;
            case READ:
                shouldRead  = false;
                shouldWrite = true;
                depthFunc = GL20.GL_LEQUAL;
                break;
            case WRITE:
                shouldRead  = false;
                shouldWrite = true;
                depthFunc = GL20.GL_ALWAYS;
                break;
            case DEFAULT:
                shouldRead  = true;
                shouldWrite = true;
                depthFunc = GL20.GL_LEQUAL;
                break;
        }

        if (shouldRead || shouldWrite) {
            gl20.glEnable(GL20.GL_DEPTH_TEST);
            gl20.glDepthMask(shouldWrite);
            gl20.glDepthFunc(depthFunc);
        } else {
            gl20.glDisable(GL20.GL_DEPTH_TEST);
        }
    }

    /**
     * Sets the mode for culling unwanted polygons based on facing.
     *
     * @param state The culling mode
     */
    private void setCullState(CullState state) {
        boolean cull = true;
        int mode = 0;
        int face = 0;

        switch (state) {
            case NONE:
                cull = false;
                mode = GL20.GL_BACK;
                face = GL20.GL_CCW;
                break;
            case CLOCKWISE:
                cull = true;
                mode = GL20.GL_BACK;
                face = GL20.GL_CCW;
                break;
            case COUNTER_CLOCKWISE:
                cull = true;
                mode = GL20.GL_BACK;
                face = GL20.GL_CW;
                break;
        }
        if (cull) {
            gl20.glEnable(GL20.GL_CULL_FACE);
            gl20.glFrontFace(face);
            gl20.glCullFace(mode);
        } else {
            gl20.glDisable(GL20.GL_CULL_FACE);
        }

    }

    /**
     * Enumeration of supported blend states.
     *
     * For reasons of convenience, we do not allow user-defined blend functions.
     * 99% of the time, we find that the following blend modes are sufficient
     * (particularly with 2D games).
     */
    private static enum BlendState {
        /** Alpha blending on, assuming the colors have pre-multipled alpha (DEFAULT) */
        ALPHA_BLEND,
        /** Alpha blending on, assuming the colors have no pre-multipled alpha */
        NO_PREMULT,
        /** Color values are added together, causing a white-out effect */
        ADDITIVE,
        /** Color values are draw on top of one another with no transparency support */
        OPAQUE
    }

    /**
     * Enumeration of supported depth states.
     *
     * For reasons of convenience, we do not allow user-defined depth functions.
     * 99% of the time, we find that the following depth modes are sufficient
     * (particularly with 2D games).
     */
    private static enum DepthState {
        /** Do not enable depth masking at all. */
        NONE,
        /** Read from the depth value, but do not write to it */
        READ,
        /** Write to the depth value, but do not read from it */
        WRITE,
        /** Read and write to the depth value, providing normal masking */
        DEFAULT
    }

    /**
     * Enumeration of supported culling states.
     *
     * For reasons of convenience, we do not allow user-defined culling operations.
     * 99% of the time, we find that the following culling modes are sufficient
     * (particularly with 2D games).
     */
    private static enum CullState {
        /** Do not remove the backsides of any polygons; show both sides */
        NONE,
        /** Remove polygon backsides, using clockwise motion to define the front */
        CLOCKWISE,
        /** Remove polygon backsides, using counter-clockwise motion to define the front */
        COUNTER_CLOCKWISE
    }

}