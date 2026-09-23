import request from "./request";
export const getOrCreateConversation = (data) => request.post("/chat/conversation", data);
export const sendChatMessage = (conversationId, content) => request.post("/chat/send", null, { params: { conversationId, content } });
export const getConversations = () => request.get("/chat/conversations");
export const getChatMessages = (conversationId, params) => request.get("/chat/messages/" + conversationId, { params });
export const markConversationRead = (conversationId) => request.put("/chat/read/" + conversationId);
export const getUnreadCount = () => request.get("/chat/unread");
