// .d는 선언 파일을 의미하며, 타입 정의만 담고 있는 파일
interface PageParam {
  page?: string | number
  size?: string | number
}

interface UseCustomMoveReturn {
  moveToList: (pageParam?: PageParam) => void
  moveToModify: (tno: number) => void
  moveToRead: (tno: number) => void
  page: number
  size: number
}

interface PageRequestDTO {
  page: number
  size: number
}

interface PageResponseDTO<T> {
  dtoList: T[] // 제네릭 배열
  pageNumList: number[] // 페이지 번호 목록 배열
  pageRequestDTO: PageRequestDTO | null // PageRequestDTO 객체 또는 null
  prev: boolean // 이전 페이지 존재 여부 (boolean)
  next: boolean // 다음 페이지 존재 여부 (boolean)
  totalCount: number // 전체 항목 개수
  prevPage: number // 이전 페이지 번호
  nextPage: number // 다음 페이지 번호
  totalPage: number // 전체 페이지 개수
  current: number // 현재 페이지 번호
}
