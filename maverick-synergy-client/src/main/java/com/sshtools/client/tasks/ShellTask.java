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

package com.sshtools.client.tasks;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;

import com.sshtools.client.SessionChannelNG;
import com.sshtools.client.SshClient;
import com.sshtools.client.shell.ShellTimeoutException;
import com.sshtools.common.shell.ShellPolicy;
import com.sshtools.common.ssh.SshConnection;
import com.sshtools.common.ssh.SshException;

/**
 * A {@link Task} that starts a remote shell with an allocated PTY.
 * You cannot directly create a {@link ShellTask}, instead use {@link ShellTaskBuilder}.
 * <pre>
 * client.addTask(ShellTaskBuilder.create().
 *      withTermType("vt320").
 *      withColumns(132).
 *      withRows(48).
 *      onClose((task, session) -> System.out.println("Closed!")).
 *      build());
 * </pre>
 *
 */
public class ShellTask extends AbstractShellTask<SessionChannelNG> {
	
	/**
	 * Functional interface for tasks run on certain shell events.
	 */
	@FunctionalInterface
	public interface ShellTaskEvent {
		/**
		 * Shell event occurred. Checked exceptions are caught and rethrown as an
		 * {@link IllegalStateException}.
		 * 
		 * @param task task
		 * @param session session
		 * @throws Exception on any error
		 */
		void shellEvent(ShellTask task, SessionChannelNG session) throws Exception;
	}

	/**
	 * Builder for {@link ShellTask}.
	 */
	public final static class ShellTaskBuilder extends AbstractConnectionTaskBuilder<ShellTaskBuilder, ShellTask> {

		private Optional<ShellTaskEvent> onClose = Optional.empty();
		private Optional<ShellTaskEvent> onBeforeOpen = Optional.empty();
		private Optional<ShellTaskEvent> onOpen = Optional.empty();
		private Optional<String> termType = Optional.empty();
		private Optional<Function<SshConnection, SessionChannelNG>> session = Optional.empty();
		private int cols = 80;
		private int rows = 24;
		private boolean withPty = false;

		private ShellTaskBuilder() {
		}

		/**
		 * Create a new {@link ShellTaskBuilder}.
		 * 
		 * @return builder
		 */
		public static ShellTaskBuilder create() {
			return new ShellTaskBuilder();
		}

		/**
		 * Set a function to create a custom session channel.
		 * 
		 * @param session session function
		 * @return builder for chaining
		 */
		public final ShellTaskBuilder withSession(Function<SshConnection, SessionChannelNG> session) {
			this.session = Optional.of(session);
			return this;
		}

		/**
		 * Set the terminal type to use when allocating a PTY. Note, this
		 * will have no effect if a custom {@link #onBeforeOpen(ShellTaskEvent)} is set.
		 * 
		 * @param term type
		 * @return builder for chaining
		 */
		public final ShellTaskBuilder withTermType(String termType) {
			return withTermType(Optional.of(termType));
		}

		/**
		 * Set the terminal type to use when allocating a PTY. Note, this
		 * will have no effect if a custom {@link #onBeforeOpen(ShellTaskEvent)} is set.
		 * 
		 * @param term type
		 * @return builder for chaining
		 */
		public final ShellTaskBuilder withTermType(Optional<String> termType) {
			this.termType = termType;
			return this;
		}

		/**
		 * Set the terminal width in columns to use when allocating a PTY. Note, this
		 * will have no effect if a custom {@link #onBeforeOpen(ShellTaskEvent)} is set.
		 * 
		 * @param cols cols
		 * @return builder for chaining
		 */
		public final ShellTaskBuilder withColumns(int cols) {
			this.cols = cols;
			return this;
		}

		/**
		 * Set the terminal height in rows to use when allocating a PTY. Note, this
		 * will have no effect if a custom {@link #onBeforeOpen(ShellTaskEvent)} is set.
		 * 
		 * @param rows row
		 * @return builder for chaining
		 */
		public final ShellTaskBuilder withRows(int rows) {
			this.rows = rows;
			return this;
		}

		/**
		 * Set a callback to run on start of the shell. By default, this will allocate a new
		 * PTY using the other configuratio in this builder, such as terminal type, columns etc.
		 * If you set a new callback, you will have to do this allocation yourself.
		 * 
		 * @param onStartShell on start shell callback
		 * @return builder for chaining
		 */
		public final ShellTaskBuilder onBeforeOpen(ShellTaskEvent onStartShell) {
			this.onBeforeOpen = Optional.of(onStartShell);
			return this;
		}

		/**
		 * Set a callback to run when the shell is closed. 
		 * 
		 * @param onBeforeOpen on start shell callback
		 * @return builder for chaining
		 */
		public final ShellTaskBuilder onClose(ShellTaskEvent onClose) {
			this.onClose = Optional.of(onClose);
			return this;
		}

		/**
		 * Set a callback to run when the shell channel is opened. 
		 * 
		 * @param onOpen on start shell open
		 * @return builder for chaining
		 */
		public final ShellTaskBuilder onOpen(ShellTaskEvent onOpen) {
			this.onOpen = Optional.of(onOpen);
			return this;
		}

		@Override
		public ShellTask build() {
			return new ShellTask(this);
		}

		public ShellTaskBuilder withPty(boolean withPty) {
			this.withPty = withPty;
			return this;
		}
	}

	private final Optional<ShellTaskEvent> onClose;
	private final Optional<ShellTaskEvent> onStartShell;
	private final Optional<ShellTaskEvent> onOpen;
	private final String termType;
	private final int rows;
	private final int cols;
	private final Optional<Function<SshConnection, SessionChannelNG>> session;
	private final boolean withPty;
	
	private ShellTask(ShellTaskBuilder builder) {
		super(builder);
		this.onClose = builder.onClose;
		this.onStartShell = builder.onBeforeOpen;
		this.onOpen = builder.onOpen;
		this.withPty = builder.withPty;
		this.termType = builder.termType.orElse("dumb");
		this.rows = builder.rows;
		this.cols = builder.cols;
		this.session = builder.session;
	}

	/**
	 * Construct a shell task. Deprecated since 3.1.0. Use a {@link ShellTaskBuilder} instead. 
	 * 
	 * @param con connection
	 * @deprecated 
	 * @see ShellTaskBuilder
	 */
	@Deprecated
	public ShellTask(SshConnection con) {
		this(ShellTaskBuilder.create().withConnection(con));
	}

	/**
	 * Construct a shell task. Deprecated since 3.1.0. Use a {@link ShellTaskBuilder} instead. 
	 * 
	 * @param ssh client
	 * @deprecated 
	 * @see ShellTaskBuilder
	 */
	@Deprecated
	public ShellTask(SshClient ssh) {
		this(ShellTaskBuilder.create().withClient(ssh));
	}

	/**
	 * Deprecated for overriding, will be made final at 3.2.0.
	 */
	@Override
	protected void onOpenSession(SessionChannelNG session) throws IOException, SshException, ShellTimeoutException {
		if(onOpen.isPresent()) {
			try {
				onOpen.get().shellEvent(this, session);
			} catch(RuntimeException re) {
				throw re;
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		}
	}

	/**
	 * Deprecated for overriding, will be made final at 3.2.0.
	 */
	@Override
	@Deprecated(since = "3.1.0")
	protected void beforeStartShell(SessionChannelNG session) {
		try {
			if(withPty) {
				session.allocatePseudoTerminal(termType, cols, rows);
			}
			
			if(onStartShell.isPresent()) {
				onStartShell.get().shellEvent(this, session);
			}
			
		} catch(RuntimeException re) {
			throw re;
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Deprecated for overriding, will be made final at 3.2.0.
	 */
	@Override
	@Deprecated(since = "3.1.0")
	protected void onCloseSession(SessionChannelNG session) {
		if(onClose.isPresent()) {
			try {
				onClose.get().shellEvent(this, session);
			} catch(RuntimeException re) {
				throw re;
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		}
	}

	/**
	 * Deprecated for overriding, will be made final at 3.2.0.
	 */
	@Override
	@Deprecated(since = "3.1.0")
	protected SessionChannelNG createSession(SshConnection con) {
		return session.orElse((c) -> new SessionChannelNG(
				c.getContext().getPolicy(ShellPolicy.class).getSessionMaxPacketSize(),
				c.getContext().getPolicy(ShellPolicy.class).getSessionMaxWindowSize(),
				c.getContext().getPolicy(ShellPolicy.class).getSessionMaxWindowSize(),
				c.getContext().getPolicy(ShellPolicy.class).getSessionMinWindowSize(),
				getChannelFuture(),
				false)).apply(con);
	}
}
