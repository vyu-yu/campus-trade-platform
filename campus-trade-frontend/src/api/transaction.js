import request from "./request";
export const createTransaction = (productId) => request.post("/transaction/create", null, { params: { productId } });
export const updateTransactionStatus = (id, status) => request.put("/transaction/" + id + "/status", null, { params: { status } });
export const getMyTransactions = (params) => request.get("/transaction/list", { params });
export const getTransactionDetail = (id) => request.get("/transaction/" + id);
