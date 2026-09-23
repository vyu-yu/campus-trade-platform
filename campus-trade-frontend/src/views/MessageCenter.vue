<template>
  <div class="msg-page">
    <h2>消息中心</h2>
    <el-row :gutter="20" style="margin-top:15px">
      <!-- Conversation List -->
      <el-col :span="8">
        <el-card shadow="never" style="min-height:500px">
          <template #header><span>对话列表</span></template>
          <div v-if="conversations.length === 0" style="text-align:center;padding:30px;color:#909399">暂无对话</div>
          <div v-for="conv in conversations" :key="conv.id" class="conv-item"
            :class="{ active: currentConv === conv.id }" @click="selectConversation(conv)">
            <div style="display:flex;align-items:center;gap:8px">
              <el-avatar :size="36" icon="UserFilled" />
              <div style="flex:1;min-width:0">
                <div style="font-weight:500;font-size:13px">{{ conv.otherNickname }}
                  <el-tag v-if="conv.unread > 0" size="small" type="danger" style="margin-left:4px">{{ conv.unread }}</el-tag>
                </div>
                <div style="font-size:12px;color:#909399;white-space:nowrap;overflow:hidden;text-overflow:ellipsis">{{ conv.lastMessage || "暂无消息" }}</div>
              </div>
              <div style="font-size:11px;color:#c0c4cc;flex-shrink:0">{{ formatTime(conv.lastTime) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- Chat Area -->
      <el-col :span="16">
        <el-card shadow="never" style="min-height:500px;display:flex;flex-direction:column">
          <template #header v-if="currentConvData">
            <span>与 {{ currentConvData.otherNickname }} 的对话</span>
          </template>
          <template #header v-else>
            <span style="color:#909399">选择一个对话开始聊天</span>
          </template>

          <div v-if="currentConvData" style="flex:1;display:flex;flex-direction:column">
            <div ref="msgBoxRef" style="flex:1;height:350px;overflow-y:auto;padding:10px;background:#f5f7fa;border-radius:8px;margin-bottom:10px">
              <div v-for="msg in messages" :key="msg.id" :style="{textAlign: msg.senderId === userId ? 'right' : 'left', marginBottom:'14px'}">
                <div style="font-size:11px;color:#909399;margin-bottom:3px">{{ msg.senderId === userId ? '我' : currentConvData.otherNickname }}</div>
                <div :style="{display:'inline-block',padding:'8px 16px',borderRadius:'14px',maxWidth:'75%',background:msg.senderId === userId ? '#409eff' : '#fff',color:msg.senderId === userId ? '#fff' : '#303133',textAlign:'left',lineHeight:'1.5',fontSize:'14px'}">
                  {{ msg.content }}
                </div>
                <div style="font-size:10px;color:#c0c4cc;margin-top:2px">{{ msg.createTime }}</div>
              </div>
            </div>
            <div style="display:flex;gap:8px">
              <el-input v-model="chatInput" type="textarea" :rows="2" placeholder="输入消息..." @keyup.enter.ctrl="sendMsg" />
              <el-button type="primary" @click="sendMsg" style="align-self:flex-end">发送</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, computed } from "vue";
import { useUserStore } from "../stores/user";
import { ElMessage } from "element-plus";
import { getConversations, getChatMessages, sendChatMessage, markConversationRead, getUnreadCount } from "../api/chat";

const userStore = useUserStore();
const userId = userStore.userInfo?.id;
const conversations = ref([]);
const currentConv = ref(null);
const messages = ref([]);
const chatInput = ref("");
const msgBoxRef = ref(null);

const currentConvData = computed(() => conversations.value.find(c => c.id === currentConv.value));

onMounted(async () => {
  await loadConversations();
});

async function loadConversations() {
  try {
    const res = await getConversations();
    conversations.value = res.data || [];
  } catch (e) {}
}

async function selectConversation(conv) {
  currentConv.value = conv.id;
  try {
    await markConversationRead(conv.id);
    conv.unread = 0;
    const res = await getChatMessages(conv.id, { pageSize: 100 });
    messages.value = res.data?.records || [];
    await nextTick();
    scrollToBottom();
  } catch (e) {}
}

async function sendMsg() {
  if (!chatInput.value.trim() || !currentConv.value) return;
  const text = chatInput.value;
  chatInput.value = "";
  try {
    await sendChatMessage(currentConv.value, text);
    await selectConversation(currentConvData.value);
  } catch (e) {
    ElMessage.error("发送失败");
  }
}

async function scrollToBottom() {
  if (msgBoxRef.value) {
    msgBoxRef.value.scrollTop = msgBoxRef.value.scrollHeight;
  }
}

function formatTime(t) {
  if (!t) return "";
  const d = new Date(t);
  const now = new Date();
  if (d.toDateString() === now.toDateString()) return d.toLocaleTimeString().slice(0, 5);
  return d.toLocaleDateString().slice(5);
}
</script>

<style scoped>
.msg-page { max-width: 1100px; margin: 0 auto; }
.msg-page h2 { font-size: 22px; color: var(--el-text-color-primary); }
.subtitle { color: var(--el-text-color-secondary); margin-top: 5px; }
.conv-item { padding: 12px; border-bottom: 1px solid var(--el-border-color-lighter); cursor: pointer; border-radius: var(--el-border-radius-base); transition: all 0.2s; }
.conv-item:hover { background: var(--el-fill-color); }
.conv-item.active { background: var(--el-color-primary-light-9); }
</style>
