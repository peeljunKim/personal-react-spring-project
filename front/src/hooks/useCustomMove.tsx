import { useCallback } from 'react'
import { createSearchParams, useNavigate, useSearchParams } from 'react-router'

function useCustomMove(): UseCustomMoveReturn {
  const nav = useNavigate()

  const [queryParams] = useSearchParams()

  const pageStr: string | null = queryParams.get('page')
  const sizeStr: string | null = queryParams.get('size')

  const page: number = pageStr ? Number(pageStr) : 1
  const size: number = sizeStr ? Number(sizeStr) : 10

  const queryDefault = createSearchParams({
    page: String(page),
    size: String(size),
  }).toString()

  const moveToList = (pageParam?: PageParam) => {
    let queryStr = ''

    if (pageParam) {
      const pageNum = Number(pageParam.page) || 1
      const sizeNum = Number(pageParam.size) || 10
      queryStr = createSearchParams({
        page: String(pageNum),
        size: String(sizeNum),
      }).toString()
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

  return { page, size, moveToList, moveToModify, moveToRead }
}

export default useCustomMove
