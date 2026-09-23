<template>
  <div class="home-page">
    <div class="hero-section">
      <h1>校园二手交易平台</h1>
      <p>让物品循环利用，让交易更简单</p>
    </div>

    <div class="search-bar">
      <el-input v-model="searchKeyword" placeholder="搜索二手商品..." prefix-icon="Search" clearable size="large" @keyup.enter="handleSearch" />
      <el-button type="primary" size="large" @click="handleSearch"><el-icon><Search /></el-icon>搜索</el-button>
    </div>

    <div class="category-tabs">
      <el-tag :type="activeCategory === null ? 'primary' : 'info'" @click="activeCategory = null; fetchProducts()">全部</el-tag>
      <el-tag v-for="cat in categories" :key="cat.id"
        :type="activeCategory === cat.id ? 'primary' : 'info'"
        @click="activeCategory = cat.id; fetchProducts()">{{ cat.name }}</el-tag>
    </div>

    <div v-loading="loading" class="product-grid">
      <el-row :gutter="20">
        <el-col v-for="item in productList" :key="item.id" :xs="12" :sm="8" :md="6" :lg="6" style="margin-bottom: 20px;">
          <el-card :body-style="{ padding: '0px' }" shadow="hover" class="product-card" @click="$router.push('/product/' + item.id)">
            <div class="product-image">
              <el-image :src="item.images || defaultImg" fit="cover" style="width:100%;height:180px" />
            </div>
            <div class="product-info">
              <h3 class="product-title">{{ item.title }}</h3>
              <div class="product-price">￥{{ item.price }}</div>
              <div class="product-meta">
                <span class="condition">{{ item.condition || "未说明" }}</span>
                <span class="seller">{{ item.sellerNickname }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-empty v-if="!loading && productList.length === 0" description="暂无商品" />
    </div>

    <div class="pagination" v-if="total > 0">
      <el-pagination background layout="prev, pager, next" :total="total" :page-size="pageSize" v-model:current-page="currentPage" @current-change="changePage" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { getProductList } from "../api/product";
import { getCategoryList } from "../api/category";

const defaultImg = "data:image/svg+xml,%3Csvg xmlns=%22http://www.w3.org/2000/svg%22 viewBox=%220 0 200 200%22%3E%3Crect width=%22200%22 height=%22200%22 fill=%22%23e0e0e0%22/%3E%3Ctext x=%2250%%22 y=%2250%%22 text-anchor=%22middle%22 dy=%22.3em%22 fill=%22%23999%22 font-size=%2216%22%3E????%3C/text%3E%3C/svg%3E";

const categories = ref([]);
const productList = ref([]);
const loading = ref(false);
const searchKeyword = ref("");
const activeCategory = ref(null);
const currentPage = ref(1);
const pageSize = 12;
const total = ref(0);

onMounted(async () => {
  try {
    const res = await getCategoryList();
    categories.value = res.data || [];
  } catch (e) {}
  await fetchProducts();
});

async function fetchProducts() {
  loading.value = true;
  try {
    const params = { page: currentPage.value, pageSize };
    if (searchKeyword.value) params.keyword = searchKeyword.value;
    if (activeCategory.value !== null) params.categoryId = activeCategory.value;
    const res = await getProductList(params);
    productList.value = res.data?.records || [];
    total.value = res.data?.total || 0;
  } catch (e) { productList.value = []; }
  finally { loading.value = false; }
}

function handleSearch() { currentPage.value = 1; fetchProducts(); }
function changePage(page) { currentPage.value = page; fetchProducts(); }
</script>

<style scoped>
.home-page { max-width: 1200px; margin: 0 auto; }
.hero-section {
  text-align: center;
  padding: 48px 0 32px;
  background: linear-gradient(135deg, var(--el-color-primary-light-9) 0%, #FAFAF8 60%);
  border-radius: var(--el-border-radius-base);
  margin-bottom: 28px;
}
.hero-section h1 { font-size: 30px; font-weight: 700; color: var(--el-text-color-primary); letter-spacing: -0.5px; }
.hero-section p { color: var(--el-text-color-secondary); margin-top: 10px; font-size: 15px; }
.search-bar { display: flex; gap: 12px; margin-bottom: 24px; }
.search-bar .el-input { flex: 1; }
.category-tabs { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 24px; }
.product-grid { min-height: 200px; }
.product-card { cursor: pointer; transition: all 0.25s ease; border-radius: var(--el-border-radius-base); overflow: hidden; }
.product-card:hover { transform: translateY(-4px); box-shadow: var(--el-box-shadow-hover); }
.product-image { background: var(--el-fill-color); }
.product-image .el-image { border-radius: var(--el-border-radius-base) var(--el-border-radius-base) 0 0; }
.product-info { padding: 14px; }
.product-title { font-size: 15px; font-weight: 500; color: var(--el-text-color-primary); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-bottom: 8px; }
.product-price { font-size: 20px; font-weight: 700; color: var(--el-color-warning); }
.product-meta { display: flex; justify-content: space-between; font-size: 12px; color: var(--el-text-color-placeholder); margin-top: 6px; }
.pagination { display: flex; justify-content: center; margin-top: 24px; padding: 24px 0; }
</style>
