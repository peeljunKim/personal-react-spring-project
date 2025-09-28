package org.personal.project.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.personal.project.dto.page.PageRequestDTO;
import org.personal.project.dto.page.PageResponseDTO;
import org.personal.project.dto.ProductDTO;
import org.personal.project.entity.Product;
import org.personal.project.entity.ProductImage;
import org.personal.project.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Log4j2
@Transactional
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public PageResponseDTO<ProductDTO> getList(PageRequestDTO pageRequestDTO) {

        log.info("getList..............");

        Pageable pageable = PageRequest.of(
                pageRequestDTO.getPage() - 1, //페이지 시작 번호가 0부터 시작하므로
                pageRequestDTO.getSize(),
                Sort.by("pno").descending());

        // Object[] => 0 product 1 productImage
        Page<Object[]> result = productRepository.selectList(pageable);

        List<ProductDTO> dtoList = result.get().map(arr -> {

            Product product = (Product) arr[0];
            ProductImage productImage = (ProductImage) arr[1];

            ProductDTO productDTO = ProductDTO.builder()
                    .pno(product.getPno())
                    .pname(product.getPname())
                    .pdesc(product.getPdesc())
                    .price(product.getPrice())
                    .build();

            String imageStr = productImage.getFileName(); // ord 0번만 나오는 이유는 selectList 쿼리에 0번만 나오게 where 절이 있어서
            productDTO.setUploadedFileNames(List.of(imageStr));

            return productDTO;
        }).collect(Collectors.toList());

        long count = result.getTotalElements();

        return PageResponseDTO.<ProductDTO>withAll()
                .dtoList(dtoList)
                .totalCount(count)
                .pageRequestDTO(pageRequestDTO)
                .build();
    }

    @Override
    public Long register(ProductDTO productDTO) {
        Product product = dtoToEntity(productDTO);

        log.info("--------------------");
        log.info(product);
        log.info(product.getImageList());

        Product result = productRepository.save(product);

        return result.getPno();
    }

    @Override
    public ProductDTO getOne(Long pno) {

        Optional<Product> result = productRepository.findById(pno);
        Product product = result.orElseThrow();

        return entityToDTO(product);
    }

    @Override
    public void modify(ProductDTO productDTO) {

        // 데이터 추출 및 가공
        Optional<Product> findProduct = productRepository.findById(productDTO.getPno());

        Product product = findProduct.orElseThrow();

        product.changeName(productDTO.getPname());
        product.changePrice(productDTO.getPrice());
        product.changeDesc(productDTO.getPdesc());
        product.changeDel(productDTO.isDelFlag());

        // 이미지 처리
        List<String> uploadedFileNames = productDTO.getUploadedFileNames();

        /**
         * 왜 전체 교체 전략을 사용하는가??
         *
         * 이미지 목록을 업데이트하는 경우, 다음과 같은 세 가지 시나리오가 발생할 수 있습니다.
         *
         * 1. 새로운 이미지 추가: 기존 목록에 없던 새로운 이미지를 추가
         * 2. 기존 이미지 삭제: 기존 목록에서 특정 이미지를 제거
         * 3. 이미지 순서 변경: 기존 이미지들의 순서를 바꿉니다.
         *
         * 만약 이 세 가지 시나리오를 각각 처리하려면, 서버는 클라이언트로부터 전달받은 이미지 리스트와
         * 데이터베이스에 저장된 기존 이미지 리스트를 비교하여 어떤 이미지가 추가되었고, 어떤 이미지가 삭제되었으며,
         * 순서가 어떻게 바뀌었는지 일일이 확인해야 합니다.
         *
         * 하지만 전체 교제 전략은 비교 로직을 생략하고 변경사항을 관리하는 복잡성을 크게 줄여줍니다
         */
        product.clearImageList();

        if (uploadedFileNames != null && !uploadedFileNames.isEmpty()) {
            uploadedFileNames.forEach(product::addImageString);
        }

        // 저장
        productRepository.save(product);
    }


    @Override
    public void remove(Long pno) {
        productRepository.updateToDelete(pno, true);
    }
}
