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
package io.vertigo.dynamo.domain.model;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import io.vertigo.core.definition.DefinitionReference;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.lang.Assertion;

/**
 * Classe de stockage des listes.
 * Une dtList est une liste constituée avec un seul type d'objet.
 * Les objets null ne sont pas autorisés.
 *
 * @author  fconstantin
 * @param <D> Type du DtObject
 */
public final class DtList<D extends DtObject> extends AbstractList<D> implements Serializable {
	private static final int TO_STRING_MAX_ELEMENTS = 50;
	private static final long serialVersionUID = -8059200549636099190L;
	/**
	 * total count
	 */
	public static final String TOTAL_COUNT_META = "totalCount";

	private final DtListURI uri;

	/** Reéférence vers la Définition. */
	private final DefinitionReference<DtDefinition> dtDefinitionRef;

	/** List des dto contenus. */
	private final List<D> dtObjects;

	/** List des dto contenus. */
	private final Map<String, Serializable> metaDatas;

	/**
	 * Constructor.
	 * @param dtDefinition Définition de DT
	 */
	public DtList(final DtDefinition dtDefinition) {
		this.dtDefinitionRef = new DefinitionReference<>(dtDefinition);
		this.uri = null; //new DtListURIForValueObject(dtDefinition);
		this.dtObjects = new ArrayList<>(); //
		this.metaDatas = new LinkedHashMap<>();

	}

	/**
	 * Constructor.
	 * @param dtList DtList to clone
	 * @param uri DtList uri
	 */
	public DtList(final DtList<D> dtList, final DtListURI uri) {
		Assertion.checkNotNull(uri);
		//---
		this.dtDefinitionRef = dtList.dtDefinitionRef; //The same DtDefinition
		this.uri = uri;
		this.dtObjects = new ArrayList<>(dtList.dtObjects); //Clone
		this.metaDatas = new LinkedHashMap<>(dtList.metaDatas); //clone
	}

	/**
	 * Constructor.
	 * @param dtObjectClass Type d'objet
	 */
	public DtList(final Class<? extends DtObject> dtObjectClass) {
		this(DtObjectUtil.findDtDefinition(dtObjectClass));
	}

	/**
	 * Static method factory for convenient creation of DtList using 'of' pattern.
	 * @param dto the mandatory dto to add  which defines the type.
	 * @param dtos Dtos to add.
	 * @return the created DtList.
	 * @param <D> Type of this list
	 */
	@SafeVarargs
	public static <D extends DtObject> DtList<D> of(final D dto, final D... dtos) {
		Assertion.checkNotNull(dto);
		Assertion.checkNotNull(dtos);
		Arrays.stream(dtos)
				.forEach(other -> Assertion.checkArgument(dto.getClass().equals(other.getClass()), "all dtos must have the same type"));
		//---
		final DtList<D> dtList = new DtList<>(DtObjectUtil.findDtDefinition(dto));
		//---
		dtList.add(dto);
		dtList.addAll(Arrays.asList(dtos));
		return dtList;
	}

	/** {@inheritDoc} */
	@Override
	public D get(final int row) {
		return dtObjects.get(row);
	}

	/** {@inheritDoc} */
	@Override
	public D set(final int row, final D object) {
		//Implementation de set, pour que la collection soit modifiable
		//Et donc pour que le Collections.sort(List<?> ) fonctionne
		return dtObjects.set(row, object);
	}

	/** {@inheritDoc} */
	@Override
	public int size() {
		return dtObjects.size();
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		final StringBuilder buf = new StringBuilder()
				.append("(def=").append(getDefinition()).append(", size=").append(dtObjects.size());
		if (dtObjects.size() > TO_STRING_MAX_ELEMENTS) {
			buf.append(" show only the ").append(TO_STRING_MAX_ELEMENTS).append(" firsts");
		}
		buf.append(")\n");
		for (int i = 0; i < Math.min(dtObjects.size(), TO_STRING_MAX_ELEMENTS); i++) { //pas plus de TO_STRING_MAX_ELEMENTS elements dans le toString
			buf.append("\tRow #").append(i).append(" : ")
					.append(get(i)).append('\n');
		}

		return buf.toString();
	}

	//==========================================================================

	/** {@inheritDoc} */
	@Override
	public boolean add(final D dto) {
		Assertion.checkNotNull(dto);
		final DtDefinition foundDtDefinition = DtObjectUtil.findDtDefinition(dto);
		Assertion.checkArgument(getDefinition().equals(foundDtDefinition), "Ne peut pas inserer un dto '{0}' dans une collection '{1}'", foundDtDefinition, getDefinition());
		//-----
		return dtObjects.add(dto);
	}

	/** {@inheritDoc} */
	@Override
	public D remove(final int row) {
		return dtObjects.remove(row);
	}

	/** {@inheritDoc} */
	@Override
	public List<D> subList(final int start, final int end) {
		throw new UnsupportedOperationException();
	}

	//==========================================================================
	//================================ Méthodes supplémentaires=================
	//==========================================================================

	/**
	 * @return Définition de la liste.
	 */
	public DtDefinition getDefinition() {
		return dtDefinitionRef.get();
	}

	/**
	 * @return URI de la ressource
	 */
	public DtListURI getURI() {
		return uri;
	}

	//==========================================================================
	//================================ Metadatas management ====================
	//==========================================================================

	// There is no all MetaData with values getter.
	// Developers should always knows which metadata they needs. It's intended use.
	//

	/**
	 * @return MetaData names (only not null ones)
	 */
	public Set<String> getMetaDataNames() {
		return Collections.unmodifiableSet(metaDatas.keySet());
	}

	/**
	 * @param metaDataName MetaData name
	 * @return if this metadata is known and not null
	 */
	public boolean containsMetaData(final String metaDataName) {
		return metaDatas.containsKey(metaDataName);
	}

	/**
	 * @param metaDataName MetaData name
	 * @param metaDataClass MetaData value class
	 * @param <O> MetaData value type
	 * @return MetaData value
	 */
	public <O extends Serializable> Optional<O> getMetaData(final String metaDataName, final Class<O> metaDataClass) {
		Assertion.checkArgNotEmpty(metaDataName);
		//-----
		final Object value = metaDatas.get(metaDataName);
		if (value == null) {
			return Optional.empty();
		}
		return Optional.of(metaDataClass.cast(value));
	}

	/**
	 * Set a metaData on this list. If value is null, the metadata is remove.
	 * <b>WARN</b>
	 * <b>Developers must ensure</b> this metaData keep coherent with current list datas, <b>all the time</b>.
	 * <b>WARN</b>
	 * @param metaDataName MetaData name
	 * @param value MetaData value
	 */
	public void setMetaData(final String metaDataName, final Serializable value) {
		Assertion.checkArgNotEmpty(metaDataName);
		//-----
		if (value == null) {
			metaDatas.remove(metaDataName);
		}
		metaDatas.put(metaDataName, value);
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object o) {
		/* A list equals only the same list */
		return o == this;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		/* A list equals only the same list, so hashCode is simpler */
		return System.identityHashCode(this);
	}
}
