<template>
  <div class="detail-page" v-loading="loading">
    <el-card v-if="product" shadow="never">
      <el-row :gutter="30">
        <el-col :span="10">
          <el-image :src="mainImage" fit="contain" style="width:100%;height:350px;background:#f5f7fa;border-radius:8px;" />
        </el-col>
        <el-col :span="14">
          <h1 class="detail-title">{{ product.title }}</h1>
          <div class="detail-price">
            <span class="price">￥{{ product.price }}</span>
            <span v-if="product.originalPrice" class="original-price">￥{{ product.originalPrice }}</span>
          </div>
          <div class="detail-info">
            <div class="info-item"><label>商品成色：</label><span>{{ product.condition || "未说明" }}</span></div>
            <div class="info-item"><label>商品分类：</label><span>{{ product.categoryName }}</span></div>
            <div class="info-item"><label>浏览次数：</label><span>{{ product.viewCount }}</span></div>
            <div class="info-item"><label>发布时间：</label><span>{{ product.createTime }}</span></div>
          </div>
          <div class="detail-seller" v-if="product.seller" style="cursor:pointer" @click="$router.push('/seller/' + product.seller.id)">
            <el-avatar icon="UserFilled" :size="48" />
            <div style="flex:1">
              <div class="seller-name">{{ product.seller.nickname }}</div>
              <div style="font-size:12px;color:#409eff;margin-top:2px">查看卖家详情 →</div>
              <div v-if="sellerStats" style="font-size:12px;color:#909399;margin-top:2px">
                交易 {{ sellerStats.count }} 笔 | 评分 \{{ sellerStats.avgScore > 0 ? '★'.repeat(Math.round(sellerStats.avgScore)) + ' ' + sellerStats.avgScore : '暂无评价' }}</div>
            </div>
          </div>
                    <div v-if="product.status === 'SOLD'" class="detail-buyer">
            <el-divider />
            <div style="display:flex;align-items:center;gap:14px;padding:12px 16px;background:#fff7e6;border-radius:8px;margin-bottom:8px">
              <el-avatar :size="48" icon="UserFilled" :style="{background: '#e6a23c'}" />
              <div>
                <div style="font-size:15px;font-weight:600;margin-bottom:6px"><span style="color:#e6a23c">已出售</span> - {{ product.buyerNickname || '卖家标记已售' }}</div>
                <div v-if="product.buyerPhone" style="font-size:14px;color:#606266">
                  <el-icon style="color:#909399;margin-right:2px"><Iphone /></el-icon>
                  <span style="font-weight:500">{{ product.buyerPhone }}</span>
                </div>
              </div>
            </div>
          </div>
<div class="detail-actions">
            <el-button v-if="userStore.isLoggedIn" type="primary" size="large" @click="handleBuy">
              <el-icon><ShoppingCart /></el-icon>立即购买
            </el-button>
            <el-button v-if="userStore.isLoggedIn"
              :type="product.favorited ? 'warning' : 'default'" size="large" @click="handleFavorite">
              <el-icon><Star /></el-icon>{{ product.favorited ? "已收藏" : "收藏" }}
            </el-button>
            <el-button v-if="userStore.isLoggedIn"
              size="large" @click="showReport = true">
              <el-icon><WarningFilled /></el-icon>举报
            </el-button>
          </div>
        </el-col>
      </el-row>

      <el-divider />
      <h3>商品描述</h3>
      <p class="detail-desc">{{ product.description || "暂无描述" }}</p>

      <el-divider />
      <h3>AI 参考价格</h3>
      <el-alert v-if="priceSuggestion" :title="'参考价格区间：' + priceSuggestion.priceRange" type="success" show-icon style="margin:10px 0" />
      <el-button size="small" @click="checkPriceSuggestion">查看AI价格建议</el-button>

      <el-divider />
      <h3>留言与咨询 ({{ messageTotal }})</h3>
      <div class="message-area">
        <el-input v-if="userStore.isLoggedIn" v-model="newMessage" type="textarea" :rows="3" placeholder="输入留言内容..." />
        <el-button v-if="userStore.isLoggedIn" type="primary" size="small" style="margin-top:10px" @click="sendMessage">发送留言</el-button>
        <div v-for="msg in messages" :key="msg.id" class="message-item">
          <div style="display:flex;align-items:center;gap:8px;margin-bottom:4px">
            <el-avatar :size="28" icon="UserFilled" style="flex-shrink:0" />
            <strong>{{ msg.userNickname || "用户" + msg.userId }}</strong>
            <el-tag v-if="msg.seller" size="small" type="warning" style="height:20px;line-height:18px">卖家</el-tag>
            <span style="color:#909399;font-size:12px;margin-left:auto">{{ msg.createTime }}</span>
          </div>
          <p style="margin:0 0 0 36px;white-space:pre-wrap">{{ msg.content }}</p>
          <div v-if="msg.parentId" style="margin:4px 0 0 36px;padding:6px 10px;background:#f5f7fa;border-radius:6px;font-size:13px;color:#909399">
            回复 @{{ replyNickname(msg.parentId) }}
          </div>
          <!-- Reply/Delete buttons for seller -->
          <div v-if="userStore.isLoggedIn && product?.seller?.id === userStore.userInfo?.id && !replyTo[msg.id]" style="margin-top:4px;margin-left:36px;display:flex;gap:8px">
            <el-button size="small" text type="primary" @click="startReply(msg)">回复</el-button>
            <el-button size="small" text type="danger" @click="handleDeleteMessage(msg.id)">删除</el-button>
          </div>
          <!-- Reply input -->
          <div v-if="replyTo[msg.id]" style="margin:8px 0 0 36px;display:flex;gap:8px">
            <el-input v-model="replyContent[msg.id]" size="small" placeholder="输入回复内容..." @keyup.enter="sendReply(msg.id)" />
            <el-button size="small" type="primary" @click="sendReply(msg.id)">发送</el-button>
            <el-button size="small" @click="cancelReply(msg.id)">取消</el-button>
          </div>
        </div>
        <el-empty v-if="messages.length === 0" description="暂无留言" />
      </div>
    </el-card>

    <!-- Report Dialog -->
    <el-dialog v-model="showReport" title="举报商品" width="400px">
      <el-form :model="reportForm">
        <el-form-item label="举报原因">
          <el-select v-model="reportForm.reason" placeholder="请选择举报原因" style="width:100%">
            <el-option label="虚假信息" value="虚假信息" />
            <el-option label="违规商品" value="违规商品" />
            <el-option label="价格欺诈" value="价格欺诈" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="补充说明">
          <el-input v-model="reportForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showReport = false">取消</el-button>
        <el-button type="primary" @click="submitReport">提交举报</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { useUserStore } from "../stores/user";
import { getProductDetail } from "../api/product";
import { addFavorite, removeFavorite } from "../api/favorite";
import { getProductMessages, sendMessage as sendMsg } from "../api/message";
import { createTransaction } from "../api/transaction";
import { createRating, getSellerRatings } from "../api/rating";
import { submitReport as submitReportApi } from "../api/report";
import { deleteMessage } from "../api/message";
import { getPriceSuggestion } from "../api/ai";

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const product = ref(null);
const loading = ref(true);
const messages = ref([]);
const messageTotal = ref(0);
const newMessage = ref("");
const replyTo = ref({});
const replyContent = ref({});
const showReport = ref(false);
const priceSuggestion = ref(null);
const reportForm = ref({ reason: "", description: "" });
const sellerStats = ref(null);

const mainImage = computed(() => {
  if (!product.value?.images) return "";
  return product.value.images.split(",")[0];
});

onMounted(async () => {
  await loadProduct();
  await loadMessages();
});

async function loadProduct() {
  try {
    const res = await getProductDetail(route.params.id);
    product.value = res.data;
    loadSellerStats(res.data.seller?.id);
  } catch (e) { ElMessage.error("加载失败"); }
  finally { loading.value = false; }
}

async function loadSellerStats(sellerId) {
  if (!sellerId) return;
  try {
    const res = await getSellerRatings(sellerId);
    sellerStats.value = res.data;
  } catch (e) {}
}

async function loadMessages() {
  try {
    const res = await getProductMessages(route.params.id, { page: 1, pageSize: 50 });
    messages.value = res.data?.records || [];
    messageTotal.value = res.data?.total || 0;
  } catch (e) {}
}

function startReply(msg) {
  replyTo.value[msg.id] = true;
  replyContent.value[msg.id] = "";
}

function cancelReply(msgId) {
  delete replyTo.value[msgId];
  delete replyContent.value[msgId];
}

function replyNickname(parentId) {
  const parent = messages.value.find(m => m.id === parentId);
  return parent ? (parent.userNickname || "用户" + parent.userId) : "未知";
}

async function sendReply(parentId) {
  const content = replyContent.value[parentId];
  if (!content || !content.trim()) return;
  try {
    await sendMsg({ productId: route.params.id, content: content, parentId: parentId });
    ElMessage.success("回复成功");
    delete replyTo.value[parentId];
    delete replyContent.value[parentId];
    await loadMessages();
  } catch (e) {}
}

async function handleDeleteMessage(msgId) {
  try {
    await ElMessageBox.confirm("确定要删除此留言吗？", "删除确认", { confirmButtonText: "确定", cancelButtonText: "取消" });
    await deleteMessage(msgId);
    ElMessage.success("留言已删除");
    await loadMessages();
  } catch (e) {}
}

async function handleBuy() {
  try {
    await ElMessageBox.confirm("确定要购买此商品吗？", "确认购买", { confirmButtonText: "确定", cancelButtonText: "取消" });
    const res = await createTransaction(route.params.id);
    ElMessage.success("购买成功，请在交易列表中查看");
    await loadProduct();
  } catch (e) { if (e !== "cancel") ElMessage.error(e?.message || "购买失败"); }
}

async function handleFavorite() {
  try {
    if (product.value.favorited) {
      await removeFavorite(product.value.id);
      product.value.favorited = false;
      ElMessage.success("已取消收藏");
    } else {
      await addFavorite(product.value.id);
      product.value.favorited = true;
      ElMessage.success("收藏成功");
    }
  } catch (e) {}
}

async function sendMessage() {
  if (!newMessage.value.trim()) return;
  try {
    await sendMsg({ productId: route.params.id, content: newMessage.value, parentId: null });
    ElMessage.success("发送成功");
    newMessage.value = "";
    await loadMessages();
  } catch (e) {}
}

async function checkPriceSuggestion() {
  try {
    const res = await getPriceSuggestion({
      category: product.value.categoryName,
      condition: product.value.condition,
      originalPrice: product.value.originalPrice || product.value.price
    });
    priceSuggestion.value = res.data;
  } catch (e) {}
}

async function submitReport() {
  if (!reportForm.value.reason) return ElMessage.warning("请选择举报原因");
  try {
    await submitReportApi({ productId: product.value.id, ...reportForm.value });
    ElMessage.success("举报已提交，我们会尽快处理");
    showReport.value = false;
  } catch (e) {}
}
</script>

<style scoped>
.detail-page { max-width: 1000px; margin: 0 auto; }
.detail-title { font-size: 24px; font-weight: 700; margin-bottom: 16px; color: var(--el-text-color-primary); letter-spacing: -0.3px; }
.detail-price { margin-bottom: 20px; }
.price { font-size: 30px; font-weight: 700; color: var(--el-color-warning); }
.original-price { margin-left: 12px; font-size: 16px; color: var(--el-text-color-placeholder); text-decoration: line-through; }
.detail-info { margin: 16px 0; background: var(--el-fill-color-light); padding: 16px; border-radius: var(--el-border-radius-base); }
.info-item { margin: 6px 0; font-size: 14px; }
.info-item label { color: var(--el-text-color-secondary); margin-right: 10px; }
.detail-seller { display: flex; align-items: center; gap: 14px; border-top: 1px solid var(--el-border-color-lighter); margin: 16px 0; cursor: pointer; border-radius: var(--el-border-radius-base); padding: 12px 16px; transition: background 0.2s; }
.detail-seller:hover { background: var(--el-color-primary-light-9); }
.seller-name { font-size: 16px; font-weight: 600; color: var(--el-text-color-primary); }
.detail-actions { display: flex; gap: 12px; margin-top: 20px; flex-wrap: wrap; }
.detail-desc { line-height: 1.8; color: var(--el-text-color-regular); white-space: pre-wrap; background: var(--el-fill-color-light); padding: 16px; border-radius: var(--el-border-radius-base); }
.message-area { margin-top: 16px; }
.message-item { padding: 12px 0; border-bottom: 1px solid var(--el-border-color-lighter); }
.message-item:last-child { border-bottom: none; }
</style>
