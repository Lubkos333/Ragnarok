export interface Message {
  sender: string;
  text: string;
  timestamp: number;
  completed: boolean;
}

export interface Chat {
  id: string;
  title: string;
  messages: Message[];
  lastUpdated: number;
}
