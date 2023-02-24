package sk.stuba.fei.uim.vsa.pr1;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static sk.stuba.fei.uim.vsa.pr1.utils.TestConstants.ID_FIELDS;
import static sk.stuba.fei.uim.vsa.pr1.utils.TestUtils.*;

public class SanityCheckTest {

    private static final Logger log = LoggerFactory.getLogger(SanityCheckTest.class);

    private static AbstractThesisService<Object, Object, Object> thesisService;
    private static Class<?> thesisClass;
    private static String thesisIdField;
    private static Class<?> teacherClass;
    private static String teacherIdField;
    private static Class<?> studentClass;
    private static String studentIdField;

    @BeforeAll
    static void setup() {
        log.info("Searching for implementation of AbstractThesisService class");
        thesisService = (AbstractThesisService<Object, Object, Object>) getServiceClass();
        assertNotNull(thesisService);
        log.info("Found class " + thesisService.getClass().getCanonicalName());
        log.info("Searching for implementation of Thesis entity class");
        thesisClass = getEntityClassFromService((Class<AbstractThesisService<?, ?, ?>>) thesisService.getClass(), 2);
        assertNotNull(thesisClass);
        log.info("Found entity class " + thesisClass.getCanonicalName());
        log.info("Searching for implementation of Teacher entity class");
        teacherClass = getEntityClassFromService((Class<AbstractThesisService<?, ?, ?>>) thesisService.getClass(), 1);
        assertNotNull(teacherClass);
        log.info("Found entity class " + teacherClass.getCanonicalName());
        log.info("Searching for implementation of Student entity class");
        studentClass = getEntityClassFromService((Class<AbstractThesisService<?, ?, ?>>) thesisService.getClass(), 0);
        assertNotNull(studentClass);
        log.info("Found entity class " + studentClass.getCanonicalName());

        log.info("Searching for identifier property/field of type Long for entity class " + thesisClass.getName());
        thesisIdField = findIdFieldOfEntityClass(Arrays.asList(ID_FIELDS), thesisClass);
        assertNotNull(thesisIdField);
        log.info("Found identifier for class " + thesisClass.getName() + " as field '" + thesisIdField + "'");
        log.info("Searching for identifier property/field of type Long for entity class " + teacherClass.getName());
        teacherIdField = findIdFieldOfEntityClass(Arrays.asList(ID_FIELDS), teacherClass);
        assertNotNull(teacherIdField);
        log.info("Found identifier for class " + teacherClass.getName() + " as field '" + teacherIdField + "'");
        log.info("Searching for identifier property/field of type Long for entity class " + studentClass.getName());
        studentIdField = findIdFieldOfEntityClass(Arrays.asList(ID_FIELDS), studentClass);
        assertNotNull(studentIdField);
        log.info("Found identifier for class " + studentClass.getName() + " as field '" + studentIdField + "'");

        try {
            log.info("Searching for date type field in " + thesisClass.getName() + " class");
            Object thesis = thesisClass.newInstance();
            Class[] dateClasses = new Class[]{Date.class, LocalDate.class, java.sql.Date.class, LocalDateTime.class, Calendar.class, GregorianCalendar.class, Timestamp.class};
            Map<Class, List<String>> dateFieldsOfClass = Arrays.stream(dateClasses)
                    .filter(clazz -> findField(thesis, clazz).size() == 2)
                    .collect(Collectors.toMap(Function.identity(), clazz -> (List<String>) findField(thesis, clazz)));
            assertNotNull(dateFieldsOfClass);
            assertFalse(dateFieldsOfClass.isEmpty());
            assertEquals(1, dateFieldsOfClass.size());
            log.info("Found field for type '" + dateFieldsOfClass.keySet().stream().findFirst().get().getName() + "' in class " + thesisClass.getName());
            List<String> dateFields = dateFieldsOfClass.entrySet().stream().findFirst().get().getValue();
            assertNotNull(dateFields);
            assertFalse(dateFields.isEmpty());
            assertEquals(2, dateFields.size());
            log.info("Found date fields " + String.join(", ", dateFields) + " in class " + thesisClass.getName());

            log.info("Searching for status field in class " + thesisClass.getName());
            List<String> statusFields = findField(thesis, "status"); // TODO zovšeobecniť
            assertNotNull(statusFields);
            assertFalse(statusFields.isEmpty());
            assertEquals(1, statusFields.size());
        } catch (Exception e) {
            fail(e);
        }
    }

    @BeforeEach
    void before() {

    }

    @AfterAll
    static void cleaning() {
        log.info("Cleaning after the test");
        thesisService.close();
    }

    @Test
    void testForProjectSetup() {
        try {
            log.info("Running basic tests of creating instances of entity classes");
            Object thesis = thesisClass.newInstance();
            assertNotNull(thesis);
            assertNull(getEntityId(thesis, thesisIdField));
            Object teacher = teacherClass.newInstance();
            assertNotNull(teacher);
            assertNull(getEntityId(teacher, teacherIdField));
            Object students = studentClass.newInstance();
            assertNotNull(students);
            assertNull(getEntityId(students, studentIdField));
        } catch (Exception e) {
            fail(e);
        }
    }
}
