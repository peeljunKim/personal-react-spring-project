package org.personal.project.dto.page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class PageRequestDTO {

    @Builder.Default
    private int page = 1;

    @Builder.Default
    private int size = 10;
}

/**
 * PageRequestDTO, PageResponseDTO 페이징이 필요한 모든 애들한테 사용할 수 있습니다 상속을 통해서
 * PageRequestDTO은 상속이 맞습니다. 검색 조건이 다양해져서 ex) 제목, 내용, 타입, 카테고리
 * PageResponseDTO은 모든 페이징 기능에서 비슷하다
 */
