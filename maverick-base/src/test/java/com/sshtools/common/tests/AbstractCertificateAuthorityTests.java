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

package com.sshtools.common.tests;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.junit.Ignore;

import com.sshtools.common.publickey.CertificateExtension;
import com.sshtools.common.publickey.CriticalOption;
import com.sshtools.common.publickey.InvalidPassphraseException;
import com.sshtools.common.publickey.OpenSshCertificate;
import com.sshtools.common.publickey.SshCertificateAuthority;
import com.sshtools.common.publickey.SshKeyUtils;
import com.sshtools.common.publickey.SshPrivateKeyFileFactory;
import com.sshtools.common.publickey.SshPublicKeyFileFactory;
import com.sshtools.common.ssh.SshException;
import com.sshtools.common.ssh.components.SshCertificate;
import com.sshtools.common.ssh.components.SshKeyPair;
import com.sshtools.common.ssh.components.SshPublicKey;
import com.sshtools.common.util.Utils;

@Ignore
public abstract class AbstractCertificateAuthorityTests extends AbstractPublicKeyTests {


	@Override
	protected String getTestingJCE() {
		return "";
	}
	
	protected boolean isJCETested() {
		return false;
	}

	public void testCertificateGenerations(String algorithm, int bits, String passphrase) throws IOException, SshException, InvalidPassphraseException {
		
		SshKeyPair ca = testKeyGeneration(algorithm, bits, passphrase, "CA", SshPrivateKeyFileFactory.OPENSSH_FORMAT);
	
		SshKeyPair userKey = testKeyGeneration(algorithm, bits, passphrase, "User Key", SshPrivateKeyFileFactory.OPENSSH_FORMAT);
	
		SshCertificate cert = SshCertificateAuthority.generateCertificate(userKey,
				1L, SshCertificate.SSH_CERT_TYPE_USER,
				"id", "john", 365, ca);
		
		assertTrue(ca.getPublicKey().equals(cert.getCertificate().getSignedBy()));
		assertTrue(userKey.getPublicKey().equals(cert.getCertificate().getSignedKey()));
		assertTrue(cert.getCertificate().getPrincipals().contains("john"));
		assertEquals(cert.getCertificate().getKeyId(), "id");
		assertEquals(cert.getCertificate().getSerial().longValue(), 1L);
		
		try {
			SshPublicKeyFileFactory.decodeSSH2PublicKey(cert.getCertificate().getEncoded());
		} catch (IOException | SshException e) {
			e.printStackTrace();
			fail("Re-encoded certificate could not be parsed");
		}
	}
	
	public void testCertificateGenerationsWithExtensions(String algorithm, int bits, String passphrase) throws IOException, SshException, InvalidPassphraseException, InterruptedException {
		
		
		Path path = Files.createTempDirectory("jad");
		
		SshKeyPair ca = testKeyGeneration(algorithm, bits, passphrase, "CA", SshPrivateKeyFileFactory.OPENSSH_FORMAT);
	
		SshKeyPair userKey = testKeyGeneration(algorithm, bits, passphrase, "User Key", SshPrivateKeyFileFactory.OPENSSH_FORMAT);
	
		SshCertificate cert = SshCertificateAuthority.generateCertificate(userKey,
				1L, SshCertificate.SSH_CERT_TYPE_USER,
				"id", Arrays.asList("john"), 365, new CriticalOption.Builder().forceCommand("ls").build(), 
				new CertificateExtension.Builder()
					.defaultExtensions()
					.customStringExtension("login@github.com", "ludup")
					.build(), ca);
		
		File certFile = new File(path.toFile(), "user");
		
		SshKeyUtils.saveCertificate(cert, "", "User certificate with extensions", certFile);
		
		String output = Utils.exec("ssh-keygen", "-l", "-f", certFile.getAbsolutePath());
		String fingerprint = bits + " " + SshKeyUtils.getFingerprint(cert.getPublicKey());
		
		assertTrue(output.startsWith(fingerprint));
		assertTrue(ca.getPublicKey().equals(cert.getCertificate().getSignedBy()));
		assertTrue(userKey.getPublicKey().equals(cert.getCertificate().getSignedKey()));
		assertTrue(cert.getCertificate().getPrincipals().contains("john"));
		assertEquals(cert.getCertificate().getKeyId(), "id");
		assertEquals(cert.getCertificate().getSerial().longValue(), 1L);
		
		for(CertificateExtension ext : cert.getCertificate().getExtensionsList()) {
			assertTrue(ext.isKnown() || ext.getName().equals("login@github.com"));
		}
		
		try {
			SshPublicKeyFileFactory.decodeSSH2PublicKey(cert.getCertificate().getEncoded());
		} catch (IOException | SshException e) {
			e.printStackTrace();
			fail("Re-encoded certificate could not be parsed");
		}
	}
	
	public void testHostSigningCAPrivateKey(String key, String passphrase, String fingerprint) throws IOException, InvalidPassphraseException, SshException {
		testPrivateKeyFile(getClass().getResourceAsStream(key), passphrase, fingerprint);
	}
	
	public void testHostSigningCAPublicKey(String key, String fingerprint) throws IOException, InvalidPassphraseException, SshException {
		testPublicKeyFile(getClass().getResourceAsStream(key), fingerprint);
	}
	
	public void testUnencryptedHostPrivateKey(String key, String fingerprint) throws IOException, InvalidPassphraseException, SshException {
		testPrivateKeyFile(getClass().getResourceAsStream(key), null, fingerprint);
	}
	
	public void testHostPublicKey(String key, String fingerprint) throws IOException, InvalidPassphraseException, SshException {
		testPublicKeyFile(getClass().getResourceAsStream(key), fingerprint);
	}
	
	public void testHostCertificate(String certpath, String fingerprint, String signedBy, String signedByFingerprint) throws IOException, InvalidPassphraseException, SshException {
		/**
		 * The certificate should have the same fingerprint as the original public key
		 */
		SshPublicKey key = testPublicKeyFile(getClass().getResourceAsStream(certpath), fingerprint);
		assertTrue(key instanceof OpenSshCertificate);
		
		
		OpenSshCertificate cert = (OpenSshCertificate) key;
		
		assertFalse(cert.isUserCertificate());
		assertTrue(cert.isHostCertificate());
		
		SshPublicKey ca = testPublicKeyFile(getClass().getResourceAsStream(signedBy), signedByFingerprint);
		assertTrue(cert.getSignedBy().equals(ca));
		
		try {
			SshPublicKeyFileFactory.decodeSSH2PublicKey(key.getEncoded());
		} catch (IOException | SshException e) {
			fail("Re-encoded certificate could not be parsed");
		}
	}
	
	public void testUserSigningCAPrivateKey(String key, String passphrase, String fingerprint) throws IOException, InvalidPassphraseException, SshException {
		testPrivateKeyFile(getClass().getResourceAsStream(key), passphrase, fingerprint);
	}
	
	public void testUserSigningCAPublicKey(String key, String fingerprint) throws IOException, InvalidPassphraseException, SshException {
		testPublicKeyFile(getClass().getResourceAsStream(key), fingerprint);
	}
	
	public void testUsersPrivateKey(String key, String passphrase, String fingerprint) throws IOException, InvalidPassphraseException, SshException {
		testPrivateKeyFile(getClass().getResourceAsStream(key), passphrase, fingerprint);
	}
	
	public void testUsersPublicKey(String key, String fingerprint) throws IOException, InvalidPassphraseException, SshException {
		testPublicKeyFile(getClass().getResourceAsStream(key), fingerprint);
	}
	
	public void testUserCertificate(String certpath, String fingerprint, String signedBy, String signedByFingerprint) throws IOException, InvalidPassphraseException, SshException {
		SshPublicKey key = testPublicKeyFile(getClass().getResourceAsStream(certpath), fingerprint);
		assertTrue(key instanceof OpenSshCertificate);
		
		OpenSshCertificate cert = (OpenSshCertificate) key;
		
		assertTrue(cert.isUserCertificate());
		assertFalse(cert.isHostCertificate());
		
		SshPublicKey ca = testPublicKeyFile(getClass().getResourceAsStream(signedBy), signedByFingerprint);
		assertTrue(cert.getSignedBy().equals(ca));

		try {
			SshPublicKeyFileFactory.decodeSSH2PublicKey(key.getEncoded());
		} catch (IOException | SshException e) {
			fail("Re-encoded certificate could not be parsed");
		}
	}
	
	
}
