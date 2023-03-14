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

package com.sshtools.callback.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.sshtools.common.auth.InMemoryMutualKeyAuthenticationStore;
import com.sshtools.common.events.Event;
import com.sshtools.common.events.EventCodes;
import com.sshtools.common.events.EventListener;
import com.sshtools.common.events.EventServiceImplementation;
import com.sshtools.common.logger.Log;
import com.sshtools.common.policy.AuthenticationPolicy;
import com.sshtools.common.policy.FileFactory;
import com.sshtools.common.policy.FileSystemPolicy;
import com.sshtools.common.ssh.SshConnection;
import com.sshtools.common.ssh.SshException;
import com.sshtools.common.ssh.components.SshKeyPair;
import com.sshtools.common.ssh.components.jce.JCEComponentManager;
import com.sshtools.server.DefaultServerChannelFactory;
import com.sshtools.server.SshServerContext;
import com.sshtools.synergy.nio.DisconnectRequestFuture;
import com.sshtools.synergy.nio.SshEngine;
import com.sshtools.synergy.nio.SshEngineContext;
import com.sshtools.synergy.ssh.ChannelFactory;
import com.sshtools.synergy.ssh.ChannelFactoryListener;

public class CallbackClient implements ChannelFactoryListener<SshServerContext> {

	SshEngine ssh = new SshEngine();
	Set<CallbackSession> clients = new HashSet<CallbackSession>();
	ExecutorService executor;
	List<SshKeyPair> hostKeys = new ArrayList<>();
	ChannelFactory<SshServerContext> channelFactory;
	List<Object> defaultPolicies = new ArrayList<>();
	FileFactory fileFactory;
	
	public CallbackClient() {
		executor = getExecutorService();
		EventServiceImplementation.getInstance().addListener(new DisconnectionListener());
		channelFactory = new DefaultServerChannelFactory();
	}
	
	public SshEngine getSshEngine() {
		return ssh;
	}
	
	protected ExecutorService getExecutorService() {
		 return Executors.newCachedThreadPool();
	}

	public void setDefaultPolicies(Object... policies) {
		defaultPolicies.addAll(Arrays.asList(policies));
	}
	
	
	
	public void start(Collection<CallbackConfiguration> configs) {
		
		for(CallbackConfiguration config : configs) {
			
			try {
				start(config, config.getServerHost(), config.getServerPort());						
			} catch (Throwable e) {
				Log.error("Could not load configuration {}", e, config.getAgentName());
			}
		}
	}
	
	public synchronized void start(CallbackConfiguration config) throws IOException {
		start(config, config.getServerHost(), config.getServerPort());
	}
	
	public synchronized void start(CallbackConfiguration config, String hostname, int port) throws IOException {
		CallbackSession session = new CallbackSession(config, this, hostname, port);
		onClientStarting(session);
		start(session);
	}
	
	public synchronized void start(CallbackSession client) {
		
		if(Log.isInfoEnabled()) {
			Log.info("Starting client " + client.getConfig().getAgentName());
		}
		executor.execute(client);
	}
	
	void onClientConnected(CallbackSession client, SshConnection connection) {
		clients.add(client);
		onClientStart(client, connection);
	}
	
	public boolean isConnected() {
		return ssh.isStarted() && !clients.isEmpty();
	}
	
	public Collection<CallbackSession> getClients() {
		return clients;
	}
	
	protected void onClientStarting(CallbackSession client) {
		
	}
	
	protected void onClientStopping(CallbackSession client) {
		
	}
	
	protected void onClientStart(CallbackSession client, SshConnection connection) {
		
	}
	
	protected void onClientStop(CallbackSession client, SshConnection connection) {
		
	}
	
	public synchronized void stop(CallbackSession client) {
		
		onClientStopping(client);
		
		if(Log.isInfoEnabled()) {
			Log.info("Stopping callback client");
		}
		
		DisconnectRequestFuture future = client.stop();
		
		if(Log.isInfoEnabled()) {
			Log.info("Callback client has disconnected [{}]", String.valueOf(future.isDone()));
		}
	}
	
	public void stop() {	
		for(CallbackSession client : new ArrayList<CallbackSession>(clients)) {
			stop(client);
		}
	}
	
	public void shutdown() {
		
		for(CallbackSession client : new ArrayList<CallbackSession>(clients)) {
			stop(client);
		}
		
		ssh.shutdownAndExit();
		executor.shutdownNow();
	}
	
	class DisconnectionListener implements EventListener {

		@Override
		public void processEvent(Event evt) {
			
			switch(evt.getId()) {
			case EventCodes.EVENT_DISCONNECTED:
				
				final SshConnection con = (SshConnection)evt.getAttribute(EventCodes.ATTRIBUTE_CONNECTION);
				
				if(!executor.isShutdown()) {
					executor.execute(new Runnable() {
						public void run() {
							if(con.containsProperty("callbackClient")) {
								CallbackSession client = (CallbackSession) con.getProperty("callbackClient");
								onClientStop(client, con);
								con.removeProperty("callbackClient");
								clients.remove(client);
								if(!client.isStopped() && client.getConfig().isReconnect()) {
									while(getSshEngine().isStarted()) {
										try {
											try {
												Thread.sleep(client.getConfig().getReconnectIntervalMs());
											} catch (InterruptedException e1) {
											}
											client.connect();
											break;
										} catch (IOException e) {
										}
									}
								} else {
									stop();
								}
							} 
						}
					});
				}
				
				break;
			default:
				break;
			}
		}
		
	}

	public SshServerContext createContext(SshEngineContext daemonContext, CallbackConfiguration config) throws IOException, SshException {
		
		SshServerContext sshContext = new SshServerContext(getSshEngine(), JCEComponentManager.getDefaultInstance());
		
		sshContext.setIdleConnectionTimeoutSeconds(0);
		sshContext.setExtendedIdentificationSanitization(false);
		for(SshKeyPair key : hostKeys) {
			sshContext.addHostKey(key);
		}
				
		for(Object policy : defaultPolicies) {
			sshContext.setPolicy(policy.getClass(), policy);
		}
		
		sshContext.setSoftwareVersionComments(String.format("%s_%s", config.getCallbackIdentifier(), config.getAgentName()));
		
		InMemoryMutualKeyAuthenticationStore authenticationStore = new InMemoryMutualKeyAuthenticationStore();
		authenticationStore.addKey(config.getAgentName(), config.getPrivateKey(), config.getPublicKey());
		MutualCallbackAuthenticationProvider provider = new MutualCallbackAuthenticationProvider(authenticationStore);
		sshContext.setAuthenicationMechanismFactory(new CallbackAuthenticationMechanismFactory<>(provider));
		sshContext.getPolicy(AuthenticationPolicy.class).addRequiredMechanism(
				MutualCallbackAuthenticationProvider.MUTUAL_KEY_AUTHENTICATION);
		
		sshContext.setSendIgnorePacketOnIdle(true);
		
		configureForwarding(sshContext, config);
		configureChannels(sshContext, config);
		configureFilesystem(sshContext, config);
		
		configureContext(sshContext, config);
				
		return sshContext;
	}

	protected void configureContext(SshServerContext sshContext, CallbackConfiguration config) {
	}

	protected void configureFilesystem(SshServerContext sshContext, CallbackConfiguration config) {
		sshContext.getPolicy(FileSystemPolicy.class).setFileFactory(fileFactory);
	}

	protected void configureChannels(SshServerContext sshContext, CallbackConfiguration config) {
		sshContext.setChannelFactory(channelFactory);
	}

	protected void configureForwarding(SshServerContext sshContext, CallbackConfiguration config) {
		sshContext.getForwardingPolicy().allowForwarding();
	}

	public void addHostKey(SshKeyPair pair) {
		this.hostKeys.add(pair);
	}

	public void setChannelFactory(ChannelFactory<SshServerContext> channelFactory) {
		this.channelFactory = channelFactory;
	}

	public void setFileFactory(FileFactory fileFactory) {
		this.fileFactory = fileFactory;
	}
	
}
