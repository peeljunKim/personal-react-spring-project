import { useEffect, useState, type ChangeEvent } from 'react'
import { deleteOne, getOne, putOne } from '../../api/TodoApi'
import useCustomMove from '../../hooks/useCustomMove'
import ResultModal from '../common/ResultModal'

const initState = {
  tno: 0,
  title: '',
  writer: '',
  dueDate: null,
  complete: false,
}

const ModifyComponent = ({ tno }: { tno: number }) => {
  const [todo, setTodo] = useState({ ...initState })
  const [result, setResult] = useState<string | null>(null)
  const { moveToList, moveToRead } = useCustomMove()

  useEffect(() => {
    getOne(tno).then((data) => setTodo(data))
  }, [tno])

  const handleChangeTodo = (e: ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target // name을 사용하여 동적으로 상태를 업데이트

    setTodo((prevState) => ({
      ...prevState,
      [name]: value, // name을 키로 사용하여 값 업데이트
    }))
  }

  const handleChangeTodoComplete = (e: ChangeEvent<HTMLSelectElement>) => {
    const value = e.target.value
    todo.complete = value === 'Y'

    setTodo({ ...todo })
  }

  const handleClickModify = () => {
    const todoModify: TodoModify = {
      tno: todo.tno,
      title: todo.title,
      dueDate: todo.dueDate,
      complete: todo.complete,
    }

    putOne(todo).then((data) => {
      console.log('modify result: ' + data)
      setResult('Modified')
    })
  }

  const handleClickDelete = () => {
    deleteOne(tno).then((data) => {
      console.log('delete result: ' + data)
      setResult('Deleted')
    })
  }

  const closeModal = () => {
    if (result === 'Deleted') {
      moveToList()
    } else {
      moveToRead(tno)
    }
  }

  return (
    <div className="border-2 border-sky-200 mt-10 m-2 p-4">
      {result && (
        <ResultModal
          title={'처리결과'}
          content={result}
          callbackFn={closeModal}
        ></ResultModal>
      )}

      <div className="flex justify-center mt-10">
        <div className="relative mb-4 flex w-full flex-wrap items-stretch">
          <div className="w-1/5 p-6 text-right font-bold">TNO</div>
          <div className="w-4/5 p-6 rounded-r border border-solid shadow-md bg-gray-100">
            {todo.tno}
          </div>
        </div>
      </div>
      <div className="flex justify-center">
        <div className="relative mb-4 flex w-full flex-wrap items-stretch">
          <div className="w-1/5 p-6 text-right font-bold">WRITER</div>
          <div className="w-4/5 p-6 rounded-r border border-solid shadow-md bg-gray-100">
            {todo.writer}
          </div>
        </div>
      </div>
      <div className="flex justify-center">
        <div className="relative mb-4 flex w-full flex-wrap items-stretch">
          <div className="w-1/5 p-6 text-right font-bold">TITLE</div>
          <input
            className="w-4/5 p-6 rounded-r border border-solid border-neutral-300 shadow-md"
            name="title"
            type={'text'}
            value={todo.title}
            onChange={handleChangeTodo}
          ></input>
        </div>
      </div>
      <div className="flex justify-center">
        <div className="relative mb-4 flex w-full flex-wrap items-stretch">
          <div className="w-1/5 p-6 text-right font-bold">DUEDATE</div>
          <input
            className="w-4/5 p-6 rounded-r border border-solid border-neutral-300 shadow-md"
            name="dueDate"
            type={'date'}
            value={todo.dueDate || ''}
            onChange={handleChangeTodo}
          ></input>
        </div>
      </div>
      <div className="flex justify-center">
        <div className="relative mb-4 flex w-full flex-wrap items-stretch">
          <div className="w-1/5 p-6 text-right font-bold">COMPLETE</div>
          <select
            name="status"
            className="border-solid border-2 rounded m-1 p-2"
            onChange={handleChangeTodoComplete}
            value={todo.complete ? 'Y' : 'N'}
          >
            <option value="Y">Completed</option>
            <option value="N">Not Yet</option>
          </select>
        </div>
      </div>

      <div className="flex justify-end p-4">
        <button
          type="button"
          className="inline-block rounded p-4 m-2 text-xl w-32 text-white bg-red-500"
          onClick={handleClickDelete}
        >
          Delete
        </button>
        <button
          type="button"
          className="rounded p-4 m-2 text-xl w-32 text-white bg-blue-500"
          onClick={handleClickModify}
        >
          Modify
        </button>
      </div>
    </div>
  )
}
export default ModifyComponent
