package lwtrt;

import lwtrt.ex.LWTRTException;

/**
 * Repr�sentiert eine Verbindung, �ber die ein Dienstnehmer mit einem Verbindungspartner Daten austauschen kann.
 * <p>
 * Die {@link LWTRTConnection} stellt eine gesicherte Verbindung zur Verf�gung. Beim Senden von Daten wird der Empfang
 * der Daten nach verfolgt. Beim Empfang von Daten wird die Reihenfolge �berwacht sowie mehrfach ausgelieferte Daten
 * erkannt.
 * 
 * @author dako.cs.hm.edu
 */
public interface LWTRTConnection {

    /**
     * Aufgebaute Verbindung wird aktiv abgebaut.
     * 
     * @throws LWTRTException Fehler die w�hrend des aktiven Verbindunsabbaus auftreten
     */
    public void disconnect() throws LWTRTException;

    /**
     * Akzeptieren einer eingegangenen Nachricht �ber den Abbau der Verbindung beim passiven Verbindungsabbau.
     * 
     * @throws LWTRTException Fehler die w�hrend des passiven Verbindungsabaus auftreten
     */
    public void acceptDisconnection() throws LWTRTException;

    /**
     * Daten an den Verbindungspartner �bertragen.
     * <p>
     * Nach dem Senden der Daten wird auf eine Best�tigung durch den Empf�nger gewartet. Ist die Best�tigung nicht vor
     * Ablauf eines Timer eingetroffen, wird der Sendeversuch wiederholt. Nach zwei erfolglosen Wiederholungen wird das
     * Senden abgebrochen und die Verbindung gilt als abgebaut.
     * 
     * @param pdu Daten die zu �bertragen sind
     * @exception LWTRTException Fehler die w�hrend des Senden auftreten (zB Abbruch wegen Timeout)
     */
    public void send(Object pdu) throws LWTRTException;

    /**
     * Daten vom Verbindungspartner empfangen.
     * <p>
     * Es wird gewartet, bis Daten vom Verbindungspartner vorliegen. Wurden mehrere Packete vom Verbindungspartner
     * gesendet, werden die Daten in der richtigen Reihenfolge (FIFO) an den Dienstnehmer �bergeben. Pakete die mehrmals
     * empfangen wurden, werden nur einmal an den Dienstnehmer �bergeben.
     * 
     * @return empfangenes Paket
     * @throws LWTRTException Fehler die w�rend des Empfangen auftreten
     */
    public Object receive() throws LWTRTException;

    /**
     * Durchf�hrung einer Lebend�berwachung.
     * <p>
     * An den Verbindungspartner wird eine Anfrage (ping) gesendet, die umgehend beantwortet werden muss. Wird auf die
     * Anfrage nicht vor Ablauf eines Timers geantwortet wird zwei mal versucht die Anfrage zu wiederholen. Ist auch der
     * dritte Versuch ohne Erfolg (keine Antwort) gilt die Verbindung als abgebaut.
     * 
     * @throws LWTRTException Fehler die w�hrend der Lebend�berwachung auftreten (zB Timeout)
     */
    public void ping() throws LWTRTException;
}
