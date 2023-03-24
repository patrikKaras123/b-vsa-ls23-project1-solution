package sk.stuba.fei.uim.vsa.pr1;

import sk.stuba.fei.uim.vsa.pr1.entities.Assignment;
import sk.stuba.fei.uim.vsa.pr1.entities.Student;
import sk.stuba.fei.uim.vsa.pr1.entities.Teacher;
import sk.stuba.fei.uim.vsa.pr1.enums.Status;
import sk.stuba.fei.uim.vsa.pr1.enums.Typ;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ThesisService extends AbstractThesisService<Student, Teacher, Assignment> {

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
            if(teacher != null) {
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
        if(student == null){
            throw new IllegalArgumentException("Student not found");
        }
        if(student.getAisId() == null) {
            throw new IllegalArgumentException("Student not found");
        }
        EntityManager em = emf.createEntityManager();
        Student student1 = em.find(Student.class, student.getAisId());
        if(student1 == null) {
            return null;
        }
        try {
            if(student.getEmail() != null && !student.getEmail().equals(student1.getEmail())) {
                TypedQuery<Student> studentTypedQuery = em.createQuery("select s from Student s where s.email= :email", Student.class);
                studentTypedQuery.setParameter("email", student.getEmail());
                if(studentTypedQuery.getResultList().size() > 0) {
                    return null;
                }
            }
            TypedQuery<Assignment> assignmentTypedQuery = em.createQuery("select a from Assignment a where a.student.aisId = :aisId", Assignment.class);
            assignmentTypedQuery.setParameter("aisId", student.getAisId());
            if(assignmentTypedQuery.getResultList().size() > 1){
                return null;
            }else if(assignmentTypedQuery.getResultList().size() == 1) {
                Assignment assignment = assignmentTypedQuery.getSingleResult();
                assignment.setStudent(null);
            }
            Assignment assignment = new Assignment();
            if(student.getAssignment() != null) {
                assignment = em.find(Assignment.class, student.getAssignment().getId());
                if(assignment == null) {
                    return null;
                }
            }
            em.getTransaction().begin();
            student1.setProgramStudia(student.getProgramStudia());
            student1.setRocnikStudia(student.getRocnikStudia());
            student1.setSemesterStudia(student.getSemesterStudia());
            student1.setAssignment(student.getAssignment());
            student1.setEmail(student.getEmail());
            student1.setMeno(student.getMeno());
            if(assignment.getId() != null) {
                assignment.setStudent(student1);
            }
            em.getTransaction().commit();
            return student1;
        }catch (Exception e){
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return null;
        }finally {
            em.close();
        }
    }

    @Override
    public List<Student> getStudents() {
        EntityManager em = emf.createEntityManager();
        TypedQuery<Student> studentTypedQuery = em.createQuery("SELECT k from Student k", Student.class);
        return studentTypedQuery.getResultList();
    }

    @Override
    public Student deleteStudent(Long id) {
        if(id == null) {
            throw new IllegalArgumentException("Id is null");
        }
        EntityManager em = emf.createEntityManager();
        try {
            Student student = em.getReference(Student.class, id);
            TypedQuery<Assignment> assignmentTypedQuery = em.createQuery("SELECT a FROM Assignment a WHERE a.student = :student", Assignment.class);
            assignmentTypedQuery.setParameter("student", student);
            List<Assignment> assignmentList = assignmentTypedQuery.getResultList();
            em.getTransaction().begin();
            for(Assignment assignment : assignmentList) {
                assignment.setStudent(null);
            }
            em.getTransaction().commit();
            if(student != null) {
                em.getTransaction().begin();
                em.remove(student);
                em.getTransaction().commit();
            }
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
        if(teacher == null) {
            throw new IllegalArgumentException("Teacher is null");
        }
        if(teacher.getAisId() == null) {
            throw new IllegalArgumentException("Id is null");
        }
        EntityManager em = emf.createEntityManager();
        try {
            Teacher pedagog = em.find(Teacher.class, teacher.getAisId());
            if(pedagog == null) {
                return null;
            }
            if(teacher.getEmail() != null && !teacher.getEmail().equals(pedagog.getEmail())) {
                TypedQuery<Teacher> pedagogTypedQuery = em.createQuery("select p from Teacher p where p.email= :email", Teacher.class);
                pedagogTypedQuery.setParameter("email", teacher.getEmail());
                if(pedagogTypedQuery.getResultList().size() > 0) {
                    return null;
                }
            }
            List<Assignment> assignmentList = this.getThesesByTeacher(teacher.getAisId());
            List<Assignment> updatedAssignments = new ArrayList<>();
            for(Assignment assignment : teacher.getAssignmentList()) {
                if(assignment.getId() == null) {
                    updatedAssignments.add(assignment);
                } else {
                    Assignment existingAssignment = em.find(Assignment.class, assignment.getId());
                    if (existingAssignment != null) {
                        updatedAssignments.add(existingAssignment);
                    }
                }
            }
            for(Assignment assignment : assignmentList) {
                boolean found = false;
                for(Assignment updatedAssignment : updatedAssignments){
                    if(updatedAssignment.getId() != null && updatedAssignment.getId().equals(assignment.getId())) {
                        found = true;
                        break;
                    }
                }
                if(!found) {
                    Assignment e = em.getReference(Assignment.class, assignment.getId());
                    System.out.println(e.getId());
                    em.getTransaction().begin();
                    em.remove(e);
                    em.getTransaction().commit();
                }
            }
            em.getTransaction().begin();
            pedagog.setInstitut(teacher.getInstitut());
            pedagog.setOddelenie(teacher.getOddelenie());
            pedagog.setEmail(teacher.getEmail());
            pedagog.setAssignmentList(updatedAssignments);
            pedagog.setMeno(teacher.getMeno());
            em.getTransaction().commit();
            return pedagog;
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
    public List<Teacher> getTeachers() {
        EntityManager em = emf.createEntityManager();
        TypedQuery<Teacher> pedagogTypedQuery = em.createQuery("SELECT p from Teacher p", Teacher.class);
        return pedagogTypedQuery.getResultList();
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
            try {
                TypedQuery<Assignment> query = em.createQuery("SELECT s FROM Assignment s WHERE s.nazov = :title AND s.typ = :typ AND s.teacher = :teacher", Assignment.class);
                query.setParameter("title", title);
                query.setParameter("teacher", teacher);
                Typ assignmentType = Typ.valueOf(type);
                query.setParameter("typ", assignmentType);
                if (query.getResultList().size() > 0) {
                    return null;
                }
            } catch (IllegalArgumentException e) {
                // pass
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
            boolean compareDates = assignment.getOdovzdaniePrace().isBefore(LocalDate.now());
            if(assignment.getStatus().equals(Status.Submitted) || assignment.getStatus().equals(Status.Taken)
            || compareDates) {
                throw new IllegalStateException("Assignment cannot be taken");
            }
            em.getTransaction().begin();
            assignment.setStudent(student);
            assignment.setStatus(Status.Taken);
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
            Student student = assignment.getStudent();
            boolean compareDates = LocalDate.now().isAfter(assignment.getOdovzdaniePrace());
            if(assignment.getStatus().equals(Status.Submitted) || compareDates || student.getAisId() == null
            || assignment.getStatus().equals(Status.Free)) {
                throw new IllegalStateException("Assignment cannot be submitted");
            }
            em.getTransaction().begin();
            assignment.setStatus(Status.Submitted);
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
            return null;
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
        TypedQuery<Assignment> assignmentTypedQuery = em.createQuery("SELECT a from Assignment a", Assignment.class);
        return assignmentTypedQuery.getResultList();
    }

    @Override
    public List<Assignment> getThesesByTeacher(Long teacherId) {
        EntityManager em = emf.createEntityManager();
        try {
            Teacher teacher = em.find(Teacher.class, teacherId);
            if(teacher == null){
                return new ArrayList<>();
            }
            return teacher.getAssignmentList();
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
            Student student = em.find(Student.class, studentId);
            if(student == null){
                return null;
            }
            return student.getAssignment();
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
        if(thesis == null || thesis.getId() == null) {
            throw new IllegalArgumentException("Instance is null");
        }
        EntityManager em = emf.createEntityManager();
        try {
            if(thesis.getTeacher() == null) {
                return null;
            }
            Assignment assignment = em.find(Assignment.class, thesis.getId());
            if(assignment == null) {
                return null;
            }
            em.getTransaction().begin();
            assignment.setNazov(thesis.getNazov());
            assignment.setDatumZverejnenia(thesis.getDatumZverejnenia());
            assignment.setPopis(thesis.getPopis());
            assignment.setStudent(thesis.getStudent());
            assignment.setPracovisko(thesis.getPracovisko());
            assignment.setOdovzdaniePrace(thesis.getOdovzdaniePrace());
            assignment.setTeacher(thesis.getTeacher());
            assignment.setTyp(thesis.getTyp());
            assignment.setStatus(thesis.getStatus());
            assignment.setRegistracneCislo(thesis.getRegistracneCislo());
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
}
