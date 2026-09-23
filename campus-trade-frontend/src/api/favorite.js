import request from "./request";
export const addFavorite = (productId) => request.post("/favorite", null, { params: { productId } });
export const removeFavorite = (productId) => request.delete("/favorite/" + productId);
export const getMyFavorites = (params) => request.get("/favorite/list", { params });
