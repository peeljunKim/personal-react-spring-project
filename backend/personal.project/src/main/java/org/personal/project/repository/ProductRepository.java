package org.personal.project.repository;

import org.personal.project.entity.Product;
import org.personal.project.repository.search.ProductSearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductSearch {

    @EntityGraph(attributePaths = "imageList")
    @Query("select p from Product p where p.pno = :pno")
    Optional<Product> selectOne(@Param("pno") Long pno);

    @Modifying
    @Query("update Product p set p.delFlag = :flag where p.pno = :pno")
    void updateToDelete(@Param("pno") Long pno, @Param("flag") boolean flag);

//    @Query("select p, pi from Product p left join p.imageList pi where pi.ord = 0 and p.delFlag = false")
    @Query("select " +
            "p, pi " +
            "from Product p " +
            "left join p.imageList pi " +
            "where pi.ord = 0 and " +
            "p.delFlag = false " +
            "order by p.createdAt desc, p.pno desc")
    Page<Object[]> selectList(Pageable pageable);


//    @Query("select p, pi from Product p left join p.imageList pi where pi.ord = 0 and p.delFlag = false")
//    Slice<Object[]> selectListWithoutCount(Pageable pageable);

    @Query("select " +
            "p, pi " +
            "from Product p " +
            "left join p.imageList pi " +
            "where pi.ord = 0 and " +
            "p.delFlag = false " +
            "order by p.createdAt desc, p.pno desc")
    List<Object[]> selectListWithoutCount(Pageable pageable);

    @Query("select p, pi " +
            "from Product p " +
            "left join p.imageList pi where pi.ord = 0 " +
            "and p.delFlag = false and " +
            "(:cursorCreatedAt is null or (p.createdAt < :cursorCreatedAt or (p.createdAt = :cursorCreatedAt and p.pno < :cursorId))) " +
            "order by p.createdAt desc, p.pno desc")
    List<Object[]> selectSeekByCursor(@Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
                                      @Param("cursorId") Long cursorId,
                                      Pageable pageable);

}
