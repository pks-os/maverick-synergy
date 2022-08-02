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

package com.sshtools.server.callback.commands;

import java.io.IOException;
import java.util.Objects;

import com.sshtools.client.SessionChannelNG;
import com.sshtools.client.shell.ShellTimeoutException;
import com.sshtools.client.tasks.ShellTask;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.common.ssh.ConnectionAwareTask;
import com.sshtools.common.ssh.SshException;
import com.sshtools.common.util.IOUtils;
import com.sshtools.server.callback.Callback;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.VirtualConsole;
import com.sshtools.server.vsession.VirtualShellNG;
import com.sshtools.server.vsession.VirtualShellNG.WindowSizeChangeListener;

public class CallbackShell extends CallbackCommand {

	public CallbackShell() {
		super("shell", "Callback", "shell <uuid>", "Open a shell to the callback client identified by uuid");
	}
	
	@Override
	public void run(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {
		
		if(args.length != 2) {
			throw new UsageException("Invalid number of arguments");
		}
		
		String clientName = args[1];
		Callback con = service.getCallbackByUUID(clientName);
		
		if(Objects.isNull(con)) {
			console.println(String.format("%s is not currently connected", clientName));
			return;
		}

		console.println(String.format("---- Opening shell on %s", clientName));
		console.println();
		
		ShellTask shell = new ShellTask(con.getConnection()) {

			private WindowSizeChangeListener listener;

			protected void beforeStartShell(SessionChannelNG session) {
				
				session.allocatePseudoTerminal(console.getTerminal().getType(), 
						console.getTerminal().getWidth(), 
						console.getTerminal().getHeight());
			}
			
			@Override
			protected void onOpenSession(SessionChannelNG session)
					throws IOException, SshException, ShellTimeoutException {
				
				console.getSessionChannel().enableRawMode();
				
				listener = new WindowSizeChangeListener() {
					@Override
					public void newSize(int rows, int cols) {
						session.changeTerminalDimensions(cols, rows, 0, 0);
					}
				};
				((VirtualShellNG)console.getSessionChannel()).addWindowSizeChangeListener(listener);
				
				con.addTask(new ConnectionAwareTask(con) {
					@Override
					protected void doTask() throws Throwable {
						IOUtils.copy(console.getSessionChannel().getInputStream(), session.getOutputStream());
					}
				});
				IOUtils.copy(session.getInputStream(), console.getSessionChannel().getOutputStream());
			}

			@Override
			protected void onCloseSession(SessionChannelNG session) {
				((VirtualShellNG)console.getSessionChannel()).removeWindowSizeChangeListener(listener);
			}
			
		};
		
		con.addTask(shell);
		shell.waitForever();
		
		console.getSessionChannel().disableRawMode();
		console.println();
		console.println(String.format("---- Exited shell on %s", clientName));
	}

}
