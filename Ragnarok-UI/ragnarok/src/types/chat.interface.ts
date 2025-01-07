export interface Message {
  sender: string;
  text: string;
  timestamp: number;
}

export interface Chat {
  id: string;
  title: string;
  messages: Message[];
  lastUpdated: number;
}
