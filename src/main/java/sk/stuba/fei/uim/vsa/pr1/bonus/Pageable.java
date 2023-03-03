package sk.stuba.fei.uim.vsa.pr1.bonus;

/**
 * Implementácia musí obsahovať prázdny konštruktor.
 */
public interface Pageable {

    Pageable of(int page, int size);

    Pageable first();

    Pageable previous();

    Pageable next();

    Integer getPageNumber();

    Integer getPageSize();

}
