package sk.stuba.fei.uim.vsa.pr1;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static sk.stuba.fei.uim.vsa.pr1.utils.TestConstants.*;
import static sk.stuba.fei.uim.vsa.pr1.utils.TestData.*;
import static sk.stuba.fei.uim.vsa.pr1.utils.TestUtils.*;

public class ThesisTest {

    private static final Logger log = LoggerFactory.getLogger(ThesisTest.class);

    private static AbstractThesisService<Object, Object, Object> thesisService;
    private static Class<?> thesisClass;
    private static String thesisIdField;
    private static Class<?> teacherClass;
    private static String teacherIdField;
    private static Class<?> studentClass;
    private static String studentIdField;
    private static Connection db;

    @BeforeAll
    static void setup() throws SQLException, ClassNotFoundException {
        thesisService = (AbstractThesisService<Object, Object, Object>) getServiceClass();
        assertNotNull(thesisService);
        thesisClass = getEntityClassFromService((Class<AbstractThesisService<?, ?, ?>>) thesisService.getClass(), 2);
        assertNotNull(thesisClass);
        teacherClass = getEntityClassFromService((Class<AbstractThesisService<?, ?, ?>>) thesisService.getClass(), 1);
        assertNotNull(teacherClass);
        studentClass = getEntityClassFromService((Class<AbstractThesisService<?, ?, ?>>) thesisService.getClass(), 0);
        assertNotNull(studentClass);
        db = getDBConnection(DB, USERNAME, PASSWORD);
        assertNotNull(db);
        thesisIdField = findIdFieldOfEntityClass(thesisClass);
        assertNotNull(thesisIdField);
        teacherIdField = findIdFieldOfEntityClass(teacherClass);
        assertNotNull(teacherIdField);
        studentIdField = findIdFieldOfEntityClass(studentClass);
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
    void TH01_shouldCreateThesisAssignment() {
        try {
            Object teacher = createTeacher(thesisService, Teacher01.class);
            assertNotNull(teacher);
            assertInstanceOf(teacherClass, teacher);
            testToHaveAnIdField(teacher, teacherIdField);
            Long teacherId = getEntityId(teacher, teacherIdField);

            Object thesis = createThesis(thesisService, Thesis01.class, teacherId);
            assertNotNull(thesis);
            assertInstanceOf(thesisClass, thesis);
            testToHaveAnIdField(thesis, thesisIdField);

            List<String> teacherFields = findField(thesis, teacherClass);
            assertFalse(teacherFields.isEmpty());
            assertEquals(1, teacherFields.size());
            String teacherField = teacherFields.get(0);
            assertNotNull(getFieldValue(thesis, teacherField, teacherClass));
            assertEquals(teacherId, getEntityId(getFieldValue(thesis, teacherField, teacherClass), teacherIdField));

            Class[] dateClasses = new Class[]{Date.class, LocalDate.class, java.sql.Date.class, LocalDateTime.class, Calendar.class, GregorianCalendar.class, Timestamp.class};
            Map<Class, List<String>> dateFieldsOfClass = Arrays.stream(dateClasses)
                    .filter(clazz -> findField(thesis, clazz).size() == 2)
                    .collect(Collectors.toMap(Function.identity(), clazz -> (List<String>) findField(thesis, clazz)));
            assertNotNull(dateFieldsOfClass);
            assertFalse(dateFieldsOfClass.isEmpty());
            assertEquals(1, dateFieldsOfClass.size());
            List<String> dateFields = dateFieldsOfClass.entrySet().stream().findFirst().get().getValue();
            assertNotNull(dateFields);
            assertFalse(dateFields.isEmpty());
            assertEquals(2, dateFields.size());
            assertNotNull(getFieldValue(thesis, dateFields.get(0)));
            assertNotNull(getFieldValue(thesis, dateFields.get(1)));
            assertNotEquals(getFieldValue(thesis, dateFields.get(0)), getFieldValue(thesis, dateFields.get(1)));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void TH01_1_shouldNOTCreateThesisWithoutTeacher() {
        try {
            try {
                Object thesis = createThesis(thesisService, Thesis01.class, null);
                assertNull(thesis);
            } catch (IllegalArgumentException ex) {
                assertNotNull(ex);
                log.info("Successfully thrown exception " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
            }
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void TH02_shouldAssignThesisToStudent() {
        try {
            Object teacher = createTeacher(thesisService, Teacher01.class);
            assertNotNull(teacher);
            assertInstanceOf(teacherClass, teacher);
            testToHaveAnIdField(teacher, teacherIdField);

            Object thesis = createThesis(thesisService, Thesis01.class, getEntityId(teacher, teacherIdField));
            assertNotNull(thesis);
            assertInstanceOf(thesisClass, thesis);
            testToHaveAnIdField(thesis, thesisIdField);
            Long thesisId = getEntityId(thesis, thesisIdField);
            Object status = getFieldValue(thesis, "status");

            Object student = createStudent(thesisService, Student01.class);
            assertNotNull(student);
            assertInstanceOf(studentClass, student);
            testToHaveAnIdField(student, studentIdField);

            thesis = thesisService.assignThesis(thesisId, getEntityId(student, studentIdField));
            assertNotNull(thesis);
            thesis = thesisService.getThesis(thesisId);
            assertNotNull(thesis);
            assertInstanceOf(thesisClass, thesis);
            assertEquals(thesisId, getEntityId(thesis, thesisIdField));
            assertNotEquals(status, getFieldValue(thesis, "status"));

            List<String> studentFields = findField(thesis, studentClass);
            assertFalse(studentFields.isEmpty());
            assertEquals(1, studentFields.size());
            String studentField = studentFields.get(0);
            assertNotNull(getFieldValue(thesis, studentField, studentClass));
            assertEquals(getEntityId(student, studentIdField),
                    getEntityId(getFieldValue(thesis, studentField, studentClass), studentIdField));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void TH02_1_shouldAssignThesisToStudentWithAlreadyAssignedThesis() {
        try {
            Object teacher = createTeacher(thesisService, Teacher01.class);
            assertNotNull(teacher);
            assertInstanceOf(teacherClass, teacher);
            testToHaveAnIdField(teacher, teacherIdField);
            Long teacherId = getEntityId(teacher, teacherIdField);

            Object thesis = createThesis(thesisService, Thesis01.class, teacherId);
            assertNotNull(thesis);
            assertInstanceOf(thesisClass, thesis);
            testToHaveAnIdField(thesis, thesisIdField);
            Long thesisId = getEntityId(thesis, thesisIdField);
            Object status = getFieldValue(thesis, "status");

            Object student = createStudent(thesisService, Student01.class);
            assertNotNull(student);
            assertInstanceOf(studentClass, student);
            testToHaveAnIdField(student, studentIdField);
            Long studentId = getEntityId(student, studentIdField);

            // Assign first thesis
            thesis = thesisService.assignThesis(thesisId, studentId);
            assertNotNull(thesis);
            thesis = thesisService.getThesis(thesisId);
            assertNotNull(thesis);
            assertInstanceOf(thesisClass, thesis);
            assertEquals(thesisId, getEntityId(thesis, thesisIdField));
            assertNotEquals(status, getFieldValue(thesis, "status"));

            List<String> studentFields = findField(thesis, studentClass);
            assertFalse(studentFields.isEmpty());
            assertEquals(1, studentFields.size());
            String studentField = studentFields.get(0);
            assertNotNull(getFieldValue(thesis, studentField, studentClass));
            assertEquals(studentId,
                    getEntityId(getFieldValue(thesis, studentField, studentClass), studentIdField));

            // Assign second thesis
            Object thesis02 = createThesis(thesisService, Thesis02.class, teacherId);
            assertNotNull(thesis02);
            assertInstanceOf(thesisClass, thesis02);
            testToHaveAnIdField(thesis02, thesisIdField);
            Long thesis02Id = getEntityId(thesis02, thesisIdField);

            thesis02 = thesisService.assignThesis(thesis02Id, studentId);
            assertNotNull(thesis02);
            thesis02 = thesisService.getThesis(thesis02Id);
            assertNotNull(thesis02);
            assertInstanceOf(thesisClass, thesis02);
            assertEquals(thesis02Id, getEntityId(thesis02, thesisIdField));
            assertNotNull(getFieldValue(thesis02, studentField, studentClass));
            assertEquals(studentId,
                    getEntityId(getFieldValue(thesis02, studentField, studentClass), studentIdField));

            thesis = thesisService.getThesis(thesisId);
            assertNotNull(thesis);
            assertEquals(status, getFieldValue(thesis, "status"));
            assertNull(getFieldValue(thesis, studentField, studentClass));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void TH02_2_shouldNOTAssignAlreadyAssignedThesis() {
        try {
            Object teacher = createTeacher(thesisService, Teacher01.class);
            assertNotNull(teacher);
            assertInstanceOf(teacherClass, teacher);
            testToHaveAnIdField(teacher, teacherIdField);
            Long teacherId = getEntityId(teacher, teacherIdField);

            Object thesis = createThesis(thesisService, Thesis01.class, teacherId);
            assertNotNull(thesis);
            assertInstanceOf(thesisClass, thesis);
            testToHaveAnIdField(thesis, thesisIdField);
            Long thesisId = getEntityId(thesis, thesisIdField);
            Object status = getFieldValue(thesis, "status");

            Object student = createStudent(thesisService, Student01.class);
            assertNotNull(student);
            assertInstanceOf(studentClass, student);
            testToHaveAnIdField(student, studentIdField);
            Long studentId = getEntityId(student, studentIdField);

            // First thesis
            thesis = thesisService.assignThesis(thesisId, studentId);
            assertNotNull(thesis);
            thesis = thesisService.getThesis(thesisId);
            assertNotNull(thesis);
            assertInstanceOf(thesisClass, thesis);
            assertEquals(thesisId, getEntityId(thesis, thesisIdField));
            assertNotEquals(status, getFieldValue(thesis, "status"));

            List<String> studentFields = findField(thesis, studentClass);
            assertFalse(studentFields.isEmpty());
            assertEquals(1, studentFields.size());
            String studentField = studentFields.get(0);
            assertNotNull(getFieldValue(thesis, studentField, studentClass));
            assertEquals(studentId,
                    getEntityId(getFieldValue(thesis, studentField, studentClass), studentIdField));

            // Second thesis
            Object student02 = createStudent(thesisService, Student02.class);
            assertNotNull(student02);
            assertInstanceOf(studentClass, student02);
            testToHaveAnIdField(student02, studentIdField);
            Long student02Id = getEntityId(student02, studentIdField);

            Object thesis02 = createThesis(thesisService, Thesis02.class, teacherId);
            assertNotNull(thesis02);
            assertInstanceOf(thesisClass, thesis02);
            testToHaveAnIdField(thesis02, thesisIdField);
            Long thesis02Id = getEntityId(thesis02, thesisIdField);

            thesis02 = thesisService.assignThesis(thesis02Id, student02Id);
            assertNotNull(thesis02);
            thesis02 = thesisService.getThesis(thesis02Id);
            assertNotNull(thesis02);
            assertInstanceOf(thesisClass, thesis02);
            assertEquals(thesis02Id, getEntityId(thesis02, thesisIdField));
            assertNotNull(getFieldValue(thesis02, studentField, studentClass));
            assertEquals(student02Id,
                    getEntityId(getFieldValue(thesis02, studentField, studentClass), studentIdField));

            try {
                thesis02 = thesisService.assignThesis(thesis02Id, studentId);
                assertNull(thesis02);
            } catch (IllegalStateException ex) {
                assertNotNull(ex);
                log.info("Successfully thrown exception " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
            }
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void TH03_shouldSubmitThesis() {
        try {
            Object teacher = createTeacher(thesisService, Teacher01.class);
            assertNotNull(teacher);
            assertInstanceOf(teacherClass, teacher);
            testToHaveAnIdField(teacher, teacherIdField);

            Object thesis = createThesis(thesisService, Thesis01.class, getEntityId(teacher, teacherIdField));
            assertNotNull(thesis);
            assertInstanceOf(thesisClass, thesis);
            testToHaveAnIdField(thesis, thesisIdField);

            Long thesisId = getEntityId(thesis, thesisIdField);
            Object status = getFieldValue(thesis, "status");

            Object student = createStudent(thesisService, Student01.class);
            assertNotNull(student);
            assertInstanceOf(studentClass, student);
            testToHaveAnIdField(student, studentIdField);

            thesis = thesisService.assignThesis(thesisId, getEntityId(student, studentIdField));
            assertNotNull(thesis);

            thesis = thesisService.submitThesis(thesisId);
            assertNotNull(thesis);
            thesis = thesisService.getThesis(thesisId);
            assertNotNull(thesis);
            assertInstanceOf(thesisClass, thesis);
            assertEquals(thesisId, getEntityId(thesis, thesisIdField));
            assertNotEquals(status, getFieldValue(thesis, "status"));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void TH04_shouldDeleteThesisButNotTeacher() {
        try {
            Object teacher = createTeacher(thesisService, Teacher01.class);
            assertNotNull(teacher);
            assertInstanceOf(teacherClass, teacher);
            testToHaveAnIdField(teacher, teacherIdField);

            Object thesis = createThesis(thesisService, Thesis01.class, getEntityId(teacher, teacherIdField));
            assertNotNull(thesis);
            assertInstanceOf(thesisClass, thesis);
            testToHaveAnIdField(thesis, thesisIdField);
            Long thesisId = getEntityId(thesis, thesisIdField);

            Object removed = thesisService.deleteThesis(thesisId);
            assertEquals(thesisId, getEntityId(removed, thesisIdField));
            Object deleteCheck = thesisService.getThesis(thesisId);
            assertNull(deleteCheck);

            teacher = thesisService.getTeacher(getEntityId(teacher, teacherIdField));
            assertNotNull(teacher);
            assertInstanceOf(teacherClass, teacher);
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void TH05_shouldGetAllTheses() {
        try {
            Object teacher = createTeacher(thesisService, Teacher01.class);
            assertNotNull(teacher);
            assertInstanceOf(teacherClass, teacher);
            testToHaveAnIdField(teacher, teacherIdField);

            Object thesis = createThesis(thesisService, Thesis01.class, getEntityId(teacher, teacherIdField));
            assertNotNull(thesis);
            assertInstanceOf(thesisClass, thesis);
            testToHaveAnIdField(thesis, thesisIdField);

            List<Object> theses = thesisService.getTheses();
            assertNotNull(theses);
            assertFalse(theses.isEmpty());
            assertEquals(1, theses.size());
            assertTrue(theses.stream().allMatch(thesisClass::isInstance));
            assertEquals(getEntityId(thesis, thesisIdField), getEntityId(theses.stream().findFirst().orElseGet(null), thesisIdField));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void TH06_shouldGetAllThesesByTeacher() {
        try {
            Object teacher = createTeacher(thesisService, Teacher01.class);
            assertNotNull(teacher);
            assertInstanceOf(teacherClass, teacher);
            testToHaveAnIdField(teacher, teacherIdField);
            Long teacherId = getEntityId(teacher, teacherIdField);

            Object thesis = createThesis(thesisService, Thesis01.class, getEntityId(teacher, teacherIdField));
            assertNotNull(thesis);
            assertInstanceOf(thesisClass, thesis);
            testToHaveAnIdField(thesis, thesisIdField);
            Long thesisId = getEntityId(thesis, thesisIdField);

            List<Object> theses = thesisService.getThesesByTeacher(teacherId);
            assertNotNull(theses);
            assertFalse(theses.isEmpty());
            assertEquals(1, theses.size());
            assertTrue(theses.stream().allMatch(thesisClass::isInstance));
            Object found = theses.get(0);
            assertEquals(thesisId, getEntityId(found, thesisIdField));

            List<String> teacherFields = findField(found, teacherClass);
            assertFalse(teacherFields.isEmpty());
            assertEquals(1, teacherFields.size());
            String teacherField = teacherFields.get(0);
            assertNotNull(getFieldValue(found, teacherField, teacherClass));
            assertEquals(teacherId, getEntityId(getFieldValue(found, teacherField, teacherClass), teacherIdField));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void TH07_shouldGetThesisByStudent() {
        try {
            Object teacher = createTeacher(thesisService, Teacher01.class);
            assertNotNull(teacher);
            assertInstanceOf(teacherClass, teacher);
            testToHaveAnIdField(teacher, teacherIdField);

            Object thesis = createThesis(thesisService, Thesis01.class, getEntityId(teacher, teacherIdField));
            assertNotNull(thesis);
            assertInstanceOf(thesisClass, thesis);
            testToHaveAnIdField(thesis, thesisIdField);
            Long thesisId = getEntityId(thesis, thesisIdField);

            Object student = createStudent(thesisService, Student01.class);
            assertNotNull(student);
            assertInstanceOf(studentClass, student);
            testToHaveAnIdField(student, studentIdField);
            Long studentId = getEntityId(student, studentIdField);

            thesis = thesisService.assignThesis(thesisId, getEntityId(student, studentIdField));
            assertNotNull(thesis);

            Object found = thesisService.getThesisByStudent(studentId);
            assertNotNull(found);
            assertInstanceOf(thesisClass, found);
            assertEquals(thesisId, getEntityId(found, thesisIdField));

            List<String> studentFields = findField(found, studentClass);
            assertFalse(studentFields.isEmpty());
            assertEquals(1, studentFields.size());
            String studentField = studentFields.get(0);
            assertNotNull(getFieldValue(found, studentField, studentClass));
            assertEquals(studentId, getEntityId(getFieldValue(found, studentField, studentClass), studentIdField));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void TH08_shouldGetAThesis() {
        try {
            Object teacher = createTeacher(thesisService, Teacher01.class);
            assertNotNull(teacher);
            assertInstanceOf(teacherClass, teacher);
            testToHaveAnIdField(teacher, teacherIdField);

            Object thesis = createThesis(thesisService, Thesis01.class, getEntityId(teacher, teacherIdField));
            assertNotNull(thesis);
            assertInstanceOf(thesisClass, thesis);
            testToHaveAnIdField(thesis, thesisIdField);
            Long thesisId = getEntityId(thesis, thesisIdField);

            Object got = thesisService.getThesis(thesisId);
            assertNotNull(got);
            assertInstanceOf(thesisClass, got);
            assertEquals(thesisId, getEntityId(got, thesisIdField));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void TH09_shouldUpdateThesis() {
        try {
            Object teacher = createTeacher(thesisService, Teacher01.class);
            assertNotNull(teacher);
            assertInstanceOf(teacherClass, teacher);
            testToHaveAnIdField(teacher, teacherIdField);
            Long teacher01Id = getEntityId(teacher, teacherIdField);

            Object thesis = createThesis(thesisService, Thesis01.class, teacher01Id);
            assertNotNull(thesis);
            assertInstanceOf(thesisClass, thesis);
            testToHaveAnIdField(thesis, thesisIdField);
            Long thesisId = getEntityId(thesis, thesisIdField);

            Object teacher02 = createTeacher(thesisService, Teacher02.class);
            assertNotNull(teacher02);
            assertInstanceOf(teacherClass, teacher02);
            testToHaveAnIdField(teacher02, teacherIdField);

            List<String> teacherFields = findField(thesis, teacherClass);
            assertFalse(teacherFields.isEmpty());
            assertEquals(1, teacherFields.size());
            String teacherField = teacherFields.get(0);
            assertNotNull(getFieldValue(thesis, teacherField, teacherClass));
            assertEquals(teacher01Id, getEntityId(getFieldValue(thesis, teacherField, teacherClass), teacherIdField));
            thesis = setFieldValue(thesis, teacherField, teacher02);
            thesis = thesisService.updateThesis(thesis);
            assertNotNull(thesis);
            thesis = thesisService.getThesis(thesisId);
            assertNotNull(thesis);
            assertInstanceOf(thesisClass, thesis);
            assertEquals(getEntityId(teacher02, teacherIdField), getEntityId(getFieldValue(thesis, teacherField, teacherClass), teacherIdField));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void TH10_shouldDeleteTeacherWithThesis() {
        try {
            Object teacher = createTeacher(thesisService, Teacher01.class);
            assertNotNull(teacher);
            assertInstanceOf(teacherClass, teacher);
            testToHaveAnIdField(teacher, teacherIdField);
            Long teacherId = getEntityId(teacher, teacherIdField);

            Object thesis = createThesis(thesisService, Thesis01.class, teacherId);
            assertNotNull(thesis);
            assertInstanceOf(thesisClass, thesis);
            testToHaveAnIdField(thesis, thesisIdField);
            Long thesisId = getEntityId(thesis, thesisIdField);

            Object removedTeacher = thesisService.deleteTeacher(teacherId);
            assertNotNull(removedTeacher);
            assertInstanceOf(teacherClass, teacher);
            Object teacherDeleteCheck = thesisService.getTeacher(teacherId);
            assertNull(teacherDeleteCheck);
            Object thesisDeleteCheck = thesisService.getThesis(thesisId);
            assertNull(thesisDeleteCheck);
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void TH11_shouldDeleteThesisButNotStudent() {
        try {
            Object teacher = createTeacher(thesisService, Teacher01.class);
            assertNotNull(teacher);
            assertInstanceOf(teacherClass, teacher);
            testToHaveAnIdField(teacher, teacherIdField);

            Object thesis = createThesis(thesisService, Thesis01.class, getEntityId(teacher, teacherIdField));
            assertNotNull(thesis);
            assertInstanceOf(thesisClass, thesis);
            testToHaveAnIdField(thesis, thesisIdField);
            Long thesisId = getEntityId(thesis, thesisIdField);

            Object student = createStudent(thesisService, Student01.class);
            assertNotNull(student);
            assertInstanceOf(studentClass, student);
            testToHaveAnIdField(student, studentIdField);
            Long studentId = getEntityId(student, studentIdField);

            thesis = thesisService.assignThesis(thesisId, getEntityId(student, studentIdField));
            assertNotNull(thesis);

            Object removed = thesisService.deleteThesis(thesisId);
            assertEquals(thesisId, getEntityId(removed, thesisIdField));
            Object deleteCheck = thesisService.getThesis(thesisId);
            assertNull(deleteCheck);

            student = thesisService.getStudent(studentId);
            assertNotNull(student);
            assertInstanceOf(studentClass, student);
            testToHaveAnIdField(student, studentIdField);
        } catch (Exception e) {
            fail(e);
        }
    }

}
