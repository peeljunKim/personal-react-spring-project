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
