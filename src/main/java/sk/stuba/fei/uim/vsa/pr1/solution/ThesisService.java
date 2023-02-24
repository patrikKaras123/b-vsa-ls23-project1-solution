package sk.stuba.fei.uim.vsa.pr1.solution;

import sk.stuba.fei.uim.vsa.pr1.AbstractThesisService;
import sk.stuba.fei.uim.vsa.pr1.bonus.Page;
import sk.stuba.fei.uim.vsa.pr1.bonus.Pageable;
import sk.stuba.fei.uim.vsa.pr1.bonus.PageableThesisService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class ThesisService extends AbstractThesisService<Student, Teacher, Thesis> implements PageableThesisService<Student, Teacher, Thesis> {

    public ThesisService() {
        super();
    }

    @Override
    public Student createStudent(Long aisId, String name, String email) {
        log.info("Creating student with AIS ID " + aisId);
        return create(() -> Student.builder()
                .aisId(aisId)
                .name(name)
                .email(email)
                .build());
    }

    @Override
    public Student getStudent(Long id) {
        if (id == null)
            throw new IllegalArgumentException("Provided id must not be null");
        return findOne(id, Student.class);
    }

    @Override
    public Student updateStudent(Student student) {
        if (student == null)
            throw new IllegalArgumentException("Provided student must not be null");
        if (student.getAisId() == null)
            throw new IllegalArgumentException("Provided student.aisId must not be null");
        return update(student);
    }

    @Override
    public List<Student> getStudents() {
        return findByNamedQuery(Student.FIND_ALL_QUERY, Student.class, Collections.emptyMap());
    }

    @Override
    public Student deleteStudent(Long id) {
        if (id == null)
            throw new IllegalArgumentException("Provided id must not be null");
        Student student = delete(id, Student.class);
        Thesis thesis = this.getThesisByStudent(student.getAisId());
        if (thesis != null) {
            thesis.setAuthor(null);
            thesis.setStatus(Thesis.Status.FREE_TO_TAKE);
            this.updateThesis(thesis);
        }
        return student;
    }

    @Override
    public Teacher createTeacher(Long aisId, String name, String email, String department) {
        return create(() -> Teacher.builder()
                .aisId(aisId)
                .name(name)
                .email(email)
                .department(department)
                .institute(department)
                .build());
    }

    @Override
    public Teacher getTeacher(Long id) {
        if (id == null)
            throw new IllegalArgumentException("Provided id must not be null");
        return findOne(id, Teacher.class);
    }

    @Override
    public Teacher updateTeacher(Teacher teacher) {
        if (teacher == null)
            throw new IllegalArgumentException("Provided teacher must not be null");
        if (teacher.getAisId() == null)
            throw new IllegalArgumentException("Provided teacher.aisId must not be null");
        return update(teacher);
    }

    @Override
    public List<Teacher> getTeachers() {
        return findByNamedQuery(Teacher.FIND_ALL_QUERY, Teacher.class, Collections.emptyMap());
    }

    @Override
    public Teacher deleteTeacher(Long id) {
        if (id == null)
            throw new IllegalArgumentException("Provided id must not be null");
        List<Thesis> theses = getThesesByTeacher(id);
        if (!theses.isEmpty()) {
            theses.stream().map(Thesis::getId).forEach(this::deleteThesis);
        }
        return delete(id, Teacher.class);
    }

    @Override
    public Thesis makeThesisAssignment(Long supervisor, String title, String type, String description) {
        return create(() -> {
            Teacher teacher = this.getTeacher(supervisor);
            if (teacher == null)
                throw new IllegalArgumentException("Provided supervisor teacher with id '" + supervisor + "' has not been found!");
            LocalDate now = LocalDate.now();
            return Thesis.builder()
                    .registrationNumber("FEI-" + new SecureRandom().nextInt(99999))
                    .title(title)
                    .description(description)
                    .type(Thesis.Type.valueOf(type.toUpperCase()))
                    .status(Thesis.Status.FREE_TO_TAKE)
                    .supervisor(teacher)
                    .department(teacher.getDepartment())
                    .publishedOn(now)
                    .deadline(now.plusMonths(3L))
                    .build();
        });
    }

    @Override
    public Thesis assignThesis(Long thesisId, Long studentId) {
        Thesis thesis = getThesis(thesisId);
        if (thesis == null)
            throw new IllegalArgumentException("Thesis with id '" + thesisId + "' has not been found!");
        if (thesis.getStatus() != Thesis.Status.FREE_TO_TAKE)
            throw new IllegalStateException("Thesis is not in the state to be assigned to a student");
        if (LocalDate.now().isAfter(thesis.getDeadline()))
            throw new IllegalStateException("Thesis cannot be assigned to a student after the deadline on " + thesis.getDeadline().toString());
        Student student = getStudent(studentId);
        if (student == null)
            throw new IllegalArgumentException("Student with id '" + studentId + "' has not been found!");
        thesis.setAuthor(student);
        thesis.setStatus(Thesis.Status.IN_PROGRESS);
        return update(thesis);
    }

    @Override
    public Thesis submitThesis(Long thesisId) {
        Thesis thesis = getThesis(thesisId);
        if (thesis == null)
            throw new IllegalArgumentException("Thesis with id '" + thesisId + "' has not been found");
        if (LocalDate.now().isAfter(thesis.getDeadline()))
            throw new IllegalStateException("Thesis cannot be submitted after the deadline on " + thesis.getDeadline().toString());
        if (thesis.getStatus() != Thesis.Status.IN_PROGRESS)
            throw new IllegalStateException("Thesis is not in the state to be submitted");
        if (thesis.getAuthor() == null)
            throw new IllegalStateException("Thesis cannot be submitted if it hasn't been assigned to a student");
        thesis.setStatus(Thesis.Status.SUBMITTED);
        return update(thesis);
    }

    @Override
    public Thesis deleteThesis(Long id) {
        if (id == null)
            throw new IllegalArgumentException("Provided id must not be null");
        return delete(id, Thesis.class);
    }

    @Override
    public List<Thesis> getTheses() {
        return findByNamedQuery(Thesis.FIND_ALL_QUERY, Thesis.class, Collections.emptyMap());
    }

    @Override
    public List<Thesis> getThesesByTeacher(Long teacherId) {
        return findByNamedQuery(Thesis.FIND_ALL_BY_SUPERVISOR, Thesis.class, Collections.singletonMap("teacherId", teacherId));
    }

    @Override
    public Thesis getThesisByStudent(Long studentId) {
        List<Thesis> results = findByNamedQuery(Thesis.FIND_ALL_BY_AUTHOR, Thesis.class, Collections.singletonMap("studentId", studentId));
        return results.stream().findFirst().orElse(null);
    }

    @Override
    public Thesis getThesis(Long id) {
        if (id == null)
            throw new IllegalArgumentException("Provided id must not be null");
        return findOne(id, Thesis.class);
    }

    @Override
    public Thesis updateThesis(Thesis thesis) {
        if (thesis == null)
            throw new IllegalArgumentException("Provided thesis must not be null");
        if (thesis.getId() == null)
            throw new IllegalArgumentException("Provided thesis.id must not be null");
        return update(thesis);
    }

    @Override
    public Page<Student> findStudents(Optional<String> name, Optional<String> year, Pageable pageable) {
        return null;
    }

    @Override
    public Page<Teacher> findTeachers(Optional<String> name, Optional<String> institute, Pageable pageable) {
        return null;
    }

    @Override
    public Page<Thesis> findTheses(Optional<String> department, Optional<Date> publishedOn, Optional<String> type, Optional<String> status, Pageable pageable) {
        return null;
    }


    // Some generic methods to help
    private <R> R create(Supplier<R> createFunction) {
        EntityManager manager = this.emf.createEntityManager();
        try {
            manager.getTransaction().begin();
            R object = createFunction.get();
            manager.persist(object);
            manager.getTransaction().commit();
            return object;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (manager.getTransaction().isActive())
                manager.getTransaction().rollback();
            return null;
        } finally {
            manager.close();
        }
    }

    private <R> R update(R entity) {
        EntityManager manager = this.emf.createEntityManager();
        try {
            manager.getTransaction().begin();
            manager.merge(entity);
            manager.getTransaction().commit();
            return entity;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (manager.getTransaction().isActive())
                manager.getTransaction().rollback();
            return null;
        } finally {
            manager.close();
        }
    }

    private <R> R findOne(Long id, Class<R> clazz) {
        EntityManager manager = this.emf.createEntityManager();
        try {
            return manager.find(clazz, id);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        } finally {
            manager.close();
        }
    }

    private <R> List<R> findByQuery(Function<EntityManager, TypedQuery<R>> querySupplier) {
        EntityManager manager = this.emf.createEntityManager();
        try {
            TypedQuery<R> query = querySupplier.apply(manager);
            return query.getResultList();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new ArrayList<>();
        } finally {
            manager.close();
        }
    }

    private <R> List<R> findByNamedQuery(String queryName, Class<R> clazz, Map<String, Object> parameters) {
        EntityManager manager = this.emf.createEntityManager();
        try {
            TypedQuery<R> query = manager.createNamedQuery(queryName, clazz);
            if (parameters != null && !parameters.isEmpty()) {
                parameters.forEach(query::setParameter);
            }
            return query.getResultList();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new ArrayList<>();
        } finally {
            manager.close();
        }
    }

    private <R> R delete(Long id, Class<R> clazz) {
        EntityManager manager = this.emf.createEntityManager();
        try {
            manager.getTransaction().begin();
            R entity = manager.find(clazz, id);
            if (entity == null) {
                throw new IllegalArgumentException("Cannot find entity of class '" + clazz.getSimpleName() + "' with id '" + id + "'");
            }
            manager.remove(entity);
            manager.getTransaction().commit();
            return entity;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (manager.getTransaction().isActive())
                manager.getTransaction().rollback();
            return null;
        } finally {
            manager.close();
        }
    }
}
