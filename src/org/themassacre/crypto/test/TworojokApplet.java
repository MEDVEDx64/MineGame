package org.themassacre.crypto.test;

import org.themassacre.crypto.*;
import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.io.UnsupportedEncodingException;

public class TworojokApplet extends Applet implements ActionListener {

	private static final long serialVersionUID = -2192554938426356262L;
	TextField inputMsg, outHash;
	Button hashBtn;
	
	public void init() {
		inputMsg = new TextField(100);
		add(inputMsg);
		
		outHash = new TextField(128);
		outHash.setFont(new Font("courier", 0, 9));
		outHash.setEditable(false);
		add(outHash);
		
		hashBtn = new Button("Compute");
		hashBtn.addActionListener(this);
		add(hashBtn);
	}
	
	public void actionPerformed(ActionEvent ae) {
		byte[] hash = new byte[1];
		try {
			hash = Tworojok64.computeDigest(inputMsg.getText().getBytes("UTF-8"));
		} catch(UnsupportedEncodingException e) {
		}
		
		StringBuffer buf = new StringBuffer();
		for(int i = 0; i < hash.length; i++)
			buf.append(String.format("%02x", hash[i]));
		outHash.setText(new String(buf));
	}
}
