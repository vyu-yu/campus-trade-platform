<template>
  <div class="admin-page">
    <h2>管理仪表盘</h2>
    <p class="subtitle">平台数据概览</p>

    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="6" v-for="card in statCards" :key="card.key">
        <el-card shadow="hover" class="stat-card" :style="{cursor:'pointer'}" @click="showTable(card.key)">
          <div class="stat-value" :style="{color:card.color}">{{ card.value }}</div>
          <div class="stat-label">{{ card.label }}</div>
          <el-icon :size="40" class="stat-icon" :style="{ color: card.color }">
            <component :is="card.icon" />
          </el-icon>
        </el-card>
      </el-col>
    </el-row>

    <!-- Charts -->
    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="14">
        <el-card shadow="hover">
          <template #header><span>近7天数据趋势</span></template>
          <div ref="lineChartRef" style="height:300px"></div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="hover">
          <template #header><span>商品状态分布</span></template>
          <div ref="pieChartRef" style="height:300px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Detail Table Dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="800px" top="5vh">
      <div v-if="currentDetailApi" style="margin-bottom:10px;display:flex;gap:10px">
        <el-input v-if="currentDetailApi !== 'admin/reports'" v-model="searchKeyword" placeholder="搜索关键词..." clearable style="width:260px" @keyup.enter="searchDetail" />
        <el-button type="primary" @click="searchDetail">搜索</el-button>
        <el-button v-if="searchKeyword" @click="searchKeyword='';loadDetailData()">重置</el-button>
      </div>
      <el-table :data="detailData" v-loading="detailLoading" stripe style="width:100%" max-height="400">
        <el-table-column label="操作" width="160" fixed="right" v-if="currentDetailApi && currentDetailApi.includes('reports')">
        <template #default="{ row }">
          <el-button size="small" type="success" @click="processReport(row.id, 'RESOLVED')">已处理</el-button>
          <el-button size="small" type="danger" @click="processReport(row.id, 'REJECTED')">驳回</el-button>
        </template>
      </el-table-column>
      <el-table-column v-for="col in detailColumns" :key="col.prop" :prop="col.prop" :label="col.label" :width="col.width" :min-width="col.minWidth">
          <template #default="{ row }" v-if="col.formatter">
            <component :is="col.formatter(row)" />
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-if="detailTotal > pageSize" background layout="prev,pager,next"
        :total="detailTotal" :page-size="pageSize" v-model:current-page="detailPage"
        @current-change="loadDetailData" style="margin-top:10px" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from "vue";
import { User, Goods, ShoppingBag, List, Coin, WarningFilled } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import request from "../api/request";

// Stat cards
const statCards = ref([
  { key: "users", label: "用户总数", value: 0, icon: "User", color: "#409eff" },
  { key: "products", label: "商品总数", value: 0, icon: "Goods", color: "#67c23a" },
  { key: "selling", label: "出售中", value: 0, icon: "ShoppingBag", color: "#e6a23c" },
  { key: "sold", label: "已售出", value: 0, icon: "List", color: "#909399" },
  { key: "transactions", label: "交易总数", value: 0, icon: "Coin", color: "#f56c6c" },
  { key: "reports", label: "待处理举报", value: 0, icon: "WarningFilled", color: "#d81e06" },
]);

// Chart refs
const lineChartRef = ref(null);
const pieChartRef = ref(null);

// Detail dialog
const dialogVisible = ref(false);
const dialogTitle = ref("");
const detailData = ref([]);
const detailLoading = ref(false);
const detailTotal = ref(0);
const detailPage = ref(1);
const detailColumns = ref([]);
const pageSize = 15;
let currentDetailApi = null;
const searchKeyword = ref("");

async function processReport(id, status) {
  try {
    await request.put('admin/reports/' + id + '/status', null, { params: { status } });
    ElMessage.success('操作成功');
    loadDetailData();
  } catch (e) {}
}

function searchDetail() {
  detailPage.value = 1;
  loadDetailData();
}

const apis = {
    users: { title: "用户列表", api: "admin/users", params: {}, columns: [
      { prop: "id", label: "ID", width: "70" },
      { prop: "username", label: "用户名", minWidth: "120" },
      { prop: "nickname", label: "昵称", minWidth: "120" },
      { prop: "email", label: "邮箱", minWidth: "150" },
      { prop: "phone", label: "手机号", width: "120" },
      { prop: "createTime", label: "注册时间", minWidth: "160" }
    ]},
    products: { title: "商品列表", api: "admin/products", params: {}, columns: [
      { prop: "id", label: "ID", width: "70" },
      { prop: "title", label: "商品名称", minWidth: "180" },
      { prop: "price", label: "价格", width: "90" },
      { prop: "status", label: "状态", width: "80" },
      { prop: "viewCount", label: "浏览量", width: "80" },
      { prop: "createTime", label: "发布时间", minWidth: "160" }
    ]},
    selling: { title: "出售中商品", api: "admin/products/selling", params: {}, columns: [
      { prop: "id", label: "ID", width: "70" },
      { prop: "title", label: "商品名称", minWidth: "180" },
      { prop: "price", label: "价格", width: "90" },
      { prop: "viewCount", label: "浏览量", width: "80" },
      { prop: "createTime", label: "发布时间", minWidth: "160" }
    ]},
    sold: { title: "已售出商品", api: "admin/products/sold", params: {}, columns: [
      { prop: "id", label: "ID", width: "70" },
      { prop: "title", label: "商品名称", minWidth: "180" },
      { prop: "price", label: "价格", width: "90" },
      { prop: "createTime", label: "售出时间", minWidth: "160" }
    ]},
    transactions: { title: "交易列表", api: "admin/transactions", params: {}, columns: [
      { prop: "id", label: "ID", width: "70" },
      { prop: "productId", label: "商品ID", width: "80" },
      { prop: "status", label: "状态", width: "80" },
      { prop: "createTime", label: "创建时间", minWidth: "160" }
    ]},
    reports: { title: "待处理举报", api: "admin/reports", params: { status: "PENDING" }, columns: [
      { prop: "id", label: "ID", width: "70" },
      { prop: "productId", label: "商品ID", width: "80" },
      { prop: "reason", label: "举报原因", minWidth: "120" },
      { prop: "description", label: "描述", minWidth: "150" },
      { prop: "createTime", label: "举报时间", minWidth: "160" }
    ]}
  };

function showTable(key) {
  detailPage.value = 1;
  detailData.value = [];
  detailTotal.value = 0;

  const cfg = apis[key];
  if (!cfg) return;
  dialogTitle.value = cfg.title;
  detailColumns.value = cfg.columns;
  currentDetailApi = cfg.api;
  if (key === 'reports') searchKeyword.value = '';
  loadDetailData();
}

async function loadDetailData() {
  detailLoading.value = true;
  dialogVisible.value = true;
  try {
    const cfg = Object.values(apis).find(a => a.api === currentDetailApi);
    const params = { page: detailPage.value, pageSize, ...(cfg?.params || {}) };
    if (searchKeyword.value) params.keyword = searchKeyword.value;
    const res = await request.get(currentDetailApi, { params });
    const d = res.data;
    detailData.value = d?.records || [];
    detailTotal.value = d?.total || 0;
  } catch (e) { console.error("加载详情失败", e); }
  finally { detailLoading.value = false; }
}

// ECharts from CDN
function loadCharts() {
  const script = document.createElement("script");
  script.src = "https://cdn.jsdelivr.net/npm/echarts@5/dist/echarts.min.js";
  script.onload = () => {
    initLineChart();
    initPieChart();
  };
  document.head.appendChild(script);
}

function initLineChart() {
  const chart = echarts.init(lineChartRef.value);
  const api = statCards.value.reduce((acc, s) => { acc[s.key] = s; return acc; }, {});

  // Transform dailyStats for chart
  const dates = (window._dailyStats || []).map(d => d.date);
  chart.setOption({
    tooltip: { trigger: "axis" },
    legend: { data: ["新增用户", "新增商品", "新增交易"], bottom: 0 },
    grid: { left: 50, right: 20, bottom: 40, top: 20 },
    xAxis: { type: "category", data: dates, axisLabel: { fontSize: 11 } },
    yAxis: { type: "value", minInterval: 1 },
    series: [
      {
        name: "新增用户",
        type: "line",
        smooth: true,
        data: (window._dailyStats || []).map(d => d.newUsers),
        itemStyle: { color: "#409eff" },
        areaStyle: { color: "rgba(64,158,255,0.1)" }
      },
      {
        name: "新增商品",
        type: "line",
        smooth: true,
        data: (window._dailyStats || []).map(d => d.newProducts),
        itemStyle: { color: "#67c23a" },
        areaStyle: { color: "rgba(103,194,58,0.1)" }
      },
      {
        name: "新增交易",
        type: "line",
        smooth: true,
        data: (window._dailyStats || []).map(d => d.newTransactions),
        itemStyle: { color: "#e6a23c" },
        areaStyle: { color: "rgba(230,162,60,0.1)" }
      }
    ]
  });
}

function initPieChart() {
  const chart = echarts.init(pieChartRef.value);
  const s = statCards.value.reduce((acc, c) => { acc[c.key] = c.value; return acc; }, {});
  chart.setOption({
    tooltip: { trigger: "item", formatter: "{b}: {c} ({d}%)" },
    legend: { bottom: 0 },
    series: [{
      type: "pie",
      radius: ["40%", "70%"],
      center: ["50%", "45%"],
      avoidLabelOverlap: true,
      label: { show: true, formatter: "{b}: {c}" },
      emphasis: { label: { show: true, fontSize: 16, fontWeight: "bold" } },
      data: [
        { value: s.selling || 0, name: "出售中", itemStyle: { color: "#e6a23c" } },
        { value: s.sold || 0, name: "已售出", itemStyle: { color: "#67c23a" } },
        { value: Math.max(0, (s.products || 0) - (s.selling || 0) - (s.sold || 0)), name: "其他", itemStyle: { color: "#909399" } }
      ]
    }]
  });
}

onMounted(async () => {
  try {
    const res = await request.get("/admin/dashboard");
    const d = res.data;
    if (d) {
      statCards.value[0].value = d.userCount || 0;
      statCards.value[1].value = d.productCount || 0;
      statCards.value[2].value = d.sellingCount || 0;
      statCards.value[3].value = d.soldCount || 0;
      statCards.value[4].value = d.transactionCount || 0;
      statCards.value[5].value = d.pendingReportCount || 0;
      window._dailyStats = d.dailyStats || [];
    }
    await nextTick();
    loadCharts();
  } catch (e) { console.error("仪表盘加载失败", e); }
});
</script>

<style scoped>
.admin-page h2 { font-size: 22px; }
.subtitle { color: var(--el-text-color-secondary); margin-top: 5px; }
.stat-card { position: relative; min-height: 100px; margin-bottom: 20px; cursor: pointer; transition: all .25s ease; border-radius: var(--el-border-radius-base); }
.stat-card:hover { transform: translateY(-4px); box-shadow: var(--el-box-shadow-hover); }
.stat-value { font-size: 32px; font-weight: 700; }
.stat-label { font-size: 14px; color: var(--el-text-color-secondary); margin-top: 5px; }
.stat-icon { position: absolute; right: 20px; top: 20px; opacity: .2; }
</style>
