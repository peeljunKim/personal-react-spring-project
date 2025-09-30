import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import { getCartItems, postChangeCart } from '../api/CartApi'

export const getCartItemsAsync = createAsyncThunk('getCartItemsAsync', () => {
  return getCartItems()
})

export const postChangeCartAsync = createAsyncThunk(
  'postChangeCartAsync',
  (param: CartItemRequest) => {
    return postChangeCart(param)
  }
)

const initState: CartItemsArray = { items: [], status: '' }

const cartSlice = createSlice({
  name: 'cartSlice',
  initialState: initState,
  reducers: {},

  extraReducers: (builder) => {
    builder
      .addCase(getCartItemsAsync.fulfilled, (state, action) => {
        console.log('getCartItemsAsync fulfilled', state)
        const newState = { items: action.payload, status: 'fulfilled' }
        return newState
      })
      .addCase(getCartItemsAsync.pending, (state, action) => {
        state.status = 'pending'
      })
      .addCase(getCartItemsAsync.rejected, (state, action) => {
        state.status = 'rejected'
      })

      .addCase(postChangeCartAsync.fulfilled, (state, action) => {
        console.log('postCartItemsAsync fulfilled ', state)
        const newState = { items: action.payload, status: 'fulfilled' }
        return newState
      })
      .addCase(postChangeCartAsync.pending, (state, action) => {
        state.status = 'pending'
      })
      .addCase(postChangeCartAsync.rejected, (state, action) => {
        state.status = 'rejected'
      })
  },
})

export default cartSlice.reducer
