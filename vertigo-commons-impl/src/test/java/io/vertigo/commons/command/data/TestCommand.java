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
package io.vertigo.commons.command.data;

import io.vertigo.commons.command.Command;
import io.vertigo.commons.command.CommandResponse;
import io.vertigo.commons.command.CommandResponseStatus;
import io.vertigo.commons.command.GenericUID;
import io.vertigo.core.component.Component;

public class TestCommand implements Component {

	@Command(handle = "/t/repeat", description = "Repeat anything you say")
	public CommandResponse<String> reply(final String text) {
		return CommandResponse.<String> builder()
				.withStatus(CommandResponseStatus.OK)
				.withDisplay("You typed : " + text)
				.withPayload(text)
				.build();
	}

	@Command(handle = "/t/exists", description = "Test if my object uid exists")
	public CommandResponse<Boolean> reply(final GenericUID<Object> myUid) {
		return CommandResponse.<Boolean> builder()
				.withStatus(CommandResponseStatus.OK)
				.withDisplay(myUid.urn())
				.withPayload(false)
				.build();
	}

}
