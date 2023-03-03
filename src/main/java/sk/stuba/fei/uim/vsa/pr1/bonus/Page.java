package sk.stuba.fei.uim.vsa.pr1.bonus;

import java.util.List;

/**
 * Implementácia musí obsahovať prázdny konštruktor.
 */
public interface Page<R> {

    List<R> getContent();

    Pageable getPageable();

    Long getTotalElements();

    void setTotalElements(Long totalElements);

    int getTotalPages();

}
