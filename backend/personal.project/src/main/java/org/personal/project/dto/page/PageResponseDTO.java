package org.personal.project.dto.page;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Data
public class PageResponseDTO<E> {

    private List<E> dtoList; // dto 목록

    private List<Integer> pageNumList; // 페이지 목록

    private PageRequestDTO pageRequestDTO; // 검색 조건

    private boolean prev, next; // 이전, 다음 페이지 존재 여부

    private int totalCount, prevPage, nextPage, totalPage, current;

    @Builder(builderMethodName = "withAll")
    public PageResponseDTO(List<E> dtoList, PageRequestDTO pageRequestDTO, long totalCount) {

        this.dtoList = dtoList;
        this.pageRequestDTO = pageRequestDTO;
        this.totalCount = (int) totalCount;

        int end = (int) (Math.ceil(pageRequestDTO.getPage() / 10.0)) * 10;

        int start = end - 9;

        int last = (int) (Math.ceil((totalCount / (double) pageRequestDTO.getSize())));

        end = end > last ? last : end;

        this.prev = start > 1;


        this.next = totalCount > end * pageRequestDTO.getSize();

        this.pageNumList = IntStream.rangeClosed(start, end).boxed().collect(Collectors.toList());

        if (prev) {
            this.prevPage = start - 1;
        }

        if (next) {
            this.nextPage = end + 1;
        }

        this.totalPage = this.pageNumList.size();
        this.current = pageRequestDTO.getPage();

    }

    @Builder(builderMethodName = "withSlice")
    public PageResponseDTO(List<E> dtoList, PageRequestDTO pageRequestDTO, boolean hasNext) {

        this.dtoList = dtoList;
        this.pageRequestDTO = pageRequestDTO;
        this.totalCount = -1;

        int currentPage = pageRequestDTO.getPage();
        this.current = currentPage;

        this.prev = currentPage > 1;
        if (this.prev) {
            this.prevPage = currentPage - 1;
        }

        this.next = hasNext;
        if (this.next) {
            this.nextPage = currentPage + 1;
        }

        this.pageNumList = List.of(currentPage);
        this.totalPage = this.pageNumList.size();
    }
}
