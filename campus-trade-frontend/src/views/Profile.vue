<template>
  <div class="profile-page">
    <el-row :gutter="24">
      <!-- 个人信息 -->
      <el-col :span="12">
        <el-card shadow="never">
          <h2>个人信息</h2>
          <el-form :model="profileForm" ref="profileRef" label-width="100px" style="margin-top:20px;max-width:400px">
            <el-form-item label="用户名">
              <el-input :model-value="userStore.userInfo?.username" disabled />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="profileForm.nickname" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="profileForm.email" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="profileForm.phone" maxlength="11" />
            </el-form-item>
            <el-form-item label="角色">
              <el-tag :type="userStore.isAdmin ? 'danger' : 'primary'" size="small">
                {{ userStore.isAdmin ? '管理员' : '普通用户' }}
              </el-tag>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="saveProfile">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 修改密码 -->
      <el-col :span="12">
        <el-card shadow="never">
          <h2>修改密码</h2>
          <el-form :model="pwdForm" :rules="pwdRules" ref="pwdRef" label-width="100px" style="margin-top:20px;max-width:400px">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwdForm.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="changingPwd" @click="changePassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { ElMessage } from "element-plus";
import { useUserStore } from "../stores/user";
import { getProfile, updateProfile } from "../api/user";
import axios from "axios";

const userStore = useUserStore();
const profileRef = ref(null);
const pwdRef = ref(null);
const saving = ref(false);
const changingPwd = ref(false);

const profileForm = reactive({
  nickname: "",
  email: "",
  phone: "",
});

onMounted(async () => {
  try {
    const res = await getProfile();
    const data = res.data;
    profileForm.nickname = data.nickname || "";
    profileForm.email = data.email || "";
    profileForm.phone = data.phone || "";
  } catch (e) {}
});

async function saveProfile() {
  saving.value = true;
  try {
    await updateProfile(profileForm);
    // 更新本地 store
    userStore.userInfo.nickname = profileForm.nickname;
    userStore.userInfo.email = profileForm.email;
    userStore.userInfo.phone = profileForm.phone;
    localStorage.setItem("userInfo", JSON.stringify(userStore.userInfo));
    ElMessage.success("保存成功");
  } catch (e) {}
  finally { saving.value = false; }
}

const pwdForm = reactive({ oldPassword: "", newPassword: "", confirmPassword: "" });
const pwdRules = {
  oldPassword: [{ required: true, message: "请输入原密码", trigger: "blur" }],
  newPassword: [{ required: true, min: 6, message: "密码至少6位", trigger: "blur" }],
  confirmPassword: [{ required: true, validator: (rule, val, cb) => val === pwdForm.newPassword ? cb() : cb(new Error("两次密码不一致")), trigger: "blur" }],
};

async function changePassword() {
  const valid = await pwdRef.value?.validate().catch(() => {});
  if (!valid) return;
  changingPwd.value = true;
  try {
    await axios.put("/api/user/password", { oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword });
    ElMessage.success("密码修改成功，请重新登录");
    pwdForm.oldPassword = "";
    pwdForm.newPassword = "";
    pwdForm.confirmPassword = "";
    userStore.logout();
  } catch (e) {}
  finally { changingPwd.value = false; }
}
</script>

<style scoped>
.profile-page { max-width: 1000px; margin: 0 auto; }
.profile-page h2 { font-size: 20px; color: var(--el-text-color-primary); }
</style>
