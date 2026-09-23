import request from "./request";
export const sendMessage = (params) => request.post("/message", null, { params });
export const getProductMessages = (productId, params) => request.get("/message/" + productId, { params });
export const deleteMessage = (id) => request.delete("/message/" + id);
