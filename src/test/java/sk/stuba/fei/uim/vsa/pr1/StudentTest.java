package sk.stuba.fei.uim.vsa.pr1;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static sk.stuba.fei.uim.vsa.pr1.utils.TestConstants.*;
import static sk.stuba.fei.uim.vsa.pr1.utils.TestData.Student01;
import static sk.stuba.fei.uim.vsa.pr1.utils.TestData.Student02;
import static sk.stuba.fei.uim.vsa.pr1.utils.TestUtils.*;

public class StudentTest {

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
        studentIdField = findIdFieldOfEntityClass(Arrays.asList(ID_FIELDS), studentClass);
        assertNotNull(studentIdField);
    }

    @BeforeEach
    void before() {
        clearDB(db);
    }

    @AfterAll
    static void cleaning() throws SQLException {
        thesisService.close();
        db.close();
    }

    @Test
    void ST01_shouldCreateStudent() throws NoSuchFieldException, IllegalAccessException {
        Object student = createStudent(thesisService, Student01.class);
        assertNotNull(student);
        assertInstanceOf(studentClass, student);
        testToHaveAnIdField(student, studentIdField);
    }

    @Test
    void ST02_shouldGetStudent() throws NoSuchFieldException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Object student = createStudent(thesisService, Student01.class);
        assertNotNull(student);
        assertInstanceOf(studentClass, student);

        Long id = getEntityId(student, studentIdField);
        Object second = thesisService.getStudent(id);
        assertNotNull(student);
        assertInstanceOf(studentClass, student);
        assertEquals(id, getEntityId(second, studentIdField));
    }

    @Test
    void ST03_shouldGetAllStudents() throws NoSuchFieldException, IllegalAccessException {
        Object student01 = createStudent(thesisService, Student01.class);
        assertNotNull(student01);
        assertInstanceOf(studentClass, student01);

        Object student02 = createStudent(thesisService, Student02.class);
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
    }

    @Test
    void ST04_shouldUpdateStudent() throws NoSuchFieldException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Object student = createStudent(thesisService, Student01.class);
        assertNotNull(student);
        assertInstanceOf(studentClass, student);

        List<String> stringFields = findField(student, String.class);
        assertFalse(stringFields.isEmpty());
        String stringField = stringFields.get(0);
        String randomSetStringForTesting = Base64.getEncoder().encodeToString((Student01.studyProgramme + new SecureRandom().nextInt(99)).getBytes());
        String originalStringValue = getFieldValue(student, stringField, String.class);

        student = setFieldValue(student, stringField, randomSetStringForTesting);
        student = thesisService.updateStudent(student);
        assertNotNull(student);
        assertEquals(randomSetStringForTesting, getFieldValue(student, stringField, String.class));
        student = thesisService.getStudent(Student01.aisId);
        assertNotNull(student);
        assertEquals(randomSetStringForTesting, getFieldValue(student, stringField, String.class));
        assertNotEquals(originalStringValue, getFieldValue(student, stringField, String.class));
    }

    @Test
    void ST05_shouldDeleteStudent() throws NoSuchFieldException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Object student = createStudent(thesisService, Student01.class);
        assertNotNull(student);
        assertInstanceOf(studentClass, student);

        Long id = getEntityId(student, studentIdField);
        Object removed = thesisService.deleteStudent(id);
        assertEquals(id, getEntityId(removed, studentIdField));
        Object deleteCheck = thesisService.getStudent(id);
        assertNull(deleteCheck);
    }

    @Test
    void ST06_emailShouldBeUnique() throws NoSuchFieldException, IllegalAccessException {
        Object student = createStudent(thesisService, Student01.class);
        assertNotNull(student);
        assertInstanceOf(studentClass, student);

        try {
            Object failed = thesisService.createStudent(
                    getTestClassFieldValues(Student02.class, "aisId", Long.class),
                    getTestClassFieldValues(Student02.class, "name", String.class),
                    getTestClassFieldValues(Student01.class, "email", String.class));
            assertNull(failed);
        } catch (Exception e) {
            fail(e);
        }
    }

}
