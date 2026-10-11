import { configureStore, createSlice } from "@reduxjs/toolkit";


const saveusertolocalstorage = (user) => {
  if (typeof window !== "undefined" && localStorage) {
    const safeUser = { ...(user || {}) };
    delete safeUser.password;
    localStorage.setItem("user", JSON.stringify(safeUser));
  }
};
const initialState = {
  user: null,
};

const userSlice = createSlice({
  name: "user",
  initialState,
  reducers: {
    setUser: (state, action) => {
      const safeUser = { ...(action.payload || {}) };
      delete safeUser.password;
      state.user = safeUser;
      saveusertolocalstorage(safeUser);
    },
    clearUser: (state) => {
      state.user = null;
      if (typeof window !== "undefined" && localStorage) {
        localStorage.removeItem("user");
      }
    },
  },
});

export const { setUser, clearUser } = userSlice.actions;

const store = configureStore({
  reducer: {
    user: userSlice.reducer,
  },
});

export default store;
