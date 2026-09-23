<template>
  <div class="admin-page">
    <h2>分类管理</h2>
    <div class="toolbar">
      <el-input v-model="newName" placeholder="新分类名称" style="width:200px" />
      <el-input-number v-model="newSort" :min="1" :max="99" placeholder="排序" />
      <el-button type="primary" @click="addCategory">新增分类</el-button>
    </div>
    <el-table :data="categories" v-loading="loading" stripe style="width:100%;margin-top:15px">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="名称">
        <template #default="{ row, $index }">
          <el-input v-if="editingIndex === $index" v-model="editName" size="small" style="width:150px" />
          <span v-else>{{ row.name }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="80">
        <template #default="{ row, $index }">
          <el-input-number v-if="editingIndex === $index" v-model="editSort" :min="1" :max="99" size="small" />
          <span v-else>{{ row.sort }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row, $index }">
          <template v-if="editingIndex === $index">
            <el-button size="small" type="primary" @click="saveEdit(row)">保存</el-button>
            <el-button size="small" @click="editingIndex = -1">取消</el-button>
          </template>
          <template v-else>
            <el-button size="small" @click="startEdit($index, row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { getCategoryList } from "../api/category";
import request from "../api/request";

const categories = ref([]);
const loading = ref(false);
const editingIndex = ref(-1);
const editName = ref("");
const editSort = ref(1);
const newName = ref("");
const newSort = ref(1);

onMounted(() => fetchData());

async function fetchData() {
  loading.value = true;
  try {
    const res = await getCategoryList();
    categories.value = res.data || [];
  } catch (e) {}
  finally { loading.value = false; }
}

function startEdit(index, row) {
  editingIndex.value = index;
  editName.value = row.name;
  editSort.value = row.sort;
}

async function saveEdit(row) {
  try {
    await request.put("/admin/categories/" + row.id, { name: editName.value, sort: editSort.value });
    ElMessage.success("修改成功");
    editingIndex.value = -1;
    await fetchData();
  } catch (e) {}
}

async function addCategory() {
  if (!newName.value.trim()) return ElMessage.warning("请输入分类名称");
  try {
    await request.post("/admin/categories", { name: newName.value, sort: newSort.value });
    ElMessage.success("新增成功");
    newName.value = "";
    newSort.value = 1;
    await fetchData();
  } catch (e) {}
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm("确定删除该分类？", "警告", { type: "warning" });
    await request.delete("/admin/categories/" + row.id);
    ElMessage.success("已删除");
    await fetchData();
  } catch (e) {}
}
</script>

<style scoped>
.admin-page h2 { font-size: 22px; color: var(--el-text-color-primary); }
.toolbar { display: flex; gap: 10px; margin-top: 15px; align-items: center; flex-wrap: wrap; }
</style>
