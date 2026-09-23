import request from "./request";
export const submitReport = (data) => request.post("/report", data);
export const getReportList = (params) => request.get("/report/list", { params });
export const handleReport = (id, status) => request.put("/report/" + id + "/status", null, { params: { status } });
