<template>
  <div class="ai-page">
    <h2>AI 智能助手</h2>
    <p class="subtitle">智能客服 | AI 商品标题 | 智能问答</p>

    <el-row :gutter="20" style="margin-top:20px">
      <!-- Customer Service -->
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon size="20"><ChatLineSquare /></el-icon>
              <span>智能客服</span>
            </div>
          </template>
          <div class="chat-box" ref="chatBox">
            <div v-for="(msg, i) in chatMessages" :key="i"
              :class="['chat-msg', msg.role === 'user' ? 'chat-user' : 'chat-ai']">
              <div class="msg-content">{{ msg.content }}</div>
              <div class="msg-time">{{ msg.role === 'user' ? '我' : 'AI' }}</div>
            </div>
          </div>
          <div class="chat-input">
            <el-input v-model="chatQuestion" placeholder="请输入您的问题..." @keyup.enter="handleAsk" />
            <el-button type="primary" :loading="chatLoading" @click="handleAsk" style="margin-top:8px">发送</el-button>
          </div>
          <div class="quick-questions">
            <el-tag size="small" @click="askQuick('推荐二手教材')">推荐教材</el-tag>
            <el-tag size="small" @click="askQuick('这个价格合理吗')">估价建议</el-tag>
            <el-tag size="small" @click="askQuick('交易注意事项')">交易须知</el-tag>
            <el-tag size="small" @click="askQuick('如何发布商品')">如何发布</el-tag>
          </div>
        </el-card>
      </el-col>

      <!-- Risk Check + Price Suggestion -->
      <el-col :span="12">
        <el-card shadow="hover" style="margin-bottom:20px">
          <template #header>
            <div class="card-header">
              <el-icon size="20"><WarningFilled /></el-icon>
              <span>风险检测</span>
            </div>
          </template>
          <el-input v-model="riskTitle" placeholder="输入商品标题" style="margin-bottom:10px" />
          <el-input v-model="riskDesc" type="textarea" :rows="3" placeholder="输入商品描述" style="margin-bottom:10px" />
          <el-input-number v-model="riskPrice" :precision="2" :min="0" placeholder="价格" />
          <el-button type="warning" :loading="riskLoading" @click="handleRiskCheck" style="margin-left:10px">检测风险</el-button>
          <el-alert v-if="riskResult" :title="'风险等级: ' + riskResult.riskLevel" :type="riskResult.riskLevel === '低风险' ? 'success' : 'warning'" show-icon style="margin-top:10px">
            <template #default>
              <div v-for="(w, i) in riskResult.warnings" :key="i">- {{ w }}</div>
              <div style="margin-top:5px"><strong>建议</strong>{{ riskResult.suggestion }}</div>
            </template>
          </el-alert>
        </el-card>

        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon size="20"><Coin /></el-icon>
              <span>AI 估价建议</span>
            </div>
          </template>
          <el-select v-model="priceCategory" placeholder="选择分类" style="width:100%;margin-bottom:10px">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.name" />
          </el-select>
          <el-select v-model="priceCondition" placeholder="选择成色" style="width:100%;margin-bottom:10px">
            <el-option label="全新" value="全新" />
            <el-option label="几乎全新" value="几乎全新" />
            <el-option label="轻微使用痕迹" value="轻微使用痕迹" />
            <el-option label="明显使用痕迹" value="明显使用痕迹" />
            <el-option label="其他" value="其他" />
          </el-select>
          <el-input-number v-model="priceOriginal" :precision="2" :min="0" placeholder="购买时的价格" style="width:200px" />
          <el-button type="primary" :loading="priceLoading" @click="handlePriceCheck" style="margin-left:10px">获取估价</el-button>
          <el-card v-if="priceSuggestion" shadow="never" style="margin-top:10px;background:#f0f9eb">
            <p>建议售价: <strong style="color:#e6a23c;font-size:22px">￥{{ priceSuggestion.suggestedPrice }}</strong></p>
            <p>参考范围: {{ priceSuggestion.priceRange }}</p>
            <p style="color:#909399;font-size:12px;margin-top:5px">{{ priceSuggestion.referenceNote }}</p>
          </el-card>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from "vue";
import { ElMessage } from "element-plus";
import { getCategoryList } from "../api/category";
import { customerService, riskCheck, getPriceSuggestion } from "../api/ai";

const chatBox = ref(null);
const chatMessages = ref([
  { role: "ai", content: "您好！我是校园二手交易平台的AI助手，请问有什么可以帮助您的？" }
]);
const chatQuestion = ref("");
const chatLoading = ref(false);

const riskTitle = ref("");
const riskDesc = ref("");
const riskPrice = ref(0);
const riskLoading = ref(false);
const riskResult = ref(null);

const categories = ref([]);
const priceCategory = ref("");
const priceCondition = ref("");
const priceOriginal = ref(0);
const priceLoading = ref(false);
const priceSuggestion = ref(null);

onMounted(async () => {
  try {
    const res = await getCategoryList();
    categories.value = res.data || [];
  } catch (e) {}
});

async function handleAsk() {
  if (!chatQuestion.value.trim()) return;
  const q = chatQuestion.value;
  chatMessages.value.push({ role: "user", content: q });
  chatQuestion.value = "";
  chatLoading.value = true;
  try {
    const res = await customerService(q);
    setTimeout(() => {
      chatMessages.value.push({ role: "ai", content: res.data.answer, isHtml: true });
      nextTick(() => { chatBox.value.scrollTop = chatBox.value.scrollHeight; });
    }, 300);
  } catch (e) {
    chatMessages.value.push({ role: "ai", content: "抱歉，AI服务暂时不可用" });
  }
  finally { chatLoading.value = false; }
}

function askQuick(q) {
  chatQuestion.value = q;
  handleAsk();
}

async function handleRiskCheck() {
  riskLoading.value = true;
  try {
    const res = await riskCheck({ title: riskTitle.value, description: riskDesc.value, price: riskPrice.value });
    riskResult.value = res.data;
  } catch (e) {}
  finally { riskLoading.value = false; }
}

async function handlePriceCheck() {
  priceLoading.value = true;
  try {
    const res = await getPriceSuggestion({
      category: priceCategory.value,
      condition: priceCondition.value,
      originalPrice: priceOriginal.value
    });
    priceSuggestion.value = res.data;
  } catch (e) {}
  finally { priceLoading.value = false; }
}
</script>

<style scoped>
.ai-page { max-width: 1100px; margin: 0 auto; }
.ai-page h2 { font-size: 22px; color: var(--el-text-color-primary); }
.subtitle { color: var(--el-text-color-secondary); margin-top: 5px; font-size: 14px; }
.card-header { display: flex; align-items: center; gap: 8px; font-weight: 600; }
.chat-box { height: 250px; overflow-y: auto; margin-bottom: 10px; padding: 14px; background: var(--el-fill-color); border-radius: var(--el-border-radius-base); border: 1px solid var(--el-border-color-lighter); }
.chat-msg { margin-bottom: 12px; }
.chat-user { text-align: right; }
.chat-ai { text-align: left; }
.msg-content { display: inline-block; padding: 10px 16px; border-radius: 14px; max-width: 80%; white-space: pre-wrap; background: var(--el-bg-color); border: 1px solid var(--el-border-color-light); color: var(--el-text-color-primary); }
.chat-user .msg-content { background: var(--el-color-primary); color: #fff; border-color: var(--el-color-primary); }
.msg-time { font-size: 11px; color: var(--el-text-color-placeholder); margin-top: 4px; }
.quick-questions { display: flex; gap: 8px; flex-wrap: wrap; margin-top: 10px; }
</style>
