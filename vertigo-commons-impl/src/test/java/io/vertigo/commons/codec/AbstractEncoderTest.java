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
package io.vertigo.commons.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.inject.Inject;

import io.vertigo.AbstractTestCaseJU5;
import io.vertigo.app.config.NodeConfig;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.commons.impl.codec.CodecManagerImpl;

/**
 * @author dchallas
 * @param <S> Type Source à encoder
 * @param <T> Type cible, résultat de l'encodage
 */
public abstract class AbstractEncoderTest<C extends Encoder<S, T>, S, T> extends AbstractTestCaseJU5 {
	protected static final String TEXT = "Les sanglots longs des violons de l'automne blessent mon coeur d'une langueur monotone.";
	protected C codec;

	@Inject
	private CodecManager codecManager;

	@Override
	protected NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.addModule(ModuleConfig.builder("commons")
						.addComponent(CodecManager.class, CodecManagerImpl.class)
						.build())
				.build();
	}

	protected abstract C obtainCodec(CodecManager inCodecManager);

	/** {@inheritDoc} */
	@Override
	public final void doSetUp() {
		codec = obtainCodec(codecManager);
	}

	/**
	 * test l'encodage et le décodage avec les chaines null.
	 */
	public abstract void testNull();

	/**
	 * test l'encodage de chaines non null.
	 */
	public abstract void testEncode();

	protected final void checkEncode(final S value, final T expectedEncodedValue) {
		final T encodedValue = codec.encode(value);
		assertEquals(expectedEncodedValue, encodedValue);
		checkEncodedValue(encodedValue);
	}

	protected void checkEncodedValue(final T encodedValue) {
		// à implementer si besoin
	}

	protected final CodecManager getCodecManager() {
		return codecManager;
	}

}
