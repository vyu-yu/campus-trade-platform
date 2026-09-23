<template>
  <div class="admin-page">
    <h2>商品管理</h2>
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="搜索商品标题" clearable style="width:250px" @clear="fetchData" @keyup.enter="fetchData" />
      <el-button type="primary" @click="fetchData">搜索</el-button>
    </div>
    <el-table :data="products" v-loading="loading" stripe style="width:100%;margin-top:15px">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
      <el-table-column prop="price" label="价格" width="100">
        <template #default="{ row }">￥{{ row.price }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="viewCount" label="浏览量" width="80" />
      <el-table-column prop="createTime" label="发布时间" width="160" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" type="warning" @click="toggleStatus(row)">{{ row.status === 'SELLING' ? '下架' : '上架' }}</el-button>
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

const products = ref([]);
const loading = ref(false);
const total = ref(0);
const currentPage = ref(1);
const pageSize = 20;
const keyword = ref("");

onMounted(() => fetchData());

function statusText(s) { return { SELLING: "出售中", SOLD: "已售出", TAKEN_DOWN: "已下架" }[s] || s; }
function statusType(s) { return { SELLING: "success", SOLD: "info", TAKEN_DOWN: "danger" }[s] || "info"; }

async function fetchData() {
  loading.value = true;
  try {
    const res = await request.get("/admin/products", { params: { page: currentPage.value, pageSize, keyword: keyword.value || undefined } });
    products.value = res.data?.records || [];
    total.value = res.data?.total || 0;
  } catch (e) {}
  finally { loading.value = false; }
}

async function toggleStatus(row) {
  const newStatus = row.status === "SELLING" ? "TAKEN_DOWN" : "SELLING";
  try {
    await ElMessageBox.confirm(newStatus === "TAKEN_DOWN" ? "确定下架该商品？" : "确定重新上架该商品？", "提示");
    await request.put("/admin/products/" + row.id + "/status?status=" + newStatus);
    ElMessage.success("操作成功");
    await fetchData();
  } catch (e) {}
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm("确定删除该商品？", "警告", { confirmButtonText: "确认删除", type: "warning" });
    await request.delete("/admin/products/" + row.id);
    ElMessage.success("已删除");
    await fetchData();
  } catch (e) {}
}
</script>

<style scoped>
.admin-page h2 { font-size: 22px; color: var(--el-text-color-primary); }
.toolbar { display: flex; gap: 10px; margin-top: 15px; align-items: center; flex-wrap: wrap; }
</style>

