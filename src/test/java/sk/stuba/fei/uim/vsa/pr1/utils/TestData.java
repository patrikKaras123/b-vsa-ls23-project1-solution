package sk.stuba.fei.uim.vsa.pr1.utils;

public class TestData {

    public static class Student01 {
        public static final Long aisId = 70127L;
        public static final String name = "Test-Student";
        public static final String email = "xtest@stuba.sk";
        public static final String studyProgramme = "API";
        public static final int year = 8;
        public static final int term = 18;
    }

    public static class Student02 {
        public static final Long aisId = 665434L;
        public static final String name = "Test-Student The Second";
        public static final String email = "xtests2@stuba.sk";
        public static final String studyProgramme = "B-API";
        public static final int year = 2;
        public static final int term = 4;
    }

    public static class Teacher01 {
        public static final Long aisId = 80123L;
        public static final String name = "Test-Teacher";
        public static final String email = "test.teacher@stuba.sk";
        public static final String institute = "UIM";
        public static final String department = "Software engineering";
    }

    public static class Teacher02 {
        public static final Long aisId = 999234L;
        public static final String name = "Test-Teacher the Second";
        public static final String email = "test.teacher.second@stuba.sk";
        public static final String institute = "UIM";
        public static final String department = "Mathematics";
    }

    public static class Thesis01 {
        public static final String title = "Excellent Simple Bachelor Thesis";
        public static final String description = "Some description of the thesis";
        public static final String type = "BACHELOR";
    }

    public static class Thesis02 {
        public static final String title = "Extra Hard Master Thesis";
        public static final String description = "Some description of the thesis but other then the first one";
        public static final String type = "MASTER";
    }
}
