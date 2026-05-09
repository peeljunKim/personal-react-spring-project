package org.personal.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


/**
 * 상품 등록, 조회할 때 사용
 * <p>
 * 등록:
 * 조회: uploadedFileNames
 */
@Builder
@AllArgsConstructor
@Data
@NoArgsConstructor
public class ProductDTO {

    private Long pno;
    private String pname;
    private int price;
    private int stock;
    private String pdesc; // 설명
    private boolean delFlag; // 삭제 여부
    private LocalDateTime createdAt;


    @Builder.Default
    private List<MultipartFile> files = new ArrayList<>();

    @Builder.Default
    private List<String> uploadedFileNames = new ArrayList<>();
}
