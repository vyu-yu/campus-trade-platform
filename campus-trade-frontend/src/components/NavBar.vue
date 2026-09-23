<template>
  <header class="navbar">
    <div class="nav-inner">
      <router-link to="/" class="logo">
        <el-icon :size="24"><ShoppingBag /></el-icon>
        <span class="logo-text">校园二手交易</span>
      </router-link>
      <div class="nav-links">
        <router-link to="/" class="nav-link">首页</router-link>
        <router-link v-if="userStore.isLoggedIn" to="/publish" class="nav-link publish-link">
          <el-icon><Plus /></el-icon>发布商品
        </router-link>
        <router-link to="/ai-assistant" class="nav-link">
          <el-icon><MagicStick /></el-icon>AI小助手
        </router-link>
      </div>
      <div class="nav-user">
        <template v-if="userStore.isLoggedIn">
          <!-- 消息通知 -->
          <router-link to="/messages" class="msg-btn" title="消息中心">
            <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
              <el-icon :size="22"><Message /></el-icon>
            </el-badge>
          </router-link>
          <el-dropdown trigger="click">
            <span class="user-info">
              <el-avatar :size="32" icon="UserFilled" />
              <span class="username">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="$router.push('/my/products')">
                  <el-icon><Goods /></el-icon>我的商品
                </el-dropdown-item>
                <el-dropdown-item @click="$router.push('/my/favorites')">
                  <el-icon><Star /></el-icon>我的收藏
                </el-dropdown-item>
                <el-dropdown-item @click="$router.push('/my/transactions')">
                  <el-icon><List /></el-icon>我的交易
                </el-dropdown-item>
                <el-dropdown-item v-if="userStore.isAdmin" @click="$router.push('/admin/dashboard')" divided>
                  <el-icon><Setting /></el-icon>后台管理
                </el-dropdown-item>
                <el-dropdown-item divided @click="$router.push('/profile')">
                  <el-icon><User /></el-icon>个人主页
                </el-dropdown-item>
                                
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button size="small" @click="$router.push('/login')">登录</el-button>
          <el-button size="small" type="primary" @click="$router.push('/register')">注册</el-button>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from "vue";
import { useRouter } from "vue-router";
import { useUserStore } from "../stores/user";
import { Message } from "@element-plus/icons-vue";
import { getUnreadCount } from "../api/chat";
const router = useRouter();
const userStore = useUserStore();

const unreadCount = ref(0);
let pollTimer = null;

async function fetchUnread() {
  if (!userStore.isLoggedIn) { unreadCount.value = 0; return; }
  try {
    const res = await getUnreadCount();
    unreadCount.value = res.data?.total || 0;
  } catch (e) { /* ignore */ }
}

onMounted(() => {
  if (userStore.isLoggedIn) {
    fetchUnread();
    pollTimer = setInterval(fetchUnread, 30000);
  }
});

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer);
});

function handleLogout() {
  userStore.logout();
  router.push("/login");
}
</script>

<style scoped>
.navbar {
  background: var(--el-bg-color);
  border-bottom: 1px solid var(--el-border-color-light);
  box-shadow: 0 1px 4px rgba(0,0,0,.04);
  position: sticky;
  top: 0;
  z-index: 100;
}
.nav-inner {
  max-width: 1200px;
  margin: 0 auto;
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 24px;
  gap: 32px;
}
.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  color: var(--el-color-primary);
  font-size: 20px;
  font-weight: 700;
  letter-spacing: -0.3px;
}
.logo .logo-text {
  color: var(--el-text-color-primary);
  font-weight: 600;
}
.nav-links {
  display: flex;
  gap: 24px;
  flex: 1;
}
.nav-link {
  text-decoration: none;
  color: var(--el-text-color-secondary);
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 500;
  padding: 6px 0;
  border-bottom: 2px solid transparent;
  transition: color 0.2s, border-color 0.2s;
}
.nav-link:hover { color: var(--el-color-primary); border-bottom-color: var(--el-color-primary); }
.nav-link.router-link-active { color: var(--el-color-primary); border-bottom-color: var(--el-color-primary); }
.publish-link {
  color: var(--el-color-primary);
  font-weight: 600;
}
.nav-user { display: flex; align-items: center; gap: 12px; }
.msg-btn {
  text-decoration: none;
  color: var(--el-text-color-secondary);
  display: flex;
  align-items: center;
  padding: 6px 8px;
  border-radius: var(--el-border-radius-base);
  transition: all 0.2s;
}
.msg-btn:hover { color: var(--el-color-primary); background: var(--el-color-primary-light-9); }
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 10px;
  border-radius: var(--el-border-radius-base);
  transition: background 0.2s;
}
.user-info:hover { background: var(--el-color-primary-light-9); }
.username { font-size: 14px; font-weight: 500; color: var(--el-text-color-primary); }
</style>
