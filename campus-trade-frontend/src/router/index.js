import { createRouter, createWebHistory } from "vue-router";
import { useUserStore } from "../stores/user";
import HomePage from "../views/HomePage.vue";
import Login from "../views/Login.vue";
import Register from "../views/Register.vue";
import ProductDetail from "../views/ProductDetail.vue";
import PublishProduct from "../views/PublishProduct.vue";
import MyProducts from "../views/MyProducts.vue";
import MyFavorites from "../views/MyFavorites.vue";
import Profile from "../views/Profile.vue";
import AiAssistant from "../views/AiAssistant.vue";

// Admin
import AdminLayout from "../views/AdminLayout.vue";
import AdminDashboard from "../views/AdminDashboard.vue";
import AdminUsers from "../views/AdminUsers.vue";
import AdminProducts from "../views/AdminProducts.vue";
import AdminOrders from "../views/AdminOrders.vue";
import AdminReports from "../views/AdminReports.vue";
import AdminCategories from "../views/AdminCategories.vue";

const routes = [
  { path: "/", redirect: "/login" },
  { path: "/login", name: "Login", component: Login },
  { path: "/register", name: "Register", component: Register },
  { path: "/home", name: "Home", component: HomePage, meta: { requiresAuth: true } },
  { path: "/product/:id", name: "ProductDetail", component: ProductDetail },
  { path: "/seller/:id", name: "SellerDetail", component: () => import("../views/SellerDetail.vue"), meta: { requiresAuth: false } },
  { path: "/publish", name: "Publish", component: PublishProduct, meta: { requiresAuth: true } },
  { path: "/my/products", name: "MyProducts", component: MyProducts, meta: { requiresAuth: true } },
  { path: "/my/favorites", name: "MyFavorites", component: MyFavorites, meta: { requiresAuth: true } },
   { path: "/my/transactions", name: "MyTransactions", component: () => import("../views/MyTransactions.vue"), meta: { requiresAuth: true } },
  { path: "/messages", name: "MessageCenter", component: () => import("../views/MessageCenter.vue"), meta: { requiresAuth: true } },
  { path: "/profile", name: "Profile", component: Profile, meta: { requiresAuth: true } },
  { path: "/ai-assistant", name: "AiAssistant", component: AiAssistant },

  // Admin routes with layout
  {
    path: "/admin",
    component: AdminLayout,
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      { path: "dashboard", name: "AdminDashboard", component: AdminDashboard },
      { path: "users", name: "AdminUsers", component: AdminUsers },
      { path: "products", name: "AdminProducts", component: AdminProducts },
      { path: "orders", name: "AdminOrders", component: AdminOrders },
      { path: "reports", name: "AdminReports", component: AdminReports },
      { path: "categories", name: "AdminCategories", component: AdminCategories },
      { path: "", redirect: "/admin/dashboard" },
    ],
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, from, next) => {
  const userStore = useUserStore();
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next("/login");
  } else if (to.meta.requiresAdmin && !userStore.isAdmin) {
    next("/home");
  } else if (to.path === "/login" && userStore.isLoggedIn) {
    next(userStore.isAdmin ? "/admin/dashboard" : "/home");
  } else {
    next();
  }
});

export default router;

