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

import java.io.IOException;

import com.sshtools.common.ssh.SshException;
import com.sshtools.common.ssh.components.ComponentFactory;
import com.sshtools.common.ssh.components.ComponentManager;
import com.sshtools.common.ssh.components.SshKeyPair;
import com.sshtools.common.ssh.components.jce.JCEComponentManager;

/**
 * <p>
 * Generate public/private key pairs.
 * </p>
 * <p>
 * To generate a new pair use the following code <blockquote>
 * 
 * <pre>
 * SshKeyPair pair = SshKeyPairGenerator.generateKeyPair(SshKeyPairGenerator.SSH2_RSA, 1024);
 * </pre>
 * 
 * </blockquote> To create formatted key file for the public key use:
 * <blockquote>
 * 
 * <pre>
 * SshPublicKeyFile pubfile = SshPublicKeyFileFactory.create(pair.getPublicKey(), "Some comment",
 * 		SshPublicKeyFileFactory.OPENSSH_FORMAT);
 * FileOutputStream fout = new FileOutputStream("mykey.pub");
 * fout.write(pubfile.getFormattedKey());
 * fout.close();
 * </pre>
 * 
 * <blockquote> To create a formatted, encrypted private key file use:
 * <blockquote>
 * 
 * <pre>
 * SshPrivateKeyFile prvfile = SshPrivateKeyFileFactory.create(pair, "my passphrase", "Some comment",
 * 		SshPrivateKeyFileFactory.OPENSSH_FORMAT);
 * FileOutputStream fout = new FileOutputStream("mykey");
 * fout.write(prvfile.getFormattedKey());
 * fout.close();
 * </pre>
 * 
 * <blockquote>
 * </p>
 * 
 * @author Lee David Painter
 */
public class SshKeyPairGenerator {

	public static final String SSH2_RSA = "ssh-rsa";
	public static final String ECDSA = "ecdsa";
	public static final String ED25519 = "ed25519";

	/**
	 * Generate a new key pair using the default bit size.
	 * 
	 * @param algorithm
	 * @return
	 * @throws IOException
	 * @throws SshException
	 */
	public static SshKeyPair generateKeyPair(String algorithm) throws IOException, SshException {
		
		switch(algorithm) {
		case ECDSA:
			return generateKeyPair(algorithm, 256);
		case ED25519:
			return generateKeyPair(algorithm, 0);
		case SSH2_RSA:
		case "rsa":
		case "RSA":
			return generateKeyPair(algorithm, 2048)	;
		default:
			throw new IOException(String.format("Unexpected key algorithm %s", algorithm));
	}
	}
	/**
	 * Generates a new key pair.
	 * 
	 * @param algorithm
	 * @param bits
	 * @return SshKeyPair
	 * @throws IOException
	 */
	public static SshKeyPair generateKeyPair(String algorithm, int bits) throws IOException, SshException {

		
		switch(algorithm) {
		case ED25519:
		case "ssh-ed25519":
			return ComponentManager.getDefaultInstance().generateEd25519KeyPair();
		case ECDSA:
			return ComponentManager.getDefaultInstance().generateEcdsaKeyPair(bits);
		case SSH2_RSA:
		case "rsa":
		case "RSA":
			return ComponentManager.getDefaultInstance().generateRsaKeyPair(bits, 2);
		default:
			ComponentFactory<KeyGenerator> generators = new ComponentFactory<>(JCEComponentManager.getDefaultInstance());
			JCEComponentManager.getDefaultInstance().loadExternalComponents("generator.properties",generators);
			
			KeyGenerator gen = generators.getInstance(algorithm);
			return gen.generateKey(bits);
		}
	}

}
