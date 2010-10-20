package lwtrt;

import lwtrt.ex.LWTRTException;

/**
 * Repräsentiert eine Verbindung, über die ein Dienstnehmer mit einem Verbindungspartner Daten austauschen kann.
 * <p>
 * Die {@link LWTRTConnection} stellt eine gesicherte Verbindung zur Verfügung. Beim Senden von Daten wird der Empfang
 * der Daten nach verfolgt. Beim Empfang von Daten wird die Reihenfolge überwacht sowie mehrfach ausgelieferte Daten
 * erkannt.
 * 
 * @author dako.cs.hm.edu
 */
public interface LWTRTConnection {

    /**
     * Aufgebaute Verbindung wird aktiv abgebaut.
     * 
     * @throws LWTRTException Fehler die während des aktiven Verbindunsabbaus auftreten
     */
    public void disconnect() throws LWTRTException;

    /**
     * Akzeptieren einer eingegangenen Nachricht über den Abbau der Verbindung beim passiven Verbindungsabbau.
     * 
     * @throws LWTRTException Fehler die während des passiven Verbindungsabaus auftreten
     */
    public void acceptDisconnection() throws LWTRTException;

    /**
     * Daten an den Verbindungspartner übertragen.
     * <p>
     * Nach dem Senden der Daten wird auf eine Bestätigung durch den Empfänger gewartet. Ist die Bestätigung nicht vor
     * Ablauf eines Timer eingetroffen, wird der Sendeversuch wiederholt. Nach zwei erfolglosen Wiederholungen wird das
     * Senden abgebrochen und die Verbindung gilt als abgebaut.
     * 
     * @param pdu Daten die zu übertragen sind
     * @exception LWTRTException Fehler die während des Senden auftreten (zB Abbruch wegen Timeout)
     */
    public void send(Object pdu) throws LWTRTException;

    /**
     * Daten vom Verbindungspartner empfangen.
     * <p>
     * Es wird gewartet, bis Daten vom Verbindungspartner vorliegen. Wurden mehrere Packete vom Verbindungspartner
     * gesendet, werden die Daten in der richtigen Reihenfolge (FIFO) an den Dienstnehmer übergeben. Pakete die mehrmals
     * empfangen wurden, werden nur einmal an den Dienstnehmer übergeben.
     * 
     * @return empfangenes Paket
     * @throws LWTRTException Fehler die während des Empfangen auftreten
     */
    public Object receive() throws LWTRTException;

    /**
     * Durchführung einer Lebendüberwachung.
     * <p>
     * An den Verbindungspartner wird eine Anfrage (ping) gesendet, die umgehend beantwortet werden muss. Wird auf die
     * Anfrage nicht vor Ablauf eines Timers geantwortet wird zwei mal versucht die Anfrage zu wiederholen. Ist auch der
     * dritte Versuch ohne Erfolg (keine Antwort) gilt die Verbindung als abgebaut.
     * 
     * @throws LWTRTException Fehler die während der Lebendüberwachung auftreten (zB Timeout)
     */
    public void ping() throws LWTRTException;
}
