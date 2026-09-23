<template>
  <div class="seller-page" v-loading="loadingSeller">
    <el-empty v-if="!loadingSeller && !seller" description="卖家信息加载失败或用户不存在" />
    
    <template v-if="seller">
      <el-card shadow="never" style="margin-bottom:20px">
        <div style="display:flex;align-items:center;gap:16px;padding:10px 0">
          <el-avatar :size="72" icon="UserFilled" />
          <div style="flex:1">
            <h2 style="margin:0 0 4px 0">{{ seller.nickname || seller.username }}</h2>
            <div style="color:#909399;font-size:13px">注册时间: {{ seller.createTime }}</div>
            <div style="margin-top:6px">
              <el-rate v-if="stats && stats.count > 0" :model-value="stats.avgScore" disabled show-score text-color="#f7ba2a" :colors="['#f7ba2a','#f7ba2a','#f7ba2a']" :score-template="`{value} 分 (${transactionCount} 笔交易)`" />
              <span v-else style="color:#909399;font-size:13px">暂无评价</span>
            </div>
          </div>
          <div>
            <el-button type="primary" size="large" @click="openChat">
              <el-icon><ChatLineSquare /></el-icon> 联系卖家
            </el-button>
          </div>
        </div>
      </el-card>

      <el-row :gutter="20" style="margin-bottom:20px">
        <el-col :span="8">
          <el-card shadow="hover">
            <div class="stat-num" style="color:#67c23a">{{ transactionCount }}</div>
            <div class="stat-lbl">累计成交</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover">
            <div class="stat-num" style="color:#e6a23c">{{ stats?.avgScore || '-' }}</div>
            <div class="stat-lbl">综合评分</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover">
            <div class="stat-num" style="color:#409eff">{{ sellingCount }}</div>
            <div class="stat-lbl">在售数量</div>
          </el-card>
        </el-col>
      </el-row>

      <!-- Sold Items -->
      <el-card shadow="never" style="margin-bottom:20px">
        <template #header><span>历史成交 ({{ transactionCount }})</span></template>
        <el-table :data="soldProducts" stripe style="width:100%" v-if="soldProducts.length > 0">
          <el-table-column label="商品" min-width="200">
            <template #default="{ row }">
              <span class="link" @click="$router.push('/product/' + row.id)">{{ row.title }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="categoryName" label="分类" width="100" />
          <el-table-column prop="price" label="成交价" width="100">
            <template #default="{ row }">￥{{ row.price }}</template>
          </el-table-column>
          <el-table-column label="评分" width="150">
            <template #default="{ row }">
              <el-rate :model-value="getProductRating(row.id)" disabled :max="5" :allow-half="true" style="display:inline-block" :colors="['#f7ba2a','#f7ba2a','#f7ba2a']" />
              <div v-if="hasRating(row.id)" style="margin-top:4px">
                <span style="font-size:12px;color:#e6a23c">{{ getProductRating(row.id) }}</span>
                <div v-if="getProductRatingContent(row.id)" style="font-size:12px;color:#606266;margin-top:2px">{{ getProductRatingContent(row.id) }}</div>
              </div>
              <span v-else style="color:#909399;font-size:12px">暂无评价</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button size="small" @click="$router.push('/product/' + row.id)">查看</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="暂无历史成交" />
      </el-card>

      <!-- Current Products -->
      <el-card shadow="never">
        <template #header><span>在售商品 ({{ sellingCount }})</span></template>
        <el-row :gutter="16">
          <el-col :span="8" v-for="p in sellingProducts" :key="p.id" style="margin-bottom:16px">
            <el-card shadow="hover" style="cursor:pointer" @click="$router.push('/product/' + p.id)">
              <el-image v-if="p.images" :src="p.images.split(',')[0]" fit="cover" style="width:100%;height:140px;border-radius:6px;background:#f5f7fa" />
              <h4 style="margin:8px 0 4px;font-size:14px">{{ p.title }}</h4>
              <div style="color:#909399;font-size:12px">{{ p.categoryName || '' }}</div>
              <span style="color:#e6a23c;font-weight:700">￥{{ p.price }}</span>
            </el-card>
          </el-col>
        </el-row>
        <el-empty v-if="sellingProducts.length === 0" description="暂无在售商品" />
      </el-card>

      <!-- Chat Dialog -->
      <el-dialog v-model="showChat" title="联系卖家" width="500px">
        <div ref="chatRef" style="height:320px;overflow-y:auto;padding:10px;background:#f5f7fa;border-radius:8px;margin-bottom:10px">
          <div v-for="(msg, i) in chatMessages" :key="i" :style="{textAlign: msg.role==='user'?'right':'left', marginBottom:'12px'}">
            <div style="font-size:11px;color:#909399;margin-bottom:2px">{{ msg.role==='user'?'我':seller.nickname }}</div>
            <div :style="{display:'inline-block',padding:'8px 14px',borderRadius:'12px',maxWidth:'75%',background:msg.role==='user'?'#409eff':'#fff',color:msg.role==='user'?'#fff':'#303133',textAlign:'left'}">
              {{ msg.content }}
            </div>
          </div>
        </div>
        <div style="display:flex;gap:8px">
          <el-input v-model="chatInput" placeholder="输入消息..." @keyup.enter="sendChat" clearable />
          <el-button type="primary" @click="sendChat">发送</el-button>
        </div>
      </el-dialog>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { ChatLineSquare } from "@element-plus/icons-vue";
import request from "../api/request";
import { getSellerRatings } from "../api/rating";
import { getOrCreateConversation, sendChatMessage, getChatMessages } from "../api/chat";

const route = useRoute();
const router = useRouter();

const sellerId = route.params.id;

const seller = ref(null);
const loadingSeller = ref(true);
const stats = ref(null);
const transactionCount = ref(0);

const sellingProducts = ref([]);
const sellingCount = ref(0);

const soldProducts = ref([]);
const productRatings = ref({});

const showChat = ref(false);
const chatInput = ref("");
const chatMessages = ref([]);
const chatRef = ref(null);

onMounted(async () => {
  await Promise.all([
    loadSeller(),
    loadSellingProducts(),
    loadSoldProducts(),
    loadRatingStats()
  ]);
  loadingSeller.value = false;
});

async function loadSeller() {
  try {
    const res = await request.get("/user/profile/public?id=" + sellerId);
    seller.value = res.data;
  } catch (e) { console.error("卖家信息加载失败", e); }
}

async function loadSellingProducts() {
  try {
    const res = await request.get("/product/list", { params: { userId: sellerId, pageSize: 50 } });
    const d = res.data;
    sellingProducts.value = d?.records || [];
    sellingCount.value = d?.total || 0;
  } catch (e) { console.error("在售商品加载失败", e); }
}

async function loadSoldProducts() {
  try {
    const res = await request.get("/product/sold-by-seller/" + sellerId, { params: { pageSize: 50 } });
    const d = res.data;
    soldProducts.value = d?.records || [];
    transactionCount.value = d?.total || 0;
  } catch (e) { console.error("历史成交加载失败", e); }
}

async function loadRatingStats() {
  try {
    const res = await getSellerRatings(sellerId);
    stats.value = res.data;
    // Build product rating map from detailRatings
    if (res.data?.detailRatings) {
      res.data.detailRatings.forEach(r => {
        if (r.productId) {
          productRatings.value[r.productId] = r;
        }
      });
    }
  } catch (e) { console.error("评分加载失败", e); }
}

function getProductRating(productId) {
  return productRatings.value[productId]?.score || 0;
}

function getProductRatingContent(productId) {
  return productRatings.value[productId]?.content || '';
}

function hasRating(productId) {
  return productRatings.value[productId]?.score > 0;
}

async function openChat() {
  if (!seller) return;
  showChat.value = true;
  chatMessages.value = [];
  try {
    const res = await getOrCreateConversation({
      sellerId: Number(sellerId),
      productId: sellingProducts.value[0]?.id || soldProducts.value[0]?.id || 0
    });
    window._currentConvId = res.data.conversationId;
    // Load existing messages
    const msgRes = await getChatMessages(res.data.conversationId, { pageSize: 100 });
    chatMessages.value = (msgRes.data?.records || []).map(m => ({
      role: m.senderId === Number(sellerId) ? "seller" : "user",
      content: m.content,
      time: m.createTime
    }));
    scrollChat();
  } catch (e) { console.error("加载对话失败", e); }
}

function scrollChat() {
  setTimeout(() => {
    if (chatRef.value) chatRef.value.scrollTop = chatRef.value.scrollHeight;
  }, 100);
}

async function sendChat() {
  if (!chatInput.value.trim() || !window._currentConvId) return;
  const text = chatInput.value;
  chatMessages.value.push({ role: "user", content: text, time: new Date().toLocaleString() });
  chatInput.value = "";
  try {
    await sendChatMessage(window._currentConvId, text);
    scrollChat();
  } catch (e) { console.error("消息发送失败", e); }
}
</script>

<style scoped>
.seller-page { max-width: 1000px; margin: 0 auto; }
.stat-num { font-size: 28px; font-weight: 700; text-align: center; }
.stat-lbl { font-size: 13px; color: var(--el-text-color-secondary); text-align: center; margin-top: 4px; }
.link { cursor: pointer; color: var(--el-color-primary); font-weight: 500; }
.link:hover { text-decoration: underline; opacity: 0.8; }
</style>
