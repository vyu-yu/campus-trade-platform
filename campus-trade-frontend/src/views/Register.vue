<template>
  <div class="auth-page">
    <div class="pattern-overlay"></div>
    <el-card class="auth-card" shadow="always">
      <h2 class="auth-title">校园二手交易平台</h2>
      <p class="auth-subtitle">用户注册</p>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="0" size="large">
        <el-form-item prop="phone">
          <el-input v-model="form.phone" placeholder="手机号" prefix-icon="Iphone" maxlength="11" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码（至少6位）" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="确认密码" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="auth-btn" :loading="loading" @click="handleRegister">
            注册
          </el-button>
        </el-form-item>
      </el-form>
      <div class="auth-footer">
        已有账号？<router-link to="/login">立即登录</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { useUserStore } from "../stores/user";

const router = useRouter();
const userStore = useUserStore();
const formRef = ref(null);
const loading = ref(false);
const form = reactive({ phone: "", password: "", confirmPassword: "" });
const validatePass2 = (rule, value, callback) => {
  if (value !== form.password) callback(new Error("两次密码输入不一致"));
  else callback();
};
const rules = {
  phone: [
    { required: true, message: "请输入手机号", trigger: "blur" },
    { pattern: /^1[3-9]\d{9}$/, message: "手机号格式不正确", trigger: "blur" },
  ],
  password: [{ required: true, min: 6, message: "密码至少6位", trigger: "blur" }],
  confirmPassword: [{ required: true, validator: validatePass2, trigger: "blur" }],
};

async function handleRegister() {
  const valid = await formRef.value.validate().catch(() => {});
  if (!valid) return;
  loading.value = true;
  try {
    await userStore.register({ phone: form.phone, password: form.password });
    ElMessage.success("注册成功，请登录");
    router.push("/login");
   } catch (e) { ElMessage.error(e?.response?.data?.message || e?.message || "注册失败"); }
  finally { loading.value = false; }
}
</script>

<style scoped>
.auth-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 64px);
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #FAFAF8 0%, #ECFDF5 40%, #D1FAE5 70%, #FAFAF8 100%);
}
/* Decorative floating elements */
.auth-page::before {
  content: "";
  position: absolute;
  width: 500px;
  height: 500px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(5, 150, 105, 0.10) 0%, transparent 70%);
  top: -120px;
  right: -80px;
  animation: float-up 8s ease-in-out infinite;
  pointer-events: none;
}
.auth-page::after {
  content: "";
  position: absolute;
  width: 400px;
  height: 400px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(217, 119, 6, 0.08) 0%, transparent 70%);
  bottom: -100px;
  left: -80px;
  animation: float-down 10s ease-in-out infinite;
  pointer-events: none;
}
@keyframes float-up {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(30px, -30px) scale(1.05); }
}
@keyframes float-down {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(-20px, 20px) scale(1.08); }
}
/* Subtle dot pattern overlay */
.auth-page .pattern-overlay {
  position: absolute;
  inset: 0;
  background-image: radial-gradient(rgba(5, 150, 105, 0.06) 1px, transparent 1px);
  background-size: 24px 24px;
  pointer-events: none;
}
.auth-card {
  width: 420px;
  border-radius: 12px;
  box-shadow: 0 8px 32px 0 rgba(0, 0, 0, 0.06), 0 2px 8px -4px rgba(0, 0, 0, 0.08);
  padding: 24px;
  position: relative;
  z-index: 1;
  backdrop-filter: blur(4px);
  background: rgba(255, 255, 255, 0.95);
}
.auth-title { text-align: center; color: var(--el-color-primary); margin-bottom: 4px; font-size: 22px; }
.auth-subtitle { text-align: center; color: var(--el-text-color-secondary); margin-bottom: 32px; font-size: 14px; }
.auth-btn { width: 100%; border-radius: var(--el-border-radius-base); }
.auth-footer { text-align: center; font-size: 14px; color: var(--el-text-color-secondary); margin-top: 8px; }
.auth-footer a { color: var(--el-color-primary); text-decoration: none; font-weight: 500; }
</style>
