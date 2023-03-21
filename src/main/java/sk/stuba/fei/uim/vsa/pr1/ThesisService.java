package sk.stuba.fei.uim.vsa.pr1;

import sk.stuba.fei.uim.vsa.pr1.entities.Assignment;
import sk.stuba.fei.uim.vsa.pr1.entities.Student;
import sk.stuba.fei.uim.vsa.pr1.entities.Teacher;
import sk.stuba.fei.uim.vsa.pr1.enums.Status;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.List;

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
            if(student.getEmail() != null) {
                TypedQuery<Student> studentTypedQuery = em.createQuery("select s from Student s where s.email= :email", Student.class);
                studentTypedQuery.setParameter("email", student.getEmail());
                if(studentTypedQuery.getResultList().size() > 0) {
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
        em.close();
        return  studentTypedQuery.getResultList();
    }

    @Override
    public Student deleteStudent(Long id) {
        if(id == null) {
            throw new IllegalArgumentException("Id is null");
        }
        EntityManager em = emf.createEntityManager();
        try {
            Student student = em.getReference(Student.class, id);
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
            TypedQuery<Teacher> query = em.createQuery("SELECT s FROM Student s WHERE s.email = :email", Teacher.class);
            query.setParameter("email", email);
            if (query.getResultList().size() > 0) {
                return null;
            }
            TypedQuery<Teacher> query1 = em.createQuery("SELECT s FROM Student s WHERE s.aisId = :ais", Teacher.class);
            query1.setParameter("ais", aisId);
            if (query1.getResultList().size() > 0) {
                return null;
            }
            return new Teacher(aisId, name, email, department);
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
            if(teacher.getEmail() != null) {
                TypedQuery<Teacher> pedagogTypedQuery = em.createQuery("select p from Teacher p where p.email= :email", Teacher.class);
                pedagogTypedQuery.setParameter("email", teacher.getEmail());
                if(pedagogTypedQuery.getResultList().size() > 0) {
                    return null;
                }
            }
            em.getTransaction().begin();
            pedagog.setInstitut(teacher.getInstitut());
            pedagog.setOddelenie(teacher.getOddelenie());
            pedagog.setAssignmentList(teacher.getAssignmentList());
            pedagog.setEmail(teacher.getEmail());
            pedagog.setMeno(teacher.getMeno());
            em.getTransaction().commit();
            return pedagog;
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
    public List<Teacher> getTeachers() {
        EntityManager em = emf.createEntityManager();
        TypedQuery<Teacher> pedagogTypedQuery = em.createQuery("SELECT p from Teacher p", Teacher.class);
        em.close();
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
            }
            return teacher;
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
            Assignment assignment = new Assignment(teacher, title, type, description);
            em.getTransaction().begin();
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
            boolean compareDates = LocalDate.now().isAfter(assignment.getOdovzdaniePrace());
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
        return null;
    }

    @Override
    public List<Assignment> getTheses() {
        return null;
    }

    @Override
    public List<Assignment> getThesesByTeacher(Long teacherId) {
        return null;
    }

    @Override
    public Assignment getThesisByStudent(Long studentId) {
        return null;
    }

    @Override
    public Assignment getThesis(Long id) {
        return null;
    }

    @Override
    public Assignment updateThesis(Assignment thesis) {
        return null;
    }
}
