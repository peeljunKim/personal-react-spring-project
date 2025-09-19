package org.personal.project.service;

import org.personal.project.dto.PageRequestDTO;
import org.personal.project.dto.PageResponseDTO;
import org.personal.project.dto.ProductDTO;
import org.personal.project.entity.Product;
import org.personal.project.entity.ProductImage;

import java.util.List;

public interface ProductService {

    PageResponseDTO<ProductDTO> getList(PageRequestDTO pageRequestDTO);

    Long register(ProductDTO productDTO);

    ProductDTO getOne(Long pno);

    void modify(ProductDTO productDTO);

    void remove(Long pno);

    default ProductDTO entityToDTO(Product product) {

        ProductDTO productDTO = ProductDTO.builder()
                .pno(product.getPno())
                .price(product.getPrice())
                .pdesc(product.getPdesc())
                .pname(product.getPname())
                .delFlag(product.isDelFlag())
                .build();

        List<ProductImage> imageList = product.getImageList();

        if (imageList == null || imageList.isEmpty()) return productDTO;

        List<String> fileNameList = imageList.stream().map(ProductImage::getFileName).toList();

        productDTO.setUploadedFileNames(fileNameList);

        return productDTO;
    }

    default Product dtoToEntity(ProductDTO productDTO) {

        Product product = Product.builder()
                .pno(productDTO.getPno())
                .pname(productDTO.getPname())
                .pdesc(productDTO.getPdesc())
                .price(productDTO.getPrice())
                .build();

        List<String> uploadedFileNames = productDTO.getUploadedFileNames();

        if (uploadedFileNames == null || uploadedFileNames.isEmpty()) {
            return product;
        }

        uploadedFileNames.forEach(product::addImageString);

        return product;
    }
}
