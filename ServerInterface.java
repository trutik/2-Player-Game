package multiplayer;

import java.rmi.Remote;
import java.rmi.RemoteException;



public interface ServerInterface extends Remote {
    
    public void run() throws RemoteException;
    public void startGame() throws RemoteException;
    public void spaceBar() throws RemoteException;
    public void spaceBar2() throws RemoteException;
    public void pause() throws RemoteException;
    public void correctShip1(AsteroidsSprite ship,boolean left,boolean right,boolean up,boolean down) throws RemoteException;
    public void correctShip2(AsteroidsSprite ship2, boolean leftC2, boolean rightC2, boolean upC2, boolean downC2) throws RemoteException;
    public statePacket requestUpdateP1() throws RemoteException;
    public statePacket requestUpdateP2() throws RemoteException;
}