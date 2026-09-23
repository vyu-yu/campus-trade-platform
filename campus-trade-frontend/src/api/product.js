import request from "./request";
export const publishProduct = (data) => request.post("/product/publish", data);
export const updateProduct = (id, data) => request.put("/product/" + id, data);
export const deleteProduct = (id) => request.delete("/product/" + id);
export const getProductDetail = (id) => request.get("/product/" + id);
export const getProductList = (params) => request.get("/product/list", { params });
export const getMyProducts = (params) => request.get("/product/my", { params });
export const updateProductStatus = (id, status) => request.put("/product/" + id + "/status", null, { params: { status } });
