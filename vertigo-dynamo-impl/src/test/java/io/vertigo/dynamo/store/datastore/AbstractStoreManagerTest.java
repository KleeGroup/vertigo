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
package io.vertigo.dynamo.store.datastore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.AbstractTestCaseJU5;
import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.commons.transaction.VTransactionWritable;
import io.vertigo.core.definition.DefinitionSpace;
import io.vertigo.dynamo.TestUtil;
import io.vertigo.dynamo.criteria.Criterions;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtListState;
import io.vertigo.dynamo.domain.model.UID;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.file.FileManager;
import io.vertigo.dynamo.file.model.FileInfo;
import io.vertigo.dynamo.file.model.VFile;
import io.vertigo.dynamo.file.util.FileUtil;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.dynamo.store.data.domain.car.Car;
import io.vertigo.dynamo.store.data.domain.car.CarDataBase;
import io.vertigo.dynamo.store.data.domain.car.MotorTypeEnum;
import io.vertigo.dynamo.store.data.domain.famille.Famille;
import io.vertigo.dynamo.store.data.domain.famille.Famille.CarFields;
import io.vertigo.dynamo.store.data.fileinfo.FileInfoStd;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamo.task.model.Task;
import io.vertigo.dynamo.task.model.TaskResult;
import io.vertigo.dynamox.task.TaskEngineProc;
import io.vertigo.dynamox.task.TaskEngineSelect;
import io.vertigo.lang.Assertion;
import io.vertigo.util.ListBuilder;

/**
 * Test de l'implémentation standard.
 *
 * @author pchretien
 */
public abstract class AbstractStoreManagerTest extends AbstractTestCaseJU5 {
	@Inject
	protected StoreManager storeManager;
	@Inject
	protected FileManager fileManager;
	@Inject
	protected VTransactionManager transactionManager;
	@Inject
	protected TaskManager taskManager;

	protected DtDefinition dtDefinitionFamille;
	private DtDefinition dtDefinitionCar;

	private CarDataBase carDataBase;

	@Override
	protected void doSetUp() throws Exception {
		carDataBase = new CarDataBase();
		dtDefinitionFamille = DtObjectUtil.findDtDefinition(Famille.class);

		dtDefinitionCar = DtObjectUtil.findDtDefinition(Car.class);

		initMainStore();
	}

	protected void initMainStore() {
		//A chaque test on recrée la table famille
		SqlUtil.execRequests(
				transactionManager,
				taskManager,
				getCreateMainStoreRequests(),
				"TkInitMain",
				Optional.empty());

		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			for (final Car car : carDataBase.getAllCars()) {
				car.setId(null);
				storeManager.getDataStore().create(car);
			}
			transaction.commit();
		}
	}

	protected List<String> getCreateMainStoreRequests() {
		return new ListBuilder<String>()
				.addAll(getCreateFamilleRequests())
				.addAll(getCreateCarRequests())
				.addAll(getCreateFileInfoRequests())
				.build();
	}

	protected List<String> getCreateFamilleRequests() {
		return new ListBuilder<String>()
				.add(" create table famille(FAM_ID BIGINT , LIBELLE varchar(255))")
				.add(" create sequence SEQ_FAMILLE start with 10001 increment by 1")
				.build();
	}

	protected List<String> getCreateCarRequests() {
		return new ListBuilder<String>()
				.add(" create table fam_car_location(FAM_ID BIGINT, ID BIGINT)")
				.add(" create table motor_type(MTY_CD varchar(50) , LABEL varchar(255))")
				.add("insert into motor_type(MTY_CD, LABEL) values ('ESSENCE', 'Essence')")
				.add("insert into motor_type(MTY_CD, LABEL) values ('DIESEL', 'Diesel')")
				.add(" create table car(ID BIGINT, FAM_ID BIGINT, MANUFACTURER varchar(50), MODEL varchar(255), DESCRIPTION varchar(512), YEAR INT, KILO INT, PRICE INT, CONSOMMATION NUMERIC(8,2), MTY_CD varchar(50) )")
				.add(" create sequence SEQ_CAR start with 10001 increment by 1")
				.build();
	}

	protected List<String> getCreateFileInfoRequests() {
		return new ListBuilder<String>()
				.add(" create table VX_FILE_INFO(FIL_ID BIGINT , FILE_NAME varchar(255), MIME_TYPE varchar(255), LENGTH BIGINT, LAST_MODIFIED date, FILE_PATH varchar(255), FILE_DATA BLOB)")
				.add(" create sequence SEQ_VX_FILE_INFO start with 10001 increment by 1")
				.build();
	}

	protected List<String> getDropRequests() {
		return new ListBuilder<String>()
				.add(" drop table if exists VX_FILE_INFO ")
				.add(" drop sequence if exists SEQ_VX_FILE_INFO")
				.add(" drop table if exists fam_car_location")
				.add(" drop table if exists car")
				.add(" drop table if exists motor_type")
				.add(" drop sequence if exists SEQ_CAR")
				.add(" drop table if exists famille")
				.add(" drop sequence if exists SEQ_FAMILLE")
				.build();
	}

	@Override
	protected void doTearDown() throws Exception {
		cleanDb();
	}

	protected void cleanDb() {
		SqlUtil.execRequests(
				transactionManager,
				taskManager,
				getDropRequests(),
				"TkShutDown",
				Optional.empty());
	}

	@Test
	public void testSelectCarCachedRowMax() {
		try (VTransactionWritable tx = transactionManager.createCurrentTransaction()) {
			final DtList<Car> dtc1 = storeManager.getDataStore().find(dtDefinitionCar, Criterions.alwaysTrue(), DtListState.of(3));
			Assertions.assertEquals(3, dtc1.size());
			//-----
			final DtList<Car> dtc = storeManager.getDataStore().find(dtDefinitionCar, Criterions.alwaysTrue(), DtListState.of(null));
			Assertions.assertEquals(9, dtc.size());
		}
	}

	@Test
	public void testSelectCarDtListState() {
		try (VTransactionWritable tx = transactionManager.createCurrentTransaction()) {
			final DtList<Car> dtc1 = storeManager.getDataStore().find(dtDefinitionCar, Criterions.alwaysTrue(), DtListState.of(3));
			Assertions.assertEquals(3, dtc1.size());
			//-----
			final DtList<Car> dtc2 = storeManager.getDataStore().find(dtDefinitionCar, Criterions.alwaysTrue(), DtListState.of(3, 2, null, null));
			Assertions.assertEquals(3, dtc2.size());
			Assertions.assertEquals(dtc1.get(2).getId(), dtc2.get(0).getId());
			//-----
			final DtList<Car> dtc3 = storeManager.getDataStore().find(dtDefinitionCar, Criterions.alwaysTrue(), DtListState.of(3, 0, CarFields.manufacturer.name(), false));
			Assertions.assertEquals(3, dtc3.size());
			Assertions.assertEquals("Audi", dtc3.get(0).getManufacturer());
			//-----
			final DtList<Car> dtc4 = storeManager.getDataStore().find(dtDefinitionCar, Criterions.alwaysTrue(), DtListState.of(3, 0, CarFields.manufacturer.name(), true));
			Assertions.assertEquals(3, dtc4.size());
			Assertions.assertEquals("Volkswagen", dtc4.get(0).getManufacturer());
			//-----
			final DtList<Car> dtc5 = storeManager.getDataStore().find(dtDefinitionCar, Criterions.alwaysTrue(), DtListState.of(3, 0, CarFields.price.name(), false));
			Assertions.assertEquals(3, dtc5.size());
			Assertions.assertEquals(2500, dtc5.get(0).getPrice().intValue());
			//-----
			final DtList<Car> dtc6 = storeManager.getDataStore().find(dtDefinitionCar, Criterions.alwaysTrue(), DtListState.of(3, 0, CarFields.price.name(), true));
			Assertions.assertEquals(3, dtc6.size());
			Assertions.assertEquals(109000, dtc6.get(0).getPrice().intValue());
		}
	}

	@Test
	public void testSelectCarAndTestMasterDataEnum() {
		try (VTransactionWritable tx = transactionManager.createCurrentTransaction()) {
			final DtList<Car> dtcEssence = storeManager.getDataStore().find(
					DtObjectUtil.findDtDefinition(Car.class),
					Criterions.isEqualTo(CarFields.mtyCd, MotorTypeEnum.essence.getEntityUID().getId()),
					DtListState.of(2));
			//---
			Assertions.assertEquals(1, dtcEssence.size());
			Assertions.assertTrue(dtcEssence.get(0).motorType().getEnumValue() == MotorTypeEnum.essence);
		}
	}

	@Test
	public void testSelectCountCars() {
		final TaskDefinition taskDefinition = TaskDefinition.builder("TkCountCars")
				.withEngine(TaskEngineSelect.class)
				.withRequest("select count(*) from CAR")
				.withOutRequired("count", Domain.builder("DoCount", DataType.Long).build())
				.build();

		try (VTransactionWritable tx = transactionManager.createCurrentTransaction()) {
			final Task task = Task.builder(taskDefinition).build();
			final long count = taskManager
					.execute(task)
					.getResult();
			//-----
			Assertions.assertEquals(9, count);
		}
	}

	protected void nativeInsertCar(final Car car) {
		Assertion.checkArgument(car.getId() == null, "L'id n'est pas null {0}", car.getId());
		//-----
		final DefinitionSpace definitionSpace = getApp().getDefinitionSpace();
		final Domain doCar = definitionSpace.resolve("DoDtCarDto", Domain.class);

		final TaskDefinition taskDefinition = TaskDefinition.builder("TkInsertCar")
				.withEngine(TaskEngineProc.class)
				.withRequest("insert into CAR (ID, FAM_ID,MANUFACTURER, MODEL, DESCRIPTION, YEAR, KILO, PRICE, MTY_CD) values "
						+ "(NEXT VALUE FOR SEQ_CAR, #dtoCar.famId#, #dtoCar.manufacturer#, #dtoCar.model#, #dtoCar.description#, #dtoCar.year#, #dtoCar.kilo#, #dtoCar.price#, #dtoCar.mtyCd#)")
				.addInRequired("dtoCar", doCar)
				.build();

		final Task task = Task.builder(taskDefinition)
				.addValue("dtoCar", car)
				.build();
		final TaskResult taskResult = taskManager
				.execute(task);
		nop(taskResult);
	}

	protected final DtList<Car> nativeLoadCarList() {
		final DefinitionSpace definitionSpace = getApp().getDefinitionSpace();
		final Domain doCarList = definitionSpace.resolve("DoDtCarDtc", Domain.class);

		final TaskDefinition taskDefinition = TaskDefinition.builder("TkLoadAllCars")
				.withEngine(TaskEngineSelect.class)
				.withRequest("select * from CAR")
				.withOutRequired("dtc", doCarList)
				.build();

		final Task task = Task.builder(taskDefinition)
				.build();
		return taskManager
				.execute(task)
				.getResult();
	}

	/**
	 * On vérifie que la liste est vide.
	 */
	@Test
	public void testGetFamille() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final DtList<Famille> dtc = storeManager.getDataStore().find(dtDefinitionFamille, null, DtListState.of(null));
			Assertions.assertNotNull(dtc);
			Assertions.assertTrue(dtc.isEmpty(), "La liste des famille est vide");
			transaction.commit();
		}
	}

	/**
	 * On charge une liste, ajoute un element et recharge la liste pour verifier l'ajout.
	 */
	@Test
	public void testAddFamille() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			DtList<Famille> dtc = storeManager.getDataStore().find(dtDefinitionFamille, null, DtListState.of(null));
			Assertions.assertEquals(0, dtc.size());
			//-----
			final Famille famille = new Famille();
			famille.setLibelle("encore un");
			final Famille createdFamille = storeManager.getDataStore().create(famille);
			// on attend un objet avec un id non null ?
			Assertions.assertNotNull(createdFamille.getFamId());
			//-----
			dtc = storeManager.getDataStore().find(dtDefinitionFamille, null, DtListState.of(null));
			Assertions.assertEquals(1, dtc.size());
			transaction.commit();
		}
	}

	/**
	 * on vérifier l'exception levée si une contrainte bdd n'est pas respecté.
	 */
	@Test
	public void testCreateFamilleFail() {
		Assertions.assertThrows(Exception.class, () -> {
			try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
				final DecimalFormat df = new DecimalFormat("000000000:");
				//-----
				final Famille famille = new Famille();
				final StringBuilder sb = new StringBuilder();
				for (int i = 0; i < 4000; i++) {
					sb.append(df.format(i));
				}
				// libelle
				famille.setLibelle(sb.toString());
				//On doit échouer car le libellé est trop long
				storeManager.getDataStore().create(famille);
				Assertions.fail();
			}
		});
	}

	@Test
	public void testCreateFile() throws Exception {
		doCreateFile(this::createFileInfo);
	}

	@Test
	public void testUpdateFile() throws Exception {
		doUpdateFile(this::createFileInfo);
	}

	@Test
	public void testDeleteFile() throws Exception {
		doDeleteFile(this::createFileInfo);
	}

	protected void doCreateFile(final Function<VFile, FileInfo> createFileInfoFct) throws Exception {
		//1.Création du fichier depuis un fichier texte du FS
		final VFile vFile = TestUtil.createVFile(fileManager, "../data/lautreamont.txt", AbstractStoreManagerTest.class);
		//2. Sauvegarde en BDD
		final FileInfo fileInfo = createFileInfoFct.apply(vFile);
		final FileInfo createdFileInfo;
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//2. Sauvegarde en Temp
			createdFileInfo = storeManager.getFileStore().create(fileInfo);
			transaction.commit(); //can't read file if not commited (TODO ?)

			System.out.println("doCreateFile " + createdFileInfo.getURI().toURN());
		}

		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//3.relecture du fichier
			final FileInfo readFileInfo = storeManager.getFileStore().read(createdFileInfo.getURI());

			//4. comparaison du fichier créé et du fichier lu.

			final String source;
			try (final OutputStream sourceOS = new java.io.ByteArrayOutputStream()) {
				try (final InputStream in = vFile.createInputStream()) {
					FileUtil.copy(in, sourceOS);
				}
				source = sourceOS.toString();
			}

			final String read;
			try (final OutputStream readOS = new java.io.ByteArrayOutputStream()) {
				try (final InputStream in = readFileInfo.getVFile().createInputStream()) {
					FileUtil.copy(in, readOS);
				}
				read = readOS.toString();
			}
			//on vérifie que le contenu des fichiers est identique.
			//assertEquals("toto", "toto");
			//assertEquals("toto", "ti");
			Assertions.assertEquals(source, read);
			Assertions.assertTrue(read.startsWith("Chant I"), "Test contenu du fichier");
			Assertions.assertTrue(read.indexOf("ses notes langoureuses,") > 0, "Test contenu du fichier : " + secureSubString(read, 16711, "ses notes langoureuses,"));
			Assertions.assertTrue(read.indexOf("mal : \"Adolescent,") > 0, "Test contenu du fichier : " + secureSubString(read, 11004, "mal : \"Adolescent,"));

			//On désactive pour l'instant
			//Ne marche pas sur la PIC pour cause de charset sur le àé			//Assertions.assertTrue("Test contenu du fichier : " + secureSubString(read, 15579, "adieu !à ;"), read.indexOf("adieu !à ;") > 0);
		}
	}

	protected void doDeleteFile(final Function<VFile, FileInfo> createFileInfoFct) throws Exception {
		//1.Création du fichier depuis un fichier texte du FS
		final VFile vFile = TestUtil.createVFile(fileManager, "../data/lautreamont.txt", AbstractStoreManagerTest.class);
		//2. Sauvegarde en BDD
		final FileInfo fileInfo = createFileInfoFct.apply(vFile);
		final FileInfo createdFileInfo;
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//2. Sauvegarde en Temp
			createdFileInfo = storeManager.getFileStore().create(fileInfo);
			transaction.commit(); //can't read file if not commited (TODO ?)

			System.out.println("doDeleteFile " + createdFileInfo.getURI().toURN());
		}

		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//3.relecture du fichier
			storeManager.getFileStore().read(createdFileInfo.getURI());
		}
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//4. suppression.
			storeManager.getFileStore().delete(createdFileInfo.getURI());
			transaction.commit();
		}
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//3.relecture du fichier
			Assertions.assertThrows(NullPointerException.class, () -> {
				storeManager.getFileStore().read(createdFileInfo.getURI());
			});
		}
	}

	protected void doUpdateFile(final Function<VFile, FileInfo> createFileInfoFct) throws Exception {
		//1.Création du fichier depuis un fichier texte du FS

		final VFile vFile = TestUtil.createVFile(fileManager, "../data/execution.kpr", AbstractStoreManagerTest.class);
		final VFile vFile2 = TestUtil.createVFile(fileManager, "../data/lautreamont.txt", AbstractStoreManagerTest.class);
		//2. Sauvegarde en BDD
		final FileInfo createdFileInfo;
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final FileInfo fileInfo = createFileInfoFct.apply(vFile);
			createdFileInfo = storeManager.getFileStore().create(fileInfo);
			transaction.commit();
			System.out.println("doUpdateFile " + createdFileInfo.getURI().toURN());
		}
		final FileInfo readFileInfo;
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//3. relecture du fichier
			readFileInfo = storeManager.getFileStore().read(createdFileInfo.getURI());
		}

		//4. comparaison du fichier créé et du fichier lu.
		final String source = readFileContent(vFile2);
		final String read = readFileContent(readFileInfo.getVFile());

		//on vérifie que le contenu des fichiers est différent.
		Assertions.assertNotEquals(source, read);

		//2. Mise à jour en BDD
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final FileInfo fileInfo2 = createFileInfoFct.apply(vFile2);
			fileInfo2.setURIStored(createdFileInfo.getURI());
			storeManager.getFileStore().update(fileInfo2);
			transaction.commit();
			System.out.println("doUpdateFile2 " + createdFileInfo.getURI().toURN());
		}
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {

			//3. relecture du fichier
			final FileInfo readFileInfo2 = storeManager.getFileStore().read(createdFileInfo.getURI());

			//4. comparaison du fichier créé et du fichier lu.
			final String read2 = readFileContent(readFileInfo2.getVFile());

			//on vérifie que le contenu des fichiers est identique.
			//assertEquals("toto", "toto");
			//assertEquals("toto", "ti");
			Assertions.assertEquals(source, read2);
			Assertions.assertTrue(read2.startsWith("Chant I"), "Test contenu du fichier");
			Assertions.assertTrue(read2.indexOf("ses notes langoureuses,") > 0, "Test contenu du fichier : " + secureSubString(read2, 16711, "ses notes langoureuses,"));
			Assertions.assertTrue(read2.indexOf("mal : \"Adolescent,") > 0, "Test contenu du fichier : " + secureSubString(read2, 11004, "mal : \"Adolescent,"));
		}

	}

	private String readFileContent(final VFile vFile) throws IOException {
		try (final OutputStream sourceOS = new ByteArrayOutputStream()) {
			try (final InputStream fileIS = vFile.createInputStream()) {
				FileUtil.copy(fileIS, sourceOS);
			}
			return sourceOS.toString();
		}
	}

	protected FileInfo createFileInfo(final VFile vFile) {
		return new FileInfoStd(vFile);
	}

	protected static String secureSubString(final String read, final int index, final String searchString) {
		if (read != null && read.length() > index) {
			return read.substring(index, Math.min(read.length() - 1, index + searchString.length()));
		}
		return "N/A";
	}

	/**
	 * Test que les listes NN ne reste pas en cache après une mise à jour.
	 * Ici l'entité en cache est la destination de la navigation : Car
	 */
	@Test
	public void testGetFamilleLocationCars() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//on crée une famille
			final Famille famille = new Famille();
			famille.setLibelle("Ma famille");
			final Famille createdFamille = storeManager.getDataStore().create(famille);

			//on récupère la liste des voitures
			final DtList<Car> cars = storeManager.getDataStore().find(dtDefinitionCar, null, DtListState.of(null));
			Assertions.assertNotNull(cars);
			Assertions.assertFalse(cars.isEmpty(), "La liste des cars est vide");

			//on associe la liste de voiture à la famille en NN
			final List<UID> carUriList = new ArrayList<>();
			for (final Car car : cars) {
				carUriList.add(car.getUID());
			}
			storeManager.getDataStore().getBrokerNN().updateNN(createdFamille.getVoituresLocationDtListURI(), carUriList);

			//On garde le résultat de l'association NN
			final DtList<Car> firstResult = createdFamille.getVoituresLocationList();
			Assertions.assertEquals(cars.size(), firstResult.size(), "Test tailles du nombre de voiture dans une NN");

			//On met à jour l'association en retirant le premier élément
			carUriList.remove(0);
			storeManager.getDataStore().getBrokerNN().updateNN(createdFamille.getVoituresLocationDtListURI(), carUriList);

			//on garde le résultat en lazy : il doit avoir le meme nombre de voiture qu'au début
			final DtList<Car> lazyResult = createdFamille.getVoituresLocationList();
			Assertions.assertEquals(firstResult.size(), lazyResult.size(), "Test tailles du nombre de voiture pour une NN");

			//on recharge la famille et on recharge la liste issus de l'association NN : il doit avoir une voiture de moins qu'au début
			final Famille famille2 = storeManager.getDataStore().readOne(UID.of(Famille.class, createdFamille.getFamId()));
			final DtList<Car> secondResult = famille2.getVoituresLocationList();
			Assertions.assertEquals(firstResult.size() - 1, secondResult.size(), "Test tailles du nombre de voiture dans une NN");
			transaction.commit();
		}
	}

	/**
	 * Test que les listes 1N ne reste pas en cache après une mise à jour.
	 * Ici l'entité en cache est la destination de la navigation : Car
	 */
	@Test
	public void testGetFamilliesCars() {
		//on crée une famille
		final Famille famille = new Famille();
		famille.setLibelle("Ma famille");

		final DtList<Car> firstResult;
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Famille createdFamille = storeManager.getDataStore().create(famille);

			//on récupère la liste des voitures
			final DtList<Car> cars = storeManager.getDataStore().find(dtDefinitionCar, null, DtListState.of(null));
			Assertions.assertNotNull(cars);
			Assertions.assertFalse(cars.isEmpty(), "La liste des cars est vide");

			//on associe la liste de voiture à la famille en 1N
			for (final Car car : cars) {
				car.setFamId(createdFamille.getFamId());
				storeManager.getDataStore().update(car);
			}

			//On garde le résultat de l'association 1N
			firstResult = createdFamille.getVoituresFamilleList();

			//On met à jour l'association en retirant le premier élément
			final Car firstCar = cars.get(0);
			firstCar.setFamId(null);
			storeManager.getDataStore().update(firstCar);
			transaction.commit(); //sans commit le cache n'est pas rafraichit
		}

		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {

			//on garde le résultat en lazy : il doit avoir le meme nombre de voiture qu'au début
			final DtList<Car> lazyResult = famille.getVoituresFamilleList();
			Assertions.assertEquals(firstResult.size(), lazyResult.size(), "Test tailles du nombre de voiture pour une 1-N");

			//on recharge la famille et on recharge la liste issus de l'association 1N : il doit avoir une voiture de moins qu'au début
			final Famille famille2 = storeManager.getDataStore().readOne(famille.getUID());
			final DtList<Car> secondResult = famille2.getVoituresFamilleList();
			Assertions.assertEquals(firstResult.size() - 1, secondResult.size(), "Test tailles du nombre de voiture pour une 1-N");
			transaction.commit();
		}
	}

	@Test
	public void testTxCrudSelectRollback() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//on récupère la liste des voitures
			checkCrudCarsCount(0);
		}
	}

	@Test
	public void testTxNativeSelectRollback() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			checkNativeCarsCount(0);
		}
	}

	@Test
	public void testTxCrudInsertCrudSelectRollback() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar();
			storeManager.getDataStore().create(car);

			//on récupère la liste des voitures
			checkCrudCarsCount(1);
		}
	}

	@Test
	public void testTxNativeInsertCrudSelectRollback() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar();
			nativeInsertCar(car);

			//on récupère la liste des voitures
			checkCrudCarsCount(1);
		}
	}

	@Test
	public void testTxCrudInsertNativeSelectRollback() {
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar();
			storeManager.getDataStore().create(car);

			//on récupère la liste des voitures
			checkNativeCarsCount(1);
		}
	}

	@Test
	public void testTxNativeInsertNativeSelectRollback() {
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar();
			nativeInsertCar(car);

			//on récupère la liste des voitures
			checkNativeCarsCount(1);
		}
	}

	@Test
	public void testTxCrudInsertRollbackCrudSelectRollback() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar();
			storeManager.getDataStore().create(car);
		}
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//on récupère la liste des voitures
			checkCrudCarsCount(0);
		}
	}

	@Test
	public void testTxNativeInsertRollbackCrudSelectRollback() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar();
			nativeInsertCar(car);
		}
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//on récupère la liste des voitures
			checkCrudCarsCount(0);
		}
	}

	@Test
	public void testTxCrudInsertRollbackNativeSelectRollback() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar();
			storeManager.getDataStore().create(car);
		}
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			checkNativeCarsCount(0);
		}
	}

	@Test
	public void testTxNativeInsertRollbackNativeSelectRollback() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar();
			nativeInsertCar(car);
		}
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			checkNativeCarsCount(0);
		}
	}

	@Test
	public void testTxNativeInsertCrudInsertCommit() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar();
			final Car car2 = createNewCar();
			nativeInsertCar(car2);
			storeManager.getDataStore().create(car);
			transaction.commit();
		}
	}

	@Test
	public void testTxCrudInsertTwoCommit() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
			try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
				final Car car = createNewCar();
				storeManager.getDataStore().create(car);
				transaction.commit();
				transaction.commit();
			}
		});
	}

	@Test
	public void testTxCrudInsertCommitCrudSelectRollback() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar();
			storeManager.getDataStore().create(car);
			transaction.commit();
		}
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//on récupère la liste des voitures
			checkCrudCarsCount(1);
		}
	}

	@Test
	public void testTxNativeInsertCommitCrudSelectRollback() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar();
			nativeInsertCar(car);
			transaction.commit();
		}
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//on récupère la liste des voitures
			checkCrudCarsCount(1);
		}
	}

	@Test
	public void testTxCrudInsertCommitNativeSelectRollback() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar();
			storeManager.getDataStore().create(car);
			transaction.commit();
		}
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//on récupère la liste des voitures
			checkNativeCarsCount(1);
		}
	}

	@Test
	public void testTxNativeInsertCommitNativeSelectRollback() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar();
			nativeInsertCar(car);
			transaction.commit();
		}
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//on récupère la liste des voitures
			checkNativeCarsCount(1);
		}
	}

	@Test
	public void testCrudInsertNoTx() {
		Assertions.assertThrows(NullPointerException.class, () -> {
			final Car car = createNewCar();
			storeManager.getDataStore().create(car);
		});
	}

	@Test
	public void testNativeInsertNoTx() {
		Assertions.assertThrows(NullPointerException.class, () -> {
			final Car car = createNewCar();
			nativeInsertCar(car);
		});
	}

	@Test
	public void testCrudSelectNoTx() {
		Assertions.assertThrows(NullPointerException.class, () -> checkCrudCarsCount(0));
	}

	@Test
	public void testNativeSelectNoTx() {
		Assertions.assertThrows(NullPointerException.class, () -> checkNativeCarsCount(0));
	}

	@Test
	public void testPerfCrudInsertCrudSelectRollback() {
		final long start = System.currentTimeMillis();
		int execCount = 0;
		while (System.currentTimeMillis() - start < 1000) {
			testTxCrudInsertCrudSelectRollback();
			execCount++;
		}
		final long time = System.currentTimeMillis() - start;
		System.out.println(execCount + " exec en 1s. moy=" + time * 1000 / execCount / 1000d + "ms");
	}

	@Test
	public void testPerfNativeInsertNativeSelectRollback() {
		final long start = System.currentTimeMillis();
		int execCount = 0;
		while (System.currentTimeMillis() - start < 1000) {
			testTxNativeInsertNativeSelectRollback();
			execCount++;
		}
		final long time = System.currentTimeMillis() - start;
		System.out.println(execCount + " exec en 1s. moy=" + time * 1000 / execCount / 1000d + "ms");
	}

	@Test
	public void testCrudCountCars() {
		try (VTransactionWritable tx = transactionManager.createCurrentTransaction()) {
			final long count = storeManager.getDataStore().count(dtDefinitionCar);
			//-----
			Assertions.assertEquals(9, count);
		}
	}

	@Test
	public void testTxCrudInsertDeleteCommit() {
		final Car car = createNewCar();
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car createdCar = storeManager.getDataStore().create(car);
			//Check cars count
			checkCrudCarsCount(1);
			storeManager.getDataStore().delete(createdCar.getUID());
			checkCrudCarsCount(1); //car is cacheable : list was'nt flush here
			transaction.commit();
		}
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			checkCrudCarsCount(0); //car is cacheable : must wait commit to see delete
		}
	}

	@Test
	public void testTxCrudInsertCommitCrudDeleteCommit() {
		final Car car = createNewCar();
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			storeManager.getDataStore().create(car);
			checkCrudCarsCount(1);
			transaction.commit();
		}
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			storeManager.getDataStore().delete(car.getUID());
			checkCrudCarsCount(0);
			transaction.commit();
		}
	}

	@Test
	public void testTxCrudLockCommit() {
		final Car car = createNewCar();
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car createdCar = storeManager.getDataStore().create(car);
			//Check cars count
			checkCrudCarsCount(1);
			storeManager.getDataStore().readOneForUpdate(createdCar.getUID());
			checkCrudCarsCount(1);
			transaction.commit();
		}
	}

	private void checkNativeCarsCount(final int deltaCount) {
		final DtList<Car> cars = nativeLoadCarList();
		Assertions.assertNotNull(cars);
		Assertions.assertEquals(carDataBase.size() + deltaCount, cars.size(), "Test du nombre de voiture");
	}

	private void checkCrudCarsCount(final int deltaCount) {
		final DtList<Car> cars = storeManager.getDataStore().find(dtDefinitionCar, null, DtListState.of(null));
		Assertions.assertNotNull(cars);
		Assertions.assertEquals(carDataBase.size() + deltaCount, cars.size(), "Test du nombre de voiture");
	}

	private static Car createNewCar() {
		final Car car = new Car();
		car.setId(null);
		car.setPrice(5600);
		car.setManufacturer("Peugeot");
		car.setModel("407");
		car.setYear(2014);
		car.setKilo(20000);
		car.motorType().setEnumValue(MotorTypeEnum.essence);
		car.setDescription("Vds 407 de test, 2014, 20000 kms, rouge, TBEG");
		return car;
	}
}
