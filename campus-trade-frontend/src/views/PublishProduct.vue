<template>
  <div class="publish-page">
    <el-card shadow="never">
      <h2>{{ editMode ? "编辑商品" : "发布新商品" }}</h2>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" style="max-width:800px;margin-top:20px">
        <el-form-item label="商品标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入商品标题" />
          <el-button size="small" type="info" style="margin-left:10px" @click="handleGenerateTitle">AI生成</el-button>
        </el-form-item>
        <el-form-item label="商品分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width:200px">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="5" placeholder="请详细描述商品的使用情况、功能状况等" />
          <el-button size="small" type="info" style="margin-top:5px" @click="handleOptimizeDesc">AI生成</el-button>
        </el-form-item>
        <el-form-item label="原价" prop="originalPrice">
          <el-input-number v-model="form.originalPrice" :precision="2" :min="0" :step="50" style="width:200px" />
          <span style="color:#909399;font-size:12px;margin-left:8px">您购买时的价格</span>
        </el-form-item>
        <el-form-item label="成色" prop="condition">
          <el-select v-model="form.condition" placeholder="请选择成色" style="width:200px">
            <el-option label="全新" value="全新" />
            <el-option label="几乎全新" value="几乎全新" />
            <el-option label="轻微使用痕迹" value="轻微使用痕迹" />
            <el-option label="明显使用痕迹" value="明显使用痕迹" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="售价" prop="price">
          <el-input-number v-model="form.price" :precision="2" :min="0.01" :step="10" style="width:200px" />
          <el-button size="small" type="warning" style="margin-left:10px" @click="handlePriceSuggestion">AI估价</el-button>
          <span style="color:#909399;font-size:12px;margin-left:8px">由AI根据原价和成色估算</span>
        </el-form-item>
        <el-form-item label="商品图片" prop="images">
          <div style="display:flex;flex-wrap:wrap;gap:10px;margin-bottom:10px">
            <div v-for="(img, idx) in imageList" :key="idx" style="position:relative;width:100px;height:100px">
              <el-image :src="img" fit="cover" style="width:100%;height:100%;border-radius:6px;border:1px solid #dcdfe6" />
              <el-button size="small" type="danger" circle
                style="position:absolute;top:-8px;right:-8px;width:22px;height:22px;min-height:22px"
                @click="removeImage(idx)">
                <el-icon><Close /></el-icon>
              </el-button>
            </div>
            <input
              type="file"
              accept="image/jpeg,image/png,image/webp"
              style="display:none"
              ref="fileInputEl"
              @change="handleFileSelect">
            <el-button type="primary" size="small" @click="chooseFile">
              <el-icon><Plus /></el-icon>
              <span style="margin-left:4px">选择图片</span>
            </el-button>
          <div style="width:100%;color:#909399;font-size:12px;padding-top:12px;line-height:1.6">支持 jpg/png/webp，单张不超过 5MB，建议尺寸 800x800</div>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            {{ editMode ? "保存修改" : "发布商品" }}
          </el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>

      <!-- AI Price Suggestion Dialog -->
      <el-dialog v-model="showPriceDialog" title="AI估价建议" width="420px">
        <div v-if="priceData">
          <div style="background:#f5f7fa;padding:12px;border-radius:8px;margin-bottom:16px;font-size:13px;color:#606266">
            <span>原价 <strong>￥{{ form.originalPrice }}</strong></span>
            <span style="margin:0 12px">|</span>
            <span>成色 <strong>{{ form.condition }}</strong></span>
            <span style="margin:0 12px">|</span>
            <span>分类 <strong>{{ priceCategoryLabel }}</strong></span>
          </div>
          <p style="font-size:14px;color:#606266;margin-bottom:6px">建议售价</p>
          <p style="margin:0 0 8px 0"><strong style="color:#e6a23c;font-size:28px">￥{{ priceData.suggestedPrice }}</strong></p>
          <p style="color:#909399;font-size:13px">参考范围: {{ priceData.priceRange }}</p>
          <p style="color:#909399;font-size:12px;margin-top:10px;border-top:1px solid #ebeef5;padding-top:10px">{{ priceData.referenceNote }}</p>
          <div style="margin-top:18px;display:flex;gap:10px">
            <el-button type="primary" @click="form.price = priceData.suggestedPrice; showPriceDialog = false">采纳此价格</el-button>
            <el-button @click="showPriceDialog = false">自行填写</el-button>
          </div>
        </div>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import { publishProduct, updateProduct, getProductDetail } from "../api/product";
import { getCategoryList } from "../api/category";
import { generateTitle, optimizeDescription, getPriceSuggestion } from "../api/ai";
import request from "../api/request";

const router = useRouter();
const route = useRoute();
const formRef = ref(null);
const categories = ref([]);
const submitting = ref(false);
const editMode = ref(false);
const productId = ref(null);
const showPriceDialog = ref(false);
const priceData = ref(null);
const priceCategoryLabel = ref("未指定");
const imageList = ref([]);
const form = reactive({
  title: "", description: "", price: null, originalPrice: null,
  categoryId: null, condition: "", images: ""
});

const rules = {
  title: [{ required: true, message: "请输入商品标题", trigger: "blur" }],
  categoryId: [{ required: true, message: "请选择商品分类", trigger: "change" }],
  description: [{ required: true, message: "请输入商品描述", trigger: "blur" }],
  originalPrice: [{ required: true, message: "请输入购买时的原价", trigger: "blur" }],
  condition: [{ required: true, message: "请选择商品成色", trigger: "change" }],
  price: [{ required: true, message: "请输入商品售价", trigger: "blur" }],
};

onMounted(async () => {
  try {
    const res = await getCategoryList();
    categories.value = res.data || [];
  } catch (e) {}
  
  if (route.query.edit) {
    editMode.value = true;
    productId.value = route.query.edit;
    try {
      const res = await getProductDetail(productId.value);
      const p = res.data;
      form.title = p.title || "";
      form.description = p.description || "";
      form.price = p.price;
      form.originalPrice = p.originalPrice;
      form.categoryId = p.categoryId;
      form.condition = p.condition || "";
      form.images = p.images || "";
      imageList.value = form.images ? form.images.split(",").filter(Boolean) : [];
    } catch (e) { ElMessage.error("加载商品信息失败"); }
  }
});

async function handleGenerateTitle() {
  if (!form.description && !form.categoryId) {
    return ElMessage.warning("请先填写商品描述或选择分类");
  }
  try {
    const cat = categories.value.find(c => c.id === form.categoryId);
    const res = await generateTitle({ description: form.description, category: cat?.name });
    form.title = res.data.title;
    ElMessage.success("AI生成成功");
  } catch (e) {}
}

async function handleOptimizeDesc() {
  if (!form.description) return ElMessage.warning("请先填写商品描述");
  try {
    const res = await optimizeDescription({ description: form.description, condition: form.condition });
    form.description = res.data.description;
    ElMessage.success("AI生成成功");
  } catch (e) {}
}

function handleUploadSuccess(res, file) {
  if (res && res.code === 200) {
    file.url = res.data;
    imageList.value.push(res.data);
    form.images = imageList.value.join(",");
  } else {
    ElMessage.error(res?.message || "上传失败");
  }
}


const fileInputEl = ref(null)

function chooseFile() {
  fileInputEl.value?.click()
}

async function handleFileSelect(e) {
  const file = e.target.files[0];
  if (!file) return;
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error("图片大小不能超过 5MB");
    e.target.value = "";
    return;
  }
  const formData = new FormData();
  formData.append("file", file);
  try {
    const res = await request.post("/file/upload", formData);
    if (res.code === 200) {
      imageList.value.push(res.data);
      form.images = imageList.value.join(",");
      e.target.value = "";
    } else {
      ElMessage.error(res?.message || "上传失败");
    }
  } catch (e) {
    ElMessage.error("上传失败");
  }
}

function beforeUpload(file) {
  const validTypes = ["image/jpeg", "image/png", "image/webp"];
  if (!validTypes.includes(file.type)) {
    ElMessage.error("仅支持 jpg/png/webp 格式");
    return false;
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error("图片大小不能超过 5MB");
    return false;
  }
  return true;
}

function removeImage(idx) {
  imageList.value.splice(idx, 1);
  form.images = imageList.value.join(",");
}

async function handlePriceSuggestion() {
  if (!form.originalPrice || form.originalPrice <= 0) {
    return ElMessage.warning("请先填写购买时的原价");
  }
  if (!form.condition) {
    return ElMessage.warning("请选择商品成色");
  }
  try {
    const cat = categories.value.find(c => c.id === form.categoryId);
    priceCategoryLabel.value = cat?.name || "未指定";
    const res = await getPriceSuggestion({
      category: cat?.name,
      condition: form.condition,
      originalPrice: form.originalPrice
    });
    priceData.value = res.data;
    showPriceDialog.value = true;
  } catch (e) {}
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => {});
  if (!valid) return;
  submitting.value = true;
  try {
    if (editMode.value) {
      await updateProduct(productId.value, form);
      ElMessage.success("修改成功");
    } else {
      await publishProduct(form);
      ElMessage.success("发布成功");
    }
    router.push("/my/products");
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || "提交失败，请稍后重试");
  }
  finally { submitting.value = false; }
}
</script>

<style scoped>
.publish-page { max-width: 900px; margin: 0 auto; }
.publish-page h2 { font-size: 22px; color: var(--el-text-color-primary); }
</style>
