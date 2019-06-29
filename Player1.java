package multiplayer;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.awt.*;
import java.applet.Applet;

public class Player1 extends Applet implements Runnable {

    //Global variables
    public static int MAX = 4;
    private static ServerInterface remoteServerObj;
    //Game state 
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
    Point[] stars;

    // Game data.
    int numStars;
    int score;
    int highScore;
    int newShipScore;
    int newUfoScore;
    int score2;
    int highScore2;
    int newShipScore2;
    int newUfoScore2;
    // Second Player.

    boolean showOtherPlayer = true;
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
    boolean[] asteroidIsSmall = new boolean[MAX_ROCKS];    // Asteroid size flag.

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

    int photonIndex;                           // Next available photon sprite.

    int photonIndex2;
    // Flying saucer data.
    int ufoPassesLeft;    // Number of flying saucer passes.
    int ufoCounter;       // Time counter for each pass.
    // Missle data.
    int missleCounter;    // Counter for life of missle.
    // Asteroid data.

    int asteroidsCounter;                            // Break-time counter.
    int asteroidsSpeed;                              // Asteroid speed.
    int asteroidsLeft;                               // Number of active asteroids.
    int explosionIndex;                         // Next available explosion sprite.
    int[] photonCounter2 = new int[MAX_SHOTS];    // Time counter for life of a photon.
    // Explosion data.
    int[] explosionCounter = new int[MAX_SCRAP];  // Time counters for explosions.
    int[] photonCounter = new int[MAX_SHOTS];    // Time counter for life of a photon.

    // Flags for looping sound clips.
    boolean thrustersPlaying;
    boolean saucerPlaying;
    boolean misslePlaying;

    boolean initialised = false;

    // Values for the offscreen image.
    Dimension offDimension;
    Image offImage;
    Graphics offGraphics;

    // Font data.
    Font font = new Font("Helvetica", Font.BOLD, 12);
    FontMetrics fm;
    int fontWidth;
    int fontHeight;

    @Override
    public void init() {
        resize(600, 600);
        Graphics g;
        g = getGraphics();

        try {
            remoteServerObj = (ServerInterface) Naming.lookup("//localhost/gameServer");
            statePacket newState = remoteServerObj.requestUpdateP1();
            updateClient(newState);
            ship = spriteCopy(newState.ship);
        } catch (Exception e) {
            System.out.println(e);
        }
        g.setFont(font);
        fm = g.getFontMetrics();
        fontWidth = fm.getMaxAdvance();
        fontHeight = fm.getHeight();
    }

    @Override
    public void paint(Graphics g) {
//        try {
//            gameState newGameState = look_up2.getServerState(1);
//            setGameState(newGameState);
//        } catch (Exception e) {
//            System.out.println("Remote exception at paint");
//            System.out.println(e);
//            System.out.println("That was exception lol");
//        }
        update(g);
    }

    @Override
    public void update(Graphics g) {

        Dimension d = size();
        int i;
        int c;
        String s;

        // Create the offscreen graphics context, if no good one exists.
        if (offGraphics == null || d.width != offDimension.width || d.height != offDimension.height) {
            offDimension = d;
            offImage = createImage(d.width, d.height);
            offGraphics = offImage.getGraphics();
        }

        // Fill in background and stars.
        offGraphics.setColor(Color.black);
        offGraphics.fillRect(0, 0, d.width, d.height);
        if (detail) {
            offGraphics.setColor(Color.white);
            for (i = 0; i < numStars; i++) {
                offGraphics.drawLine(stars[i].x, stars[i].y, stars[i].x, stars[i].y);
            }
        }

        // Draw photon bullets.
        offGraphics.setColor(Color.white);
        for (i = 0; i < MAX_SHOTS; i++) {
            if (photons[i].active) {
                offGraphics.drawPolygon(photons[i].sprite);
            }
        }
        if (showOtherPlayer) {
            for (i = 0; i < MAX_SHOTS; i++) {
                if (photons2[i].active) {
                    offGraphics.drawPolygon(photons2[i].sprite);
                }
            }
        }

        // Draw the guided missle, counter is used to quickly fade color to black when near expiration.
        c = Math.min(missleCounter * 24, 255);
        offGraphics.setColor(new Color(c, c, c));
        if (missle.active) {
            offGraphics.drawPolygon(missle.sprite);
            offGraphics.drawLine(missle.sprite.xpoints[missle.sprite.npoints - 1], missle.sprite.ypoints[missle.sprite.npoints - 1],
                    missle.sprite.xpoints[0], missle.sprite.ypoints[0]);
        }

        // Draw the asteroids.
        for (i = 0; i < MAX_ROCKS; i++) {
            //if asteroids removed, dont paint them
            if (asteroids[i] == null) {
                continue;
            }
            if (asteroids[i].active) {
                if (detail) {
                    offGraphics.setColor(Color.black);
                    offGraphics.fillPolygon(asteroids[i].sprite);
                }
                offGraphics.setColor(Color.white);
                offGraphics.drawPolygon(asteroids[i].sprite);
                offGraphics.drawLine(asteroids[i].sprite.xpoints[asteroids[i].sprite.npoints - 1], asteroids[i].sprite.ypoints[asteroids[i].sprite.npoints - 1],
                        asteroids[i].sprite.xpoints[0], asteroids[i].sprite.ypoints[0]);
            }
        }

        // Draw the flying saucer.
        if (ufo.active) {
            if (detail) {
                offGraphics.setColor(Color.black);
                offGraphics.fillPolygon(ufo.sprite);
            }
            offGraphics.setColor(Color.white);
            offGraphics.drawPolygon(ufo.sprite);
            offGraphics.drawLine(ufo.sprite.xpoints[ufo.sprite.npoints - 1], ufo.sprite.ypoints[ufo.sprite.npoints - 1],
                    ufo.sprite.xpoints[0], ufo.sprite.ypoints[0]);
        }

        // Draw the ship, counter is used to fade color to white on hyperspace.
        c = 255 - (255 / HYPER_COUNT) * hyperCounter;
        if (ship.active) {
            if (detail && hyperCounter == 0) {
                offGraphics.setColor(Color.black);
                offGraphics.fillPolygon(ship.sprite);
            }
            offGraphics.setColor(new Color(c, c, c));
            offGraphics.drawPolygon(ship.sprite);
            offGraphics.drawLine(ship.sprite.xpoints[ship.sprite.npoints - 1], ship.sprite.ypoints[ship.sprite.npoints - 1],
                    ship.sprite.xpoints[0], ship.sprite.ypoints[0]);
        }

        if (showOtherPlayer) {
            c = 255 - (255 / HYPER_COUNT) * hyperCounter2;
            if (ship2.active) {
                if (detail && hyperCounter2 == 0) {
                    offGraphics.setColor(Color.black);
                    offGraphics.fillPolygon(ship2.sprite);
                }
                offGraphics.setColor(new Color(c, c, c));
                offGraphics.drawPolygon(ship2.sprite);
                offGraphics.drawLine(ship2.sprite.xpoints[ship2.sprite.npoints - 1], ship2.sprite.ypoints[ship2.sprite.npoints - 1],
                        ship2.sprite.xpoints[0], ship2.sprite.ypoints[0]);
            }
        }

        // Draw any explosion debris, counters are used to fade color to black.
        for (i = 0; i < MAX_SCRAP; i++) {
            if (explosions[i].active) {
                c = (255 / SCRAP_COUNT) * explosionCounter[i];
                offGraphics.setColor(new Color(c, c, c));
                offGraphics.drawPolygon(explosions[i].sprite);
            }
        }

        // Display status and messages.
        offGraphics.setFont(font);
        offGraphics.setColor(Color.white);

        offGraphics.drawString("Score: " + score, fontWidth, fontHeight);
        offGraphics.drawString("Ships: " + shipsLeft, fontWidth, d.height - fontHeight);
        s = "High: " + highScore;
        offGraphics.drawString(s, d.width - (fontWidth + fm.stringWidth(s)), fontHeight);
        if (!sound) {
            s = "Mute";
            offGraphics.drawString(s, d.width - (fontWidth + fm.stringWidth(s)), d.height - fontHeight);
        }

        if (!playing) {
            s = "A S T E R O I D S";
            offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 2);
            s = "Copyright 1998 by Mike Hall";
            offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 2 + fontHeight);
            if (!loaded) {
                s = "Loading sounds...";
                offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 4);
            } else {
                s = "Game Over";
                offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 4);
                s = "'S' to Start";
                offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 4 + fontHeight);
            }
        } else if (paused) {
            System.out.println("PAUSED");
            s = "Game Paused";
            offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 4);
        }

        // Copy the off screen buffer to the screen.
        g.drawImage(offImage, 0, 0, this);
    }

    @Override
    public void start() {

        if (loopThread == null) {
            loopThread = new Thread(this);
            loopThread.start();
        }
        if (!loaded && loadThread == null) {
            loadThread = new Thread(this);
            loadThread.start();
        }
    }

    @Override
    public void stop() {

        if (loopThread != null) {
            loopThread.stop();
            loopThread = null;
        }
        if (loadThread != null) {
            loadThread.stop();
            loadThread = null;
        }
    }

    @Override
    public void run() {

        long startTime;

        // Lower this thread's priority and get the current time.
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        startTime = System.currentTimeMillis();

        // This is the main loop.
        while (Thread.currentThread() == loopThread) {
            // Update the screen and set the timer for the next loop.
            //Run method @ server plz
            try {
                //  look_up2.runAtServer();
                statePacket newState = remoteServerObj.requestUpdateP1();
                deadReckoning(newState);
                updateClient(newState);
                ship.active = newState.ship.active;
                updateShip();

            } catch (RemoteException e) {
                System.out.println("Remote exception at run()");
            }
            repaint();
            try {
                startTime += DELAY;
                Thread.sleep(Math.max(0, startTime - System.currentTimeMillis()));
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    @Override
    public boolean keyDown(Event e, int key) {

        try {
            // Check if any cursor keys have been pressed and set flags.
            if (key == Event.LEFT) {
                left = true;
            }
            if (key == Event.RIGHT) {
                right = true;
            }
            if (key == Event.UP) {
                up = true;
            }
            if (key == Event.DOWN) {
                down = true;
            }

            // Spacebar: fire a photon and start its counter.
            if (key == 32 && ship.active) {
                remoteServerObj.spaceBar();
            }

            // 'H' key: warp ship into hyperspace by moving to a random location and starting counter.
            if (key == 104 && ship.active && hyperCounter <= 0) {
                ship.currentX = Math.random() * AsteroidsSprite.width;
                ship.currentY = Math.random() * AsteroidsSprite.height;
                hyperCounter = HYPER_COUNT;
            }

            // 'P' key: toggle pause mode 
            if (key == 112) {
                paused = !paused;
                remoteServerObj.pause();
            }

            // 'D' key: toggle graphics detail on or off.
            if (key == 100) {
                detail = !detail;
            }

            // 'S' key: start the game, if not already in progress.
            if (key == 115 && loaded && !playing) {
                remoteServerObj.startGame();
            }

        } catch (RemoteException r) {
            System.out.println(r);
        }
        return true;
    }

    @Override
    public boolean keyUp(Event e, int key) {
        if (key == Event.LEFT) {
            left = false;
        }
        if (key == Event.RIGHT) {
            right = false;
        }
        if (key == Event.UP) {
            up = false;
        }
        if (key == Event.DOWN) {
            down = false;
        }
        return true;
    }

    public void updateClient(statePacket packet) {
        //Update client state variables
        asteroidsCounter = packet.asteroidsCounter;                            // Break-time counter.
        asteroidsSpeed = packet.asteroidsSpeed;                              // Asteroid speed.
        asteroidsLeft = packet.asteroidsLeft;                               // Number of active asteroids.
        explosionCounter = packet.explosionCounter;  // Time counters for explosions.
        explosionIndex = packet.explosionIndex;                         // Next available explosion sprite.
        explosions = packet.explosions;
        numStars = packet.numStars;
        stars = packet.stars;
        score = packet.score;
        highScore = packet.highScore;
        newShipScore = packet.newShipScore;
        newUfoScore = packet.newUfoScore;
        score2 = packet.score2;
        highScore2 = packet.highScore2;
        newShipScore2 = packet.newShipScore2;
        paused = packet.paused;
        playing = packet.playing;
        sound = packet.sound;
        detail = packet.detail;
        showOtherPlayer = packet.showOtherPlayer;
        ship2 = packet.ship2;
        ufo = packet.ufo;
        missle = packet.missle;
        photons = packet.photons;
        photons2 = packet.photons2;
        asteroids = packet.asteroids;
        photonCounter2 = packet.photonCounter2;    // Time counter for life of a photon.
        photonIndex2 = packet.photonIndex2;                           // Next available photon sprite.
        shipsLeft = packet.shipsLeft;       // Number of ships left to play, including current one.
        shipCounter = packet.shipCounter;     // Time counter for ship explosion.
        hyperCounter = packet.hyperCounter;    // Time counter for hyperspace.
        shipsLeft2 = packet.shipsLeft2;       // Number of ships left to play, including current one.
        shipCounter2 = packet.shipCounter2;     // Time counter for ship explosion.
        hyperCounter2 = packet.hyperCounter2;    // Time counter for hyperspace.
        photonCounter = packet.photonCounter;    // Time counter for life of a photon.
        photonIndex = packet.photonIndex;                           // Next available photon sprite.

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

    public void deadReckoning(statePacket serverState) {
        //Calculate error (distance between true position and server position) 
        double serverX = serverState.ship.currentX;
        double serverY = serverState.ship.currentY;
        double trueX = ship.currentX;
        double trueY = ship.currentY;
        double error = Math.sqrt(Math.pow(trueX - serverX, 2) + Math.pow(trueY - serverY, 2));
        // If error is above threshold then update server with true position
        if (error > 10) {
            try {
                remoteServerObj.correctShip1(ship, left, right, up, down);
            } catch (RemoteException r) {
                System.out.println(r);
            }
        }
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
                initShip();
            }
        }
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

}
