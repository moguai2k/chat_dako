/*
 * $Date$
 * $Revision$
 * $Author$
 * $HeadURL$
 * $Id$
 */

package edu.hm.training.bo;

import java.io.Serializable;



/**
 * Nachrichtenobjekt es implementiert Serializable um eine Serialisierung zu ermöglichen
 * @author christoph
 *
 */
public class Message implements Serializable {
	/**
	 * Die brauchen wir eigentlich nicht, ist allerdings für equals performanter
	 */
	private static final long serialVersionUID = -7082463627360701271L;
	private String text;
	private int number;
	
	/**
	 * ctor
	 * @param text
	 * @param number
	 */
	public Message(String text, int number) {
		super();
		this.text = text;
		this.number = number;
	}
	
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	
	@Override
	public String toString(){
		/**
		 * String + "text" + String ist unsauber und unperformant
		 * Strings werden mit Stringbuilder zusammengefügt
		 */
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("nachricht: ").append(text).append(" number:").append(number);
		return stringBuilder.toString();
	}
}
