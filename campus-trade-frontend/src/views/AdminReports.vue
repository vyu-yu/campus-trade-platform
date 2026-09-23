<template>
  <div class="my-page">
    <h2>举报管理</h2>
    <el-table :data="reports" v-loading="loading" stripe style="width:100%;margin-top:15px">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="reason" label="举报原因" width="120" />
      <el-table-column prop="description" label="举报描述" min-width="200" show-overflow-tooltip />
      <el-table-column label="商品ID" prop="productId" width="80" />
      <el-table-column label="用户ID" prop="userId" width="80" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="reportStatusType(row.status)" size="small">{{ reportStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="举报时间" width="160" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button v-if="row.status === 'PENDING'" size="small" type="success" @click="handleReport(row.id, 'RESOLVED')">通过</el-button>
          <el-button v-if="row.status === 'PENDING'" size="small" type="info" @click="handleReport(row.id, 'DISMISSED')">驳回</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!loading && reports.length === 0" description="暂无举报" />
    <el-pagination background layout="prev,pager,next" :total="total" :page-size="pageSize" v-model:current-page="currentPage" @current-change="fetchData" style="margin-top:15px" />
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { getReportList, handleReport as handleReportApi } from "../api/report";

const reports = ref([]);
const loading = ref(false);
const total = ref(0);
const currentPage = ref(1);
const pageSize = 20;

onMounted(() => fetchData());

async function fetchData() {
  loading.value = true;
  try {
    const res = await getReportList({ page: currentPage.value, pageSize });
    reports.value = res.data?.records || [];
    total.value = res.data?.total || 0;
  } catch (e) {}
  finally { loading.value = false; }
}

function reportStatusText(s) {
  return { PENDING: "待处理", RESOLVED: "已通过", DISMISSED: "已驳回" }[s] || s;
}
function reportStatusType(s) {
  return { PENDING: "danger", RESOLVED: "success", DISMISSED: "info" }[s] || "info";
}

async function handleReport(id, status) {
  try {
    const title = status === "RESOLVED" ? "确认通过" : "确认驳回";
    const msg = status === "RESOLVED" ? "确定要通过该举报吗？" : "确定要驳回该举报吗？";
    await ElMessageBox.confirm(msg, title, { confirmButtonText: "确定", cancelButtonText: "取消" });
    await handleReportApi(id, status);
    ElMessage.success("操作成功");
    await fetchData();
  } catch (e) {}
}
</script>
<style scoped>
.my-page { max-width: 1100px; margin: 0 auto; }
.my-page h2 { font-size: 22px; color: var(--el-text-color-primary); }
</style>
