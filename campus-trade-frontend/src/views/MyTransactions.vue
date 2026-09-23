<template>
  <div class="my-page">
    <h2>我的交易</h2>
    <el-table :data="transactions" v-loading="loading" stripe style="width:100%;margin-top:15px">
      <el-table-column label="编号" prop="id" width="70" />
      <el-table-column label="角色" width="70">
        <template #default="{ row }">{{ row.sellerId === userId ? "卖家" : "买家" }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="txStatusType(row.status)" size="small">{{ txStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="showDetail(row)">查看详情</el-button>
          <el-button v-if="row.status === 'PENDING'" size="small" type="success" @click="updateStatus(row.id, 'PAID')">标记付款</el-button>
          <el-button v-if="row.status === 'PAID'" size="small" type="primary" @click="updateStatus(row.id, 'COMPLETED')">确认收货</el-button>
          <el-button v-if="row.status === 'PENDING'" size="small" type="danger" @click="updateStatus(row.id, 'CANCELLED')">取消交易</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!loading && transactions.length === 0" description="暂无交易" />
    <el-pagination background layout="prev,pager,next" :total="total" :page-size="pageSize" v-model:current-page="currentPage" @current-change="fetchData" style="margin-top:15px" />

    <!-- Detail Dialog -->
    <el-dialog v-model="detailVisible" title="交易详情" width="650px" top="5vh">
      <div v-if="detailLoading" style="text-align:center;padding:40px">
        <el-icon class="is-loading" :size="32"><Loading /></el-icon>
        <p style="margin-top:10px;color:#909399">加载中...</p>
      </div>
      <template v-if="!detailLoading && detail">
        <el-descriptions :column="2" border style="margin-bottom:16px">
          <el-descriptions-item label="交易编号" :span="2">{{ detail.id }}</el-descriptions-item>
          <el-descriptions-item label="我的角色">
            <el-tag :type="detail.myRole === '买家' ? 'success' : 'warning'" size="small">{{ detail.myRole }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="交易状态">
            <el-tag :type="txStatusType(detail.status)" size="small">{{ txStatusText(detail.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">{{ detail.createTime }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">商品详情</el-divider>
        <div style="display:flex;gap:16px;margin-bottom:16px">
          <el-image v-if="detail.productImages" :src="detail.productImages.split(',')[0]" fit="cover" style="width:130px;height:130px;border-radius:8px;flex-shrink:0;background:#f5f7fa" />
          <div style="flex:1;min-width:0">
            <h3 style="margin:0 0 6px 0;font-size:16px">{{ detail.productTitle }}</h3>
            <p style="color:#e6a23c;font-size:22px;font-weight:700;margin:0 0 6px 0">&yen;{{ detail.productPrice }}
              <span v-if="detail.productOriginalPrice" style="font-size:13px;color:#909399;font-weight:400;text-decoration:line-through;margin-left:8px">&yen;{{ detail.productOriginalPrice }}</span>
            </p>
            <p style="color:#606266;font-size:13px;margin:0 0 3px 0"><label style="color:#909399">成色：</label>{{ detail.productCondition || "未说明" }}</p>
            <p style="color:#606266;font-size:13px;margin:0 0 3px 0"><label style="color:#909399">分类：</label>{{ detail.productCategoryName || "未分类" }}</p>
            <el-divider style="margin:8px 0" />
            <p style="color:#303133;font-size:13px;margin:0;white-space:pre-wrap;line-height:1.8">{{ detail.productDescription || "暂无描述" }}</p>
          </div>
        </div>

        <el-divider content-position="left">卖家信息</el-divider>
        <div style="display:flex;align-items:center;gap:14px;padding:12px 16px;background:#f8f9fa;border-radius:8px;margin-bottom:8px">
          <el-avatar :size="56" icon="UserFilled" :style="{background: '#409eff'}" />
          <div>
            <div style="font-size:16px;font-weight:600;margin-bottom:6px">{{ detail.sellerNickname || "未知用户" }}</div>
            <div v-if="detail.sellerPhone" style="font-size:14px;color:#606266">
              <el-icon style="color:#909399;margin-right:2px"><Iphone /></el-icon>
              <span style="font-weight:500">{{ detail.sellerPhone }}</span>
            </div>
            <div v-else style="font-size:13px;color:#909399">暂无联系方式</div>
          </div>
          <el-button size="small" type="primary" @click="contactSeller" style="margin-left:auto">联系卖家</el-button>
        </div>
      </template>
      <template #footer v-if="detail && detail.status === 'COMPLETED' && detail.myRole === '买家'">
        <div v-if="detail.myRatingId" style="display:flex;align-items:center;gap:10px">
          <span style="font-size:13px;color:#909399">已评价：{{ detail.myRating }}分</span>
          <el-button type="warning" @click="openRating">修改评价</el-button>
        </div>
        <el-button v-else type="warning" @click="openRating">评价卖家</el-button>
      </template>
    </el-dialog>

    <!-- Rating Dialog -->
    <el-dialog v-model="showRatingDialog" :title="detail?.myRatingId ? '修改评价' : '评价卖家'" width="400px" :destroy-on-close="true">
      <div style="text-align:center;padding:10px">
        <div style="margin-bottom:12px;font-size:15px;color:#606266">给卖家打分</div>
        <el-rate v-model="ratingForm.score" :colors="['#e6a23c','#e6a23c','#e6a23c']" style="margin-bottom:16px" />
        <el-input v-model="ratingForm.content" type="textarea" :rows="3" placeholder="写点评价内容（可选）..." />
      </div>
      <template #footer>
        <el-button @click="handleCloseRating">取消</el-button>
        <el-button type="primary" @click="submitRating">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
 import { ref, reactive, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Loading } from "@element-plus/icons-vue";
import { useRouter } from "vue-router";
import { getOrCreateConversation } from "../api/chat";
import { useUserStore } from "../stores/user";
import { getMyTransactions, getTransactionDetail, updateTransactionStatus } from "../api/transaction";
import { createRating, updateRating } from "../api/rating";

const userStore = useUserStore();
const userId = userStore.userInfo?.id;
const router = useRouter();
const transactions = ref([]);
const loading = ref(false);
const total = ref(0);
const currentPage = ref(1);
const pageSize = 12;

const detailVisible = ref(false);
const detailLoading = ref(false);
const detail = ref(null);
const showRatingDialog = ref(false);
const ratingForm = reactive({
  sellerId: null,
  transactionId: null,
  score: 5,
  content: ""
});

onMounted(() => fetchData());

async function fetchData() {
  loading.value = true;
  try {
    const res = await getMyTransactions({ page: currentPage.value, pageSize });
    transactions.value = res.data?.records || [];
    total.value = res.data?.total || 0;
  } catch (e) {}
  finally { loading.value = false; }
}

function txStatusText(s) {
  return { PENDING: "待付款", PAID: "已付款", COMPLETED: "已完成", CANCELLED: "已取消" }[s] || s;
}
function txStatusType(s) {
  return { PENDING: "warning", PAID: "primary", COMPLETED: "success", CANCELLED: "info" }[s] || "info";
}

async function showDetail(row) {
  detailVisible.value = true;
  detailLoading.value = true;
  try {
    const res = await getTransactionDetail(row.id);
    detail.value = res.data;
  } catch (e) {
    ElMessage.error("加载交易详情失败");
  }
  finally { detailLoading.value = false; }
}

async function updateStatus(id, status) {
  const messages = {
    PAID: "确定要标记为已付款吗？",
    COMPLETED: "确定要确认收货吗？",
    CANCELLED: "确定要取消交易吗？"
  };
  try {
    await ElMessageBox.confirm(messages[status] || "确定执行此操作吗？", "操作确认", { confirmButtonText: "确定", cancelButtonText: "取消" });
    await updateTransactionStatus(id, status);
    ElMessage.success("操作成功");
    await fetchData();
  } catch (e) {}
}

function openRating() {
  if (!detail.value) return;
  if (detail.value.myRatingId) {
    Object.assign(ratingForm, { sellerId: detail.value.sellerId, transactionId: detail.value.id, score: detail.value.myRating, content: detail.value.myRatingContent || "" });
  } else {
    Object.assign(ratingForm, { sellerId: detail.value.sellerId, transactionId: detail.value.id, score: 5, content: "" });
  }
  showRatingDialog.value = true;
}

function handleCloseRating() {
  showRatingDialog.value = false;
}

async function submitRating() {
  try {
    if (detail.value?.myRatingId) {
      await updateRating(detail.value.myRatingId, { score: ratingForm.score, content: ratingForm.content });
    } else {
      await createRating({ sellerId: ratingForm.sellerId, transactionId: ratingForm.transactionId, score: ratingForm.score, content: ratingForm.content });
    }
    ElMessage.success("评价成功");
    showRatingDialog.value = false;
    // Reload detail
    if (detail.value?.id) {
      const res = await getTransactionDetail(detail.value.id);
      detail.value = res.data;
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || "评价失败");
  }
}

async function contactSeller() {
  if (!detail.value) return;
  const otherId = detail.value.myRole === "卖家" ? detail.value.buyerId : detail.value.sellerId;
  if (!otherId) { ElMessage.error("无法获取对方信息"); return; }
  try {
    await getOrCreateConversation({ sellerId: otherId, productId: detail.value.productId });
  } catch (e) {
    console.error("Contact error:", e);
    ElMessage.error("联系失败，请稍后重试");
    return;
  }
  router.push("/messages");
}
</script>
<style scoped>
.my-page { max-width: 1100px; margin: 0 auto; }
.my-page h2 { font-size: 22px; color: var(--el-text-color-primary); }
</style>
