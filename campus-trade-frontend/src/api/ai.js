import request from "./request";
export const generateTitle = (params) => request.post("/ai/generate-title", null, { params });
export const optimizeDescription = (params) => request.post("/ai/optimize-description", null, { params });
export const getPriceSuggestion = (params) => request.post("/ai/price-suggestion", null, { params });
export const customerService = (question) => request.post("/ai/customer-service", null, { params: { question } });
export const riskCheck = (params) => request.post("/ai/risk-check", null, { params });
