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
package io.vertigo.commons.eventbus.data;

import io.vertigo.commons.eventbus.EventBusSubscribed;
import io.vertigo.commons.eventbus.data.aspects.Flip;
import io.vertigo.core.component.Component;

public class MySubscriber implements Component {
	private int count = 0;
	private int redCount = 0;
	private int blueCount = 0;

	@EventBusSubscribed
	public void onAllColor(final ColorEvent colorEvent) {
		count++;
	}

	@EventBusSubscribed
	public void onRedColor(final RedColorEvent colorEvent) {
		redCount++;
	}

	@EventBusSubscribed
	@Flip
	public void onBlueColor(final BlueColorEvent colorEvent) {
		blueCount++;
	}

	public int getCount() {
		return count;
	}

	public int getRedCount() {
		return redCount;
	}

	public int getBlueCount() {
		return blueCount;
	}
}
