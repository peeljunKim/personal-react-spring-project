package org.personal.project.repository.search;

import com.querydsl.jpa.JPQLQuery;
import lombok.extern.log4j.Log4j2;
import org.personal.project.dto.page.PageRequestDTO;
import org.personal.project.dto.page.PageResponseDTO;
import org.personal.project.dto.ProductDTO;
import org.personal.project.entity.Product;
import org.personal.project.entity.QProduct;
import org.personal.project.entity.QProductImage;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

@Log4j2
public class ProductSearchImpl extends QuerydslRepositorySupport implements ProductSearch {

    public ProductSearchImpl() {
        super(Product.class);
    }

    @Override
    public PageResponseDTO<ProductDTO> searchList(PageRequestDTO pageRequestDTO) {

        log.info("-------ProductSearchImpl  searchList ---------");

        Pageable pageable = PageRequest.of(
                pageRequestDTO.getPage() - 1,
                pageRequestDTO.getSize(),
                Sort.by("pno").descending());

        QProduct product = QProduct.product;
        QProductImage productImage = QProductImage.productImage;

        JPQLQuery<Product> query = from(product);
        query.leftJoin(product.imageList, productImage); // 엘리먼트 컬렉션으로 queryDSL 사용 시 주의 사항(매개변수)
        query.where(productImage.ord.eq(0));

        this.getQuerydsl().applyPagination(pageable, query);

        List<Product> productList = query.fetch();
//        List<Tuple> productList = query.select(product, productImage).fetch(); // select 절을 사용하면 제네릭에 Tuple이 들어감
        long count = query.fetchCount();

//        log.info("------------------------------------");
//        log.info(productList);

        return null;
    }
}
