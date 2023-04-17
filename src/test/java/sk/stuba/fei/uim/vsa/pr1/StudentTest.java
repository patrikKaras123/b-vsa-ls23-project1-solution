package sk.stuba.fei.uim.vsa.pr1;


import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.stuba.fei.uim.vsa.pr1.utils.TestData;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.lang.reflect.InvocationTargetException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static sk.stuba.fei.uim.vsa.pr1.utils.TestConstants.*;
import static sk.stuba.fei.uim.vsa.pr1.utils.TestUtils.*;

public class StudentTest {

    private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("vsa-project-1");
    private static final Logger log = LoggerFactory.getLogger(StudentTest.class);
    private static AbstractThesisService<Object, Object, Object> thesisService;
    private static Class<?> studentClass;
    private static String studentIdField;
    private static Connection db;

    @BeforeAll
    static void setup() throws SQLException, ClassNotFoundException {
        thesisService = (AbstractThesisService<Object, Object, Object>) getServiceClass();
        assertNotNull(thesisService);
        studentClass = getEntityClassFromService((Class<AbstractThesisService<?, ?, ?>>) thesisService.getClass(), 0);
        assertNotNull(studentClass);
        db = getDBConnection(DB, USERNAME, PASSWORD);
        assertNotNull(db);
        studentIdField = findIdFieldOfEntityClass(studentClass);
        assertNotNull(studentIdField);
    }

    @BeforeEach
    void deleteAfterEach() {
        EntityManager entityManager = emf.createEntityManager();
        entityManager.getTransaction().begin();

        entityManager.createQuery("DELETE FROM Assignment").executeUpdate();
        entityManager.createQuery("DELETE FROM Student").executeUpdate();
        entityManager.createQuery("DELETE FROM Teacher").executeUpdate();
        entityManager.getTransaction().commit();
    }

    @AfterAll
    static void cleaning() throws SQLException {
        thesisService.close();
        db.close();
    }

    @Test
    void ST01_shouldCreateStudent() {
        try {
            Object student = createStudent(thesisService, TestData.Student01.class);
            assertNotNull(student);
            assertInstanceOf(studentClass, student);
            testToHaveAnIdField(student, studentIdField);
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void ST02_shouldGetStudent() {
        try {
            Object student = createStudent(thesisService, TestData.Student01.class);
            assertNotNull(student);
            assertInstanceOf(studentClass, student);

            Long id = getEntityId(student, studentIdField);
            Object second = thesisService.getStudent(id);
            assertNotNull(student);
            assertInstanceOf(studentClass, student);
            assertEquals(id, getEntityId(second, studentIdField));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void ST03_shouldGetAllStudents() {
        try {
            Object student01 = createStudent(thesisService, TestData.Student01.class);
            assertNotNull(student01);
            assertInstanceOf(studentClass, student01);

            Object student02 = createStudent(thesisService, TestData.Student02.class);
            assertNotNull(student02);
            assertInstanceOf(studentClass, student02);

            List<?> students = thesisService.getStudents();
            assertNotNull(students);
            assertFalse(students.isEmpty());
            assertEquals(2, students.size());
            assertTrue(students.stream().allMatch(studentClass::isInstance));
            assertTrue(students.stream().anyMatch(s -> {
                try {
                    return Objects.equals(getEntityId(student01, studentIdField), getEntityId(s, studentIdField));
                } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException |
                        NoSuchFieldException e) {
                    throw new RuntimeException(e);
                }
            }));
            assertTrue(students.stream().anyMatch(s -> {
                try {
                    return Objects.equals(getEntityId(student02, studentIdField), getEntityId(s, studentIdField));
                } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException |
                        NoSuchFieldException e) {
                    throw new RuntimeException(e);
                }
            }));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void ST04_shouldUpdateStudent() {
        try {
            Object student = createStudent(thesisService, TestData.Student01.class);
            assertNotNull(student);
            assertInstanceOf(studentClass, student);

            Object finalStudent = student;
            List<String> stringFields = findField(student, String.class).stream()
                    .filter(f -> checkIfFieldHasSetter(finalStudent, f, String.class))
                    .collect(Collectors.toList());
            assertFalse(stringFields.isEmpty());
            String stringField = stringFields.get(new SecureRandom().nextInt(stringFields.size()));
            String randomSetStringForTesting = Base64.getEncoder().encodeToString((TestData.Student01.studyProgramme + new SecureRandom().nextInt(100)).getBytes());
            String originalStringValue = getFieldValue(student, stringField, String.class);

            student = setFieldValue(student, stringField, randomSetStringForTesting);
            student = thesisService.updateStudent(student);
            assertNotNull(student);
            assertEquals(randomSetStringForTesting, getFieldValue(student, stringField, String.class));
            student = thesisService.getStudent(TestData.Student01.aisId);
            assertNotNull(student);
            assertEquals(randomSetStringForTesting, getFieldValue(student, stringField, String.class));
            assertNotEquals(originalStringValue, getFieldValue(student, stringField, String.class));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void ST05_shouldDeleteStudent() {
        try {
            Object student = createStudent(thesisService, TestData.Student01.class);
            assertNotNull(student);
            assertInstanceOf(studentClass, student);

            Long id = getEntityId(student, studentIdField);
            Object removed = thesisService.deleteStudent(id);
            assertEquals(id, getEntityId(removed, studentIdField));
            Object deleteCheck = thesisService.getStudent(id);
            assertNull(deleteCheck);
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void ST06_emailShouldBeUnique() {
        try {
            Object student = createStudent(thesisService, TestData.Student01.class);
            assertNotNull(student);
            assertInstanceOf(studentClass, student);

            Object failed = thesisService.createStudent(
                    getTestClassFieldValues(TestData.Student02.class, "aisId", Long.class),
                    getTestClassFieldValues(TestData.Student02.class, "name", String.class),
                    getTestClassFieldValues(TestData.Student01.class, "email", String.class));
            assertNull(failed);
        } catch (Exception e) {
            fail(e);
        }
    }

}
