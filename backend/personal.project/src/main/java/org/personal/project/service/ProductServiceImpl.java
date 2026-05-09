package org.personal.project.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.personal.project.dto.page.OffsetLimitPageRequest;
import org.personal.project.dto.page.PageRequestDTO;
import org.personal.project.dto.page.PageResponseDTO;
import org.personal.project.dto.ProductDTO;
import org.personal.project.entity.Product;
import org.personal.project.entity.ProductImage;
import org.personal.project.repository.ProductRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Log4j2
@Transactional
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private static final int MAX_OFFSET_PAGE = 1000;
    private static final int MAX_PAGE_SIZE = 100;

    private final ProductRepository productRepository;

    private ProductDTO convertToProductDTO(Object[] arr) {
        Product product = (Product) arr[0];
        ProductImage productImage = (ProductImage) arr[1];

        ProductDTO productDTO = ProductDTO.builder()
                .pno(product.getPno())
                .pname(product.getPname())
                .pdesc(product.getPdesc())
                .price(product.getPrice())
                .stock(product.getStock())
                .createdAt(product.getCreatedAt())
                .build();

        String imageStr = productImage.getFileName(); // ord 0번만 나오는 이유는 selectList 쿼리에 0번만 나오게 where 절이 있어서
        productDTO.setUploadedFileNames(List.of(imageStr));

        return productDTO;
    }

    @Override
    public PageResponseDTO<ProductDTO> getList(PageRequestDTO pageRequestDTO) {

        // 1. count 쿼리를 꼭 써야 하는 경우
        if (pageRequestDTO.isCount()) {
            Pageable pageable = PageRequest.of(
                    pageRequestDTO.getPage() - 1,
                    pageRequestDTO.getSize()
            );

            Page<Object[]> result = productRepository.selectList(pageable);

            List<ProductDTO> dtoList = result.getContent()
                    .stream()
                    .map(this::convertToProductDTO)
                    .collect(Collectors.toList());

            return PageResponseDTO.<ProductDTO>withAll()
                    .dtoList(dtoList)
                    .totalCount(result.getTotalElements())
                    .pageRequestDTO(pageRequestDTO)
                    .build();
        }

        // 2. count 없이 next 여부만 알고 싶은 경우
        int safePage = Math.max(pageRequestDTO.getPage(), 1);
        int requestedSize = pageRequestDTO.getSize();

        if (requestedSize <= 0 || requestedSize > MAX_PAGE_SIZE) {
            requestedSize = Math.min(Math.max(requestedSize, 1), MAX_PAGE_SIZE);
        }

        pageRequestDTO.setPage(safePage);
        pageRequestDTO.setSize(requestedSize);

        int currentPage = pageRequestDTO.getPage();

        long offsetRows = (long) (currentPage - 1) * requestedSize;
        // page > 1000 이면 OFFSET은 쓰지 말고 cursor 기반으로 강제
//        if (currentPage > MAX_OFFSET_PAGE) {
        if (offsetRows >= MAX_OFFSET_PAGE) {
            if (!pageRequestDTO.hasCursor()) {
                // 필요에 따라 예외가 아니라, log.warn 후 buildOffsetPage(...)로 강제 진행해도 됩니다.
                throw new IllegalArgumentException(
                        "1000 페이지 이후에는 cursor 기반 페이징을 사용해야 합니다."
                );
            }

            // cursor 기준으로 keyset(seek) 페이징
            return buildSeekPage(pageRequestDTO);
        }

        // cursor 값이 있으면 page 가 작더라도 seek 방식 사용
        if (pageRequestDTO.hasCursor()) {
            return buildSeekPage(pageRequestDTO);
        }

        // page <= 1000 이고 cursor 없음 → OFFSET + (size + 1) 방식
        return buildOffsetPage(pageRequestDTO);
    }


//    @Override
//    public PageResponseDTO<ProductDTO> getList(PageRequestDTO pageRequestDTO) {
//
////        log.info("getList..............");
//
////        버전1, 2 동일
////        Pageable pageable = PageRequest.of(
////                pageRequestDTO.getPage() - 1, //페이지 시작 번호가 0부터 시작하므로
////                pageRequestDTO.getSize(),
////                Sort.by("pno").descending());
//
//        // 버전1 : 기본적인 페이징
////        Page<Object[]> result = productRepository.selectList(pageable);
////
////        List<ProductDTO> dtoList = result.get().map(arr -> {
////
////            Product product = (Product) arr[0];
////            ProductImage productImage = (ProductImage) arr[1];
////
////            ProductDTO productDTO = ProductDTO.builder()
////                    .pno(product.getPno())
////                    .pname(product.getPname())
////                    .pdesc(product.getPdesc())
////                    .price(product.getPrice())
////                    .build();
////
////            String imageStr = productImage.getFileName(); // ord 0번만 나오는 이유는 selectList 쿼리에 0번만 나오게 where 절이 있어서
////            productDTO.setUploadedFileNames(List.of(imageStr));
////
////            return productDTO;
////        }).collect(Collectors.toList());
////
////        long count = result.getTotalElements();
////
////        return PageResponseDTO.<ProductDTO>withAll()
////                .dtoList(dtoList)
////                .totalCount(count)
////                .pageRequestDTO(pageRequestDTO)
////                .build();
//
//        // 버전2: content, count 쿼리 분리
////        if (!pageRequestDTO.isCount()) {
////            Page<Object[]> result = productRepository.selectList(pageable);
////
////            List<ProductDTO> dtoList = result.getContent()
////                    .stream()
////                    .map(this::convertToProductDTO)
////                    .collect(Collectors.toList());
////
////            long count = result.getTotalElements();
////
////            return PageResponseDTO.<ProductDTO>withAll()
////                    .dtoList(dtoList)
////                    .totalCount(count)
////                    .pageRequestDTO(pageRequestDTO)
////                    .build();
////        }
////
////
////        Slice<Object[]> result = productRepository.selectListWithoutCount(pageable);
////
////        List<ProductDTO> dtoList = result.getContent()
////                .stream()
////                .map(this::convertToProductDTO)
////                .collect(Collectors.toList());
////
////        return PageResponseDTO.<ProductDTO>withSlice()
////                .dtoList(dtoList)
////                .pageRequestDTO(pageRequestDTO)
////                .hasNext(result.hasNext())
////                .build();
//
////        Sort sort = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("pno"));
//
//        if (pageRequestDTO.isCount()) {
//            Pageable pageable = PageRequest.of(
//                    pageRequestDTO.getPage() - 1,
//                    pageRequestDTO.getSize());
//
//            Page<Object[]> result = productRepository.selectList(pageable);
//
//            List<ProductDTO> dtoList = result.getContent()
//                    .stream()
//                    .map(this::convertToProductDTO)
//                    .collect(Collectors.toList());
//
//            long count = result.getTotalElements();
//
//            return PageResponseDTO.<ProductDTO>withAll()
//                    .dtoList(dtoList)
//                    .totalCount(count)
//                    .pageRequestDTO(pageRequestDTO)
//                    .build();
//        }
//
//        if (pageRequestDTO.hasCursor()) {
////            return buildSeekPage(pageRequestDTO, sort);
//            return buildSeekPage(pageRequestDTO);
//        }
//

    /// /        return buildOffsetPage(pageRequestDTO, sort);
//        return buildOffsetPage(pageRequestDTO);
//    }
    @Override
    public Long register(ProductDTO productDTO) {
        Product product = dtoToEntity(productDTO);

//        log.info("--------------------");
//        log.info(product);
//        log.info(product.getImageList());

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
        product.changeStock(productDTO.getStock());
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

    //    private PageResponseDTO<ProductDTO> buildOffsetPage(PageRequestDTO pageRequestDTO, Sort sort) {
    private PageResponseDTO<ProductDTO> buildOffsetPage(PageRequestDTO pageRequestDTO) {

        int size = pageRequestDTO.getSize();
        int page = pageRequestDTO.getPage();
//        int offset = (page - 1) * size;

//        Pageable pageable = PageRequest.of(pageRequestDTO.getPage() - 1, size + 1, sort);
//        Pageable pageable = PageRequest.of(pageRequestDTO.getPage() - 1, size + 1); // 문제가 되는 코드
        // DB에서 11개 옴 그중 앞의 10개만 사용자에게 보여줌 그래서 문제가 됨
        Pageable pageable = OffsetLimitPageRequest.of(page - 1, size, null);

        List<Object[]> rows = productRepository.selectListWithoutCount(pageable);
        SeekWindow window = SeekWindow.from(rows, size);

        List<ProductDTO> dtoList = window.rows()
                .stream()
                .map(this::convertToProductDTO)
                .collect(Collectors.toList());

        return PageResponseDTO.<ProductDTO>withCursor()
                .dtoList(dtoList)
                .pageRequestDTO(pageRequestDTO)
                .hasNextPage(window.hasNext())
                .nextCursorId(window.nextCursorId())
                .nextCursorCreatedAt(window.nextCursorCreatedAt())
                .build();
    }


    //    private PageResponseDTO<ProductDTO> buildSeekPage(PageRequestDTO pageRequestDTO, Sort sort) {
    private PageResponseDTO<ProductDTO> buildSeekPage(PageRequestDTO pageRequestDTO) {
//        log.info("buildSeekPage 실행");
        int size = pageRequestDTO.getSize();

//    Pageable pageable = PageRequest.of(0, size + 1, sort);
//        Pageable pageable = OffsetLimitPageRequest.of(pageRequestDTO.getPage() - 1, size);
        Pageable pageable = PageRequest.of(0, size + 1);

        List<Object[]> rows = productRepository.selectSeekByCursor(
                pageRequestDTO.getCursorCreatedAt(), // 이전 페이지 마지막 row 의 createdAt
                pageRequestDTO.getCursorId(), // 이전 페이지 마지막 row 의 pno
                pageable);

        SeekWindow window = SeekWindow.from(rows, size);

        List<ProductDTO> dtoList = window.rows()
                .stream()
                .map(this::convertToProductDTO)
                .collect(Collectors.toList());

        return PageResponseDTO.<ProductDTO>withCursor()
                .dtoList(dtoList)
                .pageRequestDTO(pageRequestDTO)
                .hasNextPage(window.hasNext())
                .nextCursorId(window.nextCursorId())
                .nextCursorCreatedAt(window.nextCursorCreatedAt())
                .build();
    }

    private record SeekWindow(List<Object[]> rows,
                              boolean hasNext,
                              Long nextCursorId,
                              LocalDateTime nextCursorCreatedAt) {

        static SeekWindow from(List<Object[]> source, int size) {
            if (source == null || source.isEmpty()) {
                return new SeekWindow(Collections.emptyList(), false, null, null);
            }

            boolean hasNext = source.size() > size;
            List<Object[]> window = hasNext ? source.subList(0, size) : source;

            Long nextCursorId = null;
            LocalDateTime nextCursorCreatedAt = null;

            if (hasNext && !window.isEmpty()) {
                Product lastProduct = (Product) window.get(window.size() - 1)[0];
                nextCursorId = lastProduct.getPno();
                nextCursorCreatedAt = lastProduct.getCreatedAt();
            }

            return new SeekWindow(window, hasNext, nextCursorId, nextCursorCreatedAt);
        }
    }
}
