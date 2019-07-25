Version history
===============

Running 2.0.1-SNAPSHOT
----------------------

more to come :)

Release 2.0.0 - 2019/05/16
----------------------
* [all] tests in junit 5 (use AbstractTestCaseJU5)
* __[all] Migrate from CONST_CASE to UpperCamelCase everywhere except in SQL databases__ a tool is provided to help migration
* __[core] all definitions are UpperCamelCase__
* [core] discovery : Added conditions to filter abstract classes
* [core] Removed deprecated Date DataType + remove all use of java.util.Date
* [core] Removed MessageTextBuilder (.of + simple)
* [core] Removed Tuple3, Tuples.Tuple2 renamed to Tuple
* [core] syntaxic sugar : use InjectorUtil instead of DIInjector
* __[core] replaced `@Named` by `@ParamValue`__
* __[core] Removed unused concept of named components__
* __[core] add yaml configuration with flippable features, xml configuration is now discouraged and will be removed in next version (even 2.x)__
* [core] refactored AppConfig -> Renamed in NodeConfig
* [commons] Fixed aggregate health status : 1 or more Red drive to Red
* [commons] Added ability to track non single thread processes
* [database] Added timeseries in database module
* __[database,dynamo,studio] remove hibernate support__
* [dynamo] Added check on association fix #129
* [dynamo] notNull is replaced by required in all ksp (task and entities)
* [dynamo] key is replaced by id in ksp
* [dynamo] URI is replaced by UID
* [dynamo] Made FacetedQueryResult serializable
* __[dynamo] Modified date pattern for ES query (breaking change)__
* [dynamo] localdate support in query
* [dynamo] ES: added optional param for embeded
* [dynamo] ES: Fix urn type, no normalizer
* [dynamo] ES: Fixed reindexall task when removed old elements
* [dynamo] From io.File to nio.Path
* [dynamo] DtListState.of
* [dynamo] Changed default charset ok Ksp loader from iso-8859-1 to utf-8
* [dynamo] Add FileInfoURI convert key for DataStore
* [dynamo] Upgraded ES version from 5.6.8 to 7.0.0, and Lucene from 6.6.0  to 8.0.0
* [dynamo] Fixed Search user DSL to escape bad syntax instead of VUser
* [dynamo] Added ElasticSearch plugin for v5.6
* [dynamo] Added clustering to CollectionsManager
* [dynamo] Fixed empty facets from Json with Vega
* __[dynamo] KSP Definition in CamelCase__
* [dynamo] remove brokerbatch and moved brokerNN in DataStore
* [dynamo] Fixed search by prefix with accents
* [dynamo] Simplified syntax to declare an association in ksp
* [dynamo] Fix domain metrics
* [dynamo] Replaced DAO access with DtListState instead of RowMax only
* [dynamo] Fix masterdatas to comply with with boot order : no more MasterDataInitializer -> replaced by a DefinitionProvider. Devs can use AbstractMasterDataDefinitionProvider
* __[es2.4] Remove ElasticSearch 2.4 plugin__
* [account] Allow comments in textauthentication plugin
* [account] Fixed account store plugin and null value
* [account] Support Id conversion from Account to Entity source
* __[account] Removed deprecated personna__
* [account] Removed definition prefix from authorization aspect
* [vega] fix Bug Content-Type sous JBoss
* [vega] Added support of '.' in exclude and include fieldname
* [vega] App version for swagger is a string
* [vega] Added Instant and LocalDate support to SwaggerApi
* [vega] UID Json encoding now send only key part, and use generics in order to resolved entity class
* [vega] Set attribute 'SessionExpired' true in case of session expiration
* [vega] Fixed iterator of uiListModifiable : remove change the expected count
* [vega] Inactive CacheControlFilter when Cache-Control header is already set
* [vega] Removed securityCheck of URL : may use @secured on WS
* [vega] Removed deprecated UiListState
* [studio] multiple files for sql init of staticmasterdatas
* [studio] dt objects can be splitted by feature
* [studio] fix dao import when dt_index = keyconcept
* [studio] created a SearchClient component dedicated to search access
* [studio] Drop if exists
* [studio] Removed sequences for non numeric PK
* [all] update dependencies log4j2 2.11.0 -> 2.11.2 ; cglib-nodep 3.2.6 -> 3.2.10 ; gson 2.8.2 -> 2.8.5 ; c3p0 0.9.5.2 -> 0.9.5.3 ; janino	3.0.8 -> 3.0.12 ; ehcache 2.10.4 -> 2.10.6 ; berkleydb sleepycat je 7.5.11 -> 18.3.12 ; rest-assured 3.0.7 -> 3.3.0 ; freemarker 2.3.23 -> 2.3.28 ; javax-mail 1.6.0 -> 1.6.2 ; h2 1.4.196 -> 1.4.199 ; struts2 2.5.16 -> 2.5.20 ; fr.opensagres.xdocreport.converter.odt.odfdom 2.0.1 -> 2.0.2 ; fr.opensagres.xdocreport.converter.docx.xwpf 2.0.1 -> 2.0.2 ; org.apache.poi 3.16 -> 4.0.1

Release 1.1.3 - 2019/03/21
----------------------
[Migration help](https://github.com/vertigo-io/vertigo/wiki/Vertigo-Migration-Guide#from-112-to-113)
* [Studio] unused attribute
* [Core] More specific temp dir for TempFiles
* [Database] add check for quoted bound param
* [Core] added conditions to filter abstract classes
* [Core] Possibility to have an optional parameter in the configuration

Release 1.1.2 - 2018/06/28
----------------------
[Migration help](https://github.com/vertigo-io/vertigo/wiki/Vertigo-Migration-Guide#from-111-to-112)
* [Vega] Moved VSecurityException to Account
* [Account] Added AuthorizationAspect : Warning reentrance not supported
* [Dynamo] Fixed defaut index data type when analyser is set
* [Dynamo] Added berkley database name in logs and fix some purge parameter
* [Account] Changed deprecated use className
* [Core] Fix for java 9+ compatibility in XMLModuleParams.
* [Dynamo] Fixing bug on index name not lowered when a prefix is used
* [vega] fix swaggerUi

Release 1.1.1 - 2018/04/27
----------------------
[Migration help](https://github.com/vertigo-io/vertigo/wiki/Vertigo-Migration-Guide#from-110-to-111)
* [Dynamo] Fixed IndexType parsing
* [Dynamo] Fixed collections facets
* [Dynamo] Fixed search.sortable fields with specific filter (lowercase, acsiifolding)
* [dynamo] additional params for batch tasks
* [Dynamo] Changed berkeley usage for compatibility 5.x to 7.x
* [Dynamo] Added maven repo for berkeleyDb
* [Dynamo] Fixed #115 use Set for dirtyElements
* [Vega] Fixed selectedFacet by label or by code
* [Vega] Fixed bad contextPath of spark
* [Vega] Fixed accept UiListState (deprecated)
* [Vega] Fixed swaggerUi
* [Vega] Added tests for search json serialisation
* [Vega] Updated swaggerUi version from 2.2.10 to 3.9.2
* [Vega] Fixed check of requests access
* [account] fix bug in textAuthenticationPlugin
* [Account] Fixed when AuthToken isn't a String
* [studio] fix security generator
* [Studio] Fixed TS mda
* [Studio] Fixed computed fields for properties and enum
* [Commons] Add specific base64 to base64url tests
* [core] fix discovery of proxies + added test
* [all] update dependencies 
 * com.h2database/h2 : 1.4.196 -> 1.4.197
 * org.codehaus.janino/janino : 3.0.7 -> 3.0.8
 * org.apache.logging.log4j/log4j-core : 2.9.1 -> 2.11.0
 * cglib/cglib-nodep : 3.2.5 -> 3.2.6
 * org.hibernate/hibernate-core : 5.2.11.Final -> 5.2.15.Final
 * org.elasticsearch/elasticsearch : 5.6.2 -> 5.6.8
 * org.elasticsearch/elasticsearch : 2.4.5 -> 2.4.6

Release 1.1.0 - 2017/12/07
----------------------
[Migration help](https://github.com/vertigo-io/vertigo/wiki/Vertigo-Migration-Guide#from-100-to-110)

* [core] stop failed started component + resilience on stop 
* [core] added proxies
* [core] Added ZoneIdProvider like LocaleProvider
* [core] Added defaultZoneId to managers.xml boot tag 
* [commons] healthchecks and metrics are handled by AnalyticsManager 
* [commons] daemons are traced by analytics
* [commons] metric provider are now registered with annotated methods
* [commons] change default port for log4j socket connector 4562 for log4j2 
* [commons] clean ThreadLocals on daemons to avoid non wanted behaviours 
* [commons] fix bug in processTracer tree 
* [database] created features' class
* [database] refac sql : named params are now supported
* [database] [dynamo] moved vendors 
* [database] added sqlMapper 
* [dynamo] Update ElasticSearch to 5.6, and Lucene to 6.6.0 
* [dynamo] Fixed lowercase dtDefinition when index is per type 
* [dynamo] added plugin for ES 2.4
* [dynamo] Removed highlights in clustered result 
* [dynamo] refactoring domain (multiple + valueobjects)
* [dynamo] replaced ZonedDateTime by Instant
* [dynamo, studio] static masterdata are accessed by an enum (via a dedicated accessor)
* [dynamo] Added multi selectable facet support
* [dynamo] Fixed SearchManager for multiple SearchIndexDef on same KeyConcept
* [dynamo] added support of list of primitives in taskengines
* [dynamo] switch to accessor for accessing fks on entities 
* [dynamo]  FsFullFileStorePlugin uses a store path with year, month, day
* [account] Renamed IdentityManager to AccountManager
* [account] Renamed AccountProvider to IdentityProvider 
* [vega] remove serialization of computed fields 
* [vega] added test for embeded entities in post payload
* [studio] added sql generation for masterdata 
* [studio] Updated Typescript generation for Focus4 (Node, Entity and masterData)
* [studio] Task with one input of Data-Object are DAO instead of PAO
* [studio] Changed sequence generator name in JPA annotation to be unique 
* [all] Added healhchecks on multiple components
* [all] Code cleaning, refactoring and documenting 
* [all] Migrated Log4j to Log4j2
* [all] Updated versions (gson, hibernate, junit) : gson 2.8.1 to 2.8.2, hibernate 5.2.10 to 5.2.11, junit 5.0.0-M4 to 5.0.1
* __[all] Execution error with jdk 1.8_51 should use more recent version__



Release 1.0.0 - 2017/07/07
----------------------
[Migration help](https://github.com/vertigo-io/vertigo/wiki/Vertigo-Migration-Guide#from-094-to-100)

__In Bold__ : Potential compatibility problems 

* [all] Code cleaning, refactoring and documenting 
*	[all] Added Dependency-check maven plugin
*	[vertigo] Builder Refactoring (General use is now : ObjectToBuild.builder() )
* [core] refactoring
* [core] silent => verbose (silently mode by default)
* [core] Params.of
*	[core] Introduced Proxy (+refactoring componentConfigBuilder)
* [core] Better exception if a looking for class don't extends the class
* [core] removed withApi(boolean) on moduleConfigBuilder
*	[core] replaced long by Instant (start date of app)
*	[core] refactoring MessageText (+ deprecated old api for migration ease)
*	[core] rem describable
* [core] first impl of a NodeManager (still a work in progress)
* [core] refactored LifeCycle
*	[core] components are now definition providers
*	[core] move classes from lang to components and locale
*	[core] definitionProvider +simple in features
* [account] introduced new module to handle security (authentication, authorization...)  
* [account] introduced a multi dimensions protection of entities (first impl ie still a work in progress)
*	[account] Added grants and overrides on advanced security
*	[account] Added support to security enum dimension
*	[account] Added support of tree security dimensions
*	[account] Splited UserSession to simpler UserSession and PersonaUserSession
*	[account] Added Store Realms Authentification and Identity
*	[account] Used salted-PBKDF2 hash algo for password instead of salted-SHA256
* [commons] replaced Listener by Consumer (Java8 style) (EventBus)
*	[commons] Added RedisCachePlugin
*	[commons] refactored eventbusManager
*	[commons] introduced CacheDefinition, EventBusSubscriptionDefinition, DaemonDefinition
*	[commons] introduced @EventSubscribed to register to a event on a component
*	[commons] introduced @DaemonScheduled to register a daemon on a component
*	[commons] Added VTransactionManager as default feature
* [commons] Refactored AnalyticsTracer : ever logs, no stack
* [commons] eventbus and daemon are now registered with annotations
* [database] introduced new module for handling databases (especially sql)
*	[database] refac Sql (use java type instead of DataType)
* [database] added tests for all databases (Oracle, Postgre, H2, SQLServer) 
* [database] added tests for batchs
* [database] insertions in Oracle databases now uses generated keys (no more callableStatements)
* [database] Hsql-> HSql
*	[database] added Java 8 LocalDate
* [database] added test on sql / blob --- java / dataStream
*	[database] sql -> functional style
*	[database] refac generatedKeys
*	[database] Declared ZonedDateTime and LocalDate as primitive types
* [dynamo] removed dynamic behaviour on DtDefinition
*	[dynamo] Made search highlight optional and desactivated by default
*	[dynamo] Show fluent sorting with generics
*	[dynamo] Added markAsDirty one uri for common usage case
* [dynamo] added convenient method .of to build DtList (DtList.of(dt1,dt2,...)
*	[dynamo] added a filter method on VCollectors
*	[dynamo] only true and false for a boolean property
* [dynamo] replaced with by add in FacetedQueryResultBuilder
*	[dynamo] introduced VAccessor concept
* [dynamo] refac CollectionsManager (replaced by java8 equivalents)
*	[dynamo] used Criterions instead Of CollectionsManager
* [dynamo] Renamed FacetedQueryResultBuilder to FacetedQueryResultMerger
* [dynamo] Added sort order support to range facet definition
* [dyanmo] moved criteria from dynamo/store to dynamo/criteria
* [dynamo] _Removed elasticSearch1_7 plugin_
*	[dynamo] removed DtListRangeFilter
*	[dynamo] replaced filterByRange by Criterion.isBetween
*	[dynamo] replaced Collections.filterValue by Criterions.isEqualTo
*	[dynamo] replaced CollectionsManager by Criterions
*	[dynamo] remove SqlCallableStatement and out parameters in sql
*	[dynamo] created object are returned (FileStore, DataStore)
*	[dynamo] lucene index plugins are stored in a map in a fix cache context
*	[dynamo] move database in new module, move transaction in commons
*	[dynamo] added Java 8 LocalDate
* [dynamo] moved parser fom dynamo to database (bound statement)
*	[dynamo] Oracle Unit Tests
* [dynamo] Removed HSQL from tests / used H2 instead
*	[dynamo] Added LocalDate and ZonedDateTime support ot FormatterDate
* [dynamo] Use lambda for comparator
*	[vega] Fixed #88
*	[vega] Added WebServiceDefinitionProvider using ClassSelector
*	[vega] Deprectaed UiListState by DtListState
* [vega] fix for #89
* [vega] added simple healthcheck webservice
* [vega] Fixed #92
*	[vega] Fixed facets term count and streamed it
* [vega] Added json empty property deserialized as null (and add tests)
*	[vega] Added json converter for LocalDate and ZonedDateTime
*	[vega] Added some ZonedDateTime tests
*	[vega] fix validator with server state
* [studio] move in vertigo repo
*	[studio] refactored freemarker
*	[studio] refactored all models (models, source objects and templates are completely independants)
*	[studio] Fixed JPA annotations for Hibernate and its "special" sequences
*	[studio] Added TS generator
* [persona] deprecated module (use accoount for new projects)
*	[tempo] _remove module_ (use vertigo-mail and vertigo-orchestra extensions)

*	[all] Updated dependencies versions  …
 org.codehaus.janino janino 2.7.8 -> 3.0.7
 net.sf.ehcache ehcache 2.10.3 -> 2.10.4
 org.slf4j slf4j-api 1.7.22 -> 1.7.25
 org.slf4j slf4j-simple 1.7.21 -> 1.7.25
 com.google.code.gson gson 2.8.0 -> 2.8.1
 com.h2database h2 1.4.193 -> 1.4.196
 cglib cglib-nodep 3.2.4 -> 3.2.5
 org.reflections reflections 0.9.10 -> 0.9.11
 org.hibernate hibernate-core 5.2.6.Final -> 5.2.10.Final
 org.hibernate hibernate-entitymanager 5.2.6.Final -> 5.2.10.Final
 org.junit 5.0.0-M2 -> 5.0.0-M4
 com.sparkjava spark-core 2.5.5 -> 2.6.0
 io.rest-assured rest-assured 3.0.1 -> 3.0.3
* [all] added an ant target to replace old quarto's ksp by a DefinitionProvider



Release 0.9.4a - 2017/03/15
----------------------
__This release is limited to Studio only__
* [Studio] Fix Studio issue 

Release 0.9.4 - 2017/03/13
----------------------
[Migration help](https://github.com/vertigo-io/vertigo/wiki/Vertigo-Migration-Guide#from-093-to-094)

__In Bold__ : Potential compatibility problems 

__Warning : You must use 0.9.4a version of Studio module !!__
* [All] Code cleaning, refactoring and documenting (and Stream java8, Optionnal, Methods refs, ...)
* [All] Always use WrappedException (wrap & unwrap), and params order changed
* [All] Moved dsl classes from core to dynamo
* __[Core] Renamed Injector to DIInjector__
* [Core] ComponentConfigBuilder is no more generic
* [Core] Support features in managers.xml
* [Core] Component discovery in features
* [Core] Changed boot config managment (features, added plugins, ...) redundancy__
* [Core] Fixed #80 : We check file exists and canRead before accept this plugin
* [Core] Updated cglib » cglib-nodep from 3.2.2 to 3.2.4
* [Core] Added notepad++ coloration config
* [Core] Changed tests Junit4 Assert to JUnit5 Assertions
* [Core] Simple ParamManager API
* [Commons] Refactor AnalyticsManager, added some plugins to log, aggregate and centralize measures 
* [Commons] Updated minor slf4j-api from 1.7.21 to 1.7.22
* [Dynamo] Adding Collectors in order to create DtList from Java 8 stream
* [Dynamo] Fixed #79 Now fragments loaded by annotations correctly have a FK to the linked entity
* [Dynamo] Closed Jsonbuilder in Search codec
* __[Dynamo] Renamed read to readOne and readNullable__
* __[Dynamo] Renamed loadList to findAll__
* [Dynamo] Updated minor elasticsearch from 1.7.5 to 1.7.6
* [Dynamo] Updated versions of hsqldb 2.3.3 to 2.3.4; lucene 5.5.0 to 5.5.2; elasticsearch 2.3.5 to 2.4.4
* [Dynamo] Updated version of hibernate 5.1.0 to 5.2.6 (break StreamDataType for file store)
* [Dynamo] Renamed H2Database.java to H2DataBase.java
* [Dynamo] Fixed create domain for DTs and Fragments
* [Dynamo] Changed BerkleyDb cleaner config (1000 elements max every minute, cleaner_min_ustilization : 90, cleaner_min_file_utilization:50)
* [Dynamo] Updated BerkleyDb version to 5.0.84
* [Dynamo] Better spaces whereInPreProcessor detection
* [Dynamo] Better search for autocomplete : correct word order is more pertinent
* [Dynamo] Fixed reloadAndMerge when fragment have ui fields
* [Dynamo] Refactored Data access (database dialect moved from StoreManager to DataBaseManager)
* [Dynamo] Changed reindexer task log level to debug, if nothing was updated
* [Dynamo] Added new Criterions class, should replace previous ListFilter and Criteria
* [Dynamo] Fixed ReindexTask to use KeyConcept URI instead of Dt Index which could be unpersistent
* [Dynamo] A DtList can't be optional (especially in Tasks)
* __[Persona] Replaced Security DTD by XSD__
* [Persona] Added Dsl parser for security rule (usefull for next version of security managment)
* [Vega] Fixed #77 (WS routes with numbers)
* __[Vega/Struts2] Merged similarity between Struts2 and Vega, now Struts2 uses Vega to limit 
* __[Vega] Renamed classes prefixed by RestXXX to VegaXXX__
* [Vega] Renamed UiObject getStringValue to getInputValue
* [Vega] Added searchApiVersion param to VegaFeatures (usefull for tests)
* [Vega] Fixed swagger custom url param
* [Vega] Fixed ResponseWrapper for XOR mode Stream or Writer
* [Vega] Made AbstractHttpServletResponseWrapper autocloseable
* [Vega] Updated version gson 2.7 to 2.8.0; spark-core 2.5 to 2.5.4; com.jayway.restassured rest-assured 2.8.0 to io.rest-assured rest-assured 3.0.1
* [Vega] Fixed #76 (Optional inner body param cannot be a DtObject)
* [Vega] Updated version of spark java from 2.5.4 to 2.5.5
* [Vega] Added originCORSFilter params in VegaFeatures
* [Vega] Fixed swaggerUi when using apiPrefix
* [Vega] Updated SwaggerUi to v2.2.8


Release 0.9.3 - 2016/10/11
----------------------
[Migration help](https://github.com/vertigo-io/vertigo/wiki/Vertigo-Migration-Guide#from-092-to-093)

__In Bold__ : Potential compatibility problems 
* __[All] Updated to JDK 8__
* [All] Code cleaning, refactoring and documenting
* [All] Use Lambda when it's possible
* __[Core] Added aspect on plugins + checked unmanaged aspect__
* [Core] Added fluent assertion when( ... ).check(...)
* [Core] Better message for DSL Solver unknown definitions error
* [Core] Clearer message in case of unresolved names
* [Commons] Refactored base64 codec to use jdk
* __[Dynamo] Split DtObject as Entity (persistent) and simple DtObject (non persistent)_
*  __[Dynamo] Added fragment of entity__
* [Dynamo] Added support to unmapped sort field
* [Dynamo] Fixed #60. Use DtListState maxRows and skipRows when clustering result. (limited to 100 elements per group)
* [Dynamo] Fixed ElasticSearch must have a dataType for simple properties type (String, long, BigDecimal,...)
* [Dynamo] Fixed search indexType with optional analyzer
* [Dynamo] Refactor DSL parser (Ksp loader)
* [Dynamo] Removed property about persistence from dtDefinition
* [Dynamo] StoreManager only deals with entities
* __[Dynamo] Added maven module to support ElasticSearch 1.7__
* __[Dynamo] Updated ElasticSearch to 2.3.5 and Lucene to 5.5.0__
* __[Dynamo] Updated FacetedQueryResult JsonSerializer to Focus v3__
* [Dynamo] Added FK to Entity in Fragments
* [Dynamo] Added sort of elements inner a search cluster. Fix #73
* [Dynamo] Fix #73 for ElasticSearch 1.7
* [Dynamo] Fixed #69, now included in SerializerV4
* [Dynamo] Fixed DslListFilter for range query starting by *
* [Dynamo] Fixed sqlexception.getSQLState when it's null
* [Dynamo] Fixed unique constrainte violation on H2 database
* [Dynamo] Made reindex of dirty elements every 1s instead of 5s
* [Dynamo] Renamed stereotype Data to ValueObject (always default sterotype)
* [Dynamo] Updated DataStore and DAO to load and update Fragments
* [Dynamo] DtListProcessor is now typed
* [Vega] __Fixed check selected facet by code instead of label__
* [Vega] Fixed serialization of FacetedQueryResult, removed facet and group if 0 elements (for emptied scope added as facets)
* [Vega] Updated FacetedQueryResultJson as discuss in #69
* [Vega] __Updated spark from 1.1.1 to 2.5 (jdk8/lambda)__
* [Dynamo] __Moved ExportManagerImpl to quarto__



Release 0.9.2 - 2016/06/28
----------------------
[Migration help](https://github.com/vertigo-io/vertigo/wiki/Vertigo-Migration-Guide#from-091-to-092)

__In Bold__ : Potential compatibility problems 
* [All] Code cleaning, refactoring and documenting
* [All] Updated 3rd party libs versions (hsqldb, rest assured, gson, janino, ehcache, cglib, mail, slf4j, hibernate, dom4j, jersey, jedis, poi, openoffice)
* __[Core] Aligned vertigo Option api to JDK api__ (`isPresent`, `ofNullable`, `orElse`, `of`)
* [Core] Fixed #56
* [Core] Splitted App and autoCloseable (should use new AutoCloseableApp in unit tests
* __[Dynamo] Renamed DAOBroker to DAO__
* [Dynamo] Fixed #57 in DslListFilterBuilder, and other pb
* [Dynamo] isNumber() on dataType
* __[Dynamo] Renamed CRUD methods__ (get => read , getList => findAll)
* [Dynamo] Made selectForUpdate overridable, and specialized selectForUpdate for Ms SqlServer
* [Dynamo] Fixed missing parenthesis in result with DslListFilterBuilder for some multi fields syntax
* __[Dynamo] Search FacetedQueryResult json v3 (#59)__ (use `searchApiVersion` param on `GoogleJsonEngine` component to select api version)
* [Dynamo] Fixed Berkeley remove too old elements
*  [Dynamo] Fixed HsqlDb rownum
* [Dynamo] Fixed bad cached list size, when rowmax was used
* __[Dynamo] Removed deprecated getConnectionProvider__
* [Dynamo] Renamed methods : `toUrn` to `urn`
* [Dynamo] Create URI public on `DtObjectUtil`
* [Dynamo] Preserve stacktrace if Search Exception
* [Dynamo] Fixed #63 - Errors in JpaDataStore
*  [Dynamo] Fixed #66 Retry indexation 5 times of dirty elements in case of error
* [Vega] Invalidate session when newly created session throws VSecurityException
* [Vega] Added token tests for anonymous users
* [Vega] Fixed Swagger Api for errors
* [Vega] Added test for String in body
* [Vega] Fixed preflight Options (misspelled header name)
* [Vega] Fixed #64 : concat hashcode to truncated name
* [Vega] Added global prefix for Spark route (param `apiPrefix` of `SparkJavaServletFilterWebServerPlugin`)
* [Vega] Added Highlight for search results in FacetedQueryResult json v4

Release 0.9.1 - 2016/02/05
----------------------
[Migration help](https://github.com/vertigo-io/vertigo/wiki/Vertigo-Migration-Guide#from-090-to-091)

__In Bold__ : Potential compatibility problems 
* __[All] Refactored all (injectable) components must extend Component__
* [All] Code cleaning, refactoring and documenting
* [All] Removed components inheritance from managers.xml (all components are component !!)
* [All] Updated FormatterException to a checkedException
* [Commons] Refactored Analytics, added AnalyticsTracker to replace module listeners
* [Commons] Refactored Daemon
* [Commons] Refactored cache to use CacheConfig instead of Modifiable interface  
* [Commons] Renamed EventManager to EventBusManager
* __[Core] Removed initClass on component in managers.xml__
* __[Core] Removed inheritance on module in managers.xml__
* [Core] Added ListBuilder
* [Core] Added init phase in the config files
* [Core] Changed msg for missing params
* [Core] Fixed #45 : Better message
* [Core] Fixed ClassPathResourceResolver should catch exception when can't resolve ressource
* [Core] Removed modifiable interface
* [Dynamo] Added VFile creator from URL
* [Dynamo] Added after and before commit functions for Transactional operations
* [Dynamo] Added count(collection) and clear(collection) on KvStore
* [Dynamo] Added deamon to remove old tempFiles
* [Dynamo] Added error message for Search syntax
* [Dynamo] Added escape char \ in DslListFilterBuilder
* [Dynamo] Added escape or remove reserved word, query operation (issue #49)
* [Dynamo] Added Search Facet order alpha or count
* [Dynamo] Added listFilter better detection of reserved keywords
* [Dynamo] Added support ][ and {} in DtListPatternFilterUtil for range
* [Dynamo] Added support to elasticsearch copyTo fields
* [Dynamo] Changed KvStore collection spliter to accept spaces
* [Dynamo] Changed ListFilterBuilderTest; Test and fix #47 and #46
* [Dynamo] Changed Tx resources to keep them ordered
* [Dynamo] Continued #38 : better hashcode on Criteria 
* [Dynamo] Created component interfaces StoreServices for Dao, Pao; Manager; SearchLoader
* [Dynamo] Fixed #47 : Bad ES Request for 1 character value
* [Dynamo] Fixed #50 : Bad ES request in case of Exact search query. Detect " " and ( ) and remove added ( )
* [Dynamo] Fixed #53 : FilterCriteriaBuilder withPrefix(DtFieldName,...) is incoherent with withPrefix(String,...)
* [Dynamo] Fixed Dsl syntax of multi fields
* [Dynamo] Fixed Facets type other than String (recreate index mandatory)
* [Dynamo] Fixed ListFilterBuilder when user query is one char
* [Dynamo] Fixed Search Loader when no data
* [Dynamo] Fixed reindex task when more than 1000 uri are dirty
* [Dynamo] Merged BerkeleyKvStore and DelayedBerkeleyKvStore
* [Dynamo] Refactored CollectionManager sort : use DtListState
* [Dynamo] Refactored lockForUpdate to readForUpdate, match main usage (as #48)
* [Dynamo] Removed Low Transactional resources priority
* [Dynamo] Renamed C"R"UD, so load=>read
* [Dynamo] Renamed PRIMARY_KEY by ID on DtDefinition (and pk to id)
* [Dynamo] Renamed fileStorePlugin remove to delete (CRUD : D => delete)
* [Dynamo] Renamed storeName to collection for KvStore and dataSpace for other store
* [Dynamo] Replaced MockConnectionProvider by a C3p0 impl
* [Dynamo] Search : Added dummy FacetDefinition when create dummy cluster
* [Dynamo] Updated elasticSearch to 1.7.5
* [Dynamo] changed count from long to int
* [Dynamo] refactored RAMLuceneindex
* [Studio] Fixed #32 : Add generated route js useable by Focus
* [Studio] Refactored lockForUpdate to readForUpdate, match main usage (as #48)
* [Studio] Renamed PRIMARY_KEY by ID on DtDefinition (and pk to id)
* __[Vega] Fixed Date / DateTime diff in Json ( supposed date if 00:00:00.000 )__
* [Vega] Added ClassPathWebServiceLoaderPlugin with org.reflections
* [Vega] Added UiSelectedFacets with toListFilters for facetted search api 
* [Vega] Added support optional parameters in SwaggerApi
* [Vega] Added support to ( ) in routes (WARN : don't work in swagger yet, don't use it)
* [Vega] Added toDtListState on UiListState
* [Vega] Added unwrap WrappedException before return httpcode
* [Vega] Default sort asc
* [Vega] Fixed #52
* [Vega] Fixed SwaggerAPI
* [Vega] Fixed contentType while preflight Cors request
* [Vega] Fixed requests not mark as succeeded
* [Vega] Refactored JsonSerializer for FacetedQueryResultJson v2      
* [Vega] Removed nullLast and ignorecase in DtListState
* [Vega] Renamed WebServiceIntrospector to WebServiceScanner


Release 0.9.0 - 2015/11/20
----------------------
[Migration help](https://github.com/vertigo-io/vertigo/wiki/Vertigo-Migration-Guide#from-083-to-090)

__In Bold__ : Potential compatibility problems 
* [All] Code cleaning and refactoring
* [All] Refactored RuntimeExceptions to VSystemException and WrappedException
* [Core] Detect prefix boot. to resolved env properties
* __[Core] Refactored initializers (not generics anymore, should use @Inject)__
* [Vega] Fixed routes order : /x/* are now after more specialized routes
*  __[Dynamo] Renamed DslXxxDefinition to DslXxx and removed deprecated RegExpListFilterBuilder__
* [Dynamo] Replaced RegExpListFilterBuilder by Dsl ones
* [Vega] Made VSecurityException runtime and using MessageText
* __[Vega] Added DtList deserializer support. WARN : no constraints check__
*  [Dynamo] Fixed delete order for TwoTablesDbFileStorePlugin
* [Core] Replaced componentSpace and definitionSpace accesses by Home.getApp().xx instead of Home.xx
* [Core] Changed syntax for params in config => '${xx}' instead of 'conf:xx' or '{xx}'
* [Core] Added method addAll with array on ListBuilder
* [Core] Added ResourceManager, LocaleManager and ConfigManager in the boot phase
* __[Core] Renamed ConfigManager to ParamManager__
* [core] Supported boot params from environment variables
* [Core] Removed EnvironmentManager 
* [core] Refactored DefinitionSpace and ComponentSpace as simple containers
* [Core] Better cyclic dependencies message
* [Dynamo] Splitted KVStoreManager from StoreManager
* [Dynamo] Added DataStoreName on DT and TK to allow multiple databases
* [Dynamo] Refactored Search : indices by conf for env, uses types for clustering documents
* [Dynamo] Fixed tests : DefinitionSpace no more injectable  
* [Dynamo] Fixed managers.xml, add locales on boot tag
* [Dynamo] Fixed search listFilterBuilder range 'to' to 'TO' ES keywords must be uppercase
* [Dynamo] Renamed cores to envIndex on ElasticSearch plugins
* [Dynamo] Changed elasticSearch version from 1.5.2 to 1.7.3 (can be downgraded)
* [All] Rollback to itext 2.1.7 (after they overrided 4.2.1 version on maven repo to scrap it)
* [Dynamo] Fix #38. Added hashCode on FilterCriteria
* [Tempo] Fixed mail starttls support (by @Jerom138)
* Merge pull request #36 from durandx/rename-tool-fix Fix rename issues for v0.8.1 & v0.8.2



Release 0.8.3 - 2015/10/08
----------------------
[Migration help](https://github.com/vertigo-io/vertigo/wiki/Vertigo-Migration-Guide#from-082-to-083)

__In Bold__ : Potential compatibility problems 
* [All] Code cleaning and refactoring
* __[All] Updated dependencies libs : gson 2.4, jedis 2.7.3, elasticsearch 1.5.2, poi 3.13__
* [Core] Fix #28 log warn for missing ressource in mono-langage app
* __[Dynamo] Renamed GroupCriteria mode intersec to intersect__
* __[Dynamo] BrokerNN is an helper, accessible from DAOBroker only, no more from StoreManager__
* __[Dynamo] Refactored brokerBatch api.__ (will be deprecated soon)
* __[Dynamo] Added DslListFilterBuilder (preconised) and renamed DefaultListFilterBuilder to RegExpListFilterBuilder (deprecated).__
* __[Dynamo] DslListFilterBuilder support MultiField, Range criteria. Better handle of query syntax.__
* [Dynamo] Renamed attribute notNull to required in DtField and TaskAttribute. (Ksp not impacted yet, only java object)
* [Dynamo] Some refactoring around JPA
* [Dynamo] Added transactional aspect in features
* [Dynamo] Fix #29 reindexAllTask missing some documents ~1/500
* [Vega] Fixed Facets order in json
* [Vega] Refactored Cors for FileUpload (handle Options requests)
* [Vega] Fixed FileUpload from Focus (dropzone)
* [Vega] Refactored JsonConverterHandler to JsonSerializer
* [Struts2] Fix #22
* [Studio] Fix #23. Cast TaskResult when Option.


Release 0.8.2 - 2015/09/10
----------------------
[Migration help](https://github.com/vertigo-io/vertigo/wiki/Vertigo-Migration-Guide#from-081-to-082)
__In Bold__ : Potential compatibility problems 
* [All] Lots of code cleaning and refactoring
* [All] Changed private methods that can be static to static
* [All] Refactored enties and dsl
* [All] Refactor java.lang.Timer to DeamonManager
* [All] Added Features to make NodeConfig simpler
* [Core] Added putNullable on MapBuilder
* [Core] Param was nullable in builder and checked when injected
* [Core] Removed injection JSR250 support. Use Activeable interface exclusively
* [Core] Added check id type of URI (no more String into numeric PK)
* [Core] Renamed Home into App in tests
* [Commons] Plugged Analytics on WebService, Task, Job and Search
* [Commons] Replaced LocalEventsPlugin by a simple processor
* [Commons] Added DaemonManager for managed daemon task
* __[Dynamo] Added AnalyticsManager when using SearchManager__
* __[Dynamo] Removed inout params (now, a param is 'in' xor 'out')__
* [Dynamo] Added support of multiple values criteria into DefaultListFilterBuilder
* [Dynamo] Assertion to avoid multi TaskEngine invocation
* [Dynamo] Merged jpa into HibernateConnectionProviderPlugin
* [Dynamo] changed FileStore's api to obtain the same api than dataStore
* [Dynamo] Kvdatastore Berkeley can manage Set as HashSet
* [Dynamo] Splitted SearchLoader SQL logic and chunk logic
* __[Dynamo] Renamed DefaultSearchLoader to AbstractSqlSearchLoader__
* __[Dynamo] Renamed Broker to DataStore__
* [Dynamo] Added DtObjectUtil.createUri by class and id
* [Dynamo] Fixed search reindex count
* [Studio] Removed Constants to generate a pretty code
* [Studio] Task has 0 or 1 result  
* [Studio] Fixed if report file already exist
* [Quarto] Error msg clearer
* [Struts2] Fixed stacking previous state of layout in tags (allow div tree, use it carefully :))
* [Persona] Added custom attributes on UserSession to register data from addons
* [Tempo] Added more tests
* [Tempo] Added log for error
* __[Vega] Replaced session attribute key : vertigo.rest.Session to vertigo.webservice.Session__ 
* __[Vega] Replaced SparkApplication by simpler VegaSparkFilter__ 
* [Vega] Fixed swagger for some path
* __[Vega] Renamed ApplicationServletContextListener to AppServletContextListener__, and removed Abstract parent
* __[Vega] Renamed ServletListener to AppServletListener__ 
* [Vega] Renamed HomeServletStarter to AppServletStarter 
* __[Vega] Renamed all RestXXX into WebServiceXXX__
* __[Vega] Renamed all WebServices related objects with WebService prefix__
* [Vega] No more override httpStatusCode if already set by WebService impl
* [Vega] Refactored RoutesRegisterPlugin to WebServerPlugin
* [Vega] Refactored JsonConverterHandler, __extract ServerState behaviour in a new optional plugin__
* [Vega] Renamed DtObjectExtended to a more generic ExtendedObject
* [Vega] Added option support in WS params
* [Vega] Added support to HTTP NotModified 304 response 
* [Vega] Renamed DefaultJsonReader to RequestJsonReader
* [Vega] Added URI json serialization
* [Vega] Added PATCH verb


Release 0.8.1 - 2015/07/31
----------------------
[Migration help](https://github.com/vertigo-io/vertigo/wiki/Vertigo-Migration-Guide#from-080-to-081)
* [All] Lots of code cleaning and refactoring
* [Core] Renamed io.vertigo.core.Home.App to io.vertigo.core.App
* [Core] Renamed io.vertigo.core.boot to io.vertigo.core.config
* [Core] Renamed io.vertigo.core.di to io.vertigo.core.component.di
* [Core] Refactor XMLNodeConfigBuilder 
* [Core] Refactor modules order/priority in managers.xml. Loading order are more predictible now : 
 1. boot module
 2. definitions
 3. other components module per module (as before)
* [Core] Moved LocalManager and ResourceManager from commons to core.
* [Core] Moved EnvironmentManager from dynamo to core
* [Dynamo] Fixed search case/accent sensitive while using wildcard
* [Dynamo] Fixed search securityFilter
* [Dynamo] Add default value to DefaultListFilterBuilder
* [Dynamo] return Future<Long> on reindexAll to allow join on reindex
* [Dynamo] CSVExporter should kept empty value as empty
* [Dynamo] Renamed ESSearchServicePlugin to ESNodeSearchServicePlugin
* [Dynamo] Fixed store object without index
* [Studio] Fix JS templates style, to fit with ESLint

Release 0.8.0 - 2015/07/02
----------------------
[Migration help](https://github.com/vertigo-io/vertigo/wiki/Vertigo-Migration-Guide#from-075-to-080)
* [All] Fixed some code style and some refactor to simpler code
* [All] Renamed prefix in Builder with ==> add
* [Core] split BootConfig from NodeConfig
* [Core] Refactoring DI
* [Core] Added better error message for missing definition while solving
* [Commons] Added daemonManager
* [Dynamo] Removed deprecated methods
* [Dynamo] Fixed FsTransactionResource, Temp files not deleted in some case
* [Dynamo] Added possibility to specify a search data type for index (in addition to the analyzer)
Use indexType : "analyzerName:dataTypeName" in domain declaration in KSP
* [Dynamo] Search return hightlights on result's fields only
* [Dynamo] Fixed search clustering create full list
* [Dynamo] Fixed and test #15
* [Dynamo] Renamed stereotype Subject to KeyConcept
* [Dynamo] Refactored and removed SearchIndexFieldNameResolver
* [Dynamo] Added EventsManager required by StoreManager (old PersistenceManager) 
* [Dynamo] Added RedisConnector
* [Dynamo] Complete refactor of SearchManager : integration in broker, KSP declaration of index definitions
* [Dynamo] Added FacetQueryResultMerger
* [Dynamo] Added DefaultSearchLoader and DefaultListFilterBuilder
* [Dynamo] Added ESSearchPlugin with TransportClient
* [Dynamo] Updated elasticSearch to 1.4.5
* [Dynamo] Fixed try with ressource of autonomous transaction
* [Dynamo, Studio] Renamed PersistenceManager to StoreManager
* [Dynamo, Studio] Renamed Broker to DataStore
* [Dynamo] Added Search facetValue sorted by count
* [Studio] Added optionnal dictionaryClassName param to specify dictionaryClassName, default to DtDefinitions (Fix #3)
* [Tempo] Added timeout in DistributedWorkerPlugins for connections from client to server
* [Tempo] Split scheduler and executor
* [Tempo] Added minute to jobManager.scheduleEveryDayAtHour
* [Quarto] Fixed UnitTests
* [Studio] Continue refactor to use stereotype KeyConcept in studio
* [Studio] Continue refactor to Search in generated DAO
* [Struts2] Fixed an error when escaping caracters in JSON
* [Struts2] Select multiple : listCssClass
* [Vega] TokenManager optionnal in JsonConverterHandler
*  [Vega] Fixed content-type name : now use '=' instead of ':'
* [Vega] Updated Swagger-ui to 2.1.0
* [Vega] Added support of FacetedQueryResult in WebService
* [Vega] Started expand jsonConverterHandlerPlugin into simplier JsonConverter per type


Release 0.7.5a - 2015/03/23
----------------------
* Fix of two 0.7.5 issues 


Release 0.7.5 - 2015/03/20
----------------------
[Migration help](https://github.com/vertigo-io/vertigo/wiki/Vertigo-Migration-Guide#from-074-to-075)
* [Core] Renamed KTransaction to VTransaction
* [Dynamo] Renamed KSecurityManager to VSecurityManager
* [Dynamo] Renamed KFile to VFile
* [Dynamo] Renamed KxFileInfo to VxFileInfo
* [Dynamo] Added DtFieldName to FilterCriteria API
* [Dynamo] Added DtListState for sorting, offset and limit query. Applied on search only
* [Dynamo] Some code style
* [Studio] Fixed Dto generated with DtListURIForXXAssociation
* [Struts2] Fixed FTL : css, checkbox
* [Quarto] XDocReportConverterPlugin : XDocReport based ODT to PDF converter plugin


Release 0.7.4 - 2015/03/12
----------------------
[Migration help](https://github.com/vertigo-io/vertigo/wiki/Vertigo-Migration-Guide#from-073-to-074)
* [All] Fixed some code style
* [All] Removed some deprecated : PersistenceManager.getBrokerConfiguration, PersistenceManager.getMasterDataConfiguration, UiMessageStack.hasErrorOnField, UiRequestUtil.getHttpServletRequest, UiRequestUtil.getHttpSession
* [Core] Aspects Order preserved
* [Core] Renamed vertigoimpl.commons to vertigo.commons.impl
* [Commons, Dynamo] Fixed index cacheability. For cache not serializable value. 
* [Dynamo] Added index settings check at startup
* [Dynamo] Fixed highlights on full_result field
* [Dynamo] Added Stereotype of DtObject : Data, MasterData or Subject
* [Dynamo] Refactored broker, cacheStore, SearchManager (introducing searchQueryBuilder)
* [Dynamo] Fixed searchQuery serializable
* [Dynamo] Added search clusering by facet, and FacetQuery is now optionnal, SearchQuery serializable
* [Dynamo] Fixed WhereIn with lowercase column name
*  [Dynamo] Fixed user exception detection/extraction
* [Dynamo] Updated ElasticSearch configFile
* [Struts2] Fixed readonly multi select
* [Struts2] Added assert uiMdList used with MasterData DtDefinition
* [Struts2] Renamed HomeServlerStarter to HomeServletStarter
* [Struts2] Added assert fieldName in camelCase
* [Struts2] Refactored formatter (no more FMT_DEFAULT)
* [Struts2][Vega] Added check of boot.applicationConfiguration
* [Struts2] Changed ftl usage as struts2.3.20
* [Struts2] Updated fielderror to add a onclick on error label to focus the field
* [Struts2] Updated to struts 2.3.20
* [Vega] Added encoder FacetedQueryResult to Json
* [Vega] Refactored RouteHandler to RestHandlerPlugin : make more configurable request processor stack
* [Vega] Added check response is commited when managed in controller
* [Studio] Made computed field always transient
* [Studio] Made generator target dir defined by plugins. Now you could generate : javagen, sqlgen, jsgen
* [Studio] Fixed hibernate sequence
* [Tempo] Added dead node detection of rest distributed
* [Quarto] Fixed #14

Release 0.7.3 - 2015/02/02
----------------------
[Migration help](https://github.com/vertigo-io/vertigo/wiki/Vertigo-Migration-Guide#from-072-to-073)
* [All] Refactored tests to made them more autonomous
* [All] Fixed some code style
* [All] Cleaned some dependencies
* [Dynamo] Split DtListProcessor to dispatch between java or index powered operations
* [Dynamo] Made Id fieldName free for FileStorePlugin
* [Dynamo] Fixed Facet filtering
* [Dynamo] Fixed #12 in ES connection settings
* [Dynamo] Fixed FsFileStorePlugin error when target directory already exists
* [Dynamo] Simpler WhereInPreProcessor using with regexp
* [Dynamo] Fixed sorting crash on null valued field
* [Dynamo] Split fileInfoURI and DtObjectURI
* [Dynamo] Renamed datastore plugin : ...persistence.oracle.OracleDataStorePlugin to ...persistence.datastore.oracle.OracleDataStorePlugin
* [Dynamo] Refactored DaoBroker, Broker and Datastore
* [Studio] Changed hibernate generationType SEQUENCE to AUTO
* [Studio] Fixed crebase.sql a semicolon ommited when no tablespace is defined
* [Quarto] Added timeout to converterTask
* [Quarto] Renamed plugins to be compliant with naming rules (MergerPluginDOCX to DOCXMergerPlugin, same for OpenOffice)
* [Commons] Renamed MapCachePlugin into MemoryCachePlugin
* [Struts2][Vega] Force to prefix servlet parameters with 'boot.' (for orphan detection)
* [Struts2] ValidationUserException accept DtFieldName
* [Struts2] Fixed fieldname in ftl for ajax component
* [Struts2] Removed ``<br>`` in radiomap, should use css instead
* [Struts2] Added getBidDecimal in UiObject for validators use
* [Vega] Refactored CorsAllowers : extended, renamed as Handler, made optional
* [Vega] Changed RateLimiting window from 15mn to 5min
* [Vega] Fixed paginator
* [Vega] Splitted fieldErrors and objectFieldErrors for easier managment client side
* [Core] Renamed XMLNodeConfigBuilder to XMLModulesBuilder
* [Core] Refactored NodeConfig, simpler external LogConfig
* [Core] Merged App and Home
* [Studio,Core] Refactored Env parameters
* [Core] Fixed aspect bug


Release 0.7.2 - 2014/12/16
----------------------
* [Dynamo] Forced Hibernate sequence handler to use DB sequence
* [core] Fixed aspect bug
* [Studio] Fixed js generator, sql generator, updates dectector
* [Studio] Added sql tablespaces (data and index) : use optional params on SqlGenerator tableSpaceData and tableSpaceIndex


Release 0.7.1 - 2014/12/09
----------------------
[Migration help](https://github.com/vertigo-io/vertigo/wiki/Vertigo-Migration-Guide#from-070-to-071)
* [Studio] Refactored : DomainGeneratorPlugin splited into more Plugins usages dependents (look at SqlGeneratorPlugin and JSGeneratorPlugin)
* [Studio] Fixed sql.ftl
* [Vega] UiListState in query instead of body
* [Dynamo] Fixed #10 and #11
* [Studio] ElasticSearch type mapping
* [Vega] Refacted JsonConverter and added DtList support in body
* [Vega] Added defaultValidator on Xxx&lt;O extends DtObject&gt;
* [Vega] fixed "already commited response" error
* [Vega] Added upload file test
* [Core] Refactored aspects : global scoped, simplier declarations with only one tag &lt;aspect&gt; with an attribute class extends Aspect


Release 0.7.0 - 2014/11/25
----------------------
[Migration help](https://github.com/vertigo-io/vertigo/wiki/Vertigo-Migration-Guide#from-06x-to-070)
A big version :)
* [All] Lots of code style fixes, tests units coverage, cleaning and refactoring
* [Core] Managers.xml syntax changed : Renamed &lt;modules&gt; to &lt;config&gt; (tag module doesn't change)
* [Core] Renamed two packages : io.vertigo.core.util, io.vertigo.core.lang
* [Core] Moved some classes : VUserException, DefinitionSpace, Engine
* [Core] Renamed ComponentSpaceConfigBuilder to NodeConfigBuilder
* [Core] Injector static instead of instance
* [Core] Renamed @PreFixed to @DefinitionPreFixed and Manager to Component
* [Commons] Added CacheConfig
* [Dynamo] Moved search public api from SearchServicePlugin to SearchManager
* [Dynamo] Added possibility to override FormatterDefault rendering
* [Dynamo] Added fluent style with ExportBuilder, PropertiesBuilder
* [Dynamo] DtField is a DtFieldName  
* [Dynamo] Refactored ElasticSearch plugins  
* [Dynamo] **Work** is migrated to tempo
* [Vega] Fixed swagger contextPath, nnerBody params, inline test POST services
* [Vega] Moved DtListDelta and UiListDelta
* [Vega] Updated export api
* [Vega] Renamed UiSecurityTokenManager to TokenManager
* [Vega] Added DtObjectExtended support
* [Vega] Added check to unused pathParams
* [Vega] Added AutoPaginator support
* [Vega] Added CorsAllowerFilter to component (must be in managers.xml). Param originCORSFilter can be set here
* [Studio] Fixed properties generator
* [Struts2] Fixed index dto with FK bind to MDL
* [Struts2] Fixed #9, added a unique uriList when list put in context, and no cache for list without uri
* [Struts2] Multiple select in simple_read
* [Struts2] Added autocompleter popinURL  
* [Struts2 Vega] Fixed file download Content-Disposition
* [Struts2] Changed layout table behaviour  : now can include div in order to declare a single cell

Release 0.6.2 - 2014/10/27
----------------------
* [Struts2] Fixed 0.6.1 broken xxx_read themes inheritance
* [Struts2] Some ftl's tags fixed and improvments (autocompleter popinURL, div in div, reMoved some \n)
* [Vega] Fixed swagger contextPath
* [Studio] Fixed resources.ftl
* Fixed some tests

Release 0.6.1 - 2014/10/17
----------------------
* [Vega] Fixed relative path support with SwaggerUI
* [Vega] Moved DtListDelta and UiListDelta
* [Dynamo] Added possibility to override FormatterDefault rendering
* [Struts2] Added VFreemarkerManager to better override of struts2 FLT (Added this to your struts.xml *&lt;constant name="struts.freemarker.manager.classname" value="io.vertigo.struts2.impl.views.freemarker.VFreemarkerManager" /&gt;*, configure it with *TemplatePath* param in *web.xml*) 
* [Struts2] Moved ftl files to /io/vertigo/struts2/ftl/template 
* [Struts2] Disabled required mark for checkbox
* [Struts2] Added Multiple select tag support
* [Struts2] Fixed index dto with FK bind to MDL
* [Struts2] Fixed autocompleter theme support
* [Studio] Fixed properties generator
* Some CodeStyle fixes

Release 0.6.0 - 2014/10/15
----------------------
* [Core] managers.xml xsd now checks params are declared before plugins
* [Vega] Added support to Swagger Api viewer (see http://swagger.io/)
* [Vega] Added HttpRequest and HttpResponse parameters support
* [Vega] Added Header param support
* [Vega] Fixed some bug with query parameters
* [Dynamo] Some refacts (first step). No major api modifications.
* [Dynamo] Some renamed (DatabaseManager => SqlDatabaseManager)
* [Dynamo] Swap search from Solr to ElasticSearch 
* [Dynamo] Added Batch support to DAOBroker
* [Struts2] Fixed struts2 checkboxlist.ftl 
* [Struts2] Added a ftl jar for overrided ui components ftl
* [Struts2] Added check page access in SecurityFilter 
* [Studio] Refact some generated code for **Focus** compatibility
* [Persona] Refact dependencies
* [Persona] Renamed securityLoaderplugin into securityResourceLoaderPlugin
* [Tempo] Added authentification support in JavaxSendMailPlugin
* [Commons] Fixed CurrentLocale can provide an unsupported Locale
* Some minor fixes

Release 0.5.2 - 2014/09/23
--------------------------
* Some pom updated for MavenCentral Release :)
* Fixed Vega fileUpload support (with Servlet 3.0 API)
* Fixed struts2 .ftl for readonly checkbox, select, radiomap
* Fixed UiList usage
* Renames somes const
* Fixed .properties charset (ISO-8859-1)

Release 0.5.1 - 2014/09/17
--------------------------
* Fixed #7 ConstraintBigDecimal 
* Added Vega DtListDelta support (compatibility with **Focus** project)

Release 0.5.0 - 2014/09/16
--------------------------
* Added Struts2 module
* Added Studio Sql generation 
* Added Vega fileUpload support
* Refact WorkManager Api(Future), Distributed
* Refact CollectionManager Api(DtListProcessor)
* Renamed io.vertigo.kernel to io.vertigo.core
* Fixed CacheManager clear conditions
* Fixed Vega #6 Changed mime type for json entity
* Fixed Vega multiPath params support
* Updated dependencies

Release 0.4.3 - 2014/08/06
--------------------------
* Fixed Vega #5 error mimeType and Json syntaxe
* Fixed Studio annotationNN package
* Fixed Vega autoPagination support
* Fixed tempo dependencies

Release 0.4.2 - 2014/08/01
--------------------------
* Refact vertigo-bundle : added tempo and reMoved studio and labs
* Added metadatas support in DtList
* Added JsDtDefinitionGenerato in vertigo-studio for **Focus** (HTML5-SPA) applications
* Added file encoding parameter of generated files in vertigo-studio
* Fixed file encoding template of loaders in vertigo-studio
* Added vertigo-tempo : Manage background operations and communications
* Merge collections.facet to collections
* Moved back Transactional annotations from stereotype package
* Renamed XxxStorePlugins as XxxDataStorePlugins
* Some refactoring by using List<Plugin> contructor injection
* Some minor internal renames


Release 0.4.1 - 2014/07/29
--------------------------
* Added Multi KvDataStore support
* Added List injection support in constructor
* Changed DateQueryParser use elacticSearch syntax instead of Solr ones
* Moved almost all annotations to a stereotype package
* Fixed missing PathPreFixed bug in Vega/rest component
* Fixed nullValue in facetByTerm


Release 0.4.0 - 2014/07/25
--------------------------
The most notable changes in Vertigo 0.4.0 over previous releases are:  
  * Changed ksp syntax to have a more similar json syntax.  Separator ';' is replaced by ','. Array {} is replaced by [] and  object notation () is replaced by {}
  * Changed declaration of resources is externalized. In this way, each module can declare its own resources (oom, ea, ksp, xml).     
  * Changed KDataType to DataType
  * Changed : Tasks must be processed using TaskManager and not WorkManager. (In fact each component must be able to process sync or async its own works) 
  * Added module labs, contains some usefull components such as mail   
  * Added module persona, built to manage all concepts around a user/persona  
  * Added module quarto, which contains two components : publisher (to create new publications by merging template and data) and converter (to convert a doc, docx into a pdf for example)   
  * Added distributed task management (by redis or http/rest)
  * Added Enterprise Architect support
  * Added vertigo-vega : RESTful WebService support for HTML5 applications
  * Fixed Issues #3 : added a new mandatory vertigo-studio parameter ```domain.dictionaryClassName``` 
  * To maintain compatibility Added : ```domain.dictionaryClassName=DtDefinitions``` in your studio-config.properties
  * Added check on unused components, params during injection  
  * Fixed Issues #2

Release 0.3.0 - 2014/03/13
--------------------------
The most notable changes in Vertigo 0.3.0 over previous releases are:
  * Added vertigo-studio
  * Added Jpa/Hibernate support
  * Added vertigo-dynamo
  * Added vertigo-ccc Command & Control Center
  * Refactor maven modules

Release 0.2.1 - 2014/02/26
--------------------------
  * Simplify Assertion api

Release 0.2.0 - 2014/02/26
--------------------------
The most notable changes in Vertigo 0.2.0 over previous releases are:
  * Refactor package and naming 
  * Added vertigo-commons tools, api and impl split
  * Added autocloseable

Release 0.1.0 - 2014/01/14
--------------------------
  * First release


