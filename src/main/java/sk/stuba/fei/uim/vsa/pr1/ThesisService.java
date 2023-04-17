package sk.stuba.fei.uim.vsa.pr1;

import sk.stuba.fei.uim.vsa.pr1.bonus.MyPage;
import sk.stuba.fei.uim.vsa.pr1.bonus.Page;
import sk.stuba.fei.uim.vsa.pr1.bonus.Pageable;
import sk.stuba.fei.uim.vsa.pr1.bonus.PageableThesisService;
import sk.stuba.fei.uim.vsa.pr1.entities.Assignment;
import sk.stuba.fei.uim.vsa.pr1.entities.Student;
import sk.stuba.fei.uim.vsa.pr1.entities.Teacher;
import sk.stuba.fei.uim.vsa.pr1.enums.Status;
import sk.stuba.fei.uim.vsa.pr1.enums.Typ;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDate;
import java.sql.Date;
import java.util.*;

public class ThesisService extends AbstractThesisService<Student, Teacher, Assignment> implements PageableThesisService<Student, Teacher, Assignment> {

    public ThesisService() {
        super();
    }

    @Override
    public Student createStudent(Long aisId, String name, String email) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Student> query = em.createQuery("SELECT s FROM Student s WHERE s.email = :email", Student.class);
            query.setParameter("email", email);
            if (query.getResultList().size() > 0) {
                return null;
            }
            TypedQuery<Student> query1 = em.createQuery("SELECT s FROM Student s WHERE s.aisId = :ais", Student.class);
            query1.setParameter("ais", aisId);
            if (query1.getResultList().size() > 0) {
                return null;
            }
            TypedQuery<Teacher> query2 = em.createQuery("SELECT s FROM Teacher s WHERE s.email = :email", Teacher.class);
            query2.setParameter("email", email);
            if (query2.getResultList().size() > 0) {
                return null;
            }
            Teacher teacher = em.find(Teacher.class, aisId);
            if (teacher != null) {
                return null;
            }
            Student student = new Student(aisId, name, email);
            em.getTransaction().begin();
            em.persist(student);
            em.getTransaction().commit();
            return student;
        }catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return null;
        }finally {
            if(em != null) {
                em.close();
            }
        }
    }

    @Override
    public Student getStudent(Long id) {
        if(id == null) {
            throw new IllegalArgumentException("Student not found");
        }
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Student.class, id);
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return null;
        } finally {
            if(em != null) {
                em.close();
            }
        }
    }

    @Override
    public Student updateStudent(Student student) {
        if (student == null) {
            throw new IllegalArgumentException("cannot be null");
        }

        if (student.getAisId() == null) {
            throw new IllegalArgumentException("AisId cannot be null");
        }
        EntityManager em = emf.createEntityManager();

        // Retrieve the existing student instance from the database
        Student student1 = em.find(Student.class, student.getAisId());
        if (student1 == null) {
            return null;
        }
        try {
            em.getTransaction().begin();

            if(!Objects.equals(student1.getEmail(), student.getEmail())) {
                TypedQuery<Student> query = em.createQuery("SELECT s FROM Student s WHERE s.email = :email", Student.class);
                query.setParameter("email", student.getEmail());
                List<Student> students = query.getResultList();
                if (!students.isEmpty()) {
                    return null;
                }
            }


            if(student.getAssignment() != null && student.getAssignment().getOdovzdaniePrace() != null && student.getAssignment().getDatumZverejnenia() != null) {
                if (student.getAssignment().getOdovzdaniePrace().isBefore(student.getAssignment().getDatumZverejnenia())) {
                    return null;
                }
            }

            //If the student has null assignment, then the set assignment is null for student and in assigment is set student to null
            /*if(student.getAssignment() == null) {
                if(student1.getAssignment() != null) {
                    student1.getAssignment().setStudent(null);
                    student1.getAssignment().setStatus(Status.VOLNA);
                }
            }
            // Update the student instance
            student1.setSemesterStudia(student.getSemesterStudia());
            student1.setRocnikStudia(student.getRocnikStudia());
            student1.setMeno(student.getMeno());
            student1.setAssignment(student.getAssignment());
            student1.setEmail(student.getEmail());
            student1.setProgramStudia(student.getProgramStudia());
            em.getTransaction().commit();*/
            student1 = em.merge(student);
            if(student.getAssignment() != null) {
                Assignment assignment = em.find(Assignment.class, student.getAssignment().getId());
                if (assignment != null) {
                    if (assignment.getStudent() != null) {
                        return null;
                    }
                    assignment.setStudent(student1);
                    em.merge(assignment);
                }
            }
            em.getTransaction().commit();
            return student1;
        } catch (Exception e) {
            // If an exception occurs during the transaction, rollback and return null
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return null;
        } finally {
            if(em != null) {
                em.close();
            }
        }
    }

    @Override
    public List<Student> getStudents() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Student> studentTypedQuery = em.createQuery("SELECT k from Student k", Student.class);
            return studentTypedQuery.getResultList();
        }
        catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return new ArrayList<>();
        }finally {
            em.close();
        }
    }

    @Override
    public Student deleteStudent(Long id) {
        if(id == null) {
            throw new IllegalArgumentException("Id is null");
        }
        EntityManager em = emf.createEntityManager();
        try {
            Student student = em.getReference(Student.class, id);
            if(student == null) {
                return null;
            }
            TypedQuery<Assignment> assignmentTypedQuery = em.createQuery("SELECT a FROM Assignment a WHERE a.student.aisId = :student", Assignment.class);
            assignmentTypedQuery.setParameter("student", student.getAisId());
            List<Assignment> assignmentList = assignmentTypedQuery.getResultList();
            em.getTransaction().begin();
            for(Assignment assignment : assignmentList) {
                assignment.setStudent(null);
            }
            em.getTransaction().commit();
            em.getTransaction().begin();
            em.remove(student);
            em.getTransaction().commit();
            return student;
        }catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return null;
        }finally {
            if(em != null) {
                em.close();
            }
        }
    }

    @Override
    public Teacher createTeacher(Long aisId, String name, String email, String department) {
        if(aisId == null) {
            return null;
        }
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Teacher> query = em.createQuery("SELECT s FROM Teacher s WHERE s.email = :email", Teacher.class);
            query.setParameter("email", email);
            if (query.getResultList().size() > 0) {
                return null;
            }
            TypedQuery<Teacher> query1 = em.createQuery("SELECT s FROM Teacher s WHERE s.aisId = :ais", Teacher.class);
            query1.setParameter("ais", aisId);
            if (query1.getResultList().size() > 0) {
                return null;
            }
            TypedQuery<Student> query2 = em.createQuery("SELECT s FROM Student s WHERE s.email = :email", Student.class);
            query2.setParameter("email", email);
            if (query2.getResultList().size() > 0) {
                return null;
            }
            Student student = em.find(Student.class, aisId);
            if(student != null) {
                return null;
            }
            Teacher teacher = new Teacher(aisId, name, email, department);
            em.getTransaction().begin();
            em.persist(teacher);
            em.getTransaction().commit();
            return teacher;
        }catch (Exception e){
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return null;
        }finally {
            if(em != null) {
                em.close();
            }
        }
    }

    @Override
    public Teacher getTeacher(Long id) {
        if(id == null) {
            throw new IllegalArgumentException("Id is null");
        }
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Teacher.class, id);
        }catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return null;
        }finally {
            if(em != null) {
                em.close();
            }
        }
    }

    @Override
    public Teacher updateTeacher(Teacher teacher) {
        if (teacher == null) {
            throw new IllegalArgumentException("teacher cannot be null");
        }

        if (teacher.getAisId() == null) {
            throw new IllegalArgumentException("teacherId cannot be null");
        }

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        try {
            Teacher teacher1 = em.find(Teacher.class, teacher.getAisId());
            if (teacher1 == null) {
                return null;
            }

            if (!Objects.equals(teacher1.getEmail(), teacher.getEmail())) {
                TypedQuery<Teacher> query = em.createQuery("SELECT t FROM Teacher t WHERE t.email = :email", Teacher.class);
                query.setParameter("email", teacher.getEmail());
                List<Teacher> teachers = query.getResultList();
                if (!teachers.isEmpty()) {
                    return null;
                }
            }
            //teacher1.setInstitut(teacher.getInstitut());
            Teacher updatedTeacher = em.merge(teacher);
            /*List<Assignment> existingThesisList = teacher1.getAssignmentList();
            List<Assignment> updatedThesisList = updatedTeacher.getAssignmentList();

            if (updatedThesisList != null) {
                for (Assignment assignments : updatedThesisList) {
                    assignments.setPracovisko(updatedTeacher.getInstitut());
                    assignments.setTeacher(updatedTeacher);
                    em.merge(assignments);
                }
            }

            if (updatedThesisList != null) {
                List<Assignment> addedThesisList = new ArrayList<>(updatedThesisList);
                assert existingThesisList != null;
                addedThesisList.removeAll(existingThesisList);
                for (Assignment addedThesis : addedThesisList) {
                    makeThesisAssignment(updatedTeacher.getAisId(), addedThesis.getNazov(), addedThesis.getTyp().name(), addedThesis.getPopis());
                }
            }*/

            em.getTransaction().commit();
            return updatedTeacher;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return null;
        }finally {
            if(em != null) {
                em.close();
            }
        }
    }

    @Override
    public List<Teacher> getTeachers() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Teacher> pedagogicTypedQuery = em.createQuery("SELECT p from Teacher p", Teacher.class);
            return pedagogicTypedQuery.getResultList();
        }catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return new ArrayList<>();
        }finally {
            if(em != null) {
                em.close();
            }
        }
    }

    @Override
    public Teacher deleteTeacher(Long id) {
        if(id == null) {
            throw new IllegalArgumentException("Id is null");
        }
        EntityManager em = emf.createEntityManager();
        try {
            Teacher teacher = em.getReference(Teacher.class, id);
            if(teacher != null) {
                em.getTransaction().begin();
                em.remove(teacher);
                em.getTransaction().commit();
                return teacher;
            }
            return null;
        }catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return null;
        }finally {
            if(em != null) {
                em.close();
            }
        }
    }

    @Override
    public Assignment makeThesisAssignment(Long supervisor, String title, String type, String description) {
        if(supervisor == null) {
            throw new IllegalArgumentException("Supervisor is null");
        }
        EntityManager em = emf.createEntityManager();
        try {
            Teacher teacher = em.find(Teacher.class, supervisor);
            if(teacher == null) {
                return null;
            }
            // check if type is valid
            if (type != null) {
                if (!Arrays.asList(Typ.values()).contains(Typ.valueOf(type))) {
                    return null;
                }
            }
            Assignment assignment = new Assignment(teacher, title, type, description);
            em.getTransaction().begin();
            teacher.addAssignment(assignment);
            em.persist(assignment);
            em.getTransaction().commit();
            return assignment;
        }catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return null;
        }finally {
            if(em != null) {
                em.close();
            }
        }
    }

    @Override
    public Assignment assignThesis(Long thesisId, Long studentId) {
        if(thesisId == null) {
            throw new IllegalArgumentException("thesisId is null");
        }
        if(studentId == null) {
            throw new IllegalArgumentException("studentId is null");
        }
        EntityManager em = emf.createEntityManager();
        try {
            Assignment assignment = em.find(Assignment.class, thesisId);
            Student student = em.find(Student.class, studentId);
            if(assignment == null || student == null) {
                return null;
            }
            TypedQuery<Assignment> assignmentTypedQuery = em.createQuery("select a from Assignment a where a.student.aisId = :aisId", Assignment.class);
            assignmentTypedQuery.setParameter("aisId", studentId);
            if(assignmentTypedQuery.getResultList().size() == 1){
                return null;
            }
            if (assignment.getDatumZverejnenia() != null && assignment.getOdovzdaniePrace() != null) {
                if (assignment.getDatumZverejnenia().isAfter(assignment.getOdovzdaniePrace())) {
                    return null;
                }
            }
            boolean compareDates = assignment.getOdovzdaniePrace().isBefore(LocalDate.now());
            if(assignment.getStatus().equals(Status.ODOVZDANA) || assignment.getStatus().equals(Status.ZABRANA)
            || compareDates) {
                throw new IllegalStateException("Assignment cannot be taken");
            }
            em.getTransaction().begin();
            student.setAssignment(assignment);
            em.merge(student);
            assignment.setStudent(student);
            em.merge(assignment);
            assignment.setStatus(Status.ZABRANA);
            em.getTransaction().commit();
            return assignment;
        }catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return null;
        }finally {
            if(em != null) {
                em.close();
            }
        }
    }

    @Override
    public Assignment submitThesis(Long thesisId) {
        if(thesisId == null) {
            throw new IllegalArgumentException("thesisId is null");
        }
        EntityManager em = emf.createEntityManager();
        try {
            Assignment assignment = em.find(Assignment.class, thesisId);
            if(assignment == null) {
                return null;
            }
            if(assignment.getStudent() == null) {
                throw new IllegalStateException("Assignment cannot be submitted");
            }
            Student student = assignment.getStudent();
            boolean compareDates = LocalDate.now().isAfter(assignment.getOdovzdaniePrace());
            if(assignment.getStatus().equals(Status.ODOVZDANA) || compareDates || student.getAisId() == null
            || assignment.getStatus().equals(Status.VOLNA)) {
                throw new IllegalStateException("Assignment cannot be submitted");
            }
            em.getTransaction().begin();
            assignment.setStatus(Status.ODOVZDANA);
            em.getTransaction().commit();
            return assignment;
        }catch (Exception e){
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return null;
        }finally {
            if(em != null) {
                em.close();
            }
        }
    }

    @Override
    public Assignment deleteThesis(Long id) {
        if(id == null) {
            throw new IllegalArgumentException("id is null");
        }
        EntityManager em = emf.createEntityManager();
        try {
            Assignment assignment = em.getReference(Assignment.class, id);
            if(assignment == null) {
                return null;
            }
            em.getTransaction().begin();
            em.remove(assignment);
            em.getTransaction().commit();
            return assignment;
        }catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return null;
        }finally {
            if(em != null) {
                em.close();
            }
        }
    }

    @Override
    public List<Assignment> getTheses() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Assignment> assignmentTypedQuery = em.createQuery("SELECT a from Assignment a", Assignment.class);
            return assignmentTypedQuery.getResultList();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return new ArrayList<>();
        } finally {
            if(em != null) {
                em.close();
            }
        }
    }

    @Override
    public List<Assignment> getThesesByTeacher(Long teacherId) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Assignment> teacher = em.createQuery("select s from Assignment s where s.teacher.aisId = :aisId", Assignment.class).setParameter("aisId", teacherId);
            if(teacher == null){
                return new ArrayList<>();
            }
            return teacher.getResultList();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return new ArrayList<>();
        } finally {
            if(em != null) {
                em.close();
            }
        }
    }

    @Override
    public Assignment getThesisByStudent(Long studentId) {
        EntityManager em = emf.createEntityManager();
        try{
            TypedQuery<Assignment> student2 = em.createQuery("select s from Assignment s where s.student.aisId = :aisId", Assignment.class).setParameter("aisId", studentId);
            if(student2.getResultList().size() == 0){
                return null;
            }
            if(student2.getResultList().size() > 1){
                return null;
            }

            return student2.getSingleResult();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return null;
        } finally {
            if(em != null) {
                em.close();
            }
        }
    }

    @Override
    public Assignment getThesis(Long id) {
        if(id == null){
            throw new IllegalArgumentException("id is null");
        }
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Assignment.class, id);
        }catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return null;
        }finally {
            if(em != null) {
                em.close();
            }
        }
    }

    @Override
    public Assignment updateThesis(Assignment thesis) {
        if (thesis == null || thesis.getId() == null) {
            throw new IllegalArgumentException("Thesis or thesis id cannot be null");
        }
        if (thesis.getDatumZverejnenia() != null && thesis.getOdovzdaniePrace() != null) {
            if (thesis.getDatumZverejnenia().isAfter(thesis.getOdovzdaniePrace())) {
                return null;
            }
        }
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Assignment existingThesis = em.find(Assignment.class, thesis.getId());
            if (existingThesis == null) {
                return null;
            }
            // check if thesis is free if not return null and its not assigned to the same student
            if(existingThesis.getStudent() != null && thesis.getStudent() != null) {
                if(!thesis.getStudent().getAisId().equals(existingThesis.getStudent().getAisId()) && existingThesis.getStatus() != Status.VOLNA) {
                    return null;
                }
            }

            // check if registration number is unique, but if it is the same as the existing one for current thesis ID, it is ok
            if (thesis.getRegistracneCislo() != null) {
                TypedQuery<Assignment> query = em.createQuery("SELECT a FROM Assignment a WHERE a.registracneCislo = :regCislo", Assignment.class);
                query.setParameter("regCislo", thesis.getRegistracneCislo());
                List<Assignment> resultList = query.getResultList();
                if (resultList.size() > 0 && !resultList.get(0).getId().equals(thesis.getId())) {
                    return null;
                }
            }

            // write query to find student by ais id in assignment table
            Assignment assignment = new Assignment();
            if(thesis.getStudent() != null) {
                TypedQuery<Assignment> assignmentTypedQuery = em.createQuery("select a from Assignment a where a.student.aisId = :aisId", Assignment.class).setParameter("aisId", thesis.getStudent().getAisId());
                if(assignmentTypedQuery.getResultList().size() > 0){
                    assignment = assignmentTypedQuery.getSingleResult();
                }
            }

            if(assignment != null && assignment.getStudent() != null) {
                if(!Objects.equals(assignment.getStudent().getAisId(), thesis.getStudent().getAisId()) && assignment.getStatus() != Status.VOLNA) {
                    return null;
                }
                assignment.setStudent(null);
                assignment.setStatus(Status.VOLNA);
            }

            List<Assignment> teacherAssignments = thesis.getTeacher().getAssignmentList();
            teacherAssignments.add(thesis);
            /*if(existingThesis.getStudent() != null) {
                existingThesis.getStudent().setAssignment(null);
                existingThesis.setStudent(null);
                existingThesis.setStatus(Status.VOLNA);
            }*/
            if(thesis.getStudent() != null) {
                thesis.getStudent().setAssignment(existingThesis);
            }
            if(!Objects.equals(existingThesis.getTeacher().getAisId(), thesis.getTeacher().getAisId())) {
                existingThesis.getTeacher().getAssignmentList().remove(existingThesis);
            }
            //existingThesis.setRegistracneCislo(thesis.getRegistracneCislo());
            //existingThesis.setTeacher(thesis.getTeacher());
            thesis.getTeacher().setAssignmentList(teacherAssignments);
            em.merge(thesis.getTeacher());
            /*existingThesis.setPracovisko(thesis.getPracovisko());
            existingThesis.setOdovzdaniePrace(thesis.getOdovzdaniePrace());
            existingThesis.setNazov(thesis.getNazov());
            existingThesis.setPopis(thesis.getPopis());
            existingThesis.setDatumZverejnenia(thesis.getDatumZverejnenia());*/
            existingThesis = em.merge(thesis);
            existingThesis.setStudent(thesis.getStudent());
            if(thesis.getStudent() != null) {
                existingThesis.setStatus(Status.ZABRANA);
            }
            if(thesis.getStudent() != null) {
                thesis.getStudent().setAssignment(existingThesis);
            }
            em.getTransaction().commit();
            return existingThesis;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return null;
        } finally {
            em.close();
        }
    }

    @Override
    public Page<Assignment> findTheses(Optional<String> department, Optional<java.util.Date> publishedOn, Optional<String> type, Optional<String> status, Pageable pageable) {
        List<Assignment> assignmentList = this.getTheses();

        if (department.isPresent()) {
            List<Assignment> filtered = new ArrayList<>();
            for (Assignment a : assignmentList) {
                if (a.getPracovisko().equals(department.get())) {
                    filtered.add(a);
                }
            }
            assignmentList = filtered;
        }

        if (publishedOn.isPresent()) {
            LocalDate date = new Date(publishedOn.get().getTime()).toLocalDate();
            List<Assignment> filtered = new ArrayList<>();
            for (Assignment a : assignmentList) {
                if (a.getDatumZverejnenia().equals(date)) {
                    filtered.add(a);
                }
            }
            assignmentList = filtered;
        }

        if (type.isPresent()) {
            List<Assignment> filtered = new ArrayList<>();
            for (Assignment a : assignmentList) {
                if (a.getTyp().name().equals(type.get())) {
                    filtered.add(a);
                }
            }
            assignmentList = filtered;
        }

        if (status.isPresent()) {
            List<Assignment> filtered = new ArrayList<>();
            for (Assignment a : assignmentList) {
                if (a.getStatus().name().equals(status.get())) {
                    filtered.add(a);
                }
            }
            assignmentList = filtered;
        }

        if (assignmentList.size() == 0) {
            return new MyPage<>(new ArrayList<>(), pageable, assignmentList.size());
        }

        int startIndex = pageable.getPageNumber() * pageable.getPageSize();
        int endIndex = Math.min(startIndex + pageable.getPageSize(), assignmentList.size());

        if (startIndex > endIndex) {
            return new MyPage<>(new ArrayList<>(), pageable, assignmentList.size());
        }

        List<Assignment> pageContent = assignmentList.subList(startIndex, endIndex);
        Page<Assignment> page = new MyPage<>(pageContent, pageable, assignmentList.size());

        return page;
    }


    @Override
    public Page<Teacher> findTeachers(Optional<String> name, Optional<String> institute, Pageable pageable) {
        List<Teacher> teacherList = this.getTeachers();

        if (name.isPresent()) {
            List<Teacher> filteredByName = new ArrayList<>();
            for (Teacher teacher : teacherList) {
                if (teacher.getMeno().equals(name.get())) {
                    filteredByName.add(teacher);
                }
            }
            teacherList = filteredByName;
        }

        if (institute.isPresent()) {
            List<Teacher> filteredByInstitute = new ArrayList<>();
            for (Teacher teacher : teacherList) {
                if (teacher.getInstitut().equals(institute.get())) {
                    filteredByInstitute.add(teacher);
                }
            }
            teacherList = filteredByInstitute;
        }

        if (teacherList.size() == 0) {
            return new MyPage<>(new ArrayList<>(), pageable, teacherList.size());
        }

        int startIndex = pageable.getPageNumber() * pageable.getPageSize();
        int endIndex = Math.min(startIndex + pageable.getPageSize(), teacherList.size());

        if (startIndex > endIndex) {
            return new MyPage<>(new ArrayList<>(), pageable, teacherList.size());
        }

        List<Teacher> pageContent = teacherList.subList(startIndex, endIndex);

        return new MyPage<>(pageContent, pageable, teacherList.size());
    }


    @Override
    public Page<Student> findStudents(Optional<String> name, Optional<String> year, Pageable pageable) {
        List<Student> studentList = this.getStudents();

        if (name.isPresent()) {
            List<Student> filteredByName = new ArrayList<>();
            for (Student s : studentList) {
                if (s.getMeno().equals(name.get())) {
                    filteredByName.add(s);
                }
            }
            studentList = filteredByName;
        }

        if (year.isPresent()) {
            Integer yr = Integer.parseInt(year.get());
            List<Student> filteredByYear = new ArrayList<>();
            for (Student s : studentList) {
                if (s.getRocnikStudia().equals(yr)) {
                    filteredByYear.add(s);
                }
            }
            studentList = filteredByYear;
        }

        if (studentList.size() == 0) {
            return new MyPage<>(new ArrayList<>(), pageable, studentList.size());
        }

        int startIndex = pageable.getPageNumber() * pageable.getPageSize();
        int endIndex = Math.min(startIndex + pageable.getPageSize(), studentList.size());

        if (startIndex > endIndex) {
            return new MyPage<>(new ArrayList<>(), pageable, studentList.size());
        }

        List<Student> pageContent = studentList.subList(startIndex, endIndex);

        Page<Student> page = new MyPage<>(pageContent, pageable, studentList.size());

        return page;
    }

}
