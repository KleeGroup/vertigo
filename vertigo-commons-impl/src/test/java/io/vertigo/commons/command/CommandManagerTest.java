/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2019, Vertigo.io, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.commons.command;

import java.util.List;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.AbstractTestCaseJU5;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.app.config.NodeConfig;
import io.vertigo.commons.CommonsFeatures;
import io.vertigo.commons.command.data.TestCommand;

public class CommandManagerTest extends AbstractTestCaseJU5 {

	@Override
	protected NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.addModule(new CommonsFeatures()
						.withCommand()
						.build())
				.addModule(ModuleConfig.builder("commands-test")
						//---Services
						.addComponent(TestCommand.class)
						.build())
				.build();
	}

	@Inject
	private CommandManager commandManager;

	@Test
	public void testCommandDefinition() {
		final List<CommandDefinition> availableCommands = commandManager.searchCommands("/t/r");
		Assertions.assertFalse(availableCommands.isEmpty());
	}

	@Test
	public void testReplyCommandDefinition() {
		final CommandDefinition command = commandManager.findCommand("/t/repeat");
		Assertions.assertEquals("/t/repeat", command.getCommand());
	}

	@Test
	public void testReplyCommand() {
		final CommandResponse<String> commandResponse = commandManager.executeCommand("/t/repeat", "something");
		Assertions.assertEquals(CommandResponseStatus.OK, commandResponse.getStatus());
		Assertions.assertEquals("something", commandResponse.getPayload());
	}

	@Test
	public void testGenricUidCommandParam() {
		final CommandResponse<String> commandResponse = commandManager.executeCommand("/t/exists", GenericUID.of("myObject", 1000L).urn());
		Assertions.assertEquals(CommandResponseStatus.OK, commandResponse.getStatus());
		Assertions.assertEquals("myObject@l-1000", commandResponse.getDisplay());
		Assertions.assertEquals(false, commandResponse.getPayload());
	}

	@Test
	public void testGenricUidIntCommandParam() {
		final CommandResponse<String> commandResponse = commandManager.executeCommand("/t/exists", GenericUID.of("myObject", 1000).urn());
		Assertions.assertEquals(CommandResponseStatus.OK, commandResponse.getStatus());
		Assertions.assertEquals("myObject@i-1000", commandResponse.getDisplay());
		Assertions.assertEquals(false, commandResponse.getPayload());
	}

	@Test
	public void testGenricUidStrCommandParam() {
		final CommandResponse<String> commandResponse = commandManager.executeCommand("/t/exists", GenericUID.of("myObject", "first").urn());
		Assertions.assertEquals(CommandResponseStatus.OK, commandResponse.getStatus());
		Assertions.assertEquals("myObject@s-first", commandResponse.getDisplay());
		Assertions.assertEquals(false, commandResponse.getPayload());
	}

}
