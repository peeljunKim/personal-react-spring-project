import { create } from 'zustand'

export interface CountState {
  current: number
  amount: number
  inc: () => void
  des: () => void
  changeAmount: (num: number) => void
}

const useZustandCount = create<CountState>((set, get) => {
  return {
    current: 999,
    amount: 1,
    inc: () => set((state) => ({ current: get().current + get().amount })),
    des: () => set((state) => ({ current: state.current - 1 })),
    changeAmount: (num: number) => set({ amount: num }),
  }
})

export default useZustandCount
