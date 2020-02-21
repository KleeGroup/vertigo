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
package io.vertigo.commons.impl.codec;

import java.io.Serializable;

import io.vertigo.commons.codec.Codec;
import io.vertigo.commons.codec.CodecManager;
import io.vertigo.commons.codec.Encoder;
import io.vertigo.commons.impl.codec.base64.Base64Codec;
import io.vertigo.commons.impl.codec.compressedserialization.CompressedSerializationCodec;
import io.vertigo.commons.impl.codec.compression.CompressionCodec;
import io.vertigo.commons.impl.codec.crypto.CryptoCodec;
import io.vertigo.commons.impl.codec.csv.CsvEncoder;
import io.vertigo.commons.impl.codec.hash.HashEncoder;
import io.vertigo.commons.impl.codec.hex.HexEncoder;
import io.vertigo.commons.impl.codec.html.HtmlCodec;
import io.vertigo.commons.impl.codec.serialization.SerializationCodec;

/**
 * Implémentation standard de CodecManager.
 *
 * @author pchretien
 */
public final class CodecManagerImpl implements CodecManager {
	/** Codage/décodage HTML de String > String. */
	private final Codec<String, String> htmlCodec;

	/** Crypto de byte[] > byte[]. */
	private final Codec<byte[], byte[]> tripleDESCodec;

	/** Crypto de byte[] > byte[]. */
	private final Codec<byte[], byte[]> aes128Codec;

	/** MD5 de byte[] > String. */
	private final Encoder<byte[], byte[]> md5Encoder;

	/** SHA-1 de byte[] > String. */
	private final Encoder<byte[], byte[]> sha1Encoder;

	/** SHA-256 de byte[] > String. */
	private final Encoder<byte[], byte[]> sha256Encoder;

	/** Sérialisation de byte[] > String. */
	private final Codec<byte[], String> base64Codec;

	/** Sérialisation de byte[] > String. */
	private final Encoder<byte[], String> hexEncoder;

	/** Compression de byte[] > byte[]. */
	private final Codec<byte[], byte[]> compressionCodec;

	/** Sérialisation de Serializable > byte[]. */
	private final Codec<Serializable, byte[]> serializationCodec;

	/** Sérialisation de Serializable > byte[]. */
	private final Codec<Serializable, byte[]> compressedSerializationCodec;

	/** CSV de String > String. */
	private final Encoder<String, String> csvEncoder;

	/**
	 * Constructor.
	 */
	public CodecManagerImpl() {
		super();
		htmlCodec = new HtmlCodec();
		//---
		final CryptoCodec tmpTripleDESCodec = new CryptoCodec(CryptoCodec.Crypto.TRIPLE_DES);
		tripleDESCodec = new NullCodec<>(tmpTripleDESCodec);
		//---
		final CryptoCodec tmpAes128Codec = new CryptoCodec(CryptoCodec.Crypto.AES);
		aes128Codec = new NullCodec<>(tmpAes128Codec);
		//---
		md5Encoder = new HashEncoder(HashEncoder.Hash.MD5);
		sha1Encoder = new HashEncoder(HashEncoder.Hash.SHA1);
		sha256Encoder = new HashEncoder(HashEncoder.Hash.SHA256);

		base64Codec = new NullCodec<>(new Base64Codec());
		hexEncoder = new HexEncoder();
		//---
		final CompressionCodec tmpCompressionCodec = new CompressionCodec();
		compressionCodec = new NullCodec<>(tmpCompressionCodec);

		//---
		serializationCodec = new NullCodec<>(new SerializationCodec());
		compressedSerializationCodec = new NullCodec<>(new CompressedSerializationCodec(serializationCodec, compressionCodec));
		csvEncoder = new CsvEncoder();
	}

	/** {@inheritDoc} */
	@Override
	public Codec<String, String> getHtmlCodec() {
		return htmlCodec;
	}

	/** {@inheritDoc} */
	@Override
	public Encoder<byte[], byte[]> getMD5Encoder() {
		return md5Encoder;
	}

	/** {@inheritDoc} */
	@Override
	public Encoder<byte[], byte[]> getSha256Encoder() {
		return sha256Encoder;
	}

	/** {@inheritDoc} */
	@Override
	public Encoder<byte[], byte[]> getSha1Encoder() {
		return sha1Encoder;
	}

	/** {@inheritDoc} */
	@Override
	public Codec<byte[], String> getBase64Codec() {
		return base64Codec;
	}

	/** {@inheritDoc} */
	@Override
	public Encoder<byte[], String> getHexEncoder() {
		return hexEncoder;
	}

	/** {@inheritDoc} */
	@Override
	public Codec<byte[], byte[]> getTripleDESCodec() {
		return tripleDESCodec;
	}

	/** {@inheritDoc} */
	@Override
	public Codec<byte[], byte[]> getAES128Codec() {
		return aes128Codec;
	}

	/** {@inheritDoc} */
	@Override
	public Codec<byte[], byte[]> getCompressionCodec() {
		return compressionCodec;
	}

	/** {@inheritDoc} */
	@Override
	public Codec<Serializable, byte[]> getCompressedSerializationCodec() {
		return compressedSerializationCodec;
	}

	/** {@inheritDoc} */
	@Override
	public Codec<Serializable, byte[]> getSerializationCodec() {
		return serializationCodec;
	}

	/** {@inheritDoc} */
	@Override
	public Encoder<String, String> getCsvEncoder() {
		return csvEncoder;
	}
}
