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

package com.sshtools.server.vsession.commands.sftp;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;

import com.sshtools.common.logger.Log;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.server.vsession.VirtualConsole;
import com.sshtools.vsession.commands.ssh.AbstractSshOptionsEvaluator;
import com.sshtools.vsession.commands.ssh.SshClientArguments;

public class SftpClientOptionsEvaluator extends AbstractSshOptionsEvaluator{
	
	public static SshClientArguments evaluate(CommandLine commandLine, VirtualConsole console) throws IOException, PermissionDeniedException {

		if (Log.isDebugEnabled()) {
			Log.debug("The argument list passed as {}", commandLine.getArgList());

			List<String> optionList = Arrays.asList(commandLine.getOptions()).stream()
					.map(option -> String.format("{} -> {}", option.getArgName(), option.getValue()))
					.collect(Collectors.toList());

			Log.debug("The option list passed as {}", optionList);
		}

		SshClientArguments arguments = new SshClientArguments();

		parsePort(commandLine, arguments);
		parseLoginName(commandLine, arguments);
		parseIdentityFilename(commandLine, arguments, console);
		parseCiphers(commandLine, arguments);
		parseMacs(commandLine, arguments);
		parseSecurityLevel(commandLine, arguments);
		parseCompression(commandLine, arguments);
		
		parseDestination(commandLine, arguments);
		
		return arguments;
	}

}
