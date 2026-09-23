<template>
  <div class="admin-page">
    <h2>用户管理</h2>
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="搜索用户名/昵称" clearable style="width:250px" @clear="fetchData" @keyup.enter="fetchData" />
      <el-button type="primary" @click="fetchData">搜索</el-button>
    </div>
    <el-table :data="users" v-loading="loading" stripe style="width:100%;margin-top:15px">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="nickname" label="昵称" width="120" />
      <el-table-column prop="email" label="邮箱" min-width="180" />
      <el-table-column prop="role" label="角色" width="80">
        <template #default="{ row }">
          <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'primary'" size="small">{{ row.role === 'ADMIN' ? '管理员' : '用户' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="160" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">{{ row.status === 1 ? '禁用' : '启用' }}</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination background layout="prev,pager,next" :total="total" :page-size="pageSize" v-model:current-page="currentPage" @current-change="fetchData" style="margin-top:15px" />
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import request from "../api/request";

const users = ref([]);
const loading = ref(false);
const total = ref(0);
const currentPage = ref(1);
const pageSize = 20;
const keyword = ref("");

onMounted(() => fetchData());

async function fetchData() {
  loading.value = true;
  try {
    const res = await request.get("/admin/users", { params: { page: currentPage.value, pageSize, keyword: keyword.value || undefined } });
    users.value = res.data?.records || [];
    total.value = res.data?.total || 0;
  } catch (e) {}
  finally { loading.value = false; }
}

async function toggleStatus(row) {
  try {
    await ElMessageBox.confirm(row.status === 1 ? "确定禁用该用户？" : "确定启用该用户？", "提示");
    await request.put("/admin/users/" + row.id + "/status");
    ElMessage.success("操作成功");
    await fetchData();
  } catch (e) {}
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm("确定删除该用户？此操作不可恢复！", "警告", { confirmButtonText: "确认删除", type: "warning" });
    await request.delete("/admin/users/" + row.id);
    ElMessage.success("已删除");
    await fetchData();
  } catch (e) {}
}
</script>

<style scoped>
.admin-page h2 { font-size: 22px; color: var(--el-text-color-primary); }
.toolbar { display: flex; gap: 10px; margin-top: 15px; align-items: center; flex-wrap: wrap; }
</style>


