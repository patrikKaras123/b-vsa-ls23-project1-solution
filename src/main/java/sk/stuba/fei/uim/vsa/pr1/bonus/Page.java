package sk.stuba.fei.uim.vsa.pr1.bonus;

import java.util.stream.Stream;

public interface Page<R> {

    Stream<R> getContent();

    Pageable getPageable();

    long getTotalElements();

    int getTotalPages();

}
