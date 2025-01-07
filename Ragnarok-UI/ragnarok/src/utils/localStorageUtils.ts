import { Chat } from "../types/chat.interface";

export function saveChats(chats: Chat[]) {
  localStorage.setItem("chats", JSON.stringify(chats));
}

export function loadChats(): Chat[] {
  const data = localStorage.getItem("chats");
  return data ? JSON.parse(data) : [];
}

export function saveChatToSessionStorage(chat: Chat) {
  sessionStorage.setItem("tempChat", JSON.stringify(chat));
}

export function loadChatFromSessionStorage(): Chat | null {
  const data = sessionStorage.getItem("tempChat");
  return data ? JSON.parse(data) : null;
}

export function clearTempChat() {
  sessionStorage.removeItem("tempChat");
}
