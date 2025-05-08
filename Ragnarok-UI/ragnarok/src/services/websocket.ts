import { chatStore } from "@/lib/stores/chatStore";

export class ChatWebSocket {
  private static instance: ChatWebSocket | null = null;
  private static isReconnecting = false;

  private socket: WebSocket;
  private messageQueue: string[] = [];
  private isSocketOpen = false;

  private constructor() {
    console.log("CHAT-WEBSOCKET");

    this.socket = new WebSocket(`ws://${process.env.NEXT_PUBLIC_RAGNAROK_UI_URL}/ws/chat`);

    this.socket.onopen = () => {
      console.log("WebSocket connected");
      this.isSocketOpen = true;
      ChatWebSocket.isReconnecting = false; 
      chatStore.getState().setIsconnected(true);

      this.messageQueue.forEach((msg) => this.socket.send(msg));
      this.messageQueue = [];
    };

    this.socket.onclose = () => {
      console.log("WebSocket disconnected");

      this.isSocketOpen = false;
      chatStore.getState().setIsconnected(false);

      if (!ChatWebSocket.isReconnecting) {
        ChatWebSocket.isReconnecting = true;
        setTimeout(() => {
          ChatWebSocket.instance = null;
          ChatWebSocket.getInstance(); 
        }, 2500); 
      }
    };

    this.socket.onerror = (error) => {
      console.error("WebSocket error:", error);
    };
  }

  static getInstance(): ChatWebSocket {
    if (!ChatWebSocket.instance) {
      ChatWebSocket.instance = new ChatWebSocket();
    } else {
      const state = ChatWebSocket.instance.socket.readyState;
      if (state !== WebSocket.OPEN && state !== WebSocket.CONNECTING) {
        console.warn("WebSocket not open, reconnecting...");
        ChatWebSocket.instance.closeWS();
        ChatWebSocket.instance = new ChatWebSocket();
      }
    }
    return ChatWebSocket.instance;
  }

  sendMessage(message: string) {
    if (this.isSocketOpen && this.socket.readyState === WebSocket.OPEN) {
      this.socket.send(message);
    } else {
      console.warn("WebSocket is not open. Queuing message.");
      this.messageQueue.push(message);
    }
  }

  closeWS() {
    if (this.socket.readyState === WebSocket.OPEN || this.socket.readyState === WebSocket.CONNECTING) {
      this.socket.close();
    }
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  onMessage(callback: (data: any) => void) {
    this.socket.onmessage = (event) => callback(event.data);
  }
}