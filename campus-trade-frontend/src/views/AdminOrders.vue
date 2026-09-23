<template>
  <div class="admin-page">
    <h2>订单管理</h2>
    <el-table :data="orders" v-loading="loading" stripe style="width:100%;margin-top:15px">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="productId" label="商品ID" width="80" />
      <el-table-column prop="sellerId" label="卖家ID" width="80" />
      <el-table-column prop="buyerId" label="买家ID" width="80" />
      <el-table-column prop="status" label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column prop="updateTime" label="更新时间" width="160" />
    </el-table>
    <el-pagination background layout="prev,pager,next" :total="total" :page-size="pageSize" v-model:current-page="currentPage" @current-change="fetchData" style="margin-top:15px" />
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import request from "../api/request";

const orders = ref([]);
const loading = ref(false);
const total = ref(0);
const currentPage = ref(1);
const pageSize = 20;

onMounted(() => fetchData());

function statusText(s) { return { PENDING: "待沟通", PAID: "已付款", COMPLETED: "已完成", CANCELLED: "已取消" }[s] || s; }
function statusType(s) { return { PENDING: "warning", PAID: "primary", COMPLETED: "success", CANCELLED: "info" }[s] || "info"; }

async function fetchData() {
  loading.value = true;
  try {
    const res = await request.get("/admin/transactions", { params: { page: currentPage.value, pageSize } });
    orders.value = res.data?.records || [];
    total.value = res.data?.total || 0;
  } catch (e) {}
  finally { loading.value = false; }
}
</script>

<style scoped>
.admin-page h2 { font-size: 22px; color: var(--el-text-color-primary); }
</style>
