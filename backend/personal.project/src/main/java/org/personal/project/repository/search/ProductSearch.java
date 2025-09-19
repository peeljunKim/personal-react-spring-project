package org.personal.project.repository.search;


import org.personal.project.dto.PageRequestDTO;
import org.personal.project.dto.PageResponseDTO;
import org.personal.project.dto.ProductDTO;

public interface ProductSearch {

    PageResponseDTO<ProductDTO> searchList(PageRequestDTO pageRequestDTO);
}
