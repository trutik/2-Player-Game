
package multiplayer;

import java.awt.*;
import java.io.Serializable;


public class statePacket implements Serializable {

    AsteroidsSprite ship;
    AsteroidsSprite ship2;
    AsteroidsSprite ufo;
    AsteroidsSprite missle;
    AsteroidsSprite[] photons;
    AsteroidsSprite[] photons2;
    AsteroidsSprite[] asteroids;
    AsteroidsSprite[] explosions;
    Point[] stars;
    int[] photonCounter;   // Time counter for life of a photon.
    int[] photonCounter2;    // Time counter for life of a photon.
    int[] explosionCounter;  // Time counters for explosions
    int photonIndex2;                           // Next available photon sprite.
    int asteroidsCounter;                            // Break-time counter.
    int asteroidsSpeed;                              // Asteroid speed.
    int asteroidsLeft;                               // Number of active asteroids.
    int photonIndex;
    int explosionIndex;                         // Next available explosion sprite.
    int shipsLeft;       // Number of ships left to play, including current one.
    int shipCounter;     // Time counter for ship explosion.
    int hyperCounter;    // Time counter for hyperspace.
    int shipsLeft2;       // Number of ships left to play, including current one.
    int shipCounter2;     // Time counter for ship explosion.
    int hyperCounter2;    // Time counter for hyperspace.
    int score;
    int highScore;
    int newShipScore;
    int newUfoScore;
    int score2;
    int highScore2;
    int newShipScore2;
    int newUfoScore2;
    int numStars;
    boolean showOtherPlayer = true;
    boolean loaded = false;
    boolean paused;
    boolean playing;
    boolean sound;
    boolean detail;
}
