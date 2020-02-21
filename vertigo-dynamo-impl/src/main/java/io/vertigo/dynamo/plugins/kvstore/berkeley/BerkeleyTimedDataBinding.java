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
package io.vertigo.dynamo.plugins.kvstore.berkeley;

import java.io.Serializable;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import io.vertigo.lang.Assertion;

/**
 * @author npiedeloup
 */
final class BerkeleyTimedDataBinding extends TupleBinding<Serializable> {
	private static final String PREFIX = "TimedValue:";
	private final TupleBinding<Serializable> serializableBinding;
	private final long timeToLiveSeconds;

	/**
	 * @param timeToLiveSeconds Time to live, is data too old return a null data
	 * @param serializableBinding TupleBinding for serializable value
	 */
	BerkeleyTimedDataBinding(final long timeToLiveSeconds, final TupleBinding<Serializable> serializableBinding) {
		Assertion.checkNotNull(serializableBinding);
		//-----
		this.serializableBinding = serializableBinding;
		this.timeToLiveSeconds = timeToLiveSeconds;
	}

	/** {@inheritDoc} */
	@Override
	public Serializable entryToObject(final TupleInput ti) {
		final String prefix = ti.readString();
		Assertion.checkArgument(PREFIX.equals(prefix), "Can't read this entry {0}", prefix);
		final long createTime = ti.readLong();
		if (isValueTooOld(createTime)) {
			//si donnée trop vieille on fait l'économie de la déserialization
			return null;
		}
		return serializableBinding.entryToObject(ti);
	}

	/** {@inheritDoc} */
	@Override
	public void objectToEntry(final Serializable value, final TupleOutput to) {
		to.writeString(PREFIX);
		to.writeLong(System.currentTimeMillis());
		serializableBinding.objectToEntry(value, to);
	}

	private boolean isValueTooOld(final long createTime) {
		return timeToLiveSeconds > 0 && (System.currentTimeMillis() - createTime) >= timeToLiveSeconds * 1000;
	}
}
