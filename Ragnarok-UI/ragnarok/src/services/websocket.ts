import { chatStore } from "@/lib/stores/chatStore";

export class ChatWebSocket {
  private static instance: ChatWebSocket | null = null;
  private static isReconnecting = false;

  private socket: WebSocket;
  private messageQueue: string[] = [];
  private isSocketOpen = false;

  private constructor() {
    const url = `wss://ragnarok-be.dyn.cloud.e-infra.cz/ws/chat`;
    console.log("Connecting to WebSocket:", url);

    this.socket = new WebSocket(url);

    this.socket.onopen = () => {
      console.log("✅ WebSocket connected");
      this.isSocketOpen = true;
      ChatWebSocket.isReconnecting = false;
      chatStore.getState().setIsconnected(true);

      this.messageQueue.forEach((msg) => this.socket.send(msg));
      this.messageQueue = [];
    };

    this.socket.onclose = () => {
      console.log("❌ WebSocket disconnected");
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
      console.error("⚠️ WebSocket error:", error);
    };
  }

  static getInstance(): ChatWebSocket {
    if (!ChatWebSocket.instance) {
      ChatWebSocket.instance = new ChatWebSocket();
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

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  onMessage(callback: (data: any) => void) {
    this.socket.onmessage = (event) => callback(event.data);
  }

}