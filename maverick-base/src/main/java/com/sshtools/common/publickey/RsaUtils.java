/*
 *    _           _             _   _
 *   (_) __ _  __| | __ _ _ __ | |_(_)_   _____
 *   | |/ _` |/ _` |/ _` | '_ \| __| \ \ / / _ \
 *   | | (_| | (_| | (_| | |_) | |_| |\ V /  __/
 *  _/ |\__,_|\__,_|\__,_| .__/ \__|_| \_/ \___|
 * |__/                  |_|
 *
 * This file is part of the Maverick Synergy Hotfixes Java SSH API
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 *
 * Copyright (C) 2002-2021 JADAPTIVE Limited - All Rights Reserved
 *
 * Use of this software may also be covered by third-party licenses depending on the choices you make about what features to use.
 *
 * Please visit the link below to see additional third-party licenses and copyrights
 *
 * https://www.jadaptive.com/app/manpage/en/article/1565029/What-third-party-dependencies-does-the-Maverick-Synergy-API-have
 */

package com.sshtools.common.publickey;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.StringTokenizer;

import javax.crypto.Cipher;

import com.sshtools.common.ssh.components.SshRsaPrivateKey;
import com.sshtools.common.ssh.components.SshRsaPublicKey;
import com.sshtools.common.util.Base64;

public class RsaUtils {

	public static String encrypt(SshRsaPrivateKey privateKey, String toEncrypt) throws Exception {
		int pos = 0;
		StringBuffer ret = new StringBuffer();
		int blockLength = privateKey.getModulus().bitLength() / 16;
		while(pos < toEncrypt.length()) {
			int count = Math.min(toEncrypt.length() - pos, blockLength);
			ret.append(doEncrypt(toEncrypt.substring(pos, pos+count), privateKey.getJCEPrivateKey()));
			ret.append('|');
			pos += count;
		}
		return ret.toString();
	}
	
	public static String decrypt(SshRsaPublicKey publicKey, String toDecrypt) throws Exception {
		StringBuffer ret = new StringBuffer();
		StringTokenizer t = new StringTokenizer(toDecrypt, "|");
		
		while(t.hasMoreTokens()) {
		
			String data = t.nextToken();
			ret.append(doDecrypt(data, publicKey.getJCEPublicKey()));
		}

		return ret.toString();
	}

	private static String doEncrypt(String toEncrypt, PrivateKey privateKey) throws Exception{

		Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		c.init(Cipher.ENCRYPT_MODE, privateKey);
		return Base64.encodeBytes(c.doFinal(toEncrypt.getBytes("UTF-8")), true);
		
	}
	

	private static String doDecrypt(String toDecrypt, PublicKey publicKey) throws Exception {
		
		Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		c.init(Cipher.DECRYPT_MODE, publicKey);
		return new String(c.doFinal(Base64.decode(toDecrypt)), "UTF-8");
	}
	
	public static String encrypt(SshRsaPublicKey publicKey, String toEncrypt) throws Exception {
		int pos = 0;
		StringBuffer ret = new StringBuffer();
		int blockLength = publicKey.getModulus().bitLength() / 16;
		while(pos < toEncrypt.length()) {
			int count = Math.min(toEncrypt.length() - pos, blockLength);
			ret.append(doEncrypt(toEncrypt.substring(pos, pos+count), publicKey.getJCEPublicKey()));
			ret.append('|');
			pos += count;
		}
		return ret.toString();
	}
	
	public static String decrypt(SshRsaPrivateKey privateKey, String toDecrypt) throws Exception {
		StringBuffer ret = new StringBuffer();
		StringTokenizer t = new StringTokenizer(toDecrypt, "|");
		
		while(t.hasMoreTokens()) {
		
			String data = t.nextToken();
			ret.append(doDecrypt(data, privateKey.getJCEPrivateKey()));
		}

		return ret.toString();
	}

	private static String doEncrypt(String toEncrypt, PublicKey publicKey) throws Exception{

		Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		c.init(Cipher.ENCRYPT_MODE, publicKey);
		return Base64.encodeBytes(c.doFinal(toEncrypt.getBytes("UTF-8")), true);
		
	}
	

	private static String doDecrypt(String toDecrypt, PrivateKey privateKey) throws Exception {
		
		Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		c.init(Cipher.DECRYPT_MODE, privateKey);
		return new String(c.doFinal(Base64.decode(toDecrypt)), "UTF-8");
	}
	
	public static String encryptOAEP(SshRsaPublicKey publicKey, String toEncrypt) throws Exception {
		int pos = 0;
		StringBuffer ret = new StringBuffer();
		int blockLength = publicKey.getModulus().bitLength() / 16;
		while(pos < toEncrypt.length()) {
			int count = Math.min(toEncrypt.length() - pos, blockLength);
			ret.append(doOAEPSHA256Encrypt(toEncrypt.substring(pos, pos+count), publicKey.getJCEPublicKey()));
			ret.append('|');
			pos += count;
		}
		return ret.toString();
	}
	
	public static String decryptOAEP(SshRsaPrivateKey privateKey, String toDecrypt) throws Exception {
		StringBuffer ret = new StringBuffer();
		StringTokenizer t = new StringTokenizer(toDecrypt, "|");
		
		while(t.hasMoreTokens()) {
		
			String data = t.nextToken();
			ret.append(doOAEPSHA256Decrypt(data, privateKey.getJCEPrivateKey()));
		}

		return ret.toString();
	}
	
	
	private static String doOAEPSHA256Encrypt(String toEncrypt, PublicKey publicKey) throws Exception{

		Cipher c = Cipher.getInstance("RSA/None/OAEPWithSHA256AndMGF1Padding");
		c.init(Cipher.ENCRYPT_MODE, publicKey);
		return Base64.encodeBytes(c.doFinal(toEncrypt.getBytes("UTF-8")), true);
		
	}

	private static String doOAEPSHA256Decrypt(String toDecrypt, PrivateKey privateKey) throws Exception {
		
		Cipher c = Cipher.getInstance("RSA/None/OAEPWithSHA256AndMGF1Padding");
		c.init(Cipher.DECRYPT_MODE, privateKey);
		return new String(c.doFinal(Base64.decode(toDecrypt)), "UTF-8");
	}
}
