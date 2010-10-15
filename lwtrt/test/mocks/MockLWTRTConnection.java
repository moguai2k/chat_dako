package test.mocks;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import lwtrt.LWTRTConnection;
import lwtrt.ex.LWTRTException;

public class MockLWTRTConnection implements LWTRTConnection {
     private static Log log = LogFactory.getLog(MockLWTRTConnection.class);

    Socket socket;
    ObjectInputStream oin;
    ObjectOutputStream oout;

    public MockLWTRTConnection(Socket socket) {
        this.socket = socket;
        retreiveStreamsIfConnected(socket);
    }

    private void retreiveStreamsIfConnected(Socket socket) {
        if (socket.isConnected()) {
            try {
                oout = new ObjectOutputStream(socket.getOutputStream());
                oin = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void acceptDisconnection() throws LWTRTException {
    // TODO Auto-generated method stub

    }

    @Override
    public void disconnect() throws LWTRTException {
        log.trace("disconnect, localport:" + socket.getLocalPort());
        try {
            socket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    public void ping() {
    // TODO Auto-generated method stub
    }

    @Override
    public Object receive() throws LWTRTException {
        log.trace("receive, localport:" + socket.getLocalPort());
        if (socket.isClosed()) {
            throw new LWTRTException("Socket is closed");
        }
        try {
            return oin.readObject();
        } catch (IOException e) {
        	throw new LWTRTException("Verbindung wurde disconnected");
        } catch (ClassNotFoundException e) {
            throw new LWTRTException(e);
        }
    }

    @Override
    public void send(Object pdu) throws LWTRTException {
        log.trace("send from " + socket.getLocalPort() + " to " + socket.getPort());
        if (socket.isClosed()) {
            throw new LWTRTException("Socket is closed");
        }
        try {
            oout.writeObject(pdu);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
