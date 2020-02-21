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
package io.vertigo.account.plugins.account.store.datastore;

import java.io.Serializable;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.vertigo.account.account.Account;
import io.vertigo.account.account.AccountGroup;
import io.vertigo.account.impl.account.AccountMapperHelper;
import io.vertigo.account.impl.account.AccountStorePlugin;
import io.vertigo.account.plugins.account.store.AbstractAccountStorePlugin;
import io.vertigo.app.Home;
import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.commons.transaction.VTransactionWritable;
import io.vertigo.core.param.ParamValue;
import io.vertigo.dynamo.criteria.Criteria;
import io.vertigo.dynamo.criteria.Criterions;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.metamodel.FormatterException;
import io.vertigo.dynamo.domain.metamodel.association.AssociationDefinition;
import io.vertigo.dynamo.domain.metamodel.association.AssociationNNDefinition;
import io.vertigo.dynamo.domain.metamodel.association.AssociationSimpleDefinition;
import io.vertigo.dynamo.domain.metamodel.association.DtListURIForNNAssociation;
import io.vertigo.dynamo.domain.metamodel.association.DtListURIForSimpleAssociation;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtListState;
import io.vertigo.dynamo.domain.model.DtListURI;
import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.dynamo.domain.model.FileInfoURI;
import io.vertigo.dynamo.domain.model.UID;
import io.vertigo.dynamo.file.metamodel.FileInfoDefinition;
import io.vertigo.dynamo.file.model.VFile;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;

/**
 * Source of identity.
 * @author npiedeloup
 */
public final class StoreAccountStorePlugin extends AbstractAccountStorePlugin implements AccountStorePlugin {

	private final VTransactionManager transactionManager;
	private final StoreManager storeManager;

	private DtField userIdField;
	private final String groupIdentityEntity;
	private final String userAuthField;
	private final Optional<String> photoFileInfo;
	private Optional<FileInfoDefinition> photoFileInfoDefinition = Optional.empty();
	private DtDefinition userGroupDtDefinition;
	private DtField groupIdField;
	private final String groupToGroupAccountMappingStr;
	private AssociationDefinition associationUserGroup;
	private String associationGroupRoleName;
	private String associationUserRoleName;

	private AccountMapperHelper<DtField, GroupProperty> mapperHelper; //GroupProperty from UserGroupAttribute

	private enum GroupProperty {
		id, displayName
	}

	/**
	 * Constructor.
	 * @param userIdentityEntity Entity name of userIdentityEntity
	 * @param groupIdentityEntity Entity name of groupIdentityEntity
	 * @param userAuthField FieldName use to find user by it's authToken
	 * @param userToAccountMappingStr User to account conversion mapping
	 * @param groupToGroupAccountMappingStr User to account conversion mapping
	 * @param storeManager Store Manager
	 */
	@Inject
	public StoreAccountStorePlugin(
			@ParamValue("userIdentityEntity") final String userIdentityEntity,
			@ParamValue("groupIdentityEntity") final String groupIdentityEntity,
			@ParamValue("userAuthField") final String userAuthField,
			@ParamValue("photoFileInfo") final Optional<String> photoFileInfo,
			@ParamValue("userToAccountMapping") final String userToAccountMappingStr,
			@ParamValue("groupToGroupAccountMapping") final String groupToGroupAccountMappingStr,
			final StoreManager storeManager,
			final VTransactionManager transactionManager) {
		super(userIdentityEntity, userToAccountMappingStr);
		Assertion.checkArgNotEmpty(userIdentityEntity);
		Assertion.checkArgNotEmpty(userAuthField);
		Assertion.checkNotNull(storeManager);
		Assertion.checkArgNotEmpty(groupToGroupAccountMappingStr);

		this.groupIdentityEntity = groupIdentityEntity;
		this.userAuthField = userAuthField;
		this.photoFileInfo = photoFileInfo;
		this.storeManager = storeManager;
		this.transactionManager = transactionManager;
		this.groupToGroupAccountMappingStr = groupToGroupAccountMappingStr;
	}

	@Override
	protected void postStart() {
		userIdField = getUserDtDefinition().getIdField().get(); //Entity with Id mandatory

		if (photoFileInfo.isPresent()) {
			photoFileInfoDefinition = Optional.of(Home.getApp().getDefinitionSpace().resolve(photoFileInfo.get(), FileInfoDefinition.class));
		}

		userGroupDtDefinition = Home.getApp().getDefinitionSpace().resolve(groupIdentityEntity, DtDefinition.class);
		groupIdField = userGroupDtDefinition.getIdField().get(); //Entity with Id mandatory
		mapperHelper = new AccountMapperHelper(userGroupDtDefinition, GroupProperty.class, groupToGroupAccountMappingStr)
				.withMandatoryDestField(GroupProperty.id)
				.withMandatoryDestField(GroupProperty.displayName)
				.parseAttributeMapping();

		for (final AssociationDefinition association : Home.getApp().getDefinitionSpace().getAll(AssociationDefinition.class)) {
			if (userGroupDtDefinition.equals(association.getAssociationNodeA().getDtDefinition())
					&& getUserDtDefinition().equals(association.getAssociationNodeB().getDtDefinition())) {
				associationUserGroup = association;
				associationUserRoleName = association.getAssociationNodeB().getRole();
				associationGroupRoleName = association.getAssociationNodeA().getRole();
				break;
			} else if (userGroupDtDefinition.equals(association.getAssociationNodeB().getDtDefinition())
					&& getUserDtDefinition().equals(association.getAssociationNodeA().getDtDefinition())) {
				associationUserGroup = association;
				associationUserRoleName = association.getAssociationNodeA().getRole();
				associationGroupRoleName = association.getAssociationNodeB().getRole();
				break;
			}
		}
		Assertion.checkNotNull(associationUserGroup, "Association between User ({0}) and Group ({1}) not found",
				getUserDtDefinition().getClassSimpleName(), userGroupDtDefinition.getClassSimpleName());
		Assertion.checkState(associationUserGroup instanceof AssociationSimpleDefinition || associationUserGroup instanceof AssociationNNDefinition,
				"Association ({0}) between User and Group must be an AssociationSimpleDefinition or an AssociationNNDefinition", associationUserGroup.getName());
	}

	/** {@inheritDoc} */
	@Override
	public Account getAccount(final UID<Account> accountURI) {
		final Entity userEntity = readUserEntity(accountURI);
		return userToAccount(userEntity);
	}

	/** {@inheritDoc} */
	@Override
	public Set<UID<AccountGroup>> getGroupUIDs(final UID<Account> accountUID) {
		if (associationUserGroup instanceof AssociationSimpleDefinition) {
			//case 1 group per user
			final Entity userEntity = readUserEntity(accountUID);
			final Object fkValue = ((AssociationSimpleDefinition) associationUserGroup).getFKField().getDataAccessor().getValue(userEntity);
			final UID<AccountGroup> groupURI = UID.of(userGroupDtDefinition, fkValue);
			return Collections.singleton(groupURI);
		}
		//case N group per user
		//other case checked in postStart by assertions
		Assertion.checkArgument(associationUserGroup instanceof AssociationNNDefinition,
				"Association ({0}) between User and Group must be an AssociationSimpleDefinition or an AssociationNNDefinition", associationUserGroup.getName());
		final DtListURI groupDtListURI = new DtListURIForNNAssociation((AssociationNNDefinition) associationUserGroup, accountUID, associationGroupRoleName);

		//-----
		final DtList<? extends Entity> result = Home.getApp().getComponentSpace().resolve(StoreManager.class).getDataStore().findAll(groupDtListURI);
		return result.stream().map(groupEntity ->

		groupToAccount(groupEntity).getUID())
				.collect(Collectors.toSet());
	}

	/** {@inheritDoc} */
	@Override
	public AccountGroup getGroup(final UID<AccountGroup> accountGroupUID) {
		final Entity groupEntity = readGroupEntity(accountGroupUID);
		return groupToAccount(groupEntity);
	}

	/** {@inheritDoc} */
	@Override
	public Set<UID<Account>> getAccountUIDs(final UID<AccountGroup> groupUID) {
		final DtListURI userDtListURI;
		if (associationUserGroup instanceof AssociationSimpleDefinition) {
			userDtListURI = new DtListURIForSimpleAssociation((AssociationSimpleDefinition) associationUserGroup, groupUID, associationUserRoleName);
		} else { //autres cas éliminés par assertion dans le postStart
			Assertion.checkArgument(associationUserGroup instanceof AssociationNNDefinition,

					"Association ({0}) between User and Group must be an AssociationSimpleDefinition or an AssociationNNDefinition", associationUserGroup.getName());
			userDtListURI = new DtListURIForNNAssociation((AssociationNNDefinition) associationUserGroup, groupUID, associationUserRoleName);
		}
		//-----
		final DtList<? extends Entity> result = Home.getApp().getComponentSpace().resolve(StoreManager.class).getDataStore().findAll(userDtListURI);
		return result.stream()
				.map(userEntity -> userToAccount(userEntity).getUID())
				.collect(Collectors.toSet());
	}

	/** {@inheritDoc} */
	@Override
	public Optional<VFile> getPhoto(final UID<Account> accountURI) {
		final Account account = getAccount(accountURI);
		final String photoId = account.getPhoto();
		if (photoId != null && photoFileInfoDefinition.isPresent()) {
			return executeInTransaction(() -> {
				final FileInfoURI photoUri = new FileInfoURI(photoFileInfoDefinition.get(), photoId);
				return Optional.of(storeManager.getFileStore().read(photoUri).getVFile());
			});
		}
		return Optional.empty();
	}

	/** {@inheritDoc} */
	@Override
	public Optional<Account> getAccountByAuthToken(final String userAuthToken) {

		Serializable userAuthTokenValue;
		try {
			userAuthTokenValue = Serializable.class.cast(getUserDtDefinition().getField(userAuthField).getDomain().stringToValue(userAuthToken));
		} catch (final FormatterException e) {
			throw WrappedException.wrap(e);
		}
		final Criteria<Entity> criteriaByAuthToken = Criterions.isEqualTo(() -> userAuthField, userAuthTokenValue);
		return executeInTransaction(() -> {
			final DtList<Entity> results = storeManager.getDataStore().find(getUserDtDefinition(), criteriaByAuthToken, DtListState.of(2));
			Assertion.checkState(results.size() <= 1, "Too many matching for authToken {0}", userAuthToken);
			if (!results.isEmpty()) {
				return Optional.of(userToAccount(results.get(0)));
			}
			return Optional.empty();
		});
	}

	private AccountGroup groupToAccount(final Entity groupEntity) {
		final String groupId = parseAttribute(GroupProperty.id, groupEntity);
		final String displayName = parseAttribute(GroupProperty.displayName, groupEntity);
		return new AccountGroup(groupId, displayName);
	}

	private String parseAttribute(final GroupProperty accountProperty, final Entity userEntity) {
		final DtField attributeField = mapperHelper.getSourceAttribute(accountProperty);
		return String.valueOf(attributeField.getDataAccessor().getValue(userEntity));
	}

	private Entity readUserEntity(final UID<Account> accountURI) {
		return executeInTransaction(() -> {
			try {
				final Serializable typedId = (Serializable) userIdField.getDomain().stringToValue((String) accountURI.getId()); //account id IS always a String
				final UID<Entity> userURI = UID.of(getUserDtDefinition(), typedId);
				return storeManager.getDataStore().readOne(userURI);
			} catch (final FormatterException e) {
				throw WrappedException.wrap(e, "Can't get UserEntity from Store");
			}
		});
	}

	private Entity readGroupEntity(final UID<AccountGroup> accountGroupURI) {
		return executeInTransaction(() -> {
			try {
				final Serializable typedId = (Serializable) groupIdField.getDomain().stringToValue((String) accountGroupURI.getId()); //accountGroup id IS always a String
				final UID<Entity> groupURI = UID.of(userGroupDtDefinition, typedId);
				return storeManager.getDataStore().readOne(groupURI);
			} catch (final FormatterException e) {
				throw WrappedException.wrap(e, "Can't get UserGroupEntity from Store");
			}
		});
	}

	private <O extends Object> O executeInTransaction(final Supplier<O> supplier) {
		if (transactionManager.hasCurrentTransaction()) {
			return supplier.get();
		}
		//Dans le cas ou il n'existe pas de transaction on en crée une.
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			return supplier.get();
		}
	}
}
