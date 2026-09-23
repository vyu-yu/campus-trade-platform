<template>
  <div class="admin-layout">
    <aside class="admin-sidebar">
      <div class="sidebar-header">
        <el-icon :size="24"><Setting /></el-icon>
        <span>管理后台</span>
      </div>
      <el-menu :default-active="activeMenu" router class="sidebar-menu">
        <el-menu-item index="/admin/dashboard">
          <el-icon><DataAnalysis /></el-icon><span>仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon><span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/products">
          <el-icon><Goods /></el-icon><span>商品管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/orders">
          <el-icon><List /></el-icon><span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/reports">
          <el-icon><WarningFilled /></el-icon><span>举报管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/categories">
          <el-icon><FolderOpened /></el-icon><span>分类管理</span>
        </el-menu-item>
      </el-menu>
      <div class="sidebar-footer">
        <el-button text @click="goHome">
          <el-icon><Back /></el-icon>返回前台
        </el-button>
      </div>
    </aside>
    <main class="admin-main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { computed } from "vue";
import { useRouter, useRoute } from "vue-router";

const router = useRouter();
const route = useRoute();
const activeMenu = computed(() => route.path);

function goHome() {
  router.push("/home");
}
</script>

<style scoped>
.admin-layout { display: flex; min-height: calc(100vh - 64px); }
.admin-sidebar {
  width: 220px;
  background: linear-gradient(180deg, #1F2937 0%, #111827 100%);
  color: #fff;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  position: relative;
  overflow: hidden;
}
/* Subtle decorative gradient spot */
.admin-sidebar::before {
  content: "";
  position: absolute;
  top: -80px;
  right: -80px;
  width: 240px;
  height: 240px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(5, 150, 105, 0.12) 0%, transparent 70%);
  pointer-events: none;
}
.sidebar-header {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  border-bottom: 1px solid rgba(255,255,255,.1);
  color: var(--el-color-primary-light-5);
  position: relative;
  z-index: 1;
}
.sidebar-menu {
  flex: 1;
  border-right: none;
  background: transparent;
  position: relative;
  z-index: 1;
}
/* Reduce Element Plus menu default padding */
.sidebar-menu .el-menu-item {
  color: rgba(255,255,255,.7);
  padding-left: 24px !important;
}
.sidebar-menu .el-menu-item:hover,
.sidebar-menu .el-menu-item.is-active {
  color: #fff;
}
.sidebar-menu .el-menu-item.is-active {
  color: var(--el-color-primary-light-5);
  background: rgba(5,150,105,.15);
  border-right: 3px solid var(--el-color-primary-light-5);
}
.sidebar-footer {
  padding: 12px;
  border-top: 1px solid rgba(255,255,255,.1);
  text-align: center;
  position: relative;
  z-index: 1;
}
.sidebar-footer .el-button {
  color: rgba(255,255,255,.6);
}
.admin-main { flex: 1; padding: 24px; background: var(--el-bg-color-page); overflow-y: auto; }
</style>
