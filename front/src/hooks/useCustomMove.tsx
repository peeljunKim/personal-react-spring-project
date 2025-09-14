import { useCallback, useState } from 'react'
import { createSearchParams, useNavigate, useSearchParams } from 'react-router'

function useCustomMove(): UseCustomMoveReturn {
  const nav = useNavigate()

  const [queryParams] = useSearchParams()
  const [refresh, setRefresh] = useState<boolean>(false) // 동일 페이지 클릭 여부

  const pageStr: string | null = queryParams.get('page')
  const sizeStr: string | null = queryParams.get('size')

  const page: number = pageStr ? Number(pageStr) : 1
  const size: number = sizeStr ? Number(sizeStr) : 10

  // 현재 주소창 쿼리 스트링
  const queryDefault = createSearchParams({
    page: String(page),
    size: String(size),
  }).toString()

  const moveToList = (pageParam?: PageParam) => {
    let queryStr = '' // 이동할 페이지의 쿼리 스트링

    if (pageParam) {
      const pageNum = Number(pageParam.page) || 1
      const sizeNum = Number(pageParam.size) || 10
      queryStr = createSearchParams({
        page: String(pageNum),
        size: String(sizeNum),
      }).toString()

      if (queryStr === queryDefault) {
        setRefresh(!refresh)
      }
    } else {
      queryStr = queryDefault
    }

    nav({ pathname: `../list`, search: queryStr })
  }

  const moveToModify = useCallback(
    (tno: number) => {
      console.log(queryDefault)
      nav({ pathname: `../modify/${tno}`, search: queryDefault })
    },
    [page, size]
  )

  const moveToRead = (tno: number) => {
    nav({
      pathname: `../read/${tno}`,
      search: queryDefault,
    })
  }

  return { page, size, moveToList, moveToModify, moveToRead, refresh }
}

export default useCustomMove
