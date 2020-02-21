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
package io.vertigo.studio.plugins.mda.domain.java.model;

import java.util.List;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateSequenceModel;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.association.AssociationNNDefinition;
import io.vertigo.dynamo.domain.metamodel.association.AssociationSimpleDefinition;
import io.vertigo.lang.Assertion;

/**
 * Permet de construire les lignes décritants l'annotation.
 *
 * @author dchallas
 */
final class AnnotationsModel implements TemplateSequenceModel {
	private final List<String> lines;

	/**
	 * Annotations pour la dtDefinition.
	 * @param annotationWriter AnnotationWriter
	 * @param propertyName Property name
	 */
	public AnnotationsModel(final AnnotationWriter annotationWriter, final String propertyName) {
		this(annotationWriter.writeAnnotations(propertyName));
	}

	/**
	 * Annotations pour la dtDefinition.
	 * @param annotationWriter AnnotationWriter
	 * @param dtDefinition DtDefinition
	 */
	public AnnotationsModel(final AnnotationWriter annotationWriter, final DtDefinition dtDefinition) {
		this(annotationWriter.writeAnnotations(dtDefinition));
	}

	/**
	 * Annotation pour un champ.
	 * @param annotationWriter AnnotationWriter
	 * @param dtField Champ
	 * @param dtDefinition DtDefinition
	 */
	AnnotationsModel(final AnnotationWriter annotationWriter, final DtFieldModel dtFieldModel) {
		this(annotationWriter.writeAnnotations(dtFieldModel.getSource()));
	}

	/**
	 * Annotations pour une association.
	 * @param annotationWriter AnnotationWriter
	 * @param associationSimple définition de l'association
	 */
	AnnotationsModel(final AnnotationWriter annotationWriter, final AssociationSimpleDefinition associationSimple) {
		this(annotationWriter.writeSimpleAssociationAnnotation(associationSimple));
	}

	/**
	 * Annotations pour une association.
	 * @param annotationWriter AnnotationWriter
	 * @param associationNN définition de l'association
	 */
	AnnotationsModel(final AnnotationWriter annotationWriter, final AssociationNNDefinition associationNN) {
		this(annotationWriter.writeNNAssociationAnnotation(associationNN));
	}

	private AnnotationsModel(final List<String> lines) {
		Assertion.checkNotNull(lines);
		//-----
		this.lines = lines;
	}

	/** {@inheritDoc} */
	@Override
	public TemplateModel get(final int index) {
		return new SimpleScalar(lines.get(index));
	}

	/** {@inheritDoc} */
	@Override
	public int size() {
		return lines.size();
	}
}
