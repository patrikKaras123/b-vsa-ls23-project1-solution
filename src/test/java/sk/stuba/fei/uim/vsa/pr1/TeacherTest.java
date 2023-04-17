package sk.stuba.fei.uim.vsa.pr1;

import static org.junit.jupiter.api.Assertions.*;
import static sk.stuba.fei.uim.vsa.pr1.utils.TestUtils.getTestClassFieldValues;

import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

import static sk.stuba.fei.uim.vsa.pr1.utils.TestConstants.*;
import static sk.stuba.fei.uim.vsa.pr1.utils.TestData.Teacher01;
import static sk.stuba.fei.uim.vsa.pr1.utils.TestData.Teacher02;
import static sk.stuba.fei.uim.vsa.pr1.utils.TestUtils.*;

public class TeacherTest {

    private static final Logger log = LoggerFactory.getLogger(TeacherTest.class);

    private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("vsa-project-1");
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
        teacherIdField = findIdFieldOfEntityClass(teacherClass);
        assertNotNull(teacherIdField);
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
    void TE01_shouldCreateTeacher() {
        try {
            Object teacher = createTeacher(thesisService, Teacher01.class);
            assertNotNull(teacher);
            assertInstanceOf(teacherClass, teacher);
            testToHaveAnIdField(teacher, teacherIdField);
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void TE02_shouldGetTeacher() {
        try {
            Object teacher = createTeacher(thesisService, Teacher01.class);
            assertNotNull(teacher);
            assertInstanceOf(teacherClass, teacher);

            Long id = getEntityId(teacher, teacherIdField);
            Object second = thesisService.getTeacher(id);
            assertNotNull(teacher);
            assertInstanceOf(teacherClass, teacher);
            assertEquals(id, getEntityId(second, teacherIdField));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void TE03_shouldGetAllTeachers() {
        try {
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
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void TE04_shouldUpdateTeacher() {
        try {
            Object teacher = createTeacher(thesisService, Teacher01.class);
            assertNotNull(teacher);
            assertInstanceOf(teacherClass, teacher);

            Object finalTeacher = teacher;
            List<String> stringFields = findField(teacher, String.class).stream()
                    .filter(f -> checkIfFieldHasSetter(finalTeacher, f, String.class))
                    .collect(Collectors.toList());
            assertFalse(stringFields.isEmpty());
            String stringField = stringFields.get(new SecureRandom().nextInt(stringFields.size()));
            String randomSetStringForTesting = Base64.getEncoder().encodeToString((Teacher01.institute + new SecureRandom().nextInt(100)).getBytes());
            String originalStringValue = getFieldValue(teacher, stringField, String.class);

            teacher = setFieldValue(teacher, stringField, randomSetStringForTesting);
            teacher = thesisService.updateTeacher(teacher);
            assertNotNull(teacher);
            assertEquals(randomSetStringForTesting, getFieldValue(teacher, stringField, String.class));
            teacher = thesisService.getTeacher(Teacher01.aisId);
            assertNotNull(teacher);
            assertEquals(randomSetStringForTesting, getFieldValue(teacher, stringField, String.class));
            assertNotEquals(originalStringValue, getFieldValue(teacher, stringField, String.class));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void TE05_shouldDeleteTeacher() {
        try {
            Object teacher = createTeacher(thesisService, Teacher01.class);
            assertNotNull(teacher);
            assertInstanceOf(teacherClass, teacher);

            Long id = getEntityId(teacher, teacherIdField);
            Object removed = thesisService.deleteTeacher(id);
            assertEquals(id, getEntityId(removed, teacherIdField));
            Object deleteCheck = thesisService.getTeacher(id);
            assertNull(deleteCheck);
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void TE06_emailShouldBeUnique() {
        try {
            Object teacher = createTeacher(thesisService, Teacher01.class);
            assertNotNull(teacher);
            assertInstanceOf(teacherClass, teacher);

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
