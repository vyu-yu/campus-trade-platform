<template>
  <div class="my-page">
    <h2>我的收藏</h2>
    <el-table :data="favorites" v-loading="loading" stripe style="width:100%;margin-top:15px">
      <el-table-column label="商品名称" min-width="250">
        <template #default="{ row }">
          <span class="link" @click="$router.push('/product/' + row.id)">{{ row.title }}</span>
        </template>
      </el-table-column>
      <el-table-column label="价格" width="120">
        <template #default="{ row }">￥{{ row.price }}</template>
      </el-table-column>
      <el-table-column label="分类" width="100">
        <template #default="{ row }">{{ row.categoryName }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button size="small" type="danger" @click="handleRemove(row.id)">取消收藏</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!loading && favorites.length === 0" description="暂无收藏" />
    <el-pagination background layout="prev,pager,next" :total="total" :page-size="pageSize" v-model:current-page="currentPage" @current-change="fetchData" style="margin-top:15px" />
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { ElMessage } from "element-plus";
import { getMyFavorites, removeFavorite } from "../api/favorite";

const favorites = ref([]);
const loading = ref(false);
const total = ref(0);
const currentPage = ref(1);
const pageSize = 12;

onMounted(() => fetchData());

async function fetchData() {
  loading.value = true;
  try {
    const res = await getMyFavorites({ page: currentPage.value, pageSize });
    favorites.value = res.data?.records || [];
    total.value = res.data?.total || 0;
  } catch (e) {}
  finally { loading.value = false; }
}

async function handleRemove(id) {
  try {
    await removeFavorite(id);
    ElMessage.success("已取消收藏");
    await fetchData();
  } catch (e) {}
}
</script>
<style scoped>
.my-page { max-width: 1100px; margin: 0 auto; }
.my-page h2 { font-size: 22px; color: var(--el-text-color-primary); }
.link { cursor: pointer; color: var(--el-color-primary); font-weight: 500; }
</style>
