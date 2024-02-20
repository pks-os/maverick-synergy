package com.sshtools.server.vsession;

/*-
 * #%L
 * Virtual Sessions
 * %%
 * Copyright (C) 2002 - 2024 JADAPTIVE Limited
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.common.ssh.SshConnection;
import com.sshtools.common.ssh.UnsupportedChannelException;
import com.sshtools.server.DefaultServerChannelFactory;
import com.sshtools.server.SshServerContext;
import com.sshtools.synergy.ssh.ChannelNG;

public class VirtualChannelFactory extends DefaultServerChannelFactory {

	CommandFactory<? extends ShellCommand>[] factories;
	String shellCommand;
	@SafeVarargs
	public VirtualChannelFactory(CommandFactory<? extends ShellCommand>... factories) {
		this.factories = factories;
	}

	@SafeVarargs
	public VirtualChannelFactory(String shellCommand, CommandFactory<? extends ShellCommand>... factories) {
		this.factories = factories;
		this.shellCommand = shellCommand;
	}
	
	@Override
	protected ChannelNG<SshServerContext> createSessionChannel(SshConnection con)
			throws UnsupportedChannelException, PermissionDeniedException {
		return new VirtualShellNG(con,  new ShellCommandFactory(factories), shellCommand);
	}
	
	protected CommandFactory<? extends ShellCommand>[] getCommandFactories() {
		return factories;
	}
}
