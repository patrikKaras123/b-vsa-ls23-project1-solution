package sk.stuba.fei.uim.vsa.pr1.bonus;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.stuba.fei.uim.vsa.pr1.AbstractThesisService;
import sk.stuba.fei.uim.vsa.pr1.utils.TestData;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static sk.stuba.fei.uim.vsa.pr1.utils.TestConstants.*;
import static sk.stuba.fei.uim.vsa.pr1.utils.TestUtils.*;

public class PageableTest {

    private static final Logger log = LoggerFactory.getLogger(PageableTest.class);

    private static PageableThesisService<Object, Object, Object> pagedThesisService;
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
        pagedThesisService = (PageableThesisService<Object, Object, Object>) getServiceClass();
        thesisService = (AbstractThesisService<Object, Object, Object>) getServiceClass();
        assertNotNull(pagedThesisService);
        assertNotNull(thesisService);
        thesisClass = getEntityClassFromService((Class<AbstractThesisService<?, ?, ?>>) thesisService.getClass(), 2);
        assertNotNull(thesisClass);
        teacherClass = getEntityClassFromService((Class<AbstractThesisService<?, ?, ?>>) thesisService.getClass(), 1);
        assertNotNull(teacherClass);
        studentClass = getEntityClassFromService((Class<AbstractThesisService<?, ?, ?>>) thesisService.getClass(), 0);
        assertNotNull(studentClass);
        db = getDBConnection(DB, USERNAME, PASSWORD);
        assertNotNull(db);
        thesisIdField = findIdFieldOfEntityClass(Arrays.asList(ID_FIELDS), thesisClass);
        assertNotNull(thesisIdField);
        teacherIdField = findIdFieldOfEntityClass(Arrays.asList(ID_FIELDS), teacherClass);
        assertNotNull(teacherIdField);
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
    void shouldGetPageOfStudents() throws NoSuchFieldException, IllegalAccessException {
        for (int i = 1; i < 11; i++) {
            Object student01 = thesisService.createStudent(
                    Integer.valueOf(i).longValue(),
                    getTestClassFieldValues(TestData.Student01.class, "name", String.class),
                    "xstudent" + i + "@stuba.sk");
            assertNotNull(student01);
            assertInstanceOf(studentClass, student01);
        }

        Pageable pageable = createPageable(0, 5);
        assertNotNull(pageable);
        Page<Object> students = pagedThesisService.findStudents(Optional.of(TestData.Student01.name), Optional.empty(), pageable);
        assertNotNull(students);
        assertNotNull(students.getContent());
        assertEquals(5, students.getContent().size());
        assertEquals(10, students.getTotalElements());
        assertEquals(2, students.getTotalPages());
        assertEquals(0, students.getPageable().getPageNumber());
        assertTrue(students.getContent().stream().allMatch(studentClass::isInstance));

        students = pagedThesisService.findStudents(Optional.of(TestData.Student01.name), Optional.empty(), pageable.next());
        assertNotNull(students);
        assertNotNull(students.getContent());
        assertEquals(5, students.getContent().size());
        assertEquals(10, students.getTotalElements());
        assertEquals(2, students.getTotalPages());
        assertEquals(1, students.getPageable().getPageNumber());
        assertTrue(students.getContent().stream().allMatch(studentClass::isInstance));
    }

    @Test
    void shouldGetPageOfTeachers() throws NoSuchFieldException, IllegalAccessException {
        for (int i = 1; i < 16; i++) {
            Object teacher = thesisService.createTeacher(
                    Integer.valueOf(i).longValue(),
                    getTestClassFieldValues(TestData.Teacher01.class, "name", String.class),
                    "xteacher" + i + "@stuba.sk",
                    getTestClassFieldValues(TestData.Teacher01.class, "department", String.class));
            assertNotNull(teacher);
            assertInstanceOf(teacherClass, teacher);
        }

        Pageable pageable = createPageable(1, 7);
        assertNotNull(pageable);
        Page<Object> teachers = pagedThesisService.findTeachers(Optional.of(TestData.Teacher01.name), Optional.empty(), pageable);
        assertNotNull(teachers);
        assertNotNull(teachers.getContent());
        assertEquals(7, teachers.getContent().size());
        assertEquals(15, teachers.getTotalElements());
        assertEquals(3, teachers.getTotalPages());
        assertEquals(1, teachers.getPageable().getPageNumber());
        assertTrue(teachers.getContent().stream().allMatch(teacherClass::isInstance));
    }

    @Test
    void shouldGetPageOfTheses() throws NoSuchFieldException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Object teacher = createTeacher(thesisService, TestData.Teacher01.class);
        assertNotNull(teacher);
        assertInstanceOf(teacherClass, teacher);
        testToHaveAnIdField(teacher, teacherIdField);
        Long teacherId = getEntityId(teacher, teacherIdField);

        for (int i = 1; i < 16; i++) {
            String type = "BACHELOR";
            if (i % 2 == 0) type = "MASTER";
            Object thesis = thesisService.makeThesisAssignment(
                    teacherId,
                    getTestClassFieldValues(TestData.Thesis01.class, "title", String.class),
                    type,
                    getTestClassFieldValues(TestData.Thesis01.class, "description", String.class));
            assertNotNull(thesis);
            assertInstanceOf(thesisClass, thesis);
            testToHaveAnIdField(thesis, thesisIdField);
        }

        Pageable pageable = createPageable(1, 7);
        assertNotNull(pageable);
        Page<Object> theses = pagedThesisService.findTheses(Optional.of(TestData.Teacher01.department), Optional.empty(), Optional.of("BACHELOR"), Optional.empty(), pageable);
        assertNotNull(theses);
        assertNotNull(theses.getContent());
        assertEquals(1, theses.getContent().size());
        assertEquals(8, theses.getTotalElements());
        assertEquals(2, theses.getTotalPages());
        assertEquals(1, theses.getPageable().getPageNumber());
        assertTrue(theses.getContent().stream().allMatch(thesisClass::isInstance));
    }
}
