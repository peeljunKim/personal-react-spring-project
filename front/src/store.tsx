import { configureStore } from '@reduxjs/toolkit'
import loginSlice from './slices/LoginSlice.tsx'
import cartSlice from './slices/CartSlice.tsx'

const store = configureStore({
  reducer: {
    loginSlice: loginSlice,
    cartSlice: cartSlice,
  },
})

export type AppDispatch = typeof store.dispatch
export type RootState = ReturnType<typeof store.getState>

export default store
