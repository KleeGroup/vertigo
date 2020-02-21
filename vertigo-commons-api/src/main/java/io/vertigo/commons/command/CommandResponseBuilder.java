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

import io.vertigo.lang.Builder;

public final class CommandResponseBuilder<P> implements Builder<CommandResponse<P>> {

	private CommandResponseStatus myStatus;
	private String myDisplay;
	private String myTargetUrl; // may be null
	private P myPayload; // may be null

	CommandResponseBuilder() {
		//package
	}

	public CommandResponseBuilder<P> withStatus(final CommandResponseStatus status) {
		myStatus = status;
		return this;
	}

	public CommandResponseBuilder<P> withDisplay(final String display) {
		myDisplay = display;
		return this;
	}

	public CommandResponseBuilder<P> withPayload(final P payload) {
		myPayload = payload;
		return this;
	}

	public CommandResponseBuilder<P> withTargetUrl(final String targetUrl) {
		myTargetUrl = targetUrl;
		return this;
	}

	@Override
	public CommandResponse<P> build() {
		return new CommandResponse<>(myStatus, myDisplay, myPayload, myTargetUrl);
	}

}
