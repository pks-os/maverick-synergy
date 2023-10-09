package com.sshtools.server.components.jce;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import com.sshtools.common.ssh.SecurityLevel;
import com.sshtools.common.ssh.components.DiffieHellmanGroups;
import com.sshtools.common.ssh.components.jce.JCEAlgorithms;
import com.sshtools.server.components.SshKeyExchangeServerFactory;

public class DiffieHellmanGroup16Sha512JCE extends DiffieHellmanGroup {

	public static final String DIFFIE_HELLMAN_GROUP16_SHA512 = "diffie-hellman-group16-sha512";

	public static class DiffieHellmanGroup16Sha512JCEFactory implements SshKeyExchangeServerFactory<DiffieHellmanGroup16Sha512JCE> {
		@Override
		public DiffieHellmanGroup16Sha512JCE create() throws NoSuchAlgorithmException, IOException {
			return new DiffieHellmanGroup16Sha512JCE();
		}

		@Override
		public String[] getKeys() {
			return new String[] { DIFFIE_HELLMAN_GROUP16_SHA512 };
		}
	}
	
	public DiffieHellmanGroup16Sha512JCE() {
		super(DIFFIE_HELLMAN_GROUP16_SHA512, JCEAlgorithms.JCE_SHA512, DiffieHellmanGroups.group16, SecurityLevel.PARANOID, 3016);
	}

}
