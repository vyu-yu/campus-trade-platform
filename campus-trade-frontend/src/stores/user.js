import { defineStore } from "pinia";
import { ref, computed } from "vue";
import { login as loginApi, register as registerApi, getProfile } from "../api/user";

export const useUserStore = defineStore("user", () => {
  const token = ref(localStorage.getItem("token") || "");
  const userInfo = ref(JSON.parse(localStorage.getItem("userInfo") || "null"));

  const isLoggedIn = computed(() => !!token.value);
  const isAdmin = computed(() => userInfo.value?.role === "ADMIN");

  function setUserInfo(info) {
    userInfo.value = info;
    localStorage.setItem("userInfo", JSON.stringify(info));
  }

  async function login(loginData) {
    const res = await loginApi(loginData);
    token.value = res.data.token;
    userInfo.value = res.data.userInfo;
    localStorage.setItem("token", res.data.token);
    localStorage.setItem("userInfo", JSON.stringify(res.data.userInfo));
  }

  async function register(registerData) {
    await registerApi(registerData);
  }

  function logout() {
    token.value = "";
    userInfo.value = null;
    localStorage.removeItem("token");
    localStorage.removeItem("userInfo");
  }

  return { token, userInfo, isLoggedIn, isAdmin, login, register, logout, setUserInfo };
});
