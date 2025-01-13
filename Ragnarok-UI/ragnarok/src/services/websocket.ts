export class ChatWebSocket {
  private static instance: ChatWebSocket;
  private socket: WebSocket;

  // ToDo: connect to real WebSocket server
  private constructor() {
    this.socket = new WebSocket("ws://localhost:3000/ws/chat");
    this.socket.onopen = () => console.log("WebSocket connected");
    this.socket.onclose = () => console.log("WebSocket disconnected");
    this.socket.onerror = (error) => console.error("WebSocket error:", error);
  }

  static getInstance() {
    if (!ChatWebSocket.instance) {
      ChatWebSocket.instance = new ChatWebSocket();
    }
    return ChatWebSocket.instance;
  }

  sendMessage(message: string) {
    this.socket.send(message as string);
  }

  // ToDo: define data type for callback
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  onMessage(callback: (data: any) => void) {
    this.socket.onmessage = (event) => callback(event.data);
  }
}
