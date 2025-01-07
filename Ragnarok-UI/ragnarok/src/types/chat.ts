export interface Chat {
  id: number;
  title: string;
  messages: {
    sender: "user" | "ragnarok";
    content: string;
    timestamp: number;
  }[];
}
