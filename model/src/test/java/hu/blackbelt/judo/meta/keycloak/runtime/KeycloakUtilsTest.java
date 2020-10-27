package hu.blackbelt.judo.meta.keycloak.runtime;

import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.keycloak.*;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.util.BasicEList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static hu.blackbelt.judo.meta.keycloak.util.builder.KeycloakBuilders.*;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("OptionalGetWithoutIsPresent")
@Slf4j
public class KeycloakUtilsTest {

    private static final String TARGET_TEST_CLASSES = "target/test-classes";
    private static final Slf4jLog logger = new Slf4jLog(log);

    private KeycloakModel keycloakModel;
    private KeycloakUtils keycloakUtils;

    @BeforeEach
    public void setup() {
        keycloakModel = KeycloakModel.buildKeycloakModel()
                .name("TestKeycloakModel")
                .build();

        keycloakModel.addContent(
                newdatabaseChangeLogBuilder().build()
        );

        keycloakUtils = new KeycloakUtils(keycloakModel.getResourceSet());
    }

    private void saveKeycloakModel(final String testName) {
        try {
            keycloakModel.saveKeycloakModel(KeycloakModel.SaveArguments.keycloakSaveArgumentsBuilder()
                    .file(new File(TARGET_TEST_CLASSES, format("%s-keycloak.xml", testName)))
                    .build());
        } catch (IOException e) {
            logger.warn("Unable to save keycloak model");
        } catch (KeycloakModel.KeycloakValidationException e) {
            fail("Keycloak model is not valid", e);
        }
    }

    private void addContent(ChangeSet... changeSets) {
        if (changeSets == null || changeSets.length == 0)
            return;
        keycloakUtils.getDatabaseChangeLog().get().getChangeSet().addAll(asList(changeSets));
    }

    @Test
    public void testGetChangeSets() {
        final String CHANGE_SET_ID = "ChangeSetId";
        final String CHANGE_SET1_ID = "ChangeSet1Id";

        // ASSERTIONS - check optional empty
        assertFalse(keycloakUtils.getChangeSets().isPresent());
        assertFalse(keycloakUtils.getChangeSet(CHANGE_SET_ID).isPresent());

        final ChangeSet changeSet = newChangeSetBuilder()
                .withId(CHANGE_SET_ID)
                .withAuthor("testGetChangeSets")
                .build();
        final ChangeSet changeSet1 = newChangeSetBuilder()
                .withId(CHANGE_SET1_ID)
                .withAuthor("testGetChangeSets")
                .build();

        addContent(changeSet, changeSet1);

        saveKeycloakModel("testGetChangeSets");

        // ASSERTION - check getChangeSets
        assertEquals(
                new BasicEList<>(asList(changeSet, changeSet1)),
                keycloakUtils.getChangeSets()
                        .orElseThrow(() -> new RuntimeException("No ChangeSet found"))
        );

        // ASSERTIONS - check getChangeSet
        assertEquals(
                changeSet,
                keycloakUtils.getChangeSet(changeSet.getId())
                        .orElseThrow(() -> new RuntimeException(format("%s not found", changeSet.getId())))
        );
        assertEquals(
                changeSet1,
                keycloakUtils.getChangeSet(changeSet1.getId())
                        .orElseThrow(() -> new RuntimeException(format("%s not found", changeSet1.getId())))
        );
    }

    @Test
    public void testGetCreateTable() {
        final String CHANGE_SET_ID = "ChangeSetID";
        final String TEST_TABLE_NAME = "TestTable";
        final String TEST_TABLE1_NAME = "TestTable1";
        final String CREATE_TABLE_NAME = "CreateTableName";
        final String ID_NAME = "_id";

        // ASSERTIONS - check optional empty
        assertFalse(keycloakUtils.getCreateTables(CHANGE_SET_ID).isPresent());
        assertFalse(keycloakUtils.getCreateTable(CHANGE_SET_ID, CREATE_TABLE_NAME).isPresent());

        final ChangeSet changeSet = newChangeSetBuilder()
                .withId(CHANGE_SET_ID)
                .withAuthor("testGetCreateTable")
                .build();

        addContent(changeSet);

        // ASSERTIONS - check optional empty
        assertFalse(keycloakUtils.getCreateTables(CHANGE_SET_ID).isPresent());
        assertFalse(keycloakUtils.getCreateTable(CHANGE_SET_ID, CREATE_TABLE_NAME).isPresent());

        final CreateTable testTable = newCreateTableBuilder()
                .withTableName(TEST_TABLE_NAME)
                .withColumn(
                        newColumnBuilder()
                                .withName(ID_NAME)
                                .withType("ID")
                )
                .build();

        final CreateTable testTable1 = newCreateTableBuilder()
                .withTableName(TEST_TABLE1_NAME)
                .withColumn(
                        newColumnBuilder()
                                .withName(ID_NAME)
                                .withType("ID")
                )
                .build();

        changeSet.getCreateTable().addAll(asList(testTable, testTable1));

        addContent(changeSet);

        saveKeycloakModel("testGetCreateTable");

        // ASSERTION - check getCreateTables
        assertEquals(
                new BasicEList<>(asList(testTable, testTable1)),
                keycloakUtils.getCreateTables(changeSet.getId())
                        .orElseThrow(() -> new RuntimeException(format("%s ChangeSet not found", changeSet.getId())))
        );

        // ASSERTIONS - check getCreateTable
        assertEquals(
                testTable,
                keycloakUtils.getCreateTable(changeSet.getId(), testTable.getTableName())
                        .orElseThrow(() -> new RuntimeException(format("%s not found", testTable.getTableName())))
        );
        assertEquals(
                testTable1,
                keycloakUtils.getCreateTable(changeSet.getId(), testTable1.getTableName())
                        .orElseThrow(() -> new RuntimeException(format("%s not found", testTable1.getTableName())))
        );
    }

    @Test
    public void testGetColumns() {
        final String CHANGE_SET_ID = "ChangeSetID";
        final String TEST_TABLE_NAME = "TestTable";
        final String COLUMN_NAME = "column";
        final String ID_NAME = "_id";

        // ASSERTIONS - check optional empty
        assertFalse(keycloakUtils.getColumns(CHANGE_SET_ID, TEST_TABLE_NAME).isPresent());
        assertFalse(keycloakUtils.getColumn(CHANGE_SET_ID, TEST_TABLE_NAME, COLUMN_NAME).isPresent());

        final ChangeSet changeSet = newChangeSetBuilder()
                .withId(CHANGE_SET_ID)
                .withAuthor("testGetColumns")
                .build();

        // ASSERTIONS - check optional empty
        assertFalse(keycloakUtils.getColumns(CHANGE_SET_ID, TEST_TABLE_NAME).isPresent());
        assertFalse(keycloakUtils.getColumn(CHANGE_SET_ID, TEST_TABLE_NAME, COLUMN_NAME).isPresent());

        final Column id = newColumnBuilder()
                .withName(ID_NAME)
                .withType("ID")
                .build();
        final Column column = newColumnBuilder()
                .withName(COLUMN_NAME)
                .withType("UNKNOWN")
                .build();

        final CreateTable createTable = newCreateTableBuilder()
                .withTableName(TEST_TABLE_NAME)
                .withColumn(asList(id, column))
                .build();

        changeSet.getCreateTable().add(createTable);

        addContent(changeSet);

        saveKeycloakModel("testGetColumns");

        // ASSERTION - check getColumns
        assertEquals(
                new BasicEList<>(asList(id, column)),
                keycloakUtils.getColumns(changeSet.getId(), createTable.getTableName())
                        .orElseThrow(() -> new RuntimeException(format("%s, %s not found", changeSet.getId(), createTable.getTableName())))
        );

        // ASSERTIONS - check getColumn
        assertEquals(
                id,
                keycloakUtils.getColumn(changeSet.getId(), createTable.getTableName(), id.getName())
                        .orElseThrow(() -> new RuntimeException(format("%s not found", id.getName())))
        );
        assertEquals(
                column,
                keycloakUtils.getColumn(changeSet.getId(), createTable.getTableName(), column.getName())
                        .orElseThrow(() -> new RuntimeException(format("%s not found", column.getName())))
        );
    }

    @Test
    public void testGetAddPrimaryKeys() {
        // ASSERTIONS - check optional empty
        final String TEST_TABLE_NAME = "TestTable";
        final String TEST_TABLE1_NAME = "TestTable1";
        final String CHANGE_SET_ID = "ChangeSetID";
        final String ID_NAME = "_id";

        assertFalse(keycloakUtils.getAddPrimaryKeys(CHANGE_SET_ID, TEST_TABLE_NAME).isPresent());
        assertFalse(keycloakUtils.getAddPrimaryKey(CHANGE_SET_ID, TEST_TABLE_NAME, ID_NAME).isPresent());

        final AddPrimaryKey addPrimaryKey = newAddPrimaryKeyBuilder()
                .withTableName(TEST_TABLE_NAME)
                .withColumnNames(ID_NAME)
                .build();
        final AddPrimaryKey addPrimaryKey1 = newAddPrimaryKeyBuilder()
                .withTableName(TEST_TABLE1_NAME)
                .withColumnNames(ID_NAME)
                .build();

        final ChangeSet changeSet = newChangeSetBuilder()
                .withId(CHANGE_SET_ID)
                .withAuthor("testGetAddPrimaryKeys")
                .withAddPrimaryKey(addPrimaryKey1)
                .build();

        addContent(changeSet);

        // ASSERTIONS - check optional empty
        assertFalse(keycloakUtils.getAddPrimaryKeys(CHANGE_SET_ID, TEST_TABLE_NAME).isPresent());
        assertFalse(keycloakUtils.getAddPrimaryKey(CHANGE_SET_ID, TEST_TABLE_NAME, ID_NAME).isPresent());

        changeSet.getAddPrimaryKey().add(addPrimaryKey);

        addContent(changeSet);

        saveKeycloakModel("testGetAddPrimaryKeys");

        // ASSERTION - check getAddPrimaryKeys
        assertEquals(
                new BasicEList<>(asList(addPrimaryKey)),
                keycloakUtils.getAddPrimaryKeys(changeSet.getId(), TEST_TABLE_NAME)
                        .orElseThrow(() -> new RuntimeException(format("%s, %s not found", changeSet.getId(), TEST_TABLE_NAME)))
        );

        // ASSERTION - check getAddPrimaryKey
        assertEquals(
                addPrimaryKey,
                keycloakUtils.getAddPrimaryKey(changeSet.getId(), TEST_TABLE_NAME, addPrimaryKey.getColumnNames())
                        .orElseThrow(() -> new RuntimeException(format("%s, %s not found", changeSet.getId(), TEST_TABLE_NAME)))
        );
    }

    @Test
    public void testGetAddForeignKeyConstraints() {
        final String CHANGE_SET_ID = "ChangeSetID";
        final String TEST_TABLE1_NAME = "TestTable1";
        final String TEST_TABLE2_NAME = "TestTable2";
        final String ID_NAME = "_id";

        // ASSERTIONS - check optional empty
        assertFalse(keycloakUtils.getAddForeignKeyConstraints(
                CHANGE_SET_ID,
                TEST_TABLE1_NAME,
                TEST_TABLE2_NAME).isPresent());
        assertFalse(keycloakUtils.getAddForeignKeyConstraint(
                CHANGE_SET_ID,
                TEST_TABLE1_NAME,
                TEST_TABLE2_NAME,
                TEST_TABLE2_NAME).isPresent());

        final AddForeignKeyConstraint addForeignKeyConstraint = newAddForeignKeyConstraintBuilder()
                .withBaseTableName(TEST_TABLE1_NAME)
                .withBaseColumnNames("TT2_fk")
                .withConstraintName(TEST_TABLE2_NAME)
                .withReferencedTableName(TEST_TABLE2_NAME)
                .withReferencedColumnNames(ID_NAME)
                .build();

        final AddForeignKeyConstraint addForeignKeyConstraint1 = newAddForeignKeyConstraintBuilder()
                .withBaseTableName("Apple")
                .withBaseColumnNames("Pear_fk")
                .withConstraintName("Pear")
                .withReferencedTableName("Pear")
                .withReferencedColumnNames(ID_NAME)
                .build();

        final ChangeSet changeSet = newChangeSetBuilder()
                .withId(CHANGE_SET_ID)
                .withAuthor("testGetAddForeignKeyConstraints")
                .withAddForeignKeyConstraint(addForeignKeyConstraint1)
                .build();

        addContent(changeSet);

        // ASSERTIONS - check optional empty
        assertFalse(keycloakUtils.getAddForeignKeyConstraints(
                changeSet.getId(),
                TEST_TABLE1_NAME,
                TEST_TABLE2_NAME).isPresent());
        assertFalse(keycloakUtils.getAddForeignKeyConstraint(
                changeSet.getId(),
                TEST_TABLE1_NAME,
                TEST_TABLE2_NAME,
                TEST_TABLE2_NAME).isPresent());

        changeSet.getAddForeignKeyConstraint().add(addForeignKeyConstraint);

        addContent(changeSet);

        saveKeycloakModel("testGetAddForeignKeyConstraints");

        // ASSERTION - check getAddForeignKeyConstraints
        assertEquals(
                new BasicEList<>(asList(addForeignKeyConstraint)),
                keycloakUtils.getAddForeignKeyConstraints(
                        changeSet.getId(),
                        TEST_TABLE1_NAME,
                        TEST_TABLE2_NAME)
                        .orElseThrow(() -> new RuntimeException(format("cs: %s, base: %s, ref: %s foreign key constraint not found",
                                changeSet.getId(), TEST_TABLE1_NAME, TEST_TABLE2_NAME)))
        );

        // ASSERTION - check getAddForeignKeyConstraint
        assertEquals(
                addForeignKeyConstraint,
                keycloakUtils.getAddForeignKeyConstraint(
                        changeSet.getId(),
                        TEST_TABLE1_NAME,
                        TEST_TABLE2_NAME,
                        TEST_TABLE2_NAME)
                        .orElseThrow(() -> new RuntimeException(format("cs: %s, base: %s, ref: %s foreign key constraint not found",
                                changeSet.getId(), TEST_TABLE1_NAME, TEST_TABLE2_NAME)))
        );
    }

    @Test
    public void testGetAddNotNullConstraint() {
        final String CHANGE_SET_ID = "ChangeSetID";
        final String TEST_TABLE_NAME = "TestTable";
        final String TEST_TABLE1_NAME = "TestTable1";
        final String ID_NAME = "_id";

        // ASSERTIONS - check optional empty
        assertFalse(keycloakUtils.getAddNotNullConstraints(CHANGE_SET_ID, TEST_TABLE_NAME).isPresent());
        assertFalse(keycloakUtils.getAddNotNullConstraint(CHANGE_SET_ID, TEST_TABLE_NAME, ID_NAME).isPresent());

        final AddNotNullConstraint addNotNullConstraint = newAddNotNullConstraintBuilder()
                .withTableName(TEST_TABLE_NAME)
                .withColumnName(ID_NAME)
                .build();

        final AddNotNullConstraint addNotNullConstraint1 = newAddNotNullConstraintBuilder()
                .withTableName(TEST_TABLE1_NAME)
                .withColumnName(ID_NAME)
                .build();

        final ChangeSet changeSet = newChangeSetBuilder()
                .withId(CHANGE_SET_ID)
                .withAuthor("testGetAddNotNullConstraint")
                .withAddNotNullConstraint(addNotNullConstraint1)
                .build();

        addContent(changeSet);

        // ASSERTIONS - check optional empty
        assertFalse(keycloakUtils.getAddNotNullConstraints(changeSet.getId(), TEST_TABLE_NAME).isPresent());
        assertFalse(keycloakUtils.getAddNotNullConstraint(changeSet.getId(), TEST_TABLE_NAME, ID_NAME).isPresent());

        changeSet.getAddNotNullConstraint().add(addNotNullConstraint);

        addContent(changeSet);

        saveKeycloakModel("testGetAddNotNullConstraint");

        // ASSERTION - check getAddNotNullConstraints
        assertEquals(
                new BasicEList<AddNotNullConstraint>(asList(addNotNullConstraint)),
                keycloakUtils.getAddNotNullConstraints(changeSet.getId(), TEST_TABLE_NAME)
                        .orElseThrow(() -> new RuntimeException(format("cs %s, table %s not found", changeSet.getId(), TEST_TABLE_NAME)))
        );

        // ASSERTION - check getAddNotNullConstraint
        assertEquals(
                addNotNullConstraint,
                keycloakUtils.getAddNotNullConstraint(changeSet.getId(), TEST_TABLE_NAME, ID_NAME)
                        .orElseThrow(() -> new RuntimeException(format("cs %s, table %s not found", changeSet.getId(), TEST_TABLE_NAME)))
        );

    }

}
