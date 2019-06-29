package multiplayer;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.awt.*;
import java.applet.AudioClip;

public class Server extends UnicastRemoteObject implements ServerInterface {

    statePacket serverPacket = new statePacket();
    // Thread control variables.
    Thread loadThread;
    Thread loopThread;

    // Constants
    static final int DELAY = 50;             // Milliseconds between screen updates.

    static final int MAX_SHIPS = 3;           // Starting number of ships per game.

    static final int MAX_SHOTS = 12;          // Maximum number of sprites for photons,
    static final int MAX_ROCKS = 2;          // asteroids and explosions.
    static final int MAX_SCRAP = 20;

    static final int SCRAP_COUNT = 30;        // Counter starting values.
    static final int HYPER_COUNT = 60;
    static final int STORM_PAUSE = 30;
    static final int UFO_PASSES = 3;

    static final int MIN_ROCK_SIDES = 8;     // Asteroid shape and size ranges.
    static final int MAX_ROCK_SIDES = 12;
    static final int MIN_ROCK_SIZE = 20;
    static final int MAX_ROCK_SIZE = 40;
    static final int MIN_ROCK_SPEED = 2;
    static final int MAX_ROCK_SPEED = 12;

    static final int BIG_POINTS = 25;     // Points for shooting different objects.
    static final int SMALL_POINTS = 50;
    static final int UFO_POINTS = 250;
    static final int MISSLE_POINTS = 500;

    static final int NEW_SHIP_POINTS = 5000;  // Number of points needed to earn a new ship.
    static final int NEW_UFO_POINTS = 2750;  // Number of points between flying saucers.

    // Background stars.
    int numStars;
    Point[] stars;

    // Game data.
    int score;
    int highScore;
    int newShipScore;
    int newUfoScore;

    // Second Player.
    int score2;
    int highScore2;
    int newShipScore2;
    int newUfoScore2;

    boolean loaded = true;
    boolean paused;
    boolean playing;
    boolean sound;
    boolean detail;

    // Key flags.
    boolean left = false;
    boolean right = false;
    boolean up = false;
    boolean down = false;

    boolean left2 = false;
    boolean right2 = false;
    boolean up2 = false;
    boolean down2 = false;

    // Sprite objects.
    AsteroidsSprite ship;
    AsteroidsSprite ship2;
    AsteroidsSprite ufo;
    AsteroidsSprite missle;
    AsteroidsSprite[] photons = new AsteroidsSprite[MAX_SHOTS];
    AsteroidsSprite[] photons2 = new AsteroidsSprite[MAX_SHOTS];
    AsteroidsSprite[] asteroids = new AsteroidsSprite[MAX_ROCKS];
    AsteroidsSprite[] explosions = new AsteroidsSprite[MAX_SCRAP];

    // Ship data.
    int shipsLeft;       // Number of ships left to play, including current one.
    int shipCounter;     // Time counter for ship explosion.
    int hyperCounter;    // Time counter for hyperspace.

    int shipsLeft2;       // Number of ships left to play, including current one.
    int shipCounter2;     // Time counter for ship explosion.
    int hyperCounter2;    // Time counter for hyperspace.

    // Photon data.
    int[] photonCounter = new int[MAX_SHOTS];    // Time counter for life of a photon.
    int photonIndex;                           // Next available photon sprite.
    int[] photonCounter2 = new int[MAX_SHOTS];    // Time counter for life of a photon.
    int photonIndex2;

    // Flying saucer data.
    int ufoPassesLeft;    // Number of flying saucer passes.
    int ufoCounter;       // Time counter for each pass.

    // Missle data.
    int missleCounter;    // Counter for life of missle.

    // Asteroid data.
    boolean[] asteroidIsSmall = new boolean[MAX_ROCKS];    // Asteroid size flag.
    int asteroidsCounter;                            // Break-time counter.
    int asteroidsSpeed;                              // Asteroid speed.
    int asteroidsLeft;                               // Number of active asteroids.

    // Explosion data.
    int[] explosionCounter = new int[MAX_SCRAP];  // Time counters for explosions.
    int explosionIndex;                         // Next available explosion sprite.

    // Sound clips.
    AudioClip crashSound;
    AudioClip explosionSound;
    AudioClip fireSound;
    AudioClip missleSound;
    AudioClip saucerSound;
    AudioClip thrustersSound;
    AudioClip warpSound;

    // Flags for looping sound clips.
    boolean thrustersPlaying;
    boolean saucerPlaying;
    boolean misslePlaying;

    boolean started = false;

    protected Server() throws RemoteException {

        super();

    }

    public void packetUpdate() {
        // update serverPacket state variables
        serverPacket.ship = spriteCopy(ship);
        serverPacket.ship2 = spriteCopy(ship2);
        serverPacket.ufo = spriteCopy(ufo);
        serverPacket.missle = spriteCopy(missle);
        serverPacket.photons = spritesArrayCopy(photons);
        serverPacket.photons2 = spritesArrayCopy(photons2);
        serverPacket.asteroids = spritesArrayCopy(asteroids);
        serverPacket.explosions = spritesArrayCopy(explosions);
        serverPacket.photonCounter = photonCounter;
        serverPacket.photonIndex = photonIndex;
        serverPacket.photonCounter2 = photonCounter2;
        serverPacket.photonIndex2 = photonIndex2;
        serverPacket.explosionCounter = explosionCounter;
        serverPacket.explosionIndex = explosionIndex;
        serverPacket.score2 = score2;
        serverPacket.highScore2 = highScore2;
        serverPacket.newShipScore2 = newShipScore2;
        serverPacket.loaded = loaded;
        serverPacket.paused = paused;
        serverPacket.playing = playing;
        serverPacket.sound = sound;
        serverPacket.detail = detail;
        serverPacket.numStars = numStars;
        serverPacket.stars = stars;
        serverPacket.score = score;
        serverPacket.highScore = highScore;
        serverPacket.newShipScore = newShipScore;
        serverPacket.newUfoScore = newUfoScore;
        serverPacket.shipsLeft = shipsLeft;
        serverPacket.shipCounter = shipCounter;
        serverPacket.hyperCounter = hyperCounter;
        serverPacket.shipsLeft2 = shipsLeft2;
        serverPacket.shipCounter2 = shipCounter2;
        serverPacket.hyperCounter2 = hyperCounter2;

    }

    public statePacket packetCopy() {
        //Make a deep copy of statePacket
        statePacket copy = new statePacket();

        copy.ship = spriteCopy(serverPacket.ship);
        copy.ship2 = spriteCopy(serverPacket.ship2);
        copy.ufo = spriteCopy(serverPacket.ufo);
        copy.missle = spriteCopy(serverPacket.missle);
        copy.photons = spritesArrayCopy(serverPacket.photons);
        copy.photons2 = spritesArrayCopy(serverPacket.photons2);
        copy.asteroids = spritesArrayCopy(serverPacket.asteroids);
        copy.explosions = spritesArrayCopy(serverPacket.explosions);
        copy.loaded = serverPacket.loaded;
        copy.paused = serverPacket.paused;
        copy.playing = serverPacket.playing;
        copy.sound = serverPacket.sound;
        copy.detail = serverPacket.detail;
        copy.numStars = serverPacket.numStars;
        copy.stars = serverPacket.stars;
        copy.score = serverPacket.score;
        copy.highScore = serverPacket.highScore;
        copy.newShipScore = serverPacket.newShipScore;
        copy.newUfoScore = serverPacket.newUfoScore;
        copy.score2 = serverPacket.score2;
        copy.highScore2 = serverPacket.highScore2;
        copy.newShipScore2 = serverPacket.newShipScore2;
        copy.shipsLeft = serverPacket.shipsLeft;
        copy.shipCounter = serverPacket.shipCounter;
        copy.hyperCounter = serverPacket.hyperCounter;
        copy.shipsLeft2 = serverPacket.shipsLeft2;
        copy.shipCounter2 = serverPacket.shipCounter2;
        copy.hyperCounter2 = serverPacket.hyperCounter2;
        copy.photonCounter = serverPacket.photonCounter;
        copy.photonIndex = serverPacket.photonIndex;
        copy.photonCounter2 = serverPacket.photonCounter2;
        copy.photonIndex2 = serverPacket.photonIndex2;
        copy.asteroidsCounter = serverPacket.asteroidsCounter;
        copy.asteroidsSpeed = serverPacket.asteroidsSpeed;
        copy.asteroidsLeft = serverPacket.asteroidsLeft;
        copy.explosionCounter = serverPacket.explosionCounter;
        copy.explosionIndex = serverPacket.explosionIndex;
        return copy;
    }

    public AsteroidsSprite spriteCopy(AsteroidsSprite sp) {
        AsteroidsSprite newSprite = new AsteroidsSprite();
        newSprite.shape = sp.shape;
        newSprite.active = sp.active;
        newSprite.angle = sp.angle;
        newSprite.deltaAngle = sp.deltaAngle;
        newSprite.currentX = sp.currentX;
        newSprite.currentY = sp.currentY;
        newSprite.sprite = sp.sprite;
        return newSprite;
    }

    public AsteroidsSprite[] spritesArrayCopy(AsteroidsSprite[] array) {
        int length = array.length;
        AsteroidsSprite[] copy = new AsteroidsSprite[length];
        for (int i = 0; i < length; i++) {
            copy[i] = spriteCopy(array[i]);
        }
        return copy;
    }

    public void nullAsteroid(statePacket packet, int i) {
        packet.asteroids[i] = null;
    }

    public boolean isAsteroidFar(AsteroidsSprite shipArg, AsteroidsSprite asteroid) {
        double distance = Math.sqrt(Math.pow(shipArg.currentX - asteroid.currentX, 2) + Math.pow(shipArg.currentY - asteroid.currentY, 2));
        return (distance > 300);
    }

    public void init() {

            started = true;
            int i;

            numStars = AsteroidsSprite.width * AsteroidsSprite.height / 5000;
            stars = new Point[numStars];
            for (i = 0; i < numStars; i++) {
                stars[i] = new Point((int) (Math.random() * AsteroidsSprite.width), (int) (Math.random() * AsteroidsSprite.height));
            }

            // Create shape for the ship sprite.
            ship = new AsteroidsSprite();
            ship.shape.addPoint(0, -10);
            ship.shape.addPoint(7, 10);
            ship.shape.addPoint(-7, 10);

            // Create shape for the ship2 sprite.
            ship2 = new AsteroidsSprite();
            ship2.shape.addPoint(0, -10);
            ship2.shape.addPoint(7, 10);
            ship2.shape.addPoint(-7, 10);

            // Create shape for the photon sprites.
            for (i = 0; i < MAX_SHOTS; i++) {
                photons[i] = new AsteroidsSprite();
                photons[i].shape.addPoint(1, 1);
                photons[i].shape.addPoint(1, -1);
                photons[i].shape.addPoint(-1, 1);
                photons[i].shape.addPoint(-1, -1);
            }

            // Create shape for the photon sprites.
            for (i = 0; i < MAX_SHOTS; i++) {
                photons2[i] = new AsteroidsSprite();
                photons2[i].shape.addPoint(1, 1);
                photons2[i].shape.addPoint(1, -1);
                photons2[i].shape.addPoint(-1, 1);
                photons2[i].shape.addPoint(-1, -1);
            }

            // Create shape for the flying saucer.
            ufo = new AsteroidsSprite();
            ufo.shape.addPoint(-15, 0);
            ufo.shape.addPoint(-10, -5);
            ufo.shape.addPoint(-5, -5);
            ufo.shape.addPoint(-5, -9);
            ufo.shape.addPoint(5, -9);
            ufo.shape.addPoint(5, -5);
            ufo.shape.addPoint(10, -5);
            ufo.shape.addPoint(15, 0);
            ufo.shape.addPoint(10, 5);
            ufo.shape.addPoint(-10, 5);

            // Create shape for the guided missle.
            missle = new AsteroidsSprite();
            missle.shape.addPoint(0, -4);
            missle.shape.addPoint(1, -3);
            missle.shape.addPoint(1, 3);
            missle.shape.addPoint(2, 4);
            missle.shape.addPoint(-2, 4);
            missle.shape.addPoint(-1, 3);
            missle.shape.addPoint(-1, -3);

            // Create asteroid sprites.
            for (i = 0; i < MAX_ROCKS; i++) {
                asteroids[i] = new AsteroidsSprite();
            }

            // Create explosion sprites.
            for (i = 0; i < MAX_SCRAP; i++) {
                explosions[i] = new AsteroidsSprite();
            }

            // Initialize game data and put us in 'game over' mode.
            highScore = 0;
            sound = false;
            detail = true;
            startGame();
            endGame();
            packetUpdate();
    }

    @Override
    public statePacket requestUpdateP1() {

        statePacket sendPacket = packetCopy();

        try {
            Thread.sleep(1);
        } catch (Exception e) {
            System.out.println(e);
        }
        int arrayLength = asteroids.length;
        for (int i = 0; i < arrayLength; i++) {

            if (isAsteroidFar(ship, asteroids[i])) {
                nullAsteroid(sendPacket, i);
            }
        }
        return sendPacket;
    }

    @Override
    public statePacket requestUpdateP2() {

        statePacket sendPacket = packetCopy();

        try {
            Thread.sleep(1);
        } catch (Exception e) {
            System.out.println(e);
        }
        int arrayLength = asteroids.length;
        for (int i = 0; i < arrayLength; i++) {

            if (isAsteroidFar(ship2, asteroids[i])) {
                nullAsteroid(sendPacket, i);
            }
        }
        return sendPacket;
    }

    @Override
    public void correctShip1(AsteroidsSprite ship1, boolean left1, boolean right1, boolean up1, boolean down1) {
        //Sets ship to its true position when error threshold is reached
        if (ship.active) {
            ship = spriteCopy(ship1);
            left = left1;
            right = right1;
            up = up1;
            down = down1;
        }
    }

    @Override
    public void correctShip2(AsteroidsSprite shipc2, boolean leftC2, boolean rightC2, boolean upC2, boolean downC2) {
        //Sets ship2 to its true position when error threshold is reached
        if (ship2.active) {
            ship2 = spriteCopy(shipc2);
            left2 = leftC2;
            right2 = rightC2;
            up2 = upC2;
            down2 = downC2;
        }
    }

    @Override
    public void startGame() {

        // Initialize game data and sprites.
        score = 0;
        shipsLeft = MAX_SHIPS;
        score2 = 0;
        shipsLeft2 = MAX_SHIPS;
        asteroidsSpeed = MIN_ROCK_SPEED;
        newShipScore = NEW_SHIP_POINTS;
        newShipScore2 = NEW_SHIP_POINTS;
        newUfoScore = NEW_UFO_POINTS;
        initShip();
        initShip2();
        initPhotons();
        initPhotons2();
        stopUfo();
        stopMissle();
        initAsteroids();
        initExplosions();
        playing = true;
        paused = false;
    }

    @Override
    public void spaceBar() {

        photonIndex++;
        if (photonIndex >= MAX_SHOTS) {
            photonIndex = 0;
        }
        photons[photonIndex].active = true;
        photons[photonIndex].currentX = ship.currentX;
        photons[photonIndex].currentY = ship.currentY;
        photons[photonIndex].deltaX = MIN_ROCK_SIZE * -Math.sin(ship.angle);
        photons[photonIndex].deltaY = MIN_ROCK_SIZE * Math.cos(ship.angle);
        photonCounter[photonIndex] = Math.min(AsteroidsSprite.width, AsteroidsSprite.height) / MIN_ROCK_SIZE;
    }

    @Override
    public void spaceBar2() {
  
        photonIndex2++;
        if (photonIndex2 >= MAX_SHOTS) {
            photonIndex2 = 0;
        }
        photons2[photonIndex2].active = true;
        photons2[photonIndex2].currentX = ship2.currentX;
        photons2[photonIndex2].currentY = ship2.currentY;
        photons2[photonIndex2].deltaX = MIN_ROCK_SIZE * -Math.sin(ship2.angle);
        photons2[photonIndex2].deltaY = MIN_ROCK_SIZE * Math.cos(ship2.angle);
        photonCounter2[photonIndex2] = Math.min(AsteroidsSprite.width, AsteroidsSprite.height) / MIN_ROCK_SIZE;
    }

    @Override
    public void pause() {
        paused = !paused;
    }

    @Override
    public void run() {
        if (!started) {
            init();
        }

        if (!paused && started) {

            // Move and process all sprites.
            updateShip();
            updateShip2();
            updatePhotons();
            updatePhotons2();
            updateUfo();
            updateMissle();
            updateAsteroids();
            updateExplosions();

            // Check the score and advance high score, add a new ship or start the flying
            // saucer as necessary.
            if (score > highScore) {
                highScore = score;
            }
            if (score2 > highScore2) {
                highScore2 = score2;
            }
            if (score > newShipScore) {
                newShipScore += NEW_SHIP_POINTS;
                shipsLeft++;
            }
            if (score2 > newShipScore2) {
                newShipScore2 += NEW_SHIP_POINTS;
                shipsLeft2++;
            }
            if (playing && score > newUfoScore && !ufo.active) {
                newUfoScore += NEW_UFO_POINTS;
                ufoPassesLeft = UFO_PASSES;
                initUfo();
            }
            if (playing && score2 > newUfoScore && !ufo.active) {
                newUfoScore += NEW_UFO_POINTS;
                ufoPassesLeft = UFO_PASSES;
                initUfo();
            }

            // If all asteroids have been destroyed create a new batch.
            if (asteroidsLeft <= 0) {
                if (--asteroidsCounter <= 0) {
                    initAsteroids();
                }
            }
            packetUpdate();
        }
        if (started) {
            packetUpdate();
        }

    }

    public void endGame() {

        // Stop ship, flying saucer, guided missle and associated sounds.
        playing = false;
        stopShip();
        stopShip2();
        stopUfo();
        stopMissle();
    }

    public void initShip() {

        ship.active = true;
        ship.angle = 0.0;
        ship.deltaAngle = 0.0;
        ship.currentX = 0.0;
        ship.currentY = 0.0;
        ship.deltaX = 0.0;
        ship.deltaY = 0.0;
        ship.render();
        hyperCounter = 0;
    }

    public void initShip2() {
        ship2.active = true;
        ship2.angle = 0.0;
        ship2.deltaAngle = 0.0;
        ship2.currentX = 0.0;
        ship2.currentY = 0.0;
        ship2.deltaX = 0.0;
        ship2.deltaY = 0.0;
        ship2.render();
        hyperCounter2 = 0;
    }

    public void updateShip() {

        double dx, dy, limit;

        if (!playing) {
            return;
        }

        // Rotate the ship if left or right cursor key is down.
        if (left) {
            ship.angle += Math.PI / 16.0;
            if (ship.angle > 2 * Math.PI) {
                ship.angle -= 2 * Math.PI;
            }
        }
        if (right) {
            ship.angle -= Math.PI / 16.0;
            if (ship.angle < 0) {
                ship.angle += 2 * Math.PI;
            }
        }

        // Fire thrusters if up or down cursor key is down. Don't let ship go past
        // the speed limit.
        dx = -Math.sin(ship.angle);
        dy = Math.cos(ship.angle);
        limit = 0.8 * MIN_ROCK_SIZE;
        if (up) {
            if (ship.deltaX + dx > -limit && ship.deltaX + dx < limit) {
                ship.deltaX += dx;
            }
            if (ship.deltaY + dy > -limit && ship.deltaY + dy < limit) {
                ship.deltaY += dy;
            }
        }
        if (down) {
            if (ship.deltaX - dx > -limit && ship.deltaX - dx < limit) {
                ship.deltaX -= dx;
            }
            if (ship.deltaY - dy > -limit && ship.deltaY - dy < limit) {
                ship.deltaY -= dy;
            }
        }

        // Move the ship. If it is currently in hyperspace, advance the countdown.
        if (ship.active) {
            ship.advance();
            ship.render();
            if (hyperCounter > 0) {
                hyperCounter--;
            }
        } // Ship is exploding, advance the countdown or create a new ship if it is
        // done exploding. The new ship is added as though it were in hyperspace.
        // (This gives the player time to move the ship if it is in imminent danger.)
        // If that was the last ship, end the game.
        else if (--shipCounter <= 0) {
            if (shipsLeft > 0) {
                initShip();
                hyperCounter = HYPER_COUNT;
            } else {
                endGame();
            }
        }
    }

    public void updateShip2() {

        double dx, dy, limit;

        if (!playing) {
            return;
        }

        // Rotate the ship if left or right cursor key is down.
        if (left2) {
            ship2.angle += Math.PI / 16.0;
            if (ship2.angle > 2 * Math.PI) {
                ship2.angle -= 2 * Math.PI;
            }
        }
        if (right2) {
            ship2.angle -= Math.PI / 16.0;
            if (ship2.angle < 0) {
                ship2.angle += 2 * Math.PI;
            }
        }

        // Fire thrusters if up or down cursor key is down. Don't let ship go past
        // the speed limit.
        dx = -Math.sin(ship2.angle);
        dy = Math.cos(ship2.angle);
        limit = 0.8 * MIN_ROCK_SIZE;
        if (up2) {
            if (ship2.deltaX + dx > -limit && ship2.deltaX + dx < limit) {
                ship2.deltaX += dx;
            }
            if (ship2.deltaY + dy > -limit && ship2.deltaY + dy < limit) {
                ship2.deltaY += dy;
            }
        }
        if (down2) {
            if (ship2.deltaX - dx > -limit && ship2.deltaX - dx < limit) {
                ship2.deltaX -= dx;
            }
            if (ship2.deltaY - dy > -limit && ship2.deltaY - dy < limit) {
                ship2.deltaY -= dy;
            }
        }

        // Move the ship. If it is currently in hyperspace, advance the countdown.
        if (ship2.active) {
            ship2.advance();
            ship2.render();
            if (hyperCounter2 > 0) {
                hyperCounter2--;
            }
        } // Ship is exploding, advance the countdown or create a new ship if it is
        // done exploding. The new ship is added as though it were in hyperspace.
        // (This gives the player time to move the ship if it is in imminent danger.)
        // If that was the last ship, end the game.
        else if (--shipCounter2 <= 0) {
            if (shipsLeft2 > 0) {
                initShip2();
                hyperCounter2 = HYPER_COUNT;
            } else {
                endGame();
            }
        }
    }

    public void stopShip() {
        ship.active = false;
        shipCounter = SCRAP_COUNT;
        if (shipsLeft > 0) {
            shipsLeft--;
        }
        thrustersPlaying = false;
    }

    public void stopShip2() {
        ship2.active = false;
        shipCounter2 = SCRAP_COUNT;
        if (shipsLeft2 > 0) {
            shipsLeft2--;
        }
        thrustersPlaying = false;
    }

    public void initPhotons() {

        int i;

        for (i = 0; i < MAX_SHOTS; i++) {
            photons[i].active = false;
            photonCounter[i] = 0;
        }
        photonIndex = 0;
    }

    public void initPhotons2() {

        int i;

        for (i = 0; i < MAX_SHOTS; i++) {
            photons2[i].active = false;
            photonCounter2[i] = 0;
        }
        photonIndex2 = 0;
    }

    public void updatePhotons() {

        int i;

        // Move any active photons. Stop it when its counter has expired.
        for (i = 0; i < MAX_SHOTS; i++) {
            if (photons[i].active) {
                photons[i].advance();
                photons[i].render();
                if (--photonCounter[i] < 0) {
                    photons[i].active = false;
                }
            }
        }
    }

    public void updatePhotons2() {

        int i;

        // Move any active photons. Stop it when its counter has expired.
        for (i = 0; i < MAX_SHOTS; i++) {
            if (photons2[i].active) {
                photons2[i].advance();
                photons2[i].render();
                if (--photonCounter2[i] < 0) {
                    photons2[i].active = false;
                }
            }
        }
    }

    public void initUfo() {

        // Randomly set flying saucer at left or right edge of the screen.
        ufo.active = true;
        ufo.currentX = -AsteroidsSprite.width / 2;
        ufo.currentY = Math.random() * AsteroidsSprite.height;
        ufo.deltaX = MIN_ROCK_SPEED + Math.random() * (MAX_ROCK_SPEED - MIN_ROCK_SPEED);
        if (Math.random() < 0.5) {
            ufo.deltaX = -ufo.deltaX;
            ufo.currentX = AsteroidsSprite.width / 2;
        }
        ufo.deltaY = MIN_ROCK_SPEED + Math.random() * (MAX_ROCK_SPEED - MIN_ROCK_SPEED);
        if (Math.random() < 0.5) {
            ufo.deltaY = -ufo.deltaY;
        }
        ufo.render();
        saucerPlaying = true;

        // Set counter for this pass.
        ufoCounter = (int) Math.floor(AsteroidsSprite.width / Math.abs(ufo.deltaX));
    }

    public void updateUfo() {

        int i, d;

        // Move the flying saucer and check for collision with a photon. Stop it when its
        // counter has expired.
        if (ufo.active) {
            ufo.advance();
            ufo.render();
            if (--ufoCounter <= 0) {
                if (--ufoPassesLeft > 0) {
                    initUfo();
                } else {
                    stopUfo();
                }
            } else {
                for (i = 0; i < MAX_SHOTS; i++) {
                    if (photons[i].active && ufo.isColliding(photons[i])) {
                        explode(ufo);
                        stopUfo();
                        score += UFO_POINTS;
                    }

                    if (photons2[i].active && ufo.isColliding(photons2[i])) {
                        explode(ufo);
                        stopUfo();
                        score2 += UFO_POINTS;
                    }
                }

                // On occassion, fire a missle at the ship if the saucer is not
                // too close to it.
                d = (int) Math.max(Math.abs(ufo.currentX - ship.currentX), Math.abs(ufo.currentY - ship.currentY));
                if (ship.active && hyperCounter <= 0 && ufo.active && !missle.active
                        && d > 4 * MAX_ROCK_SIZE && Math.random() < .03) {
                    initMissle();
                }

                d = (int) Math.max(Math.abs(ufo.currentX - ship2.currentX), Math.abs(ufo.currentY - ship2.currentY));
                if (ship2.active && hyperCounter2 <= 0 && ufo.active && !missle.active
                        && d > 4 * MAX_ROCK_SIZE && Math.random() < .03) {
                    initMissle();
                }
            }
        }
    }

    public void stopUfo() {

        ufo.active = false;
        ufoCounter = 0;
        ufoPassesLeft = 0;
        saucerPlaying = false;
    }

    public void initMissle() {

        missle.active = true;
        missle.angle = 0.0;
        missle.deltaAngle = 0.0;
        missle.currentX = ufo.currentX;
        missle.currentY = ufo.currentY;
        missle.deltaX = 0.0;
        missle.deltaY = 0.0;
        missle.render();
        missleCounter = 3 * Math.max(AsteroidsSprite.width, AsteroidsSprite.height) / MIN_ROCK_SIZE;
        misslePlaying = true;
    }

    public void updateMissle() {

        int i;

        // Move the guided missle and check for collision with ship or photon. Stop it when its
        // counter has expired.
        if (missle.active) {
            if (--missleCounter <= 0) {
                stopMissle();
            } else {
                guideMissle();
                missle.advance();
                missle.render();
                for (i = 0; i < MAX_SHOTS; i++) {
                    if (photons[i].active && missle.isColliding(photons[i])) {
                        explode(missle);
                        stopMissle();
                        score += MISSLE_POINTS;
                    }

                    if (photons2[i].active && missle.isColliding(photons2[i])) {
                        explode(missle);
                        stopMissle();
                        score2 += MISSLE_POINTS;
                    }
                }
                if (missle.active && ship.active && hyperCounter <= 0 && ship.isColliding(missle)) {
                    explode(ship);
                    stopShip();
                    stopUfo();
                    stopMissle();
                }

                if (missle.active && ship2.active && hyperCounter2 <= 0 && ship2.isColliding(missle)) {
                    explode(ship2);
                    stopShip2();
                    stopUfo();
                    stopMissle();
                }
            }
        }
    }

    public void guideMissle() {

        double dx, dy, angle;

        if (!ship.active || hyperCounter > 0) {
            return;
        }

        // Find the angle needed to hit the ship.
        dx = ship.currentX - missle.currentX;
        dy = ship.currentY - missle.currentY;
        if (dx == 0 && dy == 0) {
            angle = 0;
        }
        if (dx == 0) {
            if (dy < 0) {
                angle = -Math.PI / 2;
            } else {
                angle = Math.PI / 2;
            }
        } else {
            angle = Math.atan(Math.abs(dy / dx));
            if (dy > 0) {
                angle = -angle;
            }
            if (dx < 0) {
                angle = Math.PI - angle;
            }
        }

        // Adjust angle for screen coordinates.
        missle.angle = angle - Math.PI / 2;

        // Change the missle's angle so that it points toward the ship.
        missle.deltaX = MIN_ROCK_SIZE / 3 * -Math.sin(missle.angle);
        missle.deltaY = MIN_ROCK_SIZE / 3 * Math.cos(missle.angle);
    }

    public void stopMissle() {

        missle.active = false;
        missleCounter = 0;
        misslePlaying = false;
    }

    public void initAsteroids() {

        int i, j;
        int s;
        double theta, r;
        int x, y;

        // Create random shapes, positions and movements for each asteroid.
        for (i = 0; i < MAX_ROCKS; i++) {

            // Create a jagged shape for the asteroid and give it a random rotation.
            asteroids[i].shape = new Polygon();
            s = MIN_ROCK_SIDES + (int) (Math.random() * (MAX_ROCK_SIDES - MIN_ROCK_SIDES));
            for (j = 0; j < s; j++) {
                theta = 2 * Math.PI / s * j;
                r = MIN_ROCK_SIZE + (int) (Math.random() * (MAX_ROCK_SIZE - MIN_ROCK_SIZE));
                x = (int) -Math.round(r * Math.sin(theta));
                y = (int) Math.round(r * Math.cos(theta));
                asteroids[i].shape.addPoint(x, y);
            }
            asteroids[i].active = true;
            asteroids[i].angle = 0.0;
            asteroids[i].deltaAngle = (Math.random() - 0.5) / 10;

            // Place the asteroid at one edge of the screen.
            if (Math.random() < 0.5) {
                asteroids[i].currentX = -AsteroidsSprite.width / 2;
                if (Math.random() < 0.5) {
                    asteroids[i].currentX = AsteroidsSprite.width / 2;
                }
                asteroids[i].currentY = Math.random() * AsteroidsSprite.height;
            } else {
                asteroids[i].currentX = Math.random() * AsteroidsSprite.width;
                asteroids[i].currentY = -AsteroidsSprite.height / 2;
                if (Math.random() < 0.5) {
                    asteroids[i].currentY = AsteroidsSprite.height / 2;
                }
            }

            // Set a random motion for the asteroid.
            asteroids[i].deltaX = Math.random() * asteroidsSpeed;
            if (Math.random() < 0.5) {
                asteroids[i].deltaX = -asteroids[i].deltaX;
            }
            asteroids[i].deltaY = Math.random() * asteroidsSpeed;
            if (Math.random() < 0.5) {
                asteroids[i].deltaY = -asteroids[i].deltaY;
            }

            asteroids[i].render();
            asteroidIsSmall[i] = false;
        }

        asteroidsCounter = STORM_PAUSE;
        asteroidsLeft = MAX_ROCKS;
        if (asteroidsSpeed < MAX_ROCK_SPEED) {
            asteroidsSpeed++;
        }
    }

    public void initSmallAsteroids(int n) {

        int count;
        int i, j;
        int s;
        double tempX, tempY;
        double theta, r;
        int x, y;

        // Create one or two smaller asteroids from a larger one using inactive asteroids. The new
        // asteroids will be placed in the same position as the old one but will have a new, smaller
        // shape and new, randomly generated movements.
        count = 0;
        i = 0;
        tempX = asteroids[n].currentX;
        tempY = asteroids[n].currentY;
        do {
            if (!asteroids[i].active) {
                asteroids[i].shape = new Polygon();
                s = MIN_ROCK_SIDES + (int) (Math.random() * (MAX_ROCK_SIDES - MIN_ROCK_SIDES));
                for (j = 0; j < s; j++) {
                    theta = 2 * Math.PI / s * j;
                    r = (MIN_ROCK_SIZE + (int) (Math.random() * (MAX_ROCK_SIZE - MIN_ROCK_SIZE))) / 2;
                    x = (int) -Math.round(r * Math.sin(theta));
                    y = (int) Math.round(r * Math.cos(theta));
                    asteroids[i].shape.addPoint(x, y);
                }
                asteroids[i].active = true;
                asteroids[i].angle = 0.0;
                asteroids[i].deltaAngle = (Math.random() - 0.5) / 10;
                asteroids[i].currentX = tempX;
                asteroids[i].currentY = tempY;
                asteroids[i].deltaX = Math.random() * 2 * asteroidsSpeed - asteroidsSpeed;
                asteroids[i].deltaY = Math.random() * 2 * asteroidsSpeed - asteroidsSpeed;
                asteroids[i].render();
                asteroidIsSmall[i] = true;
                count++;
                asteroidsLeft++;
            }
            i++;
        } while (i < MAX_ROCKS && count < 2);
    }

    public void updateAsteroids() {

        int i, j;

        // Move any active asteroids and check for collisions.
        for (i = 0; i < MAX_ROCKS; i++) {
            if (asteroids[i].active) {
                asteroids[i].advance();
                asteroids[i].render();

                // If hit by photon, kill asteroid and advance score. If asteroid is large,
                // make some smaller ones to replace it.
                for (j = 0; j < MAX_SHOTS; j++) {
                    if (photons[j].active && asteroids[i].active && asteroids[i].isColliding(photons[j])) {
                        asteroidsLeft--;
                        asteroids[i].active = false;
                        photons[j].active = false;
                        explode(asteroids[i]);
                        if (!asteroidIsSmall[i]) {
                            score += BIG_POINTS;
                            initSmallAsteroids(i);
                        } else {
                            score += SMALL_POINTS;
                        }
                    }
                    if (photons2[j].active && asteroids[i].active && asteroids[i].isColliding(photons2[j])) {
                        asteroidsLeft--;
                        asteroids[i].active = false;
                        photons2[j].active = false;
                        explode(asteroids[i]);
                        if (!asteroidIsSmall[i]) {
                            score2 += BIG_POINTS;
                            initSmallAsteroids(i);
                        } else {
                            score2 += SMALL_POINTS;
                        }
                    }
                }

                // If the ship is not in hyperspace, see if it is hit.
                if (ship.active && hyperCounter <= 0 && asteroids[i].active && asteroids[i].isColliding(ship)) {
                    explode(ship);
                    stopShip();
                    stopUfo();
                    stopMissle();
                }

                // If the ship is not in hyperspace, see if it is hit.
                if (ship2.active && hyperCounter2 <= 0 && asteroids[i].active && asteroids[i].isColliding(ship2)) {
                    explode(ship2);
                    stopShip2();
                    stopUfo();
                    stopMissle();
                }
            }
        }
    }

    public void initExplosions() {

        int i;

        for (i = 0; i < MAX_SCRAP; i++) {
            explosions[i].shape = new Polygon();
            explosions[i].active = false;
            explosionCounter[i] = 0;
        }
        explosionIndex = 0;
    }

    public void explode(AsteroidsSprite s) {

        int c, i, j;

        // Create sprites for explosion animation. The each individual line segment of the given sprite
        // is used to create a new sprite that will move outward  from the sprite's original position
        // with a random rotation.
        s.render();
        c = 2;
        if (detail || s.sprite.npoints < 6) {
            c = 1;
        }
        for (i = 0; i < s.sprite.npoints; i += c) {
            explosionIndex++;
            if (explosionIndex >= MAX_SCRAP) {
                explosionIndex = 0;
            }
            explosions[explosionIndex].active = true;
            explosions[explosionIndex].shape = new Polygon();
            explosions[explosionIndex].shape.addPoint(s.shape.xpoints[i], s.shape.ypoints[i]);
            j = i + 1;
            if (j >= s.sprite.npoints) {
                j -= s.sprite.npoints;
            }
            explosions[explosionIndex].shape.addPoint(s.shape.xpoints[j], s.shape.ypoints[j]);
            explosions[explosionIndex].angle = s.angle;
            explosions[explosionIndex].deltaAngle = (Math.random() * 2 * Math.PI - Math.PI) / 15;
            explosions[explosionIndex].currentX = s.currentX;
            explosions[explosionIndex].currentY = s.currentY;
            explosions[explosionIndex].deltaX = -s.shape.xpoints[i] / 5;
            explosions[explosionIndex].deltaY = -s.shape.ypoints[i] / 5;
            explosionCounter[explosionIndex] = SCRAP_COUNT;
        }
    }

    public void updateExplosions() {

        int i;

        // Move any active explosion debris. Stop explosion when its counter has expired.
        for (i = 0; i < MAX_SCRAP; i++) {
            if (explosions[i].active) {
                explosions[i].advance();
                explosions[i].render();
                if (--explosionCounter[i] <= 0) {
                    explosions[i].active = false;
                }
            }
        }
    }

    public static void main(String[] args) {

        try {
            Naming.rebind("//localhost/gameServer", new Server());
            ServerInterface Server = (ServerInterface) Naming.lookup("//localhost/gameServer");
            System.out.println("Game Server Ready");
            while (true) {
                Server.run();
                Thread.sleep(50);
            }

        } catch (Exception e) {
            System.out.println(e);
        }

    }

}
