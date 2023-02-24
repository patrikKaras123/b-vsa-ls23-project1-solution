package sk.stuba.fei.uim.vsa.pr1.bonus;


import java.util.Date;
import java.util.Optional;

public interface PageableThesisService<S, T, A> {

    Page<S> findStudents(Optional<String> name, Optional<String> year, Pageable pageable);

    Page<T> findTeachers(Optional<String> name, Optional<String> institute, Pageable pageable);

    Page<A> findTheses(Optional<String> department, Optional<Date> publishedOn, Optional<String> type, Optional<String> status, Pageable pageable);

}
