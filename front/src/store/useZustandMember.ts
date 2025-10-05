import { create } from 'zustand'
import { removeCookie, setCookie } from '../util/CookieUtil'
import { loginPost } from '../api/MemberApi'

export interface MemberInfo {
  email: string
  nickname: string
  accessToken: string
  refreshToken: string
  roleNames: string[]
}

const initState: MemberInfo = {
  email: '',
  nickname: '',
  accessToken: '',
  refreshToken: '',
  roleNames: [],
}

export interface MemberStore {
  member: MemberInfo
  status: '' | 'pending' | 'fulfilled' | 'error'
  login: (email: string, pw: string) => void
  logout: () => void
  save: (member: MemberInfo) => void
}

const useZustandMember = create<MemberStore>((set) => {
  return {
    member: initState,
    status: '',

    login: async (email: string, pw: string) => {
      set({ status: 'pending' })

      try {
        const data = await loginPost(email, pw) // <- 비동기 호출
        console.log('save new data', data)

        set({ member: { ...data }, status: 'fulfilled' })
        const newState = { ...data, status: 'fulfilled' }

        setCookie('member', JSON.stringify(newState), 1) //1일
      } catch (error) {
        console.error('Login failed', error)
        set({ status: 'error' })
      }
    },
    logout: () => {
      set({ member: { ...initState }, status: '' })
      removeCookie('member')
    },
    save: (memberInfo: MemberInfo) => {
      set({ member: memberInfo, status: 'fulfilled' })
    },
  }
})

export default useZustandMember
