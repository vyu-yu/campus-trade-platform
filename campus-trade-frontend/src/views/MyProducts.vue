<template>
  <div class="my-page">
    <h2>我的商品</h2>
    <el-button type="primary" style="margin:15px 0" @click="$router.push('/publish')">发布新商品</el-button>
    <el-table :data="products" v-loading="loading" stripe style="width:100%">
      <el-table-column label="商品名称" min-width="200">
        <template #default="{ row }">
          <span class="link" @click="$router.push('/product/' + row.id)">{{ row.title }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="price" label="价格" width="100">
        <template #default="{ row }">￥{{ row.price }}</template>
      </el-table-column>
      <el-table-column prop="categoryName" label="分类" width="100" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="viewCount" label="浏览量" width="80" />
      <el-table-column prop="createTime" label="发布时间" width="160" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="$router.push('/publish?edit=' + row.id)">编辑</el-button>
         <el-button size="small" v-if="row.status === 'SELLING'" type="success" @click="changeStatus(row.id, 'SOLD')">标记已售</el-button>
          <el-button size="small" v-if="row.status === 'SOLD'" type="primary" @click="changeStatus(row.id, 'SELLING')">重新上架</el-button>
          <el-button size="small" v-if="row.status !== 'TAKEN_DOWN'" type="info" @click="changeStatus(row.id, 'TAKEN_DOWN')">下架</el-button>
          <el-button size="small" v-if="row.status === 'TAKEN_DOWN'" type="primary" @click="changeStatus(row.id, 'SELLING')">重新上架</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination background layout="prev,pager,next" :total="total" :page-size="pageSize" v-model:current-page="currentPage" @current-change="fetchData" style="margin-top:15px" />
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { getMyProducts, updateProductStatus } from "../api/product";

const products = ref([]);
const loading = ref(false);
const total = ref(0);
const currentPage = ref(1);
const pageSize = 12;

onMounted(() => fetchData());

async function fetchData() {
  loading.value = true;
  try {
    const res = await getMyProducts({ page: currentPage.value, pageSize });
    products.value = res.data?.records || [];
    total.value = res.data?.total || 0;
  } catch (e) {}
  finally { loading.value = false; }
}

function statusText(s) {
  return { SELLING: "出售中", SOLD: "已售出", TAKEN_DOWN: "已下架" }[s] || s;
}
function statusType(s) {
  return { SELLING: "success", SOLD: "info", TAKEN_DOWN: "danger" }[s] || "info";
}

async function changeStatus(id, status) {
  try {
    const msg = { SELLING: "确定要重新上架此商品吗？", SOLD: "确定要标记为已售出吗？", TAKEN_DOWN: "确定要下架此商品吗？" };
    await ElMessageBox.confirm(msg[status] || "确定执行此操作吗？", "操作确认", { confirmButtonText: "确定", cancelButtonText: "取消" });
    await updateProductStatus(id, status);
    ElMessage.success("操作成功");
    await fetchData();
  } catch (e) {}
}
</script>
<style scoped>
.my-page { max-width: 1100px; margin: 0 auto; }
.my-page h2 { font-size: 22px; color: var(--el-text-color-primary); }
.link { cursor: pointer; color: var(--el-color-primary); font-weight: 500; }
.link:hover { text-decoration: underline; opacity: 0.8; }
</style>
