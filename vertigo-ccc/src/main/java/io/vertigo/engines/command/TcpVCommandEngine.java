/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.engines.command;

import io.vertigo.engines.command.samples.VDescribableCommandExecutor;
import io.vertigo.engines.command.samples.VPingCommandExecutor;
import io.vertigo.engines.command.samples.VSystemCommandExecutor;
import io.vertigo.engines.command.tcp.VServer;
import io.vertigo.kernel.Home;
import io.vertigo.kernel.command.VCommand;
import io.vertigo.kernel.command.VCommandExecutor;
import io.vertigo.kernel.command.VResponse;
import io.vertigo.kernel.component.Describable;
import io.vertigo.kernel.engines.VCommandEngine;
import io.vertigo.kernel.lang.Activeable;
import io.vertigo.kernel.lang.Assertion;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author pchretien
 */
public final class TcpVCommandEngine implements VCommandEngine, Activeable {
	//int DEFAULT_PORT = 4444;

	private final int port;
	private final Map<String, VCommandExecutor> commmandExecutors = new LinkedHashMap<>();

	@Inject
	public TcpVCommandEngine(@Named("port") int port) {
		this.port = port;
	}

	private Thread tcpServerThread;

	public void registerCommandExecutor(String name, VCommandExecutor commandExecutor) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(commandExecutor);
		Assertion.checkArgument(!commmandExecutors.containsKey(name), "command '{0}' is already registered'", name);
		//---------------------------------------------------------------------
		commmandExecutors.put(name, commandExecutor);
	}

	public void start() {
		//Chargement des commandes
		registerCommandExecutor("ping", new VPingCommandExecutor());
		registerCommandExecutor("system", new VSystemCommandExecutor());

		for (String componentId : Home.getComponentSpace().keySet()) {
			Object component = Home.getComponentSpace().resolve(componentId, Object.class);
			VDescribableCommandExecutor describableCommandExecutor = new VDescribableCommandExecutor();
			if (component instanceof Describable) {
				registerCommandExecutor(componentId, describableCommandExecutor);
			}
		}
		//---
		registerCommandExecutor("help", new VCommandExecutor<Set<String>>() {
			//All commands are listed
			/** {@inheritDoc} */
			public Set<String> exec(VCommand command) {
				Assertion.checkNotNull(command);
				//Assertion.checkArgument(command.getName());
				//---------------------------------------------------------------------
				return commmandExecutors.keySet();
			}
		});

		final VServer tcpServer = new VServer(this, port);
		tcpServerThread = new Thread(tcpServer);
		tcpServerThread.start();

		//	new TcpBroadcaster().hello(port);
	}

	public void stop() {
		tcpServerThread.interrupt();
		try {
			tcpServerThread.join();
		} catch (InterruptedException e) {
			//
		}
	}

	private Object exec(VCommand command) {
		Assertion.checkNotNull(command);
		Assertion.checkArgument(commmandExecutors.containsKey(command.getName()), "command '{0}' unknown", command.getName());
		//---------------------------------------------------------------------
		VCommandExecutor<?> commandExecutor = commmandExecutors.get(command.getName());
		return commandExecutor.exec(command);
	}

	static final class VError {
		private final String error;

		VError(String error) {
			this.error = error;
		}

		public String getError() {
			return error;
		}
	}

	public VResponse execCommand(VCommand command) {
		return VResponse.createResponse(JsonUtil.toJson(exec(command)));
	}

}