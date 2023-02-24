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
import static sk.stuba.fei.uim.vsa.pr1.utils.TestData.*;
import static sk.stuba.fei.uim.vsa.pr1.utils.TestUtils.*;

public class TeacherTest {

    private static final Logger log = LoggerFactory.getLogger(TeacherTest.class);

    private static AbstractThesisService<Object, Object, Object> thesisService;
    private static Class<?> teacherClass;
    private static String teacherIdField;
    private static Connection db;

    @BeforeAll
    static void setup() throws SQLException, ClassNotFoundException {
        thesisService = (AbstractThesisService<Object, Object, Object>) getServiceClass();
        assertNotNull(thesisService);
        teacherClass = getEntityClassFromService((Class<AbstractThesisService<?, ?, ?>>) thesisService.getClass(), 1);
        assertNotNull(teacherClass);
        db = getDBConnection(DB, USERNAME, PASSWORD);
        assertNotNull(db);
        teacherIdField = findIdFieldOfEntityClass(Arrays.asList(ID_FIELDS), teacherClass);
        assertNotNull(teacherIdField);
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
    void TE01_shouldCreateTeacher() throws NoSuchFieldException, IllegalAccessException {
        Object teacher = createTeacher(thesisService, Teacher01.class);
        assertNotNull(teacher);
        assertInstanceOf(teacherClass, teacher);
        testToHaveAnIdField(teacher, teacherIdField);
    }

    @Test
    void TE02_shouldGetTeacher() throws NoSuchFieldException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Object teacher = createTeacher(thesisService, Teacher01.class);
        assertNotNull(teacher);
        assertInstanceOf(teacherClass, teacher);

        Long id = getEntityId(teacher, teacherIdField);
        Object second = thesisService.getTeacher(id);
        assertNotNull(teacher);
        assertInstanceOf(teacherClass, teacher);
        assertEquals(id, getEntityId(second, teacherIdField));
    }

    @Test
    void TE03_shouldGetAllTeachers() throws NoSuchFieldException, IllegalAccessException {
        Object teacher01 = createTeacher(thesisService, Teacher01.class);
        assertNotNull(teacher01);
        assertInstanceOf(teacherClass, teacher01);

        Object teacher02 = createTeacher(thesisService, Teacher02.class);
        assertNotNull(teacher02);
        assertInstanceOf(teacherClass, teacher02);

        List<?> teachers = thesisService.getTeachers();
        assertNotNull(teachers);
        assertFalse(teachers.isEmpty());
        assertEquals(2, teachers.size());
        assertTrue(teachers.stream().allMatch(teacherClass::isInstance));
        assertTrue(teachers.stream().anyMatch(t -> {
            try {
                return Objects.equals(getEntityId(teacher01, teacherIdField), getEntityId(t, teacherIdField));
            } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException |
                     NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }));
        assertTrue(teachers.stream().anyMatch(t -> {
            try {
                return Objects.equals(getEntityId(teacher02, teacherIdField), getEntityId(t, teacherIdField));
            } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException |
                     NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }));
    }

    @Test
    void TE04_shouldUpdateTeacher() throws NoSuchFieldException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Object teacher = createTeacher(thesisService, Teacher01.class);
        assertNotNull(teacher);
        assertInstanceOf(teacherClass, teacher);

        List<String> stringFields = findField(teacher, String.class);
        assertFalse(stringFields.isEmpty());
        String stringField = stringFields.get(0);
        String randomSetStringForTesting = Base64.getEncoder().encodeToString((Teacher01.institute + new SecureRandom().nextInt(99)).getBytes());
        String originalStringValue = getFieldValue(teacher, stringField, String.class);

        teacher = setFieldValue(teacher, stringField, randomSetStringForTesting);
        teacher = thesisService.updateTeacher(teacher);
        assertNotNull(teacher);
        assertEquals(randomSetStringForTesting, getFieldValue(teacher, stringField, String.class));
        teacher = thesisService.getTeacher(Teacher01.aisId);
        assertNotNull(teacher);
        assertEquals(randomSetStringForTesting, getFieldValue(teacher, stringField, String.class));
        assertNotEquals(originalStringValue, getFieldValue(teacher, stringField, String.class));
    }

    @Test
    void TE05_shouldDeleteTeacher() throws NoSuchFieldException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Object teacher = createTeacher(thesisService, Teacher01.class);
        assertNotNull(teacher);
        assertInstanceOf(teacherClass, teacher);

        Long id = getEntityId(teacher, teacherIdField);
        Object removed = thesisService.deleteTeacher(id);
        assertEquals(id, getEntityId(removed, teacherIdField));
        Object deleteCheck = thesisService.getTeacher(id);
        assertNull(deleteCheck);
    }

    @Test
    void TE06_emailShouldBeUnique() throws NoSuchFieldException, IllegalAccessException {
        Object teacher = createTeacher(thesisService, Teacher01.class);
        assertNotNull(teacher);
        assertInstanceOf(teacherClass, teacher);

        try {
            Object failed = thesisService.createTeacher(
                    getTestClassFieldValues(Teacher02.class, "aisId", Long.class),
                    getTestClassFieldValues(Teacher02.class, "name", String.class),
                    getTestClassFieldValues(Teacher01.class, "email", String.class),
                    getTestClassFieldValues(Teacher02.class, "department", String.class));
            assertNull(failed);
        } catch (Exception e) {
            fail(e);
        }
    }


}
