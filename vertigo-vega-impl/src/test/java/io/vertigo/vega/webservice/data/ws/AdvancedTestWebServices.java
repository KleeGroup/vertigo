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
package io.vertigo.vega.webservice.data.ws;

import java.io.File;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import javax.inject.Inject;

import io.vertigo.core.resource.ResourceManager;
import io.vertigo.dynamo.collections.CollectionsManager;
import io.vertigo.dynamo.criteria.CriterionLimit;
import io.vertigo.dynamo.criteria.Criterions;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtListState;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.domain.util.VCollectors;
import io.vertigo.dynamo.file.FileManager;
import io.vertigo.dynamo.file.model.VFile;
import io.vertigo.lang.VUserException;
import io.vertigo.util.DateUtil;
import io.vertigo.vega.engines.webservice.json.UiContext;
import io.vertigo.vega.webservice.WebServices;
import io.vertigo.vega.webservice.data.domain.Contact;
import io.vertigo.vega.webservice.data.domain.ContactCriteria;
import io.vertigo.vega.webservice.data.domain.ContactDao;
import io.vertigo.vega.webservice.data.domain.ContactValidator;
import io.vertigo.vega.webservice.data.domain.EmptyPkValidator;
import io.vertigo.vega.webservice.data.domain.MandatoryPkValidator;
import io.vertigo.vega.webservice.model.ExtendedObject;
import io.vertigo.vega.webservice.stereotype.AccessTokenConsume;
import io.vertigo.vega.webservice.stereotype.AccessTokenMandatory;
import io.vertigo.vega.webservice.stereotype.AccessTokenPublish;
import io.vertigo.vega.webservice.stereotype.AnonymousAccessAllowed;
import io.vertigo.vega.webservice.stereotype.AutoSortAndPagination;
import io.vertigo.vega.webservice.stereotype.Doc;
import io.vertigo.vega.webservice.stereotype.ExcludedFields;
import io.vertigo.vega.webservice.stereotype.GET;
import io.vertigo.vega.webservice.stereotype.HeaderParam;
import io.vertigo.vega.webservice.stereotype.IncludedFields;
import io.vertigo.vega.webservice.stereotype.InnerBodyParam;
import io.vertigo.vega.webservice.stereotype.POST;
import io.vertigo.vega.webservice.stereotype.PUT;
import io.vertigo.vega.webservice.stereotype.PathParam;
import io.vertigo.vega.webservice.stereotype.PathPrefix;
import io.vertigo.vega.webservice.stereotype.QueryParam;
import io.vertigo.vega.webservice.stereotype.ServerSideRead;
import io.vertigo.vega.webservice.stereotype.ServerSideSave;
import io.vertigo.vega.webservice.stereotype.Validate;

//basé sur http://www.restapitutorial.com/lessons/httpmethods.html

@PathPrefix("/test")
public final class AdvancedTestWebServices implements WebServices {

	@Inject
	private CollectionsManager collectionsManager;
	@Inject
	private ResourceManager resourcetManager;
	@Inject
	private FileManager fileManager;
	@Inject
	private ContactDao contactDao;

	@GET("/grantAccess")
	@AccessTokenPublish
	public void testAccessToken() {
		//access token publish
	}

	@GET("/export/pdf/")
	public VFile testExportContacts() throws URISyntaxException {
		final URL tempFile = resourcetManager.resolve("io/vertigo/vega/webservice/data/ws/contacts.pdf");
		final VFile result = fileManager.createFile(new File(tempFile.toURI()));
		//200
		return result;
	}

	@GET("/export/pdf/{conId}")
	public VFile testExportContact(@PathParam("conId") final long conId) throws URISyntaxException {
		final URL tempFile = resourcetManager.resolve("io/vertigo/vega/webservice/data/ws/contact2.pdf");
		final VFile result = fileManager.createFile(new File(tempFile.toURI()));

		//200
		return result;
	}

	@GET("/limitedAccess/{conId}")
	@AccessTokenMandatory
	public Contact testAccessToken(@PathParam("conId") final long conId) {
		return testRead(conId);
	}

	@GET("/oneTimeAccess/{conId}")
	@AccessTokenConsume
	public Contact testAccessTokenConsume(@PathParam("conId") final long conId) {
		return testRead(conId);
	}

	private Contact testRead(final long conId) {
		final Contact contact = contactDao.get(conId);
		if (contact == null) {
			//404 ?
			throw new VUserException("Contact #" + conId + " unknown");
		}
		//200
		return contact;
	}

	@GET("/filtered/{conId}")
	@ExcludedFields({ "birthday", "email" })
	@ServerSideSave
	public Contact testFilteredRead(@PathParam("conId") final long conId) {
		final Contact contact = contactDao.get(conId);
		if (contact == null) {
			//404 ?
			throw new VUserException("Contact #" + conId + " unknown");
		}
		//200
		return contact;
	}

	//PUT is indempotent : ID obligatoire
	@Doc("Exclude conId and name.")
	@PUT("/filtered/*")
	@ServerSideSave
	public Contact filteredUpdateByExclude(
			final @Validate({ ContactValidator.class, MandatoryPkValidator.class }) @ServerSideRead @ExcludedFields({ "conId", "name" }) Contact contact) {
		if (contact.getName() == null || contact.getName().isEmpty()) {
			//400
			throw new VUserException("Name is mandatory");
		}

		contactDao.put(contact);
		//200
		return contact;
	}

	//PUT is indempotent : ID obligatoire
	@Doc("Only accept firstName and email. Will blocked if other fields are send.")
	@PUT("/filteredInclude/*")
	@ServerSideSave
	public Contact filteredUpdateByInclude(//
			final @Validate({ ContactValidator.class, MandatoryPkValidator.class }) //
			@ServerSideRead //
			@IncludedFields({ "firstName", "email" }) Contact contact) {
		if (contact.getName() == null || contact.getName().isEmpty()) {
			//400
			throw new VUserException("Name is mandatory");
		}

		contactDao.put(contact);
		//200
		return contact;
	}

	@Doc("Test ws returning UiContext. Send a body with an object of to field : contactId1, contactId2. Each one should be an json of long. You get partial Contacts with clientId in each one")
	@ServerSideSave
	@ExcludedFields({ "conId", "email", "birthday", "address", "tels" })
	@POST("/uiContext")
	public UiContext testInnerBody(@InnerBodyParam("contactId1") final long contactIdFrom, @InnerBodyParam("contactId2") final long contactIdTo) {
		final UiContext uiContext = new UiContext();
		uiContext.put("contactFrom", contactDao.get(contactIdFrom));
		uiContext.put("contactTo", contactDao.get(contactIdTo));
		uiContext.put("testLong", 12);
		uiContext.put("testString", "the String test");
		uiContext.put("testDate", DateUtil.newDate());
		uiContext.put("testEscapedString", "the EscapedString \",} test");
		//offset + range ?
		//code 200
		return uiContext;
	}

	@Doc("Test ws with multiple path params.")
	@ExcludedFields({ "address", "tels" })
	@POST("/multiPath/from/{conIdFrom}/to/{conIdTo}")
	public DtList<Contact> testMultiPathParam(//
			@PathParam("conIdFrom") final long contactIdFrom, //
			@PathParam("conIdTo") final long contactIdTo) {
		final DtList<Contact> result = new DtList<>(Contact.class);
		result.add(contactDao.get(contactIdFrom));
		result.add(contactDao.get(contactIdTo));
		//offset + range ?
		//code 200
		return result;
	}

	@Doc("Test ws multipart body with serverSide objects. Send a body with an object of to field : contactFrom, contactTo. Each one should be an partial json of Contact with clientId.")
	@POST("/innerBodyServerClient")
	public List<Contact> testInnerBodyClientId(//
			@InnerBodyParam("contactFrom") @ServerSideRead final Contact contactFrom, //
			@InnerBodyParam("contactTo") @ServerSideRead final Contact contactTo) {
		//offset + range ?
		//code 200
		return Arrays.asList(contactFrom, contactTo);
	}

	@POST("/search()")
	@ExcludedFields({ "conId", "email", "birthday", "address", "tels" })
	public List<Contact> testSearch(//
			@ExcludedFields({ "conId", "email", "birthday", "address", "tels" }) final ContactCriteria contact) {
		final Predicate<Contact> predicate = createFilterFunction(contact, Contact.class);
		final DtList<Contact> allContacts = asDtList(contactDao.getList(), Contact.class);
		//offset + range ?
		//code 200
		return allContacts.stream()
				.filter(predicate)
				.collect(VCollectors.toDtList(Contact.class));
	}

	@POST("/searchPagined()")
	@ExcludedFields({ "conId", "email", "birthday", "address", "tels" })
	public List<Contact> testSearchServicePagined(//
			@InnerBodyParam("criteria") final ContactCriteria contact, //
			@InnerBodyParam("listState") final DtListState dtListState) {
		final Predicate<Contact> predicate = createFilterFunction(contact, Contact.class);
		final DtList<Contact> allContacts = asDtList(contactDao.getList(), Contact.class);

		final DtList<Contact> filteredContacts = allContacts.stream()
				.filter(predicate)
				.collect(VCollectors.toDtList(Contact.class));

		//offset + range ?
		//code 200
		return applySortAndPagination(filteredContacts, dtListState);
	}

	@POST("/_searchQueryPagined")
	@ExcludedFields({ "conId", "email", "birthday", "address", "tels" })
	public List<Contact> testSearchServiceQueryPagined(
			final ContactCriteria contact,
			@QueryParam("") final DtListState dtListState) {
		final Predicate<Contact> predicate = createFilterFunction(contact, Contact.class);
		final DtList<Contact> allContacts = asDtList(contactDao.getList(), Contact.class);
		final DtList<Contact> filteredContacts = allContacts.stream()
				.filter(predicate)
				.collect(VCollectors.toDtList(Contact.class));

		//offset + range ?
		//code 200
		return applySortAndPagination(filteredContacts, dtListState);
	}

	@AutoSortAndPagination
	@POST("/_searchAutoPagined")
	@ExcludedFields({ "conId", "email", "birthday", "address", "tels" })
	public List<Contact> testSearchServiceAutoPagined(final ContactCriteria contact) {
		final Predicate<Contact> predicate = createFilterFunction(contact, Contact.class);
		final DtList<Contact> allContacts = asDtList(contactDao.getList(), Contact.class);
		//offset + range ?
		//code 200
		return allContacts.stream()
				.filter(predicate)
				.collect(VCollectors.toDtList(Contact.class));
	}

	@POST("/uploadFile")
	public VFile testUploadFile(
			final @QueryParam("upfile") VFile inputFile, //
			final @QueryParam("id") Integer id, //
			final @QueryParam("note") String note) {

		return inputFile;
	}

	@AnonymousAccessAllowed
	@POST("/uploadFileFocus")
	public Integer testUploadFile(final @QueryParam("upfile") VFile inputFile) {
		return 1337;
	}

	@GET("/headerParams")
	@Doc("Just send x-test-param:\"i'ts fine\"")
	public String testHeaderParams(final @HeaderParam("x-test-param") String testParam) {
		if (!"i'ts fine".equals(testParam)) {
			throw new VUserException("Bad param value. Read doc.");
		}
		return "OK";
	}

	@GET("/contactExtended/{conId}")
	public ExtendedObject<Contact> testGetExtended(@PathParam("conId") final long conId) {
		final Contact contact = contactDao.get(conId);
		final ExtendedObject<Contact> result = new ExtendedObject<>(contact);
		result.put("vanillaUnsupportedMultipleIds", new int[] { 1, 2, 3 });
		//200
		return result;
	}

	@PUT("/contactExtended/{conId}")
	public ExtendedObject<Contact> testGetExtended(
			@PathParam("conId") final long conId,
			final @Validate({ ContactValidator.class, EmptyPkValidator.class }) Contact contact,
			@InnerBodyParam("vanillaUnsupportedMultipleIds") final int[] multipleIds) {
		contact.setConId(conId);
		contactDao.put(contact);
		final ExtendedObject<Contact> result = new ExtendedObject<>(contact);
		result.put("vanillaUnsupportedMultipleIds", multipleIds);
		//200
		return result;
	}

	private static <D extends DtObject> DtList<D> asDtList(final Collection<D> values, final Class<D> dtObjectClass) {
		final DtList<D> result = new DtList<>(dtObjectClass);
		for (final D element : values) {
			result.add(element);
		}
		return result;
	}

	private <D extends DtObject> DtList<D> applySortAndPagination(final DtList<D> unFilteredList, final DtListState dtListState) {
		final DtList<D> sortedList;
		if (dtListState.getSortFieldName().isPresent()) {
			sortedList = collectionsManager.sort(unFilteredList, dtListState.getSortFieldName().get(), dtListState.isSortDesc().get());
		} else {
			sortedList = unFilteredList;
		}
		if (dtListState.getMaxRows().isPresent()) {
			return sortedList
					.stream()
					.skip(dtListState.getSkipRows())
					.limit(dtListState.getMaxRows().get())
					.collect(VCollectors.toDtList(sortedList.getDefinition()));
		}
		return sortedList;
	}

	private <C extends DtObject, E extends Entity> Predicate<E> createFilterFunction(final C criteria, final Class<E> resultClass) {
		Predicate<E> filter = (o) -> true;
		final DtDefinition criteriaDefinition = DtObjectUtil.findDtDefinition(criteria);
		final DtDefinition resultDefinition = DtObjectUtil.findDtDefinition(resultClass);
		final Set<String> alreadyAddedField = new HashSet<>();
		for (final DtField field : criteriaDefinition.getFields()) {
			final String fieldName = field.getName();
			if (!alreadyAddedField.contains(fieldName)) { //when we consume two fields at once (min;max)
				final Object value = field.getDataAccessor().getValue(criteria);
				if (value != null) {
					if (fieldName.endsWith("Min") || fieldName.endsWith("Max")) {
						final String filteredField = fieldName.substring(0, fieldName.length() - "Min".length());
						final DtField resultDtField = resultDefinition.getField(filteredField);
						final DtField minField = fieldName.endsWith("Min") ? field : criteriaDefinition.getField(filteredField + "Min");
						final DtField maxField = fieldName.endsWith("Max") ? field : criteriaDefinition.getField(filteredField + "Max");
						final Serializable minValue = (Serializable) minField.getDataAccessor().getValue(criteria);
						final Serializable maxValue = (Serializable) maxField.getDataAccessor().getValue(criteria);
						filter = filter.and(Criterions.isBetween(() -> resultDtField.getName(),
								CriterionLimit.ofIncluded(minValue),
								CriterionLimit.ofExcluded(maxValue))
								.toPredicate());
					} else {
						final Predicate predicate = Criterions.isEqualTo(() -> fieldName, Serializable.class.cast(value)).toPredicate();
						filter.and(predicate);
					}
				}
			}
			//si null, alors on ne filtre pas
		}
		return filter;
	}

}
