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
package io.vertigo.commons.impl.command;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import io.vertigo.app.Home;
import io.vertigo.commons.command.Command;
import io.vertigo.commons.command.CommandDefinition;
import io.vertigo.commons.command.CommandManager;
import io.vertigo.commons.command.CommandParam;
import io.vertigo.commons.command.CommandResponse;
import io.vertigo.commons.command.GenericUID;
import io.vertigo.core.component.AopPlugin;
import io.vertigo.core.component.Component;
import io.vertigo.core.definition.DefinitionSpace;
import io.vertigo.core.definition.SimpleDefinitionProvider;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.VSystemException;
import io.vertigo.util.ClassUtil;

public final class CommandManagerImpl implements CommandManager, SimpleDefinitionProvider {

	@Override
	public List<CommandDefinition> provideDefinitions(final DefinitionSpace definitionSpace) {
		// we need to unwrap the component to scan the real class and not the enhanced version
		final AopPlugin aopPlugin = Home.getApp().getNodeConfig().getBootConfig().getAopPlugin();
		return Home.getApp().getComponentSpace().keySet()
				.stream()
				.flatMap(id -> createCommandDefinition(Home.getApp().getComponentSpace().resolve(id, Component.class), aopPlugin).stream())
				.collect(Collectors.toList());
	}

	private static List<CommandDefinition> createCommandDefinition(final Component component, final AopPlugin aopPlugin) {
		return Stream.of(aopPlugin.unwrap(component).getClass().getMethods())
				.filter(method -> method.isAnnotationPresent(Command.class))
				.map(
						method -> {
							Assertion.checkState(
									CommandResponse.class.isAssignableFrom(method.getReturnType()),
									"Method {0} on component {1} must return a CommandResult to be used as a command", method.getName(), component.getClass().getName());
							//---
							final Command command = method.getAnnotation(Command.class);
							final List<CommandParam> commandParams = Stream.of(method.getGenericParameterTypes())
									.map(CommandParam::new)
									.collect(Collectors.toList());
							return new CommandDefinition(
									command.handle(),
									command.description(),
									Arrays.asList(command.questions()),
									commandParams,
									args -> (CommandResponse) ClassUtil.invoke(component, method, args));
						})
				.collect(Collectors.toList());

	}

	@Override
	public List<CommandDefinition> searchCommands(final String prefix) {
		return Home.getApp().getDefinitionSpace().getAll(CommandDefinition.class)
				.stream()
				.filter(commandDefinition -> commandDefinition.getCommand().startsWith(prefix))
				.collect(Collectors.toList());
	}

	@Override
	public CommandDefinition findCommand(final String handle) {
		return Home.getApp().getDefinitionSpace().getAll(CommandDefinition.class)
				.stream()
				.filter(commandDefinition -> commandDefinition.getCommand().equals(handle))
				.findAny()
				.orElseThrow(() -> new VSystemException("no command availale with handle '{0}' ", handle));
	}

	@Override
	public CommandResponse executeCommand(final String handle, final String... commandParams) {
		final CommandDefinition commandDefinition = findCommand(handle);
		Assertion.checkState(
				commandParams.length == commandDefinition.getParams().size(),
				"Command '{0}' takes {1} arguments and {2} were passed", commandDefinition.getCommand(), commandDefinition.getParams().size(), commandParams.length);
		final Object[] actualArguments = IntStream.range(0, commandParams.length)
				.mapToObj(i -> {
					if (commandDefinition.getParams().get(i).getType() instanceof ParameterizedType) {
						return GenericUID.of(commandParams[i]);
					}
					return commandParams[i];
				}).toArray(Object[]::new);
		return commandDefinition.getAction().apply(actualArguments);
	}

}
